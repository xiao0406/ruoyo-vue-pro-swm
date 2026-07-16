# SWM Browser Audit

Audit date: 2026-07-15

## Scope

- Production reference: `http://58.250.244.169:15200/ssiip/`
- Migrated frontend: `http://localhost:83/`
- Production operations were limited to login, subsystem selection, menu reads, and direct read-only page navigation.
- No production create, update, delete, import, export, or command operation was executed.

## Results

- Production `/js/a/menuRoute` contains 40 SWM leaf pages.
- The migrated source contains matching Vue components for 39 of those pages.
- `/swm/helmetLocation/index` is the only missing component. It is also absent from the checked-in old `swm-frontend` source and from the production bundle component registry, so the production menu row is a historical broken entry rather than a migration regression.
- The migrated frontend currently contains 231 literal `/swm/**` calls and the backend contains 361 SWM route mappings. Static Controller coverage has no unmatched frontend URL.
- The local menu seed originally omitted nine production pages whose Vue components already exist. They were added to `sql/mysql/swm_full_menu_fix.sql`:
  - Hidden danger list and disposal list
  - Inspection plan and inspection task
  - Safety education videos and training records
  - AI daily report
  - Monitoring equipment
  - Dictionary data
- The corrected menu migration was executed successfully against `swm_ts` on 2026-07-15.
- Production runtime initialization requests were captured successfully for 31 pages. The production server stopped responding during the final nine configuration-page navigations; those pages remain covered by old-source comparison, not by runtime evidence.
- The sanitized page-to-endpoint matrix is in `SWM_PRODUCTION_RUNTIME_ENDPOINTS.md`.

## Runtime Findings

The local browser opened every route from the menu seed. Three pages reported a load failure, all caused by the same request:

`GET /admin-api/swm/dict-data/page?pageNo=1&pageSize=200&dictType=person_type_enum`

The remote database contains 21 matching rows. The failure was caused by schema mapping: `swm_dict_data` uses `dict_code` as its primary key and has no `id` column, while the migrated entity inherited an `id` column mapping. `SwmDictDataDO` now maps `dict_code` as the MyBatis primary key and exposes compatible `id` and `dictCode` response values.

## Additional Fixes

- Added RuoYi Excel export for safety helmet devices.
- Changed daily, weekly, and monthly attendance exports from the old JeeSite download-link response contract to Blob downloads from the migrated RuoYi endpoints.
- Added repeatable production comparison and API coverage tools under `tools/`.
- Replaced the empty AI daily-report and video-training compatibility mappings with tenant-aware database pagination and detail queries.
- Corrected the real `swm_dify` and `swm_safety_person_training` schema mappings; the database contains 35 daily reports and 1117 training records.

## Verification

- `pnpm ts:check`: passed.
- `mvn -pl yudao-module-swm -am -DskipTests compile` with JDK 17: passed.
- Static SWM frontend-to-Controller path coverage: 0 unmatched calls.

The running backend must be restarted before repeating the local browser check for the dictionary, export, AI-report and training-record fixes.

## Remaining Business Placeholders

Static route coverage is not business-equivalence coverage. Dashboard statistics, person trajectory, FMS compatibility, attendance summary export, schedule import/export, several device configuration branches, file preview, safety-education auxiliary operations, TDengine logs, and SOS device commands still contain empty-success compatibility implementations. These must be replaced before the whole SWM migration can be declared complete.
