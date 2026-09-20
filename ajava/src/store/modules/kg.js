import { getStore, setStore } from '@/util/store'
import LZString from 'lz-string' // 假设您已经安装并能够引入 LZString

const kg = {
    state: {
        d2rDBInfo: getStore({
            name: 'd2rDBInfo'
        }) ? JSON.parse(LZString.decompress(getStore({name: 'd2rDBInfo'}))) : {},
        d2rSelectedTable: getStore({
            name: 'd2rSelectedTable'
        }) ? JSON.parse(LZString.decompress(getStore({name: 'd2rSelectedTable'}))) : [],
        structTypeInfo: getStore({
            name: 'structTypeInfo'
        }) ? JSON.parse(LZString.decompress(getStore({name: 'structTypeInfo'}))) : {},
        d2rSelectedTableInfo: getStore({
            name: 'd2rSelectedTableInfo'
        }) ? JSON.parse(LZString.decompress(getStore({name: 'd2rSelectedTableInfo'}))) : [],
    },
    actions: {},
    mutations: {
        SET_D2RDBINFO: (state, d2rDBInfo) => {
            state.d2rDBInfo = d2rDBInfo
            try {
                setStore({
                    name: 'd2rDBInfo',
                    content: LZString.compress(JSON.stringify(state.d2rDBInfo))
                })
            } catch (error) {
                console.error("Failed to store d2rDBInfo", error);
            }
        },
        SET_D2RSelectedTB: (state, d2rSelectedTable) => {
            state.d2rSelectedTable = d2rSelectedTable
            try {
                setStore({
                    name: 'd2rSelectedTable',
                    content: LZString.compress(JSON.stringify(state.d2rSelectedTable))
                })
            } catch (error) {
                console.error("Failed to store d2rSelectedTable", error);
            }
        },
        SET_D2RSelectedTableInfo: (state, d2rSelectedTableInfo) => {
            state.d2rSelectedTableInfo = d2rSelectedTableInfo // Update d2rSelectedTableInfo instead of d2rSelectedTable
            try {
                setStore({
                    name: 'd2rSelectedTableInfo',
                    content: LZString.compress(JSON.stringify(state.d2rSelectedTableInfo))
                })
            } catch (error) {
                console.error("Failed to store d2rSelectedTableInfo", error);
            }
        }
    }
}

export default kg
