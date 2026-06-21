package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_safety_file_manage")
public class SwmSafetyFileManageDO extends SwmBaseDO {

    /** 视频封面url地址 */
    private String coverUrl;
    /** 视频标题 */
    private String title;
    /** 视频分类 */
    private String type;
    /** 工种 */
    private String jobType;
    /** 视频时长 */
    private String duration;
    /** 推送日期 */
    private LocalDateTime pushDate;
    /** 推送状态: 0-未推送, 1-已推送 */
    private String pushStatus;
    /** 文件url地址 */
    private String fileUrl;
    /** 全选标记: 1-全选 */
    private String selectAll;
    /** 文件名 */
    private String fileName;
    /** 文件路径 */
    private String filePath;
    /** 文件类型 */
    private String fileType;
    /** 文件大小 */
    private Long fileSize;
    /** 上传者 */
    private String uploadBy;
}
