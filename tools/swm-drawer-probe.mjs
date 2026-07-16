import { createRequire } from 'node:module'

const require = createRequire(import.meta.url)
const { chromium } = require('C:/Users/xy/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/.pnpm/playwright@1.61.1/node_modules/playwright')
const browser = await chromium.launch({ headless: true, executablePath: 'C:/Program Files/Google/Chrome/Application/chrome.exe' })
const page = await browser.newPage({ viewport: { width: 1600, height: 1000 } })
const errors = []
page.on('pageerror', (error) => errors.push(error.message))
try {
  await page.goto('http://localhost:83/index', { waitUntil: 'domcontentloaded' })
  await page.waitForTimeout(1200)
  if (await page.locator('input[type="password"]:visible').count()) {
    await page.locator('input[type="text"]:visible').last().fill('admin')
    await page.locator('input[type="password"]:visible').fill('admin123')
    await page.getByRole('button', { name: '登录', exact: true }).click()
    await page.waitForTimeout(4000)
  }
  const placeholder = page.getByText('请选择租户', { exact: true }).first()
  if (await placeholder.count()) {
    await placeholder.click()
    await page.getByText('芋道源码', { exact: true }).last().click()
    await page.waitForTimeout(1000)
  }
  await page.goto(`http://localhost:83${process.env.PROBE_ROUTE || '/swm/config/swm-workshop'}`, { waitUntil: 'domcontentloaded' })
  await page.waitForTimeout(1200)
  let addButton = page.getByRole('button', { name: /^\s*新增\s*$/ })
  if (!(await addButton.count())) addButton = page.getByText(/^\s*新增\s*$/, { exact: true })
  if (!(await addButton.count())) {
    console.log(JSON.stringify({ beforeClickBody: (await page.locator('body').innerText()).slice(-1600), errors }, null, 2))
    process.exitCode = 2
  } else {
    await addButton.first().click({ timeout: 5000 })
  }
  await page.waitForTimeout(1000)
  const drawers = await page.locator('[class*="drawer"]').evaluateAll((nodes) => nodes.map((node) => ({
    tag: node.tagName,
    className: node.className,
    text: node.textContent?.trim().slice(0, 400),
    rect: node.getBoundingClientRect().toJSON(),
    display: getComputedStyle(node).display,
    visibility: getComputedStyle(node).visibility,
  })))
  console.log(JSON.stringify({ url: page.url(), drawers, errors, bodyTail: (await page.locator('body').innerText()).slice(-1200) }, null, 2))
} finally {
  await browser.close()
}
