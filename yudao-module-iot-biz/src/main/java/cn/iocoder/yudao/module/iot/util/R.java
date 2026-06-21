package cn.iocoder.yudao.module.iot.util;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回结果
 *
 * 迁移自 JeeSite: com.jeesite.modules.utils.R
 */
@Data
public class R<T> implements Serializable {

    public static final int SUCCESS = 0;
    public static final int ERROR = 1;

    private int code;
    private String msg;
    private T data;

    public static <T> R<T> ok() {
        return ok(null);
    }

    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.setCode(SUCCESS);
        r.setMsg("success");
        r.setData(data);
        return r;
    }

    public static <T> R<T> error(String msg) {
        R<T> r = new R<>();
        r.setCode(ERROR);
        r.setMsg(msg);
        return r;
    }

    public static <T> R<T> error(int code, String msg) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }

}
