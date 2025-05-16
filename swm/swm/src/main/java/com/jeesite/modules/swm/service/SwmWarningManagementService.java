package com.jeesite.modules.swm.service;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmWarningManagementDao;
import com.jeesite.modules.swm.entity.SwmWarningManagement;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 预警管理Service
 * 
 * @author auto create
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
    public Page<SwmWarningManagement> findPage(Page<SwmWarningManagement> page, SwmWarningManagement swmWarningManagement) {
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
            newItem.setWarningType(SwmWarningManagement.WarningTypeEnum.getText(item.getWarningType()));
            // 预警内容保持原样
            newItem.setWarningContent(item.getWarningContent());
            // 处置状态保持原样
            newItem.setHandleStatusText(SwmWarningManagement.HandleStatusEnum.getText(item.getHandleStatus()));
            
            resultList.add(newItem);
        }
        
        return resultList;
    }
    
    /**
     * 根据条件过滤记录
     */
    private List<SwmWarningManagement> filterRecords(List<SwmWarningManagement> allRecords, SwmWarningManagement criteria) {
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
                    System.out.println("Filtered out by personName: " + record.getPersonName() + " !contains " + criteria.getPersonName());
                }
            }
            
            // 根据预警类型过滤（支持中文描述或编码匹配）
            if (criteria.getWarningType() != null && !criteria.getWarningType().isEmpty()) {
                boolean typeMatch = false;
                
                // 情况1：传入的是中文描述，需要转换为编码后比较
                if ("主动预警".equals(criteria.getWarningType()) && 
                    record.getWarningType() != null && 
                    SwmWarningManagement.WarningTypeEnum.ACTIVE.equals(record.getWarningType())) {
                    typeMatch = true;
                } else if ("被动预警".equals(criteria.getWarningType()) && 
                           record.getWarningType() != null && 
                           SwmWarningManagement.WarningTypeEnum.PASSIVE.equals(record.getWarningType())) {
                    typeMatch = true;
                }
                
                // 情况2：直接匹配（可能是编码对编码）
                if (!typeMatch && (record.getWarningType() == null || !record.getWarningType().equals(criteria.getWarningType()))) {
                    match = false;
                    System.out.println("Filtered out by warningType: " + record.getWarningType() + " != " + criteria.getWarningType());
                }
            }
            
            // 根据预警内容过滤（精确匹配）
            if (criteria.getWarningContent() != null && !criteria.getWarningContent().isEmpty()) {
                if (record.getWarningContent() == null || !record.getWarningContent().equals(criteria.getWarningContent())) {
                    match = false;
                    System.out.println("Filtered out by warningContent: " + record.getWarningContent() + " != " + criteria.getWarningContent());
                }
            }
            
            // 根据预警时间范围过滤
            if (criteria.getWarningTime() != null) {
                if (record.getWarningTime() == null || record.getWarningTime().before(criteria.getWarningTime())) {
                    match = false;
                    System.out.println("Filtered out by warningTime: " + record.getWarningTime() + " before " + criteria.getWarningTime());
                }
            }
            
            // 根据处置人过滤（模糊匹配）
            if (criteria.getHandler() != null && !criteria.getHandler().isEmpty()) {
                if (record.getHandler() == null || !record.getHandler().contains(criteria.getHandler())) {
                    match = false;
                    System.out.println("Filtered out by handler: " + record.getHandler() + " !contains " + criteria.getHandler());
                }
            }
            
            // 根据处置状态过滤（支持中文描述或编码匹配）
            if (criteria.getHandleStatus() != null && !criteria.getHandleStatus().isEmpty()) {
                boolean statusMatch = false;
                
                // 情况1：传入的是中文描述，需要转换为编码后比较
                if ("未处置".equals(criteria.getHandleStatus()) && 
                    record.getHandleStatus() != null && 
                    SwmWarningManagement.HandleStatusEnum.UNHANDLED.equals(record.getHandleStatus())) {
                    statusMatch = true;
                } else if ("已处置".equals(criteria.getHandleStatus()) && 
                           record.getHandleStatus() != null && 
                           SwmWarningManagement.HandleStatusEnum.HANDLED.equals(record.getHandleStatus())) {
                    statusMatch = true;
                }
                
                // 情况2：直接匹配（可能是编码对编码）
                if (!statusMatch && (record.getHandleStatus() == null || !record.getHandleStatus().equals(criteria.getHandleStatus()))) {
                    match = false;
                    System.out.println("Filtered out by handleStatus: " + record.getHandleStatus() + " != " + criteria.getHandleStatus());
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
    public Page<SwmWarningManagement> findPageWithTextValues(Page<SwmWarningManagement> page, SwmWarningManagement criteria) {
        System.out.println("findPageWithTextValues Start - page: " + page.getPageNo() + ", size: " + page.getPageSize());
        
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
        List<SwmWarningManagement> pageRecords = (fromIndex < toIndex) ? 
                filteredRecords.subList(fromIndex, toIndex) : new ArrayList<>();
        System.out.println("Page records size: " + pageRecords.size());
        
        // 只转换预警类型为文本值，预警内容保持原样
        for (SwmWarningManagement item : pageRecords) {
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
            
            // 转换预警类型为文本值
            newItem.setWarningType(SwmWarningManagement.WarningTypeEnum.getText(item.getWarningType()));
            // 预警内容保持原样
            newItem.setWarningContent(item.getWarningContent());
            // 转换处置状态为文本值
            newItem.setHandleStatusText(SwmWarningManagement.HandleStatusEnum.getText(item.getHandleStatus()));
            
            resultList.add(newItem);
        }
        
        // 设置分页对象属性
        page.setList(resultList);
        page.setCount(count);
        
        System.out.println("Final page result - count: " + page.getCount() + ", list size: " + (page.getList() != null ? page.getList().size() : "null"));
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
                && (swmWarningManagement.getHandleStatus() == null || swmWarningManagement.getHandleStatus().isEmpty())) {
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