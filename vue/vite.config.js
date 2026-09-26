import {defineConfig, loadEnv} from 'vite'
import createVitePlugins from './vite/plugins'


const {resolve} = require('path')
export default defineConfig(({mode, command}) => {
    const env = loadEnv(mode, process.cwd())
    return {
        build: {
            commonjsOptions: {
                transformMixedEsModules: true,
                include: [],
            },
        },
        define: {
            kkfileUrl: JSON.stringify(env.VITE_KKFILEVIEW_URL),
        },
        optimizeDeps: {
            disabled: false,
        },
        server: {
            host: '0.0.0.0', // 允许外部访问
            port: 8080,
            proxy: {
                '/api/agent': {
                    target: 'http://127.0.0.1:8099',
                    changeOrigin: true
                },
                '/api/al': {
                    target: 'http://pig-gateway:9999',
                    changeOrigin: true,
                    rewrite: (path) => path.replace(/^\/api\/al/, '/lk/api')
                },
                '/api/model3d': {
                    target: 'http://pig-gateway:9999',
                    changeOrigin: true,
                    rewrite: (path) => path.replace(/^\/api\/model3d/, '/model3dlk')
                },
                '/api/flink': {
                    target: 'http://pig-gateway:9999',
                    changeOrigin: true,
                    rewrite: (path) => path.replace(/^\/api\/flink/, '/lk/api')
                },
                '/api/kg': {
                    target: 'http://pig-gateway:9999',
                    changeOrigin: true,
                    rewrite: (path) => path.replace(/^\/api\/kg/, '/qlt')
                },
                '/api/remote': {
                    target: 'http://192.168.65.34:8000',
                    changeOrigin: true,
                    rewrite: (path) => path.replace(/^\/api\/remote/, '')
                },
                '/api/chat': {
                    target: 'http://192.168.65.34:7861',
                    changeOrigin: true,
                    rewrite: (path) => path.replace(/\/api\/chat/, '')
                },
                '/api': {
                    target: 'http://pig-gateway:9999',
                    changeOrigin: true,
                    rewrite: (path) => path.replace(/^\/api/, '')
                },
                '/flink': {
                    target: 'http://pig-gateway:9999/zyn/api',
                    changeOrigin: true,
                    rewrite: (path) => path.replace(/^\/flink/, '')
                }
            }
        },
        resolve: {
            alias: {
                "@": resolve(__dirname, "./src"),
                "components": resolve(__dirname, "./src/components"),
                "styles": resolve(__dirname, "./src/styles"),
                "utils": resolve(__dirname, "./src/utils"),
            },
            extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json', '.vue']
        },
        plugins: [
            createVitePlugins(env, command === 'build')
        ]
    }
})
