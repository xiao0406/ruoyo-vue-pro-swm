import fs from 'node:fs/promises'
import path from 'node:path'
import { createRequire } from 'node:module'

const require = createRequire(import.meta.url)
const { chromium } = require('C:/Users/xy/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/.pnpm/playwright@1.61.1/node_modules/playwright')

const outputDir = path.resolve('migration-audit/browser')
const captchaFile = path.join(outputDir, 'captcha.txt')
const resultFile = path.join(outputDir, 'production-pages.json')
const authStateFile = path.join(outputDir, 'production-auth-state.json')
const loginUrl = 'http://58.250.244.169:15200/ssiip/'
const comparison = JSON.parse(await fs.readFile(path.join(outputDir, 'production-comparison.json'), 'utf8'))
const targets = comparison.map(item => ({ name: item.title, route: item.route }))
let previousAudit = {}
try { previousAudit = JSON.parse(await fs.readFile(resultFile, 'utf8')) } catch {}

await fs.mkdir(outputDir, { recursive: true })
await fs.rm(captchaFile, { force: true })
let hasAuthState = false
try { await fs.access(authStateFile); hasAuthState = true } catch {}

const browser = await chromium.launch({
  headless: true,
  executablePath: 'C:/Program Files/Google/Chrome/Application/chrome.exe'
})
const context = await browser.newContext({
  viewport: { width: 1600, height: 1000 },
  ...(hasAuthState ? { storageState: authStateFile } : {})
})
let page = await context.newPage()
const apiRequests = previousAudit.apiRequests || []
const apiResponses = previousAudit.apiResponses || []
const browserErrors = previousAudit.browserErrors || []
const bindAuditListeners = auditedPage => {
  auditedPage.on('request', async request => {
    if (['xhr', 'fetch'].includes(request.resourceType())) {
      apiRequests.push({
        at: Date.now(),
        method: request.method(),
        url: request.url(),
        postData: request.postData()
      })
    }
  })
  auditedPage.on('pageerror', error => browserErrors.push({ type: 'pageerror', message: error.message }))
  auditedPage.on('console', message => {
    if (message.type() === 'error') browserErrors.push({ type: 'console', message: message.text() })
  })
  auditedPage.on('response', async response => {
    const request = response.request()
    if (!['xhr', 'fetch'].includes(request.resourceType())) return
    let body = ''
    try { body = (await response.text()).slice(0, 200_000) } catch {}
    apiResponses.push({ at: Date.now(), status: response.status(), url: response.url(), body })
  })
}
bindAuditListeners(page)

try {
  await page.goto(loginUrl, { waitUntil: 'domcontentloaded', timeout: 45_000 })
  await page.waitForTimeout(2500)
  const inputs = page.locator('input:visible')
  if (await inputs.count() >= 3) {
    await inputs.nth(0).fill(process.env.PROD_USER || '')
    await inputs.nth(1).fill(process.env.PROD_PASSWORD || '')
    await page.screenshot({ path: path.join(outputDir, 'production-captcha.png'), fullPage: true })
    console.log('CAPTCHA_READY')

    let captcha = ''
    const deadline = Date.now() + 300_000
    while (!captcha && Date.now() < deadline) {
      try { captcha = (await fs.readFile(captchaFile, 'utf8')).trim() } catch {}
      if (!captcha) await new Promise(resolve => setTimeout(resolve, 500))
    }
    if (!captcha) throw new Error('Captcha input timed out')

    await inputs.nth(2).fill(captcha)
    await page.getByRole('button', { name: /\u767b\u5f55/ }).click()
    await page.waitForTimeout(5000)
    await context.storageState({ path: authStateFile })
  }

  const shieldText = '\u6167\u773c\u5b89\u76fe'
  const shieldCard = page.getByText(shieldText, { exact: true }).last()
  const selectionClick = { found: await shieldCard.count() > 0 }
  await fs.writeFile(path.join(outputDir, 'production-selection.json'), JSON.stringify({
    click: selectionClick,
    url: page.url(),
    bodyText: (await page.locator('body').innerText()).slice(0, 3000)
  }, null, 2), 'utf8')
  if (selectionClick.found) {
    const previousPages = new Set(context.pages())
    await shieldCard.click({ timeout: 5000 })
    await page.waitForTimeout(25_000)
    const openedPage = context.pages().find(item => !previousPages.has(item))
    if (openedPage) {
      page = openedPage
      bindAuditListeners(page)
      await page.waitForLoadState('domcontentloaded', { timeout: 45_000 }).catch(() => {})
      await page.waitForTimeout(5000)
    }
  }

  if (/\/login(?:$|[?#])/.test(page.url())) {
    throw new Error(`Failed to enter SWM subsystem; current URL is ${page.url()}`)
  }

  await page.screenshot({ path: path.join(outputDir, 'production-home.png'), fullPage: true })
  await page.getByRole('button', { name: '\u77e5\u9053\u4e86' }).click({ timeout: 3000 }).catch(() => {})

  const menuSelector = '.el-menu-item, .el-sub-menu__title, .el-submenu__title, .ant-menu-item, .ant-menu-submenu-title, [role="menuitem"]'
  const menu = await page.locator(menuSelector).evaluateAll(items => items.map(item => ({
    text: (item.innerText || item.textContent || '').trim().replace(/\s+/g, ' '),
    className: typeof item.className === 'string' ? item.className : ''
  })).filter(item => item.text))

  for (const selector of ['.el-sub-menu__title:visible', '.el-submenu__title:visible', '.ant-menu-submenu-title:visible']) {
    const items = page.locator(selector)
    for (let index = 0; index < Math.min(await items.count(), 40); index++) {
      try { await items.nth(index).click({ timeout: 1000 }) } catch {}
    }
  }
  await page.waitForTimeout(1000)

  const pages = previousAudit.pages || []
  const completedRoutes = new Set(pages.map(item => item.route))
  for (const target of targets.filter(item => !completedRoutes.has(item.route))) {
    const startedAt = Date.now()
    const targetUrl = new URL(`/ssiip${target.route}`, loginUrl).href
    let navigationError = ''
    try {
      await page.goto(targetUrl, { waitUntil: 'commit', timeout: 15_000 })
    } catch (error) {
      navigationError = error instanceof Error ? error.message : String(error)
    }
    await page.waitForLoadState('domcontentloaded', { timeout: 20_000 }).catch(() => {})
    await page.waitForTimeout(1200)
    const tableHeaders = await page.locator('th:visible').allInnerTexts().catch(() => [])
    const bodyText = await page.locator('body').innerText().catch(() => '')
    pages.push({
      name: target.name,
      route: target.route,
      found: !/\/login(?:$|[?#])/.test(page.url()),
      url: page.url(),
      title: await page.title(),
      navigationError,
      tableHeaders: tableHeaders.map(text => text.trim()).filter(Boolean),
      bodyText: bodyText.slice(0, 3000),
      requests: apiRequests.filter(request => request.at >= startedAt)
    })
    await fs.writeFile(resultFile, JSON.stringify({ menu, pages, apiRequests, apiResponses, browserErrors }, null, 2), 'utf8')
  }

  await fs.writeFile(resultFile, JSON.stringify({ menu, pages, apiRequests, apiResponses, browserErrors }, null, 2), 'utf8')
  console.log(JSON.stringify({ menuCount: menu.length, pageCount: pages.length, found: pages.filter(item => item.found).length }))
} finally {
  await context.close()
  await browser.close()
}
