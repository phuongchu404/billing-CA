<template>
  <div>
    <!--  Component  -->
    <div id="intro">
      <Intro @open-dialog="openDialog"/>
    </div>
    <div id="product">
      <Product @open-dialog="openDialog"/>
    </div>
    <div id="review">
      <Review @open-dialog="openDialog"/>
    </div>
    <div id="price">
      <Price @open-dialog="openDialog"/>
    </div>

    <div id="contact">
      <Contact/>
    </div>

    <!-- Nav -->
    <nav id="cd-vertical-nav">
      <ul>
        <li :class="{'is-selected': currentSection === 'intro'}">
          <a href="javascript:void(0)" @click="scrollToSection('intro')">
            <span class="cd-dot"></span>
          </a>
          <span>Giải pháp</span>
        </li>
        <li :class="{'is-selected': currentSection === 'product'}">
          <a href="javascript:void(0)" @click="scrollToSection('product')">
            <span class="cd-dot"></span>
          </a>
          <span>Sản phẩm</span>
        </li>
        <li :class="{'is-selected': currentSection === 'review'}">
          <a href="javascript:void(0)" @click="scrollToSection('review')">
            <span class="cd-dot"></span>
          </a>
          <span>Hướng dẫn</span>
        </li>
        <li :class="{'is-selected': currentSection === 'price'}">
          <a href="javascript:void(0)" @click="scrollToSection('price')">
            <span class="cd-dot"></span>
          </a>
          <span>Báo giá</span>
        </li>
      </ul>
    </nav>

    <DialogComponent :visible="isDialogVisible" @update:visible="isDialogVisible = $event"/>
    <BottomSheet :visible="isBottomSheetVisible" @update:visible="isBottomSheetVisible = $event"/>
  </div>
</template>

<script lang="ts" setup>
import {onMounted, onUnmounted, ref} from 'vue';
import Intro from '@/views/home/components/IntroComponent.vue';
import Product from '@/views/home/components/ProductComponent.vue';
import Review from '@/views/home/components/ReviewComponent.vue';
import Price from '@/views/home/components/PriceComponent.vue';
import Contact from '@/views/home/components/ContactComponent.vue';
import DialogComponent from '@/views/home/components/ContactDialog.vue';
import BottomSheet from "@/views/home/components/BottomSheet.vue";

const currentSection = ref('intro');
const isDialogVisible = ref(false);
const isBottomSheetVisible = ref(false);

const isMobile = () => {
  return window.innerWidth <= 768;
};

const openDialog = () => {
  if (isMobile()) {
    isBottomSheetVisible.value = true;
  } else {
    isDialogVisible.value = true;
  }
};

const scrollToSection = (sectionId: string) => {
  const element = document.getElementById(sectionId);
  if (element) {
    element.scrollIntoView({ behavior: 'smooth' });
    currentSection.value = sectionId;
  }
};

onMounted(() => {
  window.addEventListener('scroll', handleScroll);
});

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll);
});

window.addEventListener('resize', () => {
  isDialogVisible.value = false;
});

function handleScroll() {
  const sections = ['#intro', '#product', '#review', '#price', '#contact'];
  let currentIndex = -1;

  sections.forEach((id, index) => {
    const section = document.querySelector(id);
    if (section) {
      const rect = section.getBoundingClientRect();
      if (rect.top >= 0 && rect.top <= window.innerHeight / 2) {
        currentIndex = index;
      }
    }
  });

  if (currentIndex !== -1) {
    currentSection.value = sections[currentIndex].substring(1);
  }
}

</script>

<style scoped>
a {
  text-decoration: none;
}

#cd-vertical-nav {
  position: fixed;
  right: 30px;
  top: 50% !important;
  bottom: auto;
  -webkit-transform: translateY(-50%);
  -moz-transform: translateY(-50%);
  -ms-transform: translateY(-50%);
  -o-transform: translateY(-50%);
  transform: translateY(-50%);
  z-index: 99;
}

#cd-vertical-nav ul {
  list-style: none;
  padding: 0;
}

#cd-vertical-nav li {
  padding-bottom: 40px;
  position: relative;
  text-align: right;
}

#cd-vertical-nav li:first-child:before {
  content: none;
}

#cd-vertical-nav li:before {
  content: "";
  width: 1px;
  height: 100%;
  background: var(--light-blue-s);
  position: absolute;
  left: 7px;
  top: -70%;
}

#cd-vertical-nav a {
  display: inline-block;
  -webkit-backface-visibility: hidden;
  backface-visibility: hidden;
}

#cd-vertical-nav li .cd-dot {
  background-color: var(--light-blue-s);
  transition: all .3s;
  position: relative;
  z-index: 1;
  top: 7px;
  height: 16px;
  width: 16px;
  clip-path: polygon(50% 0%, 100% 25%, 100% 75%, 50% 100%, 0% 75%, 0% 25%);
  transform-origin: 50% 50%;
  left: 0;
  float: right;
  display: inline-block;
}

#cd-vertical-nav li.is-selected .cd-dot,
#cd-vertical-nav li:hover .cd-dot {
  background-color: var(--light-blue-3);
}

#cd-vertical-nav li.is-selected span:not(.cd-dot),
#cd-vertical-nav li:hover span:not(.cd-dot) {
  opacity: 1;
  visibility: visible;
  -moz-animation: cssAnimation 0s ease-in 1s forwards;
  -webkit-animation: cssAnimation 0s ease-in 1s forwards;
  -o-animation: cssAnimation 0s ease-in 1s forwards;
  animation: cssAnimation 0s ease-in 1s forwards;
  -webkit-animation-fill-mode: forwards;
  animation-fill-mode: forwards;
}

#cd-vertical-nav span:not(.cd-dot) {
  visibility: hidden;
  opacity: 0;
  display: inline-flex;
  align-items: center;
  height: 34px;
  right: 12px;
  top: 0;
  font-weight: 500;
  font-size: 16px;
  color: var(--blue);
  text-align: center;
  line-height: 34px;
  padding: 0 15px;
  position: absolute;
  white-space: nowrap;
  transition: all .5s;
}

@media (max-width: 991px) {
  #cd-vertical-nav {
    display: none;
  }
}

@media (min-width: 992px) and (min-width: 1701px) {
  #cd-vertical-nav {
    right: 7vw;
  }
}
</style>
