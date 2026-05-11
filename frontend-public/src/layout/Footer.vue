<template>
  <footer>
    <div class="container">
      <div class="row">
<!--        <div class="col-lg-4 d-flex justify-content-start">-->
<!--          <div class="card border-0">-->
<!--            <div class="circle" style="&#45;&#45;clr: var(&#45;&#45;blue)">-->
<!--              <img :src="logoText" class="logo">-->
<!--            </div>-->
<!--            <div class="content">-->
<!--              <img :src="logoText" class="">-->
<!--              <p>MK Group is the best. </br>Our secret blend of flavors.</p>-->
<!--              <a href="#">Explore More</a>-->
<!--            </div>-->
<!--            <img :src="logo" class="product_img">-->
<!--          </div>-->
<!--        </div>-->
        <div class="col-lg-6 ft-left text-start">
          <h4>© MK Group.  Bản quyền thuộc về Tập Đoàn MK</h4>
          <p>Đăng ký nhận tin nội bộ Thế Giới Thẻ MK</p>
          <div class="resgis-email">
            <form>
              <div class="form-group">
                <input type="email" class="form-control" placeholder="Email của bạn...">
                <button>Đăng ký</button>
              </div>
            </form>
          </div>
          <div class="social-ft">
            <a href="" class="text-white">
              <font-awesome-icon icon="fa-brands fa-square-facebook" />
            </a>
            <a href="" class="text-white">
              <font-awesome-icon icon="fa-brands fa-linkedin" />
            </a>
            <a href="" class="text-white">
              <font-awesome-icon icon="fa-brands fa-youtube" class="yt-link"/>
            </a>
          </div>
        </div>
        <div class="col-lg-6 ft-right">
          <div class="link">
            <ul class="p-0 m-0">
              <li v-for="item in list">
                <a href="" :title="item">{{ item }}</a>
              </li>
            </ul>
          </div>
          <div class="location text-start">
            <div>
              <h4><font-awesome-icon icon="fa-solid fa-location-dot" /> TRỤ SỞ</h4>
              <p>Tầng 2 Tòa nhà The Vista, số 4 ngõ 15 Duy Tân, Cầu Giấy, Hà Nội, Việt Nam</p>
              <p class="w-auto">
                Điện thoại :
                <a href="https://mk.com.vn/tel:+84 24 7100 6781">+84 24 7100 6781</a>
              </p>
              <span>|</span>
              <p class="w-auto">
                Fax :
              </p>
            </div>
            <div>
              <h4><font-awesome-icon icon="fa-solid fa-location-dot" /> VĂN PHÒNG TẠI TP. HỒ CHÍ MINH</h4>
              <p>Tầng 7, Thiên Sơn Building, số 5 Nguyễn Gia Thiều, Quận 3, Tp. Hồ Chí Minh</p>
              <p class="w-auto">
                Điện thoại :
                <a href="https://mk.com.vn/tel:+84 28 3930 1055">+84 28 3930 1055</a>
              </p>
              <span>|</span>
              <p class="w-auto">
                Fax :
              </p>
              <p>
                <font-awesome-icon icon="fa-solid fa-envelope" />
                Email:
                <a href="mailto:marketing@mkgroup.com.vn">marketing@mkgroup.com.vn</a>
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import {ref} from "vue";
import logoText from "@/assets/images/logo-text.png";
import logo from "@/assets/images/logo.svg";

const list = ref([
    "Sản phẩm", "Giải pháp", "Tin tức & sự kiện", "Liên hệ"
])
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css?famiyl=Poppins:200,300,400,500,600,700,800,900&display=swap");

a {
  text-decoration: none;
}

footer {
  float: left;
  width: 100%;
  background: #2e2e2e;
  color: white;
  padding: 95px 0 80px 0;
}

footer .ft-left > h4 {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 35px;
}

footer .ft-left p {
  font-size: 15px;
  letter-spacing: 0.5px;
  margin-bottom: 25px;
}

footer .ft-left .resgis-email {
  float: left;
  margin-top: 11px;
}

footer .ft-left .resgis-email .form-group {
  margin-bottom: 0;
  position: relative;
}

footer .ft-left .resgis-email .form-group input {
  height: 40px;
  color: #9f9f9f;
  font-size: 16px;
  font-family: "HelveticaNeue", sans-serif;
  border-radius: 0;
  border: none;
  width: 390px;
  background: #f7f7f7;
}

footer .ft-left .resgis-email .form-group button {
  border: none;
  position: absolute;
  right: 0;
  top: 0;
  height: 40px;
  font-weight: 600;
  background: var(--blue);
  color: var(--white);
  padding: 0 15px;
  text-transform: uppercase;
  cursor: pointer;
  display: flex;
  align-items: center;
  display: -webkit-flex;
  -webkit-align-items: center;
  font-size: 16px;
}

footer .ft-left .social-ft {
  float: left;
  width: 100%;
  margin-top: 60px;
}

footer .ft-left .social-ft a {
  color: white;
  margin-right: 35px;
  float: left;
  transition: 0.4s;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  text-align: center;
}

footer .ft-left .social-ft a:hover .yt-link {
  color: red!important;
}

footer .ft-left .social-ft a:hover svg{
  color: var(--blue);
}

footer .ft-left .social-ft a svg {
  width: 32px;
  height: 32px;
}

footer .ft-right {
  margin-top: -4px;
}

footer .ft-right .link,
footer .ft-right .location {
  float: left;
  width: 100%;
}

footer .ft-right .link ul li {
  float: left;
}

ul li {
  list-style-type: none;
}

footer .ft-right .link ul li:first-child a {
  padding-left: 0;
  border-left: none;
}

footer .ft-right .link ul li a {
  padding: 0 30px;
  font-size: 15px;
  color: white;
  border-left: 1px solid #777777;
  font-weight: 600;
}

footer .ft-right .location h4 {
  float: left;
  width: 100%;
  font-size: 14px;
  font-weight: 400;
  text-transform: uppercase;
  margin-bottom: 5px;
  margin-top: 25px;
}

footer .ft-right .location p {
  color: #c5c5c5;
  font-weight: 400;
  font-size: 14px;
  line-height: 27px;
  margin: 0;
  float: left;
  width: 100%;
}

footer .ft-right .location p a {
  color: #c5c5c5;
}

footer .ft-right .location span {
  float: left;
  padding: 0 50px;
  color: #777777;
}

.card {
  position: relative;
  width: 350px;
  height: 350px;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: 0.5s;
  transition-delay: 0.5s;
  transform: scale(0.75);
  background: transparent;
}
.card:hover {
  width: 600px;
  transition-delay: 0.5s;
}
.card .circle {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
}
.card .circle::before {
  content: "";
  position: absolute;
  top: 30;
  left: 2;
  right: 2;
  width: 350px;
  height: 350px;
  border-radius: 50%;
  background: var(--blue);
  border: 8px solid var(--clr);
  filter: drop-shadow(0 0 10px var(--clr)) drop-shadow(0 0 60px var(--clr));
  transition: 0.5s, background 0.5s;
  transition-delay: 0.75s, 1s;
}
.card:hover .circle::before {
  transition-delay: 0.5s;
  width: 100%;
  height: 100%;
  border-radius: 20px;
  background: var(--clr);
}
.card .circle .logo {
  position: relative;
  width: 150px;
  max-width: 100%;
  height: auto;
  transition: 0.5s;
  transition-delay: 0.5s;
}
.card:hover .circle .logo {
  transform: scale(0);
  transition-delay: 0s;
}
.card .product_img {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%) scale(0) rotate(315deg);
  width: 150px;
  max-width: 300px;
  height: auto;
  transition: 0.5s ease-in-out;
}
.card:hover .product_img {
  transition-delay: 0.75s;
  top: 25%;
  left: 72%;
  height: 500px;
  transform: translate(-50%, -50%) scale(0.5) rotate(0deg);
}
.card .content {
  position: absolute;
  width: 50%;
  left: 20%;
  padding: 20px 20px 20px 20px;
  opacity: 0;
  transition: 0.5s;
  visibility: hidden;
}
.card:hover .content {
  transition-delay: 0.75s;
  opacity: 1;
  visibility: visible;
  left: 20px;
}
.card .content h2 {
  color: #fff;
  text-transform: uppercase;
  font-size: 2.5em;
  line-height: 1em;
}
.card .content p {
  color: #fff;
}
.card .content a {
  position: relative;
  color: #111;
  background: #fff;
  padding: 10px 20px;
  border-radius: 10px;
  display: inline-block;
  text-decoration: none;
  font-weight: 600;
  margin-top: 10px;
}

@media (max-width: 768px) {
  footer {
    padding: 50px 0;
  }

  .link {
    display: none;
  }

  footer .ft-left  .social-ft {
    margin-top: 30px;
  }

  footer .ft-left {
    margin-bottom: 2rem;
  }

  footer .ft-left > h4,
  footer .ft-left p {
    margin-bottom: 1rem;
  }

  footer .ft-left .resgis-email {
    margin-top: 0;
  }

  footer .ft-left .resgis-email .form-group input {
    width: 340px;
  }
}
</style>
