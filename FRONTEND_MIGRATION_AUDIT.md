# SWM Frontend Migration Audit

Scope: legacy `swm-frontend` to `yudao-ui/yudao-ui-admin-vue3`.

Date: 2026-06-23.

## Conclusion

The SWM frontend is not fully migrated yet.

Most requested page files have been copied into `yudao-ui/yudao-ui-admin-vue3/src/views/swm`, but CRUD/API migration is incomplete. The new frontend still contains old JeeSite/Vben-compatible API paths and component usage. It does not currently align with the RuoYi Vue3 admin API style or the migrated SWM backend controller paths.

## Page File Presence

| Page | Legacy File | New File | Status |
|---|---|---|---|
| `/swm-screen-new` | `src/views/swm/dataScreen/index.vue` | `src/views/swm/dataScreen/index.vue` | copied |
| `WarningList` | `src/views/swm/warning/WarningList.vue` | `src/views/swm/warning/WarningList.vue` | copied |
| `warningRecord/WarningRecordList` | `src/views/swm/warningRecord/WarningRecordList.vue` | `src/views/swm/warningRecord/WarningRecordList.vue` | copied |
| `swm/hazardSource/HazardSourceList` | `src/views/swm/hazardSource/HazardSourceList.vue` | `src/views/swm/hazardSource/HazardSourceList.vue` | copied |
| `/swm/oneKeyRecall/oneKeyRecall` | `src/views/swm/oneKeyRecall/oneKeyRecall.vue` | `src/views/swm/oneKeyRecall/oneKeyRecall.vue` | copied |
| `/swm/voiceTemplate/index` | `src/views/swm/voiceTemplate/index.vue` | `src/views/swm/voiceTemplate/index.vue` | copied |
| `swm/sos/SosList` | `src/views/swm/sos/SosList.vue` | `src/views/swm/sos/SosList.vue` | copied |
| `/swm/personnel/PersonList` | `src/views/swm/personnel/PersonList.vue` | `src/views/swm/personnel/PersonList.vue` | copied |
| `/swm/personnelBoard/PersonnelBoardList` | `src/views/swm/personnelBoard/PersonnelBoardList.vue` | `src/views/swm/personnelBoard/PersonnelBoardList.vue` | copied |
| `/swm/Attendance/AttendanceList` | `src/views/swm/Attendance/AttendanceList.vue` | `src/views/swm/Attendance/AttendanceList.vue` | copied |
| `swm/dailyAttendance/index` | `src/views/swm/dailyAttendance/index.vue` | `src/views/swm/dailyAttendance/index.vue` | copied |
| `/swm/weekly/index` | `src/views/swm/weekly/index.vue` | `src/views/swm/weekly/index.vue` | copied, but imports missing API module |
| `swm/monthlyAttendance/index` | `src/views/swm/monthlyAttendance/index.vue` | `src/views/swm/monthlyAttendance/index.vue` | copied |
| `/swm/persontrack/personTrack` | `src/views/swm/persontrack/personTrack.vue` | `src/views/swm/persontrack/personTrack.vue` | copied |
| `/swm/colorConfig/index` | `src/views/swm/colorConfig/index.vue` | `src/views/swm/colorConfig/index.vue` | copied |
| `/swm/siteMapManagement/index` | `src/views/swm/siteMapManagement/index.vue` | `src/views/swm/siteMapManagement/index.vue` | copied |
| `/swm/alarmConfig/AlarmConfigList` | `src/views/swm/alarmConfig/AlarmConfigList.vue` | `src/views/swm/alarmConfig/AlarmConfigList.vue` | copied |
| `/swm/videoManagement/index` | `src/views/swm/videoManagement/index.vue` | `src/views/swm/videoManagement/index.vue` | copied |
| `/emergencyInquiry` | `src/views/emergencyInquiry/index.vue` | `src/views/emergencyInquiry/index.vue` | not found in old or new path |
| `swm/rawMessageLog/index` | `src/views/swm/rawMessageLog/index.vue` | `src/views/swm/rawMessageLog/index.vue` | copied |
| `/swm/externalCoordinateData/index` | `src/views/swm/externalCoordinateData/index.vue` | `src/views/swm/externalCoordinateData/index.vue` | copied |
| `/swm/areaFenceData/index` | `src/views/swm/areaFenceData/index.vue` | `src/views/swm/areaFenceData/index.vue` | copied |
| `swm/tcpDeviceCommandLog/index` | `src/views/swm/tcpDeviceCommandLog/index.vue` | `src/views/swm/tcpDeviceCommandLog/index.vue` | copied |
| `swm/helmetLocation/index` | `src/views/swm/helmetLocation/index.vue` | `src/views/swm/helmetLocation/index.vue` | not found in old or new path |
| `/ssiip/swm/swmWorkshop/list` | `src/views/swm/swmWorkshop/list.vue` | `src/views/swm/swmWorkshop/list.vue` | copied |
| `/swm/swmProdLine/list` | `src/views/swm/swmProdLine/list.vue` | `src/views/swm/swmProdLine/list.vue` | copied |
| `/swm/swmWorkGroup/list` | `src/views/swm/swmWorkGroup/list.vue` | `src/views/swm/swmWorkGroup/list.vue` | copied |
| `/swm/helmetdevice/list` | `src/views/swm/helmetdevice/list.vue` | `src/views/swm/helmetdevice/list.vue` | copied |
| `/swm/beacon/BeaconList` | `src/views/swm/beacon/BeaconList.vue` | `src/views/swm/beacon/BeaconList.vue` | copied |
| `swm/beaconMap/beaconBase` | `src/views/swm/beaconMap/beaconBase.vue` | `src/views/swm/beaconMap/beaconBase.vue` | copied |
| `/swm/alarmLight/index` | `src/views/swm/alarmLight/index.vue` | `src/views/swm/alarmLight/index.vue` | copied |
| `/swm/dictType/list` | `src/views/swm/dictType/list.vue` | `src/views/swm/dictType/list.vue` | copied |

## Measured Migration State

- Compared `swm-frontend/src/views/swm` + `swm-frontend/src/api/swm` with the new project: 243 files checked, 23 identical, 220 changed, 0 missing.
- New project still contains these old or transitional patterns in `src/views/swm` and `src/api/swm`:
  - `@/utils/http/axios`: 11 hits.
  - `defHttp`: 35 hits.
  - `BasicTable`: 180 hits.
  - `BasicModal`: 203 hits.
  - `useTable`: 105 hits.
  - `useModal`: 221 hits.
  - `JeeSiteSelect`: 4 hits.
  - `jeesite`: 31 hits.
  - `corpCode`: 17 hits.
  - `@/api/sys`: 19 hits.
  - `/swm/swmDict`: 14 hits.
  - `listData`: 75 hits.
  - `saveData`: 11 hits.

## Major Problems

### 1. Frontend API paths still target JeeSite-style endpoints

The migrated SWM backend uses RuoYi-style controller paths such as:

- `/swm/warning/page`, `/swm/warning/get`, `/swm/warning/create`, `/swm/warning/update`, `/swm/warning/delete`
- `/swm/handle-record/page`
- `/swm/hazard-source/page`
- `/swm/person/page`
- `/swm/person-schedule/page`
- `/swm/daily-attendance/page`
- `/swm/site-map/page`
- `/swm/alarm-config/page`
- `/swm/alarm-light/page`
- `/swm/voice-template/page`
- `/swm/dict-type/page`
- `/swm/dict-data/page`

But the frontend still calls old endpoints such as:

- `/swm/warningManagement/listData`
- `/swm/handleRecord/listData`
- `/swm/swmPerson/listData`
- `/swm/personSchedule/listData`
- `/swm/swmDailyAttendance/listData`
- `/swm/swmSiteMapManagement/listData`
- `/swm/swmVoiceTemplate/listData`
- `/swm/swmDictType/listData`
- `/swm/swmDictData/listData`

Action: Rewrite every `src/api/swm/*.ts` module used by the requested pages to RuoYi-style APIs. Do not only rename functions; adapt request method, parameter names, pagination shape, and response shape.

### 2. Weekly attendance imports a missing API module

`src/views/swm/weekly/index.vue` imports `@/api/swm/weekly`, but `src/api/swm/weekly.ts` does not exist.

Action: Either create `src/api/swm/weekly.ts` backed by the migrated backend, or change the page to import the correct attendance API module.

### 3. Old request wrapper remains

The new project still has pages importing `@/utils/http/axios` and using `defHttp`. RuoYi Vue3 normally uses `@/config/axios` request helpers.

Action: Replace `defHttp` usage with `request.get/post/put/delete/download/upload` from `@/config/axios`, and adapt old `postJson` / `uploadFile` usages because the current request wrapper does not expose those methods.

### 4. Old SWM compatibility components dominate the pages

The requested pages still depend heavily on copied compatibility components:

- `@/components/swm/Table`
- `@/components/swm/Form`
- `@/components/swm/Modal`
- `@/components/swm/Drawer`
- `@/components/swm/Preview`
- `@/components/swm/Description`
- `@/components/swm/Dict`
- `@/components/swm/Popupinput`

Some of these resolve as files on disk, but TypeScript still reports module resolution errors for imports like `@/components/swm/Drawer`, `@/components/swm/Preview`, and `@/components/swm/Description` because they lack suitable `index.ts` exports or type declarations.

Action: Choose one migration direction:

1. Preferred: rewrite target CRUD pages with RuoYi Vue3 + Element Plus style (`ContentWrap`, `Search`, `el-table`, `Dialog`, generated API modules).
2. Transitional: repair `components/swm` exports/types so copied pages compile, then migrate page by page.

### 5. Old JeeSite dictionary and tenant remnants remain

Hits still exist for `JeeSiteSelect`, `jeesite`, `corpCode`, and SWM-local dict endpoints.

Action:

- Replace `JeeSiteSelect` with RuoYi dict/user/dept selector components or Element Plus select wrappers.
- Remove JeeSite copyright/comments when touching migrated files.
- Replace `corpCode` with RuoYi tenant/user context where genuinely needed, or remove it from request params if backend derives tenant from login context.
- Prefer system dict APIs (`/system/dict-data/*`) unless a real SWM-local dictionary table is intentionally retained.

### 6. Some listed paths are not present

`/emergencyInquiry` and `swm/helmetLocation/index` were not found at the listed old or new file paths.

Action: Confirm whether they were renamed, removed, or are under another module. Search by Chinese menu name or backend controller before implementing.

### 7. TypeScript check is currently failing

`pnpm ts:check` was blocked by a local Windows/pnpm temp-file permission error. Direct `vue-tsc` ran but timed out after producing many errors. SWM-related errors include:

- Missing `@/api/swm/weekly`.
- Missing module declarations for `@/components/swm/Drawer`, `Preview`, `Description`, `Popupinput`.
- `request.postJson` and `request.uploadFile` do not exist on the current request helper type.
- Multiple copied Vue files still import `defineProps` / `defineEmits` manually, conflicting with Vue macros.
- Several copied table columns do not satisfy current `BasicColumn` typing.

## Recommended Execution Order

1. Build a route/page inventory for the requested menu entries in RuoYi menu SQL or system menu data.
2. Fix hard compile blockers:
   - Add or remove `@/api/swm/weekly`.
   - Add `index.ts` exports for missing `components/swm` modules or replace those imports.
   - Replace `defHttp`, `postJson`, and `uploadFile`.
3. Rewrite `src/api/swm/*.ts` for the requested pages to match migrated RuoYi backend controller paths.
4. Convert CRUD pages one group at a time:
   - Safety: warning, handle record, hazard source, recall, voice template, SOS.
   - Personnel: person, board, schedule, daily/weekly/monthly attendance, track.
   - Config: color, site map, alarm config, video, raw logs, coordinates, fences, commands, workshop/prod-line/work-group, helmet device, beacon, alarm light, dict.
5. After each group, run a focused TypeScript check and browser smoke test for list/create/update/delete/export/import actions.
6. Remove old JeeSite vocabulary and tenant params from frontend requests after backend confirms tenant is derived from token context.
