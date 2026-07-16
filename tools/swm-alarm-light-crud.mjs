import fs from 'node:fs/promises'
import path from 'node:path'
import { createRequire } from 'node:module'

const require = createRequire(import.meta.url)
const { chromium } = require('C:/Users/xy/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/.pnpm/playwright@1.61.1/node_modules/playwright')
const marker = `CODEX验收报警灯_${Date.now()}`
const editedMarker = `${marker}_已编辑`
const events = []
const browser = await chromium.launch({ headless: true, executablePath: 'C:/Program Files/Google/Chrome/Application/chrome.exe' })
const context = await browser.newContext({ viewport: { width: 1600, height: 1000 } })
const page = await context.newPage()

page.on('request', (request) => {
  if (!request.url().includes('/admin-api/swm/alarm-light/')) return
  events.push({ method: request.method(), url: request.url(), postData: request.postData() })
})
page.on('response', async (response) => {
  if (!response.url().includes('/admin-api/swm/alarm-light/')) return
  let body = ''
  try { body = await response.text() } catch {}
  events.push({ method: response.request().method(), url: response.url(), status: response.status(), body })
})

async function login() {
  await page.goto('http://localhost:83/index', { waitUntil: 'domcontentloaded' })
  await page.waitForTimeout(1200)
  if (!(await page.locator('input[type="password"]:visible').count())) return
  await page.locator('input[type="text"]:visible').last().fill('admin')
  await page.locator('input[type="password"]:visible').fill('admin123')
  await page.getByRole('button', { name: '登录', exact: true }).click()
  await page.waitForTimeout(5000)
}

function item(modal, label) {
  return modal.locator('.ant-form-item').filter({ hasText: label }).first()
}

async function submitModal(modal) {
  const saveResponse = page.waitForResponse((response) =>
    response.url().includes('/admin-api/swm/alarm-light/') && ['POST', 'PUT'].includes(response.request().method()),
  )
  await modal.getByRole('button', { name: /确\s*定/ }).click()
  const response = await saveResponse
  const data = await response.json()
  if (data.code !== 0) throw new Error(`保存失败: ${JSON.stringify(data)}`)
  await page.waitForTimeout(1000)
  return data
}

try {
  await login()
  await page.goto('http://localhost:83/swm/config/alarm-light', { waitUntil: 'domcontentloaded' })
  await page.waitForTimeout(1500)

  await page.getByText('新增报警灯', { exact: true }).click()
  let modal = page.locator('.ant-modal:visible').last()
  await item(modal, '报警灯名称').locator('input').fill(marker)
  await item(modal, 'SN码').locator('input').fill(`CODEX${Date.now()}`.slice(-16))
  const textarea = item(modal, '备注').locator('textarea')
  if (await textarea.count()) await textarea.fill('Codex 浏览器 CRUD 验收数据，可删除')
  const created = await submitModal(modal)

  await page.waitForTimeout(1200)
  let row = page.locator('.ant-table-row').filter({ hasText: marker }).first()
  if (!(await row.count())) throw new Error('新增成功后表格未显示测试记录')
  const editButton = row.locator('button').filter({ has: page.locator('[data-icon="edit"]') }).first()
  if (await editButton.count()) await editButton.click()
  else await row.getByText(/编辑/).click()
  modal = page.locator('.ant-modal:visible').last()
  await item(modal, '报警灯名称').locator('input').fill(editedMarker)
  await submitModal(modal)

  await page.waitForTimeout(1200)
  row = page.locator('.ant-table-row').filter({ hasText: editedMarker }).first()
  if (!(await row.count())) throw new Error('编辑成功后表格未显示修改结果')
  const deleteButton = row.getByText(/删\s*除/)
  if (!(await deleteButton.count())) {
    throw new Error(`操作列未找到删除按钮，当前行内容：${await row.innerText()}`)
  }
  await deleteButton.click()
  const deleteResponse = page.waitForResponse((response) =>
    response.url().includes('/admin-api/swm/alarm-light/delete') && response.request().method() === 'DELETE',
  )
  await page.locator('.ant-popconfirm:visible').getByRole('button', { name: /确\s*定/ }).click()
  const response = await deleteResponse
  const deleted = await response.json()
  if (deleted.code !== 0) throw new Error(`删除失败: ${JSON.stringify(deleted)}`)
  await page.waitForTimeout(1000)
  if (await page.locator('.ant-table-row').filter({ hasText: editedMarker }).count()) throw new Error('删除后测试记录仍在表格中')

  // 清理此前异常中断可能遗留的验收记录。
  for (let index = 0; index < 10; index++) {
    const staleRow = page.locator('.ant-table-row').filter({ hasText: 'CODEX验收报警灯_' }).first()
    if (!(await staleRow.count())) break
    const staleDeleteResponse = page.waitForResponse((item) =>
      item.url().includes('/admin-api/swm/alarm-light/delete') && item.request().method() === 'DELETE',
    )
    await staleRow.getByText(/删\s*除/).click()
    await page.locator('.ant-popconfirm:visible').getByRole('button', { name: /确\s*定/ }).click()
    const staleResponse = await staleDeleteResponse
    const staleResult = await staleResponse.json()
    if (staleResult.code !== 0) throw new Error(`清理遗留记录失败: ${JSON.stringify(staleResult)}`)
    await page.waitForTimeout(700)
  }

  const result = { success: true, marker, created, events }
  await fs.writeFile(path.resolve('migration-audit/browser/alarm-light-crud.json'), JSON.stringify(result, null, 2), 'utf8')
  console.log(JSON.stringify({ success: true, marker, requests: events.length }))
} catch (error) {
  const result = { success: false, marker, error: error.stack || error.message, events }
  await fs.writeFile(path.resolve('migration-audit/browser/alarm-light-crud.json'), JSON.stringify(result, null, 2), 'utf8')
  console.error(result.error)
  process.exitCode = 1
} finally {
  await context.close()
  await browser.close()
}
