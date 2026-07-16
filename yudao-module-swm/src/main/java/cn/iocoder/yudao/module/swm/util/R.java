package cn.iocoder.yudao.module.swm.util;

import lombok.Data;

import java.io.Serializable;

@Data
public class R<T> implements Serializable {

    public static final int SUCCESS = 0;
    public static final int ERROR = 1;
    public static final int FAIL = ERROR;

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

    public static <T> R<T> fail(String msg) {
        R<T> r = new R<>();
        r.setCode(FAIL);
        r.setMsg(msg);
        return r;
    }

    public static <T> R<T> fail(int code, String msg) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }
}
