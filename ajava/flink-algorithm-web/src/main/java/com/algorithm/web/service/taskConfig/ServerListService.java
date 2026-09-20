package com.algorithm.web.service.taskConfig;

import com.algorithm.web.model.entity.taskConfig.ServerList;
import com.algorithm.web.model.entity.taskConfig.vo.ServerVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 记录所有服务器相关信息(ServerList)表服务接口
 *
 * @author makejava
 * @since 2024-11-04 17:07:52
 */
public interface ServerListService extends IService<ServerList> {

	List<ServerVo> getList();

	List<ServerVo> getListDefault();

}
