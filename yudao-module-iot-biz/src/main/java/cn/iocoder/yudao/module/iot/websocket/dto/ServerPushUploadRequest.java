package cn.iocoder.yudao.module.iot.websocket.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 服务端选择文件上传请求
 *
 * @author Shawn
 * @date 2024-06-09
 */
@Data
public class ServerPushUploadRequest {

    /**
     * 设备ID
     */
    @NotBlank(message = "设备ID不能为空")
    private String deviceId;

    /**
     * 对讲ID
     */
    @NotBlank(message = "对讲ID不能为空")
    private String talkbackId;

    /**
     * 文件列表
     */
    @NotEmpty(message = "文件列表不能为空")
    private List<String> fileList;
}
