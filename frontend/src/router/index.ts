import { createRouter, createWebHistory } from "vue-router";
import { getToken, clearAuth, isTokenExpired } from "@/utils/auth";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: "/login",
      name: "Login",
      component: () => import("@/views/auth/Login.vue"),
      meta: { requiresAuth: false },
    },
    {
      path: "/",
      component: () => import("@/layout/index.vue"),
      redirect: "/dashboard",
      meta: { requiresAuth: true },
      children: [
        {
          path: "dashboard",
          name: "Dashboard",
          component: () => import("@/views/home/index.vue"),
          meta: { title: "Trang chủ", icon: "Odometer" },
        },
        {
          path: "plans",
          name: "Plans",
          component: () => import("@/views/plans/index.vue"),
          meta: { title: "Plans", icon: "Ticket", permission: "group:view" },
        },
        {
          path: "plans/new",
          name: "AgencyCreate",
          component: () => import("@/views/plans/Create.vue"),
          meta: { title: "Thêm mới đại lý", permission: "group:create" },
        },
        {
          path: "plans/:id",
          name: "AgencyDetail",
          component: () => import("@/views/plans/Detail.vue"),
          meta: { title: "Thông tin đại lý", permission: "group:view" },
        },
        {
          path: "plans/:id/add-plan",
          name: "AgencyAddPlan",
          component: () => import("@/views/plans/AddPlan.vue"),
          meta: { title: "Thêm mới gói cước", permission: "group:update" },
        },
        {
          path: "individual-plan-config",
          name: "IndividualPlanConfig",
          component: () => import("@/views/individual/PlanConfig.vue"),
          meta: { title: "Cấu hình gói cước", permission: "plan:view" },
        },
        {
          path: "individual-plan-config/new",
          name: "IndividualPlanConfigCreate",
          component: () => import("@/views/individual/PlanConfigCreate.vue"),
          meta: { title: "Thêm mới gói cước", permission: "plan:create" },
        },
        {
          path: "individual-plan-config/:id",
          name: "IndividualPlanConfigDetail",
          component: () => import("@/views/individual/PlanConfigDetail.vue"),
          meta: { title: "Chi tiết gói cước", permission: "plan:view" },
        },
        {
          path: "individual-usage-tracking",
          name: "IndividualUsageTracking",
          component: () => import("@/views/individual/UsageTracking.vue"),
          meta: {
            title: "Theo dõi sử dụng",
            permission: "individual:usage:view",
          },
        },
        {
          path: "users",
          name: "Users",
          component: () => import("@/views/users/index.vue"),
          meta: { title: "Users", icon: "User", permission: "user:view" },
        },
        {
          path: "roles",
          name: "Roles",
          component: () => import("@/views/roles/index.vue"),
          meta: { title: "Roles", icon: "Lock", permission: "role:view" },
        },
        {
          path: "reports",
          name: "Reports",
          component: () => import("@/views/reports/index.vue"),
          meta: {
            title: "Reports",
            icon: "TrendCharts",
            permission: "report:view",
          },
        },
        // {
        //   path: "partner-access",
        //   name: "PartnerAccess",
        //   component: () => import("@/views/partner/AccessManagement.vue"),
        //   meta: { title: "Quyền truy cập đối tác", permission: "partner:access:grant" },
        // },
        {
          path: "approvals",
          name: "ApprovalList",
          component: () => import("@/views/approvals/ApprovalList.vue"),
          meta: { title: "Phê duyệt", permission: "approval:view" },
        },
        {
          path: "approvals/:id",
          name: "ApprovalDetail",
          component: () => import("@/views/approvals/ApprovalDetail.vue"),
          meta: { title: "Chi tiết phê duyệt", permission: "approval:view" },
        },
        {
          path: "audit-logs",
          name: "AuditLogs",
          component: () => import("@/views/audit-logs/index.vue"),
          meta: { title: "Audit Logs", permission: "audit-log:view" },
        },
        {
          path: "profile",
          name: "Profile",
          component: () => import("@/views/profile/index.vue"),
          meta: { title: "My Profile" },
        },
      ],
    },
    { path: "/:pathMatch(.*)*", redirect: "/dashboard" },
  ],
});

router.beforeEach((_to, _from, next) => {
  next();
});

export default router;
