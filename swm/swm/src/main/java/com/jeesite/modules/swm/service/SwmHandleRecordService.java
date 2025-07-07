package com.jeesite.modules.swm.service;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmHandleRecordDao;
import com.jeesite.modules.swm.entity.SwmHandleRecord;
import com.jeesite.modules.sys.utils.DictUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 处置记录Service
 * 
 * @author zwf
 * @version 2025-05-16
 */
@Service
@Transactional(readOnly = true)
public class SwmHandleRecordService extends CrudService<SwmHandleRecordDao, SwmHandleRecord> {

    /**
     * 获取单条数据
     * 
     * @param swmHandleRecord
     * @return
     */
    @Override
    public SwmHandleRecord get(SwmHandleRecord swmHandleRecord) {
        return super.get(swmHandleRecord);
    }

    /**
     * 查询分页数据
     * 
     * @param swmHandleRecord
     * @return
     */
    @Override
    public Page<SwmHandleRecord> findPage(SwmHandleRecord swmHandleRecord) {
        // 如果需要传入分页参数，可以通过entity中的分页参数来处理
        return super.findPage(swmHandleRecord);
    }

    /**
     * 查询列表数据
     * 
     * @param swmHandleRecord
     * @return
     */
    @Override
    public List<SwmHandleRecord> findList(SwmHandleRecord swmHandleRecord) {
        return super.findList(swmHandleRecord);
    }

    /**
     * 查询分页数据（包含文本值）
     * 
     * @param page            分页对象
     * @param swmHandleRecord
     * @return
     */
    public Page<SwmHandleRecord> findPageWithTextValues(Page<SwmHandleRecord> page, SwmHandleRecord swmHandleRecord) {
        // 将页面参数设置到entity中
        if (page != null) {
            swmHandleRecord.setPage(page);
        }

        // 调用父类的findPage方法
        Page<SwmHandleRecord> pageResult = super.findPage(swmHandleRecord);

        // 处理文本值
        List<SwmHandleRecord> originalList = pageResult.getList();
        List<SwmHandleRecord> resultList = new ArrayList<>();

        for (SwmHandleRecord record : originalList) {
            // 创建新对象以避免修改原始对象
            SwmHandleRecord newRecord = new SwmHandleRecord(record.getId());

            // 复制所有属性
            newRecord.setRecordName(record.getRecordName());
            newRecord.setWarningId(record.getWarningId());
            newRecord.setWarningRecord(record.getWarningRecord());
            newRecord.setAlarmTime(record.getAlarmTime());
            newRecord.setHandler(record.getHandler());
            newRecord.setHandleTime(record.getHandleTime());
            newRecord.setHandleProcess(record.getHandleProcess());
            newRecord.setHandleStatus(record.getHandleStatus());
            newRecord.setCreateBy(record.getCreateBy());
            newRecord.setCreateDate(record.getCreateDate());
            newRecord.setUpdateBy(record.getUpdateBy());
            newRecord.setUpdateDate(record.getUpdateDate());
            newRecord.setRemarks(record.getRemarks());
            newRecord.setStatus(record.getStatus());
            newRecord.setAttachment(record.getAttachment());

            // 复制关联查询的字段
            newRecord.setWarningMan(record.getWarningMan());

            // 处理预警内容字段：将文本值转换为字典键值
            String warningContent = record.getWarningContent();
            if (warningContent != null && !warningContent.isEmpty()) {
                // 尝试通过字典反向查找键值
                String dictValue = DictUtils.getDictValue("warning_content_enum", warningContent, "");
                if (!dictValue.isEmpty()) {
                    // 找到对应的字典键值，使用键值
                    newRecord.setWarningContent(dictValue);
                } else {
                    // 未找到对应的字典键值，直接使用原文本
                    newRecord.setWarningContent(warningContent);
                }
            } else {
                newRecord.setWarningContent(warningContent);
            }

            // 设置处置状态文本
            newRecord.setHandleStatusText(SwmHandleRecord.HandleStatusEnum.getText(record.getHandleStatus()));

            resultList.add(newRecord);
        }

        // 设置转换后的结果列表
        pageResult.setList(resultList);

        return pageResult;
    }

    /**
     * 保存数据（插入或更新）
     * 
     * @param swmHandleRecord
     */
    @Override
    @Transactional(readOnly = false)
    public void save(SwmHandleRecord swmHandleRecord) {
        // 如果是新记录且有预警ID，且状态为草稿，则先检查是否已存在相同预警ID的草稿记录
        if (swmHandleRecord.getIsNewRecord() &&
                swmHandleRecord.getWarningId() != null &&
                !swmHandleRecord.getWarningId().isEmpty() &&
                SwmHandleRecord.HandleStatusEnum.DRAFT.equals(swmHandleRecord.getHandleStatus())) {

            // 查找相同预警ID的草稿记录
            SwmHandleRecord query = new SwmHandleRecord();
            query.setWarningId(swmHandleRecord.getWarningId());
            query.setHandleStatus(SwmHandleRecord.HandleStatusEnum.DRAFT);

            List<SwmHandleRecord> existingRecords = findList(query);

            // 如果找到了已存在的草稿记录，则更新该记录而不是创建新记录
            if (existingRecords != null && !existingRecords.isEmpty()) {
                SwmHandleRecord existingRecord = existingRecords.get(0);

                // 将现有草稿记录的ID设置到当前记录，以便更新而不是插入
                swmHandleRecord.setId(existingRecord.getId());
                swmHandleRecord.setIsNewRecord(false);

                // 保留现有记录的创建信息
                swmHandleRecord.setCreateBy(existingRecord.getCreateBy());
                swmHandleRecord.setCreateDate(existingRecord.getCreateDate());
            }
        }

        // 调用父类的保存方法
        super.save(swmHandleRecord);
    }

    /**
     * 更新状态
     * 
     * @param swmHandleRecord
     */
    @Override
    @Transactional(readOnly = false)
    public void updateStatus(SwmHandleRecord swmHandleRecord) {
        super.updateStatus(swmHandleRecord);
    }

    /**
     * 删除数据
     * 
     * @param swmHandleRecord
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(SwmHandleRecord swmHandleRecord) {
        super.delete(swmHandleRecord);
    }

    /**
     * 根据预警ID查询处置记录
     * 
     * @param warningId 预警ID
     * @return 处置记录列表
     */
    public List<SwmHandleRecord> findByWarningId(String warningId) {
        SwmHandleRecord record = new SwmHandleRecord();
        record.setWarningId(warningId);
        List<SwmHandleRecord> recordList = findList(record);

        // 确保处置记录名称格式正确
        for (SwmHandleRecord item : recordList) {
            // 检查记录名称格式是否需要更新
            String recordName = item.getRecordName();
            String warningRecord = item.getWarningRecord();

            // 如果记录名称不符合"处置personName触发warningContent"格式，尝试修正
            if (recordName == null || !recordName.contains("处置") || !recordName.contains("触发")) {
                // 从预警记录中提取人员名称
                String personName = "";
                if (warningRecord != null && warningRecord.contains("触发预警记录")) {
                    personName = warningRecord.replace("触发预警记录", "");
                }

                // 从相关联的预警管理记录中获取预警内容（这里简化处理）
                String warningContent = "预警"; // 默认值

                if (personName != null && !personName.isEmpty()) {
                    String formattedName = "处置" + personName + "触发" + warningContent;
                    item.setRecordName(formattedName);
                }
            }
        }

        return recordList;
    }

    /**
     * 更新处置记录名称格式
     * 将记录名称格式从"处置（处置人）触发（预警内容）"更新为"处置personName触发warningContent"
     */
    @Transactional(readOnly = false)
    public void updateRecordNameFormat() {
        // 获取所有记录
        List<SwmHandleRecord> allRecords = this.findList(new SwmHandleRecord());

        for (SwmHandleRecord record : allRecords) {
            String recordName = record.getRecordName();

            // 如果记录名称是旧格式，更新为新格式
            if (recordName != null && recordName.contains("处置（") && recordName.contains("）触发（")) {
                // 提取处置人信息
                String content = recordName.substring(recordName.indexOf("处置（") + 3, recordName.indexOf("）触发（"));

                // 提取预警内容信息
                String warningContent = recordName.substring(recordName.indexOf("）触发（") + 4,
                        recordName.lastIndexOf("）"));

                // 构造新的记录名称格式
                String newRecordName = "处置" + content + "触发" + warningContent;
                record.setRecordName(newRecordName);

                // 保存更新
                this.save(record);
            }
        }
    }

    /**
     * 获取最新的报警处置记录
     * 
     * @param limit 获取的记录数
     * @return
     */
    public List<SwmHandleRecord> latestHandleRecord(int limit) {
        return dao.latestHandleRecord(limit);
    }
}
