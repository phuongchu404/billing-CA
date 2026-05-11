<template>
  <div class="review">
    <div class="container">
      <h2 class="text-blue fw-bold text-header">Hướng dẫn Đăng ký sử dụng Chữ ký Số</h2>
      <div class="d-flex flex-wrap">
        <div class="review-img" id="review-img" :class="{ visible: itemReviewVisible[0] }">
          <img :src="CAKey" alt="" class="w-100">
        </div>
        <div class="review-text" id="review-text" :class="{ visible: itemReviewVisible[1] }">
          <div v-for="(item, index) in review" class="review-text_item d-flex text-start">
            <div>
              <p class="text-number">{{ index + 1 }}</p>
            </div>
            <div>
              <h5 class="text-blue review-text__header">{{ item.text }}</h5>
              <p class="m-0 text-gray" v-html="item.desc"></p>
              <div v-if="item.showButton" class="mt-2">
                <button class="btn btn-primary rounded-pill bg-blue" style="font-size: 14px" @click="triggerOpenDialog">Đăng Ký Ngay</button>
              </div>

              <!-- Conditionally show the app links based on review data -->
              <div v-if="item.showAppLinks" class="d-flex mt-2">
                <div class="d-flex download-links">
                  <a>
                    <button class="btn-download rounded">
                      <img :src="AppleIcon" alt="">
                      <p class="btn-download__text m-0">Download on the <br> <span class="big-txt">App Store</span></p>
                    </button>
                  </a>
                  <a>
                    <button class="btn-download rounded">
                      <img :src="GGIcon" alt="">
                      <p class="btn-download__text m-0">GET IN ON <br> <span class="big-txt">Google Play</span></p>
                    </button>
                  </a>
                </div>
                <div class="qr-img">
                  <img :src="QR" alt="" class="w-100">
                </div>
              </div>
            </div>
          </div>
        </div >
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import CAKey from "@/assets/images/Simplified-Data-Security.svg";
import AppleIcon from "@/assets/images/AppleStore.svg";
import GGIcon from "@/assets/images/GGPlay.svg";
import QR from "@/assets/images/QR-test.png";
import {onMounted, onUnmounted, ref} from "vue";

const emit = defineEmits(['open-dialog']);

const review = ref([
  {text: "Điền thông tin đăng ký", desc: "Điền các thông tin để chúng tôi có thể <span class='text-blue fw-bold'>liên hệ</span> sớm nhất <br>", showButton: true},
  {text: "Xác minh danh tính",  desc: "Đối với cá nhân: Chuẩn bị bản sao giấy tờ cá nhân<br>Đối với doanh nghiệp: Cung cấp bản sao giấy phép kinh doanh"},
  {text: "Ký Hợp đồng Sử dụng dịch vụ", desc: ""},
  {text: "Kích hoạt tài khoản", desc: "USB Token: Thiết bị sẽ được đóng gói và đảm bảo bảo mật khi gửi đến địa chỉ của bạn.<br>Remote Signing: Tải ứng dụng MK Smart CA và kích hoạt theo hướng dẫn.<br>", showAppLinks: true}
]);

const itemReviewVisible = ref([false, false]);

const handleScroll = () => {
  const reviewText = document.getElementById("review-text");
  const reviewImg = document.getElementById("review-img");

  // Kiểm tra phần tử review-img
  if (reviewImg && reviewImg.getBoundingClientRect().top < window.innerHeight && reviewImg.getBoundingClientRect().bottom > 0) {
    itemReviewVisible.value[0] = true; // Hiển thị review-img
  } else {
    itemReviewVisible.value[0] = false; // Ẩn review-img
  }
  if (reviewText && reviewText.getBoundingClientRect().top < window.innerHeight && reviewText.getBoundingClientRect().bottom > 0) {
    itemReviewVisible.value[1] = true; // Hiển thị review-text
  } else {
    itemReviewVisible.value[1] = false; // Ẩn review-text
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
.review {
  background-image: linear-gradient(var(--white), var(--light-1));
  margin-top: 5rem;
}

.review-img,
.review-text {
  width: 50%;
}

.review .text-header {
  margin-bottom: 3rem;
}

.review-img {
  transform: translateX(-100px);
  opacity: 0;
  transition: all 1s ease;
}

.review-text {
  padding: 0 3rem;
  transform: translateX(100px);
  opacity: 0;
  transition: all 1s ease;
}

.review-img.visible,
.review-text.visible {
  transform: translateX(0);
  opacity: 1;
}

.review-text_item {
  padding-left: 4rem;
  margin-bottom: 2rem;
}

.text-number {
  display: flex;
  align-items: center;
  justify-content: center;
  width:30px;
  height:30px;
  border-radius:50%;
  border: 1px solid var(--blue);
  color: var(--blue);
  margin-right: 1rem;
}

.review-text__header {
  margin-bottom: 0.25rem;
  font-weight: 500;
}

.download-links {
  flex-direction: column;
  justify-content: space-around;
  margin-right: 2rem;
}

.btn-download {
  display: flex;
  align-items: center;
  text-align: left;
  width: 146px;
  border: 1px solid var(--gray);
  outline: none;
  background-color: var(--white);
}

.btn-download img {
  width: 24px;
  margin-right: 12px;
}

.btn-download__text {
  font-size: 12px;
}

.btn-download__text .big-txt {
  font-size: 1rem;
  font-weight: 600;
}

.qr-img {
  width: 120px;
  height: 120px;
}

@media (max-width: 768px) {
  .review {
    margin-top: 2rem;
  }

  .review .container .text-header {
    font-size: 18px;
    margin-bottom: 0;
  }

  .review-img,
  .review-text {
    width: 100%;
    padding: 2rem;
  }

  .review-text_item {
    padding: 0;
    margin-bottom: 0.5rem;
  }

  .review-text .qr-img {
    display: none;
  }

  .review-text .download-links {
    flex-direction: row;
    margin-right: 0;
  }

  .review-text .download-links a:first-child {
    margin-right: 1rem;
  }

  .review-text__header {
    font-size: 14px;
  }

  .review-text .review-text_item .text-gray {
    font-size: 12px;
  }

  .review-text .review-text_item .text-number {
    width: 22px;
    height: 22px;
    font-size: 14px;
  }

  .review-text_item .mt-2 {
    text-align: end;
  }

  .btn-download img {
    width: 18px;
  }

  .btn-download {
    width: 120px;
  }

  .btn-download__text {
    font-size: 8px;
  }

  .btn-download__text .big-txt {
    font-size: 11px;
  }
}
</style>