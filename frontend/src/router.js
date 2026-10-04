import { createRouter, createWebHistory } from 'vue-router'
import { useAuth } from './store/auth.js'

const routes = [
  { path: '/login', component: () => import('./views/Login.vue'), meta: { public: true } },
  { path: '/public', component: () => import('./views/public/PublicHome.vue'), meta: { public: true } },
  { path: '/public/events/:id', component: () => import('./views/public/PublicDetail.vue'), meta: { public: true } },
  { path: '/checkin', component: () => import('./views/Checkin.vue'), meta: { public: true, title: '扫码核销' } },
  {
    path: '/',
    component: () => import('./layouts/MainLayout.vue'),
    children: [
      { path: '', redirect: '/home' },
      { path: 'home', component: () => import('./views/Home.vue'), title: '工作台' },

      // 摊主
      { path: 'vendor/apply/:eventId?', component: () => import('./views/vendor/VendorApply.vue'), meta: { role: 'VENDOR' }, title: '摊位报名' },
      { path: 'vendor/pass/:eventId?', component: () => import('./views/vendor/VendorPass.vue'), meta: { role: 'VENDOR' }, title: '我的入场凭证' },

      // 管理/工作人员
      { path: 'admin/events', component: () => import('./views/admin/Events.vue'), meta: { role: 'ADMIN' }, title: '活动与摊位管理' },
      { path: 'admin/vendors', component: () => import('./views/admin/Vendors.vue'), meta: { role: 'ADMIN' }, title: '摊主信用档案' },
      { path: 'review', component: () => import('./views/staff/Review.vue'), meta: { role: 'STAFF' }, title: '报名审核' },
      { path: 'assign', component: () => import('./views/staff/Assign.vue'), meta: { role: 'STAFF' }, title: '摊位分配' },
      { path: 'onsite', component: () => import('./views/staff/OnSite.vue'), meta: { role: 'STAFF' }, title: '现场核销与巡检' },
      { path: 'finance', component: () => import('./views/staff/Finance.vue'), meta: { role: 'STAFF' }, title: '收款与捐赠' },
      { path: 'complaints', component: () => import('./views/staff/Complaints.vue'), meta: { role: 'STAFF' }, title: '投诉处理' },
      { path: 'issues', component: () => import('./views/staff/Issues.vue'), meta: { role: 'STAFF' }, title: '异常管理' },
      { path: 'traffic', component: () => import('./views/staff/Traffic.vue'), meta: { role: 'STAFF' }, title: '客流统计' },
      { path: 'archive', component: () => import('./views/staff/Archive.vue'), meta: { role: 'STAFF' }, title: '活动统一档案' },
      { path: 'dashboard', component: () => import('./views/staff/Dashboard.vue'), meta: { role: 'STAFF' }, title: '复盘与公示' }
    ]
  }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to) => {
  const auth = useAuth()
  if (to.meta.public) return true
  if (!auth.isLogin.value) return '/login'
  if (to.meta.role === 'VENDOR' && !auth.isVendor.value) return '/home'
  if (to.meta.role === 'STAFF' && !auth.isStaff.value) return '/home'
  return true
})

export default router
