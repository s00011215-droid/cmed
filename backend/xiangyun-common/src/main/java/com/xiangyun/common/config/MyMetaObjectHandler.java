package com.xiangyun.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.xiangyun.common.context.RlsContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        RlsContext ctx = RlsContext.get();
        Long userId = (ctx != null && ctx.getUserId() != null) ? ctx.getUserId() : 0L;
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        this.strictInsertFill(metaObject, "createdBy", Long.class, userId);
        this.strictInsertFill(metaObject, "createdAt", OffsetDateTime.class, now);
        this.strictInsertFill(metaObject, "updatedBy", Long.class, userId);
        this.strictInsertFill(metaObject, "updatedAt", OffsetDateTime.class, now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        RlsContext ctx = RlsContext.get();
        Long userId = (ctx != null && ctx.getUserId() != null) ? ctx.getUserId() : 0L;

        this.strictUpdateFill(metaObject, "updatedBy", Long.class, userId);
        this.strictUpdateFill(metaObject, "updatedAt", OffsetDateTime.class, OffsetDateTime.now(ZoneOffset.UTC));
    }
}
