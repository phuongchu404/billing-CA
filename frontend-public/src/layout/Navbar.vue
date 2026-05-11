<template>
  <header class="header d-flex align-items-center shadow-sm">
    <div class="container">
      <div class="row align-items-center">
        <div class="col-8 col-xl-9 d-flex align-items-center header-left">
          <router-link to="/" class="main-logo">
            <img :src="Logo" alt="MK-CA" class="fade show">
          </router-link>
          <!-- Menu -->
          <ul class="main-menu">
            <li v-for="(item, index) in menu" :key="index">
              <template v-if="item.router">
                <router-link :to="item.link" exact-active-class="active">{{ item.label }}</router-link>
              </template>
              <template v-else>
                <a :href="item.link" target="_blank">{{ item.label }}</a>
              </template>
            </li>
          </ul>
        </div>

        <div class="col-2 col-xl-2 text-end btn-regis">
          <button class="btn btn-primary rounded-pill bg-blue" @click="openDialog">Đăng Ký Ngay</button>
        </div>
        <div class="col-2 col-xl-1 text-end btn-hamburger">
          <button class="btn btn-outline-primary" @click="toggleSidebar">☰</button>
        </div>
      </div>
    </div>

    <!-- Sidebar -->
    <el-drawer v-model="isSidebarOpen" direction="rtl" size="60%" :with-header="false">
      <!-- <ul class="mobile-menu">
        <li v-for="(item, index) in menu" :key="index">
          <template v-if="item.router">
            <router-link :to="item.link" class="nav-link" exact-active-class="active" @click="handleLinkClick">{{ item.label }}</router-link>
          </template>
          <template v-else>
            <a :href="item.link" target="_blank" class="nav-link">{{ item.label }}</a>
          </template>
          <ul v-if="item.child" class="mobile-menu_child">
            <li v-for="child in item.child">
              <a href="javascript:void(0);" @click.prevent="handleLinkClick(child)">{{child.label}}</a>
            </li>
            <li>
              <a href="/#contact" class="text-contact" >Đăng Ký Ngay</a>
            </li>
          </ul>
        </li>
      </ul> -->

      <el-menu>
        <div v-for="(item, index) in menu" :key="index">
          <div v-if="item.child" v-for="child in item.child">
            <el-menu-item :index="child.link">
              <span slot="title">
                <div   @click="handleLinkClick(child)" >{{child.label}}</div>
              </span>
            </el-menu-item>
          </div>
          <el-menu-item :index="item.link" v-if="item.router">
            <span slot="title">
              <router-link :to="item.link" class="nav-link" exact-active-class="active" @click="handleLinkClick">{{
                item.label }}</router-link>
            </span>
          </el-menu-item>
          <el-menu-item :index="item.link" v-else="item.router">
            <span slot="title">
              <a :href="item.link" target="_blank" class="nav-link">{{ item.label }}</a>
            </span>
          </el-menu-item>
        </div>
      </el-menu>

    </el-drawer>

    <div class="overlay" v-if="isSidebarOpen" @click="toggleSidebar"></div>
  </header>
  <DialogComponent :visible="isDialogVisible" @update:visible="isDialogVisible = $event" />

</template>

<script lang="ts" setup>
import Logo from '@/assets/images/logo.svg';
import { ref } from "vue";
import DialogComponent from "@/views/home/components/ContactDialog.vue";
import router from "@/router";

const menu = ref([
  {
    label: 'Giới thiệu', link: '/', router: true, child: [
      { label: "Giải pháp", link: "#intro" },
      { label: "Sản phẩm", link: "#product" },
      { label: "Hướng dẫn", link: "#review" },
      { label: "Báo giá", link: "#price" },
    ]
  },
  { label: 'Tra cứu Chứng thư CKS', link: '/search', router: true },
  { label: 'Tài liệu hướng dẫn', link: '/guide', router: true },
  { label: 'Về chúng tôi', link: '/about', router: true },
]);

const isDialogVisible = ref(false);

const isSidebarOpen = ref(false);

const openDialog = () => {
  isDialogVisible.value = true;
};

const toggleSidebar = () => {
  isSidebarOpen.value = !isSidebarOpen.value;
};

const handleLinkClick = (item?: { link?: string }) => {
  isSidebarOpen.value = false;
  if (item && item.link && item.link.startsWith('#')) {
    if (window.location.pathname !== '/') {
      router.push({ path: '/', hash: item.link });
    } else {
      const targetLink = item.link;
      setTimeout(() => {
        window.location.href = targetLink;
      },500)
    }
  } else if (item && item.link) {
    router.push(item.link);
  }
}
</script>

<style scoped>
a {
  text-decoration: none;
  color: #666973;
}

.header {
  position: sticky;
  background: var(--white);
  top: 0;
  left: 0;
  z-index: 999;
  height: 60px;
  width: 100%;
}

.header-left {
  justify-content: space-between !important;
}

.header .main-logo {
  margin-top: 0;
  flex-shrink: 0;
  margin-right: 50px;
}

.header .main-logo img {
  height: 37px;
}

.fade {
  transition: opacity .15s linear;
}

.header .main-menu {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
}

.header .main-menu li {
  margin-left: 10px;
  position: relative;
}

.header .main-menu li a {
  font-size: 1rem;
  line-height: 40px;
  padding: 10px 20px;
}

.header .main-menu li .active {
  box-shadow: rgba(99, 99, 99, 0.2) 0px 2px 8px 0px;
  border-radius: 0.5rem;
  color: var(--blue);
}

.btn-hamburger {
  display: none;
}

::v-deep .el-drawer__body {
  padding: 0;
}

/* Mobile menu */
.mobile-menu {
  list-style: none;
  padding: 0;
}

.mobile-menu li .nav-link {
  font-size: 1rem;
  color: var(--dark-gray);
  padding: 20px;
  text-align: left;
  border-bottom: 1px solid var(--light-blue);
}

.mobile-menu .nav-link.active {
  background-image: linear-gradient(to right, var(--blue-2), var(--light-blue-1));
  color: var(--white);
}

.mobile-menu_child {
  list-style-type: none;
  text-align: start;
  border-bottom: 1px solid var(--light-blue);
}

.mobile-menu_child li {
  padding: 1rem 0;
}

.text-contact {
  background: linear-gradient(to right, var(--light-blue-2) 0%, var(--blue-3) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0;
  display: inline-block;
}

/* Hide main menu on mobile */
@media (max-width: 768px) {
  .header .main-menu {
    display: none;
  }

  .btn-hamburger {
    display: block;
  }
}

@media (min-width: 1024px) {
  .col-8 {
    width: 80%;
  }

  .header .main-menu li a {
    padding: 8px 20px;
  }
}

@media (max-width: 768px) {
  .col-8 {
    width: 80%;
  }

  .btn-regis {
    display: none;
  }
}
</style>
