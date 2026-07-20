// ================================================================
// 祥雲智方 API 型別定義 — 與後端 DTO 一一對應
// ================================================================

// --- 通用 ---
export interface Result<T> { code: number; message: string; data: T; traceId?: string }
export interface Page<T> { records: T[]; total: number; size: number; current: number }

// --- Account ---
export interface LoginRequest { username: string; password: string }
export interface LoginResponse { accessToken: string; refreshToken: string; tokenType: string; expiresIn: number; userInfo: UserInfo }
export interface UserInfo { userId: number; username: string; realName: string; role: string; clinicId: number }

// --- Patient ---
export interface Patient { id: number; name: string; gender: string; phone: string; birthDate: string; bloodType?: string; allergyInfo?: string; address?: string }
export interface PatientSave { id?: number; name: string; gender: string; phone: string; birthDate: string; bloodType?: string; allergyInfo?: string; address?: string }

// --- EMR ---
export interface EmrSave { id?: number; patientId: number; doctorId: number; visitType: string; chiefComplaint: string; presentIllness: string; detail: EmrDetail; diagnosis: string; advice?: string }
export interface EmrDetail {
  inspection?: { tongue: string; face: string; spirit: string }
  auscultation?: { voice: string; cough: string; breath: string }
  inquiry?: { appetite: string; sleep: string; stool: string; urine: string }
  palpation?: { pulseLeft: string; pulseRight: string; pulseDetail: string }
  tcmPattern: string; treatmentPrinciple: string
  temperature?: number; bloodPressure?: string; heartRate?: number
}

// --- Prescription ---
export interface PrescriptionItem { materialId: number; materialName: string; dosage: number; unit: string; decoctionNote: string; unitPrice: number; subtotal: number }
export interface PrescriptionSave { id?: number; patientId: number; doctorId: number; visitType: string; doseCount: number; doseDays: number; items: PrescriptionItem[]; decoctionMethod: string; deliveryOption: string }
export interface PrescriptionDetail extends PrescriptionSave {
  prescriptionNo: string; status: string; totalAmount: number; decoctionFee: number; deliveryFee: number
  signStatus: string; warnings: string[]; createdAt: string
}

// --- Decoction ---
export interface DecoctionOrder { id: number; prescriptionId: number; externalNo: string; status: string; doseCount: number; vacuumPkgNo?: string; craft: any; receiver: any }

// --- Logistics ---
export interface LogisticsOrder { id: number; decoctionId: number; waybillNo: string; carrier: string; status: string; latestTrace?: any; signedAt?: string }

// --- Finance ---
export interface Payment { id: number; prescriptionId: number; paymentNo: string; amount: number; method: string; status: string; paidAt: string }
export interface DailySettlement { date: string; totalRevenue: number; prescriptionCount: number }
