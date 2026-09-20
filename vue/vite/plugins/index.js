import vue from '@vitejs/plugin-vue'
import createCompression from './compression'

////////
import vueJsx from '@vitejs/plugin-vue-jsx';
import reactRefresh from '@vitejs/plugin-react-refresh';
////////

export default function createVitePlugins(viteEnv, isBuild = false) {
    const vitePlugins = [
        vue(),
        vueJsx(),
        reactRefresh(),
    ]
    isBuild && vitePlugins.push(...createCompression(viteEnv))
    return vitePlugins
}
