# SWM Production Runtime Endpoints

Read-only capture from the production SWM subsystem. Request headers, bodies, responses, credentials and session data are intentionally omitted.

- Production menu pages: 40
- Pages successfully loaded during runtime capture: 31
- Pages requiring source-code fallback because the production server stopped responding: 9

| Page | Production route | Initial query endpoints |
|---|---|---|
| 慧眼安盾驾驶舱 | `/1937484785394728960` | `GET /js/swm/api/public/swm/monitorDeviceInfo/officeDeviceList`<br>`GET /js/swm/organizationTree/getNodes`<br>`GET /js/swm/swmArea/listAll`<br>`GET /js/swm/swmHelmetSubitem/findByParentId`<br>`GET /js/swm/swmSiteMapManagement/getEnabledMap`<br>`GET /js/swm/swmVoiceTemplate/findStatusZero`<br>`POST /js/swm/personTrack/getMqttPersonPositions`<br>`POST /js/swm/swmDictData/listData` |
| 异常状态报警记录 | `/swm/warning/WarningList` | `GET /js/swm/swmPerson/listData`<br>`GET /js/swm/warningManagement/getPopupWarnings`<br>`GET /js/swm/warningManagement/listData` |
| 报警处置记录 | `/swm/warningRecord/WarningRecordList` | `GET /js/swm/handleRecord/listData`<br>`GET /js/swm/swmPerson/listData`<br>`GET /js/swm/warningManagement/getPopupWarnings` |
| 危险区域闯入提示 | `/swm/hazardSource/HazardSourceList` | `GET /js/swm/warningManagement/getPopupWarnings`<br>`POST /js/swm/swmHazardSource/listData` |
| 应急召回 | `/swm/oneKeyRecall/oneKeyRecall` | `GET /js/swm/warningManagement/getPopupWarnings` |
| 语音模板管理 | `/swm/voiceTemplate/index` | `GET /js/swm/swmVoiceTemplate/listData`<br>`GET /js/swm/warningManagement/getPopupWarnings` |
| 应急呼叫 | `/swm/sos/SosList` | `GET /js/swm/swmPerson/listData`<br>`GET /js/swm/warningManagement/getPopupWarnings`<br>`GET /js/swm/warningManagement/sosListData` |
| 隐患列表 | `/swm/hiddenDanger/HiddenDangerList` | `GET /js/swm/warningManagement/getPopupWarnings`<br>`POST /js/swm/hiddenDanger/listData` |
| 隐患处置列表 | `/swm/dangerDisposal/DangerDisposalList` | `GET /js/swm/dangerDisposal/list`<br>`GET /js/swm/warningManagement/getPopupWarnings` |
| 巡检计划 | `/swm/inspectionPlan/index` | `GET /js/swm/swmInspectionPlan/listData`<br>`GET /js/swm/warningManagement/getPopupWarnings` |
| 巡检任务 | `/swm/inspectionList/index` | `GET /js/swm/swmInspectionList/listData`<br>`GET /js/swm/warningManagement/getPopupWarnings` |
| 安全教育 | `/swm/safetyEducation/list` | `GET /js/swm/organizationTree/getNodes`<br>`GET /js/swm/safetyEducation/listData`<br>`GET /js/swm/warningManagement/getPopupWarnings` |
| 安全教育视频 | `/swm/safetyVideos/list` | `GET /js/swm/warningManagement/getPopupWarnings`<br>`POST /js/swm/swmSafetyFileManage/pageList` |
| 视频培训记录 | `/swm/swmSafetyPersonTraining/list` | `GET /js/swm/organizationTree/getNodes`<br>`GET /js/swm/warningManagement/getPopupWarnings`<br>`POST /js/swm/swmSafetyPersonTraining/pageList` |
| 人员登记 | `/swm/personnel/PersonList` | `GET /js/swm/organizationTree/getNodes`<br>`GET /js/swm/swmPerson/listData`<br>`GET /js/swm/warningManagement/getPopupWarnings`<br>`POST /js/swm/swmDictData/listData` |
| 人员看板 | `/swm/personnelBoard/PersonnelBoardList` | `GET /js/swm/organizationTree/getNodes`<br>`GET /js/swm/personnelBoard/listData`<br>`GET /js/swm/swmSiteMapManagement/getEnabledMap`<br>`GET /js/swm/warningManagement/getPopupWarnings` |
| 排班管理 | `/swm/Attendance/AttendanceList` | `GET /js/swm/organizationTree/getNodes`<br>`GET /js/swm/personSchedule/listData`<br>`GET /js/swm/warningManagement/getPopupWarnings` |
| 日考勤记录 | `/swm/dailyAttendance/index` | `GET /js/swm/organizationTree/getNodes`<br>`GET /js/swm/swmDailyAttendance/listData`<br>`GET /js/swm/warningManagement/getPopupWarnings` |
| 周考勤记录 | `/swm/weekly/index` | `GET /js/swm/swmDailyAttendance/weeklyListData`<br>`GET /js/swm/warningManagement/getPopupWarnings`<br>`POST /js/swm/swmDictData/listData` |
| 月考勤记录 | `/swm/monthlyAttendance/index` | `GET /js/swm/swmDailyAttendance/monthlyListData`<br>`GET /js/swm/warningManagement/getPopupWarnings` |
| 行动轨迹 | `/swm/persontrack/personTrack` | `GET /js/swm/organizationTree/getNodes`<br>`GET /js/swm/personTrack/getPersonPositions`<br>`GET /js/swm/swmHelmetSubitem/findByParentId`<br>`GET /js/swm/swmSiteMapManagement/getEnabledMap`<br>`GET /js/swm/warningManagement/getPopupWarnings` |
| AI日报 | `/swm/swmDify/list` | `GET /js/swm/warningManagement/getPopupWarnings`<br>`POST /js/swm/swmDify/pageList` |
| 安全帽管理 | `/swm/helmetdevice/list` | `GET /js/swm/swmHelmetDevice/list`<br>`GET /js/swm/warningManagement/getPopupWarnings` |
| 信标管理 | `/swm/beacon/BeaconList` | `GET /js/swm/swmArea/getAreaOptions`<br>`GET /js/swm/warningManagement/getPopupWarnings`<br>`POST /js/swm/swmBeaconStation/listData` |
| 信标标注 | `/swm/beaconMap/beaconBase` | `GET /js/swm/swmArea/listAll`<br>`GET /js/swm/swmBeaconStation/listAll`<br>`GET /js/swm/swmSiteMapManagement/getEnabledMap`<br>`GET /js/swm/warningManagement/getPopupWarnings` |
| 监控设备管理 | `/swm/monitoringEquipment/monitoringEquipmentManagement` | `GET /js/swm/warningManagement/getPopupWarnings`<br>`POST /js/swm/api/public/swm/monitorDeviceInfo/listData` |
| 报警灯管理 | `/swm/alarmLight/index` | `GET /js/swm/alarmConfig/list`<br>`GET /js/swm/swmAlarmLight/listData`<br>`GET /js/swm/warningManagement/getPopupWarnings` |
| 颜色配置 | `/swm/colorConfig/index` | `GET /js/swm/swmHelmetSubitem/findByParentId`<br>`GET /js/swm/warningManagement/getPopupWarnings` |
| 场地底图管理 | `/swm/siteMapManagement/index` | `GET /js/swm/common/options/companies`<br>`GET /js/swm/swmSiteMapManagement/listData`<br>`GET /js/swm/warningManagement/getPopupWarnings` |
| 预警报警配置 | `/swm/alarmConfig/AlarmConfigList` | `GET /js/swm/alarmConfig/list`<br>`GET /js/swm/warningManagement/getPopupWarnings` |
| 视频管理 | `/swm/videoManagement/index` | - |
| 设备上传日志 | `/swm/rawMessageLog/index` | Runtime capture unavailable; compare checked-in old source |
| 轨迹日志 | `/swm/externalCoordinateData/index` | Runtime capture unavailable; compare checked-in old source |
| 区域日志 | `/swm/areaFenceData/index` | Runtime capture unavailable; compare checked-in old source |
| 设备下发日志 | `/swm/tcpDeviceCommandLog/index` | Runtime capture unavailable; compare checked-in old source |
| 发送算法日志 | `/swm/helmetLocation/index` | Runtime capture unavailable; compare checked-in old source |
| 车间档案 | `/swm/swmWorkshop/list` | Runtime capture unavailable; compare checked-in old source |
| 产线档案 | `/swm/swmProdLine/list` | Runtime capture unavailable; compare checked-in old source |
| 班组档案 | `/swm/swmWorkGroup/list` | Runtime capture unavailable; compare checked-in old source |
| 字典数据 | `/swm/dictData/list` | Runtime capture unavailable; compare checked-in old source |

## Confirmed Migration Gaps

- `SwmLegacyCompatController` still contains empty-success implementations for dashboard statistics and person trajectory queries.
- FMS archive compatibility pagination and save endpoints are placeholders.
- Attendance summary list/export and schedule import/export are placeholders.
- Alarm-light configuration, helmet configuration, beacon color configuration and file preview contain placeholder branches.
- Safety-education auxiliary operations and TDengine log list/export endpoints are placeholders.
- SOS broadcast/export currently return success without executing the original device-side behavior.
- AI daily report and safety-person training list/detail queries were converted from placeholders to real tenant-aware database queries in this audit.
