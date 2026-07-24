-- ============================================================================
-- 祥雲智方中醫診症系統 — PostgreSQL 初始化腳本 V1.0
-- 適用：PostgreSQL 15+
-- 包含：核心業務表 + 審計追蹤 + 索引 + RLS + 測試資料
-- ============================================================================

-- ============================================================================
-- 0. 前置設定
-- ============================================================================
BEGIN;

-- 擴展：加密、模糊搜尋、UUID 生成
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- 1. 自定義 ENUM 類型
-- ============================================================================

-- 性別
CREATE TYPE gender AS ENUM ('male', 'female', 'other');

-- 就診類型
CREATE TYPE visit_type AS ENUM ('online', 'offline');

-- 處方狀態機
CREATE TYPE prescription_status AS ENUM (
    'draft',            -- 草稿
    'pending_review',   -- 待審核
    'approved',         -- 已審核
    'paid',             -- 已支付
    'dispensing',       -- 調劑中
    'completed',        -- 已完成
    'voided'            -- 已作廢
);

-- 煎藥狀態機
CREATE TYPE decoction_status AS ENUM (
    'pending',          -- 待接單
    'accepted',         -- 已接單
    'processing',       -- 煎煮中
    'packaged',         -- 已包裝
    'ready',            -- 待出庫
    'handed_over',      -- 已交物流
    'cancelled'         -- 已取消
);

-- 物流狀態機
CREATE TYPE logistics_status AS ENUM (
    'created',          -- 已建立
    'picked_up',        -- 已攬件
    'in_transit',       -- 運輸中
    'delivering',       -- 派送中
    'signed',           -- 已簽收
    'exception'         -- 異常
);

-- 支付狀態
CREATE TYPE payment_status AS ENUM (
    'pending',          -- 待支付
    'paid',             -- 已支付
    'refunding',        -- 退費中
    'refunded',         -- 已退費
    'failed'            -- 支付失敗
);

-- 審計操作類型
CREATE TYPE audit_action AS ENUM (
    'INSERT', 'UPDATE', 'DELETE', 'LOGIN', 'LOGOUT', 'EXPORT', 'VIEW_SENSITIVE'
);

-- ============================================================================
-- 2. 核心業務表（附完整審計欄位）
-- ============================================================================

-- 2.1 診所（租戶主表）
CREATE TABLE clinic (
    id            BIGINT PRIMARY KEY,
    name          VARCHAR(128)    NOT NULL,
    code          VARCHAR(32)     NOT NULL UNIQUE,
    address       TEXT,
    phone         VARCHAR(20),
    license_no    VARCHAR(64),                -- 執業許可證號
    status        SMALLINT        NOT NULL DEFAULT 1,  -- 1=啟用 0=停用
    created_by    BIGINT,
    created_at    TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_by    BIGINT,
    updated_at    TIMESTAMPTZ     NOT NULL DEFAULT now()
);

-- 2.2 用戶帳號（跨診所統一 SSO）
CREATE TABLE user_account (
    id            BIGINT PRIMARY KEY,
    username      VARCHAR(64)     NOT NULL UNIQUE,
    password_hash VARCHAR(256)    NOT NULL,             -- bcrypt
    real_name     VARCHAR(64)     NOT NULL,
    phone         VARCHAR(20),
    email         VARCHAR(128),
    role          VARCHAR(32)     NOT NULL DEFAULT 'patient',  -- patient, doctor, nurse, pharmacist, admin, super_admin
    clinic_id     BIGINT          REFERENCES clinic(id),
    status        SMALLINT        NOT NULL DEFAULT 1,  -- 1=啟用 0=停用 2=待驗證
    last_login_at TIMESTAMPTZ,
    created_by    BIGINT,
    created_at    TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_by    BIGINT,
    updated_at    TIMESTAMPTZ     NOT NULL DEFAULT now()
);

COMMENT ON TABLE user_account IS '統一帳號，支援跨診所 SSO 單點登入';

-- 2.3 患者（RLS 示範）
CREATE TABLE patient (
    id            BIGINT PRIMARY KEY,
    clinic_id     BIGINT          NOT NULL REFERENCES clinic(id),
    user_id       BIGINT          REFERENCES user_account(id),  -- 關聯線上帳號
    name          VARCHAR(64)     NOT NULL,
    gender        gender,
    phone         VARCHAR(20)     NOT NULL,
    phone_enc     BYTEA,                                  -- 加密手機號
    id_card_hash  VARCHAR(128),                           -- 身分證 SHA-256（比對用，不可逆）
    id_card_enc   BYTEA,                                  -- 加密身分證號
    birth_date    DATE,
    blood_type    VARCHAR(4),
    allergy_info  JSONB           DEFAULT '[]',           -- 過敏史 [{drug, reaction, severity}]
    address       TEXT,
    emergency_contact JSONB       DEFAULT '{}',            -- {name, phone, relation}
    tags          TEXT[]          DEFAULT '{}',            -- 標籤: 慢性病, 高血壓, etc.
    created_by    BIGINT,
    created_at    TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_by    BIGINT,
    updated_at    TIMESTAMPTZ     NOT NULL DEFAULT now()
);

ALTER TABLE patient ENABLE ROW LEVEL SECURITY;
CREATE POLICY p_patient_isolation ON patient
    USING (clinic_id = NULLIF(current_setting('app.clinic_id', true), '')::bigint);

CREATE INDEX idx_patient_phone ON patient (phone);
CREATE INDEX idx_patient_name_trgm ON patient USING GIN (name gin_trgm_ops);

-- 2.4 家庭成員（患者可綁定家庭成員帳號）
CREATE TABLE family_member (
    id            BIGINT PRIMARY KEY,
    patient_id    BIGINT          NOT NULL REFERENCES patient(id),
    member_id     BIGINT          NOT NULL REFERENCES patient(id),
    relation      VARCHAR(16)     NOT NULL,              -- self, spouse, child, parent, etc.
    created_by    BIGINT,
    created_at    TIMESTAMPTZ     NOT NULL DEFAULT now(),
    UNIQUE (patient_id, member_id)
);

-- 2.5 醫生
CREATE TABLE doctor (
    id            BIGINT PRIMARY KEY,
    clinic_id     BIGINT          NOT NULL REFERENCES clinic(id),
    user_id       BIGINT          NOT NULL REFERENCES user_account(id),
    title         VARCHAR(64),                           -- 職稱
    specialty     TEXT[],                                -- 專長
    department    VARCHAR(64),
    license_no    VARCHAR(64),                           -- 醫師執照號
    signature_img TEXT,                                  -- 電子簽章圖片 URL
    certificate   JSONB          DEFAULT '{}',           -- 證書資訊
    consultation_fee NUMERIC(10,2) DEFAULT 0,
    status        SMALLINT       NOT NULL DEFAULT 1,
    created_by    BIGINT,
    created_at    TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_by    BIGINT,
    updated_at    TIMESTAMPTZ    NOT NULL DEFAULT now()
);

-- 2.6 醫生排班
CREATE TABLE doctor_schedule (
    id            BIGINT PRIMARY KEY,
    clinic_id     BIGINT          NOT NULL REFERENCES clinic(id),
    doctor_id     BIGINT          NOT NULL REFERENCES doctor(id),
    schedule_date DATE            NOT NULL,
    time_slot     SMALLINT        NOT NULL,              -- 1=上午 2=下午 3=晚上
    max_patients  INT             NOT NULL DEFAULT 20,
    booked_count  INT             NOT NULL DEFAULT 0,
    status        SMALLINT        NOT NULL DEFAULT 1,
    created_by    BIGINT,
    created_at    TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_by    BIGINT,
    updated_at    TIMESTAMPTZ     NOT NULL DEFAULT now(),
    UNIQUE (doctor_id, schedule_date, time_slot),
    CONSTRAINT chk_booked_le_max CHECK (booked_count <= max_patients)
);

-- ============================================================================
-- 3. 電子病歷（EMR）
-- ============================================================================

CREATE TABLE emr (
    id              BIGINT PRIMARY KEY,
    clinic_id       BIGINT          NOT NULL,
    patient_id      BIGINT          NOT NULL REFERENCES patient(id),
    doctor_id       BIGINT          NOT NULL REFERENCES doctor(id),
    visit_type      visit_type      NOT NULL,                    -- online / offline
    chief_complaint TEXT,                                       -- 主訴
    present_illness TEXT,                                       -- 現病史
    past_history    TEXT,                                       -- 既往史
    -- 中醫望聞問切（JSONB 承載）
    detail          JSONB           NOT NULL DEFAULT '{}',
    /*
    detail JSONB 結構範例：
    {
      "inspection": { "tongue": "舌淡紅，苔薄白", "face": "面色紅潤", "spirit": "神清" },
      "auscultation": { "voice": "語聲清晰", "cough": "無", "breath": "平穩" },
      "inquiry": { "appetite": "納可", "sleep": "眠安", "stool": "便調", "urine": "小便可" },
      "palpation": { "pulse_left": "弦", "pulse_right": "滑", "pulse_detail": "左弦右滑" },
      "tcm_pattern": "肝鬱脾虛",
      "treatment_principle": "疏肝健脾",
      "temperature": 36.5,
      "blood_pressure": "120/80",
      "heart_rate": 72
    }
    */
    diagnosis       TEXT[],                                   -- 診斷（中西醫）
    advice          TEXT,                                     -- 醫囑
    follow_up_date  DATE,                                     -- 複診日期
    created_by      BIGINT,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_by      BIGINT,
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now()
);

CREATE INDEX idx_emr_detail_gin ON emr USING GIN (detail);
CREATE INDEX idx_emr_patient ON emr (patient_id, created_at DESC);
CREATE INDEX idx_emr_doctor ON emr (doctor_id, created_at DESC);

ALTER TABLE emr ENABLE ROW LEVEL SECURITY;
CREATE POLICY p_emr_isolation ON emr
    USING (clinic_id = NULLIF(current_setting('app.clinic_id', true), '')::bigint);

-- ============================================================================
-- 4. 藥材字典與庫存
-- ============================================================================

-- 4.1 藥材字典
CREATE TABLE material_dict (
    id            BIGINT PRIMARY KEY,
    name          VARCHAR(128)    NOT NULL,                  -- 藥材名稱
    pinyin        VARCHAR(256),                              -- 拼音
    latin_name    VARCHAR(256),                              -- 拉丁學名
    category      VARCHAR(32),                               -- 分類: 解表藥, 清熱藥, etc.
    properties    JSONB          DEFAULT '{}',
    /*
    properties JSONB 結構：
    {
      "nature": "寒/熱/溫/涼/平",
      "flavor": ["辛","甘","酸","苦","鹹"],
      "meridian": ["肺經","脾經"],
      "toxicity": "有小毒",
      "dosage_min": 3, "dosage_max": 9, "dosage_unit": "g",
      "contraindications": ["孕婦禁用"],
      "processing_methods": ["炒", "炙"]
    }
    */
    unit          VARCHAR(16)     NOT NULL DEFAULT 'g',     -- 單位: g, 枚, 條, etc.
    status        SMALLINT        NOT NULL DEFAULT 1,
    created_by    BIGINT,
    created_at    TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_by    BIGINT,
    updated_at    TIMESTAMPTZ     NOT NULL DEFAULT now()
);

CREATE INDEX idx_material_name_trgm ON material_dict USING GIN (name gin_trgm_ops);
CREATE INDEX idx_material_pinyin ON material_dict (pinyin);

-- 4.2 庫存（CHECK 保證不超賣）
CREATE TABLE inventory (
    id            BIGINT PRIMARY KEY,
    clinic_id     BIGINT          NOT NULL,
    material_id   BIGINT          NOT NULL REFERENCES material_dict(id),
    batch_no      VARCHAR(64)     NOT NULL,
    supplier      VARCHAR(128),
    purchase_date DATE,
    expire_date   DATE            NOT NULL,
    total_qty     NUMERIC(14,3)   NOT NULL DEFAULT 0,       -- 總數量
    locked_qty    NUMERIC(14,3)   NOT NULL DEFAULT 0,       -- 鎖定量（已開處方未調劑）
    available_qty NUMERIC(14,3)   GENERATED ALWAYS AS       -- 可用量（計算欄位）
                    (total_qty - locked_qty) STORED,
    unit_cost     NUMERIC(12,4),                            -- 成本單價
    unit_price    NUMERIC(12,4),                            -- 售價
    location      VARCHAR(64),                              -- 庫位
    notes         TEXT,
    created_by    BIGINT,
    created_at    TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_by    BIGINT,
    updated_at    TIMESTAMPTZ     NOT NULL DEFAULT now(),
    CONSTRAINT chk_qty_nonneg CHECK (total_qty >= 0 AND locked_qty >= 0),
    CONSTRAINT chk_locked_le_total CHECK (locked_qty <= total_qty),
    CONSTRAINT uq_inv_clinic_material_batch UNIQUE (clinic_id, material_id, batch_no)
);

CREATE INDEX idx_inventory_expire ON inventory (expire_date)
    WHERE total_qty - locked_qty > 0;                       -- 僅索引有庫存的效期

ALTER TABLE inventory ENABLE ROW LEVEL SECURITY;
CREATE POLICY p_inventory_isolation ON inventory
    USING (clinic_id = NULLIF(current_setting('app.clinic_id', true), '')::bigint);

-- 4.3 庫存異動記錄
CREATE TABLE inventory_transaction (
    id            BIGINT PRIMARY KEY,
    clinic_id     BIGINT          NOT NULL,
    inventory_id  BIGINT          NOT NULL REFERENCES inventory(id),
    type          VARCHAR(16)     NOT NULL,                 -- purchase, lock, unlock, dispense, return, scrap
    qty           NUMERIC(14,3)   NOT NULL,
    balance_after NUMERIC(14,3)   NOT NULL,
    reference_type VARCHAR(32),                             -- 關聯類型: prescription, purchase_order
    reference_id  BIGINT,                                   -- 關聯 ID
    notes         TEXT,
    created_by    BIGINT,
    created_at    TIMESTAMPTZ     NOT NULL DEFAULT now()
);

-- 4.4 採購單
CREATE TABLE purchase_order (
    id            BIGINT PRIMARY KEY,
    clinic_id     BIGINT          NOT NULL,
    order_no      VARCHAR(32)     NOT NULL UNIQUE,
    supplier_id   BIGINT,
    status        VARCHAR(16)     NOT NULL DEFAULT 'draft', -- draft, submitted, received, cancelled
    items         JSONB           NOT NULL DEFAULT '[]',
    /*
    items JSONB 結構：
    [{"material_id": 1, "qty": 500, "unit_cost": 0.35, "batch_no": "B20260701", "expire_date": "2028-06-30"}]
    */
    total_amount  NUMERIC(12,2)   NOT NULL DEFAULT 0,
    notes         TEXT,
    created_by    BIGINT,
    created_at    TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_by    BIGINT,
    updated_at    TIMESTAMPTZ     NOT NULL DEFAULT now()
);

ALTER TABLE purchase_order ENABLE ROW LEVEL SECURITY;
CREATE POLICY p_po_isolation ON purchase_order
    USING (clinic_id = NULLIF(current_setting('app.clinic_id', true), '')::bigint);

-- ============================================================================
-- 5. 電子處方
-- ============================================================================

-- 處方主表（分區表 — 按月分區）
CREATE TABLE prescription (
    id              BIGINT          NOT NULL,
    clinic_id       BIGINT          NOT NULL,
    prescription_no VARCHAR(32)     NOT NULL,               -- 處方編號 (業務唯一)
    patient_id      BIGINT          NOT NULL,
    doctor_id       BIGINT          NOT NULL,
    emr_id          BIGINT          REFERENCES emr(id),
    visit_type      visit_type      NOT NULL,
    status          prescription_status NOT NULL DEFAULT 'draft',
    dose_count      INT             NOT NULL CHECK (dose_count > 0),  -- 劑數
    dose_days       INT             NOT NULL DEFAULT 1,    -- 服用天數
    items           JSONB           NOT NULL DEFAULT '[]',
    /*
    items JSONB 結構：
    [{
      "material_id": 1, "material_name": "黨參",
      "dosage": 15, "unit": "g",
      "processing": "炒",           -- 炮製方法
      "decoction_note": "先煎",     -- 煎煮備註: 先煎/後下/烊化/包煎/冲服/無
      "unit_price": 0.45,
      "subtotal": 6.75
    }]
    */
    decoction_method VARCHAR(32),                           -- 煎藥方式: self, center
    delivery_option  VARCHAR(16),                           -- 配送方式: pickup, delivery
    total_amount    NUMERIC(12,2)   NOT NULL DEFAULT 0 CHECK (total_amount >= 0),
    decoction_fee   NUMERIC(10,2)   DEFAULT 0,              -- 代煎費
    delivery_fee    NUMERIC(10,2)   DEFAULT 0,              -- 配送費
    diagnosis_code  VARCHAR(32),                            -- ICD-10 或 TCM 編碼
    sign_data       BYTEA,                                  -- PKCS#7/CMS 完整簽名數據
    sign_hash       VARCHAR(128),                           -- 簽名哈希（快速校驗）
    sign_time       TIMESTAMPTZ,                            -- 簽名時間戳
    created_by      BIGINT,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_by      BIGINT,
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    PRIMARY KEY (id, created_at)
) PARTITION BY RANGE (created_at);

-- 建立初始分區（2026年7-12月）
CREATE TABLE prescription_2026_07 PARTITION OF prescription
    FOR VALUES FROM ('2026-07-01') TO ('2026-08-01');
CREATE TABLE prescription_2026_08 PARTITION OF prescription
    FOR VALUES FROM ('2026-08-01') TO ('2026-09-01');
CREATE TABLE prescription_2026_09 PARTITION OF prescription
    FOR VALUES FROM ('2026-09-01') TO ('2026-10-01');
CREATE TABLE prescription_2026_10 PARTITION OF prescription
    FOR VALUES FROM ('2026-10-01') TO ('2026-11-01');
CREATE TABLE prescription_2026_11 PARTITION OF prescription
    FOR VALUES FROM ('2026-11-01') TO ('2026-12-01');
CREATE TABLE prescription_2026_12 PARTITION OF prescription
    FOR VALUES FROM ('2026-12-01') TO ('2027-01-01');

-- 預留後續分區創建函數（由定時任務調用）
CREATE OR REPLACE FUNCTION create_monthly_prescription_partition()
RETURNS void AS $$
DECLARE
    next_month date := date_trunc('month', now()) + interval '1 month';
    next_next  date := next_month + interval '1 month';
    tbl_name   text;
BEGIN
    tbl_name := 'prescription_' || to_char(next_month, 'YYYY_MM');
    EXECUTE format(
        'CREATE TABLE IF NOT EXISTS %I PARTITION OF prescription FOR VALUES FROM (%L) TO (%L)',
        tbl_name, next_month, next_next
    );
END;
$$ LANGUAGE plpgsql;

ALTER TABLE prescription ENABLE ROW LEVEL SECURITY;
CREATE POLICY p_prescription_isolation ON prescription
    USING (clinic_id = NULLIF(current_setting('app.clinic_id', true), '')::bigint);

CREATE INDEX idx_prescription_patient ON prescription (patient_id, created_at DESC);
CREATE INDEX idx_prescription_doctor ON prescription (doctor_id, created_at DESC);
CREATE INDEX idx_prescription_status ON prescription (status, created_at);
CREATE INDEX idx_prescription_no ON prescription (prescription_no);
CREATE INDEX idx_prescription_items_gin ON prescription USING GIN (items);

-- 處方審核記錄
CREATE TABLE prescription_review (
    id              BIGINT PRIMARY KEY,
    clinic_id       BIGINT          NOT NULL,
    prescription_id BIGINT          NOT NULL,
    reviewer_id     BIGINT          NOT NULL,
    action          VARCHAR(16)     NOT NULL,               -- approve, reject, request_change
    comment         TEXT,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now()
);

-- 配伍禁忌規則表（十八反十九畏）
CREATE TABLE incompatibility_rule (
    id            BIGINT PRIMARY KEY,
    material_a    BIGINT          NOT NULL REFERENCES material_dict(id),
    material_b    BIGINT          NOT NULL REFERENCES material_dict(id),
    rule_type     VARCHAR(16)     NOT NULL,                 -- eighteen_antagonism, nineteen_fear
    description   TEXT,
    severity      VARCHAR(8)      NOT NULL DEFAULT 'warn',  -- warn, block
    created_at    TIMESTAMPTZ     NOT NULL DEFAULT now(),
    UNIQUE (material_a, material_b)
);

-- ============================================================================
-- 6. 支付與財務
-- ============================================================================

-- 6.1 支付記錄
CREATE TABLE payment (
    id              BIGINT PRIMARY KEY,
    clinic_id       BIGINT          NOT NULL,
    prescription_id BIGINT          NOT NULL,
    payment_no      VARCHAR(32)     NOT NULL UNIQUE,
    amount          NUMERIC(12,2)   NOT NULL CHECK (amount > 0),
    method          VARCHAR(16)     NOT NULL,               -- cash, octopus, alipay, wechat, credit_card, insurance
    status          payment_status  NOT NULL DEFAULT 'pending',
    transaction_id  VARCHAR(128),                           -- 第三方支付交易號
    paid_at         TIMESTAMPTZ,
    refund_amount   NUMERIC(12,2)   DEFAULT 0,
    refund_at       TIMESTAMPTZ,
    notes           TEXT,
    created_by      BIGINT,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_by      BIGINT,
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now()
);

ALTER TABLE payment ENABLE ROW LEVEL SECURITY;
CREATE POLICY p_payment_isolation ON payment
    USING (clinic_id = NULLIF(current_setting('app.clinic_id', true), '')::bigint);

-- 6.2 退款記錄
CREATE TABLE refund (
    id              BIGINT PRIMARY KEY,
    clinic_id       BIGINT          NOT NULL,
    payment_id      BIGINT          NOT NULL REFERENCES payment(id),
    refund_no       VARCHAR(32)     NOT NULL UNIQUE,
    amount          NUMERIC(12,2)   NOT NULL CHECK (amount > 0),
    reason          TEXT,
    status          VARCHAR(16)     NOT NULL DEFAULT 'pending',
    processed_at    TIMESTAMPTZ,
    created_by      BIGINT,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now()
);

-- 6.3 每日結帳
CREATE TABLE daily_settlement (
    id            BIGINT PRIMARY KEY,
    clinic_id     BIGINT          NOT NULL,
    settlement_date DATE          NOT NULL,
    total_revenue NUMERIC(12,2)   NOT NULL DEFAULT 0,
    cash_amount   NUMERIC(12,2)   DEFAULT 0,
    card_amount   NUMERIC(12,2)   DEFAULT 0,
    online_amount NUMERIC(12,2)   DEFAULT 0,
    insurance_amount NUMERIC(12,2) DEFAULT 0,
    refund_amount NUMERIC(12,2)   DEFAULT 0,
    prescription_count INT        DEFAULT 0,
    reconciled    BOOLEAN         NOT NULL DEFAULT false,
    reconciled_by BIGINT,
    reconciled_at TIMESTAMPTZ,
    created_by    BIGINT,
    created_at    TIMESTAMPTZ     NOT NULL DEFAULT now(),
    UNIQUE (clinic_id, settlement_date)
);

-- ============================================================================
-- 7. 煎藥中心對接
-- ============================================================================

CREATE TABLE decoction_order (
    id              BIGINT PRIMARY KEY,
    clinic_id       BIGINT          NOT NULL,
    prescription_id BIGINT          NOT NULL,
    external_center_id VARCHAR(64),                      -- 煎藥中心 ID
    status          decoction_status NOT NULL DEFAULT 'pending',
    craft           JSONB,
    /*
    craft JSONB 結構（煎煮工藝指示）：
    {
      "method": "decoction",        -- decoction / concentrate / powder
      "water_ratio": 10,            -- 加水量（倍）
      "decoct_times": 2,            -- 煎煮次數
      "first_decoct_min": 30,       -- 頭煎時間（分鐘）
      "second_decoct_min": 20,      -- 二煎時間
      "packaging": "vacuum",        -- 包裝: vacuum / plastic_bag / glass_bottle
      "pack_size_ml": 200,          -- 每包容量
      "special_instructions": "先煎石膏30分鐘，後下薄荷",
      "items_special": [            -- 特殊煎煮藥材
        {"material_name": "石膏", "method": "先煎", "duration_min": 30},
        {"material_name": "薄荷", "method": "後下", "duration_min": 5}
      ]
    }
    */
    dose_count      INT             NOT NULL CHECK (dose_count > 0),
    fee             NUMERIC(10,2)   NOT NULL DEFAULT 0,
    vacuum_pkg_no   VARCHAR(64),                        -- 真空包裝編號（煎藥中心回傳）
    external_no     VARCHAR(64),                        -- 煎藥中心訂單號
    receiver        JSONB,
    /*
    receiver JSONB 結構：
    {
      "name": "張三", "phone": "91234567",
      "address": "香港中環XXX大廈12樓A室",
      "note": "放管理處"
    }
    */
    status_history  JSONB           DEFAULT '[]',       -- 狀態變更歷史
    completed_at    TIMESTAMPTZ,
    created_by      BIGINT,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_by      BIGINT,
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now()
);

-- 部分唯一索引：同一處方不能有進行中的煎藥單
CREATE UNIQUE INDEX uq_active_decoction ON decoction_order (prescription_id)
    WHERE status IN ('pending', 'accepted', 'processing', 'packaged', 'ready');

ALTER TABLE decoction_order ENABLE ROW LEVEL SECURITY;
CREATE POLICY p_decoction_isolation ON decoction_order
    USING (clinic_id = NULLIF(current_setting('app.clinic_id', true), '')::bigint);

-- 煎藥中心回調記錄
CREATE TABLE decoction_callback_log (
    id              BIGINT PRIMARY KEY,
    clinic_id       BIGINT          NOT NULL,
    decoction_id    BIGINT          NOT NULL,
    external_no     VARCHAR(64),
    callback_status VARCHAR(32)     NOT NULL,
    raw_body        JSONB,
    verified        BOOLEAN         NOT NULL DEFAULT false,   -- HMAC 簽名驗證結果
    idempotent_key  VARCHAR(128)    NOT NULL,                  -- (order_id + status) 哈希
    processed_at    TIMESTAMPTZ     NOT NULL DEFAULT now(),
    UNIQUE (idempotent_key)
);

-- ============================================================================
-- 8. 物流對接
-- ============================================================================

CREATE TABLE logistics_order (
    id              BIGINT PRIMARY KEY,
    clinic_id       BIGINT          NOT NULL,
    decoction_id    BIGINT          NOT NULL REFERENCES decoction_order(id),
    carrier         VARCHAR(32),                            -- 快遞公司: sf, yto, zto, etc.
    carrier_name    VARCHAR(64),
    waybill_no      VARCHAR(64)     UNIQUE,
    electronic_sheet TEXT,                                  -- 電子面單 URL
    status          logistics_status NOT NULL DEFAULT 'created',
    receiver        JSONB           NOT NULL,
    latest_trace    JSONB,
    /*
    latest_trace JSONB 結構：
    {
      "status": "派送中",
      "location": "香港九龍XXX",
      "time": "2026-07-20T14:30:00+08:00",
      "description": "快遞員張三正在派送，聯繫電話：98765432"
    }
    */
    trace_history   JSONB           DEFAULT '[]',           -- 完整軌跡
    estimated_delivery TIMESTAMPTZ,
    signed_at       TIMESTAMPTZ,
    signed_by       VARCHAR(64),
    exception_reason TEXT,
    created_by      BIGINT,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_by      BIGINT,
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now()
);

ALTER TABLE logistics_order ENABLE ROW LEVEL SECURITY;
CREATE POLICY p_logistics_isolation ON logistics_order
    USING (clinic_id = NULLIF(current_setting('app.clinic_id', true), '')::bigint);

-- 物流軌跡回調記錄
CREATE TABLE logistics_trace_log (
    id              BIGINT PRIMARY KEY,
    clinic_id       BIGINT          NOT NULL,
    logistics_id    BIGINT          NOT NULL,
    waybill_no      VARCHAR(64)     NOT NULL,
    trace_data      JSONB           NOT NULL,
    idempotent_key  VARCHAR(128)    NOT NULL,
    processed_at    TIMESTAMPTZ     NOT NULL DEFAULT now(),
    UNIQUE (idempotent_key)
);

-- ============================================================================
-- 9. 逆向物流（退藥回收）
-- ============================================================================

CREATE TABLE return_order (
    id              BIGINT PRIMARY KEY,
    clinic_id       BIGINT          NOT NULL,
    original_decoction_id BIGINT    NOT NULL REFERENCES decoction_order(id),
    reason          TEXT            NOT NULL,
    reason_code     VARCHAR(32),                            -- damaged, wrong_item, quality_issue, patient_refuse
    carrier         VARCHAR(32),
    waybill_no      VARCHAR(64)     UNIQUE,
    status          VARCHAR(16)     NOT NULL DEFAULT 'pending', -- pending, picked_up, in_transit, received, processed
    return_fee      NUMERIC(10,2)   DEFAULT 0,
    notes           TEXT,
    created_by      BIGINT,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_by      BIGINT,
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now()
);

-- ============================================================================
-- 10. 通知記錄
-- ============================================================================

CREATE TABLE notification (
    id              BIGINT PRIMARY KEY,
    clinic_id       BIGINT          NOT NULL,
    user_id         BIGINT          NOT NULL,
    type            VARCHAR(16)     NOT NULL,               -- push, sms, email, in_app
    template_code   VARCHAR(32),
    title           VARCHAR(256),
    content         TEXT,
    data            JSONB           DEFAULT '{}',
    status          VARCHAR(16)     NOT NULL DEFAULT 'pending', -- pending, sent, delivered, read, failed
    read_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now()
);

CREATE INDEX idx_notification_user ON notification (user_id, created_at DESC);

-- ============================================================================
-- 11. 審計日誌
-- ============================================================================

CREATE TABLE audit_log (
    id              BIGINT NOT NULL,
    clinic_id       BIGINT          NOT NULL,
    user_id         BIGINT,
    user_name       VARCHAR(64),
    action          audit_action    NOT NULL,
    target_table    VARCHAR(64),
    target_id       BIGINT,
    patient_id      BIGINT,
    old_data        JSONB,
    new_data        JSONB,
    changed_fields  TEXT[],
    ip_address      INET,
    user_agent      TEXT,
    session_id      VARCHAR(128),
    notes           TEXT,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    PRIMARY KEY (id, created_at)
) PARTITION BY RANGE (created_at);

-- 審計日誌按月分區
CREATE TABLE audit_log_2026_07 PARTITION OF audit_log
    FOR VALUES FROM ('2026-07-01') TO ('2026-08-01');
CREATE TABLE audit_log_2026_08 PARTITION OF audit_log
    FOR VALUES FROM ('2026-08-01') TO ('2026-09-01');
CREATE TABLE audit_log_2026_09 PARTITION OF audit_log
    FOR VALUES FROM ('2026-09-01') TO ('2026-10-01');
CREATE TABLE audit_log_2026_10 PARTITION OF audit_log
    FOR VALUES FROM ('2026-10-01') TO ('2026-11-01');
CREATE TABLE audit_log_2026_11 PARTITION OF audit_log
    FOR VALUES FROM ('2026-11-01') TO ('2026-12-01');
CREATE TABLE audit_log_2026_12 PARTITION OF audit_log
    FOR VALUES FROM ('2026-12-01') TO ('2027-01-01');

CREATE INDEX idx_audit_log_target ON audit_log (target_table, target_id);
CREATE INDEX idx_audit_log_user ON audit_log (user_id, created_at DESC);
CREATE INDEX idx_audit_log_patient ON audit_log (patient_id, created_at DESC);

-- ============================================================================
-- 12. 本地訊息表（Transactional Outbox 模式）
-- ============================================================================

CREATE TABLE outbox_message (
    id              BIGSERIAL PRIMARY KEY,
    aggregate_id    VARCHAR(64)     NOT NULL,
    aggregate_type  VARCHAR(64)     NOT NULL,
    event_type      VARCHAR(128)    NOT NULL,
    payload         JSONB           NOT NULL,
    status          VARCHAR(16)     NOT NULL DEFAULT 'pending',  -- pending, published, failed
    retry_count     INT             NOT NULL DEFAULT 0,
    max_retries     INT             NOT NULL DEFAULT 10,
    next_retry_at   TIMESTAMPTZ     NOT NULL DEFAULT now(),
    published_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now()
);

CREATE INDEX idx_outbox_status ON outbox_message (status, next_retry_at)
    WHERE status IN ('pending', 'failed');

-- ============================================================================
-- 13. 物化視圖 — 財務報表
-- ============================================================================

-- 每日收入匯總
CREATE MATERIALIZED VIEW mv_daily_revenue AS
SELECT
    clinic_id,
    date_trunc('day', created_at)::date AS day,
    visit_type,
    COUNT(*)                         AS prescription_count,
    SUM(total_amount)                AS revenue,
    SUM(decoction_fee)               AS decoction_revenue,
    SUM(delivery_fee)                AS delivery_revenue,
    SUM(total_amount + COALESCE(decoction_fee, 0) + COALESCE(delivery_fee, 0)) AS total_revenue
FROM prescription
WHERE status IN ('paid', 'completed')
GROUP BY clinic_id, date_trunc('day', created_at)::date, visit_type;

CREATE UNIQUE INDEX idx_mv_daily_revenue ON mv_daily_revenue (clinic_id, day, visit_type);

-- 醫生績效看板
CREATE MATERIALIZED VIEW mv_doctor_performance AS
SELECT
    p.clinic_id,
    p.doctor_id,
    d.real_name                        AS doctor_name,
    date_trunc('month', p.created_at)::date AS month,
    COUNT(*)                           AS prescription_count,
    SUM(p.total_amount)                AS total_revenue,
    COUNT(DISTINCT p.patient_id)       AS patient_count
FROM prescription p
JOIN user_account d ON d.id = p.doctor_id
WHERE p.status IN ('paid', 'completed')
GROUP BY p.clinic_id, p.doctor_id, d.real_name, date_trunc('month', p.created_at)::date;

CREATE UNIQUE INDEX idx_mv_doctor_perf ON mv_doctor_performance (clinic_id, doctor_id, month);

-- ============================================================================
-- 14. 輔助函數
-- ============================================================================

-- 14.1 雪花 ID 生成函數（分散式唯一 ID）
CREATE SEQUENCE IF NOT EXISTS global_id_seq;
CREATE OR REPLACE FUNCTION next_id()
RETURNS BIGINT AS $$
DECLARE
    epoch_ms BIGINT;
    seq_val  BIGINT;
    worker_id BIGINT := 1; -- 可依節點實例設定不同 worker_id
BEGIN
    epoch_ms := (EXTRACT(EPOCH FROM clock_timestamp()) * 1000)::BIGINT - 1700000000000;
    seq_val := nextval('global_id_seq') % 4096;
    RETURN (epoch_ms << 22) | (worker_id << 12) | seq_val;
END;
$$ LANGUAGE plpgsql;

-- 14.2 刷新物化視圖
CREATE OR REPLACE FUNCTION refresh_materialized_views()
RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY mv_daily_revenue;
    REFRESH MATERIALIZED VIEW CONCURRENTLY mv_doctor_performance;
END;
$$ LANGUAGE plpgsql;

-- 14.3 設定租戶上下文（應用層在每次交易前調用）
CREATE OR REPLACE FUNCTION set_clinic_context(p_clinic_id BIGINT)
RETURNS void AS $$
BEGIN
    PERFORM set_config('app.clinic_id', p_clinic_id::text, true);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ============================================================================
-- 15. 測試資料
-- ============================================================================

-- 15.1 診所
INSERT INTO clinic (id, name, code, address, phone, license_no)
VALUES
    (next_id(), '祥雲堂中醫診所（中環）', 'XC-CENTRAL', '香港中環德輔道中XX號XX大廈1樓', '25231234', 'CM-2024-001'),
    (next_id(), '祥雲堂中醫診所（旺角）', 'XC-MONGKOK', '香港九龍旺角彌敦道XX號XX中心2樓', '23981234', 'CM-2024-002');

-- 記錄 clinic_id 供後續使用
DO $$
DECLARE
    v_clinic1 BIGINT := (SELECT id FROM clinic WHERE code = 'XC-CENTRAL');
    v_clinic2 BIGINT := (SELECT id FROM clinic WHERE code = 'XC-MONGKOK');
BEGIN
    -- 15.2 用戶帳號 (密碼均為 bcrypt 哈希的 "password123")
    INSERT INTO user_account (id, username, password_hash, real_name, phone, role, clinic_id)
    VALUES
        (next_id(), 'admin',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '系統管理員', '90001111', 'super_admin', NULL),
        (next_id(), 'dr_chan', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '陳大明',     '91234567', 'doctor',      v_clinic1),
        (next_id(), 'dr_lee',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '李麗華',     '92345678', 'doctor',      v_clinic1),
        (next_id(), 'nurse_wong', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '王美玲', '93456789', 'nurse',       v_clinic1),
        (next_id(), 'pharma_ng', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '吳志強',  '94567890', 'pharmacist',  v_clinic1);

    -- 15.3 患者
    INSERT INTO patient (id, clinic_id, name, gender, phone, birth_date, allergy_info)
    VALUES
        (next_id(), v_clinic1, '張志明', 'male',   '98765432', '1985-03-15', '[{"drug":"青黴素","reaction":"皮疹","severity":"中度"}]'),
        (next_id(), v_clinic1, '陳美玲', 'female', '97654321', '1990-07-22', '[]'),
        (next_id(), v_clinic1, '黃家強', 'male',   '96543210', '1978-11-08', '[{"drug":"磺胺類","reaction":"過敏性休克","severity":"重度"}]'),
        (next_id(), v_clinic1, '李淑貞', 'female', '95432109', '2000-01-30', '[]'),
        (next_id(), v_clinic1, '何志華', 'male',   '94321098', '1965-05-18', '[]');

    -- 15.4 醫生
    INSERT INTO doctor (id, clinic_id, user_id, title, specialty, department, consultation_fee)
    SELECT
        next_id(), v_clinic1, id,
        CASE WHEN real_name = '陳大明' THEN '主任中醫師' ELSE '高級中醫師' END,
        CASE WHEN real_name = '陳大明' THEN ARRAY['內科','脾胃病','失眠','慢性疲勞']
             ELSE ARRAY['婦科','皮膚科','針灸','痛症'] END,
        CASE WHEN real_name = '陳大明' THEN '內科' ELSE '婦科' END,
        CASE WHEN real_name = '陳大明' THEN 350 ELSE 300 END
    FROM user_account WHERE role = 'doctor' AND clinic_id = v_clinic1;

    -- 記錄 doctor IDs
    PERFORM set_config('app.clinic_id', v_clinic1::text, true);
END $$;

-- 15.5 藥材字典
INSERT INTO material_dict (id, name, pinyin, category, properties, unit)
VALUES
    (next_id(), '黨參',   'dang shen',  '補虛藥', '{"nature":"平","flavor":["甘"],"meridian":["脾經","肺經"],"dosage_min":9,"dosage_max":30}', 'g'),
    (next_id(), '黃芪',   'huang qi',   '補虛藥', '{"nature":"溫","flavor":["甘"],"meridian":["脾經","肺經"],"dosage_min":9,"dosage_max":30}', 'g'),
    (next_id(), '當歸',   'dang gui',   '補虛藥', '{"nature":"溫","flavor":["甘","辛"],"meridian":["肝經","心經","脾經"],"dosage_min":6,"dosage_max":12}', 'g'),
    (next_id(), '白朮',   'bai zhu',    '補虛藥', '{"nature":"溫","flavor":["甘","苦"],"meridian":["脾經","胃經"],"dosage_min":6,"dosage_max":12}', 'g'),
    (next_id(), '茯苓',   'fu ling',    '利水滲濕藥', '{"nature":"平","flavor":["甘","淡"],"meridian":["心經","脾經","腎經"],"dosage_min":9,"dosage_max":15}', 'g'),
    (next_id(), '甘草',   'gan cao',    '補虛藥', '{"nature":"平","flavor":["甘"],"meridian":["心經","肺經","脾經","胃經"],"dosage_min":2,"dosage_max":10}', 'g'),
    (next_id(), '柴胡',   'chai hu',    '解表藥', '{"nature":"微寒","flavor":["苦","辛"],"meridian":["肝經","膽經"],"dosage_min":3,"dosage_max":9}', 'g'),
    (next_id(), '白芍',   'bai shao',   '補虛藥', '{"nature":"微寒","flavor":["苦","酸"],"meridian":["肝經","脾經"],"dosage_min":6,"dosage_max":15}', 'g'),
    (next_id(), '陳皮',   'chen pi',    '理氣藥', '{"nature":"溫","flavor":["辛","苦"],"meridian":["脾經","肺經"],"dosage_min":3,"dosage_max":9}', 'g'),
    (next_id(), '半夏',   'ban xia',    '化痰止咳平喘藥', '{"nature":"溫","flavor":["辛"],"meridian":["脾經","胃經","肺經"],"dosage_min":3,"dosage_max":9,"toxicity":"有毒"}', 'g'),
    (next_id(), '生薑',   'sheng jiang','解表藥', '{"nature":"微溫","flavor":["辛"],"meridian":["肺經","脾經","胃經"],"dosage_min":3,"dosage_max":9}', 'g'),
    (next_id(), '大棗',   'da zao',     '補虛藥', '{"nature":"溫","flavor":["甘"],"meridian":["脾經","胃經"],"dosage_min":6,"dosage_max":15}', 'g'),
    (next_id(), '酸棗仁', 'suan zao ren','安神藥', '{"nature":"平","flavor":["甘","酸"],"meridian":["心經","肝經","膽經"],"dosage_min":9,"dosage_max":15}', 'g'),
    (next_id(), '遠志',   'yuan zhi',   '安神藥', '{"nature":"溫","flavor":["苦","辛"],"meridian":["心經","腎經","肺經"],"dosage_min":3,"dosage_max":9}', 'g'),
    (next_id(), '龍骨',   'long gu',    '安神藥', '{"nature":"平","flavor":["甘","澀"],"meridian":["心經","肝經","腎經"],"dosage_min":15,"dosage_max":30}', 'g'),
    (next_id(), '牡蠣',   'mu li',      '安神藥', '{"nature":"微寒","flavor":["鹹"],"meridian":["肝經","腎經"],"dosage_min":15,"dosage_max":30}', 'g'),
    (next_id(), '川芎',   'chuan xiong','活血化瘀藥', '{"nature":"溫","flavor":["辛"],"meridian":["肝經","膽經","心包經"],"dosage_min":3,"dosage_max":9}', 'g'),
    (next_id(), '丹參',   'dan shen',   '活血化瘀藥', '{"nature":"微寒","flavor":["苦"],"meridian":["心經","肝經"],"dosage_min":9,"dosage_max":15}', 'g'),
    (next_id(), '黃芩',   'huang qin',  '清熱藥', '{"nature":"寒","flavor":["苦"],"meridian":["肺經","膽經","脾經","大腸經","小腸經"],"dosage_min":3,"dosage_max":9}', 'g'),
    (next_id(), '梔子',   'zhi zi',     '清熱藥', '{"nature":"寒","flavor":["苦"],"meridian":["心經","肺經","三焦經"],"dosage_min":6,"dosage_max":9}', 'g');

-- 15.6 配伍禁忌規則（十八反十九畏）
DO $$
DECLARE
    v_banxia BIGINT := (SELECT id FROM material_dict WHERE name = '半夏');
    v_gancao BIGINT := (SELECT id FROM material_dict WHERE name = '甘草');
    v_dangshen BIGINT := (SELECT id FROM material_dict WHERE name = '黨參');
    v_baishao BIGINT := (SELECT id FROM material_dict WHERE name = '白芍');
BEGIN
    -- 十八反：半萎貝蘞及攻烏 → 半夏反烏頭（此處以相反藥性示範）
    -- 十九畏：硫磺畏朴硝...（此處示範結構）
    INSERT INTO incompatibility_rule (id, material_a, material_b, rule_type, description, severity)
    VALUES
        (next_id(), v_banxia, v_gancao, 'eighteen_antagonism', '半夏反甘草（十八反：藻戟遂芫俱戰草）', 'block');
END $$;

-- 15.7 庫存（中環診所）
DO $$
DECLARE
    v_clinic1 BIGINT := (SELECT id FROM clinic WHERE code = 'XC-CENTRAL');
BEGIN
    PERFORM set_config('app.clinic_id', v_clinic1::text, true);

    INSERT INTO inventory (id, clinic_id, material_id, batch_no, expire_date, total_qty, locked_qty, unit_cost, unit_price, location)
    SELECT
        next_id(), v_clinic1, id, 'B202607-A',
        '2028-06-30'::date, 1000, 0,
        0.35, 0.50, 'A-01'
    FROM material_dict WHERE name IN ('黨參','茯苓','甘草','白朮','當歸');

    INSERT INTO inventory (id, clinic_id, material_id, batch_no, expire_date, total_qty, locked_qty, unit_cost, unit_price, location)
    SELECT
        next_id(), v_clinic1, id, 'B202607-B',
        '2028-06-30'::date, 500, 0,
        0.45, 0.65, 'A-02'
    FROM material_dict WHERE name IN ('柴胡','陳皮','酸棗仁','白芍');

    INSERT INTO inventory (id, clinic_id, material_id, batch_no, expire_date, total_qty, locked_qty, unit_cost, unit_price, location)
    SELECT
        next_id(), v_clinic1, id, 'B202607-C',
        '2028-12-31'::date, 800, 0,
        0.28, 0.40, 'B-01'
    FROM material_dict WHERE name IN ('黃芪','川芎','丹參','黃芩','梔子');

    INSERT INTO inventory (id, clinic_id, material_id, batch_no, expire_date, total_qty, locked_qty, unit_cost, unit_price, location)
    SELECT
        next_id(), v_clinic1, id, 'B202607-D',
        '2027-12-31'::date, 300, 0,
        0.55, 0.80, 'B-02'
    FROM material_dict WHERE name IN ('半夏','遠志','龍骨','牡蠣','生薑','大棗');
END $$;

-- 15.8 建立一個完整的就診場景（患者張志明 → 陳大明醫生線下就診）

DO $$
DECLARE
    v_clinic1   BIGINT := (SELECT id FROM clinic WHERE code = 'XC-CENTRAL');
    v_patient   BIGINT := (SELECT id FROM patient WHERE name = '張志明' AND clinic_id = v_clinic1);
    v_doctor    BIGINT := (SELECT d.id FROM doctor d JOIN user_account u ON d.user_id = u.id WHERE u.real_name = '陳大明' AND d.clinic_id = v_clinic1);
    v_emr_id    BIGINT;
    v_presc_id  BIGINT;
    v_presc_no  VARCHAR(32);
    v_items     JSONB;
BEGIN
    PERFORM set_config('app.clinic_id', v_clinic1::text, true);

    -- 建立 EMR
    v_emr_id := next_id();
    INSERT INTO emr (id, clinic_id, patient_id, doctor_id, visit_type, chief_complaint, present_illness, detail, diagnosis, advice, created_by)
    VALUES (
        v_emr_id, v_clinic1, v_patient, v_doctor, 'offline',
        '失眠3個月，伴心悸、口苦、脅肋脹痛',
        '患者3個月前因工作壓力大開始失眠，入睡困難，多夢易醒。伴有心悸、口苦、脅肋脹痛，情緒易怒。納可，大便偏乾，小便黃。',
        '{
            "inspection": {"tongue": "舌紅，苔薄黃", "face": "面色偏紅", "spirit": "神清"},
            "auscultation": {"voice": "語聲洪亮", "breath": "平穩"},
            "inquiry": {"appetite": "納可", "sleep": "入睡困難，多夢易醒，每夜睡3-4小時", "stool": "偏乾，1-2日一行", "urine": "黃"},
            "palpation": {"pulse_left": "弦數", "pulse_right": "弦", "pulse_detail": "左弦數，右弦"},
            "tcm_pattern": "肝鬱化火，擾動心神",
            "treatment_principle": "疏肝解鬱，清熱安神",
            "blood_pressure": "135/85",
            "heart_rate": 88
        }',
        ARRAY['不寐（肝鬱化火證）', 'Insomnia (Liver depression transforming into fire)'],
        '1. 保持情緒穩定，適當運動\n2. 避免咖啡濃茶\n3. 睡前1小時不看電子產品\n4. 一週後複診',
        (SELECT id FROM user_account WHERE real_name = '陳大明')
    );

    -- 建立處方 — 柴胡加龍骨牡蠣湯加減
    v_presc_id := next_id();
    v_presc_no := 'RX' || to_char(now(), 'YYYYMMDD') || '-' || LPAD(nextval('global_id_seq')::text, 4, '0');

    v_items := '[
        {"material_name": "柴胡",   "dosage": 12, "unit": "g", "decoction_note": "無", "unit_price": 0.65, "subtotal": 7.80},
        {"material_name": "黃芩",   "dosage": 9,  "unit": "g", "decoction_note": "無", "unit_price": 0.40, "subtotal": 3.60},
        {"material_name": "龍骨",   "dosage": 30, "unit": "g", "decoction_note": "先煎", "unit_price": 0.80, "subtotal": 24.00},
        {"material_name": "牡蠣",   "dosage": 30, "unit": "g", "decoction_note": "先煎", "unit_price": 0.80, "subtotal": 24.00},
        {"material_name": "黨參",   "dosage": 15, "unit": "g", "decoction_note": "無", "unit_price": 0.50, "subtotal": 7.50},
        {"material_name": "茯苓",   "dosage": 15, "unit": "g", "decoction_note": "無", "unit_price": 0.50, "subtotal": 7.50},
        {"material_name": "酸棗仁", "dosage": 15, "unit": "g", "decoction_note": "無", "unit_price": 0.65, "subtotal": 9.75},
        {"material_name": "遠志",   "dosage": 6,  "unit": "g", "decoction_note": "無", "unit_price": 0.80, "subtotal": 4.80},
        {"material_name": "當歸",   "dosage": 9,  "unit": "g", "decoction_note": "無", "unit_price": 0.65, "subtotal": 5.85},
        {"material_name": "白芍",   "dosage": 12, "unit": "g", "decoction_note": "無", "unit_price": 0.65, "subtotal": 7.80},
        {"material_name": "川芎",   "dosage": 6,  "unit": "g", "decoction_note": "無", "unit_price": 0.40, "subtotal": 2.40},
        {"material_name": "梔子",   "dosage": 9,  "unit": "g", "decoction_note": "無", "unit_price": 0.40, "subtotal": 3.60},
        {"material_name": "甘草",   "dosage": 6,  "unit": "g", "decoction_note": "無", "unit_price": 0.40, "subtotal": 2.40},
        {"material_name": "生薑",   "dosage": 3,  "unit": "片","decoction_note": "無", "unit_price": 0.10, "subtotal": 0.30},
        {"material_name": "大棗",   "dosage": 5,  "unit": "枚","decoction_note": "無", "unit_price": 0.15, "subtotal": 0.75}
    ]'::jsonb;

    INSERT INTO prescription (id, clinic_id, prescription_no, patient_id, doctor_id, emr_id,
        visit_type, status, dose_count, dose_days, items, total_amount, decoction_method, delivery_option)
    VALUES (
        v_presc_id, v_clinic1, v_presc_no, v_patient, v_doctor, v_emr_id,
        'offline', 'paid', 5, 5,
        v_items,
        112.05,
        'self', 'pickup'
    );

    -- 建立支付記錄
    INSERT INTO payment (id, clinic_id, prescription_id, payment_no, amount, method, status, paid_at)
    VALUES (
        next_id(), v_clinic1, v_presc_id,
        'PAY' || to_char(now(), 'YYYYMMDD') || '-' || LPAD(nextval('global_id_seq')::text, 4, '0'),
        112.05, 'octopus', 'paid', now()
    );

    -- 審計日誌
    INSERT INTO audit_log (id, clinic_id, user_id, user_name, action, target_table, target_id, patient_id, ip_address)
    VALUES
        (next_id(), v_clinic1, v_doctor, '陳大明', 'INSERT', 'emr',          v_emr_id,   v_patient, '192.168.1.100'),
        (next_id(), v_clinic1, v_doctor, '陳大明', 'INSERT', 'prescription', v_presc_id, v_patient, '192.168.1.100');
END $$;

COMMIT;

-- ============================================================================
-- 16. 使用說明
-- ============================================================================

/*
## 連線設定

應用程式連線後必須執行：
  SELECT set_clinic_context(<clinic_id>);

或直接在應用層 SQL 攔截器中：
  SET LOCAL app.clinic_id = '<clinic_id>';

## 分區維護

每月初執行（可由 XXL-JOB 定時調用）：
  SELECT create_monthly_prescription_partition();

## 物化視圖刷新

每日凌晨執行（可由 XXL-JOB 定時調用）：
  SELECT refresh_materialized_views();

## 雪花 ID

所有主鍵使用 next_id() 生成，支援分散式環境。
若需更改 worker_id，請為每個服務實例設定不同的 worker_id。
*/
