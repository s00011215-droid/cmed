#!/usr/bin/env bash
# ============================================================================
# 祥雲智方 — 全鏈路整合測試腳本
# 用法：bash integration-test.sh [BASE_URL]
# 預設 BASE_URL=http://localhost:8080
# ============================================================================
set -euo pipefail
BASE="${1:-http://localhost:8080}"
PASS=0; FAIL=0

ok()  { echo "  ✅ $1"; ((PASS++)); }
fail(){ echo "  ❌ $1 — $2"; ((FAIL++)); }

assert() {
  local desc="$1" expected="$2" actual="$3"
  if [[ "$actual" == "$expected" ]]; then ok "$desc"; else fail "$desc" "expected=$expected actual=$actual"; fi
}

echo "=============================================="
echo " 祥雲智方 — 全鏈路整合測試"
echo " BASE: $BASE"
echo "=============================================="

# ---- 1. 帳號服務 ----
echo ""; echo "--- 1. Account SSO ---"

LOGIN=$(curl -s -X POST "$BASE/api/v1/account/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"dr_chan","password":"password123"}')
LOGIN_CODE=$(echo "$LOGIN" | python3 -c "import sys,json; print(json.load(sys.stdin).get('code',-1))" 2>/dev/null || echo -1)
assert "Login returns code 0" "0" "$LOGIN_CODE"

TOKEN=$(echo "$LOGIN" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['accessToken'])" 2>/dev/null || echo "")
if [[ -n "$TOKEN" ]]; then ok "JWT token extracted"; else fail "JWT token extracted" "empty token"; fi

# ---- 2. 患者服務 ----
echo ""; echo "--- 2. Patient Service ---"

PATIENTS=$(curl -s "$BASE/api/v1/patient?keyword=張" -H "Authorization: Bearer $TOKEN")
P_CODE=$(echo "$PATIENTS" | python3 -c "import sys,json; print(json.load(sys.stdin).get('code',-1))" 2>/dev/null || echo -1)
assert "Search patient returns code 0" "0" "$P_CODE"

PATIENT_ID=$(echo "$PATIENTS" | python3 -c "import sys,json; d=json.load(sys.stdin)['data']; print(d['records'][0]['id'] if d.get('records') else '')" 2>/dev/null || echo "")
if [[ -n "$PATIENT_ID" ]]; then ok "Patient ID found: $PATIENT_ID"; else fail "Patient ID found" "no records"; fi

# ---- 3. EMR 電子病歷 ----
echo ""; echo "--- 3. EMR Service ---"

EMR_PAYLOAD='{"patientId":'"${PATIENT_ID:-1}"',"doctorId":1,"visitType":"offline","chiefComplaint":"測試主訴：失眠","presentIllness":"測試現病史","detail":{"inspection":{"tongue":"舌淡紅","face":"面色紅潤","spirit":"神清"},"auscultation":{"voice":"語聲清晰","cough":"無","breath":"平穩"},"inquiry":{"appetite":"納可","sleep":"入睡困難","stool":"便調","urine":"小便可"},"palpation":{"pulseLeft":"弦","pulseRight":"滑","pulseDetail":"左弦右滑"},"tcmPattern":"肝鬱脾虛","treatmentPrinciple":"疏肝健脾"},"diagnosis":"不寐","advice":"保持情緒穩定"}'
EMR_RES=$(curl -s -X POST "$BASE/api/v1/emr" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "$EMR_PAYLOAD")
EMR_CODE=$(echo "$EMR_RES" | python3 -c "import sys,json; print(json.load(sys.stdin).get('code',-1))" 2>/dev/null || echo -1)
assert "Create EMR returns code 0" "0" "$EMR_CODE"

EMR_ID=$(echo "$EMR_RES" | python3 -c "import sys,json; print(json.load(sys.stdin)['data'])" 2>/dev/null || echo "")
if [[ -n "$EMR_ID" ]]; then ok "EMR created: $EMR_ID"; fi

# ---- 4. 處方服務 ----
echo ""; echo "--- 4. Prescription Service ---"

RX_PAYLOAD='{"patientId":'"${PATIENT_ID:-1}"',"doctorId":1,"visitType":"offline","doseCount":5,"doseDays":5,"items":[{"materialId":7,"materialName":"柴胡","dosage":12,"unit":"g","decoctionNote":"無","unitPrice":0.65},{"materialId":15,"materialName":"龍骨","dosage":30,"unit":"g","decoctionNote":"先煎","unitPrice":0.80},{"materialId":1,"materialName":"黨參","dosage":15,"unit":"g","decoctionNote":"無","unitPrice":0.50}],"decoctionMethod":"center","deliveryOption":"delivery"}'
RX_RES=$(curl -s -X POST "$BASE/api/v1/prescription" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "$RX_PAYLOAD")
RX_CODE=$(echo "$RX_RES" | python3 -c "import sys,json; print(json.load(sys.stdin).get('code',-1))" 2>/dev/null || echo -1)
assert "Create prescription returns code 0" "0" "$RX_CODE"

RX_ID=$(echo "$RX_RES" | python3 -c "import sys,json; d=json.load(sys.stdin)['data']; print(d.get('id',''))" 2>/dev/null || echo "")
WARNINGS=$(echo "$RX_RES" | python3 -c "import sys,json; d=json.load(sys.stdin)['data']; print(len(d.get('warnings',[])))" 2>/dev/null || echo "0")
if [[ -n "$RX_ID" ]]; then ok "Prescription created: $RX_ID (warnings: $WARNINGS)"; fi

# ---- 5. 處方狀態機 ----
echo ""; echo "--- 5. Prescription Status Machine ---"

for status in pending_review approved paid; do
  TRANS=$(curl -s -X POST "$BASE/api/v1/prescription/$RX_ID/transition" \
    -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
    -d "{\"status\":\"$status\"}" 2>/dev/null)
  T_CODE=$(echo "$TRANS" | python3 -c "import sys,json; print(json.load(sys.stdin).get('code',-1))" 2>/dev/null || echo -1)
  assert "Transition to $status" "0" "$T_CODE"
done

# ---- 6. 庫存鎖定 ----
echo ""; echo "--- 6. Inventory Lock ---"

LOCK_PAYLOAD='{"prescriptionId":'"$RX_ID"',"items":[{"materialId":7,"qty":60},{"materialId":15,"qty":150},{"materialId":1,"qty":75}]}'
LOCK_RES=$(curl -s -X POST "$BASE/api/v1/inventory/lock" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "$LOCK_PAYLOAD")
LOCK_CODE=$(echo "$LOCK_RES" | python3 -c "import sys,json; print(json.load(sys.stdin).get('code',-1))" 2>/dev/null || echo -1)
assert "Lock inventory returns code 0" "0" "$LOCK_CODE"

# ---- 7. 財務支付 ----
echo ""; echo "--- 7. Finance Payment ---"

PAY_RES=$(curl -s -X POST "$BASE/api/v1/finance/pay" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d "{\"prescriptionId\":$RX_ID,\"amount\":112.05,\"method\":\"octopus\"}")
PAY_CODE=$(echo "$PAY_RES" | python3 -c "import sys,json; print(json.load(sys.stdin).get('code',-1))" 2>/dev/null || echo -1)
assert "Create payment returns code 0" "0" "$PAY_CODE"

# ---- 8. 煎藥訂單 ----
echo ""; echo "--- 8. Decoction Order ---"

DC_PAYLOAD='{"prescriptionId":'"$RX_ID"',"clinicCode":"XC-CENTRAL","doseCount":5,"items":[{"materialName":"柴胡","dosage":12,"unit":"g","decoctionNote":"無"},{"materialName":"龍骨","dosage":30,"unit":"g","decoctionNote":"先煎"},{"materialName":"黨參","dosage":15,"unit":"g","decoctionNote":"無"}],"craft":{"method":"decoction","packaging":"vacuum","packSizeMl":200},"deliveryOption":"delivery","receiver":{"name":"張志明","phone":"98765432","address":"香港中環測試地址"}}'
DC_RES=$(curl -s -X POST "$BASE/api/v1/decoction/orders" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "$DC_PAYLOAD")
DC_CODE=$(echo "$DC_RES" | python3 -c "import sys,json; print(json.load(sys.stdin).get('code',-1))" 2>/dev/null || echo -1)
assert "Create decoction order returns code 0" "0" "$DC_CODE"

DC_ID=$(echo "$DC_RES" | python3 -c "import sys,json; d=json.load(sys.stdin)['data']; print(d.get('id',''))" 2>/dev/null || echo "")
DC_EXT=$(echo "$DC_RES" | python3 -c "import sys,json; d=json.load(sys.stdin)['data']; print(d.get('externalNo',''))" 2>/dev/null || echo "")
if [[ -n "$DC_ID" ]]; then ok "Decoction order created: $DC_ID (ext: $DC_EXT)"; fi

# ---- 9. 物流下單 ----
echo ""; echo "--- 9. Logistics Order ---"

LOG_RES=$(curl -s -X POST "$BASE/api/v1/logistics/orders" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d "{\"decoctionId\":${DC_ID:-1},\"carrier\":\"sf\",\"receiver\":{\"name\":\"張志明\",\"phone\":\"98765432\",\"address\":\"香港中環測試地址\"}}")
LOG_CODE=$(echo "$LOG_RES" | python3 -c "import sys,json; print(json.load(sys.stdin).get('code',-1))" 2>/dev/null || echo -1)
assert "Create logistics order returns code 0" "0" "$LOG_CODE"

WB=$(echo "$LOG_RES" | python3 -c "import sys,json; d=json.load(sys.stdin)['data']; print(d.get('waybillNo',''))" 2>/dev/null || echo "")
if [[ -n "$WB" ]]; then ok "Logistics waybill: $WB"; fi

# ---- 10. 管理後台 ----
echo ""; echo "--- 10. Admin Dashboard ---"

ADM_RES=$(curl -s "$BASE/api/v1/admin/dashboard" -H "Authorization: Bearer $TOKEN")
ADM_CODE=$(echo "$ADM_RES" | python3 -c "import sys,json; print(json.load(sys.stdin).get('code',-1))" 2>/dev/null || echo -1)
assert "Admin dashboard returns code 0" "0" "$ADM_CODE"

# ---- Summary ----
echo ""; echo "=============================================="
echo " 測試結果: $PASS passed, $FAIL failed"
echo "=============================================="
if [[ $FAIL -gt 0 ]]; then exit 1; fi
echo "🎉 全鏈路整合測試通過！"
