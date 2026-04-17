import { reactive } from 'vue'

export const cart = reactive({
  items: [],

  add(product) {
    const existing = this.items.find(i => i.productId === product.id)
    if (existing) {
      existing.quantity++
    } else {
      this.items.push({
        productId: product.id,
        productName: product.name,
        price: product.price,
        quantity: 1
      })
    }
  },

  remove(productId) {
    const idx = this.items.findIndex(i => i.productId === productId)
    if (idx > -1) this.items.splice(idx, 1)
  },

  clear() {
    this.items = []
  },

  get total() {
    return this.items.reduce((sum, i) => sum + i.price * i.quantity, 0)
  },

  get count() {
    return this.items.reduce((sum, i) => sum + i.quantity, 0)
  }
})
