-- ============================================================================
-- 祥雲智方中醫診症系統 — P0 修復：處方簽章 DDL 修正
-- 版本：V1.0
-- 說明：將簡化的 sign_hash 替換為完整的 PKCS#7/CMS 電子簽章欄位
-- 依賴：需先執行主初始化腳本（prescription 表已建立）
-- ============================================================================

BEGIN;

-- ============================================================================
-- 1. 移除原方案中不足的欄位（若有）
-- ============================================================================
-- 注意：若 prescription 表已有生產數據，請先備份
-- ALTER TABLE prescription DROP COLUMN IF EXISTS sign_hash;  -- 保留作為快速查詢索引

-- ============================================================================
-- 2. 新增完整電子簽章欄位
-- ============================================================================

-- 簽章狀態與時間
ALTER TABLE prescription ADD COLUMN IF NOT EXISTS sign_status     VARCHAR(16)  DEFAULT 'unsigned';
ALTER TABLE prescription ADD COLUMN IF NOT EXISTS signed_by       BIGINT;
ALTER TABLE prescription ADD COLUMN IF NOT EXISTS signed_at       TIMESTAMPTZ;

-- 原文快照（簽署時的處方不可變快照 + 哈希）
ALTER TABLE prescription ADD COLUMN IF NOT EXISTS content_snapshot JSONB;
ALTER TABLE prescription ADD COLUMN IF NOT EXISTS content_hash    VARCHAR(128);

-- PKCS#7/CMS 完整簽名數據
ALTER TABLE prescription ADD COLUMN IF NOT EXISTS cms_signature   BYTEA;
ALTER TABLE prescription ADD COLUMN IF NOT EXISTS cms_format      VARCHAR(16)  DEFAULT 'PKCS7';
ALTER TABLE prescription ADD COLUMN IF NOT EXISTS sign_algorithm  VARCHAR(32);

-- 簽署者憑證資訊
ALTER TABLE prescription ADD COLUMN IF NOT EXISTS signer_cert_sn     VARCHAR(128);
ALTER TABLE prescription ADD COLUMN IF NOT EXISTS signer_cert_issuer VARCHAR(256);
ALTER TABLE prescription ADD COLUMN IF NOT EXISTS signer_cert_chain  BYTEA;

-- TSA 時間戳
ALTER TABLE prescription ADD COLUMN IF NOT EXISTS tsa_token      BYTEA;
ALTER TABLE prescription ADD COLUMN IF NOT EXISTS tsa_provider   VARCHAR(128);
ALTER TABLE prescription ADD COLUMN IF NOT EXISTS tsa_serial     VARCHAR(64);

-- 簽章驗證
ALTER TABLE prescription ADD COLUMN IF NOT EXISTS last_verified_at TIMESTAMPTZ;
ALTER TABLE prescription ADD COLUMN IF NOT EXISTS verify_status    VARCHAR(16);

COMMENT ON COLUMN prescription.sign_status IS '簽章狀態: unsigned, signed, verified, expired';
COMMENT ON COLUMN prescription.cms_signature IS 'DER 編碼的 PKCS#7/CMS SignedData 完整簽名';
COMMENT ON COLUMN prescription.cms_format IS '簽章格式: PKCS7, CAdES-BES, CAdES-T';
COMMENT ON COLUMN prescription.content_snapshot IS '簽署時的處方原文快照（規範化 JSON，不可變）';
COMMENT ON COLUMN prescription.content_hash IS '原文快照的 SHA-256 哈希值';
COMMENT ON COLUMN prescription.tsa_token IS 'RFC 3161 TimeStampToken (DER 編碼)';
COMMENT ON COLUMN prescription.verify_status IS '驗證狀態: valid, invalid, expired, revoked';

-- ============================================================================
-- 3. 簽章歷史表（法律合規 — 每次重簽/修改保留歷史）
-- ============================================================================

CREATE TABLE IF NOT EXISTS prescription_signature_history (
    id                BIGINT PRIMARY KEY,
    clinic_id         BIGINT          NOT NULL,
    prescription_id   BIGINT          NOT NULL,
    version           INT             NOT NULL DEFAULT 1,
    content_snapshot  JSONB,
    content_hash      VARCHAR(128),
    cms_signature     BYTEA,
    cms_format        VARCHAR(16),
    sign_algorithm    VARCHAR(32),
    signed_by         BIGINT,
    signed_at         TIMESTAMPTZ,
    signer_cert_sn    VARCHAR(128),
    signer_cert_issuer VARCHAR(256),
    signer_cert_chain BYTEA,
    tsa_token         BYTEA,
    tsa_provider      VARCHAR(128),
    reason            VARCHAR(256),                       -- 重新簽署原因
    superseded_by     BIGINT,                              -- 被哪個新版本取代
    created_by        BIGINT,
    created_at        TIMESTAMPTZ     NOT NULL DEFAULT now()
);

ALTER TABLE prescription_signature_history ENABLE ROW LEVEL SECURITY;
CREATE POLICY p_sig_history_isolation ON prescription_signature_history
    USING (clinic_id = NULLIF(current_setting('app.clinic_id', true), '')::bigint);

CREATE INDEX idx_sig_history_prescription ON prescription_signature_history (prescription_id, version);

COMMENT ON TABLE prescription_signature_history IS '處方電子簽章歷史 — 每次重簽或修改均保留歷史版本，滿足香港《電子交易條例》不可否認性要求';

-- ============================================================================
-- 4. 簽章有效性驗證輔助函數
-- ============================================================================

-- 檢查處方簽章是否有效（快速版，僅檢查狀態）
CREATE OR REPLACE FUNCTION is_prescription_signed(p_prescription_id BIGINT)
RETURNS BOOLEAN AS $$
DECLARE
    v_status VARCHAR(16);
BEGIN
    SELECT sign_status INTO v_status
    FROM prescription
    WHERE id = p_prescription_id;

    RETURN v_status IN ('signed', 'verified');
END;
$$ LANGUAGE plpgsql;

-- 取得處方簽章摘要（供前端顯示）
CREATE OR REPLACE FUNCTION get_prescription_signature_summary(p_prescription_id BIGINT)
RETURNS JSONB AS $$
DECLARE
    v_result JSONB;
BEGIN
    SELECT jsonb_build_object(
        'signed',       (sign_status IN ('signed', 'verified')),
        'status',       sign_status,
        'signed_at',    signed_at,
        'signed_by',    (SELECT real_name FROM user_account WHERE id = prescription.signed_by),
        'algorithm',    sign_algorithm,
        'format',       cms_format,
        'verified',     (verify_status = 'valid'),
        'verified_at',  last_verified_at,
        'tsa_provider', tsa_provider
    ) INTO v_result
    FROM prescription
    WHERE id = p_prescription_id;

    RETURN v_result;
END;
$$ LANGUAGE plpgsql;

COMMIT;
