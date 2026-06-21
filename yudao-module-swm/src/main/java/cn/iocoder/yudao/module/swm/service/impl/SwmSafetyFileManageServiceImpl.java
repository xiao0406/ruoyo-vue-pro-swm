package cn.iocoder.yudao.module.swm.service.impl;

import cn.iocoder.yudao.module.swm.dal.dataobject.SwmSafetyFileManageDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmSafetyFileManageMapper;
import cn.iocoder.yudao.module.swm.service.SwmSafetyFileManageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Validated
public class SwmSafetyFileManageServiceImpl
        extends ServiceImpl<SwmSafetyFileManageMapper, SwmSafetyFileManageDO>
        implements SwmSafetyFileManageService {

    @Override
    public SwmSafetyFileManageDO getSwmSafetyFileManage(String id) {
        return getBaseMapper().selectById(id);
    }

    @Override
    public List<SwmSafetyFileManageDO> findList(LocalDateTime pushDateStart, LocalDateTime pushDateEnd) {
        return getBaseMapper().selectList(
                new LambdaQueryWrapper<SwmSafetyFileManageDO>()
                        .ge(SwmSafetyFileManageDO::getPushDate, pushDateStart)
                        .le(SwmSafetyFileManageDO::getPushDate, pushDateEnd)
        );
    }

    public String handleFileUpload(String fileUrl, String fileName, String bizType) {
        SwmSafetyFileManageDO file = new SwmSafetyFileManageDO();
        file.setFilePath(fileUrl);
        file.setFileName(fileName);
        file.setFileType(bizType);
        getBaseMapper().insert(file);
        return file.getId();
    }

}
