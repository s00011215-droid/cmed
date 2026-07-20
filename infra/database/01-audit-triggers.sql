-- ============================================================================
-- 祥雲智方中醫診症系統 — P0 修復：審計日誌自動化觸發器
-- 版本：V1.0
-- 說明：為核心業務表建立自動審計觸發器，確保所有資料變更留有完整軌跡
-- 依賴：需先執行主初始化腳本（audit_log 表已建立）
-- ============================================================================

BEGIN;

-- ============================================================================
-- 1. 通用審計觸發器函數
-- ============================================================================

-- 1.1 通用 INSERT/UPDATE/DELETE 審計函數
CREATE OR REPLACE FUNCTION fn_audit_log()
RETURNS TRIGGER AS $$
DECLARE
    v_clinic_id   BIGINT;
    v_user_id     BIGINT;
    v_user_name   VARCHAR(64);
    v_patient_id  BIGINT;
    v_old_data    JSONB;
    v_new_data    JSONB;
    v_changed_fields TEXT[];
    v_action      audit_action;
    v_field       TEXT;
BEGIN
    -- 從應用層設定的 session 變數中獲取上下文
    v_user_id   := NULLIF(current_setting('app.user_id', true), '')::BIGINT;
    v_user_name := NULLIF(current_setting('app.user_name', true), '');

    -- 確定操作類型
    IF TG_OP = 'INSERT' THEN
        v_action := 'INSERT';
        v_new_data := row_to_json(NEW)::jsonb;
    ELSIF TG_OP = 'UPDATE' THEN
        v_action := 'UPDATE';
        v_old_data := row_to_json(OLD)::jsonb;
        v_new_data := row_to_json(NEW)::jsonb;

        -- 計算變更的欄位
        v_changed_fields := '{}';
        FOR v_field IN
            SELECT key FROM jsonb_each(v_new_data)
            WHERE v_new_data->>key IS DISTINCT FROM v_old_data->>key
              AND key NOT IN ('updated_at')  -- 排除自動更新的時間戳
        LOOP
            v_changed_fields := array_append(v_changed_fields, v_field);
        END LOOP;

        -- 如果沒有任何實際變更，跳過審計
        IF array_length(v_changed_fields, 1) IS NULL THEN
            RETURN NEW;
        END IF;
    ELSIF TG_OP = 'DELETE' THEN
        v_action := 'DELETE';
        v_old_data := row_to_json(OLD)::jsonb;
    END IF;

    -- 提取 clinic_id（假設目標表有此欄位）
    BEGIN
        IF TG_OP IN ('INSERT', 'UPDATE') AND NEW ? 'clinic_id' THEN
            v_clinic_id := (NEW->>'clinic_id')::BIGINT;
        ELSIF TG_OP = 'DELETE' AND OLD ? 'clinic_id' THEN
            v_clinic_id := (OLD->>'clinic_id')::BIGINT;
        END IF;
    EXCEPTION WHEN OTHERS THEN
        v_clinic_id := NULL;
    END;

    -- 提取 patient_id
    BEGIN
        IF TG_OP IN ('INSERT', 'UPDATE') AND NEW ? 'patient_id' THEN
            v_patient_id := (NEW->>'patient_id')::BIGINT;
        ELSIF TG_OP = 'DELETE' AND OLD ? 'patient_id' THEN
            v_patient_id := (OLD->>'patient_id')::BIGINT;
        END IF;
    EXCEPTION WHEN OTHERS THEN
        v_patient_id := NULL;
    END;

    -- 寫入審計日誌
    INSERT INTO audit_log (
        id, clinic_id, user_id, user_name, action,
        target_table, target_id, patient_id,
        old_data, new_data, changed_fields,
        ip_address, session_id, created_at
    ) VALUES (
        next_id(),
        v_clinic_id,
        v_user_id,
        v_user_name,
        v_action,
        TG_TABLE_NAME,
        CASE
            WHEN TG_OP IN ('INSERT','UPDATE') AND NEW ? 'id' THEN (NEW->>'id')::BIGINT
            WHEN TG_OP = 'DELETE' AND OLD ? 'id' THEN (OLD->>'id')::BIGINT
            ELSE NULL
        END,
        v_patient_id,
        v_old_data,
        v_new_data,
        v_changed_fields,
        NULLIF(current_setting('app.client_ip', true), '')::INET,
        NULLIF(current_setting('app.session_id', true), ''),
        now()
    );

    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ============================================================================
-- 2. 為核心業務表掛載審計觸發器
-- ============================================================================

-- 2.1 處方表審計
DROP TRIGGER IF EXISTS trg_audit_prescription ON prescription;
CREATE TRIGGER trg_audit_prescription
    AFTER INSERT OR UPDATE OR DELETE ON prescription
    FOR EACH ROW EXECUTE FUNCTION fn_audit_log();

-- 2.2 電子病歷審計
DROP TRIGGER IF EXISTS trg_audit_emr ON emr;
CREATE TRIGGER trg_audit_emr
    AFTER INSERT OR UPDATE OR DELETE ON emr
    FOR EACH ROW EXECUTE FUNCTION fn_audit_log();

-- 2.3 患者資料審計
DROP TRIGGER IF EXISTS trg_audit_patient ON patient;
CREATE TRIGGER trg_audit_patient
    AFTER INSERT OR UPDATE OR DELETE ON patient
    FOR EACH ROW EXECUTE FUNCTION fn_audit_log();

-- 2.4 庫存變動審計
DROP TRIGGER IF EXISTS trg_audit_inventory ON inventory;
CREATE TRIGGER trg_audit_inventory
    AFTER INSERT OR UPDATE OR DELETE ON inventory
    FOR EACH ROW EXECUTE FUNCTION fn_audit_log();

-- 2.5 煎藥訂單審計
DROP TRIGGER IF EXISTS trg_audit_decoction_order ON decoction_order;
CREATE TRIGGER trg_audit_decoction_order
    AFTER INSERT OR UPDATE OR DELETE ON decoction_order
    FOR EACH ROW EXECUTE FUNCTION fn_audit_log();

-- 2.6 物流訂單審計
DROP TRIGGER IF EXISTS trg_audit_logistics_order ON logistics_order;
CREATE TRIGGER trg_audit_logistics_order
    AFTER INSERT OR UPDATE OR DELETE ON logistics_order
    FOR EACH ROW EXECUTE FUNCTION fn_audit_log();

-- 2.7 支付記錄審計
DROP TRIGGER IF EXISTS trg_audit_payment ON payment;
CREATE TRIGGER trg_audit_payment
    AFTER INSERT OR UPDATE OR DELETE ON payment
    FOR EACH ROW EXECUTE FUNCTION fn_audit_log();

-- 2.8 用戶帳號審計
DROP TRIGGER IF EXISTS trg_audit_user_account ON user_account;
CREATE TRIGGER trg_audit_user_account
    AFTER INSERT OR UPDATE OR DELETE ON user_account
    FOR EACH ROW EXECUTE FUNCTION fn_audit_log();

-- ============================================================================
-- 3. 敏感操作審計 — 手動調用函數
-- ============================================================================

-- 3.1 敏感資料檢視審計（供應用層在查詢敏感資料時調用）
CREATE OR REPLACE FUNCTION audit_sensitive_access(
    p_patient_id   BIGINT,
    p_target_table VARCHAR(64),
    p_target_id    BIGINT,
    p_action       VARCHAR(16) DEFAULT 'VIEW_SENSITIVE'
)
RETURNS void AS $$
DECLARE
    v_clinic_id BIGINT := NULLIF(current_setting('app.clinic_id', true), '')::BIGINT;
    v_user_id   BIGINT := NULLIF(current_setting('app.user_id', true), '')::BIGINT;
    v_user_name VARCHAR(64) := NULLIF(current_setting('app.user_name', true), '');
BEGIN
    INSERT INTO audit_log (
        id, clinic_id, user_id, user_name,
        action, target_table, target_id, patient_id,
        ip_address, session_id, notes, created_at
    ) VALUES (
        next_id(), v_clinic_id, v_user_id, v_user_name,
        p_action::audit_action, p_target_table, p_target_id, p_patient_id,
        NULLIF(current_setting('app.client_ip', true), '')::INET,
        NULLIF(current_setting('app.session_id', true), ''),
        'Manual sensitive access audit',
        now()
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 3.2 登入審計（應用層在登入成功/失敗時調用）
CREATE OR REPLACE FUNCTION audit_login(
    p_user_id     BIGINT,
    p_user_name   VARCHAR(64),
    p_success     BOOLEAN,
    p_fail_reason VARCHAR(256) DEFAULT NULL
)
RETURNS void AS $$
DECLARE
    v_clinic_id BIGINT := NULLIF(current_setting('app.clinic_id', true), '')::BIGINT;
BEGIN
    INSERT INTO audit_log (
        id, clinic_id, user_id, user_name,
        action, target_table, target_id,
        ip_address, user_agent, session_id, notes, created_at
    ) VALUES (
        next_id(), v_clinic_id, p_user_id, p_user_name,
        CASE WHEN p_success THEN 'LOGIN' ELSE 'LOGIN'::audit_action END,  -- 統一為 LOGIN，通過 notes 區分
        'user_account', p_user_id,
        NULLIF(current_setting('app.client_ip', true), '')::INET,
        NULLIF(current_setting('app.user_agent', true), ''),
        NULLIF(current_setting('app.session_id', true), ''),
        CASE WHEN p_success THEN 'Login success'
             ELSE 'Login failed: ' || COALESCE(p_fail_reason, 'unknown') END,
        now()
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 3.3 資料匯出審計
CREATE OR REPLACE FUNCTION audit_export(
    p_patient_ids  BIGINT[],
    p_export_type  VARCHAR(32),     -- csv, pdf, print
    p_reason       TEXT DEFAULT NULL
)
RETURNS void AS $$
DECLARE
    v_clinic_id BIGINT := NULLIF(current_setting('app.clinic_id', true), '')::BIGINT;
    v_user_id   BIGINT := NULLIF(current_setting('app.user_id', true), '')::BIGINT;
    v_user_name VARCHAR(64) := NULLIF(current_setting('app.user_name', true), '');
    v_pid       BIGINT;
BEGIN
    FOREACH v_pid IN ARRAY p_patient_ids LOOP
        INSERT INTO audit_log (
            id, clinic_id, user_id, user_name,
            action, target_table, target_id, patient_id,
            ip_address, session_id, notes, created_at
        ) VALUES (
            next_id(), v_clinic_id, v_user_id, v_user_name,
            'EXPORT', 'patient_data', v_pid, v_pid,
            NULLIF(current_setting('app.client_ip', true), '')::INET,
            NULLIF(current_setting('app.session_id', true), ''),
            format('Export type: %s, Reason: %s', p_export_type, COALESCE(p_reason, 'N/A')),
            now()
        );
    END LOOP;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ============================================================================
-- 4. 審計查詢輔助 — 違規行為偵測視圖
-- ============================================================================

-- 4.1 異常訪問檢測（非同診所用戶訪問患者資料）
CREATE OR REPLACE VIEW v_anomalous_access AS
SELECT
    a.id,
    a.created_at,
    a.user_id,
    a.user_name,
    a.patient_id,
    a.target_table,
    a.action,
    a.ip_address,
    a.session_id,
    u.clinic_id  AS user_clinic,
    p.clinic_id  AS patient_clinic,
    CASE WHEN u.clinic_id != p.clinic_id THEN true ELSE false END AS cross_clinic_access
FROM audit_log a
LEFT JOIN user_account u ON u.id = a.user_id
LEFT JOIN patient p ON p.id = a.patient_id
WHERE a.action IN ('VIEW_SENSITIVE', 'EXPORT')
  AND a.patient_id IS NOT NULL
  AND a.created_at > now() - interval '30 days';

-- 4.2 高頻訪問檢測（同一用戶短時間內大量訪問不同患者）
CREATE OR REPLACE VIEW v_high_freq_access AS
SELECT
    user_id,
    user_name,
    date_trunc('hour', created_at) AS hour_window,
    COUNT(DISTINCT patient_id)     AS patient_count,
    COUNT(*)                       AS access_count
FROM audit_log
WHERE action IN ('VIEW_SENSITIVE', 'EXPORT')
  AND created_at > now() - interval '7 days'
GROUP BY user_id, user_name, date_trunc('hour', created_at)
HAVING COUNT(DISTINCT patient_id) > 20  -- 1小時內訪問超過20位患者
ORDER BY access_count DESC;

-- ============================================================================
-- 5. 審計日誌自動清理（合規留存 ≥3 年後自動歸檔）
-- ============================================================================

CREATE OR REPLACE FUNCTION archive_old_audit_logs(
    p_retention_days INT DEFAULT 1095  -- 預設保留 3 年
)
RETURNS TABLE(archived_count BIGINT) AS $$
DECLARE
    v_cutoff_date TIMESTAMPTZ := now() - (p_retention_days || ' days')::INTERVAL;
    v_count       BIGINT;
BEGIN
    -- 此處為架構示範，生產環境建議配合 pg_dump / COPY 導出到 OSS 後再刪除
    -- 安全起見，預設僅統計，不實際刪除（需手動解註 DELETE）
    SELECT COUNT(*) INTO v_count
    FROM audit_log
    WHERE created_at < v_cutoff_date;

    -- DELETE FROM audit_log WHERE created_at < v_cutoff_date; -- 需手動解除註釋

    archived_count := v_count;
    RETURN NEXT;
END;
$$ LANGUAGE plpgsql;

COMMIT;

-- ============================================================================
-- 6. 使用說明
-- ============================================================================
/*
## 應用層需要在每次資料庫連線/交易開始時設定：

```sql
-- Java 端 (Spring Boot + HikariCP 連線攔截器)
SET LOCAL app.clinic_id = '12345';
SET LOCAL app.user_id = '67890';
SET LOCAL app.user_name = '陳大明';
SET LOCAL app.client_ip = '192.168.1.100';
SET LOCAL app.session_id = 'sess_abc123';
SET LOCAL app.user_agent = 'Mozilla/5.0...';
```

## 敏感操作主動審計：

```sql
-- 查看患者敏感資料時
SELECT audit_sensitive_access(12345, 'emr', 67890);

-- 登入時
SELECT audit_login(67890, '陳大明', true);
-- 登入失敗
SELECT audit_login(67890, '陳大明', false, '密碼錯誤（第3次）');

-- 匯出資料時
SELECT audit_export(ARRAY[12345, 67890], 'pdf', '保險理賠用');
```

## 異常偵測查詢：

```sql
-- 查看跨診所訪問
SELECT * FROM v_anomalous_access WHERE cross_clinic_access = true;

-- 查看高頻訪問
SELECT * FROM v_high_freq_access;
```
*/
