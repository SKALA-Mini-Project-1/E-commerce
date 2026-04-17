<template>
  <div>
    <div class="d-flex align-items-center mb-4 gap-3">
      <h2 class="mb-0">
        <i class="bi bi-shop me-2 text-primary"></i>상품 목록
      </h2>
      <button class="btn btn-outline-primary" @click="loadProducts" :disabled="loading">
        <span v-if="loading" class="spinner-border spinner-border-sm me-1"></span>
        <i v-else class="bi bi-arrow-clockwise me-1"></i>새로고침
      </button>
    </div>

    <div v-if="error" class="alert alert-danger">
      <i class="bi bi-exclamation-triangle-fill me-2"></i>{{ error }}
    </div>

    <div v-if="addedMessage" class="alert alert-success alert-dismissible">
      <i class="bi bi-cart-check me-2"></i>{{ addedMessage }}
    </div>

    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary"></div>
    </div>

    <div v-else-if="products.length === 0 && !error" class="text-center text-muted py-5">
      <i class="bi bi-inbox display-4 d-block mb-3 opacity-50"></i>
      <p>상품이 없습니다.</p>
    </div>

    <div v-else class="row row-cols-1 row-cols-md-3 g-4">
      <div v-for="product in products" :key="product.id" class="col">
        <div class="card h-100 shadow-sm">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-start mb-2">
              <h5 class="card-title mb-0">{{ product.name }}</h5>
              <span :class="product.active ? 'badge bg-success' : 'badge bg-secondary'">
                {{ product.active ? '판매중' : '판매종료' }}
              </span>
            </div>
            <p class="text-muted small mb-1">SKU: {{ product.sku }}</p>
            <p class="card-text text-muted small">{{ product.description }}</p>
          </div>
          <div class="card-footer bg-white d-flex justify-content-between align-items-center">
            <div>
              <div class="fw-bold text-primary fs-5">{{ product.price.toLocaleString() }}원</div>
              <small class="text-muted">재고: {{ product.stockQuantity }}개</small>
            </div>
            <button
              class="btn btn-primary btn-sm"
              :disabled="!product.active || product.stockQuantity === 0"
              @click="addToCart(product)"
            >
              <i class="bi bi-cart-plus me-1"></i>담기
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getProducts } from '../api/productApi.js'
import { cart } from '../store/cart.js'

const products = ref([])
const loading = ref(false)
const error = ref(null)
const addedMessage = ref(null)

async function loadProducts() {
  loading.value = true
  error.value = null
  try {
    products.value = await getProducts()
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

function addToCart(product) {
  cart.add(product)
  addedMessage.value = `"${product.name}" 을(를) 장바구니에 담았습니다.`
  setTimeout(() => { addedMessage.value = null }, 2000)
}

onMounted(loadProducts)
</script>
