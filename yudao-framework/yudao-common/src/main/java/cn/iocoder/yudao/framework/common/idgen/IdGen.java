package cn.iocoder.yudao.framework.common.idgen;

import java.util.UUID;

public final class IdGen {

    private IdGen() {
    }

    public static String nextId() {
        return UUID.randomUUID().toString();
    }
}
