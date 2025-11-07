
-- 2025/11/03 导入人员 ===================================================

-- 二车间桥梁线
INSERT INTO `swm`.`fms_prod_line` (`id`, `prod_line_name`, `prod_line_code`, `head_id`, `head_name`, `work_shop_id`, `work_shop_name`, `create_by`, `create_date`, `update_by`, `update_date`, `remarks`, `corp_code`, `corp_name`, `make_unit`) VALUES ('1762161057997701', '二车间桥梁线', 'GD_CJ2_QLX', '15328852055', NULL, '1745710598073368576', NULL, 'ZJGGGD_wangning_x3wg', NOW(), 'ZJGG_adminZJGG', NOW(), NULL, 'ZJGG', '中建钢构工程有限公司', '0001A110000000002ZWA');


-- 何正志/张彬班组
INSERT INTO `swm`.`fms_work_group` (`id`, `work_group_name`, `work_group_code`, `head_id`, `prod_line_id`, `work_shop_id`, `proc_id`, `create_by`, `create_date`, `update_by`, `update_date`, `remarks`, `corp_code`, `corp_name`, `make_unit`, `work_shop_name`, `office_work_code`) VALUES (REPLACE(UUID(), '-', ''), '何正志/张彬班组', 'HZW/ZB_BZ', '13794508580', '1736918823366025216', '1745710598073368576', NULL, 'ZJGG_adminZJGG', NOW(), 'ZJGG_adminZJGG', NOW(), NULL, 'ZJGG', '中建钢构股份有限公司', '0001A110000000002ZWA', NULL, '');
-- 杨龙坤班组
INSERT INTO `swm`.`fms_work_group` (`id`, `work_group_name`, `work_group_code`, `head_id`, `prod_line_id`, `work_shop_id`, `proc_id`, `create_by`, `create_date`, `update_by`, `update_date`, `remarks`, `corp_code`, `corp_name`, `make_unit`, `work_shop_name`, `office_work_code`) VALUES (REPLACE(UUID(), '-', ''), '杨龙坤班组', 'YLK_BZ', '19946712659', '1750786704094101504', '1745710598073368576', NULL, '15390176298', NOW(), '15390176298', NOW(), NULL, 'ZJGG', '中建钢构股份有限公司', '0001A110000000002ZWA', NULL, NULL);
-- 韦庆荣班组
INSERT INTO `swm`.`fms_work_group` (`id`, `work_group_name`, `work_group_code`, `head_id`, `prod_line_id`, `work_shop_id`, `proc_id`, `create_by`, `create_date`, `update_by`, `update_date`, `remarks`, `corp_code`, `corp_name`, `make_unit`, `work_shop_name`, `office_work_code`) VALUES (REPLACE(UUID(), '-', ''), '韦庆荣班组', 'WQR_BZ', '19946712659', '1750786704094101504', '1745710598073368576', NULL, '15390176298', NOW(), '15390176298', NOW(), NULL, 'ZJGG', '中建钢构股份有限公司', '0001A110000000002ZWA', NULL, NULL);
-- 蒲志金班组
INSERT INTO `swm`.`fms_work_group` (`id`, `work_group_name`, `work_group_code`, `head_id`, `prod_line_id`, `work_shop_id`, `proc_id`, `create_by`, `create_date`, `update_by`, `update_date`, `remarks`, `corp_code`, `corp_name`, `make_unit`, `work_shop_name`, `office_work_code`) VALUES (REPLACE(UUID(), '-', ''), '蒲志金班组', 'PZJ_BZ', '19946712659', '1750786704094101504', '1745710598073368576', NULL, '15390176298', NOW(), '15390176298', NOW(), NULL, 'ZJGG', '中建钢构股份有限公司', '0001A110000000002ZWA', NULL, NULL);
-- 马长命班组
INSERT INTO `swm`.`fms_work_group` (`id`, `work_group_name`, `work_group_code`, `head_id`, `prod_line_id`, `work_shop_id`, `proc_id`, `create_by`, `create_date`, `update_by`, `update_date`, `remarks`, `corp_code`, `corp_name`, `make_unit`, `work_shop_name`, `office_work_code`) VALUES (REPLACE(UUID(), '-', ''), '马长命班组', 'MCM_BZ', '19946712659', '1750786704094101504', '1745710598073368576', NULL, '15390176298', NOW(), '15390176298', NOW(), NULL, 'ZJGG', '中建钢构股份有限公司', '0001A110000000002ZWA', NULL, NULL);
-- 崔兰昆班组
INSERT INTO `swm`.`fms_work_group` (`id`, `work_group_name`, `work_group_code`, `head_id`, `prod_line_id`, `work_shop_id`, `proc_id`, `create_by`, `create_date`, `update_by`, `update_date`, `remarks`, `corp_code`, `corp_name`, `make_unit`, `work_shop_name`, `office_work_code`) VALUES (REPLACE(UUID(), '-', ''), '崔兰昆班组', 'CLK_BZ', '19946712659', '1750786704094101504', '1745710598073368576', NULL, '15390176298', NOW(), '15390176298', NOW(), NULL, 'ZJGG', '中建钢构股份有限公司', '0001A110000000002ZWA', NULL, NULL);
-- 原汝超班组
INSERT INTO `swm`.`fms_work_group` (`id`, `work_group_name`, `work_group_code`, `head_id`, `prod_line_id`, `work_shop_id`, `proc_id`, `create_by`, `create_date`, `update_by`, `update_date`, `remarks`, `corp_code`, `corp_name`, `make_unit`, `work_shop_name`, `office_work_code`) VALUES (REPLACE(UUID(), '-', ''), '原汝超班组', 'YRC_BZ', '19946712659', '1762161057997701', '1745710598073368576', NULL, '15390176298', NOW(), '15390176298', NOW(), NULL, 'ZJGG', '中建钢构股份有限公司', '0001A110000000002ZWA', NULL, NULL);
-- 王营辉班组
INSERT INTO `swm`.`fms_work_group` (`id`, `work_group_name`, `work_group_code`, `head_id`, `prod_line_id`, `work_shop_id`, `proc_id`, `create_by`, `create_date`, `update_by`, `update_date`, `remarks`, `corp_code`, `corp_name`, `make_unit`, `work_shop_name`, `office_work_code`) VALUES (REPLACE(UUID(), '-', ''), '王营辉班组', 'WYH_BZ', '19946712659', '1762161057997701', '1745710598073368576', NULL, '15390176298', NOW(), '15390176298', NOW(), NULL, 'ZJGG', '中建钢构股份有限公司', '0001A110000000002ZWA', NULL, NULL);
-- 刘新来班组
INSERT INTO `swm`.`fms_work_group` (`id`, `work_group_name`, `work_group_code`, `head_id`, `prod_line_id`, `work_shop_id`, `proc_id`, `create_by`, `create_date`, `update_by`, `update_date`, `remarks`, `corp_code`, `corp_name`, `make_unit`, `work_shop_name`, `office_work_code`) VALUES (REPLACE(UUID(), '-', ''), '刘新来班组', 'SGD_LXL-BZ', '19946712659', '1750786704094101504', '1745710598073368576', NULL, '15390176298', NOW(), '15390176298', NOW(), NULL, 'ZJGG', '中建钢构股份有限公司', '0001A110000000002ZWA', NULL, NULL);


-- 2025/11/07 导入人员 ===================================================

-- 产线 -----------------------
-- 整个车间维修
INSERT INTO `swm`.`fms_prod_line` (`id`, `prod_line_name`, `prod_line_code`, `head_id`, `head_name`, `work_shop_id`, `work_shop_name`, `create_by`, `create_date`, `update_by`, `update_date`, `remarks`, `corp_code`, `corp_name`, `make_unit`) VALUES ('c760273dbbab11f0b42f000c29a89b40', '整个车间维修', 'ZGCJWX', '15328852055', NULL, '1745710598073368576', NULL, 'ZJGGGD_wangning_x3wg', NOW(), 'ZJGG_adminZJGG', NOW(), NULL, 'ZJGG', '中建钢构工程有限公司', '0001A110000000002ZWA');
-- 卷管车间
INSERT INTO `swm`.`fms_prod_line` (`id`, `prod_line_name`, `prod_line_code`, `head_id`, `head_name`, `work_shop_id`, `work_shop_name`, `create_by`, `create_date`, `update_by`, `update_date`, `remarks`, `corp_code`, `corp_name`, `make_unit`) VALUES ('c760a8ddbbab11f0b42f000c29a89b40', '卷管车间', 'JGCJ', '15328852055', NULL, '1745710598073368576', NULL, 'ZJGGGD_wangning_x3wg', NOW(), 'ZJGG_adminZJGG', NOW(), NULL, 'ZJGG', '中建钢构工程有限公司', '0001A110000000002ZWA');
-- 二车间四工段
INSERT INTO `swm`.`fms_prod_line` (`id`, `prod_line_name`, `prod_line_code`, `head_id`, `head_name`, `work_shop_id`, `work_shop_name`, `create_by`, `create_date`, `update_by`, `update_date`, `remarks`, `corp_code`, `corp_name`, `make_unit`) VALUES ('c76187debbab11f0b42f000c29a89b40', '二车间四工段', 'CJ2_GD4', '15328852055', NULL, '1745710598073368576', NULL, 'ZJGGGD_wangning_x3wg', NOW(), 'ZJGG_adminZJGG', NOW(), NULL, 'ZJGG', '中建钢构工程有限公司', '0001A110000000002ZWA');


-- 班组 ===================================
-- 司羽  二车间二工段
INSERT INTO `swm`.`fms_work_group` (`id`, `work_group_name`, `work_group_code`, `head_id`, `prod_line_id`, `work_shop_id`, `proc_id`, `create_by`, `create_date`, `update_by`, `update_date`, `remarks`, `corp_code`, `corp_name`, `make_unit`, `work_shop_name`, `office_work_code`) VALUES (REPLACE(UUID(), '-', ''), '司羽', 'SY/ZB_BZ', '13794508580', '1736918823366025216', '1745710598073368576', NULL, 'ZJGG_adminZJGG', NOW(), 'ZJGG_adminZJGG', NOW(), NULL, 'ZJGG', '中建钢构股份有限公司', '0001A110000000002ZWA', NULL, '');
-- 王秀晓 整个车间维修
INSERT INTO `swm`.`fms_work_group` (`id`, `work_group_name`, `work_group_code`, `head_id`, `prod_line_id`, `work_shop_id`, `proc_id`, `create_by`, `create_date`, `update_by`, `update_date`, `remarks`, `corp_code`, `corp_name`, `make_unit`, `work_shop_name`, `office_work_code`) VALUES (REPLACE(UUID(), '-', ''), '王秀晓', 'WXX/ZB_BZ', '13794508580', 'c760273dbbab11f0b42f000c29a89b40', '1745710598073368576', NULL, 'ZJGG_adminZJGG', NOW(), 'ZJGG_adminZJGG', NOW(), NULL, 'ZJGG', '中建钢构股份有限公司', '0001A110000000002ZWA', NULL, '');
-- 万新春 卷管车间
INSERT INTO `swm`.`fms_work_group` (`id`, `work_group_name`, `work_group_code`, `head_id`, `prod_line_id`, `work_shop_id`, `proc_id`, `create_by`, `create_date`, `update_by`, `update_date`, `remarks`, `corp_code`, `corp_name`, `make_unit`, `work_shop_name`, `office_work_code`) VALUES (REPLACE(UUID(), '-', ''), '万新春', 'WXC/ZB_BZ', '13794508580', 'c760a8ddbbab11f0b42f000c29a89b40', '1745710598073368576', NULL, 'ZJGG_adminZJGG', NOW(), 'ZJGG_adminZJGG', NOW(), NULL, 'ZJGG', '中建钢构股份有限公司', '0001A110000000002ZWA', NULL, '');
-- 唐国君 二车间四工段
INSERT INTO `swm`.`fms_work_group` (`id`, `work_group_name`, `work_group_code`, `head_id`, `prod_line_id`, `work_shop_id`, `proc_id`, `create_by`, `create_date`, `update_by`, `update_date`, `remarks`, `corp_code`, `corp_name`, `make_unit`, `work_shop_name`, `office_work_code`) VALUES (REPLACE(UUID(), '-', ''), '唐国君', 'TGJ/ZB_BZ', '13794508580', 'c76187debbab11f0b42f000c29a89b40', '1745710598073368576', NULL, 'ZJGG_adminZJGG', NOW(), 'ZJGG_adminZJGG', NOW(), NULL, 'ZJGG', '中建钢构股份有限公司', '0001A110000000002ZWA', NULL, '');
# 余料班组
INSERT INTO `swm`.`fms_work_group` (`id`, `work_group_name`, `work_group_code`, `head_id`, `prod_line_id`, `work_shop_id`, `proc_id`, `create_by`, `create_date`, `update_by`, `update_date`, `remarks`, `corp_code`, `corp_name`, `make_unit`, `work_shop_name`, `office_work_code`) VALUES (REPLACE(UUID(), '-', ''), '余料班组', 'YLBZ_ZJ', '13719644368', '1775065603265200128', '1745709611073945600', NULL, 'ZJGG_adminZJGG', NOW(), 'ZJGG_adminZJGG', NOW(), NULL, 'ZJGG', '中建钢构股份有限公司', '0001A110000000002ZWA', NULL, NULL);


