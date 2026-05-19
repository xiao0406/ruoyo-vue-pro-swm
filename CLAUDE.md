# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

CSCEC-Jeesite-Cloud-Swm — 中建集团智慧工地安全帽人员管理系统，基于 JeeSite Cloud 5.0.1 微服务架构。

- **Root POM**: `com.jeesite:CSCEC-Jeesite-Cloud-Swm`（聚合模块，pom）
- **Business module**: `swm/swm` → `jeesite-cloud-module-swm`（jar, port 8992）
- **Client module**: `swm/swm-client` → `jeesite-cloud-module-swm-client`（jar, Feign API definition）
- **Java 8**, Spring Boot + Spring Cloud + Nacos (discovery & config on `10.50.103.166:8848`, namespace `swm`)
- **Database**: SQL Server（via mssql-jdbc） + TDengine（时序数据）
- Start class: `com.jeesite.modules.SwmApplication`

## Build & Run Commands

```bash
# Build entire project (skip tests)
mvn clean install -DskipTests

# Build single module
mvn clean install -pl swm/swm -DskipTests

# Run locally (needs Nacos)
cd swm/swm && mvn spring-boot:run

# Package jar
mvn clean package -pl swm/swm
```

The project depends on JeeSite internal nexus repositories (`maven.jeesite.net`, `10.50.103.163:8081`). Build will fail outside the corporate network without access.

## Module Architecture

```
CSCEC-Jeesite-Cloud-Swm/           (pom, 聚合)
├── swm/swm/                        (jar, 业务服务)
│   └── src/main/java/com/jeesite/modules/
│       ├── SwmApplication.java    (@SpringBootApplication + @EnableFeignClients + @EnableDiscoveryClient)
│       ├── config/                (RabbitMQ, WebMVC, ThreadPool, OkHttp)
│       ├── job/                   (XXL-Job 定时任务)
│       ├── web/                   (全局 Controller: AiController, SwmTestController)
│       └── swm/                   (核心业务包)
│           ├── web/               (56+ Controllers, extends BaseController)
│           ├── service/           (60+ Services, extends CrudService<Dao, Entity>)
│           ├── dao/               (40+ DAOs, @MyBatisDao + extends CrudDao)
│           ├── entity/            (实体, DTO, VO, extends DataEntity)
│           ├── mq/                (RabbitMQ: producers/consumers/queues)
│           ├── cache/             (Redis 缓存)
│           ├── excel/             (EasyExcel import/export listeners)
│           └── util/              (IdCardUtil, MqSendUtil, RsaUtil, SignatureUtil)
└── swm/swm-client/                (jar, Feign API)
    └── src/main/java/com/jeesite/modules/
        ├── api/                   (Feign 接口: SwmSendZjtServiceApi 等)
        ├── client/                (Feign 实现: extends Feign API)
        ├── constant/              (SwmRedisConstant, TdengineSuperTableConstant)
        ├── enums/                 (CorpDbEnum)
        ├── vo/                    (DeviceDataDTO, SwmAlarmConfigDetailVO 等)
        └── utils/                 (BatchOperationsUtil, R, BimFace 等)
```

## Technology Stack

| Tech | Usage |
|------|-------|
| Spring Cloud + Nacos | 服务注册发现 & 配置中心 |
| MyBatis (JeeSite CrudDao) | ORM，实体用 `@Table`/`@Column` 注解映射，SQL 写在 `mappings/modules/swm/*.xml` |
| RabbitMQ | 人员/设备/信标变更通知，含死信队列 |
| Redis | 缓存（在职人员、设备映射、在线设备集合） |
| TDengine | 时序数据库（坐标轨迹、电量、围栏数据） |
| XXL-Job 2.3.0 | 分布式调度（全量同步、考勤、巡检等） |
| EasyExcel | 人员 Excel 导入导出 |
| DingTalk SDK | 钉钉消息推送 |
| OpenFeign | 跨服务调用（调用 bpm, fms, tpi 等模块） |

## Core Business Domains

- **人员管理** (`SwmPerson`, `SwmPersonDeparture`) — 在职/离职，身份证绑定，安全帽绑定
- **安全帽设备** (`SwmHelmetDevice`) — 设备注册、参数配置、绑定/解绑
- **区域 & 信标** (`SwmArea`, `SwmBeaconStation`) — 工厂区域划分，蓝牙信标定位
- **考勤** (`SwmDailyAttendance`, `SwmAttendanceSummary`) — 日常考勤汇总
- **安全教育** (`SwmSafetyEducation`) — 入场安全教育记录
- **巡检** (`SwmInspectionPlan`, `SwmInspectionList`) — 巡检计划与任务
- **报警** (`SwmAlarmConfig`, `SwmHiddenDanger`) — 报警规则与隐患管理
- **组织架构** (`FmsPositionArchive`, `FmsProdLine`, `FmsWorkGroup`) — 车间/产线/班组（来自 fms 模块）

## JeeSite Framework Patterns

**Controller → Service → DAO → MyBatis XML → Database**

```java
// Controller pattern
@Controller
@RequestMapping(value = "${adminPath}/swmXxx")
public class SwmXxxController extends BaseController {
    @Autowired private SwmXxxService service;

    @ModelAttribute
    public SwmXxx get(String id, boolean isNewRecord) { return service.get(id, isNewRecord); }

    @RequestMapping(value = "listData")
    @ResponseBody
    public Page<SwmXxx> listData(SwmXxx entity, HttpServletRequest req, HttpServletResponse res) {
        entity.setPage(new Page<>(req, res));
        return service.findPage(entity);
    }

    @PostMapping(value = "save")
    @ResponseBody
    public String save(@Validated SwmXxx entity) {
        service.save(entity);
        return renderResult(Global.TRUE, text("保存成功！"));
    }
}

// Entity pattern
@Table(name = "swm_xxx", alias = "a", columns = {
    @Column(name = "id", attrName = "id", isPK = true),
    @Column(includeEntity = DataEntity.class),  // createBy, createDate, updateBy, updateDate, remarks
    @Column(includeEntity = BaseEntity.class),  // status, corpCode, corpName
})
@Data
public class SwmXxx extends DataEntity<SwmXxx> { ... }
```

**Key conventions:**
- Controllers return `Map<String, Object>` or use `renderResult(Global.TRUE/FALSE, text("..."))` for save/delete
- Validation via `@Length`/`@NotBlank` on getters, NOT on fields
- Multi-tenant via `corpCode`/`corpName` fields on every table
- Entity enums defined as inner static classes with string constants (not Java enums)
- Some Controllers have heavy manual Map-building for listData responses

## RabbitMQ

Three domains with full DLQ chains: `person_change_*`, `device_change_*`, `beacon_change_*`. Config in `RabbitMqConfig.java`. JSON serialization (`Jackson2JsonMessageConverter`), 3-retry exponential backoff.

## External Service Dependencies

| Feign Client | Module |
|---|---|
| `jeesite-cloud-module-core-client` | 核心（组织架构） |
| `jeesite-cloud-module-bpm-client` | 业务流程 |
| `jeesite-cloud-module-tpi-client` | 中建通消息推送 |
| `jeesite-cloud-module-fms-client` | 车间/产线/班组（FMS） |

## Gotchas

- **Circular dependency**: SwmPersonService ↔ SwmPersonCacheService — resolved via `ApplicationContext.getBean()` lazy lookup
- **Worktree awareness**: Claude Code sessions may create git worktrees under `.claude/worktrees/`. Always verify you're editing the right path (main project: `E:\ideaProject\cscec-jeesite-cloud-swm`). Use `git branch --show-current` to confirm.
- **Import listeners**: Excel imports use EasyExcel `AnalysisEventListener` — they live in `swm/swm/*/excel/` and require the same patterns as Controllers
- **Identity card parsing**: Use `IdCardUtil.fillGenderAndAge(SwmPerson)` — auto-fills gender (男/女) and age from 18-digit ID card
- **Nacos config**: Main config is `swm.yml` on Nacos, not in the codebase. Local `bootstrap.yml` only points to Nacos address

---

## Behavioral Guidelines

**Tradeoff:** These guidelines bias toward caution over speed. For trivial tasks, use judgment.

### 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:
- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them - don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

## 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

## 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:
- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it - don't delete it.

When your changes create orphans:
- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

## 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:
- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:
```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]

Strong success criteria let you loop independently. Weak criteria ("make it work") require constant clarification.

---

**These guidelines are working if:** fewer unnecessary changes in diffs, fewer rewrites due to overcomplication, and clarifying questions come before implementation rather than after mistakes.
