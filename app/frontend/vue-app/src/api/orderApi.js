const API_BASE = '/api'

export async function getOrders(userId) {
  const url = userId ? `${API_BASE}/orders?userId=${userId}` : `${API_BASE}/orders`
  const res = await fetch(url)
  if (!res.ok) throw new Error(`주문 조회 실패: ${res.status}`)
  return res.json()
}

export async function getOrder(id) {
  const res = await fetch(`${API_BASE}/orders/${id}`)
  if (!res.ok) throw new Error(`주문 조회 실패: ${res.status}`)
  return res.json()
}

export async function createOrder(userId, items) {
  const res = await fetch(`${API_BASE}/orders`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userId, items })
  })
  if (!res.ok) throw new Error(`주문 생성 실패: ${res.status}`)
  return res.json()
}

export async function cancelOrder(id) {
  const res = await fetch(`${API_BASE}/orders/${id}/cancel`, { method: 'POST' })
  if (!res.ok) throw new Error(`주문 취소 실패: ${res.status}`)
  return res.json()
}
