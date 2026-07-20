# ============================================================================
# 祥雲智方 — 上線清單 (Go-Live Checklist)
# ============================================================================

## 上線日期：___________    審批人：___________

### 🔴 Phase A：基礎設施（上線前 2 週完成）

- [ ] K8s 集群已創建（至少 3 個 Worker Node，每個 4C8G）
- [ ] PostgreSQL 高可用集群已部署（Patroni + etcd + PgBouncer）
- [ ] SSL/TLS 證書已申請並安裝（Let's Encrypt 或商業 CA）
- [ ] DNS 記錄已配置（api.xiangyun-zhifang.com → Ingress LB）
- [ ] Redis Cluster 已部署並測試
- [ ] RocketMQ 集群已部署
- [ ] MinIO 儲存已配置（含生命週期規則）
- [ ] Nacos 已部署並建立 namespace
- [ ] Prometheus + Grafana + AlertManager 已部署
- [ ] ELK 日誌收集已配置（Filebeat → Elasticsearch）
- [ ] 備份策略已配置（pgBackRest + OSS 歸檔，每日全備 + 每小時增量）
- [ ] 防火牆規則已設定（僅必要端口對外）

### 🟡 Phase B：應用部署（上線前 1 週完成）

- [ ] 所有微服務 Docker Image 已推送至 Registry
- [ ] Helm Chart 已部署到 Staging 環境
- [ ] 資料庫初始化腳本已在 Staging 執行（含測試資料）
- [ ] 全鏈路整合測試全部通過（`integration-test.sh`）
- [ ] 前端已構建並部署（PWA Service Worker 已註冊）
- [ ] API Gateway 路由規則驗證（所有服務可達）
- [ ] JWT 鑑權流程驗證（登入 → Token → 訪問受保護 API）
- [ ] RLS 租戶隔離驗證（跨 clinic_id 無法訪問）
- [ ] 配伍禁忌引擎測試（warn + block 規則觸發）

### 🟢 Phase C：外部對接（上線前 3 天完成）

- [ ] 煎藥中心 API Key 已獲取並配置
- [ ] 物流服務商 API Key 已獲取並配置
- [ ] 支付網關（八達通/Alipay/WeChat Pay）已配置
- [ ] 短信網關已配置
- [ ] Web Push VAPID Key 已生成並配置
- [ ] 煎藥回調 HMAC Key 已與煎藥中心同步
- [ ] 物流回調端點已配置到服務商後台
- [ ] 電子簽章憑證已導入（醫生的 e-Cert .p12）

### 🔵 Phase D：安全審計（上線前 2 天完成）

- [ ] OWASP Top 10 掃描通過
- [ ] SQL 注入測試通過（所有 API 端點）
- [ ] XSS 防護測試通過
- [ ] CSRF Token 機制驗證
- [ ] TLS 1.3 強制啟用確認
- [ ] 密鑰管理（Vault/KMS）配置完成
- [ ] 審計日誌觸發器已啟用（P0-1）
- [ ] 資料加密靜態/傳輸層驗證
- [ ] 滲透測試報告已審閱

### 🟣 Phase E：效能測試（上線前 1 天完成）

- [ ] 壓力測試：500 並發用戶模擬
- [ ] 處方開立 API：p95 < 500ms
- [ ] EMR 查詢 API：p95 < 300ms
- [ ] 資料庫連接池未飽和（< 80%）
- [ ] Redis 命中率 > 90%
- [ ] K8s HPA 自動伸縮驗證
- [ ] PostgreSQL 慢查詢日誌已審閱

### ⚪ Phase F：上線當日

- [ ] 資料庫備份（上線前最後一次全備）
- [ ] Staging → Production Helm 部署
- [ ] Smoke Test（核心 API 快速驗證）
- [ ] 監控面板檢查（所有指標正常）
- [ ] 警報規則確認（測試警報可觸發）
- [ ] 回滾方案已準備（上一版本 Helm Release）

### 🟠 回滾方案 (Rollback Plan)

```bash
# 若上線後發現嚴重問題，執行以下回滾：
helm rollback xiangyun-zhifang -n xiangyun-zhifang

# 緊急情況下暫停流量：
kubectl scale deployment -n xiangyun-zhifang --replicas=0 --all

# 恢復資料庫（PITR）：
# pgBackRest --stanza=xiangyun --type=time --target="2026-XX-XX XX:XX:XX" restore
```

### 📞 緊急聯絡

| 角色 | 姓名 | 電話 |
|---|---|---|
| DevOps 工程師 | _________ | _________ |
| 系統架構師 | _________ | _________ |
| DPO | _________ | _________ |
| 煎藥中心技術窗口 | _________ | _________ |
| 物流服務商窗口 | _________ | _________ |
| 雲端服務商支援 | _________ | _________ |
