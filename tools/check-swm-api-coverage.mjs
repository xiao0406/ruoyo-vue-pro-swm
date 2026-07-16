import fs from 'node:fs/promises'
import path from 'node:path'

const root = process.cwd()
const frontendRoots = [
  'yudao-ui/yudao-ui-admin-vue3/src/api/swm',
  'yudao-ui/yudao-ui-admin-vue3/src/views/swm'
].map(value => path.join(root, value))
const controllerRoot = path.join(root, 'yudao-module-swm/src/main/java')

async function filesUnder(dir, extensions) {
  const result = []
  for (const entry of await fs.readdir(dir, { withFileTypes: true })) {
    const file = path.join(dir, entry.name)
    if (entry.isDirectory()) result.push(...await filesUnder(file, extensions))
    else if (extensions.some(extension => entry.name.endsWith(extension))) result.push(file)
  }
  return result
}

function cleanUrl(value) {
  return value
    .replace(/\$\{[^}]+\}/g, '*')
    .replace(/\?.*$/, '')
    .replace(/\/$/, '') || '/'
}

const frontendCalls = []
for (const dir of frontendRoots) {
  for (const file of await filesUnder(dir, ['.ts', '.vue'])) {
    const content = await fs.readFile(file, 'utf8')
    for (const match of content.matchAll(/url\s*:\s*(['"`])([^'"`]+)\1/g)) {
      if (!match[2].startsWith('/swm/')) continue
      frontendCalls.push({
        url: cleanUrl(match[2]),
        file: path.relative(root, file).replaceAll('\\', '/'),
        line: content.slice(0, match.index).split('\n').length
      })
    }
  }
}

const backendRoutes = []
for (const file of await filesUnder(controllerRoot, ['.java'])) {
  const content = await fs.readFile(file, 'utf8')
  const classIndex = content.search(/\bclass\s+\w+/)
  const classPrefix = classIndex >= 0 ? content.slice(0, classIndex) : ''
  const classMappings = [...classPrefix.matchAll(/@RequestMapping\s*\(([^)]*)\)/gs)]
  const baseArguments = classMappings.at(-1)?.[1] || ''
  const base = [...baseArguments.matchAll(/"([^"]+)"/g)].map(item => item[1]).find(item => item.startsWith('/swm')) || ''
  const methodContent = classIndex >= 0 ? content.slice(classIndex) : content
  for (const match of methodContent.matchAll(/@(GetMapping|PostMapping|PutMapping|DeleteMapping|RequestMapping)\s*\(([^)]*)\)/gs)) {
    const paths = [...match[2].matchAll(/"([^"]*)"/g)].map(item => item[1])
    if (!paths.length) paths.push('')
    for (const route of paths) {
      const fullPath = `${base.replace(/\/$/, '')}/${route.replace(/^\//, '')}`.replace(/\/$/, '') || '/'
      if (fullPath.startsWith('/swm/')) {
        backendRoutes.push({
          url: fullPath,
          method: match[1].replace('Mapping', '').toUpperCase(),
          file: path.relative(root, file).replaceAll('\\', '/')
        })
      }
    }
  }
}

function routeMatches(frontendUrl, backendUrl) {
  const escape = value => value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const backendPattern = backendUrl.split(/(\{[^}]+\})/).map(part => {
    if (part.startsWith('{')) return '[^/]+'
    return escape(part).replace(/\\\*\\\*/g, '.*').replace(/\\\*/g, '[^/]+')
  }).join('')
  const frontendPattern = frontendUrl.split('*').map(escape).join('[^/]+')
  return new RegExp(`^${backendPattern}$`).test(frontendUrl) || new RegExp(`^${frontendPattern}$`).test(backendUrl)
}

const uniqueCalls = [...new Map(frontendCalls.map(item => [`${item.url}|${item.file}|${item.line}`, item])).values()]
const missing = uniqueCalls.filter(call => !backendRoutes.some(route => routeMatches(call.url, route.url)))
const lines = [
  '# SWM API Coverage',
  '',
  `Generated: ${new Date().toISOString()}`,
  '',
  `- Frontend SWM URL calls: ${uniqueCalls.length}`,
  `- Backend SWM route mappings: ${backendRoutes.length}`,
  `- Frontend calls without a matching Controller mapping: ${missing.length}`,
  '',
  '## Missing Controller Mappings',
  '',
  ...missing.map(item => `- \`${item.url}\` at \`${item.file}:${item.line}\``),
  '',
  '## Notes',
  '',
  '- This is static path coverage. It does not prove request parameters, SQL, response fields, or business behavior are correct.',
  '- Dynamic URLs are normalized to a single path segment wildcard.',
  ''
]

await fs.writeFile(path.join(root, 'SWM_API_COVERAGE.md'), lines.join('\n'), 'utf8')
await fs.writeFile(path.join(root, 'migration-audit/browser/api-coverage.json'), JSON.stringify({ frontendCalls: uniqueCalls, backendRoutes, missing }, null, 2), 'utf8')
console.log(JSON.stringify({ frontendCalls: uniqueCalls.length, backendRoutes: backendRoutes.length, missing: missing.length }))
