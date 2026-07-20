package com.xiangyun.common.util;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;

/**
 * 雪花 ID 生成器（單例）
 * <p>
 * 使用 Hutool 的 Snowflake 實現，workerId 從環境變數 WORKER_ID 讀取。
 * 分散式部署時，每個服務實例應設定不同的 WORKER_ID（1-31）。
 * </p>
 *
 * <pre>
 * 結構：41-bit 時間戳 | 5-bit workerId | 5-bit datacenterId | 12-bit 序列號
 * 範圍：每秒每節點約 40 萬個唯一 ID
 * </pre>
 */
public final class SnowflakeIdGenerator {

    private static final Snowflake SNOWFLAKE;

    static {
        long workerId = Long.parseLong(
                System.getenv().getOrDefault("WORKER_ID", "1"));
        long datacenterId = Long.parseLong(
                System.getenv().getOrDefault("DATACENTER_ID", "1"));
        SNOWFLAKE = IdUtil.getSnowflake(workerId, datacenterId);
    }

    private SnowflakeIdGenerator() {
    }

    /**
     * 生成全域唯一 ID（64-bit long）
     */
    public static long nextId() {
        return SNOWFLAKE.nextId();
    }

    /**
     * 生成全域唯一 ID（字串）
     */
    public static String nextIdStr() {
        return String.valueOf(SNOWFLAKE.nextId());
    }
}
