import fs from 'node:fs/promises'
import path from 'node:path'
import { createRequire } from 'node:module'

const require = createRequire(import.meta.url)
const { chromium } = require('C:/Users/xy/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/.pnpm/playwright@1.61.1/node_modules/playwright')
const outputDir = path.resolve('migration-audit/browser')
const menuSql = await fs.readFile(path.resolve('sql/mysql/swm_full_menu_fix.sql'), 'utf8')
const targets = menuSql.split('\n').flatMap(line => {
  if (!line.trim().startsWith('(901')) return []
  const values = [...line.matchAll(/'([^']*)'/g)].map(match => match[1])
  if (values.length < 4) return []
  const [group, title, , routePath] = values
  const groupPath = group === 'root' ? '' : `/${group}`
  return [{ title, route: `/swm${groupPath}/${routePath}` }]
})

const browser = await chromium.launch({
  headless: true,
  executablePath: 'C:/Program Files/Google/Chrome/Application/chrome.exe'
})
const context = await browser.newContext({ viewport: { width: 1600, height: 1000 } })
const page = await context.newPage()
const requests = []
const responses = []
page.on('request', request => {
  if (['xhr', 'fetch'].includes(request.resourceType())) {
    requests.push({ at: Date.now(), method: request.method(), url: request.url(), postData: request.postData() })
  }
})
page.on('response', async response => {
  if (!['xhr', 'fetch'].includes(response.request().resourceType())) return
  let body = ''
  try { body = (await response.text()).slice(0, 2000) } catch {}
  responses.push({ at: Date.now(), status: response.status(), url: response.url(), body })
})

try {
  await page.goto('http://localhost:83/', { waitUntil: 'domcontentloaded', timeout: 30_000 })
  await page.waitForTimeout(2000)
  const inputs = page.locator('input:visible')
  if (await page.locator('input[type="password"]:visible').count()) {
    const textInputs = page.locator('input[type="text"]:visible')
    await textInputs.last().fill(process.env.LOCAL_USER || 'admin')
    await page.locator('input[type="password"]:visible').fill(process.env.LOCAL_PASSWORD || 'admin123')
    await page.getByRole('button', { name: '\u767b\u5f55', exact: true }).click()
    await page.waitForTimeout(6000)
  }

  const pages = []
  for (const target of targets) {
    const startedAt = Date.now()
    let found = true
    try {
      await page.goto(`http://localhost:83${target.route}`, { waitUntil: 'domcontentloaded', timeout: 20_000 })
      await page.waitForTimeout(1800)
    } catch { found = false }
    const pageResponses = responses.filter(item => item.at >= startedAt)
    pages.push({
      title: target.title,
      expectedRoute: target.route,
      found,
      url: page.url(),
      bodyText: (await page.locator('body').innerText()).slice(0, 1200),
      requests: requests.filter(item => item.at >= startedAt),
      responses: pageResponses,
      failures: pageResponses.filter(item => {
        if (item.status >= 400) return true
        try {
          const data = JSON.parse(item.body)
          return typeof data.code === 'number' && data.code !== 0
        } catch { return false }
      })
    })
  }
  await fs.writeFile(path.join(outputDir, 'local-pages.json'), JSON.stringify(pages, null, 2), 'utf8')
  await page.screenshot({ path: path.join(outputDir, 'local-final.png'), fullPage: true })
  console.log(JSON.stringify({
    total: pages.length,
    found: pages.filter(item => item.found).length,
    pagesWithFailures: pages.filter(item => item.failures.length).length
  }))
} finally {
  await context.close()
  await browser.close()
}
