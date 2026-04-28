package com.jeesite.modules.swm.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.sys.utils.CorpUtils;
import com.jeesite.modules.swm.dao.SwmSiteMapManagementDao;
import com.jeesite.modules.swm.entity.SwmSiteMapManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 场地底图管理表Service
 * 
 * @author zwf
 * @version 2025-05-30
 */
@Service
@Transactional(readOnly = true)
public class SwmSiteMapManagementService extends CrudService<SwmSiteMapManagementDao, SwmSiteMapManagement> {

    private static final Logger logger = LoggerFactory.getLogger(SwmSiteMapManagementService.class);
    
    /**
     * 获取单条数据
     * @param swmSiteMapManagement 查询条件
     * @return 场地底图管理信息
     */
    @Override
    public SwmSiteMapManagement get(SwmSiteMapManagement swmSiteMapManagement) {
        return super.get(swmSiteMapManagement);
    }
    
    /**
     * 查询分页数据
     * @param swmSiteMapManagement 查询条件
     * @return 分页数据
     */
    public Page<SwmSiteMapManagement> findPage(SwmSiteMapManagement swmSiteMapManagement) {
        return super.findPage(swmSiteMapManagement);
    }
    
    /**
     * 查询列表数据
     * @param swmSiteMapManagement 查询条件
     * @return 列表数据
     */
    @Override
    public List<SwmSiteMapManagement> findList(SwmSiteMapManagement swmSiteMapManagement) {
        return super.findList(swmSiteMapManagement);
    }
    
    /**
     * 查询启用状态的地图（多租户隔离）
     * @return 启用状态的地图
     * @author Shawn @date 2026-04-08 修复多租户隔离
     */
    public SwmSiteMapManagement findActiveMap() {
        String corpCode = CorpUtils.getCurrentCorpCode();
        return dao.findActiveMap(corpCode);
    }

    /**
     * 获取完整地图树选项。
     * 现在接口返回“厂区 -> 建筑 -> 楼层”完整结构，前端拿一次就能把所有地图节点用起来。
     * 这里不额外按 status=0 过滤，和所属区域下拉保持一致。
     *
     * @return 地图树选项列表
     * @author Shawn @date 2026-04-07
     */
    public List<Map<String, Object>> getBuildingFloorOptions() {
        List<Map<String, Object>> options = new ArrayList<>();

        // 和所属区域下拉一样，这里不额外加 status=0，直接返回所有厂区下可选建筑/楼层。
        List<SwmSiteMapManagement> factories = findFactoryMaps();
        if (factories == null || factories.isEmpty()) {
            logger.info("获取建筑楼层选项时未找到厂区节点");
            return options;
        }

        // 逐个厂区展开完整树，顶层就是地图节点，下面再挂建筑和楼层。
        for (SwmSiteMapManagement factory : factories) {
            options.add(buildFactoryOption(factory));
        }

        logger.info("获取完整地图树选项成功, factoryCount={}", options.size());
        return options;
    }

    /**
     * 查询指定父节点下的直接子节点列表（多租户隔离）
     * 同时为每个节点设置 hasChildren 标记，告诉前端是否可以继续展开
     * @param parentId 父节点ID，首次进页面传 "0" 查所有厂区
     * @return 子节点列表
     * @author Shawn @date 2026-04-02
     * @author Shawn @date 2026-04-08 修复多租户隔离
     */
    public List<SwmSiteMapManagement> findChildren(String parentId) {
        String corpCode = CorpUtils.getCurrentCorpCode();
        // 查出直接子节点
        List<SwmSiteMapManagement> children = dao.findChildren(parentId, corpCode);

        // 填充每个子节点的 hasChildren 标记
        fillHasChildren(children, corpCode);

        logger.info("查询父节点[{}]下的子节点，共{}条, corpCode={}", parentId, children.size(), corpCode);
        return children;
    }

    /**
     * 批量填充 hasChildren 标记（多租户隔离）。
     * 遍历列表，对每条记录查一次子节点数量并设置标记，
     * listData 分页接口和 children 懒加载接口都复用这里。
     *
     * @param list 需要填充的节点列表
     * @param corpCode 租户编码
     * @author Shawn @date 2026-04-07
     * @author Shawn @date 2026-04-08 修复多租户隔离
     */
    public void fillHasChildren(List<SwmSiteMapManagement> list, String corpCode) {
        if (list == null || list.isEmpty()) {
            return;
        }
        // 逐个查子节点数量，count>0 则代表还有下级
        for (SwmSiteMapManagement node : list) {
            int count = dao.countChildren(node.getId(), corpCode);
            node.setHasChildren(count > 0);
        }
    }

    /**
     * 查询所有厂区节点。
     */
    private List<SwmSiteMapManagement> findFactoryMaps() {
        // 和所属区域下拉一样，关闭自动状态过滤，避免因为状态值把下拉选项过滤掉。
        SwmSiteMapManagement query = new SwmSiteMapManagement();
        query.setStatus(null);
        query.getSqlMap().getWhere().disableAutoAddStatusWhere();
        query.setMapType("factory");
        return findList(query);
    }

    /**
     * 组装厂区节点。
     */
    private Map<String, Object> buildFactoryOption(SwmSiteMapManagement factory) {
        // 顶层节点就是地图本身，前端可以直接拿来渲染一级下拉或树。
        Map<String, Object> factoryOption = buildMapOption(factory);
        factoryOption.put("child", buildBuildingOptions(factory));
        return factoryOption;
    }

    /**
     * 组装某个厂区下的建筑列表。
     */
    private List<Map<String, Object>> buildBuildingOptions(SwmSiteMapManagement factory) {
        List<Map<String, Object>> buildingOptions = new ArrayList<>();
        List<SwmSiteMapManagement> buildings = findChildren(factory.getId());
        if (buildings == null || buildings.isEmpty()) {
            return buildingOptions;
        }

        // 这里只保留建筑节点，防止历史脏数据把别的类型混进来。
        for (SwmSiteMapManagement building : buildings) {
            if (!"building".equals(building.getMapType())) {
                continue;
            }

            Map<String, Object> buildingOption = buildMapOption(building);
            buildingOption.put("factoryId", factory.getId());
            buildingOption.put("factoryName", factory.getMapName());
            buildingOption.put("child", buildFloorOptions(factory, building));
            buildingOptions.add(buildingOption);
        }
        return buildingOptions;
    }

    /**
     * 组装某栋建筑下的楼层列表。
     */
    private List<Map<String, Object>> buildFloorOptions(SwmSiteMapManagement factory,
            SwmSiteMapManagement building) {
        List<Map<String, Object>> floorOptions = new ArrayList<>();
        List<SwmSiteMapManagement> floors = findChildren(building.getId());
        if (floors == null || floors.isEmpty()) {
            return floorOptions;
        }

        // 楼层只允许挂在建筑下面，这里直接按直属子节点筛出 floor。
        for (SwmSiteMapManagement floor : floors) {
            if (!"floor".equals(floor.getMapType())) {
                continue;
            }

            Map<String, Object> floorOption = buildMapOption(floor);
            floorOption.put("factoryId", factory.getId());
            floorOption.put("factoryName", factory.getMapName());
            floorOption.put("buildingId", building.getId());
            floorOption.put("buildingName", building.getMapName());
            floorOptions.add(floorOption);
        }
        return floorOptions;
    }

    /**
     * 组装前端下拉选项。
     */
    private Map<String, Object> buildMapOption(SwmSiteMapManagement map) {
        // 统一补 value/label，前端 select 直接可用，少做一次字段转换。
        Map<String, Object> option = new HashMap<>();
        option.put("id", map.getId());
        option.put("value", map.getId());
        option.put("label", map.getMapName());
        option.put("mapName", map.getMapName());
        option.put("mapType", map.getMapType());
        option.put("parentId", map.getParentId());
        option.put("hasChildren", map.getHasChildren());
        return option;
    }
    
    /**
     * 保存数据（插入或更新）
     * @param swmSiteMapManagement 实体对象
     */
    @Override
    @Transactional(readOnly = false)
    public void save(SwmSiteMapManagement swmSiteMapManagement) {
        try {
            // 先把前端传来的空串、空格统一清洗掉，避免出现“看起来有值，其实是空”的情况。
            normalizeParentInfo(swmSiteMapManagement);

            // 新增时补默认状态，保持原来的业务行为不变。
            if (swmSiteMapManagement.getIsNewRecord()) {
                swmSiteMapManagement.setStatus("1"); // 1表示禁用
            }

            // 兜底完成后再走框架默认保存，避免空 parentId 落库。
            super.save(swmSiteMapManagement);
            logger.info("保存底图成功, id={}, mapType={}, parentId={}",
                    swmSiteMapManagement.getId(),
                    swmSiteMapManagement.getMapType(),
                    swmSiteMapManagement.getParentId());
        } catch (RuntimeException e) {
            // 这里统一打印关键信息，方便后面直接从日志定位是哪种节点保存失败。
            logger.error("保存底图失败, id={}, mapType={}, parentId={}",
                    swmSiteMapManagement.getId(),
                    swmSiteMapManagement.getMapType(),
                    swmSiteMapManagement.getParentId(),
                    e);
            throw e;
        }
    }

    /**
     * 统一兜底父节点信息。
     */
    private void normalizeParentInfo(SwmSiteMapManagement swmSiteMapManagement) {
        // 先把字符串两端空格去掉，空串直接按未传处理。
        String mapType = normalizeText(swmSiteMapManagement.getMapType());
        String parentId = normalizeText(swmSiteMapManagement.getParentId());
        swmSiteMapManagement.setMapType(mapType);
        swmSiteMapManagement.setParentId(parentId);

        // 厂区节点固定就是顶层，前端没传或传错时统一改成 0。
        if (isFactoryNode(mapType)) {
            swmSiteMapManagement.setParentId("0");
            return;
        }

        // 建筑、楼层这两类必须挂在父节点下面，parentId 为空直接拦住。
        if (requiresParentNode(mapType) && isBlank(parentId)) {
            throw new IllegalArgumentException(getNodeTypeText(mapType) + "节点父节点不能为空");
        }

        validateParentNode(mapType, parentId);
    }

    /**
     * 校验父节点是否存在，以及父子节点类型是否匹配。
     */
    private void validateParentNode(String mapType, String parentId) {
        // 非顶级节点必须挂真实父节点，parentId=0 对 building/floor 来说都是非法数据。
        if ("0".equals(parentId)) {
            throw new IllegalArgumentException(getNodeTypeText(mapType) + "节点父节点类型不正确");
        }

        SwmSiteMapManagement parentNode = get(parentId);
        if (parentNode == null) {
            logger.warn("保存底图时父节点不存在, mapType={}, parentId={}", mapType, parentId);
            throw new IllegalArgumentException(getNodeTypeText(mapType) + "节点父节点不存在，请刷新后重试");
        }

        // building 只能挂 factory，floor 只能挂 building，避免树结构被保存歪了。
        String expectedParentType = getRequiredParentType(mapType);
        if (expectedParentType == null) {
            return;
        }
        if (expectedParentType.equals(parentNode.getMapType())) {
            return;
        }

        logger.warn("保存底图时父节点类型不匹配, mapType={}, parentId={}, parentType={}",
                mapType, parentId, parentNode.getMapType());
        throw new IllegalArgumentException(
                getNodeTypeText(mapType) + "节点父节点必须是" + getNodeTypeText(expectedParentType));
    }

    /**
     * 规范化字符串，去掉首尾空格。
     */
    private String normalizeText(String value) {
        // null、空串、全空格都统一转成 null，后面的判断会简单很多。
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();
        if (trimmedValue.isEmpty()) {
            return null;
        }
        return trimmedValue;
    }

    /**
     * 是否为厂区节点。
     */
    private boolean isFactoryNode(String mapType) {
        // 厂区节点始终视为根节点，parentId 一律用 0。
        return "factory".equals(mapType);
    }

    /**
     * 是否必须存在父节点。
     */
    private boolean requiresParentNode(String mapType) {
        // 目前只有建筑和楼层属于子节点，保存时必须带上父节点 ID。
        return "building".equals(mapType) || "floor".equals(mapType);
    }

    /**
     * 获取当前节点要求的父节点类型。
     */
    private String getRequiredParentType(String mapType) {
        // 树结构固定为 factory -> building -> floor，后端这里直接守住。
        if ("building".equals(mapType)) {
            return "factory";
        }
        if ("floor".equals(mapType)) {
            return "building";
        }
        return null;
    }

    /**
     * 判断字符串是否为空。
     */
    private boolean isBlank(String value) {
        // 这里复用统一规则，避免一处 trim、一处不 trim 导致判断不一致。
        return normalizeText(value) == null;
    }

    /**
     * 获取节点类型中文名。
     */
    private String getNodeTypeText(String mapType) {
        // 日志和报错都用中文，前端和排查时更直观。
        if ("building".equals(mapType)) {
            return "建筑";
        }
        if ("floor".equals(mapType)) {
            return "楼层";
        }
        return "底图";
    }
    
    /**
     * 删除数据
     * 删除前校验：如果有子节点，抛异常拒绝删除
     * @param swmSiteMapManagement 实体对象
     * @author Shawn @date 2026-04-02 增加子节点校验
     * @author Shawn @date 2026-04-08 修复多租户隔离
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(SwmSiteMapManagement swmSiteMapManagement) {
        // 校验是否有子节点，有的话不允许删除
        String corpCode = CorpUtils.getCurrentCorpCode();
        int childCount = dao.countChildren(swmSiteMapManagement.getId(), corpCode);
        if (childCount > 0) {
            throw new RuntimeException("该节点下还有" + childCount + "个子节点，请先删除子节点");
        }

        // 执行物理删除
        dao.physicalDelete(swmSiteMapManagement);
        logger.info("物理删除底图节点, id={}", swmSiteMapManagement.getId());
    }
    
    /**
     * 启用/禁用底图
     * @param id 底图ID
     * @param status 状态（0-启用，1-禁用）
     */
    @Transactional(readOnly = false)
    public void changeStatus(String id, String status) {
        SwmSiteMapManagement map = get(id);
        if (map == null) {
            return;
        }
        
        // 如果是启用操作
        if ("0".equals(status)) {
            // 禁用所有其他底图（不论项目）
            disableAllOtherMaps(id);
        }
        
        // 更新当前底图状态
        map.setStatus(status);
        // 设置更新时间和更新人
        map.preUpdate();
        
        // 直接通过DAO更新状态，确保状态字段被更新
        dao.updateMapStatus(map);
    }
    
    /**
     * 根据多个 ID 批量查询底图记录，不加 status 条件
     * @param ids ID 列表
     * @return 底图记录列表
     * @author Shawn @date 2026-04-10
     */
    public List<SwmSiteMapManagement> findByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        return dao.findByIds(ids);
    }

    /**
     * 禁用所有其他底图（不论项目）
     * @param currentMapId 当前底图ID（不会被禁用）
     */
    @Transactional(readOnly = false)
    private void disableAllOtherMaps(String currentMapId) {
        // 查询所有底图
        SwmSiteMapManagement query = new SwmSiteMapManagement();
        List<SwmSiteMapManagement> maps = findList(query);
        
        // 禁用除当前底图外的所有底图
        for (SwmSiteMapManagement map : maps) {
            if (!map.getId().equals(currentMapId)) {
                map.setStatus("1"); // 设置为禁用
                map.preUpdate(); // 设置更新时间和更新人
                dao.updateMapStatus(map);
            }
        }
    }

    public List<SwmSiteMapManagement> findListByNames(List<String> strings) {
        if (strings == null || strings.isEmpty()) {
            return null;
        }
        return dao.findListByNames(strings);
    }
}
