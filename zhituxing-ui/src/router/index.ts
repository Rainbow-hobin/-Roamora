import { createRouter, createWebHistory } from 'vue-router'
import MessagesView from '@/views/MessagesView.vue'
import HomeView from '@/views/HomeView.vue'
import SpotsView from '@/views/SpotsView.vue'
import DetailView from '@/views/DetailView.vue'
import PlanView from '@/views/PlanView.vue'
import CommunityView from '@/views/CommunityView.vue'
import PostDetailView from '@/views/PostDetailView.vue'
import TripDetailView from '@/views/TripDetailView.vue'
import NotesView from '@/views/NotesView.vue'
import SettingsView from '@/views/SettingsView.vue'
import ProfileView from '@/views/ProfileView.vue'
import LoginView from '@/views/LoginView.vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import DashboardView from '@/views/admin/DashboardView.vue'
import SpotsManageView from '@/views/admin/SpotsManageView.vue'
import UsersManageView from '@/views/admin/UsersManageView.vue'
import ReviewsManageView from '@/views/admin/ReviewsManageView.vue'
import { getToken } from '@/api/http'
import { userState } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/spots', name: 'spots', component: SpotsView },
    { path: '/spots/:id', name: 'detail', component: DetailView, props: true },
    { path: '/plan', name: 'plan', component: PlanView },
    { path: '/trips/:id', name: 'trip-detail', component: TripDetailView },
    { path: '/community', name: 'community', component: CommunityView },
    { path: '/community/:id', name: 'post-detail', component: PostDetailView },
    { path: '/notes', name: 'notes', component: NotesView },
    { path: '/settings', name: 'settings', component: SettingsView },
    { path: '/messages', name: 'messages', component: MessagesView, meta: { requiresAuth: true } },
    { path: '/profile', name: 'profile', component: ProfileView },
    { path: '/login', name: 'login', component: LoginView, meta: { bare: true } },
    {
      path: '/admin',
      component: AdminLayout,
      meta: { bare: true, requiresAdmin: true },
      children: [
        { path: '', name: 'admin-dashboard', component: DashboardView },
        { path: 'spots', name: 'admin-spots', component: SpotsManageView },
        { path: 'users', name: 'admin-users', component: UsersManageView },
        { path: 'reviews', name: 'admin-reviews', component: ReviewsManageView },
      ],
    },
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
})

router.beforeEach((to) => {
  if (to.meta.requiresAuth && !getToken()) return { path:'/login', query:{redirect:to.fullPath} }
  if (to.meta.requiresAdmin) {
    if (!getToken()) return { path: '/login', query: { redirect: to.fullPath } }
    if (userState.role !== 'admin') return { path: '/profile' }
  }
})

export default router
