const API_BASE = '/api'

export async function getProducts() {
  const res = await fetch(`${API_BASE}/products`)
  if (!res.ok) throw new Error(`상품 조회 실패: ${res.status}`)
  return res.json()
}

export async function getProduct(id) {
  const res = await fetch(`${API_BASE}/products/${id}`)
  if (!res.ok) throw new Error(`상품 조회 실패: ${res.status}`)
  return res.json()
}
