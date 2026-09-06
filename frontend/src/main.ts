import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { useThemeStore } from './stores/theme'
import './style.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)

// Apply persisted/OS-preferred theme before mount to avoid flash-of-wrong-theme.
const themeStore = useThemeStore()
themeStore.init()

app.mount('#app')
