<template>
  <div class="contact">
    <div class="container">
      <div class="d-flex justify-content-around">
        <div class="contact-img">
          <img :src="border" alt="" class="border-img">
          <img :src="contact" alt="" class="ca-img">
        </div>
        <div class="form-contact">
          <div class="text-container">
            <h2 class="text-blue fw-bold text-product__header">Tư vấn miễn phí</h2>
            <img :src="shadow" class="background-image">
          </div>
          <el-form
              :model="form"
              :rules="registerFormRules"
              ref="formRef"
              label-width="120px"
              label-position="top"
              class="text-start"
              hide-required-asterisk
          >
            <el-form-item label="Họ và Tên" prop="fullName">
              <el-input v-model="form.fullName" placeholder="Nhập họ và tên"></el-input>
            </el-form-item>

            <el-form-item label="Email" prop="email">
              <el-input v-model="form.email" placeholder="Nhập email"></el-input>
            </el-form-item>

            <el-form-item label="Số điện thoại" prop="phoneNum">
              <el-input v-model="form.phoneNum" placeholder="Nhập số điện thoại"></el-input>
            </el-form-item>

            <el-form-item label="Yêu cầu tư vấn (không bắt buộc)" prop="details">
              <el-input v-model="form.details" placeholder="Hãy cho chúng tôi biết nhu cầu hiện tại của bạn"></el-input>
            </el-form-item>

            <el-form-item>
              <button type="button" class="btn btn-primary rounded-pill bg-blue px-4 py-2 mx-auto" @click.prevent="handleSubmit">Đăng Ký Ngay</button>
            </el-form-item>
          </el-form>
<!--          <form>-->
<!--            <div class="mb-4 text-start">-->
<!--              <label>Họ và tên</label>-->
<!--              <input type="text" v-model="form.fullName" class="form-control" placeholder="Vui lòng nhập họ tên của bạn">-->
<!--            </div>-->
<!--            <div class="mb-4 text-start">-->
<!--              <label>Số điện thoại</label>-->
<!--              <input type="text" v-model="form.phoneNum" class="form-control" placeholder="Vui lòng nhập số điện thoại của bạn">-->
<!--            </div>-->
<!--            <div class="mb-4 text-start">-->
<!--              <label>Email</label>-->
<!--              <input type="email" v-model="form.email" class="form-control" placeholder="Vui lòng nhập email của bạn">-->
<!--            </div>-->
<!--            <div class="mb-4 text-start">-->
<!--              <label>Yêu cầu tư vấn (không bắt buộc)</label>-->
<!--              <input type="text" v-model="form.details" class="form-control" placeholder="Hãy cho chúng tôi biết nhu cầu của bạn hiện tại">-->
<!--            </div>-->
<!--            <button class="btn btn-primary rounded-pill bg-blue px-4 py-2" @click="handleSubmit">Đăng Ký Ngay</button>-->
<!--          </form>-->
        </div>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import border from "@/assets/images/Border.svg";
import contact from "@/assets/images/Card.png";
import shadow from "@/assets/images/shadow.svg";
import {ref} from "vue";
import {registerUser} from "@/api/register";
import {ElMessage} from "element-plus";

const form = ref({
  fullName: '',
  phoneNum: '',
  email: '',
  details: '',
});

const formRef = ref();

const registerFormRules = {
  fullName: [{ required: true, message: "Họ và tên không được để trống", trigger: "blur" }],
  email: [
    { required: true, message: "Email không được để trống", trigger: "blur" },
    { type: "email", message: "Email không hợp lệ", trigger: "blur" },
  ],
  phoneNum: [
    { required: true, message: "Số điện thoại không được để trống", trigger: "blur" },
    {
      pattern: /^[0-9]{10}$/,
      message: "Số điện thoại phải là 10 chữ số",
      trigger: "blur",
    },
  ],
};

const handleSubmit = async () => {
  if (formRef.value) {
    formRef.value.validate(async (valid: boolean) => {
      if (valid) {
        try {
          await registerUser(form.value);
          ElMessage({
            message: "Gửi yêu cầu thành công",
            type: "success",
            duration: 5000,
            showClose: true,
          });
          resetForm();
        } catch (error) {
          ElMessage.error("Đã xảy ra lỗi khi gửi dữ liệu");
        }
      } else {
        ElMessage.error("Vui lòng kiểm tra lại thông tin");
      }
    });
  } else {
    ElMessage.error("Form chưa được khởi tạo");
  }
};

const resetForm = () => {
  formRef.value.resetFields();
};
</script>

<style scoped>
.contact {
  margin: 3rem 0;
}

.contact-img {
  position: relative;
  height: 520px;
  margin-right: 2rem;
}

.border-img {
  position: absolute;
  z-index: -1;
  width: 188px;
}

.ca-img {
  position: relative;
  z-index: 1;
  width: 520px;
  top: 40px;
  left: 40px;
  height: 520px;
}

.form-contact {
  width: 620px;
  height: 520px;
  box-shadow: rgba(99, 99, 99, 0.2) 0px 2px 8px 0px;
  padding: 1.5rem 2rem;
  border-radius: 10px;
  margin-left: 2rem;
  margin-top: 40px;
}

.text-container {
  position: relative;
}

.background-image {
  position: absolute;
  top: 50%;
  left: 25%;
  z-index: -1;
}

.text-product__header {
  position: relative;
  z-index: 1;
}

.form-contact .background-image {
  top: 75%;
  left: 20%;
  width: 280px;
}

::v-deep .form-contact .el-input__wrapper {
  padding: 0.75rem!important;
  font-size: 17px;
}

::v-deep .el-form-item--label-top .el-form-item__label {
  margin-bottom: 0;
}

@media (max-width: 768px) {
  .contact-img {
    display: none;
  }

  .form-contact {
    margin: 0;
    box-shadow: none;
    width: 100%;
  }

  .form-contact .btn {
    width: 100%;
  }

  .form-contact form label {
    font-size: 12px;
  }

  input::placeholder {
    font-size: 14px;
  }

  .background-image {
    display: none;
  }

  .text-product__header {
    font-size: 18px;
  }
}
</style>