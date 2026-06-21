package cn.iocoder.yudao.module.swm.job.task;

import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.swm.enums.SwmRedisKeyConstants;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmHelmetDeviceDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmHelmetDeviceMapper;
import cn.iocoder.yudao.module.swm.service.cache.DeviceCorpMappingCache;
import cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantDO;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.system.service.tenant.TenantService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 维护设备→租户映射（Redis + 本地缓存）
 */
@Slf4j
@Component
public class DeviceCorpTask {

    @Resource
    private SwmHelmetDeviceMapper swmHelmetDeviceMapper;
    @Resource
    private TenantService tenantService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private DeviceCorpMappingCache deviceCorpMappingCache;

    @XxlJob("deviceCorpMapping")
    @Transactional(rollbackFor = Exception.class)
    public void deviceCorpMapping() {
        List<TenantDO> tenantList = tenantService.getTenantListByStatus(CommonStatusEnum.ENABLE.getStatus());
        if (CollectionUtils.isEmpty(tenantList)) {
            XxlJobHelper.log("没有租户信息");
            return;
        }

        List<SwmHelmetDeviceDO> deviceListAll = new ArrayList<>();
        for (TenantDO tenant : tenantList) {
            Long tenantId = tenant.getId();
            try {
                TenantUtils.execute(tenantId, () -> {
                    List<SwmHelmetDeviceDO> deviceList = swmHelmetDeviceMapper.selectList(new LambdaQueryWrapper<>());
                    if (CollectionUtils.isEmpty(deviceList)) {
                        XxlJobHelper.log("租户 {} 没有设备", tenantId);
                        return;
                    }
                    deviceListAll.addAll(deviceList);
                });
            } catch (Exception e) {
                XxlJobHelper.log("租户 {} 获取设备列表异常: {}", tenantId, e.getMessage());
            }
        }

        if (CollectionUtils.isEmpty(deviceListAll)) {
            XxlJobHelper.log("所有租户均没有设备，设备租户映射关系不更新");
            return;
        }

        // 先写入临时 key，再原子替换
        String tempKey = SwmRedisKeyConstants.GlobalKey.DEVICE_TO_CORP + "_TMP";
        Map<String, String> map = new HashMap<>();
        for (SwmHelmetDeviceDO d : deviceListAll) {
            String tenantIdStr = d.getTenantId() != null ? d.getTenantId().toString() : null;
            map.put(d.getDeviceId(), tenantIdStr);
        }
        stringRedisTemplate.opsForHash().putAll(tempKey, map);

        stringRedisTemplate.execute((RedisCallback<Object>) connection -> {
            byte[] temp = tempKey.getBytes(StandardCharsets.UTF_8);
            byte[] real = SwmRedisKeyConstants.GlobalKey.DEVICE_TO_CORP.getBytes(StandardCharsets.UTF_8);
            connection.rename(temp, real);
            return null;
        });

        XxlJobHelper.log("设备租户映射关系生成完成，设备数量：{}", deviceListAll.size());
        deviceCorpMappingCache.refreshCache();
    }
}
