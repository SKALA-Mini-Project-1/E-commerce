<template>
  <div>
    <div class="d-flex align-items-center mb-4 gap-3">
      <router-link to="/orders" class="btn btn-outline-secondary btn-sm">
        <i class="bi bi-arrow-left me-1"></i>목록으로
      </router-link>
      <h2 class="mb-0"><i class="bi bi-receipt-cutoff me-2 text-primary"></i>주문 상세</h2>
    </div>

    <div v-if="error" class="alert alert-danger">
      <i class="bi bi-exclamation-triangle-fill me-2"></i>{{ error }}
    </div>

    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary"></div>
    </div>

    <div v-else-if="order">
      <!-- 주문 정보 -->
      <div class="card shadow-sm mb-4">
        <div class="card-header d-flex justify-content-between align-items-center">
          <span class="fw-bold font-monospace">{{ order.orderNumber }}</span>
          <span :class="statusBadge(order.status)">{{ statusLabel(order.status) }}</span>
        </div>
        <div class="card-body">
          <table class="table table-borderless mb-0">
            <tbody>
              <tr v-for="item in order.items" :key="item.productId">
                <td>{{ item.productName }}</td>
                <td class="text-muted">{{ item.quantity }}개</td>
                <td class="text-end">{{ item.unitPrice.toLocaleString() }}원</td>
                <td class="text-end fw-bold">{{ item.subtotal.toLocaleString() }}원</td>
              </tr>
            </tbody>
            <tfoot class="border-top">
              <tr>
                <td colspan="3" class="text-end fw-bold">합계</td>
                <td class="text-end fw-bold text-primary fs-5">{{ order.totalAmount.toLocaleString() }}원</td>
              </tr>
            </tfoot>
          </table>
          <p class="text-muted small mb-0 mt-2">주문일시: {{ formatDate(order.createdAt) }}</p>
        </div>
      </div>

      <!-- 결제 정보 -->
      <div class="card shadow-sm mb-4">
        <div class="card-header fw-bold">
          <i class="bi bi-credit-card me-2"></i>결제 정보
        </div>
        <div class="card-body">

          <!-- 결제 완료 -->
          <div v-if="payment">
            <div class="row g-2 mb-3">
              <div class="col-md-6">
                <small class="text-muted">결제키</small>
                <div class="font-monospace">{{ payment.paymentKey }}</div>
              </div>
              <div class="col-md-3">
                <small class="text-muted">결제방법</small>
                <div>{{ methodLabel(payment.method) }}</div>
              </div>
              <div class="col-md-3">
                <small class="text-muted">결제상태</small>
                <div><span :class="paymentBadge(payment.status)">{{ paymentLabel(payment.status) }}</span></div>
              </div>
            </div>
            <div class="d-flex gap-2">
              <button
                v-if="payment.status === 'PENDING'"
                class="btn btn-success"
                :disabled="actionLoading"
                @click="doConfirmPayment"
              >
                <span v-if="actionLoading" class="spinner-border spinner-border-sm me-1"></span>
                <i v-else class="bi bi-check-circle me-1"></i>결제 승인
              </button>
              <button
                v-if="payment.status === 'PENDING' || payment.status === 'APPROVED'"
                class="btn btn-outline-danger"
                :disabled="actionLoading"
                @click="doCancelPayment"
              >
                <i class="bi bi-x-circle me-1"></i>결제 취소
              </button>
            </div>
          </div>

          <!-- 결제 대기 / 신규 결제 -->
          <div v-else-if="canPay">
            <p class="text-muted mb-3">결제 수단을 선택하세요.</p>
            <div class="d-flex gap-2 align-items-center flex-wrap mb-3">
              <div v-for="m in paymentMethods" :key="m.value">
                <input type="radio" class="btn-check" :id="`method-${m.value}`" v-model="selectedMethod" :value="m.value">
                <label class="btn btn-outline-secondary" :for="`method-${m.value}`">{{ m.label }}</label>
              </div>
            </div>
            <button class="btn btn-primary" :disabled="!selectedMethod || actionLoading" @click="doCreatePayment">
              <span v-if="actionLoading" class="spinner-border spinner-border-sm me-1"></span>
              <i v-else class="bi bi-credit-card me-1"></i>결제하기
            </button>
          </div>

          <div v-else class="text-muted">
            결제를 진행할 수 없는 상태입니다.
          </div>
        </div>
      </div>

      <!-- 주문 취소 -->
      <div v-if="canCancel" class="text-end">
        <button class="btn btn-outline-danger" :disabled="actionLoading" @click="doCancelOrder">
          <i class="bi bi-x-circle me-1"></i>주문 취소
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getOrder, cancelOrder } from '../api/orderApi.js'
import { getPaymentByOrder, createPayment, confirmPayment, cancelPayment } from '../api/paymentApi.js'

const route = useRoute()
const order = ref(null)
const payment = ref(null)
const loading = ref(false)
const actionLoading = ref(false)
const error = ref(null)
const selectedMethod = ref('CARD')

const paymentMethods = [
  { value: 'CARD', label: '카드' },
  { value: 'BANK_TRANSFER', label: '계좌이체' },
  { value: 'DIGITAL_WALLET', label: '디지털지갑' }
]

const canPay = computed(() =>
  order.value && ['PENDING', 'CONFIRMED', 'PAYMENT_PENDING'].includes(order.value.status) && !payment.value
)

const canCancel = computed(() =>
  order.value && ['PENDING', 'CONFIRMED', 'PAYMENT_PENDING'].includes(order.value.status)
)

async function loadAll() {
  loading.value = true
  error.value = null
  try {
    order.value = await getOrder(route.params.id)
    try {
      payment.value = await getPaymentByOrder(order.value.id)
    } catch {
      payment.value = null
    }
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

async function doCreatePayment() {
  actionLoading.value = true
  error.value = null
  try {
    payment.value = await createPayment(order.value.id, order.value.totalAmount, selectedMethod.value)
    await loadAll()
  } catch (e) {
    error.value = e.message
  } finally {
    actionLoading.value = false
  }
}

async function doConfirmPayment() {
  actionLoading.value = true
  error.value = null
  try {
    await confirmPayment(payment.value.id)
    await loadAll()
  } catch (e) {
    error.value = e.message
  } finally {
    actionLoading.value = false
  }
}

async function doCancelPayment() {
  actionLoading.value = true
  error.value = null
  try {
    await cancelPayment(payment.value.id, '사용자 취소')
    await loadAll()
  } catch (e) {
    error.value = e.message
  } finally {
    actionLoading.value = false
  }
}

async function doCancelOrder() {
  actionLoading.value = true
  error.value = null
  try {
    await cancelOrder(order.value.id)
    await loadAll()
  } catch (e) {
    error.value = e.message
  } finally {
    actionLoading.value = false
  }
}

function statusLabel(status) {
  const map = {
    PENDING: '주문접수', CONFIRMED: '주문확인', PAYMENT_PENDING: '결제대기',
    PAID: '결제완료', SHIPPED: '배송중', DELIVERED: '배송완료', CANCELLED: '취소됨'
  }
  return map[status] || status
}

function statusBadge(status) {
  const map = {
    PENDING: 'badge bg-secondary', CONFIRMED: 'badge bg-info',
    PAYMENT_PENDING: 'badge bg-warning text-dark', PAID: 'badge bg-success',
    SHIPPED: 'badge bg-primary', DELIVERED: 'badge bg-success', CANCELLED: 'badge bg-danger'
  }
  return map[status] || 'badge bg-secondary'
}

function paymentLabel(status) {
  const map = { PENDING: '승인대기', APPROVED: '승인완료', CANCELLED: '취소됨', FAILED: '실패' }
  return map[status] || status
}

function paymentBadge(status) {
  const map = {
    PENDING: 'badge bg-warning text-dark', APPROVED: 'badge bg-success',
    CANCELLED: 'badge bg-danger', FAILED: 'badge bg-danger'
  }
  return map[status] || 'badge bg-secondary'
}

function methodLabel(method) {
  const map = { CARD: '카드', BANK_TRANSFER: '계좌이체', DIGITAL_WALLET: '디지털지갑' }
  return map[method] || method
}

function formatDate(dt) {
  if (!dt) return '-'
  return new Date(dt).toLocaleString('ko-KR')
}

onMounted(loadAll)
</script>
