<template>
  <div class="product">
    <div class="container">
      <div class="text-center text-product">
        <p>Giải pháp được cung cấp bởi MK Hi-tek</p>
        <div class="text-container">
          <h2 class="text-blue fw-bold text-product__header">Chứng thực Chữ ký số Công cộng</h2>
          <img :src="shadow" class="background-image">
        </div>
      </div>
      <div class="d-flex flex-wrap">
        <div class="product-item" v-for="(item, index) in productItems"
             :key="index"
             :id="`product-item-${index}`"
             :class="{ visible: itemProductVisible[index] }">
          <div class="product-item__img">
            <img :src="item.icon" alt="">
          </div>
          <div class="product-item__text">
            <h5 class="fw-bold">{{ item.text }}</h5>
            <p v-html="item.desc"></p>
          </div>
        </div>
      </div>
      <button class="btn btn-primary rounded-pill bg-blue px-4 py-3"
              @click="triggerOpenDialog"
              :class="{ visible: itemProductVisible[itemProductVisible.length - 1]}"
      >
        Đăng Ký Ngay
      </button>
    </div>
  </div>
  <!--  Advantage  -->
  <div class="advantage">
    <div class="container">
      <h2 class="text-blue fw-bold">Ưu điểm nổi bật</h2>
      <div class="d-flex flex-wrap">
        <div class="advantage-text d-flex flex-column justify-content-center" id="advantage-text" :class="{ visible: itemAdvantageVisible[0] }">
          <div class="advantage-item text-white"
               v-for="(item, index) in advantage">
            <h5 class="fw-bold">{{ item.text }}</h5>
            <p class="m-0">{{ item.desc }}</p>
          </div>
          <button class="btn-advantage" @click="triggerOpenDialog"><h5 class="text-btn">Đăng Ký Ngay</h5></button>
        </div>
        <div class="advantage-img" id="advantage-img" :class="{ visible: itemAdvantageVisible[1]}">
          <img :src="CAPhone" alt="" class="w-100">
        </div>
      </div>
    </div>
  </div>

</template>
<script setup lang="ts">
import shadow from "@/assets/images/shadow.svg";
import CAPhone from "@/assets/images/CA-phone.png";
import {onMounted, onUnmounted, ref} from "vue";
import USBIcon from "@/assets/images/ekm-usb.svg";
import phoneIcon from "@/assets/images/ekm-phone.svg";
import keyIcon from "@/assets/images/ekm-challenge.svg";
import lapIcon from "@/assets/images/ekm-key-control.svg";
import { useScrollAnimation } from '@/utils/useScrollAnimation'

const emit = defineEmits(['open-dialog']);

const productItems = ref([
  {icon: USBIcon, text: "USB Token", desc: "Thiết bị nhỏ gọn giúp ký số an toàn mà <span class='text-blue fw-bold'>không cần kết nối internet</span>", visible: false},
  {icon: phoneIcon, text: "Remote Signing", desc: "Ký số từ xa, đảm bảo tính bảo mật và pháp lý, áp dụng <span class='text-blue fw-bold'>trên nhiều nền tảng, mọi lúc, mọi nơi</span>.", visible: false},
  {icon: keyIcon, text: "Kiểm tra hiệu lực chứng thư", desc: "Dễ dàng <router-link to='/search' class='text-blue fw-bold text-decoration-underline' style='cursor: pointer'><i>kiểm tra</i></router-link> tình trạng và hiệu lực của chứng thư chữ ký số theo thời gian thực để đảm bảo an toàn cho các giao dịch điện tử.", visible: false},
  {icon: lapIcon, text: "Quản lý tập trung", desc: "Theo dõi, kiểm tra và gia hạn chứng thu một cách dễ dàng.", visible: false},
]);

const advantage = ref([
  {text: "An toàn Bảo mật cao", desc: "Đảm bảo tính toàn vẹn của tài liệu, chống lại các hành vi gia mạo và chỉnh sửa trái phép"},
  {text: "Pháp lý rõ ràng", desc: "Được pháp luật công nhận, có giá trị tương đương với chữ ký tay, hợp thức hóa các giao dịch điện tử với đầy đủ cơ sở pháp lý"},
  {text: "Tiết kiệm Thời gian và Chi phí", desc: "Giảm thiểu chi phí in ấn, lưu trữ giấy tờ và vận chuyển tài liệu, đồng thời tối ưu hóa quy trình"},
]);

const itemProductVisible = useScrollAnimation(productItems.value, 'product');
const itemAdvantageVisible = ref([false, false]);

const handleScroll = () => {
  const advantageText = document.getElementById("advantage-text");
  const advantageImg = document.getElementById("advantage-img");

  if (advantageText && advantageText.getBoundingClientRect().top < window.innerHeight && advantageText.getBoundingClientRect().bottom > 0) {
    itemAdvantageVisible.value[0] = true; // Hiển thị advantage-text
  } else {
    itemAdvantageVisible.value[0] = false; // Ẩn advantage-text
  }

  // Kiểm tra phần tử advantage-img
  if (advantageImg && advantageImg.getBoundingClientRect().top < window.innerHeight && advantageImg.getBoundingClientRect().bottom > 0) {
    itemAdvantageVisible.value[1] = true; // Hiển thị advantage-img
  } else {
    itemAdvantageVisible.value[1] = false; // Ẩn advantage-img
  }
};

onMounted(() => {
  window.addEventListener("scroll", handleScroll);
  handleScroll(); // Kiểm tra trạng thái ngay khi mount
});

onUnmounted(() => {
  window.removeEventListener("scroll", handleScroll);
});

const triggerOpenDialog = () => {
  emit('open-dialog');
};
</script>

<style scoped>
.product,
.advantage
{
  margin: 5rem 0;
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
  z-index: -1;
}

.text-product__header {
  position: relative;
  z-index: 1;
}

.product-item {
  flex: 0 0 calc(50% - 2rem);
  overflow: hidden;
  margin: 2rem 1rem;
  display: flex;
  flex-direction: column;
  min-height: 300px;
  padding: 0 4rem;
  transform: translateY(100px);
  opacity: 0;
  transition: all 1s ease;
}

.product-item.visible {
  transform: translateY(0);
  opacity: 1;
}

.product-item__img {
  margin-bottom: 1.5rem;
}

.product-item__img img {
  height: 200px;
}

.product-item__text p {
  text-align: center;
  color: var(--gray);
}

.btn {
  transform: translateY(20px);
  opacity: 0;
  transition: transform 1s ease, opacity 0.5s ease;
}

.btn.visible {
  transform: translateY(0);
  opacity: 1;
}

.advantage-text {
  width: 34%;
  padding-right: 2rem;
  transform: translateX(-100px);
  opacity: 0;
  transition: all 1s ease;
}

.advantage-text.visible,
.advantage-img.visible{
  transform: translateX(0);
  opacity: 1;
}

.advantage-item {
  margin: 1rem 0;
  padding: 1rem;
  border-radius: 25px;
  background-image: linear-gradient(to right, var(--light-blue-1), var(--blue-2));
}

.advantage-item h5,
.advantage-item p {
  text-align: end;
}

.btn-advantage {
  width: 100%;
  background-color: var(--white);
  outline: none;
  border: none;
  padding: 1rem;
  box-shadow: rgba(14, 30, 37, 0.12) 0px 2px 4px 0px, rgba(14, 30, 37, 0.2) 0px 2px 16px 0px;
  border-radius: 25px;
  margin: 1rem 0;
}

.btn-advantage .text-btn {
  background: linear-gradient(to right, var(--light-blue-2) 0%, var(--blue-3) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  font-weight: bold;
  margin: 0;
  display: inline-block;
}

.advantage-img {
  width: 66%;
  padding-left: 5rem;
  transform: translateX(100px);
  opacity: 0;
  transition: all 1s ease;
}

@media (max-width: 768px) {
  .product,
  .advantage
  {
    margin: 2rem 0;
  }

  .text-product p {
    font-size: 12px;
    margin: 0;
  }

  .text-product__header {
    font-size: 20px;
  }

  .background-image {
    left: 4%;
  }

  .product-item {
    flex: 0 0 100%;
    flex-direction: row;
    padding: 1rem;
    margin: 0;
    min-height: 100px;
    align-items: center;
  }

  .product-item__img {
    margin: 0;
    width: 100px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .product-item__img img {
    height: 70px;
    width: auto;
  }

  .product-item__text {
    flex: 1;
    padding-left: 1rem;
  }

  .product-item__text h5,
  .product-item__text p {
    text-align: start;
  }

  .product-item__text h5 {
    font-size: 1rem;
  }

  .product-item__text p {
    font-size: 12px;
  }

  .advantage {
    margin: 2rem 0;
  }

  .advantage .container .text-blue {
    font-size: 20px;
  }

  .advantage-text,
  .advantage-img {
    width: 100%;
    padding: 1rem 2rem;
  }

  .advantage-img {
    order: -1;
  }

  .advantage-item h5,
  .advantage-item p {
    text-align: start;
  }

  .advantage-item h5,
  .text-btn {
    font-size: 14px;
  }

  .advantage-item p {
    font-size: 12px;
  }

  .btn-advantage {
    padding: 0.75rem 0;
  }
}

</style>