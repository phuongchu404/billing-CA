<template>
  <el-dialog
      v-model="dialogVisible"
      @close="handleClose"
      @confirm="handleConfirm"
      width="620px"
  >
    <template #default>
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
<!--        <form>-->
<!--          <div class="mb-4 text-start">-->
<!--            <label>Họ và tên</label>-->
<!--            <input type="text" class="form-control" placeholder="Vui lòng nhập họ tên của bạn">-->
<!--          </div>-->
<!--          <div class="mb-4 text-start">-->
<!--            <label>Số điện thoại</label>-->
<!--            <input type="text" class="form-control" placeholder="Vui lòng nhập số điện thoại của bạn">-->
<!--          </div>-->
<!--          <div class="mb-4 text-start">-->
<!--            <label>Email</label>-->
<!--            <input type="email" class="form-control" placeholder="Vui lòng nhập email của bạn">-->
<!--          </div>-->
<!--          <div class="mb-4 text-start">-->
<!--            <label>Yêu cầu tư vấn (không bắt buộc)</label>-->
<!--            <input type="text" class="form-control" placeholder="Hãy cho chúng tôi biết nhu cầu của bạn hiện tại">-->
<!--          </div>-->
<!--          <button class="btn btn-primary rounded-pill bg-blue px-4 py-2">Đăng Ký Ngay</button>-->
<!--        </form>-->
      </div>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import {computed, defineProps, defineEmits, ref} from 'vue';
import shadow from "@/assets/images/shadow.svg";
import {registerUser} from "@/api/register";
import {ElMessage} from "element-plus";

const props = defineProps({
  visible: {
    type: Boolean,
    required: true,
  },
});

const emit = defineEmits(['update:visible']);

const dialogVisible = computed({
  get: () => props.visible,
  set: (value: boolean) => emit('update:visible', value),
});

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
          handleClose();
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

const handleClose = () => {
  emit('update:visible', false);
};

const handleConfirm = () => {
  emit('update:visible', false);
};
</script>

<style scoped>
.form-contact {
  padding: 1.5rem 2rem;
  border-radius: 10px;
}

.text-container {
  position: relative;
}

.background-image {
  position: absolute;
  top: 75%;
  left: 20%;
}

.text-product__header {
  position: relative;
  z-index: 1;
}

input::placeholder {
  color: var(--gray);
}

::v-deep .el-input__wrapper {
  padding: 0.75rem!important;
  font-size: 17px;
}

::v-deep .el-form-item--label-top .el-form-item__label {
  margin-bottom: 0;
}
</style>
