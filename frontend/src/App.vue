<template>
  <el-config-provider :locale="locale">
    <router-view />
  </el-config-provider>
</template>

<script setup lang="ts">
import en from 'element-plus/es/locale/lang/en'
const locale = en
</script>

<style>
* {
  font-family: 'Public Sans', sans-serif;
}

:root {
  /* ── Primary: #1B60CB ── */
  --el-color-primary:           #1B60CB;
  --el-color-primary-dark-2:    #1650A2;
  --el-color-primary-light-3:   #5F8FDB;
  --el-color-primary-light-5:   #8DB0E5;
  --el-color-primary-light-7:   #BBCFEF;
  --el-color-primary-light-8:   #D1DFF5;
  --el-color-primary-light-9:   #E8EFF9;

  /* ── Backgrounds ── */
  --el-bg-color:                #FFFFFF;
  --el-bg-color-page:           #F0F2F7;
  --el-bg-color-overlay:        #FFFFFF;
  --el-fill-color-blank:        #FFFFFF;
  --el-fill-color-light:        #F0F2F7;
  --el-fill-color-lighter:      #F5F6FA;
  --el-fill-color:              #F0F2F7;

  /* ── Borders ── */
  --el-border-color:            #B5C3DB;
  --el-border-color-light:      #C4CEE2;
  --el-border-color-lighter:    #D3DBE9;
  --el-border-color-extra-light:#E1E7F1;

  /* ── Text ── */
  --el-text-color-primary:      #2F2B3D;
  --el-text-color-regular:      #2F2B3D;
  --el-text-color-secondary:    rgba(47, 43, 61, 0.7);
  --el-text-color-placeholder:  rgba(47, 43, 61, 0.4);

  /* ── Cards ── */
  --el-card-border-radius:      6px;
  --card-shadow:                0 3px 12px rgba(47, 43, 61, 0.14);

  /* ── Success: #28C76F ── */
  --el-color-success:           #28C76F;
  --el-color-success-dark-2:    #20A85A;
  --el-color-success-light-3:   #64D493;
  --el-color-success-light-5:   #93E2B7;
  --el-color-success-light-7:   #C2F0DA;
  --el-color-success-light-8:   #D8F5E8;
  --el-color-success-light-9:   #EDFAF3;

  /* ── Warning: #FF9F43 ── */
  --el-color-warning:           #FF9F43;
  --el-color-warning-dark-2:    #CC7F36;
  --el-color-warning-light-3:   #FFB673;
  --el-color-warning-light-5:   #FFCF99;
  --el-color-warning-light-7:   #FFE5BF;
  --el-color-warning-light-8:   #FFEED9;
  --el-color-warning-light-9:   #FFF6EC;

  /* ── Danger: #EA5455 ── */
  --el-color-danger:            #EA5455;
  --el-color-danger-dark-2:     #BB4344;
  --el-color-danger-light-3:    #EF7E7F;
  --el-color-danger-light-5:    #F4A9A9;
  --el-color-danger-light-7:    #F9D4D4;
  --el-color-danger-light-8:    #FBE4E4;
  --el-color-danger-light-9:    #FDF2F2;

  /* ── Info: #A8AAAE ── */
  --el-color-info:              #A8AAAE;
  --el-color-info-dark-2:       #86888B;
  --el-color-info-light-3:      #BCBEBF;
  --el-color-info-light-5:      #D3D4D6;
  --el-color-info-light-7:      #E9EAEB;
  --el-color-info-light-8:      #F2F2F3;
  --el-color-info-light-9:      #F8F8F9;
}

/* ── Card global style ── */
.el-card {
  border-radius: 6px !important;
  border: none !important;
  box-shadow: var(--card-shadow) !important;
  background-color: #FFFFFF !important;
}

.el-card__header {
  border-bottom: 1px solid var(--el-border-color-extra-light);
  color: #1B60CB;
  font-weight: 600;
  font-size: 14px;
  text-transform: uppercase;
}
</style>
