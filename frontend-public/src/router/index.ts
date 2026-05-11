import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'home',
    component: () => import('@/layout/index.vue'),
    children: [
      { path: '', name: 'home-page', component: () => import('@/views/home/index.vue') },
      { path: 'search', name: 'search-page', component: () => import('@/views/search-certi/index.vue') },
      { path: 'about', name: 'about', component: () => import('@/views/about/index.vue') },
      { path: 'guide', name: 'guide', component: () => import('@/views/guide/index.vue') },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (to.hash) {
      return { el: to.hash, behavior: 'smooth' };
    } else if (savedPosition) {
      return savedPosition;
    } else {
      return { top: 0 };
    }
  },
});

export default router;
