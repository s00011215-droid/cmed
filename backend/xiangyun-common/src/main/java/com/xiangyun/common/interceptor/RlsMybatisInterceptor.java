package com.xiangyun.common.interceptor;

import com.xiangyun.common.context.RlsContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * MyBatis 攔截器 — 在每次 SQL 執行前設定 RLS 上下文
 * <p>
 * 透過 {@code SET LOCAL app.clinic_id = ?} 在資料庫層強制租戶隔離。
 * 每個 SQL 執行前自動注入，避免應用層遺漏。
 * </p>
 */
@Slf4j
@Intercepts({
        @Signature(
                type = StatementHandler.class,
                method = "prepare",
                args = {Connection.class, Integer.class}
        )
})
public class RlsMybatisInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        RlsContext ctx = RlsContext.get();
        if (ctx != null && ctx.getClinicId() != null) {
            Connection connection = (Connection) invocation.getArgs()[0];
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("SET LOCAL app.clinic_id = '" + ctx.getClinicId() + "'");
                stmt.execute("SET LOCAL app.user_id = '" + ctx.getUserId() + "'");
                stmt.execute("SET LOCAL app.user_name = '"
                        + (ctx.getUserName() != null ? ctx.getUserName().replace("'", "''") : "")
                        + "'");
                stmt.execute("SET LOCAL app.client_ip = '"
                        + (ctx.getClientIp() != null ? ctx.getClientIp() : "")
                        + "'");
                stmt.execute("SET LOCAL app.session_id = '"
                        + (ctx.getSessionId() != null ? ctx.getSessionId() : "")
                        + "'");
            } catch (SQLException e) {
                log.warn("Failed to SET LOCAL RLS context: {}", e.getMessage());
            }
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // no-op
    }
}
