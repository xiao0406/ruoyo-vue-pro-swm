# CSCEC-Jeesite-Cloud-SWM (dev-ssiip分支) 项目分析报告

## 一、项目概述

### 1.1 基本信息
- **项目名称**: CSCEC-Jeesite-Cloud-SWM（中建智慧工地管理系统）
- **版本**: 5.0.1-SNAPSHOT
- **分支**: dev-ssiip（相比dev分支有349个文件变更）
- **技术框架**: JeeSite Cloud 5.0.1（基于Spring Boot + Spring Cloud的微服务架构）
- **应用端口**: 8992
- **应用名称**: swm
- **启动类**: `com.jeesite.modules.SwmApplication`

### 1.2 项目定位
基于JeeSite框架构建的企业级智慧工地管理云平台，提供人员管理、考勤统计、设备监控、预警管理、数据可视化等核心功能。

---

## 二、技术栈与依赖

### 2.1 核心技术栈

| 技术 | 说明 | 用途 |
|------|------|------|
| **Spring Boot** | 应用框架 | 基础应用容器 |
| **Spring Cloud** | 微服务架构 | 服务治理 |
| **Nacos** | 注册/配置中心 | 服务发现与配置管理（10.50.103.166:8848） |
| **OpenFeign** | 服务调用 | 微服务间通信 |
| **MyBatis** | ORM框架 | 数据持久化 |
| **RabbitMQ** | 消息队列 | 异步消息处理 |
| **XXL-JOB** | 任务调度 | 分布式定时任务（v2.3.0） |
| **TDengine** | 时序数据库 | IoT设备位置数据存储 |
| **SQL Server** | 关系数据库 | 业务数据存储（jdbc:sqlserver） |
| **EasyExcel** | Excel处理 | 数据导入导出 |
| **Beetl** | 模板引擎 | 视图渲染 |
| **Redis** | 缓存中间件 | 数据缓存 |
| **Swagger** | API文档 | 接口文档生成 |

### 2.2 Maven模块结构

```
CSCEC-Jeesite-Cloud-SWM (父POM)
├── swm/swm (主服务模块)
│   ├── Controller层: 50+ REST API控制器
│   ├── Service层: 60+ 业务服务类
│   ├── DAO层: 47 数据访问接口
│   ├── Entity层: 40+ 实体类
│   └── MyBatis XML: 47 映射文件
└── swm/swm-client (客户端模块)
    ├── Feign Client: 3个远程服务接口
    ├── API定义: 安全文件、人员培训、中建通消息
    └── 共享实体/枚举/工具类
```

---

## 三、项目目录结构详解

```
C:\Users\xy\.lingma\worktree\cscec-jeesite-cloud-swm\615IEq\
├── pom.xml                          # 父级Maven POM
├── README.md                        # 项目说明
├── .gitignore                       # Git忽略配置
│
├── docs/                            # 开发文档
│   ├── TDengine分页导出开发指南.md
│   └── 固定表格底部分页栏解决方案.md
│
└── swm/                             # SWM主模块
    ├── db/                          # 数据库脚本
    │   ├── 20260511.sql            # 最新数据库变更（2026-05-11）
    │   ├── 251104_人员登记增加部门和职务.sql
    │   └── 导入脚本.sql
    │
    ├── swm/                         # 核心服务模块
    │   ├── pom.xml
    │   └── src/main/
    │       ├── java/com/jeesite/modules/
    │       │   ├── SwmApplication.java          # Spring Boot启动类
    │       │   │
    │       │   ├── config/                      # 全局配置
    │       │   │   ├── CorpContextInterceptor.java      # 企业上下文拦截器（多租户）
    │       │   │   ├── CorpContextTaskDecorator.java    # 异步任务装饰器
    │       │   │   ├── CustomWebMvcConfigurer.java      # Web MVC配置
    │       │   │   ├── OkHttpClientManager.java         # HTTP客户端管理
    │       │   │   ├── RabbitMqConfig.java              # RabbitMQ配置
    │       │   │   ├── ThreadPoolTaskExecutorConfig.java # 线程池配置
    │       │   │   └── WebConfig.java                   # Web配置
    │       │   │
    │       │   ├── job/task/                        # 定时任务
    │       │   │   ├── AttendanceTask.java (208KB)        # 考勤计算核心任务
    │       │   │   ├── AiTask.java                      # AI相关任务
    │       │   │   ├── ClearTdengineTableDataTask.java  # TDengine数据清理
    │       │   │   ├── DeviceCorpTask.java              # 设备企业同步
    │       │   │   ├── InspectionPlanTask.java          # 巡检计划任务
    │       │   │   ├── PersonScheduleTask.java          # 人员排班任务
    │       │   │   ├── SafetyManageTask.java            # 安全管理任务
    │       │   │   ├── SwmBeaconStationFullSyncXxlJob.java # 信标站全量同步
    │       │   │   ├── SwmHelmetDeviceFullSyncXxlJob.java  # 安全帽设备全量同步
    │       │   │   └── SwmPersonFullSyncXxlJob.java        # 人员全量同步
    │       │   │
    │       │   ├── web/                             # 通用Web控制器
    │       │   │   ├── AiController.java
    │       │   │   ├── TableInfoController.java
    │       │   │   └── SwmTestController.java
    │       │   │
    │       │   ├── service/                         # 通用服务
    │       │   │   └── AiServiceImpl.java
    │       │   │
    │       │   ├── annotation/                      # 自定义注解
    │       │   │   └── SavePersonScheduleLog.java   # 排班日志注解
    │       │   │
    │       │   ├── enums/                           # 枚举类
    │       │   │   └── SyncDataOperateTypeEnum.java
    │       │   │
    │       │   └── swm/                             # ★核心业务模块★
    │       │       ├── aspect/                      # AOP切面
    │       │       │   └── PersonScheduleLogAspect.java
    │       │       │
    │       │       ├── cache/                       # 缓存管理
    │       │       │   ├── DeviceCorpMappingCache.java
    │       │       │   ├── SwmAreaCache.java
    │       │       │   └── SwmHazardSourceCache.java
    │       │       │
    │       │       ├── dao/                         # 数据访问层(47个DAO)
    │       │       │   ├── SwmPersonDao.java                # 人员DAO
    │       │       │   ├── SwmDailyAttendanceDao.java       # 日考勤DAO
    │       │       │   ├── SwmWarningManagementDao.java     # 预警管理DAO
    │       │       │   ├── SwmHelmetDeviceDao.java          # 安全帽设备DAO
    │       │       │   ├── SwmBeaconStationDao.java         # 信标基站DAO
    │       │       │   └── ... (共47个)
    │       │       │
    │       │       ├── entity/                      # 实体类(40+)
    │       │       │   ├── dto/                     # DTO对象
    │       │       │   │   ├── SwmAttendanceDto.java
    │       │       │   │   ├── SwmDashboardDto.java
    │       │       │   │   └── SwmPersonScheduleDto.java
    │       │       │   ├── vo/                      # VO对象
    │       │       │   │   ├── SwmAreaFenceDataVO.java
    │       │       │   │   ├── SwmExternalCoordinateDataVO.java
    │       │       │   │   └── ...
    │       │       │   ├── SwmPerson.java                    # 人员登记
    │       │       │   ├── SwmDailyAttendance.java           # 日考勤统计
    │       │       │   ├── SwmMonthlyAttendance.java         # 月考勤统计
    │       │       │   ├── SwmAttendanceSummary.java         # 考勤汇总
    │       │       │   ├── SwmHelmetDevice.java              # 安全帽设备
    │       │       │   ├── SwmBeaconStation.java             # 信标基站
    │       │       │   ├── SwmWarningManagement.java         # 预警管理
    │       │       │   ├── SwmAlarmLight.java                # 报警灯
    │       │       │   ├── SwmArea.java                      # 区域管理
    │       │       │   ├── SwmHazardSource.java              # 危险源
    │       │       │   ├── SwmInspectionPlan.java            # 巡检计划
    │       │       │   ├── SwmSafetyEducation.java           # 安全教育
    │       │       │   ├── SwmPersonnelBoard.java            # 人员看板
    │       │       │   ├── SwmScheduleTime.java              # 排班时间
    │       │       │   └── ... (共40+实体)
    │       │       │
    │       │       ├── service/                     # 业务服务层(60+)
    │       │       │   ├── impl/                    # 服务实现
    │       │       │   ├── SwmPersonService.java            # 人员服务
    │       │       │   ├── SwmDailyAttendanceService.java   # 日考勤服务
    │       │       │   ├── SwmWarningManagementService.java # 预警管理服务(126KB)
    │       │       │   ├── SwmHelmetDeviceService.java      # 安全帽设备服务
    │       │       │   ├── SwmHelmetCacheService.java       # 安全帽缓存服务
    │       │       │   ├── SwmPersonCacheService.java       # 人员缓存服务
    │       │       │   ├── PersonTrackService.java          # 人员轨迹服务(54KB)
    │       │       │   ├── AreaFenceDataService.java        # 区域围栏服务(67KB)
    │       │       │   ├── OrgValidationService.java        # 组织验证服务
    │       │       │   ├── SwmDashboardService.java         # 仪表盘服务
    │       │       │   └── ... (共60+服务)
    │       │       │
    │       │       ├── web/                         # Web控制器(50+)
    │       │       │   ├── SwmPersonController.java (102KB)         # 人员管理
    │       │       │   ├── SwmDailyAttendanceController.java (49KB) # 考勤管理
    │       │       │   ├── SwmDashboardController.java (91KB)       # 数据大屏
    │       │       │   ├── SwmDashboardNewController.java (70KB)    # 新数据大屏
    │       │       │   ├── PersonTrackController.java (68KB)        # 人员轨迹
    │       │       │   ├── SwmWarningManagementController.java (49KB) # 预警管理
    │       │       │   ├── SwmHelmetDeviceController.java           # 安全帽设备
    │       │       │   ├── SwmBeaconStationController.java          # 信标基站
    │       │       │   ├── SwmAreaController.java                   # 区域管理
    │       │       │   ├── SwmFileUploadController.java             # 文件上传
    │       │       │   └── ... (共50+控制器)
    │       │       │
    │       │       ├── mq/                          # 消息队列
    │       │       │   ├── consumer/                # 消费者
    │       │       │   │   ├── PersonChangeConsumer.java     # 人员变更消费
    │       │       │   │   ├── DeviceChangeConsumer.java     # 设备变更消费
    │       │       │   │   ├── BeaconChangeConsumer.java     # 信标变更消费
    │       │       │   │   └── AgencyMessageConsumer.java    # 代理消息消费
    │       │       │   ├── producer/                # 生产者
    │       │       │   │   ├── entity/
    │       │       │   │   └── RabbitMqSender.java
    │       │       │   └── SwmQueueKey.java
    │       │       │
    │       │       ├── excel/                       # Excel导入导出
    │       │       │   ├── SwmPersonImportListener.java
    │       │       │   ├── SwmPersonImportEnhancedListener.java
    │       │       │   ├── SwmPersonExcelModel.java
    │       │       │   └── SwmPersonExcelEnhancedModel.java
    │       │       │
    │       │       ├── util/                        # 工具类
    │       │       │   ├── MqSendUtil.java          # MQ发送工具
    │       │       │   ├── RsaUtil.java             # RSA加密工具
    │       │       │   └── SignatureUtil.java       # 签名工具
    │       │       │
    │       │       ├── param/                       # 参数对象
    │       │       │   └── SwmOneClickRecallSaveParam.java
    │       │       │
    │       │       └── job/                         # SWM特定任务
    │       │           └── FmsMonthPlanProlongTask.java
    │       │
    │       └── resources/
    │           ├── config/
    │           │   ├── bootstrap.yml               # 启动配置(Nacos)
    │           │   ├── logback-spring.xml          # 日志配置
    │           │   └── beetl.properties            # Beetl模板配置
    │           │
    │           ├── mappings/modules/swm/           # MyBatis XML(47个)
    │           │   ├── SwmPersonDao.xml
    │           │   ├── SwmDailyAttendanceDao.xml (51KB)
    │           │   ├── SwmWarningManagementDao.xml
    │           │   └── ... (共47个XML)
    │           │
    │           └── views/modules/swm/              # 视图模板
    │
    └── swm-client/                  # SWM客户端模块(Feign接口)
        ├── pom.xml
        └── src/main/java/com/jeesite/modules/
            ├── api/                 # API接口定义
            │   ├── SwmSafetyFileManageServiceApi.java
            │   ├── SwmSafetyPersonTrainingServiceApi.java
            │   └── SwmSendZjtServiceApi.java
            │
            ├── client/              # Feign客户端
            │   ├── SwmSafetyFileManageServiceClient.java
            │   ├── SwmSafetyPersonTrainingServiceClient.java
            │   └── SwmSendZjtServiceClient.java
            │
            ├── constant/            # 常量定义
            │   ├── DebugConstant.java
            │   ├── SwmRedisConstant.java
            │   └── TdengineSuperTableConstant.java
            ├── entity/              # 共享实体
            ├── enums/               # 枚举
            │   └── CorpDbEnum.java  # 多租户数据库映射
            ├── utils/               # 工具类
            │   └── R.java           # 响应封装
            ├── valid/               # 验证器
            └── vo/                  # 视图对象
                ├── DeviceDataDTO.java
                ├── QueryParamDTO.java
                └── SwmAlarmConfigDetailVO.java
```

---

## 四、核心业务模块详解

### 4.1 人员管理模块 (SwmPerson)

**实体字段**:
- 基本信息：姓名、身份证、手机号、照片
- 组织信息：所属单位、车间、产线、班组、工种
- 设备关联：安全帽编号(safetyHelmetId)
- 状态管理：人员状态(在职/离职)、厂内/厂外标识

**核心功能**:
- CRUD操作（支持多租户隔离）
- 批量导入导出（EasyExcel流式处理）
- 增强版导入（组织架构级联验证）
- 厂内/厂外员工智能字段控制
- 离职人员查询与管理

**关键文件**:
- Entity: `SwmPerson.java`
- DAO: `SwmPersonDao.java` + `SwmPersonDao.xml`
- Service: `SwmPersonService.java`
- Controller: `SwmPersonController.java` (102KB)
- Excel: `SwmPersonImportListener.java`, `SwmPersonImportEnhancedListener.java`

---

### 4.2 考勤管理模块

#### 4.2.1 日考勤 (SwmDailyAttendance)

**实体字段**:
- 员工信息：employeeId, employeeName, identityCard
- 考勤时间：attendanceDate, clockInTime, clockOutTime
- 工时统计：scheduledHours（应考勤）, actualHours（实际）, idleHours（怠工）, effectiveWorkHours（有效工作）
- 班次信息：classes（白班/夜班）
- 绩效指标：dailyEfficiency（今日功效）

**核心功能**:
- 上下班打卡记录
- 工时自动计算
- 考勤异常检测
- 按日期/员工/身份证多维度查询

**关键文件**:
- Entity: `SwmDailyAttendance.java`
- DAO: `SwmDailyAttendanceDao.java` + `SwmDailyAttendanceDao.xml` (51KB)
- Service: `SwmDailyAttendanceService.java` (75KB)
- Controller: `SwmDailyAttendanceController.java` (49KB)

#### 4.2.2 月度考勤汇总 (SwmAttendanceSummary)

**实体字段**:
- 统计周期：month (yyyy-MM格式)
- 出勤统计：scheduledDays, actualDays, attendanceRate
- 工时统计：scheduledHours, actualHours, idleHours
- 效率指标：efficiency（工效）, attendanceAchievementRate（考勤达成率）

**核心功能**:
- 月度考勤数据聚合
- 出勤率计算
- 工效分析
- 达成率统计

**关键文件**:
- Entity: `SwmAttendanceSummary.java`
- Service: `SwmAttendanceSummaryService.java` (27KB)

---

### 4.3 预警管理模块 (SwmWarningManagement)

**实体字段**:
- 预警信息：personName, warningType（主动/被动）, warningContent（危险源/跌落/静默等）
- 时间信息：warningTime, alarmTime
- 处置信息：handleStatus（未处置/已处置/草稿）, handler, disposalDuration
- 位置信息：hazardCategory, location, x坐标, y坐标

**核心功能**:
- 多种预警类型管理（危险源报警、跌落报警、静默报警、SOS等）
- 预警规则配置
- 预警处置流程
- 今日预警列表查询
- 预警统计分析
- 前端弹框提示
- 中建通消息推送集成

**关键文件**:
- Entity: `SwmWarningManagement.java`
- DAO: `SwmWarningManagementDao.java` + `SwmWarningManagementDao.xml`
- Service: `SwmWarningManagementService.java` (126KB - 最大服务类)
- Controller: `SwmWarningManagementController.java` (49KB)

---

### 4.4 安全帽设备管理 (SwmHelmetDevice)

**实体字段**:
- 设备信息：deviceId, deviceName, deviceType
- 绑定信息：personId, personName（关联人员）
- 状态信息：deviceStatus, batteryLevel
- 位置信息：lastLocation, lastUpdateTime

**核心功能**:
- 设备绑定/解绑
- 设备状态监控
- 电池电量管理
- 最后位置追踪
- 全量同步任务（SwmHelmetDeviceFullSyncXxlJob）

**关键文件**:
- Entity: `SwmHelmetDevice.java`
- DAO: `SwmHelmetDeviceDao.java` + `SwmHelmetDeviceDao.xml`
- Service: `SwmHelmetDeviceService.java`, `SwmHelmetCacheService.java`
- Controller: `SwmHelmetDeviceController.java`

---

### 4.5 信标基站管理 (SwmBeaconStation)

**实体字段**:
- 基站信息：stationId, stationName, macAddress
- 位置信息：location, x坐标, y坐标, z坐标
- 信号信息：signalStrength, coverageRadius
- 状态信息：stationStatus

**核心功能**:
- 基站位置管理
- 信号覆盖范围配置
- 基站状态监控
- 全量同步任务（SwmBeaconStationFullSyncXxlJob）

**关键文件**:
- Entity: `SwmBeaconStation.java`
- DAO: `SwmBeaconStationDao.java` + `SwmBeaconStationDao.xml`
- Service: `SwmBeaconStationService.java`
- Controller: `SwmBeaconStationController.java`

---

### 4.6 人员轨迹追踪 (PersonTrack)

**核心功能**:
- 实时位置追踪
- 历史轨迹查询
- 区域围栏判断
- 组织树查询
- 实时坐标获取

**技术特点**:
- 集成TDengine时序数据库存储位置数据
- 支持大规模并发位置更新
- 轨迹数据压缩与优化

**关键文件**:
- Service: `PersonTrackService.java` (54KB)
- Controller: `PersonTrackController.java` (68KB)
- DAO: `PersonTrackDao.java`

---

### 4.7 区域管理与电子围栏 (SwmArea)

**实体字段**:
- 区域信息：areaId, areaName, areaType
- 围栏数据：fenceCoordinates（多边形坐标）
- 权限信息：allowedPersonTypes, restrictedAreas
- 危险标识：isHazardArea, hazardLevel

**核心功能**:
- 电子围栏定义（多边形区域）
- 危险区域标识
- 区域权限控制
- 越界告警
- 区域人员统计

**关键文件**:
- Entity: `SwmArea.java`, `SwmAreaFenceData.java`
- Service: `AreaFenceDataService.java` (67KB)
- Controller: `SwmAreaController.java`
- Cache: `SwmAreaCache.java`

---

### 4.8 数据大屏 (SwmDashboard)

**核心功能**:
- 人员分布统计
- 出勤率分析
- 班次折线图
- 预警统计
- 实时数据展示
- 多维度数据分析

**接口路径**: `/dashboard2` (新版)

**关键文件**:
- DTO: `SwmDashboardDto.java`
- Service: `SwmDashboardService.java`
- Controller: `SwmDashboardController.java` (91KB), `SwmDashboardNewController.java` (70KB)

---

### 4.9 其他重要模块

#### 4.9.1 巡检管理
- Entity: `SwmInspectionPlan.java`, `SwmInspectionList.java`
- Task: `InspectionPlanTask.java`
- 功能：巡检计划制定、巡检记录管理

#### 4.9.2 安全教育
- Entity: `SwmSafetyEducation.java`
- 功能：安全教育培训记录、培训档案管理

#### 4.9.3 危险源管理
- Entity: `SwmHazardSource.java`
- Cache: `SwmHazardSourceCache.java`
- 功能：危险源识别、风险评估、管控措施

#### 4.9.4 隐患处置
- Entity: `SwmHiddenDanger.java`, `SwmHandleRecord.java`
- 功能：隐患发现、处置流程、整改跟踪

#### 4.9.5 排班管理
- Entity: `SwmPersonSchedule.java`, `SwmScheduleTime.java`
- Task: `PersonScheduleTask.java`
- Aspect: `PersonScheduleLogAspect.java`
- 功能：人员排班、班次时间管理、排班日志

#### 4.9.6 字典管理
- Entity: `SwmDictType.java`, `SwmDictData.java`
- 功能：系统字典配置、数据字典维护

#### 4.9.7 报警灯管理
- Entity: `SwmAlarmLight.java`, `SwmAlarmConfig.java`
- 功能：报警灯配置、报警策略管理

---

## 五、架构设计特点

### 5.1 分层架构

```
┌─────────────────────────────────────┐
│         Web层 (Controller)          │  ← REST API接口，50+控制器
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│        Service层 (业务逻辑)          │  ← 60+服务类，事务控制
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│         DAO层 (数据访问)             │  ← 47个MyBatis Mapper
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│      Database (SQL Server + TDengine)│
└─────────────────────────────────────┘

并行架构：
┌─────────────────────────────────────┐
│      MQ消息队列 (RabbitMQ)           │  ← 异步消息处理
└─────────────────────────────────────┘
┌─────────────────────────────────────┐
│      定时任务 (XXL-JOB)              │  ← 10+定时任务
└─────────────────────────────────────┘
```

### 5.2 消息驱动架构

**RabbitMQ配置** (`RabbitMqConfig.java`):

**主要队列**:
1. `person_change_queue` / `person_change_exchange` - 人员变更
2. `device_change_queue` / `device_change_exchange` - 设备变更
3. `beacon_change_queue` / `beacon_change_exchange` - 信标变更

**特性**:
- JSON序列化：`Jackson2JsonMessageConverter`
- 生产端重试：指数退避策略，最多3次
- 死信队列：每个队列都有对应的DLQ
- 消息确认：ConfirmCallback + ReturnsCallback

**消费者** (`mq/consumer/`):
- `PersonChangeConsumer.java` - 人员变更处理
- `DeviceChangeConsumer.java` - 设备变更处理
- `BeaconChangeConsumer.java` - 信标变更处理
- `AgencyMessageConsumer.java` - 代理消息处理

**生产者** (`mq/producer/`):
- `RabbitMqSender.java` - 统一消息发送

---

### 5.3 缓存策略

**缓存服务**:
- `SwmPersonCacheService` - 人员信息缓存（Redis）
- `SwmHelmetCacheService` - 安全帽设备缓存
- `SwmAreaCache` - 区域信息缓存
- `SwmHazardSourceCache` - 危险源缓存
- `DeviceCorpMappingCache` - 设备企业映射缓存

**缓存用途**:
- 减少数据库查询压力
- 提升高频访问数据性能
- 支持实时数据快速读取

---

### 5.4 定时任务调度

**XXL-JOB配置** (`XxlJobConfig.java`):

**核心任务**:
1. **AttendanceTask** (208KB) - 考勤计算核心
   - `calculateIdleHours` - 计算怠工时长
   - `monthlyAttendanceStatistics` - 月度考勤统计
   
2. **同步任务**:
   - `SwmPersonFullSyncXxlJob` - 人员全量同步
   - `SwmHelmetDeviceFullSyncXxlJob` - 安全帽设备全量同步
   - `SwmBeaconStationFullSyncXxlJob` - 信标基站全量同步

3. **其他任务**:
   - `ClearTdengineTableDataTask` - TDengine数据清理
   - `DeviceCorpTask` - 设备企业同步
   - `InspectionPlanTask` - 巡检计划任务
   - `PersonScheduleTask` - 人员排班任务
   - `SafetyManageTask` - 安全管理任务
   - `AiTask` - AI相关任务

---

### 5.5 多租户隔离

**企业上下文拦截器** (`CorpContextInterceptor.java`):

**实现机制**:
- 通过拦截器获取当前租户标识（corpCode）
- MyBatis自动拼接租户条件到SQL
- 特殊查询可绕过租户隔离（`findListWithoutCorpCode`）

**多租户数据库映射** (`CorpDbEnum.java`):
```java
ZJGGJS -> plb_ZJGGJS
ZJGGGD -> plb
ZJGGSC -> plb_ZJGGSC
ZJZK   -> plb_ZJZK
ZHY    -> plb_ZHY
```

---

### 5.6 微服务通信

**Feign Client** (`swm-client/`):

**远程服务接口**:
1. `SwmSafetyFileManageServiceClient` - 安全文件管理服务
2. `SwmSafetyPersonTrainingServiceClient` - 安全人员培训服务
3. `SwmSendZjtServiceClient` - 中建通消息发送服务

**配置示例**:
```java
@FeignClient(name="${service.swm.name}", path="${service.swm.path}")
```

---

## 六、数据库设计

### 6.1 数据库类型

- **主库**: SQL Server（jeesite核心业务数据）
- **时序库**: TDengine（人员定位轨迹、设备位置等时序数据）

### 6.2 核心数据表

根据实体类推断的主要表结构：

| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `swm_person` | 人员登记表 | id, name, identityCard, phoneNumber, department, team, jobType, safetyHelmetId, personnelStatus |
| `swm_daily_attendance` | 日考勤统计 | employeeId, attendanceDate, clockInTime, clockOutTime, scheduledHours, actualHours, idleHours |
| `swm_monthly_attendance` | 月考勤统计 | employeeId, month, attendanceDays, workHours |
| `swm_attendance_summary` | 考勤汇总 | employeeId, month, attendanceRate, efficiency, achievementRate |
| `swm_helmet_device` | 安全帽设备 | deviceId, personId, deviceStatus, batteryLevel, lastLocation |
| `swm_beacon_station` | 信标基站 | stationId, macAddress, location, x, y, z, signalStrength |
| `swm_warning_management` | 预警管理 | personName, warningType, warningContent, warningTime, handleStatus, handler |
| `swm_area` | 区域管理 | areaId, areaName, areaType, isHazardArea |
| `swm_area_fence_data` | 区域围栏 | areaId, fenceCoordinates |
| `swm_inspection_plan` | 巡检计划 | planId, planName, startDate, endDate |
| `swm_safety_education` | 安全教育 | educationId, personId, educationType, educationDate |
| `swm_hazard_source` | 危险源 | hazardId, hazardName, hazardLevel, location |
| `swm_alarm_light` | 报警灯 | lightId, lightName, status, config |
| `swm_schedule_time` | 排班时间 | scheduleId, startTime, endTime, shiftType |
| `swm_dict_type` | 字典类型 | dictTypeId, dictTypeName |
| `swm_dict_data` | 字典数据 | dictDataId, dictTypeId, dictLabel, dictValue |

### 6.3 TDengine超级表

**常量定义** (`TdengineSuperTableConstant.java`):
- `helmet_runde_ca_report_location` - 安全帽位置数据
- `area_fence_data` - 区域围栏数据
- `external_coordinate_data` - 外部坐标数据
- `mqtt_device_position` - MQTT设备位置数据

### 6.4 数据库变更管理

**位置**: `swm/db/`

**最新版本**:
- `20260511.sql` - 2026-05-11的数据库变更
- `251104_人员登记增加部门和职务.sql` - 人员登记字段扩展
- `导入脚本.sql` - 数据导入脚本

---

## 七、配置文件详解

### 7.1 Bootstrap配置 (`bootstrap.yml`)

```yaml
server:
  port: 8992

spring:
  application:
    name: swm
  profiles:
    active: swm  # 环境标识（不能用test，是单元测试专用）
  
  cloud:
    nacos:
      discovery:
        server-addr: 10.50.103.166:8848  # Nacos服务地址
        namespace: ${spring.profiles.active}  # 命名空间: swm
      
      config:
        server-addr: 10.50.103.166:8848
        namespace: ${spring.profiles.active}
        group: DEFAULT_GROUP
        file-extension: yml
        refresh-enabled: true  # 支持配置在线刷新
        
        # 扩展配置文件
        extension-configs[0]:
          data-id: swm-ext0.yml
          group: DEFAULT_GROUP
          refresh: true
        extension-configs[1]:
          data-id: swm-ext1.yml
          group: DEFAULT_GROUP
          refresh: true
```

**重要说明**:
- 敏感配置（数据库、RabbitMQ、TDengine等）存储在Nacos配置中心
- 本地仅保留基础配置
- 支持配置热更新

### 7.2 日志配置 (`logback-spring.xml`)

**日志级别**:
- Root: WARN
- `com.jeesite.modules.swm.dao.SwmWarningManagementDao`: INFO（屏蔽频繁告警查询日志）
- `io.seata`: INFO
- `zipkin2.reporter.AsyncReporter`: ERROR
- `org.springframework.cloud.openfeign.FeignClientFactoryBean`: ERROR

**日志输出**:
- 控制台：彩色输出，格式 `%d{MM-dd HH:mm:ss.SSS} %-5p [logger] - message`
- Debug文件：`${logPath}/logs/debug.log`，按天滚动，保留30天
- Error文件：`${logPath}/logs/error.log`，按天滚动，保留30天

### 7.3 其他配置

- **Beetl模板**: `beetl.properties` - 自定义静态方法导入
- **线程池**: `ThreadPoolTaskExecutorConfig.java` - 名为`swmExecutor`的线程池
- **HTTP客户端**: `OkHttpClientManager.java` - 用于TDengine REST API调用

---

## 八、dev-ssiip分支特性分析

### 8.1 分支概况

- **分支名称**: dev-ssiip
- **相比dev分支**: 349个文件变更
- **最近提交**: 
  - d274111 - 广东厂20260507批次需求开发
  - 9cac6da - 广东厂需求修改
  - dd3f1ce - 增加字典管理
  - f53ed1e ~ ba93aed - 报警功能代码优化（多次迭代）
  - 8a857db - 驾驶舱-今日数据-夜班逻辑修改
  - ea5f25b - 看板改造，动态区分白夜班

### 8.2 主要变更内容

根据git diff分析，dev-ssiip分支的主要变更包括：

#### 8.2.1 新增模块
1. **swm-client模块** - Feign客户端模块
   - 3个API接口定义
   - 3个Feign Client实现
   - 共享实体、枚举、工具类

2. **字典管理** - 系统字典功能
   - `SwmDictType` - 字典类型
   - `SwmDictData` - 字典数据

3. **AI相关功能**
   - `AiTask.java` - AI定时任务
   - `AiController.java` - AI接口
   - `AiServiceImpl.java` - AI服务实现
   - `AiDto.java` - AI数据传输对象

#### 8.2.2 功能优化
1. **报警功能优化** - 多次迭代优化
   - 报警逻辑重构
   - 报警性能优化
   - 报警数据处理优化

2. **看板改造**
   - 动态区分白夜班逻辑
   - 今日数据统计优化
   - 夜班统计从前30分钟开始

3. **考勤优化**
   - 班次逻辑优化
   - 考勤计算改进

4. **TDengine数据管理**
   - 定时删除数据任务
   - 数据清理逻辑优化
   - TDengine分页导出功能（见docs文档）

#### 8.2.3 配置增强
1. **多租户支持**
   - `CorpContextInterceptor` - 企业上下文拦截器
   - `CorpContextTaskDecorator` - 异步任务装饰器
   - `CorpDbEnum` - 多租户数据库映射

2. **缓存优化**
   - 新增多个缓存服务
   - 缓存策略优化

3. **消息队列增强**
   - RabbitMQ配置优化
   - 消息重试机制
   - 死信队列支持

#### 8.2.4 文档完善
1. **开发文档**
   - `TDengine分页导出开发指南.md`
   - `固定表格底部分页栏解决方案.md`

2. **代码注释**
   - 新增README.md文件
   - 关键类添加注释

---

## 九、关键技术实现

### 9.1 TDengine时序数据库集成

**配置项** (存储在Nacos):
```java
@Value("${tdengine.url}")           // TDengine HTTP API地址
@Value("${tdengine.authorization}") // 认证令牌
@Value("${tdengine.dbname}")        // 数据库名称
@Value("${tdengine.retentionPolicy}") // 数据保留策略
```

**使用场景**:
- 人员实时位置坐标存储
- 预警/报警记录存储
- 设备轨迹数据存储
- 历史轨迹查询

**优势**:
- 高吞吐写入（适合IoT设备高频上报）
- 高效时序查询
- 数据自动压缩
- 支持数据保留策略

---

### 9.2 EasyExcel智能导入导出

**核心类**:
- `SwmPersonImportListener.java` - 标准导入监听器
- `SwmPersonImportEnhancedListener.java` - 增强版导入监听器
- `SwmPersonExcelModel.java` - Excel数据模型
- `SwmPersonExcelEnhancedModel.java` - 增强版数据模型

**特性**:
- 流式处理（避免OOM）
- 组织架构级联验证
- 厂内/厂外员工智能字段控制
- 详细的错误信息反馈
- 支持大数据量导入

---

### 9.3 RSA安全通信

**工具类**: `RsaUtil.java`

**算法**:
- 加密：RSA/ECB/PKCS1Padding
- 签名：SHA1WithRSA

**用途**:
- 内部服务间安全通信
- 敏感数据加密传输
- 请求签名验证

---

### 9.4 异步处理

**线程池配置** (`ThreadPoolTaskExecutorConfig.java`):
```java
@Qualifier("swmExecutor")
@Autowired
private ThreadPoolTaskExecutor swmExecutor;
```

**使用场景**:
- 大数据量并行查询
- 异步统计计算
- 消息异步发送
- 后台任务处理

---

## 十、项目规模统计

| 类型 | 数量 | 说明 |
|------|------|------|
| Controller | 50+ | REST API控制器 |
| Service | 60+ | 业务服务类 |
| DAO | 47 | 数据访问接口 |
| Entity | 40+ | 实体类 |
| MyBatis XML | 47 | SQL映射文件 |
| 定时任务 | 10+ | XXL-JOB任务 |
| MQ Consumer | 4 | 消息消费者 |
| Feign Client | 3 | 远程服务调用 |
| 缓存服务 | 5 | Redis缓存服务 |
| 工具类 | 10+ | 通用工具类 |
| **总代码行数** | **约50万+** | 估算值 |

**最大文件**:
- `AttendanceTask.java` - 208KB（考勤计算核心）
- `SwmWarningManagementService.java` - 126KB（预警管理服务）
- `SwmPersonController.java` - 102KB（人员管理控制器）
- `SwmDashboardController.java` - 91KB（数据大屏控制器）

---

## 十一、部署架构

```
                    ┌─────────────────┐
                    │     Nacos       │
                    │ 注册/配置中心    │
                    │ 10.50.103.166   │
                    └────────┬────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
   ┌────▼────┐        ┌─────▼─────┐       ┌─────▼─────┐
   │  SWM    │        │ 其他微服务 │       │  Gateway  │
   │ :8992   │◄──────►│           │◄─────►│           │
   └────┬────┘        └───────────┘       └───────────┘
        │
   ┌────┴────────┐
   │  RabbitMQ   │
   │ 消息队列     │
   └────┬────────┘
        │
   ┌────┴──────────┐
   │  SQL Server   │  ← 业务数据
   │  TDengine     │  ← 时序数据
   │  Redis        │  ← 缓存数据
   └───────────────┘
```

---

## 十二、业务流程示例

### 12.1 考勤统计流程

```
1. XXL-JOB定时任务触发
   ↓
2. 查询所有在职人员 (SwmPersonService)
   ↓
3. 获取排班信息 (SwmPersonScheduleService)
   ↓
4. 从TDengine查询位置轨迹 (TDengineService)
   ↓
5. 计算打卡时间、工作时长、怠工时长
   ↓
6. 生成日考勤记录 (SwmDailyAttendance)
   ↓
7. 汇总生成月度考勤 (SwmAttendanceSummary)
   ↓
8. 计算出勤率、工效等指标
   ↓
9. 更新缓存 (SwmPersonCacheService)
```

### 12.2 预警处理流程

```
1. 设备上报异常数据（安全帽/SOS/越界等）
   ↓
2. 生成预警记录 (swm_warning_management)
   ↓
3. 前端弹框提示 (frontAlarm)
   ↓
4. 推送中建通消息 (SwmSendZjtService)
   ↓
5. 人工处置 (handle_status更新)
   ↓
6. 记录处置过程和时长
   ↓
7. 发送MQ消息通知相关系统
```

### 12.3 人员变更同步流程

```
1. 人员信息变更（新增/修改/离职）
   ↓
2. 发送MQ消息到 person_change_queue
   ↓
3. PersonChangeConsumer消费消息
   ↓
4. 更新相关缓存 (SwmPersonCacheService)
   ↓
5. 同步到其他微服务（通过Feign）
   ↓
6. 更新TDengine人员关联信息
```

---

## 十三、技术亮点

### 13.1 智能导入导出
- EasyExcel流式处理，支持百万级数据
- 组织架构级联验证，确保数据一致性
- 厂内/厂外员工智能字段控制
- 详细的错误信息反馈机制

### 13.2 实时数据处理
- RabbitMQ消息驱动，异步解耦
- 人员/设备/信标变更实时同步
- WebSocket实时推送（可能）
- 支持高并发位置上报

### 13.3 多数据源支持
- SQL Server：业务数据（ACID保证）
- TDengine：时序数据（高吞吐写入）
- Redis：缓存数据（高性能读取）

### 13.4 安全管理
- RSA加密工具，保障数据安全
- 签名验证，防止篡改
- 中建通消息推送集成
- 多租户数据隔离

### 13.5 钉钉集成
- 钉钉SDK 1.4.50
- 消息通知
- 可能的身份认证集成

### 13.6 分布式任务调度
- XXL-JOB统一管理
- 任务分片执行
- 故障自动转移
- 执行日志记录

---

## 十四、潜在优化点

### 14.1 性能优化
1. **大Service类拆分**
   - `SwmWarningManagementService` (126KB) 建议拆分为多个子服务
   - `AttendanceTask` (208KB) 建议按功能模块化

2. **数据库查询优化**
   - 复杂查询添加索引
   - 批量操作优化
   - 慢查询监控

3. **缓存策略优化**
   - 缓存穿透/击穿/雪崩防护
   - 缓存过期策略
   - 热点数据预加载

### 14.2 代码质量
1. **代码规范**
   - 统一异常处理
   - 完善日志记录
   - 增加单元测试

2. **设计模式应用**
   - 策略模式（不同预警类型处理）
   - 工厂模式（设备类型创建）
   - 观察者模式（事件通知）

### 14.3 安全性
1. **敏感信息保护**
   - 密码加密存储
   - API接口鉴权
   - SQL注入防护

2. **访问控制**
   - 细粒度权限控制
   - 操作审计日志
   - IP白名单

---

## 十五、开发建议

### 15.1 新功能开发
1. **遵循现有架构**
   - Controller → Service → DAO 分层
   - 使用JeeSite提供的基类
   - 遵循RESTful规范

2. **数据库设计**
   - 添加租户字段（corp_code）
   - 设计合理的索引
   - 考虑数据增长

3. **接口设计**
   - 使用Swagger注解
   - 统一的响应格式
   - 完善的参数校验

### 15.2 代码修改
1. **修改前**
   - 阅读相关代码
   - 理解业务逻辑
   - 评估影响范围

2. **修改中**
   - 保持代码风格一致
   - 添加必要的注释
   - 考虑边界情况

3. **修改后**
   - 运行单元测试
   - 验证功能正确性
   - 检查性能影响

### 15.3 调试技巧
1. **日志调试**
   - 使用SLF4J日志
   - 关键节点打点
   - 日志级别控制

2. **断点调试**
   - IDE远程调试
   - 条件断点
   - 异常断点

3. **数据追踪**
   - SQL日志查看
   - MQ消息追踪
   - 缓存数据检查

---

## 十六、总结

### 16.1 项目特点

这是一个**成熟的企业级智慧工地管理系统**，具有以下特点：

✅ **架构清晰**：标准的微服务分层架构  
✅ **技术先进**：Spring Cloud + Nacos + TDengine  
✅ **功能完善**：人员、考勤、设备、预警、大屏等核心功能  
✅ **性能优秀**：消息驱动、缓存优化、异步处理  
✅ **安全可靠**：多租户隔离、RSA加密、签名验证  
✅ **可扩展性**：模块化设计、Feign远程调用  

### 16.2 适用场景

- 建筑工地人员管理
- 工厂车间人员考勤
- IoT设备监控管理
- 实时位置追踪
- 安全预警管理
- 数据可视化展示

### 16.3 后续工作方向

1. **功能增强**
   - AI智能分析（已在开发）
   - 更多数据可视化
   - 移动端适配

2. **性能优化**
   - 大数据量查询优化
   - 缓存命中率提升
   - 异步处理优化

3. **安全加固**
   - 接口鉴权完善
   - 数据加密加强
   - 审计日志完善

---

**报告生成时间**: 2026-05-13  
**分析分支**: dev-ssiip  
**工作目录**: C:\Users\xy\.lingma\worktree\cscec-jeesite-cloud-swm\615IEq
