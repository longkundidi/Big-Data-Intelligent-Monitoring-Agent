import { createStore } from 'vuex'
import user from './modules/user'
import common from './modules/common'
import tags from './modules/tags'
import logs from './modules/logs'
import getters from './getters'
import kg from "@/store/modules/kg";
import taskConfig from "@/store/modules/taskConfig";
const store = createStore({
  modules: {
    user,
    common,
    logs,
    tags,
    kg,
    taskConfig
  },
  getters
})

export default store
