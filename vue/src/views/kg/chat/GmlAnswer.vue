<template>
  <div id="container1" style="width: 100%">
    <el-main style="padding: 0;height:100% ;background-color: white">
      <div class="hh" style="display: flex; flex-direction: row; align-items: center; justify-content: space-between; width: 100%; padding: 10px;">
        <div style="flex-grow: 1; display: flex; flex-direction: column; align-items: center; justify-content: center;">
    <span style="font-size: 27px; font-weight: 900; padding-top: 8px ;">
      智能问答系统
    </span>
          <form style="font-size:14px; padding-bottom: 10px;">
            <input type="radio" id="option1" name="qamode" value="option1" v-model="selectedMode" checked>
            <label for="option1">知识库搜索问答</label>
            <input type="radio" id="option2" name="qamode" value="option2" v-model="selectedMode" style="margin-left:20px;">
            <label for="option2">大模型文档问答</label>
            <input type="radio" id="option3" name="qamode" value="option3" v-model="selectedMode" style="margin-left:20px;">
            <label for="option2">领域大模型问答</label>
          </form>
        </div>
      </div>

      <div class="main" id="div1">
        <div v-if="selectedMode=='option1'" v-for="(item,index) in list" :key="item.id" style="width:100%;">
          <div class="robot">
            <div class="robot1" v-if="item.id === 1">
              <div style="width: calc(100% - 55px); display: inline; float: left">
                <div class="clear"></div>
                <div class="aBox">
<!--显示左侧部分-->
                  <div style="width: 100%; float: left;">
                    <img src="../../../assets/kg/image/robot.png" style="width: 35px; height: 35px; vertical-align: middle; margin: 15px 5px 15px 5px; float: left;"  >
                    <div style="width: calc(100% - 55px); ;display: inline;float: left">
<!--用来显示图表-->
                      <div v-if="item.flag !== 0">
                        <ChildComponent :flag="item.flag" :tableInfo="item.tableInfo"></ChildComponent>
                      </div>
                      <div class="bBox">
                        <div class="bTalk">
                          <span v-html="item.content" style="white-space: pre-line"></span>
                          <div v-if="index >= 3" class="button-group">
                            <button @click="readText(convert(item.content))" class="sendButton">朗读问答方案</button>
                            <button @click="pauseSpeech" class="sendButton">暂停语音</button>
                            <button @click="resumeSpeech" class="sendButton">语音继续</button>
                          </div>
                        </div>
                      </div>

                    </div>
<!--                    显示图表部分-->

                  </div>
<!--                    显示右侧部分-->
                </div>

              </div>
              <div class="clear"></div>
            </div>
          </div>

          <div class="user">
            <div class="user1" v-if="item.id === 2 && item.content !== ''" style="width:100%;">
              <img src="../../../assets/kg/image/user.png" style="width: 35px;height: 35px;vertical-align: middle;margin:15px 5px 15px 5px;float:left;" fit="fill">
              <div style="width: calc(100% - 55px); ;display: inline;float: left">
                <!--            <div style="width: 80%;display: inline;float: left">-->
                <div class="bBox">
                  <div class="bTalk">
                    <span>{{ item.content }}</span>
                  </div>
                </div>
              </div>
              <div class="clear"></div>
            </div>
          </div>

        </div>
        <div v-if="selectedMode == 'option2'" v-for="(item, index) in llm_list" :key="item.id" style="width:100%;">
          <div class="robot">
            <div class="robot1" v-if="item.id === 1">
              <div style="width: calc(100% - 55px); display: inline; float: left">
                <div class="clear"></div>
                <div class="aBox">
                  <!--显示左侧部分-->
                  <!--显示右侧部分-->
                  <div style="width: 100%; float: left;">
                    <img src="../../../assets/kg/image/fileai.png" style="width: 35px;height: 35px;vertical-align: middle;margin:15px 5px 15px 5px;float:left;" fit="fill">
                    <div style="width: calc(100% - 55px); display: inline; float: left">
                      <div class="bBox">
                        <div class="bTalk">
                          <span v-html="convert(item.GML_content)" style="white-space: pre-line"></span>
                          <div v-if="index === 0" style="margin-top: 10px;">
                            <el-select v-model="curKgFile1" :popper-append-to-body="false" :placeholder="curKgFile1" style="width: 250px;" @change="handleKgFile1Change">
                              <el-option v-for="item in allFilebases" :key="item" :label="item" :value="item"></el-option>
                            </el-select>
                          </div>
                          <!-- 仅在第三个 robot 输出时显示 button group -->
                          <div v-if="index >= 3" class="button-group">
                            <button @click="readText(convert(item.GML_content))" class="sendButton">朗读问答方案</button>
                            <button @click="pauseSpeech" class="sendButton">暂停语音</button>
                            <button @click="resumeSpeech" class="sendButton">语音继续</button>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div class="clear"></div>
            </div>
          </div>

          <div class="user">
            <div class="user1" v-if="item.id === 2 && item.content !== ''" style="width:100%;">
              <img src="../../../assets/kg/image/user.png" style="width: 35px;height: 35px;vertical-align: middle;margin:15px 5px 15px 5px;float:left;">
              <div style="width: calc(100% - 55px); display: inline; float: left">
                <div class="bBox">
                  <div class="bTalk">
                    <span>{{ item.content }}</span>
                  </div>
                </div>
              </div>
              <div class="clear"></div>
            </div>
          </div>
        </div>

        <div v-if="selectedMode=='option3'" v-for="(item, index) in domain_llm_list" :key="item.id" style="width:100%;">
            <div class="robot">
              <div class="robot1" v-if="item.id === 1">
                <div style="width: calc(100% - 55px); display: inline; float: left">
                  <div class="clear"></div>
                  <div class="aBox">
                    <!--显示左侧部分-->
                    <!--                    显示右侧部分-->
                    <div  style="width: 100%; float: left;">
                      <img src="../../../assets/kg/image/robot4.png" style="width: 35px;height: 35px;vertical-align: middle;margin:15px 5px 15px 5px;float:left;" fit="fill">
                      <div style="width: calc(100% - 55px); ;display: inline;float: left">
                        <div class="bBox">
                          <div class="bTalk">
                            <el-loading v-if="loading" text="拼命加载中..." :fullscreen="false"></el-loading>
                            <span v-html="convert(item.GML_content)" style="white-space: pre-line"></span>
                            <div v-if="index >= 3" class="button-group">
                              <button @click="readText(convert(item.GML_content))" class="sendButton">朗读回答</button>
                              <button @click="pauseSpeech" class="sendButton">暂停语音</button>
                              <button @click="resumeSpeech" class="sendButton">语音继续</button>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>

                  </div>

                </div>
                <div class="clear"></div>
              </div>
            </div>

            <div class="user">
              <div class="user1" v-if="item.id === 2 && item.content !== ''" style="width:100%;">
                <img src="../../../assets/kg/image/user.png" style="width: 35px;height: 35px;vertical-align: middle;margin:15px 5px 15px 5px;float:left;" >
                <div style="width: calc(100% - 55px); ;display: inline;float: left">
                  <div class="bBox">
                    <div class="bTalk">
                      <span>{{ item.content }}</span>
                    </div>
                  </div>
                </div>
                <div class="clear"></div>
              </div>
            </div>

          </div>
      </div>
      <div class="send">
        <img src="../../../assets/kg/image/voice.png" @click="sendVoice()" class="btnVoice" alt="语音输入">
        <textarea v-model="keyword" class="sendBox" @keyup.enter.prevent="sendMsg()" placeholder="请输入问题，按enter键进行发送" required></textarea>
        <img src="../../../assets/kg/image/send.png" @click="sendMsg()" class="btnSend" alt="发送">
      </div>
      <div class="else">
       <span style="display: block; text-align: center; font-size: 15px; font-weight: 300; margin-left: auto; margin-right: auto;margin-top: 8px;">
          知识图谱问答系统
        </span>
      </div>

    </el-main>
  </div>
</template>

<script setup>
//开始语音识别
import {ElMessage} from "element-plus";

import {chat,sengmllogs} from "../../../api/kg/chat/chat";
import ChildComponent from './graph.vue';
import { useRouter } from "vue-router";
import { onMounted, ref } from "vue";
// import { sengmllogs} from "../../../api/kg/chat/chat";
// import {chat, highLight} from "../../service/kg/partinst";
// import * as echarts from 'echarts';
// import {sengmllogs} from "../../service/kg/questions";
import axios from "axios";
import { watch } from 'vue';
const $router = useRouter()
let kg_flag = 1;
const router = useRouter();
const selectedMode = ref('option1');
let list = ref([
  { id: 1, content: "我是知识库问答系统，可以对知识图谱中的知识进行检索。", flag: 0,tableInfo:[],score_Mark:0,GML_content:"我是大语言模型问答系统。"},
  { id: 2, content: "你好", flag: 0 ,tableInfo: [],score_Mark:0,GML_content: ""},
  { id: 1, content: "开始提问吧!", flag: 0 ,tableInfo: [],score_Mark:0,GML_content: "开始提问吧！"}
]);
let llm_list = ref([
  { id: 1, content: "我是知识库问答系统。", flag: 0,tableInfo:[],score_Mark:0,GML_content:"我是大语言模型文档问答系统，请先选择想提问的标书文档库："},
  { id: 2, content: "你好", flag: 0 ,tableInfo: [],score_Mark:0,GML_content: ""},
  { id: 1, content: "开始提问吧!", flag: 0 ,tableInfo: [],score_Mark:0,GML_content: "开始提问吧！"}
]);
let domain_llm_list = ref([
  { id: 1, content: "我是知识库问答系统。", flag: 0,tableInfo:[],score_Mark:0,GML_content:"我是经风电领域知识微调后的大语言模型系统。"},
  { id: 2, content: "你好", flag: 0 ,tableInfo: [],score_Mark:0,GML_content: ""},
  { id: 1, content: "开始提问吧!", flag: 0 ,tableInfo: [],score_Mark:0,GML_content: "开始提问吧！"}
]);
// const prelist =list;
let curKgFile1 = ref("")
let keyword = ref("");
let fileDataBase= ref("fengji");
let allFilebases = ref([]);
let copyWord = "";
let gml_answer = ref("");
let gml_keyword = ref("");
// let loading = ref(true);
let tableData = ref([]);
let speech = null;
let entity = ref({});
const  handleButtonClick=function ()
{
  $router.push({
    path: `/kg/gmlExamine`,
  })
}

//语音识别
// 初始化语音识别对象R
let isListening = ref(false);  // 监听状态
let errorMsg = ref("");  // 错误信息

// 初始化语音识别对象
function initSpeechRecognition() {
  if (!('webkitSpeechRecognition' in window)) {
    alert('您的浏览器不支持语音识别功能，请使用支持的浏览器。');
    return null;
  }

  let recognition = new webkitSpeechRecognition();
  recognition.lang = 'zh-CN';
  recognition.continuous = false;
  recognition.interimResults = false;

  recognition.onresult = function(event) {
    keyword.value = event.results[0][0].transcript;  // 获取识别结果并赋值
    console.log("识别结果: ", keyword.value);
  };
  recognition.onerror = function(event) {
    errorMsg.value = "语音识别错误: " + event.error;
    console.error("语音识别错误: ", event.error);
  };

  recognition.onend = function() {
    isListening.value = false;  // 更新监听状态
    ElMessage({
      message: '录音结束',
      type: 'success',
      duration: 2000
    });
    console.log("语音识别结束");
  };

  return recognition;
}

// 开始语音识别
function sendVoice(){
  let recognition = initSpeechRecognition();
  if (recognition) {
    ElMessage({
      message: '录音开始，请说话',
      type: 'info',
      duration: 2000  // Message display duration in milliseconds
    });
    recognition.start();
    isListening.value = true;
    console.log("开始语音识别...");
  }
}

// visible: false
//endregion
// 初始化语音合成对象
function initSpeech() {
  if (speech) {
    window.speechSynthesis.cancel();
  }
  speech = new SpeechSynthesisUtterance();
  speech.lang = "zh-CN";
  speech.volume = 0.7;
  speech.rate = 1;
  speech.pitch = 1;
}
function readText(text) {
  if (!text) return;
  initSpeech();
  speech.text = text;
  window.speechSynthesis.speak(speech);
}

function pauseSpeech() {
  window.speechSynthesis.pause();
}

function resumeSpeech() {
  window.speechSynthesis.resume();
}
// 监听 gml_answer 变化
// watch(llm_list, (newVal) => {
//   if (newVal) {
//     readText(newVal);
//   }
// });
//结束语音识别
function convert(text)
{
  const urlRegex = /\[([^\]]+)\]\((https?:\/\/[^\s\)]+)\)/g;
  return text.replace(urlRegex, (match, label, url) => {
    return `<a href="${url}" target="_blank" style="text-decoration: underline;">${label}</a>`;
  });
}
//选中不同模式调用不同模式的后端
watch(selectedMode, (newValue) => {
  if (newValue === 'option1') {
    handleOption1();
    pauseSpeech();
  } else if (newValue === 'option2') {
    handleOption2();
    pauseSpeech();
  }
  else
  {
    handleOption3();
    pauseSpeech();
  }
});
function handleOption1() {
  // 在这里添加知识库搜索问答的逻辑
  kg_flag=1;
  console.log('执行知识库搜索问答的逻辑');

}
function handleOption2() {
  kg_flag=0;
  // 在这里添加大模型问答的逻辑
  console.log('执行大模型生成的逻辑');
}
function handleOption3()
{
  kg_flag=2;
  console.log('执行领域大模型问答的逻辑');
}

let param_gpt={
  "history": [
    [
      "你好",
      "你好！我是人工智能助手dongqi-GPT，很高兴见到你，欢迎问我任何问题。"
    ]
  ]}

const back = function () {
  router.back(-1);
};
onMounted(async () => {
  getallFilebases()
  curKgFile1.value="fengji"
// keyword="";
// selectedMode= 'option1';
});
//向服务器chatchat发送请求
async  function getallFilebases()
{
  await getfilebases().then((r) => {
    console.log("响应：", r);
    //过滤掉一个默认的数据库
    const filteredData = r.data.filter(item => item !== 'samples' && item !== 'weixiufile');
    allFilebases.value= filteredData;
    console.log("allFilebases",allFilebases)
  })
}
async function getfilebases() {
  try {
    const response = await axios.get('/chat/knowledge_base/list_knowledge_bases', {
      headers: {
        'Content-Type': 'application/json' // 指定请求头的内容类型为 JSON
      }
    });
    console.log("filebases",response.data)
    return response.data; // 返回响应数据

  } catch (error) {
    throw new Error(`Axios error! ${error.message}`); // 捕获错误并抛出
  }
}
const handleKgFile1Change = (newValue) => {
  console.log("Selected Knowledge Base: ", newValue);
  fileDataBase.value=newValue;
};
async function sendMsg() {

  if(keyword.value.trim() !== "")
  {
    await bBoxAdd();
    scroll2buttom();
    copyWord = keyword.value;
    console.log("kg_flag",kg_flag)
    // 向知识库发送请求
    if(kg_flag == 1){
      await renderTable()
    }
    //向大语言模型发送请求
    else if (kg_flag ==0){
      // loading.value= true;
      keyword.value = "";
      await renderTable1(copyWord);
      console.log("大语言模型返回结果",gml_answer)
      console.log("key_word",keyword)
    }
    else
    {//执行领域大模型问答的逻辑
      // loading.value=true;
      keyword.value= "";
      await renderTable2(copyWord);
    }

    scroll2buttom();
    // 问答对结果入库审核
    // await sengmllogs(gml_keyword,gml_answer)
  }

}
function bBoxAdd() {
  if(kg_flag=== 1)
  { list.value.push({ id: 2, content: keyword.value });}
  else if(kg_flag=== 0){
    llm_list.value.push({ id: 2, content: keyword.value });
  }
  else
  {
    domain_llm_list.value.push({ id: 2, content: keyword.value })
  }
}
function scroll2buttom() {
  let contentDiv = document.getElementById('div1');
  contentDiv.scrollTop = contentDiv.scrollHeight;
}
async function RewriteQuery(keyword)
{
  let tableData="（元部套件模型，部署，感知变量标准类" +
      "（元部套件模型，包含，异常类型）\n" +
      "（元部套件模型，子类，元部套件模型）\n" +
      "（感知变量标准类，表征，异常类型）\n" +
      "（部套件，子类，部套件）\n" +
      "（部套件，属于，机组）\n" +
      "（部套件，实例，元部套件模型）\n" +
      "（项目，包含，机组）\n" +
      "（制造质量检测，对象，部套件）\n" +
      "（巡检记录，对象，部套件）\n" +
      "（感知变量，实例，感知变量标准类）\n" +
      "（感知变量，对象，部套件）\n" +
      "（异常告警事件，实例，异常类型）\n" +
      "（异常告警事件，对象，部套件）\n" +
      "（故障事件，实例，故障模式）\n" +
      "（故障事件，来源，异常告警事件）\n" +
      "（故障事件，来源，巡检记录）\n" +
      "（异常类型，诊断，故障模式）\n" +
      "（故障模式，溯因，故障原因）\n" +
      "（故障模式，处理，维修工步）\n" +
      "（原因分析，实例，故障原因）\n" +
      "（原因分析，对象，故障事件）\n" +
      "（检修方法，实例，维修工步）\n" +
      "（检修方法，对象，部套件）\n" +
      "（检修方法，对象，故障事件） "
  console.log("开始进行问句重写!",keyword)
      let renderedString =
"请根据以下信息回答提出的问题：${keyword},如下是我的知识图谱本体的三元组,知识库三元组:${tableData}请你根据我的知识图谱的本体进行对问句的重写和分解。保证分解为简单的满足本体条件的一跳的三元组数据";

  // 交给大模型进行处理
//endregion 先检索后生成
  await renderTable2(renderedString)


}
async function renderTable() {
  console.log("开始执行知识库查询111！")
  if (copyWord === "") {
    list.value.push({ id: 1, content: copyWord });
  } else
  {
    let param = {
      "keyword": keyword.value,
      "last_entity": entity.value
    };
    keyword.value=""
    await chat(param).then((r) => {
      console.log("知识库返回结果r",r);
      tableData.value = r.data.data.answer;
      entity.value = r.data.data.entity;
      console.log("知识库返回的数据",tableData);
    });
    await dataProcess();
  }
}
let history = [

    {
      "role": "user",
      "content": "你好，请给我尽量返回完整的答案（返回字数大于20字），要保证语义的一致性，对我问到的维修方法和解决方案更是如此"
    },
    {
      "role": "assistant",
      "content": "你好，我了解你的需求了，请开始提问吧！"
    }

];
async function dataProcess() {
  let flags = 0;
  let newTableData = [];
  let score_mark=12;
  let gradeInform={'优': [], '良': [], '中': [], '差': [] }
  tableData.value.forEach((answer, index) => {
    answer = answer.trim();
    const chartIndex = answer.indexOf("展示图表：");
    if (answer.startsWith("展示图表：")) {
      let temp = answer.trim().replace("展示图表：", "");
      let parts = temp.split(';');
      let event = parts[0];
      let event_count = parts[1];
      let neighbor_type = parts[2];
      let neighbor_typeinstance = parts.slice(3);
      neighbor_typeinstance = neighbor_typeinstance.filter(item => item);
      flags = {
        event: event,
        event_count: event_count,
        neighbor_type: neighbor_type,
        neighbor_typeinstance: neighbor_typeinstance
      };
      if (chartIndex !== -1) {
        // 如果包含 "展示图表：" 字段，将其删除
        answer = answer.substr(0, chartIndex);
      }
    }
    let link = '';
    const formats = [".mp4", ".htm", ".doc", ".xlsx", ".ppt", ".png", ".jpg"];
    const groupFormats = [".pdf", ".doc", ".ppt", ".htm", ".xls", ".mp4"];
// 检查答案是否包含指定的文件格式以及必备的子字符串group
    if ((answer.includes("group"))&&formats.some(format => answer.includes(format))) {
      let url = answer.substring(answer.indexOf(":") + 1).trim();
      // 对非"htm"链接添加"http://"前缀
      url = url.includes("htm") ? url : `http://${url}`;
      // 根据URL内容决定链接的形式
      if(groupFormats.some(format => url.includes(format))){
        answer = answer.replace(url, "");
        link = `<a href="${url}" target="_blank">查看</a>`;
        console.log("文件url",url)
        answer=""
      }
      else  {
        answer = answer.replace(url, "");
        console.log("图片url",url)
        link = `<img src="${url}" alt="Image" style="height: 200px; width: auto;"><a href="${url}" target="_blank">查看/下载图片</a>`;
        // 把链接从文字替换为图片，就不需要文字了
        answer="";
      }
    }
    newTableData.push({ answer, link });
  });
  let sentence = '';
  newTableData.forEach(({ answer, link }) => {
    sentence += answer;
    if (link) {
      sentence += ' ' + link;
    }
    sentence += '\r';
  });
  if (!sentence.includes("知识库未检索到相关信息！"))
  {
    sentence="知识库检索结果如下：\n"+sentence
  }
  readText(sentence)
  list.value.push({ id: 1, content: sentence.trim(), flag: flags,tableInfo:gradeInform,score_Mark:score_mark })
}
async function renderTable1(copyWord) {
  let flags = 0;
  let score_mark =0;
  let gradeInform={'优': [], '良': [], '中': [], '差': [] }
      if (copyWord !== "") {
        // 构建大模型请求参数,这里的知识库可以自己选择或者定义
        let param_gpt = {
          "query": copyWord,
          "knowledge_base_name": fileDataBase.value,
          "top_k": 3,
          "score_threshold": 0.7,
          "history": [
            {
              "role": "user",
              "content": "你好，请给我尽量返回完整的答案，要保证语义的一致性"
            },
            {
              "role": "assistant",
              "content": "你好，我了解你的需求了，请开始提问吧！"
            }
          ],
          "stream": false,
          "model_name": "chatglm3-6b",
          "temperature": 0.3,
          "max_tokens": 10000,
          "prompt_name": "default"
        }
        // 向大模型chat发送请求
        await postRequest(param_gpt).then((responseData) => {
          console.log("解析后的响应数据：", responseData);
          let assistantResponse = responseData.answer;
          let documentReferences = responseData.docs;
          // 将数组转换为字符串，每个元素之间用换行符分隔
          if (Array.isArray(documentReferences)) {
            documentReferences = documentReferences.join('\n');
          }
// 添加换行符到“出处”前
          documentReferences = documentReferences.replace(/(,出处)/g, "\n$1");
// 移除不必要的换行符
          documentReferences = documentReferences.replace(/\n(?!\n,出处)/g, " ");
// 最终添加到响应中
          gml_answer = assistantResponse;
          assistantResponse = assistantResponse + "\n知识库匹配内容如下：\n" + documentReferences;
          // loading.value=false;
          history.push({
            "role": "user",
            "content": copyWord
          })
          history.push({
            "role": "assistant",
            "content": assistantResponse
          });
          llm_list.value.push({
            id: 1,
            GML_content: assistantResponse,
            flag: flags,
            tableInfo: gradeInform,
            score_Mark: score_mark
          });
          readText(convert(assistantResponse))
          // 更新列表显示
          gml_keyword = copyWord;
        }).catch(error => {
          console.error("请求过程中发生错误：", error);
        });
        // 向大模型chat发送请求
      }
    }

async function postRequest(param) {
  try {
    const response = await axios.post('/chat/chat/knowledge_base_chat', param, {
      headers: {
        'Content-Type': 'application/json' // 指定请求头的内容类型为 JSON
      }
    });
    // 验证响应类型是不是我们期望的 JSON
    if (typeof response.data === 'string' && response.data.startsWith('data:')) {
      // 移除前缀，尝试解析JSON
      const jsonData = JSON.parse(response.data.substring(5));
      return jsonData; // 返回解析后的数据
    } else {
      // 直接返回数据
      return response.data;
    }
  } catch (error) {
    console.error("Axios error! " + error.message); // 捕获错误并抛出
    throw new Error(`请求数据格式错误或网络问题: ${error.message}`);
  }
}

async function renderTable2(keyword) {
  let flags = 0;
  let score_mark =0;
  let newTableData = [];
  let gradeInform={'优': [], '良': [], '中': [], '差': [] }
  if (keyword !== "") {
    // 将用户的新消息添加到历史对话中
    history.push({
      "role": "user",
      "content": keyword
    });
    // 构建大模型请求参数
    let param_gpt = {
      "model": "chatglm3-6b",
      "messages": [
        {
          "role": "system",
          "content": "You are ChatGLM3-fengji, a large language model trained by Zhipu.AI. Follow the user’s instructions carefully. Respond using markdown."
        },
        {
          "role": "user",
          "content": "请认真准确地回复下面问题："+ keyword
        }
      ],
      "stream": false,
      "max_tokens": 100,
      "temperature": 0.1,
      "top_p": 0.8,

    };
    // 向大模型glm3发送请求
    await postRequest_GLM3(param_gpt).then((r) => {
      // console.log("历史对话：", history);
      console.log("响应：", r);
      // 获取模型的响应并更新历史对话
      let assistantResponse = r.choices[0].message.content;
      history.push({
        "role": "assistant",
        "content": assistantResponse
      });
      readText(assistantResponse)
      // llm_list.value.push({ id: 1, GML_content: assistantResponse, flag: flags,tableInfo:gradeInform,score_Mark:score_mark })
      // 更新列表显示
      // list[list.length - 1].GML_content = "大语言模型生成结果如下:\n" + assistantResponse;
      domain_llm_list.value.push({
        id: 1,
        GML_content: assistantResponse,
        flag: flags,
        tableInfo: gradeInform,
        score_Mark: score_mark
      });
      gml_answer = assistantResponse;
      gml_keyword = keyword;
    });
  }
}
async function postRequest_GLM3(param) {
  try {
    const response = await axios.post('/remote/v1/chat/completions', param, {
      headers: {
        'Content-Type': 'application/json' // 指定请求头的内容类型为 JSON
      }
    });
    return response.data; // 返回响应数据
  } catch (error) {
    throw new Error(`Axios error! ${error.message}`); // 捕获错误并抛出


// glm2的调用函数
// async function postRequest(param) {
//   const response = await fetch('/remote', {
//     method: 'POST', // 请求方法
//     headers: {
//       'Content-Type': 'application/json' // 内容类型，告诉服务器我们发送的是JSON
//     },
//     body: JSON.stringify(param) // 将参数对象转为JSON字符串
//   });
//
//   if (!response.ok) { // 检查响应状态，如果不是2xx，就抛出错误
//     throw new Error(`HTTP error! status: ${response.status}`);
//   }
//
//   return await response.json(); // 如果响应OK，则解析并返回JSON响应体
// }
    async function postRequest(param) {
      try {
        const response = await axios.post('/chat/chat/knowledge_base_chat', param, {
          headers: {
            'Content-Type': 'application/json' // 指定请求头的内容类型为 JSON
          }
        });
        // 验证响应类型是不是我们期望的 JSON
        if (typeof response.data === 'string' && response.data.startsWith('data:')) {
          // 移除前缀，尝试解析JSON
          const jsonData = JSON.parse(response.data.substring(5));
          return jsonData; // 返回解析后的数据
        } else {
          // 直接返回数据
          return response.data;
        }
      } catch (error) {
        console.error("Axios error! " + error.message); // 捕获错误并抛出
        throw new Error(`请求数据格式错误或网络问题: ${error.message}`);
      }
    }

    async function postRequest_v1(param) {
      const response = await axios.post('/chat/chat/chat', param, {
        headers: {
          'Content-Type': 'application/json' // 指定请求头的内容类型为 JSON
        }
      });
      console.log("response111", response)
      // 验证响应类型是不是我们期望的 JSON
      return response
    }

  }
}
</script>

<style scoped>
.hh {
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #d0cccc;
  text-align: center;
  height: 3rem; /*增加高度以便更好的展示效果*/
  background-color: white;
  margin-top: 0;
  padding: 10px;

}
.main {

  user-select: text;
  height: 69vh;
  background-color: white;
  overflow-y: auto;
}
.robot
{ overflow-x: hidden;
  background-color: #F5F5F7FF;
  /*margin-left: 15%;*/
  /*margin-right: 15%;*/
}
.robot1
{
  /*box-sizing: border-box;*/
  margin-left: 10%;
  margin-right: 10%;

}
.user
{
  overflow-x: hidden;
  /*margin-left: 10%;*/
  /*margin-left: 10%;*/
}
.user1
{
  /*box-sizing: border-box;*/
  /*!*margin-left: 15%;*!*/
  /*margin-right: 15%;*/
  margin-left: 10%;
  margin-right: 10%;
}

.clear {
  clear: both;
}

.aBox {
  align-items: center;
  line-height: 14px;
  width: 100%;
  font-size: 14px;
  /*background-color: #f8f9fa; !* 更淡的背景颜色 *!*/
  /*border: 1px solid #ccc; !* 更细的边框 *!*/
  /*border-radius: 10px;*/

}
.bBox{
  /*align-content: center;*/
  /*vertical-align:center;*/
  line-height: 24px;
  font-size: 18px;
  width: 100%;
  /*background-color: #c5c5c5; !* 更淡的背景颜色 *!*/
  /*border: 1px solid #4c86a6; !* 更细的边框 *!*/
  /*border-radius: 10px;*/

}

/*.aBox {*/
/*  margin-top: 8px;*/
/*  margin-bottom: 8px;*/
/*  float: left;*/
/*}*/

.bBox {
  margin-top: 8px;
  margin-bottom: 8px;
  float: left;
}

.aTalk, .bTalk {
  margin-top: 10px;
  margin-bottom: 10px;
}

.aTalk span, .bTalk span {
  display: inline-block;
  padding: 3px 10px;
  /* 深色的文本颜色 */
  color: #333;
}

.send {
  margin: 10px 10%;
  outline: none;
  height: 3.5vh;
  position: relative;
  display: flex;
  align-items: center;
  border: 1px solid #ccc;
  border-radius: 20px;
  padding: 5px;
}
.else {
  margin-left: 15%;
  margin-right: 15%;
  height: 2vh;
  background-color: white;

}
.sendBox {
  outline: none;
  width: 100%;
  height: 100%;
  border: none;
  font-size: 24px;
  transition: border-color 0.3s;
  resize: none;
  padding: 0 10px;
  flex-grow: 1;
  border-radius: 20px;
}

.btnVoice {
  width: 32px; /* 放大图标 */
  height: 32px; /* 放大图标 */
  cursor: pointer;
  margin-left: 15px; /* 调整左侧图标的间距 */
}

.btnSend {
  width: 32px; /* 放大图标 */
  height: 32px; /* 放大图标 */
  cursor: pointer;
  margin-right: 15px; /* 调整右侧图标的间距 */
}

.btnSend:active {

  border-color: #007bff
}

#container1 {

  border-left: 5px solid #e6ebec;

  background-color: #f5f5f5;
  width: 100% !important;
  height: 100% !important;
  background-size: 100% 100%;
  overflow-x: hidden;
  text-align: left;
}
.sendButton {
  margin-left: 10px;
  height: 30px; /* 确保按钮高度与输入框一致 */
  padding: 0 15px; /* 调整内边距以适应按钮内容 */
  border: none;
  background-color: #007bff;
  color: white;
  border-radius: 10px;
  cursor: pointer;
  transition: background-color 0.3s;
  white-space: nowrap; /* 确保按钮文字不换行 */
}
</style>
