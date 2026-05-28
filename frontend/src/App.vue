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
  --el-text-color-primary:      #2F2B3DE5;
  --el-text-color-regular:      #2F2B3DB2;
  --el-text-color-secondary:    rgba(47, 43, 61, 0.7);
  --el-text-color-placeholder:  rgba(47, 43, 61, 0.4);

  /* ── Cards ── */
  --el-card-border-radius:      6px;
  --card-shadow:                0 3px 12px rgba(47, 43, 61, 0.14);

  /* ── Success: #28C76F ── */
  --el-color-success:           #24B364;
  --el-color-success-dark-2:    #20A85A;
  --el-color-success-light-3:   #64D493;
  --el-color-success-light-5:   #93E2B7;
  --el-color-success-light-7:   #C2F0DA;
  --el-color-success-light-8:   #D8F5E8;
  --el-color-success-light-9:   #EDFAF3;

  /* ── Warning: #FF9F43 ── */
  --el-color-warning:           #FF9F43;
  --el-color-warning-dark-2:    #CC7F36;
  --el-color-warning-light:   #FF9F4329;
  --el-color-warning-light-3:   #FFB673;
  --el-color-warning-light-5:   #FFCF99;
  --el-color-warning-light-7:   #FFE5BsF;
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

  --color-light-grey: #80839014;
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

/* Keep Element Plus sort carets on the same row as custom table labels.
   Header slots with filter controls are taller, so the default inline caret
   falls below the label unless it is pinned to the top label line. */
.el-table th.el-table__cell.is-sortable > .cell {
  position: relative;
  padding-right: 32px;
}

.el-table th.el-table__cell.is-sortable > .cell > .caret-wrapper {
  position: absolute;
  top: 5px;
  right: 8px;
  margin-left: 0;
}

.el-table th.el-table__cell > .cell:has(> .col-filter) {
  min-height: 74px;
  position: relative;
}

.el-table th.el-table__cell > .cell > .col-filter {
  left: 12px;
  margin-top: 0;
  min-height: 32px;
  position: absolute;
  right: 12px;
  top: 38px;
}

.el-table--small th.el-table__cell > .cell > .col-filter {
  left: 8px;
  right: 8px;
}

/*form*/

#app .el-input__wrapper,
#app .el-range-editor .el-input__wrapper,
#app .el-select__wrapper {
  font-size: 15px;
}

#app .el-form-item__label {
  font-size: 1rem;
  font-weight: 500;
  color: var(--el-text-color-regular);
}

/* page-header */
#app  .page-header {
  margin-bottom: 1.5rem;
}

#app .page-header h2 {
  margin: 0 0 0.5rem 0;
  color: var(--el-text-color-primary);
  font-weight: 500;
}

/*section*/
#app .section-card {
  margin-bottom: 1rem;
}

#app .section-title {
  color: var(--el-color-primary);
  font-weight: 700;
  font-size: 18px;
  margin-bottom: 0.5rem;
}

/*dialog*/
#app .el-dialog {
  border-radius: 0.5rem;
  padding: 1.5rem 2.25rem;
}

/*custom button*/
#app .btn {
  border-radius: 0.5rem;
  padding: 0.6875rem 1.26rem;
  min-height: 3rem;
  min-width: 150px;
  font-size: 17px;
  color: var(--el-text-color-primary);
}

#app .btn-cancel {
  color: #808390;
  background-color: #8083903D;
  border: none;
}

#app .btn-primary {
  background-color: transparent;
  color: var(--el-color-primary);
  border-color: var(--el-color-primary);
  border-radius: 6px;
  padding: 0.5rem 1.25rem;
  min-height: 38px;
  min-width: 150px;
}

#app .btn-regular {
  border-color: var(--el-text-color-regular);
}

#app .btn-warning {
  background-color: var(--el-color-warning-light) !important;
  color: var(--el-color-warning);
  border: none;
}

/* notes */
#app .plan-notes {
  margin-top: 0.5rem;
  font-size: 17px;
  color: var(--el-text-color-regular);
  line-height: 1.6;
  font-style: italic;
}

#app .plan-notes p {
  margin: 0;
}

/* Text colors */
#app .text-color-primary {
  color: var(--el-color-primary)!important;
}

#app .text-primary,
#app .el-dialog__title  {
  color: var(--el-text-color-primary);
  font-size: 18px;
  font-weight: 500;
  line-height: 28px;
}

#app .text-regular {
  color: var(--el-text-color-regular);
  font-size: 18px;
  font-weight: 500;
  line-height: 28px;
}

#app p.text-regular {
  margin: 0;
}

#app .field-hint {
  font-size: 12px;
  color: var(--el-text-color-regular);
  margin: 0 0 0 1rem;
  font-style: italic;
  height: 1rem;
}

/*tags*/
#app .custom-tag {
  border: none !important;      
  font-weight: 600;             
  border-radius: 4px;           
  padding: 0 10px;
  height: 28px;      
  font-size: 13px;     
  width: 115px; 
  justify-content: center;      
}

#app .tag-applying {
  background-color: #1B60CB3D !important; 
  color: var(--el-color-primary) !important;
}

#app .tag-approved,
#app .tag-pending,
#app .tag-active {
  background-color: var(--color-light-grey) !important; 
  color: var(--el-color-primary) !important;
}

#app .tag-available {
  background-color: var(--color-light-grey) !important; 
  color: #808390 !important;
}

#app .tag-unavailable {
  background-color: transparent !important; 
  color: #808390 !important;
  padding: 0 !important;
}

#app .tag-paused {
  background-color: transparent !important; 
  color: var(--el-color-warning) !important;
  padding: 0 !important;
}

/* Action buttons */
#app .action-btns {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem; 
}

/* action buttons  */
#app .action-btns .el-button {
  font-weight: 600;
  color: var(--el-text-color-primary);
  border-color: var(--el-text-color-primary);
  font-size: 13px;
  line-height: 18px;
}

#app .action-btns .el-button:hover {
  color: var(--el-color-primary);
  border-color: var(--el-color-primary);
  background-color: #fff;
}

#app .action-btns .btn-light-blue {
  background-color: #1B60CB29 !important; 
  border-color: transparent !important; 
  color: var(--el-color-primary) !important; 
}

#app .action-btns .btn-light-blue:hover {
  background-color: #1B60CB3D !important;
}

#app .action-btns .el-button--primary.is-plain {
  background-color: #fff !important;
  border-color: var(--el-color-primary) !important;
  color: var(--el-color-primary) !important;
}

#app .action-btns .el-button--primary.is-plain:hover {
  background-color: #f0f5ff !important;
}

#app .action-btns .el-button+.el-button {
  margin: 0;
}


/* ── Custom class ── */
.text-italic {
  font-style: italic;
}
</style>
