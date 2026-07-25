// Multi-app entry — imports based on MODE
const mode = import.meta.env.MODE
if (mode === 'doctor') import('./apps/doctor/src/main')
else if (mode === 'admin') import('./apps/admin/src/main')
else import('./apps/patient/src/main')
