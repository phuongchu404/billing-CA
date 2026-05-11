// composables/useScrollAnimation.ts
import { ref, onMounted, onUnmounted } from 'vue';

export function useScrollAnimation(items: any[], type: string) {
    const itemVisible = ref(items.map(() => false)); // Trạng thái hiển thị cho từng item
    const observer = ref<IntersectionObserver | null>(null);

    const handleIntersection = (entries: IntersectionObserverEntry[]) => {
        entries.forEach((entry) => {
            const index = parseInt(entry.target.id.split('-')[2]);

            if (entry.isIntersecting) {
                // Nếu phần tử vào viewport
                setTimeout(() => {
                    itemVisible.value[index] = true;
                }, index * 200); // Độ trễ theo index
            } else {
                // Nếu phần tử ra khỏi viewport
                itemVisible.value[index] = false;
            }
        });
    };

    onMounted(() => {
        observer.value = new IntersectionObserver(handleIntersection);

        items.forEach((_, index) => {
            const itemElement = document.getElementById(`${type}-item-${index}`);
            if (itemElement) {
                observer.value?.observe(itemElement); // Sử dụng dấu hỏi để kiểm tra
            }
        });
    });

    onUnmounted(() => {
        observer.value?.disconnect(); // Cũng sử dụng dấu hỏi ở đây
    });

    return itemVisible;
}
