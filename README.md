# 祥雲智方中醫診症系統

> XiangYun ZhiFang TCM Clinic System — 一體化中醫診療平台

## 技術棧

| 層級 | 技術 |
|---|---|
| 前端 | Vue 3 + TypeScript + Vite + PWA |
| 後端 | Java 17 + Spring Boot 3.2 + Spring Cloud Alibaba |
| 資料庫 | PostgreSQL 15 (JSONB + RLS + 分區表) |
| 快取 | Redis 7 |
| 訊息佇列 | RocketMQ 5.x |
| 物件儲存 | MinIO |
| 服務治理 | Nacos 2.x + Sentinel |
| 部署 | Docker + Kubernetes |

## 目錄結構

```
xiangyun-zhifang/
├── backend/                     # 後端微服務 (Maven Monorepo)
│   ├── pom.xml                  # 父 POM (依賴管理)
│   ├── xiangyun-common/         # 通用模組 (ID生成/RLS/審計/統一回應)
│   ├── xiangyun-gateway/        # API 網關 (Spring Cloud Gateway + JWT)
│   ├── xiangyun-account/        # 帳號服務 (SSO/登入/註冊)
│   ├── xiangyun-patient/        # 患者服務
│   ├── xiangyun-doctor/         # 醫生排班服務
│   ├── xiangyun-consult/        # 問診服務
│   ├── xiangyun-emr/            # 電子病歷服務
│   ├── xiangyun-prescription/   # 電子處方服務
│   ├── xiangyun-sign/           # 電子簽章服務 (PKCS#7/CMS)
│   ├── xiangyun-his/            # 診所門診服務
│   ├── xiangyun-material/       # 藥材字典服務
│   ├── xiangyun-inventory/      # 庫存管理服務
│   ├── xiangyun-finance/        # 財務結算服務
│   ├── xiangyun-decoction/      # 煎藥中心對接
│   ├── xiangyun-logistics/      # 物流對接
│   ├── xiangyun-insurance/      # 醫保對接
│   ├── xiangyun-notify/         # 通知服務
│   ├── xiangyun-risk/           # 風控服務
│   ├── xiangyun-admin/          # 雙後台服務
│   └── xiangyun-audit/          # 審計服務
├── frontend/                    # 前端 (Monorepo)
│   ├── packages/ui/             # 通用組件庫
│   ├── packages/utils/          # 通用工具
│   └── apps/
│       ├── patient/             # 患者端 PWA
│       ├── doctor/              # 醫生工作台
│       ├── nurse/               # 護士收銀台
│       ├── pharmacy/            # 藥房調劑台
│       └── admin/               # 院長/平台後台
├── infra/                       # 基礎設施
│   ├── database/                # SQL 腳本
│   │   ├── 00-init-schema.sql
│   │   ├── 01-audit-triggers.sql
│   │   └── 02-prescription-signature.sql
│   ├── docker/                  # Docker Compose
│   ├── k8s/                     # Kubernetes YAML
│   └── monitoring/              # Prometheus + Grafana 配置
├── docs/                        # 文檔
│   ├── api/                     # OpenAPI 規格
│   ├── architecture/            # 架構文檔
│   └── compliance/              # 合規文檔
└── .github/workflows/           # CI/CD Pipeline
```

## 快速啟動 (開發環境)

```bash
# 1. 啟動基礎設施
cd infra/docker
docker-compose up -d

# 2. 初始化資料庫
docker exec -i xiangyun-pg-primary psql -U xiangyun_app -d xiangyun_zhifang \
  < ../database/00-init-schema.sql
docker exec -i xiangyun-pg-primary psql -U xiangyun_app -d xiangyun_zhifang \
  < ../database/01-audit-triggers.sql

# 3. 啟動 Nacos
# Nacos Console: http://localhost:8848/nacos (用戶: nacos / 密碼: nacos)

# 4. 啟動 Gateway
cd backend/xiangyun-gateway
mvn spring-boot:run

# 5. 啟動 Account Service
cd backend/xiangyun-account
mvn spring-boot:run

# 6. 測試 API
curl -X POST http://localhost:8080/api/v1/account/login \
  -H "Content-Type: application/json" \
  -d '{"username":"dr_chan","password":"password123"}'
```

## 開發階段

| 階段 | 狀態 | 內容 |
|---|---|---|
| Phase 1 | ✅ 進行中 | 專案腳手架 + DB + Docker + Common 模組 |
| Phase 2 | ⬜ 待開始 | Gateway 網關 + Account SSO |
| Phase 3 | ⬜ 待開始 | 核心業務服務 (患者/EMR/處方/HIS) |
| Phase 4 | ⬜ 待開始 | 營運服務 (庫存/財務/煎藥/物流) |
| Phase 5 | ⬜ 待開始 | 前端 (PWA + 醫生工作台 + 後台) |
| Phase 6 | ⬜ 待開始 | 整合測試 + K8s 部署 |
