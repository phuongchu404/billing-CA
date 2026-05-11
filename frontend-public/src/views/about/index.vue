<template>
  <div class="bg-light">
    <div class="container text-start">
      <h3 class="text-blue text-header text-about">VỀ CHÚNG TÔI</h3>
      <div class="box shadow-blue bg-white">
        <h3 class="text-blue text-header text-uppercase text-center">Công ty Cổ phần Công nghệ cao MK (MK Hi-tek)</h3>
        <div class="d-flex flex-wrap">
          <div class="about-item">
            <p class="text-desc">
              <i class="text-blue fw-600">Công ty Cổ phần Công nghệ cao MK (MK Hi-tek)</i>
              là thành viên của <i class="text-blue fw-600">MK Group</i>
              - thành lập năm 1999, doanh nghiệp chuyên sâu về các giải pháp Xác thực Bảo mật số và Thẻ thông minh. Chúng tôi tự hào về việc làm chủ hoàn toàn các công nghệ lõi liên quan đến lĩnh vực bảo mật số thông minh, thẻ thông minh, sinh trắc học, đáp ứng các nhu cầu khắt khe của thị trường trong nước và xuất khẩu ra nước ngoài.
            </p>
            <p class="text-desc">
              Trải qua hơn 24 năm xây dựng và phát triển, Tập đoàn MK cùng các đơn vị thành viên không ngừng đầu tư nghiên cứu các giải pháp mang hàm lượng chất xám công nghệ cao dựa trên những thành tựu và xu hướng công nghệ thế giới để phục vụ tốt hơn.
              <a href="https://mk.com.vn/gioi-thieu" target="_blank" class="text-blue">
                <i>Tìm hiểu thêm</i>
                <font-awesome-icon icon="fa-solid fa-up-right-from-square" class="ms-2"/>
              </a>
            </p>
          </div>
          <div class="about-item d-flex align-items-center">
            <div class="hexagon-grid">
              <div
                  class="hexagon hex-center"
                  :style="{ background: hexCenter.gradient }"
              >
                <span class="hex-text">{{ hexCenter.text }}</span>
              </div>

              <div v-for="(hex, index) in hexagons"
                   :key="index"
                   class="hexagon hex-around"
                   :style="{ backgroundImage: hex.gradient }"
              >
                <span class="hex-text" v-if="hex.text !== logo">{{ hex.text }}</span>
                <img v-if="hex.text == logo" :src="hex.text" alt="" class="w-75">
              </div>
            </div>

          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script lang="ts" setup>
import {ref} from "vue";
import logo from '@/assets/images/logo-text.png'

const hexCenter = ref({
  gradient:
      "radial-gradient(circle, rgba(252, 148, 25, 1) 0%, rgba(255, 174, 53, 1) 50%, rgba(232, 102, 29, 1) 100%), " +
      "linear-gradient(135deg, rgba(241, 241, 241, 0.5) 50%, transparent 50%), " +
      "linear-gradient(-135deg, rgba(241, 241, 241, 0.5) 50%, transparent 50%)",
  text: "5S"
});


const hexagons = ref([
  { gradient: "radial-gradient(circle, rgba(103, 61, 240, 1) 0%, rgba(140, 123, 243, 1) 50%, rgba(115, 76, 242, 1) 100%)", text: "Smart cards"},
  { gradient: "radial-gradient(circle, rgba(23, 107, 232, 1) 0%, rgba(72, 162, 245, 1) 50%, rgba(28, 105, 218, 1) 100%)", text: "Security"},
  { gradient: "radial-gradient(circle, rgba(254, 206, 29, 1) 0%, rgba(255, 230, 96, 1) 50%, rgba(254, 206, 29, 1) 100%)", text: "Services"},
  { gradient: "linear-gradient(180deg, rgba(184, 225, 248, 1) 0%, rgba(255, 254, 248, 1) 50%, rgba(233, 255, 242, 1) 100%)", text: logo},
  { gradient: "radial-gradient(circle, rgba(23, 197, 51, 1) 0%, rgba(52, 218, 118, 1) 50%, rgba(11, 165, 82, 1) 100%)", text: "Secure Transactions"},
  { gradient: "radial-gradient(circle, rgba(224, 74, 53, 1) 0%, rgba(242, 109, 80, 1) 50%, rgba(224, 41, 63, 1) 100%)", text: "Solutions"}
])
</script>

<style scoped>
.container {
  width: calc(1000px + 4rem);
  padding: 2rem 0;
}

.container .text-header {
  margin-bottom: 2rem;
  font-size: 24px;
}

.box {
  padding: 2rem 4rem;
  margin-bottom: 3rem;
  border-radius: 0.625rem;
}

.about-item {
  width: 50%;
}

.text-desc {
  font-size: 17px;
  text-align: justify;
}

.fw-600 {
  font-weight: 600;
}

.hexagon-grid {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
  position: relative;
  width: 300px;
  height: 300px;
  margin: 0 auto;
}

.hexagon {
  height: 100px;
  aspect-ratio: 1/cos(30deg);
  clip-path: polygon(50% -50%,100% 50%,50% 150%,0 50%);
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
  text-align: center;
  line-height: 1.2;
  position: absolute;
  background-image: radial-gradient(circle, rgba(241, 241, 241, 0.12) 40%, transparent 50%);
  background-size: cover;
}

.hex-center {
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 2;
}

.hex-center .hex-text {
  font-size: 2.5rem;
}

.hex-text {
  font-size: 1rem;
  font-weight: 800;
}

@keyframes moveToPosition {
  0% {
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
  }
  100% {
    top: var(--top, 0);
    left: var(--left, 0);
    transform: var(--transform, translate(0, 0));
  }
}

.hex-around {
  z-index: 1;
  animation: moveToPosition 1s ease forwards;
}

.hex-around:nth-child(2) {
  --top: -1%;
  --left: 50%;
  --transform: translate(-50%, 0);
}

.hex-around:nth-child(3) {
  --top: 33%;
  --left: 80%;
  --transform: translate(-50%, -50%);
}

.hex-around:nth-child(4) {
  --top: 67%;
  --left: 80%;
  --transform: translate(-50%, -50%);
}

.hex-around:nth-child(5) {
  --top: 101%;
  --left: 50%;
  --transform: translate(-50%, -100%);
}

.hex-around:nth-child(6) {
  --top: 67%;
  --left: 20%;
  --transform: translate(-50%, -50%);
}

.hex-around:nth-child(7) {
  --top: 33%;
  --left: 20%;
  --transform: translate(-50%, -50%);
}

@media (max-width: 768px) {
  .container {
    width: auto;
    padding: 0;
  }

  .box {
    padding: 1rem 2rem;
    margin: 0;
    background: linear-gradient(var(--light-blue), var(--white))!important;
  }

  .container .text-header {
    font-size: 18px;
    margin-bottom: 2rem;
    text-align: center;
  }

  .text-about {
    display: none;
  }

  .about-item {
    width: 100%;
  }

  .text-desc {
    font-size: 15px;
  }

  .about-item:first-child {
    order: 2;
  }

  .about-item:last-child {
    order: 1;
    margin-bottom: 1rem;
  }

}

</style>
