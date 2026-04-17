<template>
  <div>
    <div class="d-flex align-items-center mb-4 gap-3">
      <h2 class="mb-0">
        <i class="bi bi-people-fill me-2 text-primary"></i>사용자 관리
      </h2>
      <button class="btn btn-primary" @click="loadUsers" :disabled="loading">
        <span v-if="loading" class="spinner-border spinner-border-sm me-1" role="status"></span>
        <i v-else class="bi bi-search me-1"></i>사용자 검색
      </button>
    </div>

    <div v-if="currentUser.id" class="alert alert-success d-flex align-items-center justify-content-between">
      <span><i class="bi bi-person-check-fill me-2"></i>현재 사용자: <strong>{{ currentUser.name }}</strong> (ID: {{ currentUser.id }})</span>
      <button class="btn btn-sm btn-outline-danger" @click="currentUser.clear()">로그아웃</button>
    </div>

    <div v-if="error" class="alert alert-danger d-flex align-items-center" role="alert">
      <i class="bi bi-exclamation-triangle-fill me-2"></i>{{ error }}
    </div>

    <div v-if="users.length > 0" class="card shadow-sm">
      <div class="card-header bg-dark text-white d-flex justify-content-between align-items-center">
        <span><i class="bi bi-table me-2"></i>사용자 목록</span>
        <span class="badge bg-primary fs-6">총 {{ users.length }}명</span>
      </div>
      <div class="card-body p-0">
        <table class="table table-hover table-striped mb-0">
          <thead class="table-secondary">
            <tr>
              <th style="width: 80px;">#</th>
              <th>이름</th>
              <th>이메일</th>
              <th style="width: 120px;"></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in users" :key="user.id">
              <td class="text-muted align-middle">{{ user.id }}</td>
              <td class="align-middle">
                <i class="bi bi-person-circle me-1 text-primary"></i>{{ user.name }}
              </td>
              <td class="align-middle">
                <i class="bi bi-envelope me-1 text-secondary"></i>{{ user.email }}
              </td>
              <td class="align-middle">
                <button
                  class="btn btn-sm"
                  :class="currentUser.id === user.id ? 'btn-success' : 'btn-outline-primary'"
                  @click="selectUser(user)"
                >
                  <i class="bi bi-person-check me-1"></i>
                  {{ currentUser.id === user.id ? '선택됨' : '선택' }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-else-if="!loading && searched" class="alert alert-info d-flex align-items-center">
      <i class="bi bi-info-circle-fill me-2"></i>검색된 사용자가 없습니다.
    </div>

    <div v-else-if="!loading && !searched" class="text-center text-muted py-5">
      <i class="bi bi-search display-4 d-block mb-3 text-primary opacity-50"></i>
      <p class="fs-5">"사용자 검색" 버튼을 클릭하여 사용자를 조회하세요.</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { getUsers } from '../api/userApi.js'
import { currentUser } from '../store/currentUser.js'

const users = ref([])
const loading = ref(false)
const error = ref(null)
const searched = ref(false)

async function loadUsers() {
  loading.value = true
  error.value = null
  try {
    users.value = await getUsers()
    searched.value = true
  } catch (e) {
    error.value = e.message
    users.value = []
  } finally {
    loading.value = false
  }
}

function selectUser(user) {
  currentUser.set(user)
}
</script>
