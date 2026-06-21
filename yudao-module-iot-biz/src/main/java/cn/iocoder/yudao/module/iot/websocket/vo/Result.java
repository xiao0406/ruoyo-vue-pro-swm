package cn.iocoder.yudao.module.iot.websocket.vo;

import java.io.Serializable;

/**
 * WebSocket API返回结果类
 * 
 * @author Shawn
 * @date 2023-11-01
 */
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码，200表示成功
     */
    private int code;

    /**
     * 消息内容
     */
    private String msg;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 默认构造函数
     */
    public Result() {
    }

    /**
     * 构造函数
     */
    public Result(int code, String msg, T data, boolean success) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.success = success;
    }

    /**
     * 创建成功结果
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null, true);
    }

    /**
     * 创建带消息的成功结果
     */
    public static <T> Result<T> success(String msg) {
        return new Result<>(200, msg, null, true);
    }

    /**
     * 创建带数据的成功结果
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data, true);
    }

    /**
     * 创建带消息和数据的成功结果
     */
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(200, msg, data, true);
    }

    /**
     * 创建失败结果
     */
    public static <T> Result<T> error() {
        return new Result<>(500, "操作失败", null, false);
    }

    /**
     * 创建带消息的失败结果
     */
    public static <T> Result<T> error(String msg) {
        return new Result<>(500, msg, null, false);
    }

    /**
     * 创建带状态码和消息的失败结果
     */
    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg, null, false);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}