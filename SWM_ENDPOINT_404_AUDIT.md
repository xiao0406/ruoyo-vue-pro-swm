# SWM 前端接口 404 检查报告

检查时间：2026-06-23

## 结论

当前 404 的主要原因不是网关或 `admin-api` 前缀问题，而是前端仍然保留大量 JeeSite 旧接口路径，例如：

- `/swm/organizationTree/getNodes`
- `/swm/personSchedule/listData`
- `/swm/swmDailyAttendance/listData`
- `/swm/personnelBoard/listData`
- `/swm/personTrack/getPersonPositions`

迁移后的 `yudao-module-swm` 后端大多使用 RuoYi/Yudao 风格路径，例如：

- `/swm/person-schedule/page`
- `/swm/daily-attendance/page`
- `/swm/personnel-board/page`
- `/swm/site-map/page`

所以会出现“前端页面有、接口在老项目有、但 8990 当前服务返回请求地址不存在”的情况。

## 本次已修复

### 后端补齐

已补齐组织树接口：

- `GET /admin-api/swm/organizationTree/getNodes`

已补齐本轮发现的旧前端兼容接口：

- `GET /admin-api/swm/safetyEducation/listData`
- `GET /admin-api/swm/swmSiteMapManagement/getEnabledMap`
- `GET /admin-api/swm/swmArea/listAll`
- `GET /admin-api/swm/common/options/companies`
- `GET /admin-api/swm/common/options/departments`
- `GET /admin-api/swm/common/options/prodLines`
- `GET /admin-api/swm/common/options/workGroups`

涉及文件：

- `yudao-module-swm/src/main/java/cn/iocoder/yudao/module/swm/controller/admin/organization/SwmOrganizationTreeController.java`
- `yudao-module-swm/src/main/java/cn/iocoder/yudao/module/swm/service/SwmOrganizationTreeService.java`
- `yudao-module-swm/src/main/java/cn/iocoder/yudao/module/swm/service/impl/SwmOrganizationTreeServiceImpl.java`
- `yudao-module-swm/src/main/java/cn/iocoder/yudao/module/swm/dal/dataobject/TreeNode.java`
- `yudao-module-swm/src/main/resources/mapper/swm/SwmOrganizationTreeMapper.xml`
- `yudao-module-swm/src/main/java/cn/iocoder/yudao/module/swm/controller/admin/legacy/SwmLegacyCompatController.java`

### 前端路径迁移

已将人员管理主干查询接口从 JeeSite 旧路径调整到 RuoYi/Yudao 已存在路径：

- `/swm/swmDailyAttendance/listData` -> `/swm/daily-attendance/page`
- `/swm/swmDailyAttendance/get` -> `/swm/daily-attendance/get`
- `/swm/personnelBoard/listData` -> `/swm/personnel-board/page`
- `/swm/personSchedule/listData` -> `/swm/person-schedule/page`
- `/swm/personSchedule/form` -> `/swm/person-schedule/get`
- `/swm/personSchedule/save` -> `/swm/person-schedule/create` 或 `/swm/person-schedule/update`
- `/swm/personSchedule/delete` -> `/swm/person-schedule/delete`
- `/swm/scheduleTime/listData` -> `/swm/schedule-time/page`
- `/swm/scheduleTime/form` -> `/swm/schedule-time/get`
- `/swm/scheduleTime/save` -> `/swm/schedule-time/create` 或 `/swm/schedule-time/update`
- `/swm/scheduleTime/delete` -> `/swm/schedule-time/delete`

涉及文件：

- `yudao-ui/yudao-ui-admin-vue3/src/api/swm/dailyAttendance.ts`
- `yudao-ui/yudao-ui-admin-vue3/src/api/swm/personnelBoard.ts`
- `yudao-ui/yudao-ui-admin-vue3/src/api/swm/staffSchedule.ts`
- `yudao-ui/yudao-ui-admin-vue3/src/api/swm/scheduleTime.ts`

## 仍有 404 风险的重点接口

### 人员管理

这些接口仍需要继续迁移或补后端：

- `/swm/personSchedule/batchSave`
- `/swm/personSchedule/batchUpdateClasses`
- `/swm/personSchedule/deleteAll`
- `/swm/personSchedule/export`
- `/swm/personSchedule/getPersonIdList`
- `/swm/personSchedule/getWorkGroups`
- `/swm/personSchedule/import`
- `/swm/personSchedule/importTemplate`
- `/swm/personSchedule/monthStats`
- `/swm/scheduleTime/deleteAll`
- `/swm/scheduleTime/getAllScheduleTime`
- `/swm/scheduleTime/saveAll`
- `/swm/daily-attendance/export-excel`
- `/swm/swmDailyAttendance/exportData`
- `/swm/swmDailyAttendance/exportMonthlyData`
- `/swm/swmDailyAttendance/exportWeeklyListData`
- `/swm/swmDailyAttendance/findAttendanceRange`
- `/swm/swmDailyAttendance/monthlyListData`
- `/swm/swmDailyAttendance/save`
- `/swm/swmDailyAttendance/weeklyListData`
- `/swm/swmAttendanceSummary/attendanceDetails`
- `/swm/swmAttendanceSummary/exportData`
- `/swm/swmAttendanceSummary/listData`
- `/swm/personTrack/getAreaFenceDataByIdCard`
- `/swm/personTrack/getAreaFenceDataByIdCardByDateTime`
- `/swm/personTrack/getMqttDevicePositionTrajectory`
- `/swm/personTrack/getMqttPersonPositions`
- `/swm/personTrack/getOrgTree`
- `/swm/personTrack/getPersonPositions`
- `/swm/personTrack/getPersonPositionsNew`
- `/swm/personTrack/getPersonTrajectory`
- `/swm/personTrack/getPersonTrajectoryByDateTime`
- `/swm/personTrack/searchPerson`

### 配置管理

- `/swm/areaFenceData/listData`
- `/swm/areaFenceData/exportData`
- `/swm/externalCoordinateData/listData`
- `/swm/externalCoordinateData/exportData`
- `/swm/swmRawMessageLog/listData`
- `/swm/swmRawMessageLog/exportData`
- `/swm/swmTcpDeviceCommandLog/listData`
- `/swm/swmTcpDeviceCommandLog/exportData`
- `/swm/fmsPositionArchive/pageList`
- `/swm/fmsPositionArchive/save`
- `/swm/fmsProdLine/pageList`
- `/swm/fmsProdLine/save`
- `/swm/fmsProdLine/getCode`
- `/swm/fmsWorkGroup/pageList`
- `/swm/fmsWorkGroup/save`
- `/swm/swmDictData/listData`
- `/swm/swmDictData/treeData`
- `/swm/swmDictType/listData`
- `/swm/swmDictType/treeData`

### 安全管理

- `/swm/warning/popup-warnings`
- `/swm/warning/export-excel`
- `/swm/warning/enum-options`
- `/swm/warningManagement/form`
- `/swm/warningManagement/sendSOSAlarm`
- `/swm/warningManagement/sosExport`
- `/swm/warningManagement/sosListData`
- `/swm/handle-record/export-excel`
- `/swm/handle-record/unhandled-warnings`
- `/swm/safetyEducation/save`
- `/swm/safetyEducation/delete`
- `/swm/safetyEducation/form`
- `/swm/safetyEducation/complete`
- `/swm/safetyEducation/upload`
- `/swm/safetyEducation/download`
- `/swm/safetyEducation/fileList`

### 驾驶舱

- `/swm/dashboard/overview`
- `/swm/dashboard/attendance/dashboard`
- `/swm/dashboard/warningRecordsForPast7Days`
- `/swm/dashboard/warningRecordsForPast7DaysNew`
- `/swm/dashboard2/attendance/list`
- `/swm/dashboard2/attendance/manageList`
- `/swm/dashboard2/attendance/workList`
- `/swm/dashboard2/abnormalAttendance/list`
- `/swm/dashboard2/worker/list`
- `/swm/dashboard2/working/list`
- `/swm/dashboard2/beLatePerson`
- `/swm/dashboard2/leaveEarlyPerson`
- `/swm/dashboard2/noAttendancePerson`
- `/swm/dashboard2/noAttendancePersonExport`
- `/swm/dashboard2/departmentAttendanceAnalysis`
- `/swm/dashboard2/teamAttendanceAnalysis`
- `/swm/dashboard2/hourWorkingCount24`
- `/swm/dashboard2/hourNightWorkingCount`
- `/swm/dashboard2/personIdleHoursRanking`
- `/swm/dashboard2/personManagementOnDuty`

## 后续迁移建议

1. 先修人员管理页面，因为当前用户已经在验证这些页面。
2. 能直接映射到现有 RuoYi controller 的接口，优先改前端路径。
3. 旧接口包含复杂业务逻辑的，例如人员轨迹、考勤周/月汇总、驾驶舱统计，不要只做空壳 alias，需要从 `cscec-jeesite-cloud-swm` 迁移 service/mapper 逻辑。
4. 对导入、导出、批量保存类接口，需要按 Yudao 标准重新接 Excel 工具和 `@RequestBody` 参数。
5. 接口迁移完成后，再执行一次前端 URL 与后端 mapping 的差异扫描，直到请求地址不存在类 404 清零。

## 当前验证状态

后端 Maven 编译仍被本机 Maven 仓库权限阻塞：

- `E:\repository\maven\com\aliyun\tea\resolver-status.properties`
- 错误：`拒绝访问`

该问题发生在依赖解析阶段，不是当前新增代码明确报出的 Java 编译错误。
