
const taskConfig = {
    state: {
        scadaTrainUrl: null,
        scadaTestUrl: null,
        cmsTrainUrl: null,
        cmsTestUrl: null,
        modelParamsList: [],
        wavePacket:null,
        wavePacketImage:''
    },
    mutations: {
        setScadaTrainUrl(state, path) {
            state.scadaTrainUrl = path;
        },
        setScadaTestUrl(state, path) {
            state.scadaTestUrl = path;
        },
        setCmsTrainUrl(state, path) {
            state.cmsTrainUrl = path;
        },
        setCmsTestUrl(state, path) {
            state.cmsTestUrl = path;
        },
        clearModelParams(state) {
            state.modelParamsList = [];
            state.wavePacket=null
            state.scadaTrainUrl=null
            state.scadaTestUrl= null
            state.cmsTrainUrl= null
            state.cmsTestUrl= null
        },
        addModelParams(state,params){
            const existingIndex = state.modelParamsList.findIndex(item => item.alName === params.alName);
            if (existingIndex !== -1) {
                state.modelParamsList[existingIndex] = { ...state.modelParamsList[existingIndex], ...params };
            } else {
                state.modelParamsList.push(params);
            }
        },
        setWavePacket(state,value){
            state.wavePacket=value
        },
        setWavePacketImage(state,value){
            state.wavePacketImage=value
        }

    },
    getters: {
        getScadaTrainUrl(state) {
            return state.scadaTrainUrl;
        },
        getScadaTestUrl(state) {
            return state.scadaTestUrl;
        },
        getCmsTrainUrl(state) {
            return state.cmsTrainUrl;
        },
        getCmsTestUrl(state) {
            return state.cmsTestUrl;
        },
        getAllModelParams(state) {
            return state.modelParamsList;
        },
        getWavePackt(state){
            return state.wavePacket
        },
        getWavePacketParamsByAlName: (state) => (alName) => {
            return state.modelParamsList.filter(item => item.alName === alName) || null;
        },
        getWavePacketImage(state) {
            return state.wavePacketImage
        }

    }
}

export default taskConfig
