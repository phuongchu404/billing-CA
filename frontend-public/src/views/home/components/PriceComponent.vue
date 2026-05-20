<template>
  <div class="price">
    <div class="container">
      <div class="text-center text-product">
        <div class="text-container">
          <h2 class="text-blue fw-bold text-product__header">Combo Dịch vụ Chứng thực Chữ ký số</h2>
          <img :src="shadow" class="background-image">
        </div>
      </div>
      <div class="row price-list">
        <div v-for="plan in displayPlans" :key="plan.subjectType" class="col-lg-4 col-md-6">
          <div class="price-card shadow-sm">
            <div class="price-card__icon">
              <img
                v-if="plan.iconUrl"
                :src="plan.iconUrl"
                :alt="plan.cardName"
                @error="(e: Event) => ((e.target as HTMLImageElement).src = fallbackIconFor(plan.subjectType))"
              />
              <img v-else :src="fallbackIconFor(plan.subjectType)" :alt="plan.cardName" />
            </div>
            <h3>{{ plan.cardName }}</h3>
            <div class="price-card__price">
              <template v-if="plan.minFeeFormatted">
                <span class="price-card__label">từ</span>
                <span class="price-card__amount">{{ plan.minFeeFormatted }}</span>
              </template>
            </div>
            <ul>
              <li v-for="feature in plan.features" :key="feature">
                <span class="price-card__check">✓</span>
                <span>{{ feature }}</span>
              </li>
            </ul>
            <button class="price-card__button" type="button" @click="emit('open-dialog')">
              Đăng Ký Ngay
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import axios from 'axios'
import shadow from '@/assets/images/shadow.svg'
import planeIcon from '@/assets/images/icon-plane.svg'
import planeAltIcon from '@/assets/images/icon-plane1.svg'
import rocketIcon from '@/assets/images/icon-rocket.svg'

const emit = defineEmits(['open-dialog'])

const apiBaseUrl = import.meta.env.VITE_API_URL as string

// ─── Static card metadata (name, fallback icon) per subject type ─────────────

const SUBJECT_META: Record<string, { cardName: string; fallbackIcon: string }> = {
  INDIVIDUAL: { cardName: 'Cá nhân', fallbackIcon: planeIcon },
  ORGANIZATION: { cardName: 'Tổ chức', fallbackIcon: planeAltIcon },
  INDIVIDUAL_OF_ORG: { cardName: 'Cá nhân thuộc Tổ chức', fallbackIcon: rocketIcon },
}

const SUBJECT_ORDER = ['INDIVIDUAL', 'ORGANIZATION', 'INDIVIDUAL_OF_ORG']

// ─── Plan data from API ───────────────────────────────────────────────────────

interface PublicPlanCard {
  subjectType: string
  planName: string
  iconUrl: string | null
  minFee: number | null
  minFeeFormatted: string | null
  features: string[]
}

const apiPlans = ref<PublicPlanCard[]>([])

const displayPlans = computed(() => {
  if (apiPlans.value.length === 0) {
    // Khi API không trả dữ liệu, hiển thị 3 card rỗng giữ layout
    return SUBJECT_ORDER.map(subjectType => ({
      subjectType,
      cardName: SUBJECT_META[subjectType].cardName,
      iconUrl: null,
      displayPrice: null,
      features: [],
    }))
  }

  return SUBJECT_ORDER.map(subjectType => {
    const card = apiPlans.value.find(c => c.subjectType === subjectType)
    return {
      subjectType,
      cardName: SUBJECT_META[subjectType].cardName,
      iconUrl: card?.iconUrl ?? null,
      minFeeFormatted: card?.minFeeFormatted ?? null,
      features: card?.features ?? [],
    }
  })
})

function fallbackIconFor(subjectType: string): string {
  return SUBJECT_META[subjectType]?.fallbackIcon ?? planeIcon
}

async function fetchPlans() {
  try {
    const res = await axios.get<PublicPlanCard[]>(`${apiBaseUrl}/api/v1/public/plans`)
    apiPlans.value = res.data ?? []
  } catch {
    // Silently fall back to empty; static card names still render
  }
}

// ─── SSE — auto-refresh when admin publishes a plan change ───────────────────
// Kết nối SSE đến /api/v1/public/plan-updates/stream.
// Khi backend phát sự kiện "plan-updated", gọi lại fetchPlans().
// Browser tự reconnect khi mất kết nối.

let sseSource: EventSource | null = null

function connectSse() {
  const url = `${apiBaseUrl}/api/v1/public/plan-updates/stream`
  sseSource = new EventSource(url)

  sseSource.addEventListener('plan-updated', () => {
    fetchPlans()
  })

  sseSource.onerror = () => {
    // Browser sẽ tự reconnect; không cần xử lý thêm ở đây
  }
}

onMounted(() => {
  fetchPlans()
  connectSse()
})

onUnmounted(() => {
  sseSource?.close()
})
</script>

<style scoped>
.price {
  background-image: linear-gradient(var(--light-1), var(--white));
  min-height: 830px;
  padding-top: 5rem;
  padding-bottom: 2.5rem;
}

.text-product {
  margin-bottom: 2.5rem;
}

.text-container {
  position: relative;
}

.background-image {
  position: absolute;
  top: 75%;
  left: 25%;
}

.text-product__header {
  position: relative;
  z-index: 1;
}

.price-list {
  row-gap: 1.5rem;
}

.price-card {
  background: var(--white);
  border: 1px solid var(--light-2);
  border-radius: 0.375rem;
  min-height: 630px;
  padding: 3rem 2rem 1.4rem;
  text-align: left;
}

.price-card__icon {
  height: 115px;
  margin-bottom: 1rem;
  text-align: center;
}

.price-card__icon img {
  height: 95px;
  max-width: 120px;
  object-fit: contain;
}

.price-card h3 {
  color: #2f3b4f;
  font-size: 24px;
  font-weight: 500;
  margin-bottom: 0.5rem;
  text-align: center;
}

.price-card__price {
  align-items: baseline;
  display: flex;
  justify-content: center;
  margin-bottom: 2.2rem;
  min-height: 48px;
}

.price-card__label {
  color: var(--gray);
  font-size: 14px;
  margin-right: 4px;
  align-self: center;
}

.price-card__amount {
  color: var(--blue);
  font-size: 36px;
  font-weight: 700;
  line-height: 1;
}

.price-card ul {
  list-style: none;
  margin: 0 0 2rem;
  padding: 0;
}

.price-card li {
  align-items: center;
  color: #111;
  display: flex;
  font-size: 16px;
  gap: 1rem;
  margin-bottom: 1rem;
}

.price-card__check {
  align-items: center;
  background: var(--light-blue);
  border-radius: 50%;
  color: var(--blue);
  display: inline-flex;
  flex: 0 0 16px;
  font-size: 11px;
  font-weight: 700;
  height: 16px;
  justify-content: center;
  width: 16px;
}

.price-card__button {
  background: #fff6df;
  border: none;
  border-radius: 0.375rem;
  color: #ffb400;
  font-size: 16px;
  font-weight: 600;
  min-height: 40px;
  width: 100%;
}

@media (max-width: 768px) {
  .price {
    min-height: auto;
    padding-top: 0;
  }

  .text-product__header {
    font-size: 18px;
  }

  .background-image {
    left: 0;
  }

  .price-card {
    min-height: auto;
    padding: 2rem 1.5rem 1.25rem;
  }
}
</style>
