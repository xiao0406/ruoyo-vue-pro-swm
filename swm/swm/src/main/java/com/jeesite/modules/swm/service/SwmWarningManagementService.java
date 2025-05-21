package com.jeesite.modules.swm.service;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmWarningManagementDao;
import com.jeesite.modules.swm.entity.SwmWarningManagement;
import com.jeesite.modules.sys.utils.DictUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 预警管理Service
 * 
 * @author zwf
 * @version 2025-05-16
 */
@Service
@Transactional(readOnly = true)
public class SwmWarningManagementService extends CrudService<SwmWarningManagementDao, SwmWarningManagement> {

    /**
     * 获取单条数据
     */
    @Override
    public SwmWarningManagement get(SwmWarningManagement swmWarningManagement) {
        return super.get(swmWarningManagement);
    }

    /**
     * 查询分页数据
     */
    @Override
    public Page<SwmWarningManagement> findPage(SwmWarningManagement swmWarningManagement) {
        return super.findPage(swmWarningManagement);
    }

    /**
     * 查询分页数据（带分页参数）
     */
    public Page<SwmWarningManagement> findPage(Page<SwmWarningManagement> page,
            SwmWarningManagement swmWarningManagement) {
        swmWarningManagement.setPage(page);
        return this.findPage(swmWarningManagement);
    }

    /**
     * 查询所有记录，不带默认的状态过滤
     */
    public List<SwmWarningManagement> findAllWithoutStatusFilter() {
        return dao.findAllWithoutStatusFilter();
    }

    /**
     * 查询所有记录并转换预警类型为显示文本
     */
    public List<SwmWarningManagement> findAllWithTextValues() {
        List<SwmWarningManagement> originalList = dao.findAllWithoutStatusFilter();
        List<SwmWarningManagement> resultList = new ArrayList<>();

        // 只转换预警类型为文本值，预警内容保持原样
        for (SwmWarningManagement item : originalList) {
            // 创建新对象以避免修改原始对象
            SwmWarningManagement newItem = new SwmWarningManagement(item.getId());

            // 复制所有属性
            newItem.setPersonName(item.getPersonName());
            newItem.setWarningTime(item.getWarningTime());
            newItem.setAlarmRecord(item.getAlarmRecord());
            newItem.setAlarmTime(item.getAlarmTime());
            newItem.setTriggerReason(item.getTriggerReason());
            newItem.setHandler(item.getHandler());
            newItem.setHandleTime(item.getHandleTime());
            newItem.setHandleProcess(item.getHandleProcess());
            newItem.setHandleStatus(item.getHandleStatus());
            newItem.setAttachment(item.getAttachment());
            newItem.setCreateBy(item.getCreateBy());
            newItem.setCreateDate(item.getCreateDate());
            newItem.setUpdateBy(item.getUpdateBy());
            newItem.setUpdateDate(item.getUpdateDate());
            newItem.setRemarks(item.getRemarks());
            newItem.setStatus(item.getStatus());

            // 只转换预警类型为文本值
            newItem.setWarningType(DictUtils.getDictLabel("warning_type_enum", item.getWarningType(), ""));
            // 预警内容保持原样
            newItem.setWarningContent(
                    DictUtils.getDictLabel("warning_content_enum", item.getWarningContent(), item.getWarningContent()));
            // 处置状态保持原样
            newItem.setHandleStatusText(DictUtils.getDictLabel("handle_status_enum", item.getHandleStatus(), ""));

            resultList.add(newItem);
        }

        return resultList;
    }

    /**
     * 根据条件过滤记录
     */
    private List<SwmWarningManagement> filterRecords(List<SwmWarningManagement> allRecords,
            SwmWarningManagement criteria) {
        if (criteria == null) {
            return allRecords;
        }

        // 打印过滤条件
        System.out.println("Filter criteria - personName: " + criteria.getPersonName() +
                "\nwarningType: " + criteria.getWarningType() +
                "\nwarningContent: " + criteria.getWarningContent() +
                "\nwarningTime: " + criteria.getWarningTime() +
                "\nhandler: " + criteria.getHandler() +
                "\nhandleStatus: " + criteria.getHandleStatus());

        List<SwmWarningManagement> filteredList = new ArrayList<>();

        for (SwmWarningManagement record : allRecords) {
            boolean match = true;

            // 根据人员名称过滤（模糊匹配）
            if (criteria.getPersonName() != null && !criteria.getPersonName().isEmpty()) {
                if (record.getPersonName() == null || !record.getPersonName().contains(criteria.getPersonName())) {
                    match = false;
                    System.out.println("Filtered out by personName: " + record.getPersonName() + " !contains "
                            + criteria.getPersonName());
                }
            }

            // 根据预警类型过滤（支持中文描述或编码匹配）
            if (criteria.getWarningType() != null && !criteria.getWarningType().isEmpty()) {
                boolean typeMatch = false;

                // 情况1：传入的是中文描述，需要查找对应的dictValue
                String warningTypeLabel = DictUtils.getDictLabel("warning_type_enum", record.getWarningType(), "");
                if (criteria.getWarningType().equals(warningTypeLabel)) {
                    typeMatch = true;
                }

                // 情况2：传入的是dictValue，直接比较
                if (!typeMatch && !criteria.getWarningType().equals(record.getWarningType())) {
                    // 情况3：传入的可能是dictValue的标签，尝试查找对应的dictValue
                    String dictValue = DictUtils.getDictValue("warning_type_enum", criteria.getWarningType(), "");
                    if (!dictValue.isEmpty() && dictValue.equals(record.getWarningType())) {
                        typeMatch = true;
                    } else {
                        match = false;
                        System.out.println("Filtered out by warningType: " + record.getWarningType() + " != "
                                + criteria.getWarningType());
                    }
                }
            }

            // 根据预警内容过滤（精确匹配）
            if (criteria.getWarningContent() != null && !criteria.getWarningContent().isEmpty()) {
                boolean contentMatch = false;

                // 直接匹配内容值
                if (criteria.getWarningContent().equals(record.getWarningContent())) {
                    contentMatch = true;
                } else {
                    // 尝试匹配字典值或字典标签
                    String contentLabel = DictUtils.getDictLabel("warning_content_enum", record.getWarningContent(),
                            "");
                    String contentValue = DictUtils.getDictValue("warning_content_enum", criteria.getWarningContent(),
                            "");

                    if (criteria.getWarningContent().equals(contentLabel) ||
                            (!contentValue.isEmpty() && contentValue.equals(record.getWarningContent()))) {
                        contentMatch = true;
                    }
                }

                if (!contentMatch) {
                    match = false;
                    System.out.println("Filtered out by warningContent: " + record.getWarningContent() + " != "
                            + criteria.getWarningContent());
                }
            }

            // 根据预警时间范围过滤
            if (criteria.getWarningTime() != null) {
                if (record.getWarningTime() == null || record.getWarningTime().before(criteria.getWarningTime())) {
                    match = false;
                    System.out.println("Filtered out by warningTime: " + record.getWarningTime() + " before "
                            + criteria.getWarningTime());
                }
            }

            // 根据处置人过滤（模糊匹配）
            if (criteria.getHandler() != null && !criteria.getHandler().isEmpty()) {
                if (record.getHandler() == null || !record.getHandler().contains(criteria.getHandler())) {
                    match = false;
                    System.out.println(
                            "Filtered out by handler: " + record.getHandler() + " !contains " + criteria.getHandler());
                }
            }

            // 根据处置状态过滤（支持中文描述或编码匹配）
            if (criteria.getHandleStatus() != null && !criteria.getHandleStatus().isEmpty()) {
                boolean statusMatch = false;

                // 情况1：匹配字典标签
                String handleStatusLabel = DictUtils.getDictLabel("handle_status_enum", record.getHandleStatus(), "");
                if (criteria.getHandleStatus().equals(handleStatusLabel)) {
                    statusMatch = true;
                }

                // 情况2：匹配字典值
                if (!statusMatch && !criteria.getHandleStatus().equals(record.getHandleStatus())) {
                    // 情况3：传入的可能是字典标签，尝试找对应的字典值
                    String dictValue = DictUtils.getDictValue("handle_status_enum", criteria.getHandleStatus(), "");
                    if (!dictValue.isEmpty() && dictValue.equals(record.getHandleStatus())) {
                        statusMatch = true;
                    } else {
                        match = false;
                        System.out.println("Filtered out by handleStatus: " + record.getHandleStatus() + " != "
                                + criteria.getHandleStatus());
                    }
                }
            }

            if (match) {
                filteredList.add(record);
            }
        }

        return filteredList;
    }

    /**
     * 根据条件过滤记录并转换为分页数据
     */
    public Page<SwmWarningManagement> findPageWithTextValues(Page<SwmWarningManagement> page,
            SwmWarningManagement criteria) {
        System.out
                .println("findPageWithTextValues Start - page: " + page.getPageNo() + ", size: " + page.getPageSize());

        // 添加字典测试代码
        String testLabel = DictUtils.getDictLabel("warning_type_enum", "0", "未知");
        System.out.println("字典测试 - 预警类型0的标签: " + testLabel);
        testLabel = DictUtils.getDictLabel("warning_type_enum", "1", "未知");
        System.out.println("字典测试 - 预警类型1的标签: " + testLabel);

        testLabel = DictUtils.getDictLabel("warning_content_enum", "0", "未知");
        System.out.println("字典测试 - 预警内容0的标签: " + testLabel);
        testLabel = DictUtils.getDictLabel("warning_content_enum", "1", "未知");
        System.out.println("字典测试 - 预警内容1的标签: " + testLabel);

        testLabel = DictUtils.getDictLabel("handle_status_enum", "0", "未知");
        System.out.println("字典测试 - 处置状态0的标签: " + testLabel);
        testLabel = DictUtils.getDictLabel("handle_status_enum", "1", "未知");
        System.out.println("字典测试 - 处置状态1的标签: " + testLabel);

        List<SwmWarningManagement> allRecords = findAllWithoutStatusFilter();
        System.out.println("All records size: " + (allRecords != null ? allRecords.size() : "null"));

        List<SwmWarningManagement> filteredRecords = filterRecords(allRecords, criteria);
        System.out.println("Filtered records size: " + (filteredRecords != null ? filteredRecords.size() : "null"));

        List<SwmWarningManagement> resultList = new ArrayList<>();

        // 设置分页结果
        int pageNo = page.getPageNo();
        int pageSize = page.getPageSize();
        int count = filteredRecords.size();

        // 计算起止索引
        int fromIndex = (pageNo - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, count);
        System.out.println("Pagination indexes - from: " + fromIndex + ", to: " + toIndex + ", count: " + count);

        // 防止越界
        if (fromIndex >= count) {
            fromIndex = Math.max(0, count - pageSize);
            toIndex = count;
            System.out.println("Adjusted indexes - from: " + fromIndex + ", to: " + toIndex);
        }

        // 获取当前页数据
        List<SwmWarningManagement> pageRecords = (fromIndex < toIndex) ? filteredRecords.subList(fromIndex, toIndex)
                : new ArrayList<>();
        System.out.println("Page records size: " + pageRecords.size());

        // 只转换预警类型为文本值，预警内容保持原样
        for (SwmWarningManagement item : pageRecords) {
            // 打印调试信息 - 处理前
            System.out.println("处理项目ID:" + item.getId() +
                    ", warningType:" + item.getWarningType() +
                    ", warningContent:" + item.getWarningContent() +
                    ", handleStatus:" + item.getHandleStatus());

            // 创建新对象以避免修改原始对象
            SwmWarningManagement newItem = new SwmWarningManagement(item.getId());

            // 复制所有属性
            newItem.setPersonName(item.getPersonName());
            newItem.setWarningTime(item.getWarningTime());
            newItem.setAlarmRecord(item.getAlarmRecord());
            newItem.setAlarmTime(item.getAlarmTime());
            newItem.setTriggerReason(item.getTriggerReason());
            newItem.setHandler(item.getHandler());
            newItem.setHandleTime(item.getHandleTime());
            newItem.setHandleProcess(item.getHandleProcess());
            newItem.setHandleStatus(item.getHandleStatus());
            newItem.setAttachment(item.getAttachment());
            newItem.setCreateBy(item.getCreateBy());
            newItem.setCreateDate(item.getCreateDate());
            newItem.setUpdateBy(item.getUpdateBy());
            newItem.setUpdateDate(item.getUpdateDate());
            newItem.setRemarks(item.getRemarks());
            newItem.setStatus(item.getStatus());

            // 保存原始值，以便调试
            String origWarningType = item.getWarningType();
            String origWarningContent = item.getWarningContent();
            String origHandleStatus = item.getHandleStatus();

            // 转换预警类型为文本值，需要处理原始值已经是文本的情况
            String warningTypeText;
            if (origWarningType != null) {
                // 检查是否已经是文本值（非数字）
                if (origWarningType.matches("\\d+")) {
                    // 是数字，从字典获取标签
                    warningTypeText = DictUtils.getDictLabel("warning_type_enum", origWarningType, "");
                } else {
                    // 已经是文本，直接使用
                    warningTypeText = origWarningType;
                }
            } else {
                warningTypeText = "";
            }
            newItem.setWarningType(warningTypeText);
            newItem.setWarningTypeText(warningTypeText); // 同时设置显示文本属性

            // 预警内容转换逻辑
            String warningContentText;
            if (origWarningContent != null) {
                if (origWarningContent.matches("\\d+")) {
                    warningContentText = DictUtils.getDictLabel("warning_content_enum", origWarningContent,
                            origWarningContent);
                } else {
                    warningContentText = origWarningContent;
                }
            } else {
                warningContentText = "";
            }
            newItem.setWarningContent(warningContentText);

            // 转换处置状态为文本值
            String handleStatusText;
            if (origHandleStatus != null) {
                if (origHandleStatus.matches("\\d+")) {
                    handleStatusText = DictUtils.getDictLabel("handle_status_enum", origHandleStatus, "");
                } else {
                    handleStatusText = origHandleStatus;
                }
            } else {
                handleStatusText = "";
            }
            newItem.setHandleStatusText(handleStatusText);
            newItem.setHandleStatus(origHandleStatus); // 保留原始值，避免干扰前端处理

            // 打印调试信息 - 处理后
            System.out.println("转换后 ID:" + newItem.getId() +
                    ", 原warningType:" + origWarningType +
                    ", 转换后warningType:" + newItem.getWarningType() +
                    ", 原warningContent:" + origWarningContent +
                    ", 转换后warningContent:" + newItem.getWarningContent() +
                    ", 原handleStatus:" + origHandleStatus +
                    ", 转换后handleStatusText:" + newItem.getHandleStatusText());

            resultList.add(newItem);
        }

        // 设置分页对象属性
        page.setList(resultList);
        page.setCount(count);

        System.out.println("Final page result - count: " + page.getCount() + ", list size: "
                + (page.getList() != null ? page.getList().size() : "null"));

        // 打印第一条数据的详细信息（如果有）
        if (resultList.size() > 0) {
            SwmWarningManagement first = resultList.get(0);
            System.out.println("第一条数据详情: ID:" + first.getId() +
                    ", warningType:" + first.getWarningType() +
                    ", warningTypeText:" + first.getWarningTypeText() +
                    ", warningContent:" + first.getWarningContent() +
                    ", handleStatus:" + first.getHandleStatus() +
                    ", handleStatusText:" + first.getHandleStatusText());
        }

        return page;
    }

    /**
     * 保存数据
     */
    @Override
    @Transactional(readOnly = false)
    public void save(SwmWarningManagement swmWarningManagement) {
        // 如果是新记录，设置预警时间为当前时间
        if (swmWarningManagement.getIsNewRecord()) {
            if (swmWarningManagement.getWarningTime() == null) {
                swmWarningManagement.setWarningTime(new Date());
            }
            // 新记录设置默认处置状态为未处置
            if (swmWarningManagement.getHandleStatus() == null) {
                swmWarningManagement.setHandleStatus(SwmWarningManagement.HandleStatusEnum.UNHANDLED);
            }
        }

        // 如果有处置人和处置时间但没有设置处置状态，则自动设置为已处置
        if (swmWarningManagement.getHandler() != null && !swmWarningManagement.getHandler().isEmpty()
                && swmWarningManagement.getHandleTime() != null
                && (swmWarningManagement.getHandleStatus() == null
                        || swmWarningManagement.getHandleStatus().isEmpty())) {
            swmWarningManagement.setHandleStatus(SwmWarningManagement.HandleStatusEnum.HANDLED);
        }

        super.save(swmWarningManagement);
    }

    /**
     * 删除数据
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(SwmWarningManagement swmWarningManagement) {
        super.delete(swmWarningManagement);
    }
}