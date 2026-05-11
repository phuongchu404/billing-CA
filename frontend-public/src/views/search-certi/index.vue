<template>
  <div class="bg-light">
    <div class="container text-start">
      <h3 class="text-blue text-header">TRA CỨU CHỨNG THƯ CHỮ KÝ SỐ</h3>
      <div class="box shadow bg-white">
        <p class="text-desc">Vui lòng nhập Serial Chứng thư cần tra cứu hoặc Mã số thuế với Tổ chức/ Doanh nghiệp, số Căn cước với Cá nhân</p>
        <el-form :model="queryForm" :rules="rules" ref="ruleValidate" label-position="top" class="form-search" @submit.prevent="getData">
          <div class="d-flex flex-wrap align-items-end justify-content-between">
            <el-form-item prop="serialNumber" label="Serial Chứng thư" @click="handleInputClick('serialNumber')">
              <el-input
                  v-model="queryForm.serialNumber"
                  :disabled="isInputDisabled === 'serialNumber'"
                  placeholder="Nhập Serial number Chứng thư cần tra cứu"
              />
              <div v-if="isInputDisabled === 'serialNumber'" class="input-overlay"></div>
            </el-form-item>
            <h5 class="text-blue">HOẶC</h5>
            <div>
              <el-form-item class="mb-1">
                <el-checkbox-group v-model="selectedValues" @change="handleCheckboxChange">
                  <el-checkbox :value="1">Tổ chức/ Doanh nghiệp</el-checkbox>
                  <el-checkbox :value="0">Cá nhân</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
              <el-form-item prop="taxCode" :label="ui.organizationType ? 'Mã số thuế/ Mã ngân sách' : 'CCCD/ Hộ chiếu'" @click="handleInputClick('taxCode')">
                <el-input
                    v-model="queryForm.taxCode"
                    :disabled="isInputDisabled === 'taxCode'"
                    :placeholder="ui.organizationType ? 'Nhập Mã số thuế/ Mã ngân sách của công ty' : 'Nhập CCCD/ Hộ chiếu'"
                />
                <div v-if="isInputDisabled === 'taxCode'" class="input-overlay"></div>
              </el-form-item>
            </div>
          </div>
          <div class="d-flex flex-wrap align-items-center form-captcha">
            <el-form-item prop="verifyCode" class="input-captcha">
              <el-input v-model="queryForm.verifyCode" @keyup="enterLogin($event)" auto-complete="off" placeholder="Nhập mã xác thực"/>
            </el-form-item>
            <img class="code_img" @click="getCaptcha" :src="captchaPath" alt="">
            <font-awesome-icon icon="fa-solid fa-arrows-rotate" class="icon-change" @click="getCaptcha"/>

            <button class="btn btn-primary btn-search" :disabled="!(queryForm.verifyCode && (queryForm.serialNumber || queryForm.taxCode))" @click="search">Tra cứu</button>
          </div>

        </el-form>
      </div>

      <div v-if="data.length > 0 &&
      ui.typeChoose !== null &&
      data.some(item => item.endEntityType === selectedEntityType)"  class="box shadow bg-white">
        <h3 class="result-text_header">Chứng thư Chữ ký số {{ data.some(item => item.endEntityType === 'Company') ? 'Tổ chức / Doanh nghiệp' : 'Cá nhân' }}</h3>
        <div class="result">
          <div v-for="(item, index) in data" class="result-item rounded">
            <h5 class="text-blue mb-3 result-item_name text-center">{{ extractCommonName("company", item.subjectDn) }}</h5>
            <div>
              <el-row class="result-item_text" style="margin-bottom: 2rem">
                <el-col :span="24" class="text-label"><strong>Số Seri:</strong></el-col>
                <el-col :span="24" class="text-result">{{ item.serialNumber }}</el-col>
              </el-row>
              <el-row class="result-item_text">
                <el-col :span="14" class="text-label">
                  <strong>{{ (item.endEntityType === 'Company') ? 'Mã số thuế/ Mã ngân sách' : 'CCCD/ Hộ chiếu' }}:</strong>
                </el-col>
<!--                <el-col :span="10" class="text-result">{{ extractCommonName("taxCode", item.subjectDn) }}</el-col>-->
                <el-col :span="10" class="text-result">{{ (item.endEntityType === 'Company') ? item.Tax : item.idNumber }}</el-col>
              </el-row>
              <el-row class="result-item_text">
                <el-col :span="14" class="text-label"><strong>Ngày bắt đầu hiệu lực:</strong></el-col>
                <el-col :span="10" class="text-result">{{ formatDate(item.notBefore) }}</el-col>
              </el-row>
              <el-row class="result-item_text">
                <el-col :span="14" class="text-label"><strong>Ngày hết hiệu lực:</strong></el-col>
                <el-col :span="10" class="text-result">{{ formatDate(item.expireDate) }}</el-col>
              </el-row>
              <div class="d-flex justify-content-between align-items-center mb-3">
                <a :href="item.downloadurl">
                  <el-button style="width: 80px">
                    <el-icon class="me-1 text-primary"><Download /></el-icon>
                    <span class="text-primary" style="font-size: 13px">Tải về</span>
                  </el-button>
                </a>
                <el-tag :type="(item.status == 'normal') ? 'success' : 'info'" effect="dark" round style="height: 28px">{{ (item.status == 'normal') ? 'Hoạt động' : 'Thu hồi' }}</el-tag>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {computed, h, onMounted, ref, watch} from 'vue';
import {ElMessage, ElNotification} from "element-plus";
import { Download } from '@element-plus/icons-vue'
import { WarningFilled } from '@element-plus/icons-vue';
import {getSerialNumber} from "@/api/search";
import * as Utils from '@/utils';

type EntityType = 0 | 1;

interface CertificateItem {
  serialNumber: string;
  notBefore: string;
  expireDate: string;
  status: string | number;
  subjectDn: string;
  downloadurl: string;
  Tax: string;
  idNumber: string;
  endEntityType: string;
}

const ui = ref<{
  organizationType: boolean;
  typeChoose: EntityType | null;
}>({
  organizationType: true,
  typeChoose: null
})

const customIcon = () => h(WarningFilled);
const selectedValues = ref<EntityType[]>([1]);

const handleCheckboxChange = (val: EntityType[]) => {
  if (val.length > 1) {
    selectedValues.value = [val[val.length - 1]];
  }
  // queryForm.value.serialNumber = ''
  // queryForm.value.taxCode = ''
};

const queryForm = ref({
  entityType: selectedValues.value,
  taxCode: "",
  serialNumber: "",
  status: 0,
  uuid: '',
  verifyCode: "",
});

const data = ref<CertificateItem[]>([{
  serialNumber: '',
  notBefore: '',
  expireDate: '',
  status: 0,
  subjectDn: '',
  downloadurl: '',
  Tax: '',
  idNumber: '',
  endEntityType: ''
}]);

const fileToDownload = ref<File | null>(null);

const customValidator = (rule: any, value: string, callback: (error?: Error) => void) => {
  if (!queryForm.value.taxCode && !queryForm.value.serialNumber) {

  } else {
    callback();
  }
};

const ruleValidate = ref<any>(null);

const rules = ref({
  taxCode: [
    {validator: customValidator, trigger: 'blur'},
  ],
  serialNumber: [
    {validator: customValidator, trigger: 'blur'},
  ],
  verifyCode: [
    {required: true, message: "", trigger: 'blur'},
  ],
});

const isInputDisabled = ref('');

const status = ref([
  {id: [1, 2], label: "Chờ cấp duyệt ", type: "warning"},
  {id: [3], label: "Chờ kích hoạt", type: "primary"},
  {id: [4], label: "Đang hoạt động", type: "success"},
  {id: [5, 6, 11, 12, 13, 14], label: "Đã thu hồi", type: "info"},
  {id: [], label: "Hết hạn", type: "info"},
  {id: [15], label: "Bị khóa", type: "info"},
]);

const entityTypeMap: Record<EntityType, string> = {
  1: 'Company',
  0: 'Individual',
};

const selectedEntityType = computed(() => (
  ui.value.typeChoose === null ? '' : entityTypeMap[ui.value.typeChoose]
));

const handleInputClick = (field: string) => {
  if (field === 'serialNumber') {
    isInputDisabled.value = 'taxCode';
    queryForm.value.taxCode = '';
  }
  else if (field === 'taxCode') {
    isInputDisabled.value = 'serialNumber';
    queryForm.value.serialNumber = '';
  }
};

const getUuid = () => {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
    const r = Math.random() * 16 | 0;
    const v = c === 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16)
  })
}

function enterLogin(e: any) {
  if (e.keyCode === 13) {
    getData();
  }
}

const captchaPath = ref('');
const getCaptcha = async () => {
  queryForm.value.uuid = getUuid()
  const result = await Utils.doGetImage( '/api/captcha?uuid=' + queryForm.value.uuid)
  // let result = await  captchaPath.value = +"/api/captcha?uuid=" + queryForm.value.uuid;
  if (result) {
    const blob = new Blob([result.data], {type:'image/jpeg'})
    const url = URL.createObjectURL(blob)
    captchaPath.value = url
  } else console.log("Failed to get captcha!")
  console.log("path captcha : " + captchaPath.value)
}

const getData = () => {
  const param: {
    serialNumber: string;
    tax?: string;
    uid?: string;
  } = {
    serialNumber: queryForm.value.serialNumber,
  };

  if (selectedValues.value[0]) {
    param.tax = queryForm.value.taxCode;
  } else {
    param.uid = queryForm.value.taxCode;
  }

  getSerialNumber(param)
      .then(({ data: response }) => {
        if (response.code === '0000') {
          data.value = response.data;

          // const base64String = data.value.base64Cert;
          // if (base64String) {
          //   const fileName = extractCommonName("company", data.value.subjectDn) + '.cer';
          //   const mimeType = 'application/x-x509-ca-cert';
          //
          //   fileToDownload.value = base64ToFile(base64String, fileName, mimeType);
          //
          // } else {
          //   console.error('Base64 string is undefined or missing in the response.');
          // }
        }
      })
      .catch(error => {
        open4()
      });
};

const search =  () => {
  getData()
  getCaptcha()
  queryForm.value.verifyCode = ''
  ui.value.typeChoose = selectedValues.value[0]
}

onMounted(() => {
  getCaptcha();
});

watch(selectedValues, (newValue) => {
  ui.value.organizationType = newValue.includes(1);
});

const formatDate = (dateString: string) => {
  const date = new Date(dateString);
  return date.toLocaleDateString('vi-VN') + ' ' + date.toLocaleTimeString('vi-VN'); // Định dạng ngày giờ theo chuẩn Việt Nam
};

const base64ToFile = (base64String: string, fileName: string, mimeType: string): File => {
  const isBase64 = /^data:([A-Za-z-+/]+);base64,(.+)$/.test(base64String);

  if (isBase64) {
    const matches = base64String.match(/^data:([A-Za-z-+/]+);base64,(.+)$/);
    if (matches && matches[2]) {
      base64String = matches[2];
    } else {
      throw new Error('Invalid Base64 string');
    }
  }

  const byteString = atob(base64String);
  const ab = new ArrayBuffer(byteString.length);
  const ia = new Uint8Array(ab);

  for (let i = 0; i < byteString.length; i++) {
    ia[i] = byteString.charCodeAt(i);
  }

  const blob = new Blob([ab], {type: mimeType});
  return new File([blob], fileName, {type: mimeType});
};

const handleDownload = (_url: string) => {
  if (fileToDownload.value) {
    downloadFile(fileToDownload.value);
  } else {
    console.error('No file available to download.');
  }
};

const downloadFile = (file: File) => {
  const url = URL.createObjectURL(file);
  const link = document.createElement('a');
  link.href = url;
  link.download = file.name; // Sử dụng tên file đã đặt
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
};

const extractCommonName = (field: string, subjectDn: string) => {
  let value: string | null = null;
  if (field === "company") {
    const cnMatch = subjectDn.match(/CN=([^,]+)/);
    value = cnMatch ? cnMatch[1].trim() : null;
  } else if (field === "taxCode") {
    const uidMatch = subjectDn.match(/UniqueIdentifier=([^,]+)/);
    value = uidMatch ? uidMatch[1].trim() : null;
  }

  return value; // Trả về giá trị tìm được
};

const open4 = () => {
  ElMessage({
    message: 'Không tìm thấy thông tin Chứng thư.<br>Vui lòng kiểm tra lại',
    type: 'error',
    duration: 5000,
    showClose: true,
    dangerouslyUseHTMLString: true,
    icon: customIcon()
  })
}

</script>

<style scoped>
.input-wrapper {
  position: relative;
}

.input-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: transparent;
  cursor: pointer;
}

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

.text-desc {
  margin-bottom: 1rem;
  font-style: italic;
}

.form-search h5.text-blue {
  margin-bottom: 2rem;
}

::v-deep .box .el-form-item {
  width: 400px;
  position: relative;
}

::v-deep .el-input__inner::placeholder {
  font-size: 14px;
}

::v-deep .el-checkbox__input.is-checked+.el-checkbox__label {
  color: var(--el-text-color-regular);
}

::v-deep .el-checkbox__input.is-checked .el-checkbox__inner {
  background-color: var(--blue);
  border-color: var(--blue);
}

::v-deep .box .el-input__wrapper {
  padding: 0.5rem 1rem;
  font-size: 1rem;
}

.form-captcha {
  width: 400px;
  justify-content: space-between;
}

::v-deep .el-form-item.input-captcha {
  width: 150px;
  margin-bottom: 0;
}

::v-deep .el-form-item.input-captcha .el-input {
  --el-input-border-color: var(--light-blue-4)
}

.result {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
}

.result-item {
  width: 440px;
  overflow: hidden;
  margin-top: 1rem;
  background-color: var(--light-2);
  padding: 1rem 2rem 0;
}

.el-form-item__content p {
  margin: 0;
}

.form-item-center {
  display: flex;
  align-items: center;
}

::v-deep .result-item .el-form-item__label {
  font-weight: 600;
}

.code_img {
  width: 100px;
  height: 30px;
}

.result-item_text {
  height: 22px;
  margin-bottom: 14px;
}

.result-item_name {
  font-size: 18px;
}

.text-label {
  text-align: start;
  font-size: 15px;
  color: var(--dark-gray);
}

.text-result {
  text-align: end;
  font-size: 15px;
}

.icon-change {
  width: 26px;
  height: 26px;
  color: var(--orange);
  cursor: pointer;
}

::v-deep .el-form-item--label-top .el-form-item__label {
  margin-bottom: 0.25rem;
}

.btn-search {
  background-color: var(--light-blue-5);
  width: 100px;
  height: 3rem;
}

@media (max-width: 768px) {
  .container {
    width: auto;
  }

  .box {
    padding: 1rem 2rem;
  }

  .container .text-header {
    font-size: 18px;
    margin-bottom: 0.5rem;
    text-align: center;
  }

  .text-desc {
    font-size: 12px;
    color: var(--blue);
  }

  .form-search h5.text-blue {
    font-size: 15px;
    margin-bottom: 1.5rem;
  }

  ::v-deep .el-form-item.input-captcha {
    width: 180px!important;
  }

  .result-text_header {
    font-size: 1rem;
    text-align: center;
    margin-bottom: 0;
  }

  .result-item {
    padding: 1rem 1.75rem;
  }

  .result-item h5.text-blue {
    font-size: 15px;
  }

  .form-captcha {
    justify-content: unset;
  }

  .code_img {
    margin: 0 1rem;
  }

  .btn-search {
    width: 50%;
    margin: 1rem auto;
    padding: 0.75rem 0;
  }

  ::v-deep .box .el-form-item {
    width: 330px;
  }

  .text-label, .text-result {
    font-size: 13px;
  }
}

</style>
