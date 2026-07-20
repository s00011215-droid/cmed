package com.xiangyun.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.xiangyun.common.context.RlsContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自動填充處理器
 * <p>
 * 在 INSERT / UPDATE 時自動填充審計欄位：
 * - createdBy / createdAt → INSERT 時填充
 * - updatedBy / updatedAt → INSERT 和 UPDATE 時填充
 * </p>
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        RlsContext ctx = RlsContext.get();
        Long userId = ctx != null ? ctx.getUserId() : 0L;
        LocalDateTime now = LocalDateTime.now();

        this.strictInsertFill(metaObject, "createdBy", Long.class, userId);
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updatedBy", Long.class, userId);
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        RlsContext ctx = RlsContext.get();
        Long userId = ctx != null ? ctx.getUserId() : 0L;

        this.strictUpdateFill(metaObject, "updatedBy", Long.class, userId);
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }
}
