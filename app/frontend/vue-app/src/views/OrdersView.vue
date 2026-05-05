<template>
  <div>
    <div class="d-flex align-items-center mb-4 gap-3">
      <h2 class="mb-0"><i class="bi bi-receipt me-2 text-primary"></i>주문 내역</h2>
      <button class="btn btn-outline-primary" @click="loadOrders" :disabled="loading">
        <span v-if="loading" class="spinner-border spinner-border-sm me-1"></span>
        <i v-else class="bi bi-arrow-clockwise me-1"></i>새로고침
      </button>
    </div>

    <div v-if="!currentUser.id" class="alert alert-warning">
      <i class="bi bi-person-exclamation me-2"></i>
      <router-link to="/users">사용자 페이지</router-link>에서 사용자를 먼저 선택하세요.
    </div>

    <div v-if="error" class="alert alert-danger">
      <i class="bi bi-exclamation-triangle-fill me-2"></i>{{ error }}
    </div>

    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary"></div>
    </div>

    <div v-else-if="orders.length === 0 && !error" class="text-center text-muted py-5">
      <i class="bi bi-inbox display-4 d-block mb-3 opacity-50"></i>
      <p>주문 내역이 없습니다.</p>
    </div>

    <div v-else class="card shadow-sm">
      <div class="card-body p-0">
        <table class="table table-hover mb-0">
          <thead class="table-light">
            <tr>
              <th>주문번호</th>
              <th>상태</th>
              <th class="text-end">금액</th>
              <th>주문일시</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in orders" :key="order.id">
              <td class="align-middle font-monospace">{{ order.orderNumber }}</td>
              <td class="align-middle">
                <span :class="statusBadge(order.status)">{{ statusLabel(order.status) }}</span>
              </td>
              <td class="text-end align-middle fw-bold">{{ order.totalAmount.toLocaleString() }}원</td>
              <td class="align-middle text-muted small">{{ formatDate(order.createdAt) }}</td>
              <td class="align-middle">
                <router-link :to="`/orders/${order.id}`" class="btn btn-sm btn-outline-primary">
                  상세보기
                </router-link>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getOrders } from '../api/orderApi.js'
import { currentUser } from '../store/currentUser.js'

const orders = ref([])
const loading = ref(false)
const error = ref(null)

async function loadOrders() {
  loading.value = true
  error.value = null
  try {
    orders.value = await getOrders(currentUser.id)
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

function statusLabel(status) {
  const map = {
    PENDING: '주문접수',
    CONFIRMED: '주문확인',
    PAYMENT_PENDING: '결제대기',
    PAID: '결제완료',
    SHIPPED: '배송중',
    DELIVERED: '배송완료',
    CANCELLED: '취소됨'
  }
  return map[status] || status
}

function statusBadge(status) {
  const map = {
    PENDING: 'badge bg-secondary',
    CONFIRMED: 'badge bg-info',
    PAYMENT_PENDING: 'badge bg-warning text-dark',
    PAID: 'badge bg-success',
    SHIPPED: 'badge bg-primary',
    DELIVERED: 'badge bg-success',
    CANCELLED: 'badge bg-danger'
  }
  return map[status] || 'badge bg-secondary'
}

function formatDate(dt) {
  if (!dt) return '-'
  return new Date(dt).toLocaleString('ko-KR')
}

onMounted(loadOrders)
</script>
