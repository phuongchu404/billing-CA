<template>
  <div class="price">
    <div class="container">
      <div class="text-center text-product">
        <div class="text-container">
          <h2 class="text-blue fw-bold text-product__header">Combo Dịch vụ Chứng thực Chữ ký số</h2>
          <img :src="shadow" class="background-image">
        </div>
      </div>
      <div class="row price-list">
        <div v-for="plan in plans" :key="plan.name" class="col-lg-4 col-md-6">
          <div class="price-card shadow-sm">
            <div class="price-card__icon">
              <img :src="plan.icon" :alt="plan.name">
            </div>
            <h3>{{ plan.name }}</h3>
            <div class="price-card__price">
              <span class="price-card__amount">{{ plan.price }}</span>
              <span class="price-card__period">/mo</span>
            </div>
            <ul>
              <li v-for="feature in plan.features" :key="feature">
                <span class="price-card__check">✓</span>
                <span>{{ feature }}</span>
              </li>
            </ul>
            <button class="price-card__button" type="button" @click="emit('open-dialog')">
              Đăng Ký Ngay
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>

</template>
<script setup lang="ts">
import shadow from "@/assets/images/shadow.svg";
import planeIcon from "@/assets/images/icon-plane.svg";
import planeAltIcon from "@/assets/images/icon-plane1.svg";
import rocketIcon from "@/assets/images/icon-rocket.svg";

const emit = defineEmits(['open-dialog']);

const plans = [
  {
    name: 'Cá nhân',
    price: '$19',
    icon: planeIcon,
    features: [
      'Timeline',
      'Basic search',
      'Live chat widget',
      'Email marketing',
      'Custom Forms',
      'Traffic analytics',
      'Basic Support',
    ],
  },
  {
    name: 'Tổ chức',
    price: '$29',
    icon: planeAltIcon,
    features: [
      'Everything in basic',
      'Timeline with database',
      'Advanced search',
      'Marketing automation',
      'Advanced chatbot',
      'Campaign management',
      'Collaboration tools',
    ],
  },
  {
    name: 'Cá nhân thuộc Tổ chức',
    price: '$49',
    icon: rocketIcon,
    features: [
      'Everything in premium',
      'Timeline with database',
      'Fuzzy search',
      'A/B testing sanbox',
      'Custom permissions',
      'Social media automation',
      'Sales automation tools',
    ],
  },
];
</script>

<style scoped>
.price {
  background-image: linear-gradient(var(--light-1), var(--white));
  min-height: 830px;
  padding-top: 5rem;
  padding-bottom: 2.5rem;
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

.price-list {
  row-gap: 1.5rem;
}

.price-card {
  background: var(--white);
  border: 1px solid var(--light-2);
  border-radius: 0.375rem;
  min-height: 630px;
  padding: 3rem 2rem 1.4rem;
  text-align: left;
}

.price-card__icon {
  height: 115px;
  margin-bottom: 1rem;
  text-align: center;
}

.price-card__icon img {
  height: 95px;
  max-width: 120px;
}

.price-card h3 {
  color: #2f3b4f;
  font-size: 24px;
  font-weight: 500;
  margin-bottom: 0.5rem;
  text-align: center;
}

.price-card__price {
  align-items: baseline;
  display: flex;
  justify-content: center;
  margin-bottom: 2.2rem;
}

.price-card__amount {
  color: var(--blue);
  font-size: 42px;
  font-weight: 700;
  line-height: 1;
}

.price-card__period {
  color: var(--gray);
  font-size: 16px;
  margin-left: 2px;
}

.price-card ul {
  list-style: none;
  margin: 0 0 2rem;
  padding: 0;
}

.price-card li {
  align-items: center;
  color: #111;
  display: flex;
  font-size: 16px;
  gap: 1rem;
  margin-bottom: 1rem;
}

.price-card__check {
  align-items: center;
  background: var(--light-blue);
  border-radius: 50%;
  color: var(--blue);
  display: inline-flex;
  flex: 0 0 16px;
  font-size: 11px;
  font-weight: 700;
  height: 16px;
  justify-content: center;
  width: 16px;
}

.price-card__button {
  background: #fff6df;
  border: none;
  border-radius: 0.375rem;
  color: #ffb400;
  font-size: 16px;
  font-weight: 600;
  min-height: 40px;
  width: 100%;
}

@media (max-width: 768px) {
  .price {
    min-height: auto;
    padding-top: 0;
  }

  .text-product__header {
    font-size: 18px;
  }

  .background-image {
    left: 0;
  }

  .price-card {
    min-height: auto;
    padding: 2rem 1.5rem 1.25rem;
  }
}
</style>
