<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">{{ t('settings.title') }}</h1>
    </div>

    <el-tabs v-model="activeTab" class="settings-tabs">

      <!-- ── General ───────────────────────────────────────────── -->
      <el-tab-pane :label="t('settings.generalTab')" name="general">
        <el-card shadow="never" v-loading="loading">
          <el-table :data="generalMeta" stripe style="width:100%">
            <el-table-column :label="t('settings.settingDesc')" prop="description" min-width="260" />
            <el-table-column :label="t('settings.settingType')" width="110">
              <template #default="{ row }">
                <el-tag
                  size="small"
                  :type="(typeTagMap[row.type as keyof typeof typeTagMap] as any) ?? 'info'"
                >{{ row.type }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column :label="t('settings.settingValue')" min-width="180">
              <template #default="{ row }">
                <el-switch
                  v-if="row.type === 'BOOLEAN'"
                  v-model="general[row.key]"
                  active-value="true"
                  inactive-value="false"
                />
                <el-input-number
                  v-else-if="row.type === 'INTEGER'"
                  :model-value="Number(general[row.key] || 0)"
                  @update:model-value="(v: number | undefined) => general[row.key] = String(v ?? 0)"
                  style="width:140px"
                />
                <el-input
                  v-else
                  v-model="general[row.key]"
                  style="max-width:320px"
                />
              </template>
            </el-table-column>
          </el-table>

          <div class="card-footer">
            <el-button type="primary" :loading="saving" @click="saveGeneral">{{ t('common.save') }}</el-button>
          </div>
        </el-card>
      </el-tab-pane>

      <!-- ── Email Configuration ────────────────────────────── -->
      <el-tab-pane :label="t('settings.emailTab')" name="email">
        <el-card shadow="never" v-loading="loading">
          <el-form :model="email" label-width="200px" label-position="left" class="settings-form">

            <el-form-item :label="t('settings.emailEnabled')">
              <el-switch v-model="email['email.enabled']" active-value="true" inactive-value="false" />
              <span class="hint">{{ t('settings.emailEnabledHint') }}</span>
            </el-form-item>

            <el-divider />

            <el-form-item :label="t('settings.smtpHost')">
              <el-input v-model="email['email.host']" placeholder="smtp.gmail.com" style="max-width:360px" />
            </el-form-item>
            <el-form-item :label="t('settings.smtpPort')">
              <el-input v-model="email['email.port']" placeholder="587" style="max-width:120px" />
            </el-form-item>
            <el-form-item :label="t('settings.smtpUsername')">
              <el-input v-model="email['email.username']" autocomplete="off" style="max-width:360px" />
            </el-form-item>
            <el-form-item :label="t('settings.smtpPassword')">
              <el-input v-model="email['email.password']" type="password" show-password autocomplete="new-password" style="max-width:360px" />
            </el-form-item>
            <el-form-item :label="t('settings.fromAddress')">
              <el-input v-model="email['email.from.address']" placeholder="noreply@example.com" style="max-width:360px" />
            </el-form-item>
            <el-form-item :label="t('settings.fromName')">
              <el-input v-model="email['email.from.name']" style="max-width:360px" />
            </el-form-item>
            <el-form-item :label="t('settings.sslEnabled')">
              <el-switch v-model="email['email.ssl.enabled']" active-value="true" inactive-value="false" />
              <span class="hint">{{ t('settings.sslHint') }}</span>
            </el-form-item>
          </el-form>

          <div class="card-footer">
            <el-button @click="openTestEmail">{{ t('settings.testEmail') }}</el-button>
            <el-button type="primary" :loading="saving" @click="saveEmail">{{ t('common.save') }}</el-button>
          </div>
        </el-card>
      </el-tab-pane>

      <!-- ── Notifications ──────────────────────────────────── -->
      <el-tab-pane :label="t('settings.notificationTab')" name="notification">
        <el-card shadow="never" v-loading="loading">
          <el-form :model="notif" label-width="200px" label-position="left" class="settings-form">

            <el-form-item :label="t('settings.expiryNotifEnabled')">
              <el-switch v-model="notif['notification.expiry.enabled']" active-value="true" inactive-value="false" />
              <span class="hint">{{ t('settings.expiryNotifHint') }}</span>
            </el-form-item>

            <el-divider />

            <el-form-item :label="t('settings.daysBefore')">
              <el-input v-model="notif['notification.expiry.days_before']" placeholder="7,3,1" style="max-width:200px" />
              <span class="hint">{{ t('settings.daysBeforeHint') }}</span>
            </el-form-item>
            <el-form-item :label="t('settings.emailSubject')">
              <el-input v-model="notif['notification.expiry.subject']" style="max-width:480px" />
            </el-form-item>
            <el-form-item :label="t('settings.emailBody')">
              <div style="width:100%;max-width:560px">
                <el-input v-model="notif['notification.expiry.body']" type="textarea" :rows="6" />
                <div class="hint" style="margin-top:4px">{{ t('settings.bodyPlaceholders') }}</div>
              </div>
            </el-form-item>
          </el-form>

          <div class="card-footer">
            <el-button :loading="triggering" @click="doTrigger">{{ t('settings.triggerNow') }}</el-button>
            <el-button type="primary" :loading="saving" @click="saveNotif">{{ t('common.save') }}</el-button>
          </div>
        </el-card>
      </el-tab-pane>

    </el-tabs>

    <!-- Test-email dialog -->
    <el-dialog v-model="testVisible" :title="t('settings.testEmail')" width="400px">
      <el-form :model="testForm" label-width="110px">
        <el-form-item :label="t('settings.recipient')">
          <el-input v-model="testForm.recipient" type="email" placeholder="you@example.com" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="testVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="testing" @click="doTestEmail">{{ t('settings.send') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { getSettings, updateSettings, testEmail, triggerNotifications } from '@/api/settings'
import type { SettingItem } from '@/api/settings'

const { t } = useI18n()

const activeTab = ref('email')
const loading   = ref(false)
const saving    = ref(false)
const testing   = ref(false)
const triggering = ref(false)

// Settings maps (key → value)
const email = reactive<Record<string, string>>({})
const notif = reactive<Record<string, string>>({})
const general = reactive<Record<string, string>>({})
const generalMeta = ref<SettingItem[]>([])

const typeTagMap = { BOOLEAN: 'success', INTEGER: 'warning', STRING: '' } as const

function applySettings(target: Record<string, string>, items: SettingItem[]) {
  items.forEach(s => { target[s.key] = s.value })
}

async function fetchAll() {
  loading.value = true
  try {
    const [emailRes, notifRes, generalRes] = await Promise.all([
      getSettings('EMAIL'),
      getSettings('NOTIFICATION'),
      getSettings('GENERAL'),
    ])
    applySettings(email, emailRes.data ?? [])
    applySettings(notif, notifRes.data ?? [])
    const generalItems: SettingItem[] = generalRes.data ?? []
    generalMeta.value = generalItems
    applySettings(general, generalItems)
  } finally {
    loading.value = false
  }
}

onMounted(fetchAll)

async function saveGeneral() {
  saving.value = true
  try {
    const res = await updateSettings('GENERAL', { ...general })
    const items: SettingItem[] = res.data ?? []
    generalMeta.value = items
    applySettings(general, items)
    ElMessage.success(t('settings.saved'))
  } catch {
    ElMessage.error(t('settings.saveFailed'))
  } finally {
    saving.value = false
  }
}

async function saveEmail() {
  saving.value = true
  try {
    const res = await updateSettings('EMAIL', { ...email })
    applySettings(email, res.data ?? [])
    ElMessage.success(t('settings.saved'))
  } catch {
    ElMessage.error(t('settings.saveFailed'))
  } finally {
    saving.value = false
  }
}

async function saveNotif() {
  saving.value = true
  try {
    const res = await updateSettings('NOTIFICATION', { ...notif })
    applySettings(notif, res.data ?? [])
    ElMessage.success(t('settings.saved'))
  } catch {
    ElMessage.error(t('settings.saveFailed'))
  } finally {
    saving.value = false
  }
}

// Test email
const testVisible = ref(false)
const testForm = reactive({ recipient: '' })

function openTestEmail() {
  testForm.recipient = ''
  testVisible.value = true
}

async function doTestEmail() {
  if (!testForm.recipient) return
  testing.value = true
  try {
    await testEmail(testForm.recipient)
    ElMessage.success(t('settings.testSent'))
    testVisible.value = false
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || t('settings.testFailed'))
  } finally {
    testing.value = false
  }
}

// Trigger notifications
async function doTrigger() {
  triggering.value = true
  try {
    await triggerNotifications()
    ElMessage.success(t('settings.triggerDone'))
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || t('settings.triggerFailed'))
  } finally {
    triggering.value = false
  }
}
</script>

<style scoped>
.page-container {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.page-header {
  display: flex;
  align-items: center;
}
.page-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
}
.settings-tabs { flex: 1; }
.settings-form { max-width: 800px; padding: 8px 0; }
.hint {
  font-size: 12px;
  color: #909399;
  margin-left: 10px;
  line-height: 1.4;
}
.card-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}
</style>
