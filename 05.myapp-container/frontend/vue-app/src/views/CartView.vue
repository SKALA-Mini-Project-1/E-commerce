<template>
  <div>
    <h2 class="mb-4"><i class="bi bi-cart3 me-2 text-primary"></i>장바구니</h2>

    <div v-if="!currentUser.id" class="alert alert-warning">
      <i class="bi bi-person-exclamation me-2"></i>
      주문하려면 먼저 <router-link to="/users">사용자 페이지</router-link>에서 사용자를 선택하세요.
    </div>

    <div v-if="cart.items.length === 0" class="text-center text-muted py-5">
      <i class="bi bi-cart display-4 d-block mb-3 opacity-50"></i>
      <p>장바구니가 비어있습니다.</p>
      <router-link to="/" class="btn btn-primary">
        <i class="bi bi-shop me-1"></i>쇼핑하러 가기
      </router-link>
    </div>

    <div v-else>
      <div class="card shadow-sm mb-4">
        <div class="card-body p-0">
          <table class="table table-hover mb-0">
            <thead class="table-light">
              <tr>
                <th>상품명</th>
                <th class="text-center">수량</th>
                <th class="text-end">단가</th>
                <th class="text-end">소계</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in cart.items" :key="item.productId">
                <td class="align-middle">{{ item.productName }}</td>
                <td class="text-center align-middle">
                  <div class="btn-group btn-group-sm">
                    <button class="btn btn-outline-secondary" @click="decrease(item)">-</button>
                    <span class="btn btn-outline-secondary disabled">{{ item.quantity }}</span>
                    <button class="btn btn-outline-secondary" @click="item.quantity++">+</button>
                  </div>
                </td>
                <td class="text-end align-middle">{{ item.price.toLocaleString() }}원</td>
                <td class="text-end align-middle fw-bold">{{ (item.price * item.quantity).toLocaleString() }}원</td>
                <td class="text-center align-middle">
                  <button class="btn btn-sm btn-outline-danger" @click="cart.remove(item.productId)">
                    <i class="bi bi-trash"></i>
                  </button>
                </td>
              </tr>
            </tbody>
            <tfoot class="table-light">
              <tr>
                <td colspan="3" class="text-end fw-bold">합계</td>
                <td class="text-end fw-bold text-primary fs-5">{{ cart.total.toLocaleString() }}원</td>
                <td></td>
              </tr>
            </tfoot>
          </table>
        </div>
      </div>

      <div v-if="error" class="alert alert-danger">
        <i class="bi bi-exclamation-triangle-fill me-2"></i>{{ error }}
      </div>

      <div class="d-flex justify-content-between">
        <button class="btn btn-outline-secondary" @click="cart.clear()">
          <i class="bi bi-trash me-1"></i>장바구니 비우기
        </button>
        <button
          class="btn btn-primary px-4"
          :disabled="!currentUser.id || ordering"
          @click="placeOrder"
        >
          <span v-if="ordering" class="spinner-border spinner-border-sm me-1"></span>
          <i v-else class="bi bi-bag-check me-1"></i>
          주문하기
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { cart } from '../store/cart.js'
import { currentUser } from '../store/currentUser.js'
import { createOrder } from '../api/orderApi.js'

const router = useRouter()
const ordering = ref(false)
const error = ref(null)

function decrease(item) {
  if (item.quantity > 1) {
    item.quantity--
  } else {
    cart.remove(item.productId)
  }
}

async function placeOrder() {
  ordering.value = true
  error.value = null
  try {
    const items = cart.items.map(i => ({ productId: i.productId, quantity: i.quantity }))
    const order = await createOrder(currentUser.id, items)
    cart.clear()
    router.push(`/orders/${order.id}`)
  } catch (e) {
    error.value = e.message
  } finally {
    ordering.value = false
  }
}
</script>
