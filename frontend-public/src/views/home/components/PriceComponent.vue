<template>
  <div class="price">
    <div class="container">
      <div class="text-center text-product">
        <div class="text-container">
          <h2 class="text-blue fw-bold text-product__header">Combo Dịch vụ Chứng thực Chữ ký số</h2>
          <img :src="shadow" class="background-image">
        </div>
      </div>
    </div>
  </div>

</template>
<script setup lang="ts">
import shadow from "@/assets/images/shadow.svg";
</script>

<style scoped>
.price {
  background-image: linear-gradient(var(--light-1), var(--white));
  height: 600px;
  padding-top: 5rem;
}

.text-product {
  margin-bottom: 2.5rem;
}

.text-container {
  position: relative;
}

.background-image {
  position: absolute;
  top: 75%;
  left: 25%;
}

.text-product__header {
  position: relative;
  z-index: 1;
}

@media (max-width: 768px) {
  .price {
    padding-top: 0;
  }

  .text-product__header {
    font-size: 18px;
  }

  .background-image {
    left: 0;
  }
}
</style>