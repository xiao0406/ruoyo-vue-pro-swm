# SWM / IOT Migration Findings

## Verified

- `yudao-module-iot-biz -am -DskipTests compile` passes.
- `yudao-module-swm -am -DskipTests compile` passes.
- IOT scan finds no active `corpCode`, `corpName`, `CorpUtils`, or `setCurrentCorpCode`.
- IOT scan finds no direct imports of SWM internal `dal`, `service`, `controller`, `framework`, `convert`, `enums`, `util`, or `mapper` packages.
- SWM/SWM API/IOT scan finds no active `corpCode`, `corpName`, `corp_code`, `corp_name`, `CorpDbEnum`, `DeviceCorp*`, or `DEVICE_TO_CORP` symbols.
- IOT is allowed to depend on `yudao-module-swm-api` for shared constants/contracts.

## Main Changes Made

- Added `IotBaseDO` and moved IOT DO inheritance away from `SwmBaseDO`.
- Added IOT-local TDengine REST client and TDengine service methods.
- Added IOT-local placeholder services for helmet device, helmet config, and helmet cache flows.
- Replaced old SWM entity aliases with local DTO-style classes used by IOT TCP handlers.
- Rewrote broken/garbled TCP/MQTT migration files into compile-safe IOT-local versions.
- Replaced active IOT tenant key usage with `TenantContextHolder.getTenantId()` derived tenant keys.
- Removed the old `getCorpCode` compatibility entry from `DeviceCorpMappingCache`.
- Rewrote `MIGRATION_DIRECTION.md` as a clean UTF-8 handoff file.
- Replaced `CorpDbEnum` with `TenantDbEnum`.
- Renamed device mapping job/cache/Redis key to tenant terminology.
- Implemented IOT helmet device/config/cache services.
- Implemented TCP voice command dispatch through `VoiceAlarmService`.
- Restored helmet-off, long-time-static, and hazard-source TCP alarm side effects.
- Implemented TDengine writes for helmet login/local-record/push/upload/raw/Runde/sending-message/SIP-SOS/SOS services.
- Replaced several service-level fake-success defaults with concrete implementations.
- IOT now calls SWM through `SwmWarningApi` in `yudao-module-swm-api`; the old Redis warning bridge is only fallback/replay metadata.
- IOT no longer has a Maven dependency on `yudao-module-swm`.
- IOT tenant cleanup uses `TenantCommonApi`, not System internal service/DO classes.
- IOT dictionary lookup uses RuoYi `DictDataApi`.
- Location engine resolves Chat algorithm base URLs from `corp_code_chat_base_url` and falls back to BLE on failure.

## Remaining Risks

- Hazard-source voice text uses a safe hazard-name fallback unless SWM exposes voice template text through API/cache.
- The literal `corp_code_chat_base_url` is intentionally retained because it is the current RuoYi dictionary type name shown by the user.
- Local `R` is still transitional and can be replaced by yudao/common result types later.

## Recommended Next Checks

```powershell
Get-ChildItem yudao-module-swm/src/main,yudao-module-swm-api/src/main -Recurse -Include *.java,*.xml |
  Select-String -Pattern 'corpCode|corpName|CorpUtils|setCurrentCorpCode|com\.jeesite|CrudService|QueryType|getSqlMap'
```

Then migrate each active hit to RuoYi tenant/query/service style.

## Frontend Migration Audit - 2026-06-23

- Added root handoff file `FRONTEND_MIGRATION_AUDIT.md`.
- Most requested SWM frontend page files are present in `yudao-ui/yudao-ui-admin-vue3/src/views/swm`, but this is not a completed CRUD migration.
- Two listed pages were not found at the specified old or new paths: `/emergencyInquiry` and `swm/helmetLocation/index`.
- `src/api/swm` is still largely old endpoint style. Examples: `/swm/warningManagement/listData`, `/swm/swmPerson/listData`, `/swm/swmVoiceTemplate/listData`, `/swm/swmDictType/listData`.
- Migrated backend controller paths are RuoYi style such as `/swm/warning/page`, `/swm/person/page`, `/swm/voice-template/page`, `/swm/dict-type/page`.
- New frontend still has old/transitional patterns: `defHttp`, `@/utils/http/axios`, `BasicTable`, `BasicModal`, `JeeSiteSelect`, `corpCode`, `@/api/sys`, `listData`.
- `src/views/swm/weekly/index.vue` imports missing module `@/api/swm/weekly`.
- Direct `vue-tsc` produced many errors before timeout, including missing SWM component module exports/types, missing weekly API, current `request` helper lacking old `postJson`/`uploadFile`, and copied Vue macro/import/type issues.

## SWM Frontend Runtime Visibility Recheck - 2026-06-30

- `sql/mysql/swm_full_menu_fix.sql` contains 31 SWM page menu records; every `component` resolves to an existing Vue file and every `component_name` is present in that file.
- A likely runtime menu problem remains if old database menu records already exist with the same SWM path but stale component paths. The current script inserts/updates by component, so stale path duplicates can still appear and route to missing components.
- Requested pages still missing as exact Vue3 files: `/emergencyInquiry`, `/swm/helmetLocation/index`; legacy source search only found helmet device/config pages, not a distinct `helmetLocation` source page.
- Requested config log pages call old endpoints and no matching SWM admin controllers were found for those names: `swmRawMessageLog`, `externalCoordinateData`, `areaFenceData`, `swmTcpDeviceCommandLog`.
- Active copied frontend remnants remain in requested page code: `currentCorpCode` in beacon/beaconMap/persontrack map components, and JeeSite copyright/component labels in some copied files.
- Follow-up fixes now normalize stale same-path menu records, hide duplicate migrated menu rows, and add compatibility mappings for the old URLs that the requested page entries still import directly.
- Active `corpCode`/`corpName` frontend references under `src/views/swm` and `src/api/swm` are now removed; remaining algorithm base URL lookup is tenant-id based with a first-option fallback.
- Compatibility mappings intentionally return empty standard structures for dashboard, personTrack, TDengine logs, and some auxiliary copied-page endpoints where no migrated read service/controller exists yet. This prevents 404/page-open failures, but real business data still requires proper service implementations.
