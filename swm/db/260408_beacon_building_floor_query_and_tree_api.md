# 信标管理 - 建筑楼层查询与地图树下拉补充说明

**日期**：2026-04-08

---

## 概述

这份文档是对 `260407_beacon_building_floor_fields.md` 的补充，不替代原文档。

主要补两件事：

1. 信标列表里建筑、楼层字段现在怎么查
2. 新增的地图树下拉接口怎么给前端用

---

## 信标详情返回补充

**接口**：`GET /swm/swmBeaconStation/form?id={id}`（原有接口，无变化）

详情接口同样返回以下字段：

```json
{
  "floorId": "2000460065136517123",
  "floor": "2层",
  "buildingId": "2000460065136517001",
  "building": "1号楼"
}
```

---

## BeaconList 查询用法

这部分是给前端列表页同事看的，直接按下面规则接就行：

**接口**：`POST /swm/swmBeaconStation/listData`

- **建筑**：传 `building`，按建筑名称查
- **楼层**：传 `floorId`，按楼层 ID 查

不要这样理解：

- 不是按 `floor` 楼层名称查
- 也不是拿 `building` 预留字段做本表精确匹配

后端现在的实际规则如下：

| 查询项 | 前端传参 | 查询方式 | 后端处理方式 |
|---|---|---|---|
| 所属建筑 | `building` | 模糊查询 | 关联 `swm_site_map_management.map_name` 按建筑名称模糊匹配 |
| 所属楼层 | `floorId` | 精确查询 | 直接按 `swm_beacon_station.floor_id` 精确匹配 |

### 1. 建筑名称查询

**请求示例**

```json
{
  "pageNo": 1,
  "pageSize": 20,
  "building": "1号楼"
}
```

**说明**

- 前端输入 `1号楼`
- 后端会去底图表里查建筑名称包含 `1号楼` 的节点
- 比如 `1号楼`、`1号楼东侧` 都可能命中

### 2. 楼层 ID 查询

**请求示例**

```json
{
  "pageNo": 1,
  "pageSize": 20,
  "floorId": "2000460065136517123"
}
```

**说明**

- 前端传的是楼层节点 ID，不是楼层名称
- 后端实际按 `floor_id = 2000460065136517123` 去查
- 只有绑定这个楼层 ID 的信标会返回

### 3. 建筑 + 楼层组合查询

**请求示例**

```json
{
  "pageNo": 1,
  "pageSize": 20,
  "building": "1号楼",
  "floorId": "2000460065136517123"
}
```

**说明**

- 先按建筑名称过滤
- 再按楼层 ID 过滤
- 比如 `1号楼` 下面有 `1层、2层、3层`，传了 `2层` 对应的 `floorId` 后，只会留下这一层的数据

### 4. 列表展示怎么用

列表返回后，前端直接展示返回值里的：

- `building`：建筑名称
- `floor`：楼层名称

也就是说：

- 查询时，建筑用 `building`，楼层用 `floorId`
- 展示时，用返回值里的 `building`、`floor`

### 5. 不推荐的传法

**错误示例 1：拿楼层名称查列表**

```json
{
  "floor": "2层"
}
```

说明：后端列表查询**不按 `floor` 名称过滤**，这样传拿不到预期结果。

**错误示例 2：把建筑 ID 塞到 `building` 里**

```json
{
  "building": "2000460065136517001"
}
```

说明：`building` 是建筑名称，不是建筑 ID。

---

## 地图/建筑/楼层下拉接口

为了和“所属区域”下拉保持一致，后端现在新增了一个专门的地图树选项接口，前端可以一次性拿到厂区、建筑和对应楼层，不需要再自己连发多次请求去拼装。

**接口**：`GET /swm/swmSiteMapManagement/getBuildingFloorOptions`

**请求参数**：无

**返回说明**

- 不专门按 `status = '0'` 过滤，和所属区域下拉接口保持一致
- `options` 是厂区（地图）列表
- `options[].child` 是当前厂区下面的建筑列表
- `options[].child[].child` 是当前建筑下面的楼层列表
- 如果系统里有多个厂区，会把多个厂区下面的地图树都返回出来

**返回示例**

```json
{
  "success": true,
  "options": [
    {
      "id": "factory001",
      "value": "factory001",
      "label": "华南厂区",
      "mapName": "华南厂区",
      "mapType": "factory",
      "parentId": "0",
      "hasChildren": true,
      "child": [
        {
          "id": "building001",
          "value": "building001",
          "label": "1号楼",
          "mapName": "1号楼",
          "mapType": "building",
          "factoryId": "factory001",
          "factoryName": "华南厂区",
          "parentId": "factory001",
          "hasChildren": true,
          "child": [
            {
              "id": "floor001",
              "value": "floor001",
              "label": "1层",
              "mapName": "1层",
              "mapType": "floor",
              "factoryId": "factory001",
              "factoryName": "华南厂区",
              "buildingId": "building001",
              "buildingName": "1号楼",
              "parentId": "building001",
              "hasChildren": false
            },
            {
              "id": "floor002",
              "value": "floor002",
              "label": "2层",
              "mapName": "2层",
              "mapType": "floor",
              "factoryId": "factory001",
              "factoryName": "华南厂区",
              "buildingId": "building001",
              "buildingName": "1号楼",
              "parentId": "building001",
              "hasChildren": false
            }
          ]
        }
      ]
    }
  ],
  "total": 1,
  "message": "获取建筑楼层选项成功"
}
```

**字段怎么用**

| 字段 | 用途 |
|---|---|
| `options[].value` | 厂区节点值，前端可直接作为一级地图节点 |
| `options[].label` | 厂区节点显示名称 |
| `options[].child[].value` | 建筑下拉的值，前端可直接绑定到 `buildingId` |
| `options[].child[].label` | 建筑下拉显示名称 |
| `options[].child[].child[].value` | 楼层下拉的值，前端可直接绑定到 `floorId` |
| `options[].child[].child[].label` | 楼层下拉显示名称 |

**空结果场景**

下面两种情况都会正常返回空数组，不会报错：

- 系统中没有厂区节点
- 所有厂区下面都还没有建筑

空结果示例：

```json
{
  "success": true,
  "options": [],
  "total": 0,
  "message": "获取建筑楼层选项成功"
}
```

---

## 地图树数据来源

前端选择器现在建议直接请求：

- `GET /swm/swmSiteMapManagement/getBuildingFloorOptions`

也就是说：

- 先从 `options` 渲染厂区/地图下拉
- 用户选中厂区后，再用该厂区节点的 `child` 渲染建筑下拉
- 用户选中建筑后，再用该建筑节点的 `child` 渲染楼层下拉

选中后字段映射如下：

| 选择项 | 提交字段 | 取值来源 |
|---|---|---|
| 厂区/地图 | 前端自用 | 厂区节点 `value` / `label` |
| 建筑 | `buildingId` | 建筑节点 `value` |
| 建筑名称 | `building` | 建筑节点 `label` |
| 楼层 | `floorId` | 楼层节点 `value` |
| 楼层名称 | `floor` | 楼层节点 `label` |

如果前端还沿用旧做法，自己按 `listData` 分开查建筑和楼层，也不是不能用；但现在推荐统一切到 `getBuildingFloorOptions`，这样接口更稳定，前端代码也更简单。
