-- 场地底图管理表：新增树形结构相关字段
-- @author Shawn @date 2026-04-02

-- 1. 新增字段
ALTER TABLE `swm_site_map_management` 
  ADD COLUMN `parent_id` varchar(64) DEFAULT '0' COMMENT '父节点ID，顶层填0',
  ADD COLUMN `map_type` varchar(20) DEFAULT NULL COMMENT '图纸类型：factory=厂区，building=建筑，floor=楼层',
  ADD COLUMN `origin_pixel_x` int(11) DEFAULT NULL COMMENT '图纸起始X像素偏移量',
  ADD COLUMN `origin_pixel_y` int(11) DEFAULT NULL COMMENT '图纸起始Y像素偏移量',
  ADD COLUMN `sort_order` int(11) DEFAULT 0 COMMENT '排序值，越小越靠前';

-- 2. 新增索引
ALTER TABLE `swm_site_map_management` ADD INDEX `idx_parent_id` (`parent_id`);
ALTER TABLE `swm_site_map_management` ADD INDEX `idx_map_type` (`map_type`);

-- ============================================================
-- 信标基站表：新增楼层/建筑关联字段
-- @author Shawn @date 2026-04-07
-- 说明：方案A（前端上传冗余存储），全部字段可为空。
--       floor 字段已存在，本次仅更新注释语义为"楼层名称"。
-- ============================================================

-- 3. swm_beacon_station 新增 3 个关联字段
ALTER TABLE `swm_beacon_station`
  ADD COLUMN `floor_id`    varchar(64)  DEFAULT NULL COMMENT '楼层ID（关联 swm_site_map_management floor节点）'    AFTER `floor`,
  ADD COLUMN `building_id` varchar(64)  DEFAULT NULL COMMENT '建筑ID（关联 swm_site_map_management building节点）' AFTER `floor_id`,
  ADD COLUMN `building`    varchar(100) DEFAULT NULL COMMENT '建筑名称（冗余存储）'                                 AFTER `building_id`;

-- 4. 原 floor 字段注释更新为"楼层名称"
ALTER TABLE `swm_beacon_station`
  MODIFY COLUMN `floor` varchar(20) DEFAULT NULL COMMENT '楼层名称';
