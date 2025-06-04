-- 添加场地底图管理表新字段
ALTER TABLE `swm_site_map_management` 
ADD COLUMN `drawing_pixel_x` int DEFAULT NULL COMMENT '图纸X像素坐标' AFTER `file_path`,
ADD COLUMN `drawing_pixel_y` int DEFAULT NULL COMMENT '图纸Y像素坐标' AFTER `drawing_pixel_x`,
ADD COLUMN `site_coordinate_x_m` decimal(10,2) DEFAULT NULL COMMENT '场地X坐标（米）' AFTER `drawing_pixel_y`,
ADD COLUMN `site_coordinate_y_m` decimal(10,2) DEFAULT NULL COMMENT '场地Y坐标（米）' AFTER `site_coordinate_x_m`;

-- 从现有数据中解析像素值填充到新字段
UPDATE `swm_site_map_management` SET 
  `drawing_pixel_x` = SUBSTRING_INDEX(SUBSTRING_INDEX(`map_size`, '×', 1), ' ', -1),
  `drawing_pixel_y` = SUBSTRING_INDEX(SUBSTRING_INDEX(`map_size`, '像素', 1), '×', -1)
WHERE `map_size` REGEXP '[0-9]+×[0-9]+';

-- 更新备注
COMMIT; 