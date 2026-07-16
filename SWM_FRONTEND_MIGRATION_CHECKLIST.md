# SWM Frontend Migration Checklist

This file is for Codex/agent handoff during the SWM JeeSite-to-RuoYi frontend migration.

## Current Goal

Make every page under `yudao-ui/yudao-ui-admin-vue3/src/views/swm` usable, with list/create/update/delete/detail/export/import operations calling valid RuoYi backend endpoints.

## Reference Projects

- Old frontend reference: `swm-frontend`
- New frontend target: `yudao-ui/yudao-ui-admin-vue3`
- Backend module: `yudao-module-swm`

## Completed In This Pass

- Rebuilt SWM `BasicForm.vue` to support:
  - search actions
  - API options
  - RuoYi dict options
  - `MonthPicker`
  - `FormGroup`, `Divider`, `IconPicker` compatibility
- Fixed SWM `useTable().getForm()` to return the real table search form instance.
- Fixed SWM dict bridge to load `/system/dict-data/simple-list`.
- Rebuilt daily attendance page labels and columns.
- Fixed several frontend API modules:
  - `alarmConfig.ts`: update now uses `PUT /swm/alarm-config/update`
  - `alarmLight.ts`: update now uses `PUT /swm/alarm-light/update`
  - `siteMap.ts`: rewritten against `/swm/site-map/*`; enable/disable use update status
  - `dangerDisposal.ts`: rewritten against `/swm/danger-disposal/*`
  - `fileUpload.ts`: rewritten against RuoYi `/infra/file/*`
  - `safetyVideos.ts`: rewritten against `/swm/media-file/*`
- Fixed `useForm()` so modal forms actually delegate to the registered `BasicForm` instance.
- Implemented dynamic schema operations in `BasicForm` (`updateSchema`, `resetSchema`, `removeSchemaByFiled`, `appendSchemaByField`).
- Added backend legacy compatibility for:
  - `/swm/personSchedule/deleteAll`
  - `/swm/personSchedule/export`
  - `/swm/personSchedule/import`
  - `/swm/personSchedule/importTemplate`
  - `/swm/alarm-light/config-list`
  - `/swm/alarm-light/create-config`
  - `/swm/alarm-light/update-config`
  - `/swm/alarm-light/delete-config`
- Added/confirmed SWM action compatibility for copied JeeSite pages:
  - warning POST update, enum options, popup warnings, confirm warning
  - handle-record POST update and unhandled-warning selector
  - hazard-source temp-save, find-by-beacon, inspection records, not-patrolled list
  - helmet-device batch delete, config get/save, battery update, charge reminder, usage records, condition lookup
  - legacy `/swm/swmPersonDeparture/findListByIdentityCard`
  - legacy `/swm/swmAttendanceSummary/listData` and `/swm/swmAttendanceSummary/exportData`
  - legacy `/swm/swmArea/saveAreaWithBeaconIds`
- Migrated `personWorkArea.ts` from old `/swm/swmPersonWorkArea/*` to RuoYi `/swm/person-work-area/*`.
- Migrated dashboard voice template lookup from old `/swm/swmVoiceTemplate/findStatusZero` to `/swm/voice-template/active-list`.

## High-Risk Remaining Areas

- `helmetConfig.ts`: still uses old `/swm/swmHelmetConfig/*` and `/swm/swmBeaconColorConfig/*`
- dashboard/data screen APIs: many old reporting endpoints are only compatibility stubs
- person track APIs: many old trajectory endpoints are compatibility stubs
- schedule import/export endpoints are still legacy compatibility or reserved
- Some compatibility endpoints intentionally return empty lists or success placeholders. They prevent SWM pages from crashing/404ing, but real business data still requires mapping to the final RuoYi domain services.

## Verification

- Frontend typecheck command:
  ```powershell
  $env:NODE_OPTIONS='--max-old-space-size=8192'; node ./node_modules/vue-tsc/bin/vue-tsc.js --noEmit --incremental false --pretty false
  ```
- Latest frontend typecheck: passed.
- Backend compile has not been run because local `mvn` and `mvnw` are not available.
