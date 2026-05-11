<template>
  <Navbar />
  <router-view />
  <Footer/>
</template>

<script lang="ts" setup>
import Navbar from '@/layout/Navbar.vue';
import Footer from '@/layout/Footer.vue';
</script>
