# SWM / IOT Migration Progress

## 2026-06-22

- Continued direct migration work after the planning/audit phase.
- Added IOT-local base DO, service boundaries, TDengine client, and DTO aliases needed to remove IOT dependency on SWM internals.
- Replaced IOT DO inheritance from `SwmBaseDO` with `IotBaseDO`.
- Reworked TCP/MQTT handlers that were still tied to JeeSite/SWM internals or had broken migrated source text.
- Replaced active IOT `corpCode` / `corpName` usage with tenant-key logic based on `TenantContextHolder.getTenantId()`.
- Removed the IOT `getCorpCode` compatibility method.
- Verified IOT no longer scans with active JeeSite tenant symbols or SWM internal imports.
- Verified both compile commands pass:
  - `yudao-module-iot-biz -am -DskipTests compile`
  - `yudao-module-swm -am -DskipTests compile`
- Rewrote `MIGRATION_DIRECTION.md`, `task_plan.md`, and `findings.md` to reflect the current baseline and next-agent tasks.
- Removed active `corpCode` / `corpName` / `Corp*` tenant naming from SWM, SWM API, and IOT source/resources.
- Replaced `CorpDbEnum` with `TenantDbEnum` in SWM API and SWM module.
- Renamed device tenant mapping code from `DeviceCorp*` to `DeviceTenant*`, including Redis key constants.
- Migrated SWM Mapper/XML tenant filters from `corp_code/corpCode` to `tenant_id/tenantId`.
- Implemented IOT helmet device/config/cache services with `IotDeviceMapper` and Redis-backed parameter storage.
- Implemented `VoiceAlarmService` TCP voice command dispatch and wired `DeviceSoSAlarmHandler`.
- Verified no IOT imports of SWM internal packages remain.
- Verified both modules compile after the second migration pass.
- Compared old JeeSite IOT TCP alarm processors with migrated IOT processors.
- Restored persistence/notification behavior for helmet-off, long-time-static, and hazard-source TCP alarms.
- Implemented real TDengine writes for previously empty/default-success helmet message services through `TdengineJsonPayloadWriter`.
- Replaced fake `R.ok()` service defaults for those TDengine services with required interface methods.
- Added an IOT warning bridge that writes structured warning events to Redis without importing SWM internals.
- Verified both modules compile after the latest migration pass.
- Added SWM warning public API and implementation, then changed IOT warning bridge to call it.
- Removed IOT direct Maven dependency on SWM implementation module.
- Added explicit IOT dependencies that were previously coming transitively through SWM: system, xxl-job, spring-rabbit.
- Replaced System internal tenant service usage with `TenantCommonApi`.
- Replaced IOT `DictUtils` placeholder with RuoYi `DictDataApi`.
- Implemented local BLE fallback location service and external coordinate TDengine persistence.
- Wired Chat location algorithm calls through RuoYi dict type `corp_code_chat_base_url`.
- Implemented Beacon MAC Redis cache lookup and Geo/BLE comparison Redis decision cache.
- Final scans found no active corp/JeeSite tenant remnants, no IOT imports of SWM internals, and no fake `R.ok()`/not-implemented service placeholders in IOT.

## Next

- Validate the `corp_code_chat_base_url` dictionary values in each environment.
- Add SWM API/cache for voice template text if hazard-source voice must match legacy templates exactly.
- Add focused tests for TCP/MQTT alarm, device registration, Redis cache, and TDengine persistence flows.

## 2026-06-23 Frontend Audit

- Compared legacy `swm-frontend` with target `yudao-ui/yudao-ui-admin-vue3` for the requested SWM pages.
- Confirmed most requested page files are present in the target Vue3 project, but `/emergencyInquiry` and `swm/helmetLocation/index` were not found at the listed paths.
- Found frontend CRUD migration is incomplete: target `src/api/swm` still calls old JeeSite-style endpoints while backend controllers expose RuoYi-style `/page`, `/get`, `/create`, `/update`, `/delete` endpoints.
- Found old/transitional frontend dependencies remain: `defHttp`, `@/utils/http/axios`, `BasicTable`, `BasicModal`, `JeeSiteSelect`, `corpCode`, `@/api/sys`, old `listData`/`save` style endpoints.
- `pnpm ts:check` did not run because pnpm hit a Windows temp-file `EPERM` error.
- Direct `vue-tsc` produced many migration errors before timeout, including missing `@/api/swm/weekly`, missing SWM component module exports/types, and old request helper methods that do not exist in current RuoYi request typing.
- Wrote detailed handoff report to `FRONTEND_MIGRATION_AUDIT.md`.

## 2026-06-23 Frontend Migration Pass

- Added RuoYi axios compatibility for copied SWM pages: `defHttp`, `postJson`, `uploadFile`, and old-style second request options.
- Added/expanded SWM compatibility components: `Drawer`, `Preview`, `Description`, `Popupinput`, `PageWrapper`, `BasicTree`, and `ColorPicker`.
- Relaxed SWM `BasicTable`, `BasicForm`, and `useMessage` compatibility to support old copied-page usage such as computed schemas/columns, object message payloads, old table action fields, and modal/drawer registration callbacks.
- Migrated several requested SWM API files away from old JeeSite endpoint names toward RuoYi-style endpoints, including warning, warning record, hazard source, person, voice template, alarm config, alarm light, site map, beacon, and weekly attendance.
- Added legacy type shims for `/#/axios`, `/#/config`, `/#/store`, plus lightweight replacements for old `cipher`, `router/types`, `getConfigFileName`, and protocol utility imports.
- Removed many Vue3 macro import conflicts from copied SWM pages (`defineProps`, `defineEmits`, `defineExpose`) and duplicate `defineComponent` imports in copied setup scripts.
- Current direct `vue-tsc` runs to completion with `NODE_OPTIONS=--max-old-space-size=8192`; remaining errors are now mostly page-specific SWM type/logic issues plus unrelated full-repo baseline errors in BPMN, SimpleProcessDesigner, IM, MP, and system menu modules.
- Remaining high-priority SWM page clusters: `beaconMap`, `persontrack`, `warningPopup`, `rawMessageLog/externalCoordinateData/areaFenceData/tcpDeviceCommandLog`, `helmetdevice`, `safetyEducation`, `regionManagement`, and data screen labor/safety components.

## 2026-06-23 Frontend Type Closure

- Continued direct migration fixes in `yudao-ui/yudao-ui-admin-vue3`.
- Fixed the remaining SWM page/component TypeScript blockers found by direct `vue-tsc`, including `beaconMap`, `persontrack`, `warningPopup`, `inspectionPlan`, `personnelBoard`, `regionManagement`, `safetyEducation`, `safetyVideos`, warning record modal, table action compatibility, and data screen attendance card compatibility.
- Expanded SWM compatibility types for copied Vben-style `BasicTable`/`TableAction` usage so old action objects, computed columns, data sources, and row keys compile in Vue3.
- Fixed several non-SWM yudao-ui baseline TypeScript blockers that prevented a full frontend type check: BPMN viewer event typing, QR code logo typing, SimpleProcessDesigner optional node props, TagsView route typing, form-create component registration, IM store cached-map typing, MP ECharts options, and system menu JSX button typing.
- Verified direct frontend type check now passes:
  - `NODE_OPTIONS=--max-old-space-size=8192 node ./node_modules/vue-tsc/bin/vue-tsc.js --noEmit --incremental false --pretty false`
- Vite build validation is still blocked by local Windows/environment issues:
  - `pnpm build:dev` fails before Vite with `EPERM: operation not permitted, unlink ... _tmp_*` during pnpm dependency/status handling.
  - Direct `node ./node_modules/vite/bin/vite.js build --mode dev` reaches Vite config loading but fails with `spawn EPERM` and native `@swc/core-win32-x64-msvc` loading error.
  - An unsandboxed retry could not be executed in this session because the approval attempt was rejected by usage-limit policy.

## Current Frontend Notes

- SWM migrated pages now pass the TypeScript gate, but many copied labels/comments are still mojibake from the legacy project encoding and need a separate UI text cleanup pass.
- Some migrated SWM API endpoint paths were inferred from RuoYi naming and still need runtime backend verification against actual controllers.
- Next validation should run Vite build and then start the dev server for page-by-page CRUD smoke testing.

## 2026-06-30 SWM Frontend Runtime Visibility Recheck

- Started a second full pass because many migrated SWM pages still do not open at runtime.
- Confirmed `sql/mysql/swm_full_menu_fix.sql` displays mojibake only when read with the console default encoding; with UTF-8 the menu seed labels and SQL quoting are valid.
- Current focus is verifying dynamic router compatibility: menu `component` must match a real file under `src/views`, and `component_name` should match the page component name used for keep-alive/route identity.
- Verified all 31 page entries in `swm_full_menu_fix.sql` resolve to existing Vue files and matching component names.
- Strengthened `swm_full_menu_fix.sql` so it normalizes old same-path SWM menu records to the new component path before insert, then hides duplicate migrated menu rows. This addresses databases where stale copied menu rows were still shown in the sidebar.
- Added backend compatibility mappings for old page-entry URLs still used by copied Vue pages, including SOS warning paths, dashboard/dashboard2 paths, personTrack paths, FMS workshop/prod-line/work-group paths, TDengine log list/export paths, personnel-board helper paths, safety education helper paths, and SWM dict type/data paths.
- Replaced active SWM frontend `corpCode`/`corpName` remnants in beacon import and map components with RuoYi tenant-id based logic. The algorithm URL dictionary lookup now uses the current tenant id and falls back to the first configured option.
- Verified `yudao-ui-admin-vue3` direct `vue-tsc` passes after the changes.
- Backend compile is still blocked before code compilation by local Maven repository permissions: `C:\Users\xy\.m2\repository\com\aliyun\tea\resolver-status.properties (拒绝访问。)`.

## 2026-06-30 Login Loading Blocker Fix

- Investigated the reported full-screen loading state after login.
- Hardened dynamic route component resolution in `src/utils/routerHelper.ts`: when a backend menu row still points to a missing/stale copied component, the router now uses the 404 component and logs the missing component instead of registering an invalid route.
- Added error handling to `src/permission.ts` around user-info and dynamic-route initialization. If initialization fails, the app clears the token, stops the page loading indicator, and redirects back to login instead of spinning forever.
- Verified frontend type check still passes after these changes.

## 2026-07-01 SWM All Pages 404 Fix

- Connected to the remote MySQL database `10.50.103.166:3306/swm_ts` with JDBC and audited `system_menu`.
- Found the root cause of SWM pages all returning 404: the SWM top-level menu path was `swm`, while RuoYi/Vue Router top-level dynamic routes must be absolute paths such as `/system`, `/infra`, `/iot`. Because the root route was not `/swm`, the whole SWM route tree could not match.
- Fixed the remote database row: `system_menu.id = 900100` now has `path = '/swm'`.
- Updated `sql/mysql/swm_full_menu_fix.sql` so future executions create/select the SWM root path as `/swm`.
- Hardened `src/utils/routerHelper.ts` to automatically prefix `/` for top-level non-URL dynamic routes if the database path is missing it.
- Verified frontend type check passes after the route normalization change.

## 2026-07-01 SWM Blank Page Runtime Fix

- Investigated the browser log reported by the user. `PersonList.vue` and `PersonnelBoardList.vue` failed while loading options because copied JeeSite API code requested `/admin-api/admin-api/sys/dictData/treeData`.
- Replaced `src/api/sys/dictData.ts` with a RuoYi-compatible wrapper around `/system/dict-data/type`, `/system/dict-data/get`, `/system/dict-data/create`, `/system/dict-data/update`, and `/system/dict-data/delete`. Old copied pages can still call `dictDataTreeData` / `dictDataListData`, but the URL is no longer double-prefixed.
- Fixed `AttendanceList.vue` to tolerate an empty `shift_type_enum` dictionary instead of reading `getDictList('shift_type_enum')[0].value` unguarded.
- Verified frontend type check passes after these runtime fixes.

## 2026-06-23 Runtime Endpoint Fixes

- Fixed missing backend endpoint reported by the frontend: `GET /admin-api/swm/beacon-station/list-all`.
- Added `SwmBeaconStationService#getBeaconStationList` and controller mapping `GET /swm/beacon-station/list-all`.
- Reused a shared beacon-station query wrapper for both `/page` and `/list-all`, so filters such as `beaconId`, `deviceName`, `beaconType`, `area`, `location`, `beaconStatus`, `deployStatus`, and `building` behave consistently.
- Backend compile could not be fully verified in this session because Maven is blocked by local repository write permission errors under `E:\repository\maven`.

## 2026-06-23 Personnel Menu Visibility Fix

- Investigated the issue where many Personnel Management pages were not visible even though their backend APIs were reachable.
- Confirmed the target Vue3 page files exist, so the main visibility problem is dynamic-route menu data/permission assignment rather than missing page components.
- Added `sql/mysql/swm_personnel_menu_fix.sql` to create or normalize the SWM Personnel Management menu records for Personnel Registration, Personnel Board, Schedule Management, Daily Attendance, Weekly Attendance, Monthly Attendance, and Person Track.
- The SQL also grants the menu records to built-in `super_admin` and `tenant_admin` roles when those roles exist. If the logged-in user uses another role, that role must also receive these menus.
- Fixed stale/duplicate route component names in attendance/person-track pages:
  - `ViewsSwmDailyAttendanceIndex`
  - `ViewsSwmWeeklyAttendanceIndex`
  - `ViewsSwmMonthlyAttendanceIndex`
  - `ViewsSwmPersontrackPersonTrack`
- Verified the frontend type gate passes after the changes:
  - `NODE_OPTIONS=--max-old-space-size=8192 node ./node_modules/vue-tsc/bin/vue-tsc.js --noEmit --incremental false --pretty false`

## 2026-06-23 SWM Endpoint 404 Audit

- Confirmed `GET /admin-api/swm/organizationTree/getNodes` returned 404 because the migrated RuoYi module had mapper/VO pieces but no registered controller/service.
- Added the missing SWM organization tree controller and service, preserving the legacy path `/swm/organizationTree/getNodes` for copied frontend pages.
- Scanned SWM frontend API/view calls and compared them with registered `yudao-module-swm` controller mappings.
- Wrote the full missing-endpoint report to `SWM_ENDPOINT_404_AUDIT.md`.
- Migrated several Personnel Management frontend API paths from JeeSite camelCase/listData style to existing RuoYi/Yudao endpoints:
  - daily attendance list/detail
  - personnel board page
  - person schedule list/detail/create/update/delete
  - schedule time list/detail/create/update/delete
  - weekly/monthly attendance edit modal detail/save
- Verified the focused old-path scan no longer reports the migrated Personnel Management main paths. Remaining focused items are `daily-attendance/export-excel` and `scheduleTime/saveAll`.
- Verified frontend type check still passes.
- Backend compile is still blocked by local Maven repository write permissions under `E:\repository\maven`.

## 2026-06-23 SWM Legacy Endpoint Compatibility

- Added `SwmLegacyCompatController` for old JeeSite-style URLs that copied Vue pages still call directly.
- Added compatibility mappings for:
  - `/swm/safetyEducation/listData`
  - `/swm/swmSiteMapManagement/getEnabledMap`
  - `/swm/swmArea/listAll`
  - `/swm/common/options/companies`
  - `/swm/common/options/departments`
  - `/swm/common/options/prodLines`
  - `/swm/common/options/workGroups`
- Extended `SwmOrganizationTreeMapper` with common option queries migrated from old `SwmCommonOptionsDao.xml`.
- Migrated the Safety Education frontend list/detail/save/delete API methods to the new `/swm/safety-education/**` RuoYi paths.
- Added `sql/mysql/swm_safety_config_menu_fix.sql` because the Safety Education page component exists but no matching menu record was found in SQL scans; without menu records, the page will not appear in the dynamic sidebar even if APIs work.
- Verified the listed legacy mappings exist in source and frontend type check passes.
- Backend compile remains blocked by local Maven repository write permissions under `E:\repository\maven`.

## 2026-06-23 SWM Frontend Menu/Route Visibility Pass

- Investigated why many SWM pages are still not visible even after endpoint compatibility fixes.
- Confirmed the Vue3 router builds dynamic routes only from backend menu data. Existing Vue files do not appear in the sidebar unless `system_menu.component` points to the correct `src/views` component path and the current role has the menu.
- Found missing Vue3 target files for the originally requested `/swm-screen-new`, `/emergencyInquiry`, and `/swm/helmetLocation/index` paths. The new cockpit page exists as `swm/dataScreen/index` and is used as the cockpit menu entry.
- Fixed stale/duplicate component names in copied pages:
  - `swm/oneKeyRecall/oneKeyRecall`
  - `swm/beaconMap/beaconBase`
  - `swm/dataScreen/index`
  - `swm/swmWorkshop/list`
  - `swm/swmProdLine/list`
  - `swm/swmWorkGroup/list`
  - `swm/dictType/list`
- Added `sql/mysql/swm_full_menu_fix.sql`, a comprehensive idempotent menu repair script for the migrated SWM cockpit, safety, personnel, and config pages that are present in Vue3.
- Verified every component path in `swm_full_menu_fix.sql` matches an existing Vue file.
- Verified frontend type check still passes.
