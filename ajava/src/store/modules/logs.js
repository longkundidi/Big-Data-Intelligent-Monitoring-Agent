import { getStore, setStore } from '@/util/store'
import dayjs from 'dayjs'

const logs = {
  state: {
    logsList: getStore({ name: 'logsList' }) || []
  },
  actions: {
    // 发送错误日志
    SendLogs({ state, commit }) {
      return new Promise((resolve, reject) => {
        resolve()
      })
    }
  },
  mutations: {
    ADD_LOGS: (state, { type, message, stack, info }) => {
      const MAX_LOGS = 1000;  // 限制最大条数

      const newLog = Object.assign({
        id: state.logsList.length,
        url: window.location.href,
        time: dayjs().format('YYYY-MM-DD HH:mm:ss')
      }, {
        type,
        message,
        stack,
        info: info.toString()
      });

      state.logsList.push(newLog);

      // 仅保留最近 N 条日志
      if (state.logsList.length > MAX_LOGS) {
        state.logsList = state.logsList.slice(state.logsList.length - MAX_LOGS);
      }

      // 写入 localStorage，带 try-catch 容错
      try {
        setStore({ name: 'logsList', content: state.logsList });
      } catch (e) {
        console.warn('日志写入 localStorage 失败，可能已超出容量限制:', e);
      }
    }
    ,
    CLEAR_LOGS: (state) => {
      state.logsList = []
      setStore({ name: 'logsList', content: state.logsList })
    }
  }

}

export default logs
