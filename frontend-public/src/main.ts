import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap/dist/js/bootstrap.bundle.min.js';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { library } from '@fortawesome/fontawesome-svg-core'
import { fas } from '@fortawesome/free-solid-svg-icons'
import { fab } from '@fortawesome/free-brands-svg-icons'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

library.add(fas, fab)

createApp(App)
.use(store)
.use(router)
.use(ElementPlus)
.component('font-awesome-icon', FontAwesomeIcon)
.mount('#app')
