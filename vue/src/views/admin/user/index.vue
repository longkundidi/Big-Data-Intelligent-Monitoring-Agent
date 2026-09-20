<!--
  -    Copyright (c) 2018-2025, lengleng All rights reserved.
  -
  - Redistribution and use in source and binary forms, with or without
  - modification, are permitted provided that the following conditions are met:
  -
  - Redistributions of source code must retain the above copyright notice,
  - this list of conditions and the following disclaimer.
  - Redistributions in binary form must reproduce the above copyright
  - notice, this list of conditions and the following disclaimer in the
  - documentation and/or other materials provided with the distribution.
  - Neither the name of the pig4cloud.com developer nor the names of its
  - contributors may be used to endorse or promote products derived from
  - this software without specific prior written permission.
  - Author: lengleng (wangiegie@gmail.com)
  -->

<template>
  <div class="user">
    <basic-container>
      <avue-crud
          ref="crud"
          v-model="form"
          v-model:page="page"
          :option="option"
          :table-loading="listLoading"
          :before-open="handleOpenBefore"
          :data="list"
          @on-load="getList"
          @size-change="sizeChange"
          @current-change="currentChange"
          @search-change="handleFilter"
          @refresh-change="handleRefreshChange"
          @row-update="update"
          @row-save="create"
      >
        <!-- form存储表单记录-->
        <!-- data存储表格记录-->
        <template #menu-left="{}">
          <el-button
              class="normalBtn"
              icon="el-icon-edit"
              @click="$refs.crud.rowAdd()"
          >添加
          </el-button>
          <el-button
              class="normalBtn"
              plain
              icon="el-icon-upload"
              @click="$refs.excelUpload.show()"
          >导入
          </el-button>
          <el-button
              plain
              class="normalBtn"
              icon="el-icon-download"
              @click="exportExcel"
          >导出
          </el-button>
        </template>
        <!--表格-->
        <template #username="scope">
          <span>{{ scope.row.username }}</span>
        </template>
        <template #role="scope">
          <span v-for="(r, index) in scope.row.roleList" :key="index">
            <el-tag>{{ r.roleName }} </el-tag>&nbsp;&nbsp;
          </span>
        </template>
        <template #farm="scope">
          <span v-if="scope.row.farmList && scope.row.farmList.length"
                v-for="(f, index) in scope.row.farmList" :key="index"
                style="white-space: normal; word-wrap: break-word;">
            <el-tag>{{ f.farmName }} </el-tag>&nbsp;&nbsp;
          </span>
          <span v-else style="color: #999;">暂无管理元模型</span>
        </template>
        <template #scene="scope">
          <span v-if="scope.row.sceneList && scope.row.sceneList.length"
                v-for="(f, index) in scope.row.sceneList" :key="index"
                style="white-space: normal; word-wrap: break-word;">
            <el-tag>{{ f.sceneName }} </el-tag>&nbsp;&nbsp;
          </span>
          <span v-else style="color: #999;">暂无管理场景</span>
        </template>
        <template #post="scope">
          <span v-for="(p, index) in scope.row.postList" :key="index">
            <el-tag>{{ p.postName }} </el-tag>&nbsp;&nbsp;
          </span>
        </template>
        <template #deptName="scope">
          {{ scope.row.deptName }}
        </template>
        <template #lockFlag="scope">
          <dict-tag :options="scope.dic" :value="scope.row.lockFlag"/>
        </template>
        <template #menu="scope">
          <el-button
              v-if="permissions.sys_user_edit"
              class="normalBtn"
              icon="el-icon-edit"
              @click="handleUpdate(scope.row, scope.index)"
          >编辑
          </el-button>
          <el-button
              v-if="permissions.sys_user_del"
              class="normalBtn"
              icon="el-icon-delete"
              @click="deletes(scope.row, scope.index)"
          >删除
          </el-button>
        </template>
        <!-- 表单-->
        <template #deptId-form>
          <avue-input-tree
              v-model="form.deptId"
              placeholder="请选择所属部门"
              :node-click="getNodeData"
              :dic="treeDeptData"
              :props="defaultProps"
          ></avue-input-tree>
        </template>
        <template #role-form>
          <avue-select
              v-model="role"
              multiple
              placeholder="请选择角色"
              :dic="rolesOptions"
              :props="roleProps"
          ></avue-select>
        </template>
        <template #farm-form>
          <avue-select
              v-model="farm"
              placeholder="请选择元模型"
              :dic="farmsOptions"
              :props="farmProps"
          >
            <!-- 自定义选项渲染 -->
            <template v-slot:option="{ item }">
              <div style="display: flex; align-items: center; justify-content: space-between;">
                <span>{{ item.label }}</span>
                <el-button
                    type="text"
                    size="mini"
                    @click.native.stop="cancelFarm(item.value)"
                >
                  取消
                </el-button>
              </div>
            </template>
          </avue-select>
        </template>
        <template #scene-form>
          <avue-select
              v-model="scene"
              multiple
              placeholder="请选择场景"
              :dic="sceneOptions"
              :props="sceneProps"
          >
            <!-- 自定义选项渲染 -->
            <template v-slot:option="{ item }">
              <div style="display: flex; align-items: center; justify-content: space-between;">
                <span>{{ item.label }}</span>
                <el-button
                    type="text"
                    size="mini"
                    @click.native.stop="cancelScene(item.value)"
                >
                  取消
                </el-button>
              </div>
            </template>
          </avue-select>
        </template>
        <template #post-form>
          <avue-select
              v-model="post"
              multiple
              placeholder="请选择岗位"
              :dic="postOptions"
              :props="postProps"
          ></avue-select>
        </template>
      </avue-crud>

      <!--excel 模板导入 -->
      <excel-upload
          ref="excelUpload"
          title="用户信息导入"
          url="/admin/user/import"
          temp-name="用户信息.xlsx"
          temp-url="/admin/sys-file/local/user.xlsx"
          @refreshDataList="handleRefreshChange"
      ></excel-upload>
    </basic-container>
  </div>
</template>

<script>
import {
  addObj,
  delObj,
  fetchList,
  getFarmsByUserId,
  getProjects,
  putObj,
  batchAdd,
  deleteByUserId, batchUpdate,getProjectsByMetaModelId
} from '@/api/admin/user'
import { getAllSceneMetaModels } from '@/api/diagnosis/scene/scene'
import {deptRoleList} from '@/api/admin/role'
import {listPosts} from '@/api/admin/post'
import {fetchTree} from '@/api/admin/dept'
import {tableOption} from '@/const/crud/admin/user'
import {mapGetters} from 'vuex'
import ExcelUpload from '@/components/ExcelUpload/index.vue'

export default {
  name: 'table_user',
  components: {ExcelUpload},
  data() {
    return {
      option: tableOption,
      treeDeptData: [],
      checkedKeys: [],
      postProps: {
        label: 'postName',
        value: 'postId'
      },
      roleProps: {
        label: 'roleName',
        value: 'roleId'
      },
      farmProps: {
        label: 'farmName',
        value: 'farmId'
      },
      sceneProps: {
        label: 'sceneName',
        value: 'sceneId'
      },
      defaultProps: {
        label: 'name',
        value: 'id'
      },
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条,
        isAsc: false //是否倒序
      },
      query: {},
      list: [],
      listLoading: true,
      post: [],
      role: [],
      farm: '',
      scene: [],
      form: {},
      postOptions: [],
      rolesOptions: [],
      farmsOptions: [],
      sceneOptions: [],
      cancelledFarms: []
    }
  },
  computed: {
    ...mapGetters(['permissions'])
  },
  watch: {
    role() {
      this.form.role = this.role
    },
    post() {
      this.form.post = this.post
    },
    scene() {
      this.form.scene = this.scene
    },
    farm: {
      handler(newVal) {
        this.form.farm = newVal
        if (newVal) {
          this.scene = []
          this.getSceneOptions(newVal)
        } else {
          this.sceneOptions = []
          this.scene = []
        }
      },
      immediate: false
    }
  },
  methods: {
    getList(page, params) {
      this.listLoading = true;
      // 返回 fetchList 的 Promise 链
      return fetchList(
          Object.assign(
              {
                current: page.currentPage,
                size: page.pageSize
              },
              params
          )
      ).then(response => {
        this.list = response.data.data.records;
        this.page.total = response.data.data.total;

        // 先获取所有元模型列表，用于匹配
        return getAllSceneMetaModels().then(metaModelsResponse => {
          const allMetaModels = metaModelsResponse.data.data || [];
          
          // 使用 Promise.all 等待所有 getFarmsByUserId 完成
          const farmPromises = this.list.map(item => {
            return getFarmsByUserId(item.userId).then(res => {
              const original = res.data.data;
              item.sceneList = [];
              item.farmList = [];
              
              if (original!==null && original.length !== 0) {
                // 处理场景列表（包含 farmName 的数据项）
                const seen = new Set();
                original.forEach(subItem => {
                  // 只筛选包含 farmName 字段的数据项（排除 metaModelName）
                  if (subItem.farmName && !subItem.metaModelName) {
                    if (!seen.has(subItem.farmName)) {
                      item.sceneList.push({
                        sceneId: subItem.farmId,
                        sceneName: subItem.farmName
                      });
                      seen.add(subItem.farmName);
                    }
                  }
                });
                
                // 处理元模型列表（包含 metaModelName 和 metaModelId 的数据项）
                const seenMetaModelIds = new Set();
                original.forEach(subItem => {
                  // 筛选包含 metaModelName 和 metaModelId 的数据项
                  if (subItem.metaModelName && subItem.metaModelId) {
                    // 避免重复添加相同的 metaModelId
                    if (!seenMetaModelIds.has(subItem.metaModelId)) {
                      // 在所有元模型列表中查找匹配的元模型
                      const matchedMetaModel = allMetaModels.find(metaModel => {
                        return String(metaModel.id) === String(subItem.metaModelId);
                      });
                      
                      if (matchedMetaModel) {
                        item.farmList.push({
                          farmId: matchedMetaModel.id,
                          farmName: matchedMetaModel.project
                        });
                        seenMetaModelIds.add(subItem.metaModelId);
                      }
                    }
                  }
                });
              }
            });
          });

          // 等待所有 farm 数据加载完成
          return Promise.all(farmPromises).then(() => {
            this.listLoading = false;
            return response; // 返回 response 供外部链式调用
          });
        });
      });
    },

    getNodeData(data) {
      deptRoleList().then(response => {
        this.rolesOptions = response.data.data
      })
      listPosts().then(response => {
        this.postOptions = response.data.data
      })
      //获取元模型信息
      getAllSceneMetaModels().then(response => {
        this.farmsOptions = response.data.data
      })
    },
    sizeChange(pageSize) {
      this.page.pageSize = pageSize
    },
    currentChange(current) {
      this.page.currentPage = current
    },
    handleFilter(param, done) {
      this.query = param
      this.page.currentPage = 1
      this.getList(this.page, param)
      done()
    },
    handleRefreshChange() {
      this.getList(this.page)
    },
    getSceneOptions(metaModelId) {
      if (!metaModelId) {
        this.sceneOptions = []
        return
      }
      // 调用接口获取场景
      getProjectsByMetaModelId(metaModelId).then(response => {
        // 返回的是字符串数组
        const original = response.data.data
        this.sceneOptions = (original || []).map(sceneName => ({
          sceneId: sceneName,
          sceneName: sceneName   // 用场景名作为 label
        }))
        // if (this.form.sceneList && this.form.sceneList.length > 0) {
        //   this.scene = this.form.sceneList.map(item => item.sceneName)
        // }
      })
    },
    handleOpenBefore(show, type) {
      window.boxType = type
      // 查询部门树
      fetchTree().then(response => {
        this.treeDeptData = response.data.data
      })
      // 查询角色列表
      deptRoleList().then(response => {
        this.rolesOptions = response.data.data

      })
      // 查询元模型列表和对应场景
      getAllSceneMetaModels().then(response => {
        let original = response.data.data
        original.forEach(item => {
          let newItem = { // 在每次循环都创建新对象
            farmId: item.id,
            farmName: item.project
          };
          this.farmsOptions.push(newItem)
        })
        const seen = new Set();
        this.farmsOptions = this.farmsOptions.filter((item) => {
          // 如果 farmName 已经存在于 Set 中，则过滤掉
          if (seen.has(item.farmName)) {
            return false;
          }
          // 否则，将 farmName 添加到 Set 中并保留该元素
          seen.add(item.farmName);
          return true;
        });
        if (type === 'add') {
          this.farm = ''
          this.scene = []
        }
        if (['edit', 'views'].includes(type)) {
          this.scene = []
          if (this.form.sceneList && this.form.sceneList.length > 0) {
            this.scene = this.form.sceneList.map(item => item.sceneName)
          }
          const farmList = this.form.farmList || []
          if (farmList.length > 0) {
            const farmItem = farmList[0]
            const matchedFarm = this.farmsOptions.find(option => {
              if (farmItem.farmId && option.farmId === farmItem.farmId) {
                return true
              }
              return option.farmName === farmItem.farmName
            })
            this.farm = matchedFarm ? matchedFarm.farmId : ''
          } else {
            this.farm = ''
          }
        }
        show()


      })
      //查询岗位列表
      listPosts().then(response => {
        this.postOptions = response.data.data
      })
      // 若是编辑、查看回显角色名称
      if (['edit', 'views'].includes(type)) {
        this.role = []
        for (let i = 0; i < this.form.roleList.length; i++) {
          this.role[i] = this.form.roleList[i].roleId
        }
        this.post = []
        for (let i = 0; i < this.form.postList.length; i++) {
          this.post[i] = this.form.postList[i].postId
        }
        const farmList = this.form.farmList || []
        this.farm = ''
        if (farmList.length > 0) {
          const targetName = farmList[0].farmName
          const matchedFarm = this.farmsOptions.find(item => item.farmName === targetName)
          this.farm = matchedFarm ? matchedFarm.farmId : ''
        }
        this.scene = []
        for (let i = 0; i < this.form.sceneList.length; i++) {
          this.scene[i] = this.form.sceneList[i].sceneName
        }
      } else if (type === 'add') {
        // 若是添加角色列表设置为空
        this.role = []
        this.post = []
        this.farm = ''
        this.scene = []
      }
      show()
    },
    handleUpdate(row, index) {
      this.$refs.crud.rowEdit(row, index)
      this.form.password = undefined
    },

    async create(row, done, loading) {
      try {
        // 1. 提交新增用户请求
        await addObj(this.form);

        // 2. 等待 getList 完成并更新列表数据
        await this.getList(this.page);

        // 3. 检查数据是否有效
        if (!this.list.length || !this.list[0].userId) {
          this.$notify.error('获取用户列表失败');
          return;
        }

        // 4. 处理场景关联逻辑
        // this.scene 是选中的场景id数组（或单值），sceneOptions 是所有可选场景
        // 需要将选中的 sceneId 转为场景名但用farmNames表示，为了不改后端
        let farmNames = [];
        if (Array.isArray(this.scene)) {
          farmNames = this.scene.map(sceneId => {
            const sceneOption = this.sceneOptions.find(option => option.sceneId === sceneId);
            return sceneOption ? sceneOption.sceneName : sceneId;
          });
        } else if (this.scene) {
          // 单选情况
          const sceneOption = this.sceneOptions.find(option => option.sceneId === this.scene);
          farmNames = [sceneOption ? sceneOption.sceneName : this.scene];
        }

        const metaModelId = this.farm || null
        const metaModelOption = this.farmsOptions.find(item => item.farmId === this.farm)
        const metaModelName = metaModelOption ? metaModelOption.farmName : null

        if ((farmNames && farmNames.length) || metaModelId) {
          console.log(farmNames)
          const res = await new Promise((resolve, reject) => {
            batchAdd({
              userId: this.list[0].userId,
              farmNames: farmNames,
              metaModelId: metaModelId,
              metaModelName: metaModelName
            })
                .then(response => resolve(response))
                .catch(error => reject(error));
          });
          if (res.data.code === 0) {
            await this.getList(this.page)
            done()
            this.$notify.success('创建成功');
          } else {
            this.$notify.error(res.data.msg || '创建失败');
          }
        }
      } catch (error) {
        loading();
        this.$notify.error('操作失败，请重试');
      }
    },

    async update(row, index, done, loading) {
      await putObj(this.form)

      // 处理场景关联逻辑，用farmNames接收场景名，为了不改后端
      let farmNames = [];
      if (Array.isArray(this.scene)) {
        farmNames = this.scene.map(sceneId => {
          const sceneOption = this.sceneOptions.find(option => option.sceneId === sceneId);
          return sceneOption ? sceneOption.sceneName : sceneId;
        });
      } else if (this.scene) {
        // 单选情况
        const sceneOption = this.sceneOptions.find(option => option.sceneId === this.scene);
        farmNames = [sceneOption ? sceneOption.sceneName : this.scene];
      }
      const metaModelId = this.farm || null
      const metaModelOption = this.farmsOptions.find(item => item.farmId === this.farm)
      const metaModelName = metaModelOption ? metaModelOption.farmName : null
      batchUpdate({
        userId: row.userId,
        farmNames: farmNames,
        metaModelId: metaModelId,
        metaModelName: metaModelName
      }).then(res => {
        if (res.data.code === 0) {
          this.getList(this.page);
          done()
          this.$notify.success('修改成功')
        } else {
          this.$notify.error(res.data.msg || '修改失败')
        }
      })
    },
    deletes(row) {
      this.$confirm(
          '此操作将永久删除该用户(用户名:' + row.username + '), 是否继续?',
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
      ).then(() => {
        delObj(row.userId)
            .then((res) => {
              //删除风场-用户记录
              getFarmsByUserId(row.userId).then((res) => {
                if (res.data.data!==null && res.data.data.length !== 0 ) {
                  deleteByUserId(row.userId)
                }
                this.getList(this.page)
                this.$notify.success('删除成功')
              })

            })
            .catch(() => {
              this.$notify.error('删除失败')
            })
      })
    },
    exportExcel() {
      this.downBlobFile('/admin/user/export', this.query, 'user.xlsx')
    }
  }
}
</script>
