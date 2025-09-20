# TDengine分页导出开发指南

## 概述

本文档总结了在开发TDengine原始消息日志查询页面过程中遇到的关键技术问题及解决方案，为后续类似页面开发提供参考。

## 核心问题与解决方案

### 1. TDengine 999条记录限制问题

#### 问题描述
- TDengine REST API单次查询最多返回999条记录
- JeeSite框架的Page对象也有类似限制
- 用户需要查看更多数据（如10000条）时被限制

#### 解决方案
**采用分批查询策略**

```java
@Override
public List<SwmRawMessageLogVO> findPageData(int pageNo, int pageSize, SwmRawMessageLog entity) {
    List<SwmRawMessageLogVO> allResults = new ArrayList<>();

    // 如果pageSize > 999，使用分批查询绕过TDengine限制
    if (pageSize > 999) {
        int batchSize = 999; // TDengine的限制
        int offset = (pageNo - 1) * pageSize;
        int remaining = pageSize;
        int currentOffset = offset;

        while (remaining > 0) {
            int currentBatchSize = Math.min(batchSize, remaining);

            // 构建当前批次的SQL
            String batchSql = buildQuerySql(entity, true,
                currentOffset / batchSize + 1, currentBatchSize);

            // 手动调整SQL的OFFSET
            batchSql = batchSql.replaceAll("LIMIT \\d+ OFFSET \\d+",
                "LIMIT " + currentBatchSize + " OFFSET " + currentOffset);

            // 执行查询并合并结果
            R<JSONObject> result = tdengineService.executeTDengineSQL(batchSql);
            // ... 处理结果并添加到allResults

            currentOffset += currentBatchSize;
            remaining -= currentBatchSize;
        }
    } else {
        // 正常查询流程
        // ...
    }

    return allResults;
}
```

**关键点：**
- 检测pageSize是否超过999
- 将大查询拆分为多个小查询
- 精确控制OFFSET和LIMIT参数
- 合并所有批次的查询结果

### 2. 跨页序号连续性问题

#### 问题描述
- 每页序号都从1开始，用户无法了解记录的全局位置
- 导出Excel时序号也不连续

#### 解决方案
**计算全局序号**

**前端实现：**
```javascript
// 表格列配置
{
  title: '序号',
  dataIndex: 'index',
  width: 80,
  fixed: 'left',
  customRender: ({ index }) => {
    // 正确计算跨页序号：(当前页码-1) × 每页条数 + 当前页内索引 + 1
    return (currentPage.value - 1) * currentPageSize.value + index + 1;
  },
}

// 在beforeFetch中记录分页参数
beforeFetch: (params) => {
  if (params.pageNo) currentPage.value = params.pageNo;
  if (params.pageSize) currentPageSize.value = params.pageSize;
  // ...
}
```

**后端导出实现：**
```java
private List<SwmRawMessageLogExcelVO> convertToExcelVOList(List<SwmRawMessageLogVO> list, int pageNo, int pageSize) {
    List<SwmRawMessageLogExcelVO> result = new ArrayList<>();
    for (int i = 0; i < list.size(); i++) {
        SwmRawMessageLogVO vo = list.get(i);
        SwmRawMessageLogExcelVO excelVO = convertToExcelVO(vo);
        // 计算跨页序号
        int rowNum = (pageNo - 1) * pageSize + i + 1;
        excelVO.setRowNum(rowNum);
        result.add(excelVO);
    }
    return result;
}
```

**序号计算公式：**
```
全局序号 = (当前页码 - 1) × 每页条数 + 当前页内索引 + 1
```

### 3. 导出当前页问题

#### 问题描述
- 用户在第N页点击导出，但导出的总是第1页数据
- 导出功能没有考虑用户当前查看的页面

#### 解决方案
**传递分页上下文**

**前端修改：**
```javascript
// 导出函数
const handleExport = async () => {
  const form = getForm();
  const formValues = await form.getFieldsValue();

  const exportParams = { ...formValues };
  // ... 处理其他参数

  // 添加当前页分页参数，确保导出当前页数据
  exportParams.pageNo = currentPage.value;
  exportParams.pageSize = currentPageSize.value;

  const data = await exportRawMessageLog(exportParams);
  // ...
};
```

**后端修改：**
```java
@RequestMapping(value = "exportData")
@ResponseBody
public String exportData(SwmRawMessageLog swmRawMessageLog, HttpServletRequest request, HttpServletResponse response) {
    // 获取分页参数
    int pageNo = 1;
    int pageSize = 1000;

    String pageNoStr = request.getParameter("pageNo");
    String pageSizeStr = request.getParameter("pageSize");

    // 解析分页参数
    if (pageNoStr != null && !pageNoStr.isEmpty()) {
        pageNo = Integer.parseInt(pageNoStr);
    }
    if (pageSizeStr != null && !pageSizeStr.isEmpty()) {
        pageSize = Integer.parseInt(pageSizeStr);
    }

    // 使用findPageData方法获取当前页数据
    List<SwmRawMessageLogVO> list = swmRawMessageLogService.findPageData(pageNo, pageSize, swmRawMessageLog);

    // 导出时传入分页参数用于序号计算
    EasyExcel.write(response.getOutputStream(), SwmRawMessageLogExcelVO.class)
        .sheet("原始消息日志")
        .doWrite(convertToExcelVOList(list, pageNo, pageSize));
}
```

## 完整开发模式

### 1. 前端开发要点

**表格配置：**
```javascript
const [registerTable, { reload, getForm, setProps }] = useTable({
  // 分页配置
  pagination: {
    pageSize: 1000,
    showSizeChanger: true,
    showQuickJumper: true,
    pageSizeOptions: ['100', '500', '1000', '5000', '10000'],
    showTotal: (total) => `共 ${total} 条数据`,
  },

  // 固定底部分页栏的关键配置
  scroll: {
    x: 1200,
    y: 'calc(100vh - 300px)', // 关键：固定高度
  },

  // 数据处理
  beforeFetch: (params) => {
    // 记录分页参数用于序号计算
    if (params.pageNo) currentPage.value = params.pageNo;
    if (params.pageSize) currentPageSize.value = params.pageSize;

    // 处理查询参数
    // ...
    return params;
  },
});
```

**序号列配置：**
```javascript
{
  title: '序号',
  dataIndex: 'index',
  width: 80,
  fixed: 'left',
  customRender: ({ index }) => {
    return (currentPage.value - 1) * currentPageSize.value + index + 1;
  },
}
```

### 2. 后端开发要点

**Service层实现：**
```java
public interface SwmRawMessageLogService {
    // 传统分页方法（兼容性）
    Page<SwmRawMessageLogVO> findPage(Page<SwmRawMessageLogVO> page, SwmRawMessageLog entity);

    // 直接分页方法（绕过框架限制）
    List<SwmRawMessageLogVO> findPageData(int pageNo, int pageSize, SwmRawMessageLog entity);

    // 计数方法
    long count(SwmRawMessageLog entity);
}
```

**Controller层实现：**
```java
// 列表查询
@RequestMapping(value = "listData")
@ResponseBody
public Page<SwmRawMessageLogVO> listData(SwmRawMessageLog entity, HttpServletRequest request) {
    // 手动获取分页参数，绕过JeeSite框架限制
    int pageNo = Integer.parseInt(request.getParameter("pageNo"));
    int pageSize = Math.min(Integer.parseInt(request.getParameter("pageSize")), 10000);

    List<SwmRawMessageLogVO> list = service.findPageData(pageNo, pageSize, entity);
    long totalCount = service.count(entity);

    // 手动构建Page对象
    Page<SwmRawMessageLogVO> page = new Page<>();
    page.setPageNo(pageNo);
    page.setPageSize(pageSize);
    page.setList(list);
    page.setCount(totalCount);

    return page;
}

// 导出功能
@RequestMapping(value = "exportData")
@ResponseBody
public String exportData(SwmRawMessageLog entity, HttpServletRequest request, HttpServletResponse response) {
    // 获取分页参数
    int pageNo = Integer.parseInt(request.getParameter("pageNo"));
    int pageSize = Integer.parseInt(request.getParameter("pageSize"));

    // 使用相同的查询逻辑
    List<SwmRawMessageLogVO> list = service.findPageData(pageNo, pageSize, entity);

    // 导出时包含序号
    EasyExcel.write(response.getOutputStream(), ExcelVO.class)
        .sheet("数据")
        .doWrite(convertToExcelVOList(list, pageNo, pageSize));
}
```

### 3. Excel VO设计

```java
public static class SwmRawMessageLogExcelVO {
    @ExcelProperty("序号")
    private Integer rowNum;

    @ExcelProperty("time")
    private String time;

    // 其他字段...

    // getter和setter方法
    public Integer getRowNum() { return rowNum; }
    public void setRowNum(Integer rowNum) { this.rowNum = rowNum; }
    // ...
}
```

## 最佳实践总结

### 1. 分页查询
- 始终提供绕过框架限制的直接分页方法
- 使用分批查询处理大数据量
- 手动构建Page对象确保参数正确传递

### 2. 序号处理
- 前端和后端都要计算全局序号
- 公式：`(pageNo - 1) * pageSize + index + 1`
- 导出时要包含序号字段

### 3. 导出功能
- 导出要考虑用户当前页面状态
- 传递完整的分页上下文
- 复用查询逻辑确保数据一致性

### 4. TDengine特殊处理
- 了解REST API的999条限制
- 实现分批查询策略
- 正确处理OFFSET和LIMIT参数

### 5. 代码质量
- 保持前后端逻辑一致性
- 提供详细的参数验证和错误处理
- 遵循项目编码规范

## 常见问题排查

1. **序号不连续**：检查全局序号计算公式
2. **分页显示不全**：检查TDengine批量查询逻辑
3. **导出错误页**：检查分页参数传递
4. **底部分页栏不固定**：检查scroll.y配置
5. **性能问题**：优化SQL查询和分批处理逻辑

通过遵循本指南，可以有效避免类似问题，快速开发出功能完整的TDengine分页查询导出页面。