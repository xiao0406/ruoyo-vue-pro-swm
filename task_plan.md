# SWM / IOT Migration Task Plan

## Goal

Complete the JeeSite-to-RuoYi migration for `yudao-module-swm`, `yudao-module-iot-biz`, and the shared API boundary.

## Current Status

| Phase | Status | Notes |
|---|---|---|
| Reconfirm compile blockers | done | Both migrated modules now compile with tests skipped. |
| Repair IOT compile blockers | done | Replaced broken JeeSite/SWM-dependent code with IOT-local RuoYi-style boundaries. |
| Remove active JeeSite tenant code | done | SWM/SWM API/IOT scans no longer find active `corpCode`, `corpName`, `CorpUtils`, `CorpDbEnum`, or device-corp mapping names. |
| Remove IOT direct SWM internals dependency | done | IOT no longer imports SWM `dal/service/controller/util` internals; `swm-api` imports are allowed. |
| Implement IOT service placeholders | done | Helmet device/config/cache services and voice alarm dispatch now have real Mapper/Redis/TCP behavior. |
| Restore full IOT business behavior | in_progress | Some simplified alarm handlers still need parity review for warning/TDengine details. |
| Final verification | done | SWM and IOT compile with tests skipped; focused tests still need to be added. |

## Next Agent Priorities

1. Add focused tests for SWM tenant-filtered mappers and IOT TCP/MQTT flows.
2. Compare simplified IOT alarm handlers with `cscec-jessite-cloud-iot` for warning-management and TDengine parity.
3. Replace temporary local utilities (`R`, `DictUtils`, SWM-named DTO aliases) with proper yudao/common APIs.
4. Keep shared constants/enums/DTO/service contracts in `yudao-module-swm-api`; do not let IOT import SWM internals again.

## Verification Commands

```powershell
$env:JAVA_HOME='C:\Users\xy\.jdks\temurin-17.0.19'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
& 'C:\Users\xy\.m2\wrapper\dists\apache-maven-3.9.9-bin\4nf9hui3q3djbarqar9g711ggc\apache-maven-3.9.9\bin\mvn.cmd' -q -pl yudao-module-iot-biz -am -DskipTests compile
& 'C:\Users\xy\.m2\wrapper\dists\apache-maven-3.9.9-bin\4nf9hui3q3djbarqar9g711ggc\apache-maven-3.9.9\bin\mvn.cmd' -q -pl yudao-module-swm -am -DskipTests compile
```

## Frontend Migration Audit

Goal: Compare legacy `swm-frontend` pages with migrated `yudao-ui/yudao-ui-admin-vue3` pages for the SWM screen, safety management, personnel management, and configuration management pages listed by the user.

| Phase | Status | Notes |
|---|---|---|
| Locate legacy and migrated frontend modules | done | Legacy is `swm-frontend`; target is `yudao-ui/yudao-ui-admin-vue3`. |
| Page-by-page presence comparison | done | Most requested pages are copied; `/emergencyInquiry` and `swm/helmetLocation/index` were not found at listed paths. |
| CRUD/API migration review | done | API modules still call old JeeSite-style endpoints and pages still use old compatibility components. |
| Report migration gaps | done | See `FRONTEND_MIGRATION_AUDIT.md`. |
| Runtime route/menu recheck | done | 31 menu entries match Vue files/component names; menu SQL now normalizes stale same-path records and hides duplicates. |
| Page-entry 404 compatibility | done | Added backend compatibility for old URLs still directly imported by requested migrated pages; empty responses remain where real migrated query services are absent. |
