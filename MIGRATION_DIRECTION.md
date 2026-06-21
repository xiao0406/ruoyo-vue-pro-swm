# Agent 执行文件：SWM / IOT JeeSite 到 RuoYi 迁移

生成时间：2026-06-21

## 读取说明

你正在接手 JeeSite 到 RuoYi / yudao 的迁移任务。本文件是执行依据，请按顺序推进。

本次迁移不是“补几个兼容方法让代码编译”，而是完成框架迁移、模块边界整理、租户模型替换、目录风格统一、旧业务能力补齐。

允许修改的新项目：

- `yudao-module-swm`
- `yudao-module-iot-biz`
- 必要时新增公共模块，例如 `yudao-module-swm-api`

只作为参考的旧项目，不要修改：

- `cscec-jeesite-cloud-swm`
- `cscec-jessite-cloud-iot`

参考资料：

- JeeSite 官方文档：https://jeesite.com/docs/
- RuoYi-Vue-Pro 多租户文档：https://doc.iocoder.cn/saas-tenant/
- RuoYi-Vue-Pro 官方仓库：https://gitee.com/zhijiantianya/ruoyi-vue-pro

## 核心结论

1. JeeSite 旧项目里的 `corpCode`、`corpName`、`CorpUtils`、`getSqlMap()`、`QueryType`、`CrudService`、`BaseEntity/DataEntity`、JeeSite 工具类都不是迁移目标。
2. RuoYi-Vue-Pro 的租户主线是 `system_tenant.id`、`tenant_id`、`TenantContextHolder`、`TenantUtils.execute(...)`、`TenantBaseDO`。
3. SWM 新模块已有不少代码，但仍保留大量 JeeSite 调用习惯，目前不能编译。
4. IOT 新模块缺口更大，且直接依赖 SWM 的 `dal`、`service`、`cache`、`constant`、`config`，模块边界不正确。
5. 两个新模块 resources 为空，旧项目中的 Mapper XML / 配置尚未系统迁移。

## 当前对比结果

| 项目 | Java 文件 | resources 文件 | XML |
|---|---:|---:|---:|
| `cscec-jeesite-cloud-swm/swm/swm` | 297 | 52 | 48 |
| `yudao-module-swm` | 362 | 0 | 0 |
| `cscec-jessite-cloud-iot/iot/iot` | 231 | 14 | 11 |
| `yudao-module-iot-biz` | 167 | 0 | 0 |

已执行编译命令：

```powershell
$env:JAVA_HOME='C:\Users\xy\.jdks\temurin-17.0.19'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
& 'C:\Users\xy\.m2\wrapper\dists\apache-maven-3.9.9-bin\4nf9hui3q3djbarqar9g711ggc\apache-maven-3.9.9\bin\mvn.cmd' -pl yudao-module-swm,yudao-module-iot-biz -am -DskipTests compile
```

结果：

- 构建停在 `yudao-module-swm`。
- `yudao-module-iot-biz` 尚未进入编译阶段。
- 编译错误集中在 SWM 的 `job/task`、旧租户模型、旧 Service 方法、旧 DO 字段、旧查询 DSL。

## 必须先建立的模块边界

当前 `yudao-module-iot-biz` 大量直接引用 `cn.iocoder.yudao.module.swm...` 下的实体类、常量、缓存、服务、配置类。这个边界不正确。

推荐新增公共模块：

```text
yudao-module-swm-api
  src/main/java/cn/iocoder/yudao/module/swm/api
    constant
    dto
    enums
    service
```

目标依赖方向：

```text
yudao-module-swm      -> yudao-module-swm-api
yudao-module-iot-biz  -> yudao-module-swm-api
```

避免长期存在：

```text
yudao-module-iot-biz  -> yudao-module-swm
```

公共模块可以放：

- SWM / IOT 共享 DTO
- Redis key 常量
- MQ topic / queue / routing key 常量
- MQTT topic 常量
- TCP 协议常量
- TDengine 超级表名、字段名、消息类型常量
- 设备类型、告警类型、人员状态、数据同步类型等枚举
- `tenantId -> TDengine dbName` 的映射接口或配置对象
- IOT 需要调用的 SWM API 接口定义

公共模块不要放：

- MyBatis Mapper
- 持久化 `*DO`，除非确实是跨模块数据契约；一般改成 DTO
- Service 实现类
- Controller
- Job / Task
- 具体 Spring `@Configuration`
- 直接依赖数据库、RedisTemplate、MQ 客户端、TDengine 客户端的实现类

IOT 当前错误边界必须改：

- IOT DO 不要继承 `SwmBaseDO`，应建立 `IotBaseDO extends TenantBaseDO`，或直接继承 `TenantBaseDO`。
- IOT 不要 import `swm.dal.dataobject.*`，改为使用 `swm.api.dto.*`。
- IOT 不要直接注入 SWM 业务 Service，改为调用公共 API。
- IOT 不要引用 SWM `config`、`job`、`controller`、`service.impl`。

## 租户迁移规则

### RuoYi 正确租户链路

RuoYi-Vue-Pro 字段隔离多租户的主线是：

```text
system_tenant.id
-> TenantContextHolder / TenantUtils
-> tenant_id
-> MyBatis Plus 自动租户过滤
```

本地代码依据：

- `TenantContextHolder`：保存当前线程租户 ID。
- `TenantUtils.execute(Long tenantId, Runnable runnable)`：切换到指定租户执行逻辑，并自动恢复旧上下文。
- `TenantBaseDO`：提供 `tenantId` 字段。
- `TenantDO`：RuoYi 租户对象，主键是 `id`，租户名称是 `name`，没有 `corpCode/corpName`。
- `TenantService.getTenantIdList()` / `getTenantListByStatus(...)`：获取租户列表。

错误旧链路：

```text
corpCode / corpName
-> CorpUtils ThreadLocal
-> 手动拼接 corp_code 条件
```

### 必须删除的旧代码

以下 JeeSite 租户上下文代码必须删除：

```java
CorpUtils.setCurrentCorpCode(corpCode, corpName);
CorpUtils.setCurrentCorpCode(corpCode);
CorpUtils.getCurrentCorpCode();
CorpUtils.getCurrentCorpName();
CorpUtils.removeCurrentCorpCode();
```

不要继续新增或扩展 `CorpUtils` 兼容方法。

### 定时任务迁移

旧写法：

```java
for (User user : corpList) {
    String corpCode = user.getCorpCode();
    String corpName = user.getCorpName();
    CorpUtils.setCurrentCorpCode(corpCode, corpName);
    // 执行业务
}
```

新写法：

```java
for (Long tenantId : tenantService.getTenantIdList()) {
    TenantUtils.execute(tenantId, () -> {
        // 执行业务，Mapper 自动按 tenant_id 过滤
    });
}
```

或：

```java
for (TenantDO tenant : tenantService.getTenantListByStatus(CommonStatusEnum.ENABLE.getStatus())) {
    TenantUtils.execute(tenant.getId(), () -> {
        String tenantName = tenant.getName(); // 只用于日志或展示
    });
}
```

如果项目可使用 `@TenantJob`，优先使用它让框架遍历租户。

不要：

- 构造 `AdminUserDO` 获取企业列表。
- 调用 `TenantContext.set(corpCode)`。
- 手动传 `corpCode` 到 Service。
- 给 `TenantDO` 增加 `getTenantId()` 兼容方法。

### `corpCode/corpName` 保留规则

新业务主流程禁止使用 `corpCode/corpName`。

允许短期保留的边界场景：

- 旧 TDengine 按企业编码分库，需要建立历史映射。
- 外部设备协议、MQTT/TCP 报文中确实包含旧企业编码。
- 历史数据迁移脚本需要从旧 `corp_code` 映射到新 `tenant_id`。

保留时必须满足：

1. 业务主流程仍使用 `tenantId`。
2. 转换方法命名体现边界含义，例如：
   - `resolveLegacyCorpCodeByTenantId`
   - `resolveTenantIdByLegacyCorpCode`
   - `resolveTdengineDbNameByTenantId`
3. 转换逻辑集中放在公共 API / mapping 层。
4. 最终交付必须列出 `corpCode/corpName` 保留点和原因。

## 按链路迁移方案

### 1. HTTP 管理接口

RuoYi 管理端接口应统一为：

- `@RestController`
- 返回 `CommonResult<T>`
- 分页返回 `PageResult<T>`
- 权限使用 `@PreAuthorize("@ss.hasPermission('...')")`
- 请求对象使用 `ReqVO`
- 返回对象使用 `RespVO`
- 创建/更新对象使用 `SaveReqVO`
- 分页查询对象使用 `PageReqVO`

不要迁移 JeeSite 页面跳转、Beetl 视图、`Model`、`RedirectAttributes`、JeeSite tag。

Controller 不要接收 `corpCode/corpName` 作为租户过滤条件。普通查询依赖当前租户上下文和 MyBatis Plus 自动过滤。

### 2. Service 层

旧 JeeSite Service 常见特点：

- 继承 `CrudService<Dao, Entity>`
- 使用 `dao.findList(...)`、`dao.get(...)`、`super.save(...)`
- Service 中混合缓存、定时任务、MQ、TDengine、Excel 等业务

新 RuoYi Service 应统一为：

- 接口 `XxxService`
- 实现 `XxxServiceImpl`
- Mapper 使用 `BaseMapperX`
- CRUD 使用 `insert`、`updateById`、`selectById`、`selectPage`
- 查询使用 `LambdaQueryWrapper` 或 Mapper XML
- 事务使用 `@Transactional`

重点：不要只补旧方法签名让编译通过。必须对照旧 Service 实现逐项迁移业务。

例如旧 `SwmHelmetDeviceService` 不只是 CRUD，它包含：

- 设备启动缓存初始化
- Redis 设备缓存
- 设备编号查询
- TDengine 设备数据
- MQ 同步
- Excel 导入导出
- 设备在线状态、低电量、告警关联

当前新 `SwmHelmetDeviceServiceImpl` 更接近代码生成 CRUD 骨架，业务能力不足。需要建立迁移清单：

```text
旧方法名 -> 新方法名 -> 是否迁移 -> 新位置 -> 是否需要 XML -> 是否需要租户上下文 -> 验证方式
```

### 3. Mapper / DAO / XML

旧项目有大量 `mappings/modules/.../*Dao.xml`。新模块 resources 当前为空，不能默认丢弃。

新 RuoYi Mapper 结构：

- `dal/mysql/XxxMapper.java`
- 继承 `BaseMapperX<XxxDO>`
- 简单查询用 `LambdaQueryWrapper`
- 复杂 SQL 迁到 `src/main/resources/mapper/.../*Mapper.xml`

每个旧 XML 必须做处置标记：

- 已由 MyBatis-Plus CRUD 覆盖
- 需要迁移为 Mapper XML
- 已废弃
- 等待业务确认

迁移 XML 时：

- namespace 改为新 Mapper。
- 字段改为新 DO 字段。
- `corp_code/corp_name` 改为 `tenant_id` 或删除租户条件交给插件。
- 手写 SQL 如绕过插件，必须显式使用当前 `tenantId`。
- `status = '0'` 要结合 `@TableLogic` 和旧表结构判断，不能盲删。

旧 SWM 重点 XML：

- `SwmHelmetDeviceDao.xml`
- `SwmPersonDao.xml`
- `SwmDailyAttendanceDao.xml`
- `SwmAttendanceSummaryDao.xml`
- `SwmPersonScheduleDao.xml`
- `SwmBeaconStationDao.xml`
- `SwmAreaDao.xml`
- `SwmHazardSourceDao.xml`
- `SwmInspectionPlanDao.xml`
- `SwmInspectionListDao.xml`
- `SwmSafetyFileManageDao.xml`
- `SwmWarningManagementDao.xml`
- `PersonTrackDao.xml`

旧 IOT 重点 XML：

- `IotDeviceDao.xml`
- `HelmetDeviceDao.xml`
- `BeaconStationDao.xml`
- `PersonTrackDao.xml`
- `TcpSendMessageLogDao.xml`
- `VoiceTemplateDao.xml`
- `OneKeyRecallDao.xml`

### 4. DO / Entity / VO / DTO

旧 JeeSite Entity 不要原封不动迁移。

新对象分层：

- `DO`：数据库对象，放 `dal/dataobject`
- `ReqVO`：请求对象
- `RespVO`：响应对象
- `SaveReqVO`：创建/更新对象
- `PageReqVO`：分页查询对象
- `DTO`：跨模块或内部传输对象
- `ExcelVO` / ImportVO：导入导出对象

处理规则：

- 删除 `page`、`sqlMap`、`corpCode/corpName` 等 JeeSite 字段。
- `status` 如果是逻辑删除，保留并配置 `@TableLogic`。
- `status` 如果是业务状态，改为业务字段和枚举。
- 导入导出字段拆到 Excel 对象，不要污染 DO。
- SWM / IOT DO 都要有 `tenantId`。

### 5. Redis / Cache

RuoYi 多租户文档说明 Redis 无法通过 `tenant_id` 字段隔离，需要 key 带租户维度，例如 `:t{tenantId}`。

迁移规则：

- Spring Cache 优先使用 RuoYi 租户 Redis 封装。
- 手写 RedisTemplate / StringRedisTemplate 时，key 必须包含租户维度。
- 设备入口需要的 `deviceId -> tenantId` 可作为全局索引，但必须标注为全局索引，不能混入业务缓存。
- 旧 `DEVICE_TO_CORP` 应改为设备到租户映射。
- 旧 `corp` 命名全部改为 `tenant`。

推荐：

```text
swm:device:index:{deviceId} -> tenantId
swm:device:tenant:t{tenantId}
```

禁止：

```text
deviceId -> corpCode
```

### 6. MQ / MQTT / TCP / WebSocket

RuoYi MQ 层会将租户编号放入消息头 `tenant-id`，消费时恢复租户上下文。

但 MQTT / TCP / WebSocket 外部设备入口通常没有 HTTP Header，也不一定经过 RuoYi MQ 封装，因此必须在协议入口解析租户。

推荐链路：

```text
设备上报
-> 解析 deviceId / helmetId / beaconId
-> 查询 deviceId -> tenantId
-> TenantUtils.execute(tenantId)
-> 写 DB / Redis / TDengine / MQ
-> 触发告警、定位、考勤、安全帽状态等业务
```

禁止链路：

```text
设备上报
-> corpCode
-> CorpUtils.setCurrentCorpCode
-> service.setCorpCode
```

IOT 的 TCP/MQTT/WebSocket 处理器不要直接依赖 SWM 内部 Service。需要 SWM 能力时，通过公共 API 调用。

### 7. TDengine

TDengine 是特殊边界。旧系统可能按企业编码分库，但业务层仍必须只认 `tenantId`。

迁移规则：

- 将 `CorpDbEnum` 改为更准确的公共映射，例如 `TenantTdengineDbMapping`。
- 映射键统一为 `tenantId`，值为 TDengine `dbName`。
- 如果历史只有 `corpCode -> dbName`，建立一次性映射：

```text
legacyCorpCode -> tenantId -> dbName
```

`TdengineRestClient` 对外方法改为：

```java
executeByCurrentTenant(String sql)
executeByTenantId(String sql, Long tenantId)
```

删除或废弃：

```java
executeTDengineSQLByXXJOB(String sql, String corpCode)
CorpUtils.getCurrentCorpCode()
CorpDbEnum.getDbNameByCorpCode(...)
```

SQL 中库名解析集中在 TDengine 客户端处理，不要让业务层到处字符串替换库名。

### 8. 文件 / OSS / MinIO / Excel

旧 JeeSite OSS、busifilemanager、ExcelImport 等要替换。

迁移规则：

- 文件上传下载优先接入 RuoYi `infra` 文件能力。
- MinIO 只作为具体存储实现，不要把 MinIO 业务逻辑散落在 SWM/IOT。
- 旧文件记录表如果保留，迁移为标准 DO + Mapper。
- Excel 导入导出使用 RuoYi Excel starter / EasyExcel。
- 旧 `ExcelImport`、JeeSite 文件管理类全部替换。

### 9. 工具类与返回对象

| 旧工具 | 新方案 |
|---|---|
| `R` | `CommonResult` 或内部 DTO |
| `UserUtils` | `SecurityFrameworkUtils` / system API |
| `DictUtils` | RuoYi dict API / DictFrameworkUtils |
| `CacheUtils` | Spring Cache / RedisTemplate |
| `Global` | Spring `@ConfigurationProperties` |
| `BatchOperationsUtil` | MyBatis-Plus 批处理或自定义 util |
| `FieldUtil` | BeanUtils / MapStruct / 谨慎使用反射 |
| `MqSendUtil` | RuoYi MQ producer |

工具类能删就删。必须保留的工具类放 `util`，不要保留 `utils` 和 JeeSite 命名。

### 10. 配置文件与启动配置

旧 resources 中的：

- `beetl.properties`
- `bootstrap.yml`
- `logback-spring.xml`
- `views/...`

处理方式：

- Beetl 和 views 属于旧页面渲染体系，通常废弃。
- bootstrap.yml 中的业务配置拆到 RuoYi application 配置或当前配置中心方案。
- logback 通常沿用 RuoYi 项目统一配置。
- Mapper XML 按需迁移到新模块 resources。
- 模块新增配置属性类使用 `@ConfigurationProperties`，不要读取 JeeSite `Global`。

## 目录和命名风格

统一结构：

```text
cn.iocoder.yudao.module.swm
  api
    constant
    dto
    enums
  controller.admin.xxx
  dal.dataobject.xxx
  dal.mysql.xxx
  service.xxx
  service.xxx.impl
  job.task
  mq
  util
```

```text
cn.iocoder.yudao.module.iot
  api
    constant
    dto
    enums
  controller.admin.xxx
  dal.dataobject.xxx
  dal.mysql.xxx
  service.xxx
  service.xxx.impl
  job.task
  mqtt
  tcp
  websocket
  util
```

常量命名：

| 类型 | 推荐命名 | 示例 |
|---|---|---|
| Redis key | `XxxRedisKeyConstants` | `SwmRedisKeyConstants` |
| MQ 常量 | `XxxMqConstants` | `SwmMqConstants` |
| MQTT 常量 | `XxxMqttConstants` | `IotMqttConstants` |
| TCP 常量 | `XxxTcpConstants` | `IotTcpConstants` |
| TDengine 常量 | `XxxTdengineConstants` | `SwmTdengineConstants` |
| 业务枚举 | `XxxEnum` | `DeviceTypeEnum` |
| 配置属性 | `XxxProperties` | `TdengineProperties` |
| 配置类 | `XxxConfiguration` | `MqttConfiguration` |

避免：

- `SwmRedisConstant`
- `MqttConstant`
- `DebugConstant`
- `TdengineSuperTableConstant`
- 常量类放在 `enums` 包
- 同时存在 `util` 和 `utils`

## 当前优先修复文件

SWM 当前编译阻塞集中在：

- `yudao-module-swm/src/main/java/cn/iocoder/yudao/module/swm/job/task/AttendanceTask.java`
- `yudao-module-swm/src/main/java/cn/iocoder/yudao/module/swm/job/task/ClearTdengineTableDataTask.java`
- `yudao-module-swm/src/main/java/cn/iocoder/yudao/module/swm/job/task/DeviceCorpTask.java`
- `yudao-module-swm/src/main/java/cn/iocoder/yudao/module/swm/job/task/InspectionPlanTask.java`
- `yudao-module-swm/src/main/java/cn/iocoder/yudao/module/swm/job/task/PersonScheduleTask.java`
- `yudao-module-swm/src/main/java/cn/iocoder/yudao/module/swm/job/task/SafetyManageTask.java`

典型错误类型：

- `AdminUserDO` 被当作企业/租户对象使用。
- `TenantDO.getTenantId()` 不存在。
- `getCorpCode()` / `getCorpName()` 不存在。
- `setCorpCode()` / `setCorpName()` / `setRandom()` 旧 DO 方法不存在。
- `getSqlMap()` / `QueryType` 旧查询 DSL 不存在。
- `executeTDengineSQLByXXJOB(String, String)` 仍使用 `corpCode`。
- Service 里缺少旧业务方法，但不能用空实现糊过去。

## IOT 功能覆盖缺口

旧 IOT Java 文件 231，新 IOT Java 文件 167，存在明显缺口。

旧项目中未按同名出现在新模块的重点类包括：

- 告警灯相关：`AlarmLightApiClient`、`AlarmLightController`、`AlarmLightService`、`VoiceAlarmController`
- 地图/定位相关：`JIAIMapService`、`CoordinateConverter`、`MapMatchAlgorithm`、`ProjectionGroupBuilder`
- TCP / WebSocket 相关：`TcpServerController`、`TcpSendMessageController`、`WebSocketService`
- 设备与 TDengine 相关：`IotDeviceDao`、`TDengineController`、`TdengineServiceImpl`
- 一键召回相关：`OneKeyRecallController`、`OneKeyRecallDao`
- 文件上传相关：`IotFileUploadDao`、`IotFileProcessServiceImpl`
- 缓存刷新相关：`UnifiedCacheRefreshController`、`UnifiedCacheRefreshService`
- 人员轨迹相关：`PersonTrackController`、`PersonTrackService`
- 语音模板相关：`VoiceTemplateController`、`VoiceTemplateService`

迁移顺序：

1. 先迁移 MQTT/TCP/WebSocket 运行链路直接依赖的类。
2. 再迁移设备、告警、定位、TDengine、Redis 相关服务。
3. 再迁移管理端 Controller 和 VO。
4. 最后迁移测试 Controller、工具类、兼容接口。

## 推荐执行顺序

### P0：先做

1. 新增或规划 `yudao-module-swm-api`。
2. 把 SWM / IOT 共享 DTO、常量、枚举、轻量 API 放入公共模块。
3. 改掉 IOT 对 SWM `dal`、`service.impl`、`config`、`job`、`controller` 的直接依赖。
4. 修复 `yudao-module-swm` 编译错误。
5. 清理 SWM `job/task` 下旧租户模型。
6. 替换 `getSqlMap()` / `QueryType`。
7. 补齐真实 Service 方法，不要空实现。

### P1：随后做

1. 逐个判断旧 SWM Mapper XML：迁移、CRUD 覆盖、废弃、待确认。
2. 逐个判断旧 IOT Mapper XML。
3. 补齐 IOT 缺失业务域。
4. 编译 `yudao-module-iot-biz` 并修复错误。
5. 清理 JeeSite import、JeeSite 工具、旧返回对象、旧配置。

### P2：联调验证

1. 启动 `yudao-server`。
2. 验证后台 Controller。
3. 验证定时任务按租户执行。
4. 验证 MQTT / TCP / WebSocket。
5. 验证 Redis / TDengine。
6. 验证 MQ 消费租户上下文。
7. 验证多租户数据隔离。

## 常用检查命令

### 编译 SWM 与 IOT

```powershell
$env:JAVA_HOME='C:\Users\xy\.jdks\temurin-17.0.19'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
& 'C:\Users\xy\.m2\wrapper\dists\apache-maven-3.9.9-bin\4nf9hui3q3djbarqar9g711ggc\apache-maven-3.9.9\bin\mvn.cmd' -pl yudao-module-swm,yudao-module-iot-biz -am -DskipTests compile
```

### 检查 JeeSite 残留

```powershell
Get-ChildItem -Path yudao-module-swm,yudao-module-iot-biz -Recurse -Include *.java,*.xml,*.yml,*.yaml,*.properties -File |
  Select-String -Pattern 'jeesite|com\.jeesite|org\.jeesite|AdminUserDO|getSqlMap\(|QueryType|disableAutoAddCorpCodeWhere' -CaseSensitive:$false
```

### 检查 IOT 直接依赖 SWM 内部实现

```powershell
Get-ChildItem yudao-module-iot-biz\src\main\java -Recurse -Include *.java -File |
  Select-String -Pattern 'cn\.iocoder\.yudao\.module\.swm\.dal|cn\.iocoder\.yudao\.module\.swm\.service|cn\.iocoder\.yudao\.module\.swm\.config|cn\.iocoder\.yudao\.module\.swm\.job|cn\.iocoder\.yudao\.module\.swm\.controller'
```

### 检查 `corpCode/corpName`

```powershell
Get-ChildItem -Path yudao-module-swm,yudao-module-iot-biz -Recurse -Include *.java,*.xml -File |
  Select-String -Pattern 'corpCode|corpName|getCorpCode\(|setCorpCode\(|getCorpName\(|setCorpName\(' -CaseSensitive:$false
```

### 检查旧假实现

```powershell
Get-ChildItem -Path yudao-module-swm,yudao-module-iot-biz -Recurse -Include *.java -File |
  Select-String -Pattern 'return null|UnsupportedOperationException|TODO.*迁移|not yet migrated|兼容 JeeSite' -CaseSensitive:$false
```

## 验收清单

迁移完成前至少满足：

- [ ] `mvn -pl yudao-module-swm -am -DskipTests compile` 通过。
- [ ] `mvn -pl yudao-module-iot-biz -am -DskipTests compile` 通过。
- [ ] `mvn -pl yudao-server -am -DskipTests compile` 通过。
- [ ] 已建立 SWM / IOT 共享公共层，例如 `yudao-module-swm-api`。
- [ ] IOT 不再直接依赖 SWM 的 `dal`、`service.impl`、`job`、`controller`、`config`。
- [ ] 共享常量、枚举、DTO 命名风格统一。
- [ ] 新模块无 `com.jeesite` / `org.jeesite` import。
- [ ] 新模块无 `getSqlMap()` / `QueryType` 运行代码。
- [ ] 新模块无 `AdminUserDO` 被当作企业租户使用。
- [ ] 业务逻辑无 `CorpUtils.setCurrentCorpCode(...)`。
- [ ] `corpCode/corpName` 只保留在明确边界转换层，并有清单说明原因。
- [ ] 旧 SWM Mapper XML 已逐个判断迁移或废弃。
- [ ] 旧 IOT Mapper XML 已逐个判断迁移或废弃。
- [ ] 旧 IOT 缺失功能类已按业务域补齐或明确废弃。
- [ ] 所有定时任务能启动并跑通核心流程。
- [ ] MQTT / TCP / WebSocket 能正常启动和处理消息。
- [ ] Redis key、TDengine 库名、租户 ID 映射已验证。
- [ ] 多租户数据隔离已验证。

## Agent 交付要求

执行 Agent 完成后，应交付：

1. 修改过的模块和新增公共模块说明。
2. 已迁移的旧功能域列表。
3. 放弃迁移或暂缓迁移的旧类 / 旧 XML 清单及原因。
4. 编译命令执行结果。
5. JeeSite 残留扫描结果。
6. IOT 对 SWM 内部依赖扫描结果。
7. `corpCode/corpName` 保留点清单及保留原因。
8. 尚未完成的运行时联调项。
