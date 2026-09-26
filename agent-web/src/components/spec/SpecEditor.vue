<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { Plus, Trash2 } from 'lucide-vue-next'

const props = defineProps<{ modelValue: any }>()
const emit = defineEmits<{ (event: 'update:modelValue', value: any): void }>()
const copyData = <T,>(value: T): T => JSON.parse(JSON.stringify(value))
const local = ref<any>(copyData(props.modelValue || {}))
let syncing = false

watch(() => props.modelValue, (value) => {
  syncing = true
  local.value = copyData(value || {})
  queueMicrotask(() => { syncing = false })
}, { deep: true })
watch(local, (value) => { if (!syncing) emit('update:modelValue', copyData(value)) }, { deep: true })

const hosts = computed(() => local.value.hosts || (local.value.hosts = []))
const services = computed(() => local.value.services || (local.value.services = []))
const nodes = computed(() => local.value.nodes || (local.value.nodes = []))
const edges = computed(() => local.value.edges || (local.value.edges = []))

function id(prefix: string) { return `${prefix}-${crypto.randomUUID().slice(0, 8)}` }
function addHost() { hosts.value.push({ id: id('host'), name: '新服务器', environment: 'unknown', address: '' }) }
function addService() { services.value.push({ id: id('service'), name: '新服务', type: 'custom', host_id: null, port: null, protocol: 'http', version: '', credential_ref: null, config: {} }) }
function addNode() { nodes.value.push({ id: id('node'), name: '新节点', type: 'custom', service_id: null, config: {} }) }
function addEdge() { if (nodes.value.length >= 2) edges.value.push({ id: id('edge'), source: nodes.value[0].id, target: nodes.value[1].id, type: 'depends_on' }) }
function remove(list: any[], index: number) { list.splice(index, 1) }
function setConfig(target: any, event: Event) {
  const input = event.target as HTMLInputElement
  const value = input.value
  try { target.config = value.trim() ? JSON.parse(value) : {}; input.setCustomValidity('') }
  catch { input.setCustomValidity('请输入合法 JSON'); input.reportValidity() }
}
</script>

<template>
  <div class="spec-editor">
    <label class="spec-name">链路名称<input v-model="local.name" maxlength="120" /></label>

    <section class="spec-section">
      <header><div><strong>服务器</strong><span>部署位置、环境和 IP/主机名</span></div><button type="button" @click="addHost"><Plus :size="13" />添加</button></header>
      <div v-if="hosts.length" class="spec-table spec-hosts">
        <div class="spec-table-head"><span>名称</span><span>地址</span><span>环境</span><span></span></div>
        <div v-for="(host, index) in hosts" :key="host.id" class="spec-table-row"><input v-model="host.name" /><input v-model="host.address" placeholder="IP 或主机名" /><input v-model="host.environment" placeholder="demo/prod" /><button type="button" title="删除服务器" @click="remove(hosts, index)"><Trash2 :size="13" /></button></div>
      </div><p v-else class="spec-empty">尚未添加服务器。</p>
    </section>

    <section class="spec-section">
      <header><div><strong>服务资源</strong><span>Kafka、Flink、模型、数据库等服务</span></div><button type="button" @click="addService"><Plus :size="13" />添加</button></header>
      <div v-if="services.length" class="spec-items"><div v-for="(service, index) in services" :key="service.id" class="spec-item"><div class="spec-item-grid"><label>名称<input v-model="service.name" /></label><label>类型<select v-model="service.type"><option value="kafka">Kafka</option><option value="flink">Flink</option><option value="model">模型服务</option><option value="mysql">MySQL</option><option value="redis">Redis</option><option value="custom">自定义</option></select></label><label>服务器<select v-model="service.host_id"><option :value="null">未指定</option><option v-for="host in hosts" :key="host.id" :value="host.id">{{ host.name }}</option></select></label><label>端口<input v-model.number="service.port" type="number" min="1" max="65535" /></label><label>协议<input v-model="service.protocol" /></label><label>版本<input v-model="service.version" /></label></div><label class="wide-field">扩展配置 JSON<input :value="JSON.stringify(service.config || {})" @change="setConfig(service, $event)" /></label><button type="button" class="spec-delete" title="删除服务" @click="remove(services, index)"><Trash2 :size="13" /></button></div></div><p v-else class="spec-empty">尚未添加服务。</p>
    </section>

    <section class="spec-section">
      <header><div><strong>链路节点</strong><span>Topic、消费组、Flink Job、接口和结果表</span></div><button type="button" @click="addNode"><Plus :size="13" />添加</button></header>
      <div v-if="nodes.length" class="spec-items"><div v-for="(node, index) in nodes" :key="node.id" class="spec-item"><div class="spec-item-grid node-grid"><label>名称<input v-model="node.name" /></label><label>类型<input v-model="node.type" placeholder="kafka_topic" /></label><label>所属服务<select v-model="node.service_id"><option :value="null">未指定</option><option v-for="service in services" :key="service.id" :value="service.id">{{ service.name }}</option></select></label></div><label class="wide-field">节点配置 JSON<input :value="JSON.stringify(node.config || {})" @change="setConfig(node, $event)" /></label><button type="button" class="spec-delete" title="删除节点" @click="remove(nodes, index)"><Trash2 :size="13" /></button></div></div><p v-else class="spec-empty">尚未添加链路节点。</p>
    </section>

    <section class="spec-section">
      <header><div><strong>节点关系</strong><span>生产、消费、调用、响应和写入关系</span></div><button type="button" :disabled="nodes.length < 2" @click="addEdge"><Plus :size="13" />添加</button></header>
      <div v-if="edges.length" class="edge-list"><div v-for="(edge, index) in edges" :key="edge.id"><select v-model="edge.source"><option v-for="node in nodes" :key="node.id" :value="node.id">{{ node.name }}</option></select><span>→</span><select v-model="edge.target"><option v-for="node in nodes" :key="node.id" :value="node.id">{{ node.name }}</option></select><input v-model="edge.type" placeholder="关系类型" /><button type="button" @click="remove(edges, index)"><Trash2 :size="13" /></button></div></div><p v-else class="spec-empty">尚未配置节点关系。</p>
    </section>
  </div>
</template>
