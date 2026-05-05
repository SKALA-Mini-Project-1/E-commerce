const API_BASE = '/api'

export async function getPaymentByOrder(orderId) {
  const res = await fetch(`${API_BASE}/payments/order/${orderId}`)
  if (!res.ok) throw new Error(`결제 조회 실패: ${res.status}`)
  return res.json()
}

export async function createPayment(orderId, amount, method) {
  const res = await fetch(`${API_BASE}/payments`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ orderId, amount, method })
  })
  if (!res.ok) throw new Error(`결제 생성 실패: ${res.status}`)
  return res.json()
}

export async function confirmPayment(id) {
  const res = await fetch(`${API_BASE}/payments/${id}/confirm`, { method: 'POST' })
  if (!res.ok) throw new Error(`결제 승인 실패: ${res.status}`)
  return res.json()
}

export async function cancelPayment(id, reason) {
  const res = await fetch(`${API_BASE}/payments/${id}/cancel`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ reason })
  })
  if (!res.ok) throw new Error(`결제 취소 실패: ${res.status}`)
  return res.json()
}
