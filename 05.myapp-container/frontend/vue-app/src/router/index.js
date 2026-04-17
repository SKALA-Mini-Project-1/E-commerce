import { createRouter, createWebHistory } from 'vue-router'
import ProductsView from '../views/ProductsView.vue'
import CartView from '../views/CartView.vue'
import OrdersView from '../views/OrdersView.vue'
import OrderDetailView from '../views/OrderDetailView.vue'
import UserView from '../views/UserView.vue'

const routes = [
  { path: '/', component: ProductsView },
  { path: '/cart', component: CartView },
  { path: '/orders', component: OrdersView },
  { path: '/orders/:id', component: OrderDetailView },
  { path: '/users', component: UserView }
]

export default createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})
