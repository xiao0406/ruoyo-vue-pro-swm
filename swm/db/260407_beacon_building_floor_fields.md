# 信标管理 - 新增建筑/楼层字段接口说明

**日期**：2026-04-07

---

## 概述

信标管理（`/swm/beacon/BeaconList`）的新增/编辑接口新增了以下 4 个字段，支持为信标绑定所属建筑和楼层信息。建筑和楼层数据来源于场地底图管理树（`/swm/swmSiteMapManagement`）。

---

## 新增字段说明

| 前端字段名 | 类型 | 是否必填 | 说明 |
|---|---|---|---|
| `floorId` | String | 否 | 楼层ID，取自场地底图管理中 `mapType = 'floor'` 的节点 `id` |
| `floor` | String | 否 | 楼层名称，取自对应楼层节点的 `mapName` |
| `buildingId` | String | 否 | 建筑ID，取自场地底图管理中 `mapType = 'building'` 的节点 `id` |
| `building` | String | 否 | 建筑名称，取自对应建筑节点的 `mapName` |

> 4 个字段均可为空，不填时保持为 `null`。

---

## 涉及接口

### 新增/编辑信标

**接口**：`POST /swm/swmBeaconStation/save`（原有接口，无变化）

在原有请求体基础上，新增 4 个字段一起提交即可：

```json
{
  "beaconId": "AA:BB:CC:DD:EE:FF",
  "beaconType": "1",
  "buildingId": "建筑节点的id",
  "building": "1号楼",
  "floorId": "楼层节点的id",
  "floor": "2层",
  ...其他原有字段
}
```

### 列表/详情返回字段

**接口**：`POST /swm/swmBeaconStation/listData`（原有接口，无变化）

响应中每条记录新增以下字段：

```json
{
  "floorId": "xxx",
  "floor": "2层",
  "buildingId": "yyy",
  "building": "1号楼",
  ...其他原有字段
}
```

---

## 建筑/楼层数据来源

前端选择器的数据请求场地底图管理接口：

- **建筑列表**：`GET /swm/swmSiteMapManagement/listData?mapType=building`
- **楼层列表**（依赖建筑）：`GET /swm/swmSiteMapManagement/listData?parentId={buildingId}&mapType=floor`

选中后，将节点的 `id` 作为 `buildingId` / `floorId`，将节点的 `mapName` 作为 `building` / `floor` 一起提交。

---

## 批量管理

批量管理复用现有逐条调用 `/save` 的方式，在每条请求体中带上 `buildingId`、`building`、`floorId`、`floor` 即可完成批量更新，后端无需额外接口。
