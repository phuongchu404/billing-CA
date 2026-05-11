<template>
  <div class="intro">
    <div class="container d-flex flex-wrap align-items-center justify-content-center">
      <div class="text-intro">
        <h1 class="text-blue fw-bold text-header">Chữ ký số -  An toàn giao dịch,
          <br>
          Vững chắc tương lai
        </h1>
        <p class="text-desc">Giải pháp chữ ký số từ xa được <a href="" target="_blank" class="text-blue"><i>Bộ TT&TT cấp phép</i></a>, đảm bảo an toàn và tuân thủ pháp lý Việt Nam và quốc tế.</p>
        <button class="btn btn-primary rounded-pill bg-blue px-4 py-3" @click="triggerOpenDialog">Đăng Ký Ngay</button>
      </div>
      <div class="img-intro">
        <img :src="CAHeader" alt="" class="w-100">
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import CAHeader from "@/assets/images/CA-Header.png";
import Axios from 'axios';

const emit = defineEmits(['open-dialog']);
// const triggerOpenDialog = () => {
//   Axios({
//     url: 'http://10.30.101.74:15555/api/document/file.rar',
//     method: 'GET',
//     responseType: 'blob',
//   }).then((response:any) => {
//     // Add BOM to the beginning of the content
//     const bom = new Uint8Array([0xEF, 0xBB, 0xBF]);
//     const contentWithBom = new Blob([bom, response.data]);
//
//     const url = window.URL.createObjectURL(contentWithBom);
//     const link = document.createElement('a');
//     link.href = url;
//     const file = 'file.rar';
//     link.setAttribute('download', file);
//     document.body.appendChild(link);
//     link.click();
//   });
// };
const triggerOpenDialog = () => {
  // Axios({
  //   url: 'http://10.30.101.74:15555/api/document/test.xlsx',
  //   method: 'GET',
  //   responseType: 'arraybuffer', // Use 'arraybuffer' instead of 'blob'
  // }).then((response) => {
  //   // Create a Blob from the response data
  //   const contentBlob = new Blob([response.data], { type: 'application/octet-stream' });
  //
  //   // Create a URL for the Blob
  //   const url = window.URL.createObjectURL(contentBlob);
  //
  //   // Create a link element for download
  //   const link = document.createElement('a');
  //   link.href = url;
  //   link.download = 'file.xlsx'; // Set the desired filename
  //
  //   // Trigger the download
  //   document.body.appendChild(link);
  //   link.click();
  //
  //   // Clean up
  //   document.body.removeChild(link);
  // });
  emit('open-dialog');
};

</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Poltawski+Nowy:wght@400;500;700&display=swap');

.intro{
  background: linear-gradient(var(--light-blue), var(--white));
  margin-bottom: 3rem;
}

.text-intro,
.img-intro {
  width: 50%;
}

.img-intro img{
  animation: right 1s ease forwards;
}

@keyframes right {
  0% {opacity: 0; transform: translateX(50%);}
  100% {opacity: 1; transform: translateX(0px);}
}

@keyframes left {
  0% {opacity: 0; transform: translateX(-50%);}
  100% {opacity: 1; transform: translateX(0px);}
}

.text-intro {
  text-align: left;
  animation: left 1s ease forwards;
}

.text-header {
  font-size: 2.5rem;
  line-height: 4rem;
  margin-bottom: 2.5rem;
  font-family: 'Poltawski Nowy', serif;
}

.text-desc {
  font-size: 1.25rem;
  line-height: 2rem;
}

@media (min-width: 1024px) {
  .container {
    padding: 1rem 2rem;
  }

  .text-header {
    font-size: 2rem;
  }

  .text-desc {
    font-size: 1rem;
  }
}

@media (max-width: 768px) {
  .container {
    flex-direction: column;
    align-items: center;
  }

  .text-intro {
    width: 100%;
    text-align: center;
    padding: 2rem;
  }

  .img-intro {
    order: 1;
    margin: 1rem 0;
    width: 230px;
  }

  .text-header {
    font-size: 22px;
    line-height: 30px;
    order: 0;
  }

  .text-desc {
    font-size: 13px;
    line-height: 20px;
    order: 2;
  }

  .btn {
    order: 3;
  }
}


</style>