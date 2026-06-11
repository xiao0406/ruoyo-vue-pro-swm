package com.jeesite.modules.swm.entity;

import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;

import java.util.Date;

/**
 * 第三方接口调用日志。
 */
@Table(name = "swm_third_api_log", alias = "a", columns = {
        @Column(name = "id", attrName = "id", label = "ID", isPK = true),
        @Column(name = "business_type", attrName = "businessType", label = "业务类型", queryType = QueryType.LIKE),
        @Column(name = "http_method", attrName = "httpMethod", label = "请求方式", queryType = QueryType.LIKE),
        @Column(name = "request_url", attrName = "requestUrl", label = "接口地址", queryType = QueryType.LIKE),
        @Column(name = "request_param", attrName = "requestParam", label = "请求参数"),
        @Column(name = "response_param", attrName = "responseParam", label = "返回参数"),
        @Column(name = "request_time", attrName = "requestTime", label = "请求时间"),
        @Column(name = "response_time", attrName = "responseTime", label = "响应时间"),
        @Column(name = "duration", attrName = "duration", label = "接口耗时"),
        @Column(name = "http_status", attrName = "httpStatus", label = "HTTP状态码"),
        @Column(name = "execute_status", attrName = "executeStatus", label = "执行状态", queryType = QueryType.LIKE),
        @Column(name = "exception_info", attrName = "exceptionInfo", label = "异常信息"),
        @Column(includeEntity = DataEntity.class),
        @Column(includeEntity = BaseEntity.class),
}, orderBy = "a.request_time DESC")
public class SwmThirdApiLog extends DataEntity<SwmThirdApiLog> {

    private static final long serialVersionUID = 1L;

    private String businessType; // 业务类型
    private String httpMethod; // 请求方式
    private String requestUrl; // 接口地址
    private String requestParam; // 请求参数
    private String responseParam; // 返回参数
    private Date requestTime; // 请求时间
    private Date responseTime; // 响应时间
    private Long duration; // 接口耗时（毫秒）
    private Integer httpStatus; // HTTP状态码
    private String executeStatus; // 执行状态：1-成功，0-失败
    private String exceptionInfo; // 异常信息

    public SwmThirdApiLog() {
        this(null);
    }

    public SwmThirdApiLog(String id) {
        super(id);
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getRequestParam() {
        return requestParam;
    }

    public void setRequestParam(String requestParam) {
        this.requestParam = requestParam;
    }

    public String getResponseParam() {
        return responseParam;
    }

    public void setResponseParam(String responseParam) {
        this.responseParam = responseParam;
    }

    public Date getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Date requestTime) {
        this.requestTime = requestTime;
    }

    public Date getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Date responseTime) {
        this.responseTime = responseTime;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(Integer httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getExecuteStatus() {
        return executeStatus;
    }

    public void setExecuteStatus(String executeStatus) {
        this.executeStatus = executeStatus;
    }

    public String getExceptionInfo() {
        return exceptionInfo;
    }

    public void setExceptionInfo(String exceptionInfo) {
        this.exceptionInfo = exceptionInfo;
    }
}
