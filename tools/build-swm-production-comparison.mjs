import fs from 'node:fs/promises'
import path from 'node:path'

const root = process.cwd()
const audit = JSON.parse(await fs.readFile(path.join(root, 'migration-audit/browser/production-pages.json'), 'utf8'))
const menuResponse = audit.apiResponses.find(item => item.url.includes('/menuRoute'))
if (!menuResponse) throw new Error('Production menuRoute response is missing')
const menuTree = JSON.parse(menuResponse.body)

const oldViews = path.join(root, 'swm-frontend/src/views')
const newViews = path.join(root, 'yudao-ui/yudao-ui-admin-vue3/src/views')

async function exists(file) {
  try { await fs.access(file); return true } catch { return false }
}

async function listFiles(dir) {
  const result = []
  if (!await exists(dir)) return result
  for (const entry of await fs.readdir(dir, { withFileTypes: true })) {
    const file = path.join(dir, entry.name)
    if (entry.isDirectory()) result.push(...await listFiles(file))
    else if (/\.(vue|ts|js)$/.test(entry.name)) result.push(file)
  }
  return result
}

async function endpointsFor(component, baseDir) {
  if (!component || component === 'LAYOUT' || component === 'IFRAME') return []
  const relative = component.replace(/^\//, '')
  const file = path.join(baseDir, `${relative}.vue`)
  const dir = path.dirname(file)
  const files = await listFiles(dir)
  const endpoints = new Set()
  for (const sourceFile of files) {
    const content = await fs.readFile(sourceFile, 'utf8')
    for (const match of content.matchAll(/url\s*:\s*['"`]([^'"`\s]*(?:\/swm\/|\/sys\/|\/system\/)[^'"`\s]*)['"`]/g)) {
      endpoints.add(match[1])
    }
  }
  return [...endpoints].sort()
}

const leaves = []
function walk(items, parents = [], inShield = false) {
  for (const item of items || []) {
    const title = item.meta?.title || ''
    const currentParents = title ? [...parents, title] : parents
    const shield = inShield || item.id === '1787287323076624384' || title === '\u6167\u773c\u5b89\u76fe'
    if (shield && (!item.children || item.children.length === 0) && item.component) {
      leaves.push({ ...item, menuPath: currentParents.join(' > ') })
    }
    walk(item.children, currentParents, shield)
  }
}
walk(menuTree)

const rows = []
for (const item of leaves) {
  const relative = item.component && !['LAYOUT', 'IFRAME'].includes(item.component)
    ? item.component.replace(/^\//, '')
    : ''
  const oldFile = relative ? path.join(oldViews, `${relative}.vue`) : ''
  const newFile = relative ? path.join(newViews, `${relative}.vue`) : ''
  rows.push({
    title: item.meta?.title || '',
    menuPath: item.menuPath,
    route: item.path || '',
    component: item.component || '',
    url: item.url || '',
    oldExists: oldFile ? await exists(oldFile) : item.component === 'IFRAME',
    newExists: newFile ? await exists(newFile) : item.component === 'IFRAME',
    oldFile: oldFile ? path.relative(root, oldFile).replaceAll('\\', '/') : '',
    newFile: newFile ? path.relative(root, newFile).replaceAll('\\', '/') : '',
    oldEndpoints: await endpointsFor(item.component, oldViews),
    newEndpoints: await endpointsFor(item.component, newViews)
  })
}

const missingOld = rows.filter(row => !row.oldExists)
const missingNew = rows.filter(row => !row.newExists)
const endpointDifferences = rows.filter(row => row.oldExists && row.newExists &&
  JSON.stringify(row.oldEndpoints) !== JSON.stringify(row.newEndpoints))

const lines = [
  '# SWM Production Comparison',
  '',
  `Generated: ${new Date().toISOString()}`,
  '',
  'Production source: `http://58.250.244.169:15200/ssiip/` (`/js/a/menuRoute`, read-only capture).',
  '',
  '## Summary',
  '',
  `- Production SWM leaf menus: ${rows.length}`,
  `- Missing in old source: ${missingOld.length}`,
  `- Missing in migrated source: ${missingNew.length}`,
  `- Pages whose direct URL literal sets differ: ${endpointDifferences.length}`,
  '',
  '## Page Matrix',
  '',
  '| Menu | Production route | Old view | Migrated view |',
  '|---|---|---:|---:|',
  ...rows.map(row => `| ${row.menuPath} | \`${row.route}\` | ${row.oldExists ? 'YES' : 'NO'} | ${row.newExists ? 'YES' : 'NO'} |`),
  '',
  '## Missing Implementations',
  '',
  ...missingNew.map(row => `- **${row.menuPath}**: production route \`${row.route}\`, component \`${row.component}\`; old source: ${row.oldExists ? `\`${row.oldFile}\`` : 'also missing'}.`),
  '',
  '## Endpoint Differences',
  '',
  ...endpointDifferences.flatMap(row => [
    `### ${row.menuPath}`,
    '',
    `- Production/old direct URL literals: ${row.oldEndpoints.length ? row.oldEndpoints.map(value => `\`${value}\``).join(', ') : '(none in page directory)'}`,
    `- Migrated direct URL literals: ${row.newEndpoints.length ? row.newEndpoints.map(value => `\`${value}\``).join(', ') : '(none in page directory)'}`,
    ''
  ]),
  '## Machine Data',
  '',
  'Full structured comparison: `migration-audit/browser/production-comparison.json`.'
]

await fs.writeFile(path.join(root, 'SWM_PRODUCTION_COMPARISON.md'), `${lines.join('\n')}\n`, 'utf8')
await fs.writeFile(path.join(root, 'migration-audit/browser/production-comparison.json'), JSON.stringify(rows, null, 2), 'utf8')
console.log(JSON.stringify({ leafCount: rows.length, missingOld: missingOld.length, missingNew: missingNew.length, endpointDifferences: endpointDifferences.length }))
