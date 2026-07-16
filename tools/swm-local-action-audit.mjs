import fs from 'node:fs/promises'
import path from 'node:path'
import { createRequire } from 'node:module'

const require = createRequire(import.meta.url)
const { chromium } = require('C:/Users/xy/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/.pnpm/playwright@1.61.1/node_modules/playwright')
const outputDir = path.resolve('migration-audit/browser')
const menuSql = await fs.readFile(path.resolve('sql/mysql/swm_full_menu_fix.sql'), 'utf8')
const allTargets = menuSql.split('\n').flatMap((line) => {
  if (!line.trim().startsWith('(901')) return []
  const values = [...line.matchAll(/'([^']*)'/g)].map((match) => match[1])
  if (values.length < 4) return []
  const [group, title, , routePath] = values
  return [{ title, route: `/swm${group === 'root' ? '' : `/${group}`}/${routePath}` }]
})
const routeFilter = new Set((process.env.TARGET_ROUTES || '').split(',').filter(Boolean))
const targets = routeFilter.size ? allTargets.filter((item) => routeFilter.has(item.route)) : allTargets

const browser = await chromium.launch({
  headless: true,
  executablePath: 'C:/Program Files/Google/Chrome/Application/chrome.exe'
})
const context = await browser.newContext({ viewport: { width: 1600, height: 1000 } })
const page = await context.newPage()
const responses = []
const runtimeErrors = []
page.on('pageerror', (error) => runtimeErrors.push({ at: Date.now(), message: error.message }))
page.on('console', (message) => {
  if (message.type() === 'error') runtimeErrors.push({ at: Date.now(), message: message.text() })
})
page.on('response', async (response) => {
  if (!['xhr', 'fetch'].includes(response.request().resourceType())) return
  let body = ''
  try { body = (await response.text()).slice(0, 1200) } catch {}
  responses.push({ at: Date.now(), status: response.status(), method: response.request().method(), url: response.url(), body })
})

async function login() {
  await page.goto('http://localhost:83/index', { waitUntil: 'domcontentloaded', timeout: 30_000 })
  await page.waitForTimeout(1500)
  if (!(await page.locator('input[type="password"]:visible').count())) return
  await page.locator('input[type="text"]:visible').last().fill(process.env.LOCAL_USER || 'admin')
  await page.locator('input[type="password"]:visible').fill(process.env.LOCAL_PASSWORD || 'admin123')
  await page.getByRole('button', { name: '登录', exact: true }).click()
  await page.waitForTimeout(5000)
  await ensureTenant()
}

async function ensureTenant() {
  const tenantPlaceholder = page.getByText('请选择租户', { exact: true }).first()
  if (!(await tenantPlaceholder.count())) return
  await tenantPlaceholder.click().catch(() => {})
  await page.waitForTimeout(300)
  const tenantOption = page.getByText('芋道源码', { exact: true }).last()
  if (await tenantOption.count()) {
    await tenantOption.click().catch(() => {})
    await page.waitForTimeout(1200)
  }
}

async function closeOverlayOrReturn(route) {
  const overlay = page.locator('.ant-modal:visible, .ant-drawer-content-wrapper:visible').last()
  if (await overlay.count()) {
    const close = overlay.locator('.ant-modal-close, .ant-drawer-close').first()
    if (await close.count()) await close.click().catch(() => {})
    else {
      const cancel = overlay.getByRole('button', { name: /取消|关闭/ }).last()
      if (await cancel.count()) await cancel.click().catch(() => {})
    }
    await page.waitForTimeout(300)
  }
  if (new URL(page.url()).pathname !== route) {
    await page.goto(`http://localhost:83${route}`, { waitUntil: 'domcontentloaded', timeout: 20_000 })
    await page.waitForTimeout(800)
  }
}

function failed(items) {
  return items.filter((item) => {
    if (item.status >= 400) return true
    try {
      const data = JSON.parse(item.body)
      return typeof data.code === 'number' && data.code !== 0
    } catch { return false }
  })
}

const actionPatterns = [
  ['查询', /^查\s*询$/],
  ['重置', /^重\s*置$/],
  ['新增', /^\s*(新增|新建|新建隐患|新增报警灯)\s*$/],
  ['新增信标', /^\s*新增信标\s*$/],
  ['新增区域', /^\s*新增区域\s*$/],
  ['新增分组', /^\s*新增分组\s*$/],
  ['新增监控设备', /^\s*新增监控设备\s*$/],
  ['编辑', /^\s*(编辑|编\s*辑|编辑字典)\s*$/],
  ['查看', /^\s*(查看|详情|查看详情)\s*$/],
  ['配置', /^\s*(配置|参数配置|排班时间管理|绑定安全帽)\s*$/],
  ['记录', /^\s*(使用记录|排班记录|巡检记录|字典数据)\s*$/],
  ['人员排班', /^\s*人员排班\s*$/],
  ['班次调整', /^\s*班次调整\s*$/],
  ['离职', /^\s*处理离职\s*$/],
  ['导入', /^\s*(批量导入|导入)\s*$/],
  ['导出', /^\s*(导出|批量导出)\s*$/]
]

try {
  await login()
  const results = []
  for (const target of targets) {
    await page.goto(`http://localhost:83${target.route}`, { waitUntil: 'domcontentloaded', timeout: 20_000 })
    await page.waitForTimeout(1200)
    const labels = await page.locator('button:visible, a:visible').allInnerTexts()
    const pageResult = {
      ...target,
      bodyText: (await page.locator('body').innerText()).slice(0, 4000),
      labels: [...new Set(labels.map((item) => item.trim()).filter(Boolean))],
      actions: []
    }
    for (const [name, pattern] of actionPatterns) {
      let candidates = page.getByRole('button', { name: pattern, exact: true })
      if (!(await candidates.count())) candidates = page.getByRole('link', { name: pattern, exact: true })
      if (!(await candidates.count())) candidates = page.getByText(pattern, { exact: true })
      if (!(await candidates.count())) continue
      const startedAt = Date.now()
      const beforeUrl = page.url()
      let clickError = ''
      try {
        await candidates.first().scrollIntoViewIfNeeded({ timeout: 3000 })
        await candidates.first().click({ timeout: 3000 })
        await page.waitForTimeout(700)
      } catch (error) {
        clickError = error.message
      }
      const overlay = page.locator('.ant-modal:visible, .ant-drawer-content-wrapper:visible').last()
      const overlayText = await overlay.count() ? (await overlay.innerText()).slice(0, 1000) : ''
      const actionResponses = responses.filter((item) => item.at >= startedAt)
      pageResult.actions.push({
        name,
        beforeUrl,
        afterUrl: page.url(),
        overlayText,
        clickError,
        failures: failed(actionResponses),
        runtimeErrors: runtimeErrors.filter((item) => item.at >= startedAt)
      })
      await closeOverlayOrReturn(target.route)
    }
    results.push(pageResult)
    console.log(`${results.length}/${targets.length} ${target.title}`)
  }
  await fs.writeFile(path.join(outputDir, 'local-actions.json'), JSON.stringify(results, null, 2), 'utf8')
  const actionFailures = results.flatMap((item) => item.actions.map((action) => ({ page: item.title, route: item.route, ...action })))
    .filter((item) => item.clickError || item.failures.length || item.runtimeErrors.length ||
      (['新增', '新增信标', '新增区域', '新增分组', '新增监控设备', '编辑', '查看', '配置', '记录', '班次调整', '离职', '导入'].includes(item.name) && !item.overlayText && item.beforeUrl === item.afterUrl))
  console.log(JSON.stringify({ pages: results.length, actions: results.reduce((sum, item) => sum + item.actions.length, 0), actionFailures: actionFailures.length }))
} finally {
  await context.close()
  await browser.close()
}
