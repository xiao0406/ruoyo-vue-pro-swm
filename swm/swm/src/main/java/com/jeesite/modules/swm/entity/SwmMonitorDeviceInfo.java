package com.jeesite.modules.swm.entity;

import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;

/**
 * 监控设备信息实体类
 * 
 * @author Shawn
 */
@Table(name = "js_sys_monitor_device_info", alias = "a", columns = {
        @Column(name = "id", attrName = "id", label = "主键id", isPK = true),
        @Column(name = "rec_type", attrName = "recType", label = "记录类型"),
        @Column(name = "parent_id", attrName = "parentId", label = "父级编号"),
        @Column(name = "tree_sort", attrName = "treeSort", label = "排序号（升序）"),
        @Column(name = "tree_leaf", attrName = "treeLeaf", label = "是否最末级"),
        @Column(name = "parent_name", attrName = "parentName", label = "父级名称"),
        @Column(name = "name", attrName = "name", label = "设备名称", queryType = QueryType.LIKE),
        @Column(name = "code", attrName = "code", label = "设备编号，随机生成"),
        @Column(name = "device_type", attrName = "deviceType", label = "设备类型：海康、大华,数据字典"),
        @Column(name = "ip", attrName = "ip", label = "视频设备IP地址"),
        @Column(name = "rtsp_port", attrName = "rtspPort", label = "rtmp直播流端口"),
        @Column(name = "admin_user", attrName = "adminUser", label = "设备管理用户"),
        @Column(name = "admin_password", attrName = "adminPassword", label = "设备管理用户密码"),
        @Column(name = "rtsp_uri", attrName = "rtspUri", label = "设备资源路径"),
        @Column(name = "channel", attrName = "channel", label = "直播通道"),
        @Column(name = "subtype", attrName = "subtype", label = "码流：主码流、辅码流"),
        @Column(name = "load_source", attrName = "loadSource", label = "加载路径"),
        @Column(name = "stream_url", attrName = "streamUrl", label = "直播流获取路径"),
        @Column(name = "camera_index_code", attrName = "cameraIndexCode", label = "摄像机唯一标识码"),
        @Column(includeEntity = DataEntity.class),
        @Column(includeEntity= BaseEntity.class),
}, orderBy = "a.update_date DESC")
public class SwmMonitorDeviceInfo extends DataEntity<SwmMonitorDeviceInfo> {

    private static final long serialVersionUID = 1L;

    private String recType; // 记录类型
    private String parentId; // 父级编号
    private String treeSort; // 排序号（升序）
    private String treeLeaf; // 是否最末级
    private String parentName; // 父级名称
    private String name; // 设备名称
    private String code; // 设备编号，随机生成
    private String deviceType; // 设备类型：海康、大华,数据字典
    private String ip; // 视频设备IP地址
    private String rtspPort; // rtmp直播流端口
    private String adminUser; // 设备管理用户
    private String adminPassword; // 设备管理用户密码
    private String rtspUri; // 设备资源路径
    private String channel; // 直播通道
    private String subtype; // 码流：主码流、辅码流
    private String loadSource; // 加载路径
    private String streamUrl; // 直播流获取路径
    private String cameraIndexCode; // 摄像机唯一标识码

    public SwmMonitorDeviceInfo() {
        this(null);
    }

    public SwmMonitorDeviceInfo(String id) {
        super(id);
    }

    // Getter and Setter methods
    public String getRecType() {
        return recType;
    }

    public void setRecType(String recType) {
        this.recType = recType;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getTreeSort() {
        return treeSort;
    }

    public void setTreeSort(String treeSort) {
        this.treeSort = treeSort;
    }

    public String getTreeLeaf() {
        return treeLeaf;
    }

    public void setTreeLeaf(String treeLeaf) {
        this.treeLeaf = treeLeaf;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getRtspPort() {
        return rtspPort;
    }

    public void setRtspPort(String rtspPort) {
        this.rtspPort = rtspPort;
    }

    public String getAdminUser() {
        return adminUser;
    }

    public void setAdminUser(String adminUser) {
        this.adminUser = adminUser;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public String getRtspUri() {
        return rtspUri;
    }

    public void setRtspUri(String rtspUri) {
        this.rtspUri = rtspUri;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public String getLoadSource() {
        return loadSource;
    }

    public void setLoadSource(String loadSource) {
        this.loadSource = loadSource;
    }

    public String getStreamUrl() {
        return streamUrl;
    }

    public void setStreamUrl(String streamUrl) {
        this.streamUrl = streamUrl;
    }

    public String getCameraIndexCode() {
        return cameraIndexCode;
    }

    public void setCameraIndexCode(String cameraIndexCode) {
        this.cameraIndexCode = cameraIndexCode;
    }

    public boolean getIsTreeLeaf() {
        return "1".equals(this.treeLeaf);
    }
}
