<template>
  <nav class="navbar navbar-expand navbar-dark bg-primary mb-4">
    <div class="container">
      <router-link to="/" class="navbar-brand fw-bold fs-5">
        <i class="bi bi-bag-heart-fill me-2"></i>SKALA Shop
      </router-link>

      <div class="navbar-nav me-auto">
        <router-link to="/" class="nav-link" active-class="active" exact>
          <i class="bi bi-shop me-1"></i>상품
        </router-link>
        <router-link to="/orders" class="nav-link" active-class="active">
          <i class="bi bi-receipt me-1"></i>주문내역
        </router-link>
        <router-link to="/users" class="nav-link" active-class="active">
          <i class="bi bi-people me-1"></i>사용자
        </router-link>
      </div>

      <div class="d-flex align-items-center gap-3">
        <span v-if="currentUser.id" class="text-white-50 small">
          <i class="bi bi-person-fill me-1"></i>{{ currentUser.name }}
        </span>
        <router-link to="/cart" class="btn btn-light btn-sm position-relative">
          <i class="bi bi-cart3"></i>
          <span v-if="cart.count > 0" class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger">
            {{ cart.count }}
          </span>
        </router-link>
      </div>
    </div>
  </nav>
  <div class="container">
    <router-view />
  </div>
</template>

<script setup>
import { cart } from './store/cart.js'
import { currentUser } from './store/currentUser.js'
</script>
