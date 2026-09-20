<template>
  <el-dialog
      append-to-body
      width="50%"
      :close-on-click-modal="false"
      v-model="recordDialogVisible"
      :show-close="true"
      @close="closeRecordDialog"
      :title="recordDialogTitle"
      class="custom-dialog"
  >
    <template #header>
      <div class="custom-title">
        <span>{{ recordDialogTitle }}</span>
        <el-button
            icon="el-icon-close"
            @click="recordDialogVisible = false"
            class="custom-close-btn"
        ></el-button>
      </div>
    </template>
    <pre class="dialog-content">{{ recordDialogContent }}</pre>
  </el-dialog>
  <basic-container id="container" style="display: flex; flex-direction: column">
    <el-header class="top-page">
      <span class="top-text"> 维修智能推荐->维修方法图谱推荐服务 </span>
    </el-header>
    <el-container style="height: 900px; width: 100%; overflow-x: hidden">
      <el-aside width="18%" class="thisAside">
        <el-scrollbar>
          <div class="lineyes treestyle">
            <el-tree
                highlight-current
                :data="treeData"
                :indent="0"
                ref="tree"
                :accordion="true"
                :props="defaultProps"
                :expand-on-click-node="true"
                node-key="id"
                :default-expanded-keys="defaultExpandKeys"
                icon="none"
                :lazy="false"
                currentKey=""
                :show-checkbox="false"
                :check-strictly="true"
                :default-expand-all="false"
                @node-click="handleNodeClick"
                @node-expand="handleNodeExpand"
            >
              <template #default="{ node, data }">
                <div
                    class="custom-tree"
                    @mouseover="mouseover(data)"
                    @mouseleave="mouseout(data)"
                >
                  <span style="line-height: 16px"> {{ data.name }} </span>
                  <div
                      class="tree-btn"
                      v-show="data.myshow"
                      @click.stop="() => {}"
                  >
                    <span>&nbsp&nbsp&nbsp&nbsp</span>
                    <el-tooltip
                        content="节点搜索"
                        placement="top"
                        :disabled="showTooltip"
                    >
                    </el-tooltip>
                  </div>
                </div>
              </template>
            </el-tree>
          </div>
        </el-scrollbar>
      </el-aside>
      <el-main
          class="thisMain"
          v-loading="loading"
          element-loading-text="维修方案正在生成中..."
      >
        <div class="legend-tooltip" style="color: #1c1b1b">
          <div class="send" style="display: flex; align-items: center">
            <textarea
                v-model="keyword"
                class="sendBox"
                placeholder="请输入故障描述，点击按钮生成维修推荐方案"
                required
                style="resize: none"
            ></textarea>
            <button class="sendVoice" @click="sendVoice()">语音输入</button>
            <button class="sendButton" @click="sendMsg()">
              生成维修推荐方案
            </button>
            <button class="send1Button" @click="clear()">清空问题</button>
          </div>
        </div>
        <div class="vrmViewerDiv">
          <VrmViewer />
        </div>
        <!--        ////////-->
        <div
            v-if="gml_answer && References"
            class="main-content"
            style="
            display: flex;
            justify-content: space-between;
            margin-top: 60px;
          "
        >
          <div class="box-wide">
            <p style="font-size: 20px">维修方案推荐：</p>
            <pre
                v-html="convert_gmlanswer(gml_answer)"
                style="white-space: pre-line; font-size: 20px"
            ></pre>
            <div class="button-group">
              <button @click="readText(gml_answer)" class="sendButton">
                朗读维修方案
              </button>
              <button @click="pauseSpeech" class="sendButton">暂停语音</button>
              <button @click="resumeSpeech" class="sendButton">语音继续</button>
            </div>
          </div>
          <div class="box-narrow">
            <p style="font-size: 20px">参考实例：</p>
            <pre
                v-html="convert(References)"
                style="white-space: pre-line; font-size: 20px"
                @click="handleClick"
            ></pre>
          </div>
        </div>
      </el-main>
    </el-container>
  </basic-container>
</template>

<script setup lang='ts'>
/////////////////////////////////////////////////////////////////////////////////////////////
import { reactive, toRefs, inject } from "vue";
import IconButton from "./IconButton";
import ChatLog from "./ChatLog";
import Settings from "./Settings";
import AssistantText from "./AssistantText";
import VrmViewer from "./vrmViewer.vue";
import { useViewerContext } from "@/features/vrmViewer/viewerContext";
import { speakCharacter } from "@/features/messages/speakCharacter";
import { Screenplay } from "@/features/messages/messages";
import { Viewer } from "@/features/vrmViewer/viewer";

const showSettings = ref(false);
const showChatLog = ref(false);
const fileInputRef = ref(null);
const viewer = useViewerContext();

const assistantMessage = ref("");

let koeiromapKey = ref("");
//////////////////////////////////////////s///////////////////////////////////////////////////
import { onBeforeUnmount, onMounted, ref, watch, nextTick } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import * as echarts from "echarts";
import {
  getGraphByStruct,
  reqAddRootNode,
  reqSonNodes,
  reqTreeNodes,
  getGraphNodeInfo,
} from "@/api/kg/searchService";
import { Search } from "@element-plus/icons-vue";
import {
  checkTaskStatus,
  createTask,
} from ".././../../api/kg/manage/taskStatus";
import BasicContainer from "components/BasicContainer/main.vue";
import axios from "axios";
import { audio } from "./audio"
let loading = ref(false);
let keyword = ref("");
let References = ref("");
let gml_answer = ref("");
let showModal = ref(false);
let modalContent = ref("");
let dialogVisible = ref(true);
let dialogContent = ref("");

//region vue变量
const $router = useRouter();
const $route = useRoute();
//endregion

//语音准文字部分
// 初始化语音识别对象R
let isListening = ref(false); // 监听状态
let errorMsg = ref(""); // 错误信息

// 初始化语音识别对象
function initSpeechRecognition() {
  if (!("webkitSpeechRecognition" in window)) {
    alert("您的浏览器不支持语音识别功能，请使用支持的浏览器。");
    return null;
  }

  let recognition = new webkitSpeechRecognition();
  recognition.lang = "zh-CN";
  recognition.continuous = false;
  recognition.interimResults = false;

  recognition.onresult = function (event) {
    keyword.value = event.results[0][0].transcript; // 获取识别结果并赋值
    console.log("识别结果: ", keyword.value);
  };
  recognition.onerror = function (event) {
    errorMsg.value = "语音识别错误: " + event.error;
    console.error("语音识别错误: ", event.error);
  };

  recognition.onend = function () {
    isListening.value = false; // 更新监听状态
    ElMessage({
      message: "录音结束",
      type: "success",
      duration: 2000,
    });
    console.log("语音识别结束");
  };

  return recognition;
}

// 开始语音识别
function sendVoice() {
  let recognition = initSpeechRecognition();
  if (recognition) {
    ElMessage({
      message: "录音开始，请说话",
      type: "info",
      duration: 2000, // Message display duration in milliseconds
    });

    recognition.start();
    isListening.value = true;
    console.log("开始语音识别...");
  }
}
//结束语音识别
//链接部分
let recordDialogVisible = ref(false);
let recordDialogContent = ref("");
let recordDialogTitle = ref("");

const handleClick = (event) => {
  if (event.target.classList.contains("show-record")) {
    event.preventDefault(); //防止跳转页面
    const content = event.target.getAttribute("data-content");
    const title = event.target.textContent;
    showRecord(content, title);
  }
};

const showRecord = (content, title) => {
  recordDialogContent.value = content;
  recordDialogTitle.value = title;
  recordDialogVisible.value = true;
};

const closeRecordDialog = () => {
  recordDialogVisible.value = false;
};

//region 结构树变量
let globeParams = {}; //  声明一个全局参数对象
let treeData = ref([]);
let showTooltip = ref(true);
let expandKeys = []; //  缓存待扩展的节点
let defaultExpandKeys = ref([]);
let defaultProps = ref({
  children: "children",
  label: "name",
  isLeaf: "leaf",
});
let curStruct = ""; //节点id
let curStructLabel = "";
//endregion
// region 图变量
let myChart = null;
let currentLayout = ref("");
let layoutTypes = ref([
  { label: "经典力导向布局", value: "force" },
  { label: "环形布局", value: "circular" },
]);
let zoomSize = {}; //zoomSize放缩大小
let des = ref([]);
let nodes = [];
let edges = [];
let name = [];
let type = [];
let nodesinfo = [];
let colorType = [];
let typeNumber = {};
//endregion
//region 功能变量
let tree = ref(null);
let visible = ref(false);
let ontologyName = "";
let kgType = "";
let taskStatus = "";
let taskId = "";
let speech = null;


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

let onCompleteCallback;  // 声明全局的 onComplete 回调
let animationInterval;   // 声明全局的动画间隔
let isPlaying = false;  // 声明全局的状态变量
let isSpeakingAi = true;  // 添加新的全局变量

function readText(text) {
  if (!text) return;
  initSpeech();
  speech.text = text;
  isPlaying = true; // 恢复播放状态
  isSpeakingAi = true;  // 恢复播放状态
  // 恢复循环播放动画
  const handleSpeakAi = async (screenplay, onStart, onComplete) => {
    await speakCharacter(screenplay, viewer.viewer, koeiromapKey.value, onStart, onComplete);
  };

  const exampleScreenplay = {
    expression: "neutral",
    talk: {
      style: "talk",
      speakerX: 0,
      speakerY: 0,
      message: "1",
    },
  };

  const onStart = () => {
    console.log("Speech started");
    isPlaying = true; // 设置状态为播放中
    // 开始循环播放动画
    if (animationInterval) {
      clearInterval(animationInterval); // 确保之前的间隔被清除
    }
    animationInterval = setInterval(() => {
      if (isPlaying) {
        handleSpeakAi(exampleScreenplay, onStart, onCompleteCallback);
      }
    }, 800); // 调整时间间隔以适应动画长度
  };
  onCompleteCallback = () => {
    console.log("Speech ended");
    clearInterval(animationInterval); // 清除动画间隔
    isPlaying = false; // 设置状态为未播放
    isSpeakingAi = false;  // 设置状态为未播放
  };

  if (animationInterval) {
    clearInterval(animationInterval); // 确保之前的间隔被清除
  }
  animationInterval = setInterval(() => {
    if (isPlaying) {
      handleSpeakAi(exampleScreenplay, onStart, onCompleteCallback);
    }
  }, 800); // 调整时间间隔以适应动画长度

  speech.onstart = onStart;
  speech.onend = onCompleteCallback;
  window.speechSynthesis.speak(speech);
  // resumeSpeech();
  // pauseSpeech();
  // resumeSpeech()
}

//
// handleSpeakAi(exampleScreenplay, onStart, onCompleteCallback);
// speech.onstart = onStart;
// speech.onend = onCompleteCallback;

// 启动语音播放

// }

function pauseSpeech() {
  isPlaying = false; // 设置状态为未播放
  isSpeakingAi = false;  // 设置状态为未播放
  window.speechSynthesis.pause(); // 暂停语音播放
  clearInterval(animationInterval); // 清除动画间隔
}

function resumeSpeech() {
  if (window.speechSynthesis.paused) {
    window.speechSynthesis.resume();
    isPlaying = true; // 恢复播放状态
    isSpeakingAi = true;  // 恢复播放状态
    // 恢复循环播放动画
    const handleSpeakAi = async (screenplay, onStart, onComplete) => {
      await speakCharacter(screenplay, viewer.viewer, koeiromapKey.value, onStart, onComplete);
    };

    const exampleScreenplay = {
      expression: "neutral",
      talk: {
        style: "talk",
        speakerX: 0,
        speakerY: 0,
        message: "1",
      },
    };

    const onStart = () => {
      console.log("Speech started");
      isPlaying = true; // 设置状态为播放中
      // 开始循环播放动画
      if (animationInterval) {
        clearInterval(animationInterval); // 确保之前的间隔被清除
      }
      animationInterval = setInterval(() => {
        if (isPlaying) {
          handleSpeakAi(exampleScreenplay, onStart, onCompleteCallback);
        }
      }, 800); // 调整时间间隔以适应动画长度
    };

    if (animationInterval) {
      clearInterval(animationInterval); // 确保之前的间隔被清除
    }
    animationInterval = setInterval(() => {
      if (isPlaying) {
        handleSpeakAi(exampleScreenplay, onStart, onCompleteCallback);
      }
    }, 800); // 调整时间间隔以适应动画长度
  }
}

// 监听 gml_answer 变化
watch(gml_answer, (newVal) => {
  if (newVal) {
    readText(newVal);
  }
});





// 监听 gml_answer 变化
watch(gml_answer, (newVal) => {
  if (newVal) {
    readText(newVal);
  }
});

function convert(text) {
  // 替换 URL 为带下划线的链接
  const urlRegex = /\[([^\]]+)\]\((https?:\/\/[^\s\)]+)\)/g;
  const formattedText = text.replace(urlRegex, (match, label, url) => {
    return "";
  });

  // 分割记录，每条记录都是以 "记录 数字" 开始
  const records = formattedText.split(/(记录 \d+)/).filter(Boolean); // 使用 split 正则保留分隔符，并去除空字符串

  let resultHtml = "";
  let firstRecordShown = false; // 标记是否已经处理了第一条记录

  for (let i = 0; i < records.length; i++) {
    if (records[i].startsWith("记录 ") && i + 1 < records.length) {
      if (!firstRecordShown) {
        // 展示第一条记录的全部内容
        resultHtml += `<div style="white-space: pre-line; font-size: 20px;">${
            records[i]
        } ${records[i + 1]}</div>`;
        firstRecordShown = true;
        i++; // 跳过下一个内容部分，因为它已经被添加
      } else {
        // 创建显示记录内容的弹窗
        const recordContent = `${records[i]} ${records[i + 1]}`;
        resultHtml += `<div class="highlight" style="white-space: pre-line; font-size: 20px;">
                         <a href="#" class="show-record" data-content="${recordContent.replace(
            /'/g,
            "\\'"
        )}" style="text-decoration: underline;color: blue;">${
            records[i]
        } </a>
                       `;
        i++; // 跳过下一个内容部分，因为它已经被添加
      }
    }
  }

  return resultHtml;
}

function convert_gmlanswer(text) {
  const numberRegex = /(?<!\d\.\d+)(\d+\.\d*\s)/g;
  // 在每个匹配到的数字前添加换行符，除非它是数字.数字
  const result = text.replace(numberRegex, "\n$1");
  // readText(result);
  return result;
}



async function sendMsg() {
  pauseSpeech();
  loading.value = true;
  if (keyword.value.trim() !== "") {
    gml_answer.value = "";
    References.value = "";
    await renderTable1(keyword);
    await highlightTreeNodes(gml_answer.value);
    loading.value = false;
  }
}
function clear() {
  keyword.value = "";
}

// 展开目标节点及其所有祖先节点
async function expandParentNodes(node) {
  let current = node;
  while (current.parent) {
    tree.value.store.nodesMap[current.id].expanded = true;
    current = current.parent;
  }
  // 最后展开根节点
  tree.value.store.nodesMap[current.id].expanded = true;
}

// 折叠所有不在目标节点分支上的节点，仅显示根节点的直接子节点
function collapseAllExceptTargetAndRootChildren(rootNode, targetNode) {
  rootNode.children.forEach((child) => {
    if (!isDescendantOrSelf(child, targetNode)) {
      collapseNode(child);
    }
  });
}

// 判断一个节点是否是另一个节点的子节点或本身
function isDescendantOrSelf(node, targetNode) {
  if (node === targetNode) {
    return true;
  }
  if (node.children) {
    return node.children.some((child) => isDescendantOrSelf(child, targetNode));
  }
  return false;
}

// 折叠节点及其所有子节点
function collapseNode(node) {
  tree.value.store.nodesMap[node.id].expanded = false;
  if (node.children) {
    node.children.forEach((child) => {
      collapseNode(child);
    });
  }
}

// 在定位节点后折叠其他节点
async function highlightTreeNodes(answer) {
  const regex = /故障定位(?:可能性较高的|最可能的|可能|部件)(.*?)[。,；,:]/;
  const match = answer.match(regex);
  if (match && match[1]) {
    const part = match[1].trim();
    console.log("返回的part", part);
    const node = findNodeByName(treeData.value, part);
    console.log("匹配的节点", node);
    if (node) {
      await expandParentNodes(node); // 展开目标节点及其祖先节点
      tree.value.setCurrentKey(node.id);
      await expandNode(node); // 展开目标节点
      scrollToNode(node); // 确保节点展开后滚动到视图中
      // 折叠不相关的节点，仅显示根节点的直接子节点
      treeData.value.forEach((rootNode) => {
        collapseAllExceptTargetAndRootChildren(rootNode, node);
      });
    }
  }
}

// 保持目标节点及其子节点展开
async function expandNode(node) {
  tree.value.store.nodesMap[node.id].expanded = true;
  return new Promise((resolve) => setTimeout(resolve, 300)); // 等待展开动画完成
}

// 找到节点并滚动到视图中
function scrollToNode(node) {
  nextTick(() => {
    const el = document.querySelector(
        `[node-key="${node.id}"] .el-tree-node__content`
    );
    if (el) {
      const scrollbar = document.querySelector(".el-scrollbar__wrap");
      if (scrollbar) {
        const elTop = el.getBoundingClientRect().top;
        const wrapTop = scrollbar.getBoundingClientRect().top;
        scrollbar.scrollTop +=
            elTop - wrapTop - scrollbar.clientHeight / 2 + el.clientHeight / 2;
      }
    }
  });
}

function findNodeByName(nodes, name) {
  let bestMatch = null;
  let bestDistance = Infinity;
  for (let i = 0; i < nodes.length; i++) {
    const distance = getLevenshteinDistance(nodes[i].name, name);
    if (distance < bestDistance) {
      bestMatch = nodes[i];
      bestDistance = distance;
    }
    if (nodes[i].children) {
      const childNode = findNodeByName(nodes[i].children, name);
      if (childNode) {
        const childDistance = getLevenshteinDistance(childNode.name, name);
        if (childDistance < bestDistance) {
          bestMatch = childNode;
          bestDistance = childDistance;
        }
      }
    }
  }
  return bestMatch;
}

function getLevenshteinDistance(a, b) {
  const matrix = [];
  if (a.length === 0) return b.length;
  if (b.length === 0) return a.length;
  for (let i = 0; i <= b.length; i++) {
    matrix[i] = [i];
  }
  for (let j = 0; j <= a.length; j++) {
    matrix[0][j] = j;
  }
  for (let i = 1; i <= b.length; i++) {
    for (let j = 1; j <= a.length; j++) {
      if (b.charAt(i - 1) === a.charAt(j - 1)) {
        matrix[i][j] = matrix[i - 1][j - 1];
      } else {
        matrix[i][j] = Math.min(
            matrix[i - 1][j - 1] + 1,
            Math.min(matrix[i][j - 1] + 1, matrix[i - 1][j] + 1)
        );
      }
    }
  }
  return matrix[b.length][a.length];
}

async function postRequest(param) {
  try {
    const response = await axios.post("/chat/chat/knowledge_base_chat", param, {
      headers: {
        "Content-Type": "application/json", // 指定请求头的内容类型为 JSON
      },
    });
    // 验证响应类型是不是我们期望的 JSON
    if (
        typeof response.data === "string" &&
        response.data.startsWith("data:")
    ) {
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

async function renderTable1(keyword) {
  if (keyword.value !== "") {
    // 构建大模型请求参数,这里的知识库可以自己选择或者定义
    let param_gpt = {
      query:
          "发现故障现象：" +
          keyword.value +
          '该怎么维修呢？请列出检修步骤。另外故障定位最可能是哪一个部件(一)？\n故障定位的回答按照以下键值对的形式！！！如："最可能的故障定位部件":变流器。你的回答不应该包括我提的问题',
      knowledge_base_name: "weixiufile",
      top_k: 3,
      score_threshold: 0.7,
      history: [
        {
          role: "user",
          content: "你好，请给我尽量返回完整的答案，要保证语义的一致性",
        },
        {
          role: "assistant",
          content: "你好，我了解你的需求了，请开始提问吧！",
        },
      ],
      stream: false,
      model_name: "chatglm3-6b",
      temperature: 0.3,
      max_tokens: 10000,
      prompt_name: "default",
    };
    // 向大模型chat发送请求
    await postRequest(param_gpt)
        .then((responseData) => {
          console.log("解析后的响应数据：", responseData);
          let assistantResponse = responseData.answer;
          let documentReferences = responseData.docs;
          // 将数组转换为字符串，每个元素之间用换行符分隔
          if (Array.isArray(documentReferences)) {
            documentReferences = documentReferences.join("\n");
          }
          // 添加换行符到“出处”前
          documentReferences = documentReferences.replace(/(,出处)/g, "\n$1");
          // 移除不必要的换行符
          documentReferences = documentReferences.replace(/\n(?!\n,出处)/g, " ");
          References.value = documentReferences;
          // 最终添加到响应中
          gml_answer.value = assistantResponse;
          readText(gml_answer.value);
          console.log("gml_answer", gml_answer);
        })
        .catch((error) => {
          console.error("请求过程中发生错误：", error);
        });
    // 向大模型chat发送请求
  }
}

//region vue周期
onBeforeUnmount(async () => {
  if (!myChart) {
    return;
  }
  myChart.dispose();
  myChart = null;
});

onMounted(() => {
  document.addEventListener("click", handleClick);
  ontologyName = "维修知识本体";
  kgType = "维修知识";
  initParams();
  getTreeNodes(globeParams.nodeLevel);
  curStruct = $route.query.curStruct; // 结构树id
  curStructLabel = $route.query.curStructLabel; //结构名称
  if (!isEmpty(curStruct)) {
    //非空直接查询
    graphStruct();
  }
});
onBeforeUnmount(() => {
  document.removeEventListener("click", handleClick);
});
//endregion

//region 结构树
const initParams = function () {
  globeParams.nodeLevel = 4; //    预先展开3层节点
};
const mouseover = function (data) {
  data.myshow = true;
};
const mouseout = function (data) {
  // 鼠标移出
  data.myshow = false;
};
//  节点点击消息响应
const handleNodeClick = function (data, node, treeNode, event) {};
//  节点扩展消息响应
const handleNodeExpand = async function (data, node, treeNode) {
  if (node.level >= globeParams.nodeLevel || _sonNodeHasLoading(data)) {
    let response = await reqSonNodes(data.nodeCode);
    if (response.data.data) {
      response.data.data.forEach((item) => {
        item.children = item.leaf
            ? []
            : [{ id: "loading", name: "节点加载中..." }];
      });
      node.data.children = response.data.data;
    }
  }
};
/* 检查一个节点的子节点中，是否有待加载（Loading）状态的子节点
 *  这种情况只会发生在第二层节点复制后，它的子节点没有加载的情况下。这时，虽然节点层级小于globeParams.nodeLevel，但是仍然需要查询后台
 */

// 根据节点层级数，加载结构树的一组节点
const getTreeNodes = async function (nodeLevel) {
  try {
    let response = await reqTreeNodes(nodeLevel);
    if (response.data.data && response.data.data.length > 0) {
      treeData.value = [];
      expandKeys = []; //  缓存待扩展的节点
      let rootNodes = response.data.data.filter(
          (ele) => ele.nodeType === "Root" || ele.nodeType === "Root-Leaf"
      );
      for (let item of rootNodes) {
        treeData.value.push(item);
        expandKeys.push(item.id);
        await setChildren(item, response.data.data);
      }
      defaultExpandKeys.value = expandKeys; //  扩展节点
    } else {
      let confirmResult = await this.$confirm(
          "首次编辑GBOM，系统未找到根节点，是否创建根节点?",
          "提示",
          {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
          }
      );
      if (confirmResult) {
        let addRootNodeResponse = await reqAddRootNode({
          nodeName: "模版根节点",
          nodeCode: "TM1",
        });
        if (addRootNodeResponse.data.code === 200) {
          this.$message.success("默认根节点创建成功");
          getTreeNodes(globeParams.nodeLevel);
        }
      }
    }
  } catch (error) {
    console.log(error);
  }
};
//  递归查询节点pNode的全部子节点，并装配成el-tree的数据结构
const setChildren = function (pNode, nodeList) {
  let res = getChildrenByNodeCode(pNode.nodeCode, nodeList);
  let children = res.sonNodes;
  if (children.length === 0) {
    if (pNode.nodeType === "Mid" || pNode.nodeType === "Root") {
      //  如果不是叶子节点，节点前显示"+"号
      expandKeys = expandKeys.filter((item) => item !== pNode.id); //  从扩展节点中删除它
      pNode.children = [{ id: "loading", name: "节点加载中..." }];
    }
    return pNode;
  } else {
    pNode.children = children;
    children.forEach((item) => {
      expandKeys.push(item.id); //  添加到扩展节点
      setChildren(item, res.otherNodes);
    });
  }
};
//  正则表达式，根据节点编码，查找它的下一层子节点。将节点列表nodeList分解为2个数组：sonNodes——pNodeCode的子节点；otherNodes非子节点
const getChildrenByNodeCode = function (pNodeCode, nodeList) {
  let sonNodes = [];
  let otherNodes = [];
  let regex = new RegExp("^" + pNodeCode + "-[A-Za-z0-9]+$");
  nodeList.forEach((item) => {
    if (regex.test(item.nodeCode))
        //  正则表达式判定item是不是pNodeCode的下一层子节点，如果是就放入数组sonNodes
      sonNodes.push(item);
    else otherNodes.push(item);
  });
  return { sonNodes: sonNodes, otherNodes: otherNodes };
};
//endregion
//region 图布局
//初始化图
const initEcharts = function () {
  myChart = echarts.init(document.getElementById("mychart"));
  myChart.clear();
  myChart.showLoading();
  nodes = [];
  edges = [];
  colorType = [];
  typeNumber = {};
  nodesinfo = [];
};
//绘制echarts图形
const drawEcharts = function (colorType, nodes, edges, layout, zoomSize) {
  let arrayEcharts = [];
  colorType.forEach((type) => {
    arrayEcharts.push({ name: type });
  });
  const option = {
    title: {
      top: "top",
      left: "left",
    },
    tooltip: {},
    legend: [
      {
        backgroundColor: "#ffffff",
        top: "1%",
        type: "scroll",
        data: arrayEcharts.map(function (a) {
          return a.name;
        }),
      },
    ],
    series: [
      {
        name: "知识图谱可视化",
        type: "graph",
        top: 20,
        layout: layout,
        data: nodes,
        links: edges,
        categories: arrayEcharts,
        roam: true,
        zoom: zoomSize,
        label: {
          show: true,
          position: "right",
          formatter: "{b}",
        },
        labelLayout: {
          hideOverlap: false, //是否隐藏下层重叠的节点的标签
        },
        tooltip: {
          //nodesinfo
          formatter: function (params) {
            let curname = params.data;
            let count = 0;
            for (let i in curname) {
              count++;
            } //长度三的才是节点
            if (count === 3) {
              let res = "";
              let templist = nodesinfo.filter(
                  (nodeinfo) =>
                      nodeinfo.name === curname.name &&
                      nodeinfo.types === curname.category
              );
              if (templist.length > 0) {
                let tempitem = templist[0];
                res = `名称：${tempitem["name"]}<br/>`;
                for (let key in tempitem) {
                  if (key !== "name" && key !== "code" && key !== "types") {
                    res = res + `${key}：${tempitem[key]}<br/>`;
                  }
                }
                return res;
              }
              return params.name;
            }
            let tempdata = params.data;
            let source = tempdata["source"];
            let target = tempdata["target"];
            let start = "";
            let end = "";
            for (let i = 0; i < nodes.length; i++) {
              if (nodes[i].id === source) {
                start = nodes[i].name;
              }
              if (nodes[i].id === target) {
                end = nodes[i].name;
              }
            }
            return start + "->" + end;
          },
        },
        scaleLimit: {
          min: 0.5,
          max: 3,
        },
        lineStyle: {
          color: "source",
          curveness: 0.3,
        },
        force: {
          initLayout: "circular",
          repulsion: 50,
          gravity: 0.1,
          layoutAnimation: true,
          friction: 0.1,
        },
        cursor: "pointer",
        legendHoverLink: true,
        hoverAnimation: true,
        symbol: "roundRect", //图形 'circle', 'rect', 'roundRect', 'triangle', 'diamond', 'pin', 'arrow'
        draggable: true, //节点是否可以拖拽
        symbolSize: 10, //设置节点大小
        edgeSymbol: ["", "arrow"], //箭头指向
        emphasis: {
          scale: 1.8,
          focus: "adjacency",
        },
        left: 0,
        animation: true,
        animationEasing: "cubicOut",
        animationDuration: 1500,
        animationEasingUpdate: "quinticInOut",
      },
    ],
  };
  option && myChart.setOption(option);
  //随着屏幕大小调节图表
  window.addEventListener("resize", () => {
    myChart.resize();
  });
  myChart.off();
  myChart.on("click", function (params) {
    if (params.dataType === "node") {
      //点击节点才处理
      // 通过后端获取到当前节点的基本属性、关系等信息进行处理
      getGraphNodeInfo(params.name, kgType).then((r) => {
        nodeData.value = r.data.data;
        nodeData.value.forEach((item) => {
          if (item.includes("名称:")) {
            name = item.split("名称:")[1];
          }
          if (item.includes("类型:")) {
            type = item.split("类型:")[1];
          }
        });
        entity_title.value = type + ":" + name + "信息查看";
      });
      NodeVisible.value = true;
      // 打开弹窗展示信息
    }
  });
};
//endregion

//region 功能实现
//点击结构树节点搜索
const handleTreeClick = (node, data) => {
  curStruct = data.id;
  curStructLabel = data.name;
  graphStruct();
};
//以结构节点查询对应数据
const graphStruct = async function () {
  //以结构节点查询对应数据
  initEcharts(); //初始化图谱
  try {
    taskStatus = "inProcess";
    const taskRespond = await createTask();
    taskId = taskRespond.data.data;
    const temp = getGraphByStruct(ontologyName, curStruct, taskId);
    await checkInteractionStatus(taskId);
  } catch (error) {
    taskStatus = "falseProcess";
    console.error("后端请求出错：", error);
    myChart.hideLoading();
  }
};
const drawGraphByStruct = function (r) {
  r = JSON.parse(r);
  nodes = r.nodes;
  if (nodes.length === 0) {
    ElMessage({
      message: `${curStructLabel}结构还未导入数据！`,
      type: "warning",
    });
  }
  edges = r.edges;
  colorType = r.types;
  typeNumber = r.typeNumber;
  nodesinfo = r.info;
  drawEcharts(colorType, nodes, edges, "force", 2);
};
//交互状态查询
const checkInteractionStatus = async function (taskId) {
  let waitTime = 0;
  let t = 0;
  while (taskStatus === "inProcess") {
    //轮询后端交互状态
    let response = await checkTaskStatus(taskId);
    response = response.data;
    if (response.data.status === "endProcess") {
      await ElMessage({
        type: "success",
        message: "查询完成！",
      });
      if (nodes.length === 0) {
        await ElMessage({
          type: "warning",
          message: "该结构无数据！",
        });
        myChart.hideLoading();
      }
      taskStatus = "endProcess";
    } else if (response.data.status === "inProcess") {
      t = t + 3000;
      await new Promise((resolve) => setTimeout(resolve, t)); //等待5秒后再轮询
      waitTime = waitTime + t / 1000;
      await ElMessage({
        type: "info",
        message: `正在查询，已加载${waitTime}秒。`,
        duration: 2000,
      });
      if (response.data.result !== null && response.data.result !== "") {
        if (nodes.length === 0) {
          //第一次渲染
          myChart.hideLoading();
        }
        drawGraphByStruct(response.data.result); //去渲染
      }
    } else {
      await ElMessage({
        type: "error",
        message: "查询出错",
        duration: 3000,
      });
      taskStatus = "falseProcess";
      myChart.hideLoading();
    }
  }
};
//endregion
const gotoTablePage = function () {
  //携带图谱类型，当前域，结构树id
  $router.push({
    path: "./indexTable",
    query: {
      curStruct: curStruct,
      curStructLabel: curStructLabel,
    },
  });
};

//判断是否为空的办法
function isEmpty(obj) {
  if (typeof obj == "undefined" || obj == null || obj === "") {
    return true;
  } else {
    return false;
  }
}

const handleChangeSystemPrompt = (event) => {
  props.onChangeSystemPrompt(event.target.value);
};

const handleAiKeyChange = (event) => {
  props.onChangeAiKey(event.target.value);
};

const handleChangeKoeiromapKey = (event) => {
  props.onChangeKoeiromapKey(event.target.value);
};

const handleChangeKoeiroParam = (x, y) => {
  props.onChangeKoeiromapParam({
    speakerX: x,
    speakerY: y,
  });
};

const handleClickOpenVrmFile = () => {
  fileInputRef.value?.click();
};

const handleChangeVrmFile = (event) => {
  const files = event.target.files;
  if (!files) return;

  const file = files[0];
  if (!file) return;

  const file_type = file.name.split(".").pop();

  if (file_type === "vrm") {
    const blob = new Blob([file], { type: "application/octet-stream" });
    const url = window.URL.createObjectURL(blob);
    viewer.loadVrm(url);
  }
  event.target.value = "";
};
</script>

<style scoped>
/* //////// */
.vrmViewerDiv {
  width: 300px; /* 设置宽度 */
  height: 300px; /* 设置高度 */
  background-color: rgba(255, 255, 255, 0.8); /* 半透明背景 */
  position: absolute;
  top: 83%; /* 垂直居中 */
  left: 89%; /* 水平居中 */
  transform: translate(-50%, -50%); /* 使其居中 */
  z-index: 10; /* 确保在最上层 */
  border: 0px solid black; /* 可选：添加边框以便更容易看到 */
  display: flex;
  align-items: center;
  justify-content: center;
}
.vrmViewerDiv canvas {
  width: 100%;
  height: 100%;
}
/* //////// */
.top-page {
  display: flex;
  flex-direction: row;
  justify-content: space-between; /* 内容靠左 */
  align-items: center; /* 垂直居中 */
}

.top-text {
  font-size: 20px;
  font-weight: bold;
}

.thisMain {
  background: white;
  height: 100%;
  border-top: 1px solid #757373;
  border-right: 1px solid #757373;
  border-bottom: 1px solid #757373;
  overflow-x: hidden; /* 隐藏水平滚动条 */
  box-sizing: border-box; /* 确保边框包含在宽度内 */
}

.thisAside {
  background: white;
  height: 100% !important;
  overflow-y: auto;
  border: 1px solid #757373;
}

.el-dialog__header .el-dialog__title {
  font-size: 30px;
}

:deep(.treestyle .el-tree-node) {
  position: relative;
  padding-left: 16px;
}

:deep(.treestyle .el-tree) {
  background-color: Transparent; /*背景透明*/
  color: #212020; /*字体颜色：黑灰色*/
}

:deep(.treestyle .el-tree-node__expand-icon.is-leaf) {
  /* 叶子节点隐藏图标  */
  display: none;
}

/*  下面的样式设置与连线有关    */
:deep(.treestyle .el-tree-node__children) {
  padding-left: 18px;
}

:deep(.treestyle .el-tree-node :last-child:before) {
  height: 38px;
}

.treestyle .el-tree > .el-tree-node:before {
  border-left: none;
}

.treestyle .el-tree > .el-tree-node:after {
  border-top: none;
}

:deep(.treestyle .el-tree-node:before) {
  content: "";
  left: -4px;
  position: absolute;
  right: auto;
  border-width: 1px;
}

:deep(.treestyle .el-tree-node:after) {
  content: "";
  left: -4px;
  position: absolute;
  right: auto;
  border-width: 1px;
}

:deep(.lineyes .el-tree .el-tree-node__expand-icon.expanded) {
  /*节点图标不旋转*/
  -webkit-transform: rotate(0deg);
  transform: rotate(0deg);
}

:deep(.lineyes .el-tree-node__expand-icon) {
  font-size: 16px; /*图标大小*/
}

:deep(.lineyes .el-tree-node__expand-icon:before) {
  /*有子节点 且未展开*/
  content: "";
  background: url("/img/kgtree/circleplus.svg") no-repeat 0 0px;
  display: block;
  width: 16px;
  height: 16px;
  background-size: cover;
}

:deep(.lineyes .el-tree-node__expand-icon.expanded:before) {
  /*有子节点 且已展开*/
  content: "";
  background: url("/img/kgtree/remove.svg") no-repeat 0 0px;
  display: block;
  width: 16px;
  height: 16px;
  background-size: cover;
}

:deep(.lineyes .el-tree-node__content:hover) {
  /*鼠标滑过，修改背景色*/
  color: cyan;
  font-weight: bold;
  background-color: rgb(59, 59, 164) !important;
}

.lineyes .el-tree-node:focus > .el-tree-node__content {
  /*节点选中，节点获取焦点*/
  color: gold;
  font-weight: bold;
  background-color: rgba(138, 194, 252, 0.53) !important;
}

.lineyes .el-tree-node.is-current > .el-tree-node__content {
  color: gold;
}

:deep(.lineyes .el-tree-node:before) {
  /*显示节点间连接的竖线*/
  border-left: 1px dashed #dcdcdc;
  bottom: 0px;
  height: 100%;
  top: -26px;
  width: 3px;
}

:deep(.lineyes .el-tree-node:after) {
  /*显示节点间连接的横线*/
  border-top: 1px dashed #dcdcdc;
  height: 20px;
  top: 12px;
  width: 24px;
}

.custom-tree {
  display: flex;
  height: 100%;
  width: 100%;
  align-items: center; /*垂直对齐*/
}

.custom-tree .tree-btn {
  display: flex;
  height: 100%;
  width: 100%;
  align-items: center; /*垂直对齐*/
}

.custom-tree .tree-btn .btn {
  height: 100%;
  width: 30px;
}

.main-content {
  display: flex;
  padding-left: 20px;
  justify-content: space-between;
  width: calc(100% - 50px); /* 宽度调整以适应内容并避免水平滚动 */
  box-sizing: border-box; /* 确保边框包含在宽度内 */
}

.box-wide {
  flex: 2; /* 使得 box-wide 占据更多的空间 */
  height: auto;
  min-width: 300px; /* 设置最小宽度以保证布局 */
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 10px;
  background-color: #f9f9f9;
  box-sizing: border-box;
  margin-right: 20px; /* 增加右边距以分隔两个框 */
}

.box-narrow {
  flex: 1;
  width: 30%; /* 设置固定宽度 */
  height: auto; /* 自动调整高度 */
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 10px;
  background-color: #f9f9f9;
  box-sizing: border-box;
  margin-right: 0; /* 移除最后一个框的右边距 */
}

.send {
  padding-left: 20px;
  padding-top: 20px;
  display: flex;
  align-items: center;
  width: 100%;
}

.sendBox {
  /*font-size: 16px;*/
  outline: none;
  width: 70%;
  height: 55px; /* 调整高度使其适应行内布局 */
  border: 1px solid #ccc;
  border-radius: 10px;
  margin-right: 10px; /* 调整右边距以分隔按钮 */
  font-size: 28px;
  transition: border-color 0.3s;
  box-sizing: border-box; /* 确保边框包含在宽度内 */
}
.sendVoice {
  height: 55px; /* 确保按钮高度与输入框一致 */
  padding: 0 15px; /* 调整内边距以适应按钮内容 */
  border: none;
  background-color: #007bff;
  color: white;
  border-radius: 10px;
  cursor: pointer;
  transition: background-color 0.3s;
  white-space: nowrap; /* 确保按钮文字不换行 */
}
.sendButton {
  margin-left: 10px;
  height: 55px; /* 确保按钮高度与输入框一致 */
  padding: 0 15px; /* 调整内边距以适应按钮内容 */
  border: none;
  background-color: #007bff;
  color: white;
  border-radius: 10px;
  cursor: pointer;
  transition: background-color 0.3s;
  white-space: nowrap; /* 确保按钮文字不换行 */
}

.send1Button {
  margin-left: 10px;
  height: 55px; /* 确保按钮高度与输入框一致 */
  padding: 0 15px; /* 调整内边距以适应按钮内容 */
  border: none;
  background-color: #007bff;
  color: white;
  border-radius: 10px;
  cursor: pointer;
  transition: background-color 0.3s;
  white-space: nowrap; /* 确保按钮文字不换行 */
}

.sendButton:hover {
  background-color: #0056b3;
}

/*.box-text-2 {*/
/*  vertical-align: center;*/
/*  font-size: 24px; !* 调整字体大小 *!*/
/*  text-align: center; !* 文字居中 *!*/
/*  margin: 0; !* 去除默认外边距 *!*/
/*}*/
.modal-content {
  white-space: pre-line;
  font-size: 20px;
}
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 999;
}
.button-group {
  display: flex;
  gap: 10px; /* Adjust the space between buttons as needed */
}
:deep(
    .el-tree--highlight-current
      .el-tree-node.is-current
      > .el-tree-node__content
  ) {
  background-color: rgba(255, 215, 0, 1) !important;
  font-weight: bold !important;
  color: #000 !important;
}
.custom-dialog .el-dialog__header {
  background-color: #1264bb !important; /* 确保标题背景色应用 */
  color: #16165e !important; /* 确保标题文字颜色应用 */
}

.custom-title {
  font-size: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  color: #020b0c;
}

.custom-close-btn {
  background: none;
  border: none;
  color: white;
  font-size: 24px;
  cursor: pointer;
}

.dialog-content {
  white-space: pre-line;
  font-size: 20px;
  background-color: white; /* 内容背景色 */
  padding: 20px; /* 内容内边距 */
  border-radius: 5px; /* 内容圆角 */
  color: #333; /* 内容字体颜色 */
}
.highlight a.show-record {
  color: #007bff; /* 链接颜色 */
  cursor: pointer; /* 鼠标手形指针 */
  text-decoration: underline; /* 下划线 */
}

.highlight a.show-record:hover {
  text-decoration: none; /* 鼠标悬停时去除下划线 */
  color: #0056b3; /* 鼠标悬停时的颜色 */
}
</style>



