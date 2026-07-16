import fs from 'node:fs/promises';
import path from 'node:path';
import { createRequire } from 'node:module';

const require = createRequire(import.meta.url);
const { chromium } = require('C:/Users/xy/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/.pnpm/playwright@1.61.1/node_modules/playwright');

const outputDir = path.resolve('migration-audit/browser');
await fs.mkdir(outputDir, { recursive: true });

async function login(page, username, password) {
  const visibleInputs = page.locator('input:visible');
  const count = await visibleInputs.count();
  if (count < 2) throw new Error(`登录页只找到 ${count} 个可见输入框`);

  const userSelectors = [
    'input[placeholder*="账号"]', 'input[placeholder*="用户名"]',
    'input[name="username"]', 'input[name="userName"]', 'input[type="text"]'
  ];
  const passwordSelectors = [
    'input[placeholder*="密码"]', 'input[name="password"]', 'input[type="password"]'
  ];
  let filledUser = false;
  for (const selector of userSelectors) {
    const input = page.locator(`${selector}:visible`).first();
    if (await input.count()) {
      await input.fill(username);
      filledUser = true;
      break;
    }
  }
  if (!filledUser) await visibleInputs.nth(0).fill(username);

  let filledPassword = false;
  for (const selector of passwordSelectors) {
    const input = page.locator(`${selector}:visible`).first();
    if (await input.count()) {
      await input.fill(password);
      filledPassword = true;
      break;
    }
  }
  if (!filledPassword) await visibleInputs.nth(1).fill(password);

  const loginButton = page.getByRole('button', { name: /登录|登 录|login/i }).first();
  if (await loginButton.count()) await loginButton.click();
  else await visibleInputs.nth(1).press('Enter');
}

async function auditSite(browser, config) {
  const context = await browser.newContext({ viewport: { width: 1600, height: 1000 } });
  const page = await context.newPage();
  const requests = [];
  const responses = [];
  page.on('request', request => {
    const url = request.url();
    if (url.startsWith(config.origin)) {
      requests.push({ method: request.method(), url, postData: request.postData() });
    }
  });
  page.on('response', async response => {
    const url = response.url();
    if (url.startsWith(config.origin) && /api|swm|ssiip|menu|login|auth/i.test(url)) {
      responses.push({ status: response.status(), url });
    }
  });

  await page.goto(config.url, { waitUntil: 'domcontentloaded', timeout: 45_000 });
  await page.waitForTimeout(2500);
  const before = {
    title: await page.title(),
    url: page.url(),
    text: (await page.locator('body').innerText()).slice(0, 5000),
    inputs: await page.locator('input').evaluateAll(items => items.map(item => ({
      name: item.name, type: item.type, placeholder: item.placeholder
    })))
  };
  await page.screenshot({ path: path.join(outputDir, `${config.name}-login.png`), fullPage: true });

  if (/登录|login/i.test(before.text) || before.inputs.some(item => item.type === 'password')) {
    await login(page, config.username, config.password);
    await page.waitForTimeout(7000);
  }

  const expandSelectors = ['.el-sub-menu__title', '.el-submenu__title', '.ant-menu-submenu-title'];
  for (const selector of expandSelectors) {
    const items = page.locator(`${selector}:visible`);
    const itemCount = Math.min(await items.count(), 30);
    for (let index = 0; index < itemCount; index++) {
      try { await items.nth(index).click({ timeout: 1000 }); } catch {}
    }
  }
  await page.waitForTimeout(1500);

  const menuSelectors = [
    'aside a', 'nav a', '.el-menu-item', '.el-sub-menu__title', '.el-submenu__title',
    '.ant-menu-item', '.ant-menu-submenu-title', '[role="menuitem"]'
  ].join(',');
  const menu = await page.locator(menuSelectors).evaluateAll(items => items.map(item => ({
    text: (item.innerText || item.textContent || '').trim().replace(/\s+/g, ' '),
    href: item.href || item.getAttribute('data-path') || item.getAttribute('route') || '',
    className: typeof item.className === 'string' ? item.className : ''
  })).filter(item => item.text));

  const links = await page.locator('a').evaluateAll(items => items.map(item => ({
    text: (item.innerText || item.textContent || '').trim().replace(/\s+/g, ' '),
    href: item.href
  })).filter(item => item.text || item.href));
  const result = {
    name: config.name,
    title: await page.title(),
    url: page.url(),
    before,
    bodyText: (await page.locator('body').innerText()).slice(0, 20000),
    menu,
    links,
    requests,
    responses
  };
  await page.screenshot({ path: path.join(outputDir, `${config.name}-after-login.png`), fullPage: true });
  await fs.writeFile(path.join(outputDir, `${config.name}.json`), JSON.stringify(result, null, 2));
  await context.close();
  return result;
}

const browser = await chromium.launch({
  headless: true,
  executablePath: 'C:/Program Files/Google/Chrome/Application/chrome.exe'
});
try {
  const production = await auditSite(browser, {
    name: 'production',
    origin: 'http://58.250.244.169:15200',
    url: 'http://58.250.244.169:15200/ssiip/',
    username: process.env.PROD_USER || '',
    password: process.env.PROD_PASSWORD || ''
  });
  const local = await auditSite(browser, {
    name: 'local',
    origin: 'http://localhost:83',
    url: 'http://localhost:83/',
    username: process.env.LOCAL_USER || 'admin',
    password: process.env.LOCAL_PASSWORD || 'admin123'
  });
  console.log(JSON.stringify({
    production: { url: production.url, title: production.title, menuCount: production.menu.length },
    local: { url: local.url, title: local.title, menuCount: local.menu.length }
  }, null, 2));
} finally {
  await browser.close();
}
