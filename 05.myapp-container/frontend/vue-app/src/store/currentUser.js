import { reactive } from 'vue'

export const currentUser = reactive({
  id: localStorage.getItem('userId') ? Number(localStorage.getItem('userId')) : null,
  name: localStorage.getItem('userName') || null,

  set(user) {
    this.id = user.id
    this.name = user.name
    localStorage.setItem('userId', user.id)
    localStorage.setItem('userName', user.name)
  },

  clear() {
    this.id = null
    this.name = null
    localStorage.removeItem('userId')
    localStorage.removeItem('userName')
  }
})
