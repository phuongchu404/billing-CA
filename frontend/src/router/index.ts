import { createRouter, createWebHistory } from 'vue-router'
import { getToken, clearAuth, isTokenExpired } from '@/utils/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/auth/Login.vue'),
      meta: { requiresAuth: false },
    },
    {
      path: '/',
      component: () => import('@/layout/index.vue'),
      redirect: '/dashboard',
      meta: { requiresAuth: true },
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/home/index.vue'),
          meta: { title: 'Trang chủ', icon: 'Odometer' },
        },
        {
          path: 'plans',
          name: 'Plans',
          component: () => import('@/views/plans/index.vue'),
          meta: { title: 'Plans', icon: 'Ticket' },
        },
        {
          path: 'plans/new',
          name: 'AgencyCreate',
          component: () => import('@/views/plans/Create.vue'),
          meta: { title: 'Thêm mới đại lý' },
        },
        {
          path: 'plans/:id',
          name: 'AgencyDetail',
          component: () => import('@/views/plans/Detail.vue'),
          meta: { title: 'Thông tin đại lý' },
        },
        {
          path: 'plans/:id/add-plan',
          name: 'AgencyAddPlan',
          component: () => import('@/views/plans/AddPlan.vue'),
          meta: { title: 'Thêm mới gói cước' },
        },
        {
          path: 'subscriptions',
          name: 'Subscriptions',
          component: () => import('@/views/subscriptions/index.vue'),
          meta: { title: 'Subscriptions', icon: 'DocumentChecked' },
        },
        {
          path: 'partners',
          name: 'Partners',
          component: () => import('@/views/groups/index.vue'),
          meta: { title: 'Partners', icon: 'UserFilled' },
        },
        {
          path: 'partners/new',
          name: 'PartnerCreate',
          component: () => import('@/views/groups/Create.vue'),
          meta: { title: 'New Partner', icon: 'UserFilled' },
        },
        {
          path: 'partners/:id',
          name: 'PartnerDetail',
          component: () => import('@/views/groups/Detail.vue'),
          meta: { title: 'Partner Detail', icon: 'UserFilled' },
        },
        {
          path: 'individual-plans',
          name: 'IndividualPlans',
          component: () => import('@/views/individual/Plans.vue'),
          meta: { title: 'Individual Plans' },
        },
        {
          path: 'individual-subscriptions',
          name: 'IndividualSubscriptions',
          component: () => import('@/views/individual/Subscriptions.vue'),
          meta: { title: 'Individual Subscriptions' },
        },
        {
          path: 'individual-plan-config',
          name: 'IndividualPlanConfig',
          component: () => import('@/views/individual/PlanConfig.vue'),
          meta: { title: 'Cấu hình gói cước' },
        },
        {
          path: 'individual-plan-config/new',
          name: 'IndividualPlanConfigCreate',
          component: () => import('@/views/individual/PlanConfigCreate.vue'),
          meta: { title: 'Thêm mới gói cước' },
        },
        {
          path: 'individual-plan-config/:id',
          name: 'IndividualPlanConfigDetail',
          component: () => import('@/views/individual/PlanConfigDetail.vue'),
          meta: { title: 'Chi tiết gói cước' },
        },
        {
          path: 'individual-usage-tracking',
          name: 'IndividualUsageTracking',
          component: () => import('@/views/individual/UsageTracking.vue'),
          meta: { title: 'Theo dõi sử dụng' },
        },
        {
          path: 'users',
          name: 'Users',
          component: () => import('@/views/users/index.vue'),
          meta: { title: 'Users', icon: 'User', roles: ['ROLE_ADMIN'] },
        },
        {
          path: 'roles',
          name: 'Roles',
          component: () => import('@/views/roles/index.vue'),
          meta: { title: 'Roles', icon: 'Lock', roles: ['ROLE_ADMIN'] },
        },
        {
          path: 'certificates',
          name: 'Certificates',
          component: () => import('@/views/certificates/index.vue'),
          meta: { title: 'Certificates', icon: 'Key' },
        },
        {
          path: 'reports',
          name: 'Reports',
          component: () => import('@/views/reports/index.vue'),
          meta: { title: 'Reports', icon: 'TrendCharts', roles: ['ROLE_ADMIN'] },
        },
        {
          path: 'approvals',
          name: 'Approvals',
          component: () => import('@/views/approvals/index.vue'),
          meta: { title: 'Approvals' },
        },
        {
          path: 'settings',
          name: 'Settings',
          component: () => import('@/views/settings/index.vue'),
          meta: { title: 'Settings' },
        },
        {
          path: 'audit-logs',
          name: 'AuditLogs',
          component: () => import('@/views/audit-logs/index.vue'),
          meta: { title: 'Audit Logs', roles: ['ROLE_ADMIN'] },
        },
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('@/views/profile/index.vue'),
          meta: { title: 'My Profile' },
        },
      ],
    },
    { path: '/:pathMatch(.*)*', redirect: '/dashboard' },
  ],
})

router.beforeEach((_to, _from, next) => {
  next()
})

export default router
