# SWM / IOT JeeSite to RuoYi Migration Handoff

Generated: 2026-06-22

This file is for the next coding agent. Treat it as the execution checklist for continuing the migration.

## Scope

Editable migrated modules:

- `yudao-module-swm`
- `yudao-module-iot-biz`
- `yudao-module-swm-api`

Reference-only old projects:

- `cscec-jeesite-cloud-swm`
- `cscec-jessite-cloud-iot`

Do not modify the old projects. Use them only to compare missing business behavior.

## Current Verified Baseline

Both migrated modules compile with tests skipped:

```powershell
$env:JAVA_HOME='C:\Users\xy\.jdks\temurin-17.0.19'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
& 'C:\Users\xy\.m2\wrapper\dists\apache-maven-3.9.9-bin\4nf9hui3q3djbarqar9g711ggc\apache-maven-3.9.9\bin\mvn.cmd' -q -pl yudao-module-iot-biz -am -DskipTests compile
& 'C:\Users\xy\.m2\wrapper\dists\apache-maven-3.9.9-bin\4nf9hui3q3djbarqar9g711ggc\apache-maven-3.9.9\bin\mvn.cmd' -q -pl yudao-module-swm -am -DskipTests compile
```

The latest scan under SWM, SWM API, and IOT source/resources finds no active hits for:

- `corpCode`
- `corpName`
- `corp_code`
- `corp_name`
- `CorpUtils`
- `setCurrentCorpCode`
- `CorpDbEnum`
- `DeviceCorp*`
- direct imports of SWM internal packages such as `swm.dal`, `swm.service`, `swm.controller`, `swm.util`

IOT may still import `cn.iocoder.yudao.module.swm.api.*`; this is the intended public API boundary.

## Migration Rules

Use RuoYi tenant model:

- Use `tenantId`.
- Use `TenantBaseDO` for tenant data objects.
- Use `TenantContextHolder` for current tenant.
- Use `TenantUtils.execute(tenantId, ...)` or `@TenantJob` for cross-tenant scheduled work.
- Let MyBatis tenant interceptor handle normal tenant filtering where possible.

Do not keep JeeSite tenant logic:

- Delete `CorpUtils.setCurrentCorpCode(...)`.
- Delete `CorpUtils.getCurrentCorpCode()` and `getCurrentCorpName()`.
- Do not pass `corpCode` / `corpName` through new service APIs.
- Do not rebuild a new compatibility `CorpUtils`.

If historical TDengine or external protocol data still contains old enterprise codes, isolate conversion in one mapping boundary with names such as:

- `resolveTenantIdByLegacyCorpCode`
- `resolveLegacyCorpCodeByTenantId`
- `resolveTdengineDbNameByTenantId`

## Work Already Done

IOT compile and boundary cleanup:

- Added `IotBaseDO extends TenantBaseDO`.
- Changed IOT DOs away from `SwmBaseDO`.
- Added IOT-local TDengine REST client and service boundary methods.
- Added IOT-local cache/service placeholders needed by TCP and MQTT handlers.
- Removed IOT direct dependency on SWM `dal/service/controller/util` internals.
- Replaced `corpCode` variable usage in IOT TCP/MQTT flow with tenant-key logic derived from `TenantContextHolder.getTenantId()`.
- Removed old `getCorpCode` compatibility method from `DeviceCorpMappingCache`.
- Replaced `CorpDbEnum` with `TenantDbEnum`.
- Renamed device mapping cache/job/Redis constants from corp terminology to tenant terminology.
- Migrated SWM Mapper/XML tenant filters from legacy enterprise columns to `tenant_id` / `tenantId`.
- Implemented IOT helmet device/config/cache services with Mapper and Redis behavior.
- Implemented TCP voice command dispatch through `VoiceAlarmService`.
- Restored business side effects for the main TCP alarm processors:
  - `HelmetOffTcpProcessor`
  - `LongTimeStaticTcpProcessor`
  - `HazardSourceTcpProcessor`
- Added real TDengine writes for previously empty/default-success helmet services:
  - helmet SOS
  - login
  - local record
  - push to client
  - upload photo
  - raw message
  - Runde CA report location
  - sending message
  - SIP SOS
- Added `TdengineJsonPayloadWriter` as a shared migration writer for JSON payload supertables.
- Added an IOT-side SWM warning bridge that records structured warning events in Redis instead of importing SWM internals.
- Added public SWM warning API in `yudao-module-swm-api` and implemented it in `yudao-module-swm`.
- Changed IOT warning bridge to call `SwmWarningApi`; Redis warning events are now a fallback/replay record.
- Removed `yudao-module-iot-biz` direct Maven dependency on `yudao-module-swm`; IOT now uses `yudao-module-swm-api`.
- Replaced IOT `RedisTask` dependency on System internal `TenantService/TenantDO` with `TenantCommonApi`.
- Replaced IOT `DictUtils` placeholder with RuoYi `DictDataApi` lookup.
- Replaced location/external-coordinate placeholders with deterministic local implementations:
  - BLE fallback location result based on the strongest beacon.
  - `external_coordinate_data` payload persistence through TDengine JSON writer.
- Wired Chat location algorithm calls:
  - Resolve algorithm base URL from RuoYi dict type `corp_code_chat_base_url`.
  - Dict value is the legacy project code such as `ZJGGGD`, `ZJGGJS`, `ZJGGSC`, `ZHY`, `ZJZK`.
  - Dict label is the algorithm base URL such as `http://192.168.0.115:8881`.
  - This dict name is an allowed external integration boundary; do not treat this literal as a JeeSite tenant-code residue.
- Implemented Beacon MAC cache and Geo/BLE comparison decision cache through Redis.
- Rewrote broken/garbled migrated handlers into compilable IOT-local versions:
  - `TcpMessageHandler`
  - `HelmetOffTcpProcessor`
  - `LongTimeStaticTcpProcessor`
  - `HazardSourceTcpProcessor`
  - `DeviceBatteryStatusHandler`
  - `DeviceFallAlarmHandler`
  - `DeviceSoSAlarmHandler`
  - `FenceAlarmMessageHandler`
  - `RedisUtil`
  - `HelmetDeviceRegistrationService`

Shared boundary:

- `SwmRedisKeyConstants` is currently used from `yudao-module-swm-api`; keep constants in API/common packages, not SWM internals.

## Important Caveat

The main TCP alarm handlers now have persistence/notification side effects again, but exact production parity still needs domain validation:

- Hazard-source voice text is safely derived from the hazard name when only Redis hazard data is available. If voice template text must match old JeeSite `swm_voice_template`, expose the template through SWM API/cache rather than querying SWM tables directly from IOT.
- Location engine now calls the Chat algorithm URL resolved from `corp_code_chat_base_url` and falls back to local BLE if the algorithm is unavailable or returns an error.
- IOT now depends on `yudao-module-system` because this repo has System APIs in the System module instead of a separate `system-api` module.

Do not reintroduce JeeSite code to fill those gaps. Compare old project behavior, then implement the equivalent using RuoYi module style.

## Next Agent Tasks

1. Finish optional production parity:
   - Add SWM public API/cache for voice template text if hazard-source voice must exactly match legacy templates.
   - Validate the `corp_code_chat_base_url` dictionary values in each environment.
   - Add tests around SWM warning API creation and IOT fallback Redis warning events.

2. Move only additional shared contracts to `yudao-module-swm-api`:
   - constants
   - enums
   - request/response DTOs
   - service API interfaces needed across modules

   Do not move mapper classes, controllers, service implementations, job classes, or Spring configuration into the API module.

3. Clean compatibility utility debt:
   - replace local `R` with `CommonResult` or a proper internal result type.
   - replace local `DictUtils` with yudao dictionary APIs.
   - remove temporary aliases named after SWM entities once proper DTOs/services exist.

4. Add focused tests and rerun both compile commands after every meaningful batch.

## Useful Scans

```powershell
Get-ChildItem yudao-module-iot-biz/src/main/java -Recurse -Include *.java |
  Select-String -Pattern 'corpCode|corpName|CorpUtils|setCurrentCorpCode|cn\.iocoder\.yudao\.module\.swm\.(dal|service|controller|framework|convert|enums|util|mapper)'

Get-ChildItem yudao-module-swm/src/main,yudao-module-swm-api/src/main -Recurse -Include *.java,*.xml |
  Select-String -Pattern 'corpCode|corpName|CorpUtils|setCurrentCorpCode|com\.jeesite|CrudService|QueryType|getSqlMap'
```
