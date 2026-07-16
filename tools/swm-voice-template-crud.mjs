import fs from 'node:fs/promises'
import path from 'node:path'
import { createRequire } from 'node:module'

const require = createRequire(import.meta.url)
const { chromium } = require('C:/Users/xy/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/.pnpm/playwright@1.61.1/node_modules/playwright')
const marker = `CODEX-AUDIT-VOICE-${Date.now()}`
const editedMarker = `${marker}-EDITED`
const events = []
const browser = await chromium.launch({ headless: true, executablePath: 'C:/Program Files/Google/Chrome/Application/chrome.exe' })
const context = await browser.newContext({ viewport: { width: 1600, height: 1000 } })
const page = await context.newPage()
page.on('pageerror', (error) => console.log(JSON.stringify({ pageError: error.message, stack: error.stack })))
page.on('console', (message) => {
  if (message.type() === 'error') console.log(JSON.stringify({ consoleError: message.text() }))
})

const text = {
  login: '\u767b\u5f55',
  add: '\u65b0\u589e',
  edit: '\u7f16\u8f91',
  delete: '\u5220\u9664',
  enable: '\u542f\u7528',
  disable: '\u7981\u7528',
  confirm: '\u786e\u5b9a',
  name: '\u6a21\u677f\u540d\u79f0',
  type: '\u6a21\u677f\u7c7b\u578b',
  content: '\u8bed\u97f3\u6587\u5b57',
  status: '\u72b6\u6001',
}

page.on('response', async (response) => {
  if (!response.url().includes('/admin-api/swm/voice-template/')) return
  let body = ''
  try { body = await response.text() } catch {}
  events.push({ method: response.request().method(), url: response.url(), status: response.status(), body })
})

function formItem(modal, label) {
  return modal.locator('.ant-form-item').filter({ hasText: label }).first()
}

async function selectFirst(modal, label) {
  await formItem(modal, label).locator('.ant-select-selector').click()
  const option = page.locator('.ant-select-dropdown:not(.ant-select-dropdown-hidden) .ant-select-item-option:not(.ant-select-item-option-disabled)').first()
  await option.waitFor({ state: 'attached' })
  await option.click({ force: true })
}

async function login() {
  await page.goto('http://localhost:83/index', { waitUntil: 'domcontentloaded' })
  await page.waitForTimeout(1200)
  if (!(await page.locator('input[type="password"]:visible').count())) return
  await page.locator('input[type="text"]:visible').last().fill('admin')
  await page.locator('input[type="password"]:visible').fill('admin123')
  await page.getByRole('button', { name: text.login, exact: true }).click()
  await page.waitForTimeout(4500)
}

async function submit(modal, method, endpoint) {
  const responsePromise = page.waitForResponse(
    (response) => response.request().method() === method && response.url().includes(endpoint),
    { timeout: 8000 },
  ).catch((waitError) => ({ waitError }))
  await modal.getByRole('button', { name: /\u786e\s*\u5b9a|OK/ }).click()
  const response = await responsePromise
  if ('waitError' in response) {
    const validation = await modal.locator('.ant-form-item-has-error').allInnerTexts()
    await page.screenshot({ path: 'migration-audit/browser/voice-template-submit-error.png', fullPage: true })
    throw new Error(`form did not submit; validation=${JSON.stringify(validation)}; modal=${await modal.innerText()}; cause=${response.waitError.message}`)
  }
  const data = await response.json()
  if (data.code !== 0) throw new Error(`${method} ${endpoint} failed: ${JSON.stringify(data)}`)
  await page.waitForTimeout(900)
}

function action(row, labels) {
  return row.locator('.table-action').getByRole('button', { name: new RegExp(labels.join('|')) }).first()
}

async function deleteRow(row) {
  const responsePromise = page.waitForResponse(
    (response) => response.url().includes('/voice-template/delete'),
    { timeout: 10000 },
  ).catch((waitError) => ({ waitError }))
  await action(row, [text.delete]).click()
  const confirm = page.locator('.ant-popconfirm:visible').last()
  await confirm.locator('.ant-popconfirm-buttons .ant-btn-primary').click()
  const response = await responsePromise
  if ('waitError' in response) {
    throw new Error(`delete request was not sent; row=${await row.innerText()}; events=${JSON.stringify(events.slice(-5))}`)
  }
  const data = await response.json()
  if (data.code !== 0) throw new Error(`delete failed: ${JSON.stringify(data)}`)
  await page.waitForTimeout(700)
}

try {
  await login()
  await page.goto('http://localhost:83/swm/safety/voice-template', { waitUntil: 'domcontentloaded' })
  await page.waitForTimeout(1400)

  await page.getByRole('button', { name: new RegExp(text.add), exact: true }).click()
  let modal = page.locator('.ant-modal:visible').last()
  await formItem(modal, text.name).locator('input').fill(marker)
  await selectFirst(modal, text.type)
  await selectFirst(modal, text.status)
  await formItem(modal, text.content).locator('textarea').fill('Codex browser CRUD acceptance test')
  await modal.locator('.ant-checkbox-input').first().check({ force: true })
  await modal.locator('.ant-radio-input').first().check({ force: true })
  await submit(modal, 'POST', '/voice-template/create')

  let row = page.locator('.ant-table-row').filter({ hasText: marker }).first()
  if (!(await row.count())) throw new Error('created template is missing from table')
  await action(row, [text.edit]).click()
  modal = page.locator('.ant-modal:visible').last()
  await formItem(modal, text.name).locator('input').fill(editedMarker)
  await submit(modal, 'PUT', '/voice-template/update')

  row = page.locator('.ant-table-row').filter({ hasText: editedMarker }).first()
  const statusPromise = page.waitForResponse((response) => response.url().includes('/voice-template/update-status'))
  await action(row, [text.disable, text.enable]).click()
  const statusData = await (await statusPromise).json()
  if (statusData.code !== 0) throw new Error(`status toggle failed: ${JSON.stringify(statusData)}`)
  await page.waitForTimeout(700)

  row = page.locator('.ant-table-row').filter({ hasText: editedMarker }).first()
  await deleteRow(row)
  if (await page.locator('.ant-table-row').filter({ hasText: editedMarker }).count()) throw new Error('deleted template is still visible')

  for (let index = 0; index < 10; index++) {
    const staleRow = page.locator('.ant-table-row').filter({ hasText: /CODEX(?:-AUDIT|\u9a8c\u6536)/ }).first()
    if (!(await staleRow.count())) break
    await deleteRow(staleRow)
  }

  await fs.writeFile(path.resolve('migration-audit/browser/voice-template-crud.json'), JSON.stringify({ success: true, marker, events }, null, 2), 'utf8')
  console.log(JSON.stringify({ success: true, marker, requests: events.length }))
} catch (error) {
  await fs.writeFile(path.resolve('migration-audit/browser/voice-template-crud.json'), JSON.stringify({ success: false, marker, error: error.stack || error.message, events }, null, 2), 'utf8')
  throw error
} finally {
  await context.close()
  await browser.close()
}
