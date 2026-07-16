import { createRequire } from 'node:module'

const require = createRequire(import.meta.url)
const { chromium } = require('C:/Users/xy/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/.pnpm/playwright@1.61.1/node_modules/playwright')
const browser = await chromium.launch({ headless: true, executablePath: 'C:/Program Files/Google/Chrome/Application/chrome.exe' })
const context = await browser.newContext({ viewport: { width: 1600, height: 1000 } })
const page = await context.newPage()
const requests = []
page.on('request', request => {
  if (request.url().includes('/admin-api/swm/alarm-light/')) requests.push(`${request.method()} ${request.url()}`)
})

await page.goto('http://localhost:83/index', { waitUntil: 'domcontentloaded' })
await page.waitForTimeout(1200)
if (await page.locator('input[type="password"]:visible').count()) {
  await page.locator('input[type="text"]:visible').last().fill('admin')
  await page.locator('input[type="password"]:visible').fill('admin123')
  await page.getByRole('button', { name: '登录', exact: true }).click()
  await page.waitForTimeout(4000)
}
await page.goto('http://localhost:83/swm/config/alarm-light', { waitUntil: 'domcontentloaded' })
await page.waitForTimeout(1500)
const row = page.locator('.ant-table-row').filter({ hasText: 'CODEX验收报警灯_' }).first()
if (!(await row.count())) throw new Error('没有可供探测的 CODEX 报警灯记录')
await row.getByText(/删\s*除/).click()
const popconfirm = page.locator('.ant-popconfirm:visible').last()
const buttons = await popconfirm.getByRole('button').allTextContents()
const popText = await popconfirm.innerText()
const storage = await page.evaluate(() => Object.fromEntries(Object.entries(localStorage)))
await popconfirm.getByRole('button').last().click()
await page.waitForTimeout(3000)
console.log(JSON.stringify({ row: await row.innerText(), popText, buttons, requests, storageKeys: Object.keys(storage) }, null, 2))
await context.close()
await browser.close()
