import fs from 'node:fs/promises'
import path from 'node:path'

const root = process.cwd()
const input = JSON.parse(await fs.readFile(path.join(root, 'migration-audit/browser/production-pages.json'), 'utf8'))

const rows = input.pages.map(page => {
  const endpoints = [...new Set((page.requests || [])
    .map(request => {
      try {
        const url = new URL(request.url)
        return url.pathname.startsWith('/js/swm/') || url.pathname.startsWith('/js/fms/')
          ? `${request.method} ${url.pathname}`
          : null
      } catch {
        return null
      }
    })
    .filter(Boolean))].sort()
  return {
    page: page.name,
    route: page.route,
    loaded: page.url?.startsWith('http://58.250.244.169:15200/ssiip/'),
    endpoints
  }
})

await fs.writeFile(
  path.join(root, 'migration-audit/browser/production-runtime-endpoints.json'),
  JSON.stringify(rows, null, 2),
  'utf8'
)

const lines = [
  '# SWM Production Runtime Endpoints',
  '',
  'Read-only capture from the production SWM subsystem. Request headers, bodies, responses, credentials and session data are intentionally omitted.',
  '',
  `- Production menu pages: ${rows.length}`,
  `- Pages successfully loaded during runtime capture: ${rows.filter(row => row.loaded).length}`,
  `- Pages requiring source-code fallback because the production server stopped responding: ${rows.filter(row => !row.loaded).length}`,
  '',
  '| Page | Production route | Initial query endpoints |',
  '|---|---|---|',
  ...rows.map(row => `| ${row.page} | \`${row.route}\` | ${row.loaded ? (row.endpoints.map(item => `\`${item}\``).join('<br>') || '-') : 'Runtime capture unavailable; compare checked-in old source'} |`),
  '',
  '## Confirmed Migration Gaps',
  '',
  '- `SwmLegacyCompatController` still contains empty-success implementations for dashboard statistics and person trajectory queries.',
  '- FMS archive compatibility pagination and save endpoints are placeholders.',
  '- Attendance summary list/export and schedule import/export are placeholders.',
  '- Alarm-light configuration, helmet configuration, beacon color configuration and file preview contain placeholder branches.',
  '- Safety-education auxiliary operations and TDengine log list/export endpoints are placeholders.',
  '- SOS broadcast/export currently return success without executing the original device-side behavior.',
  '- AI daily report and safety-person training list/detail queries were converted from placeholders to real tenant-aware database queries in this audit.',
  ''
]
await fs.writeFile(path.join(root, 'SWM_PRODUCTION_RUNTIME_ENDPOINTS.md'), lines.join('\n'), 'utf8')
