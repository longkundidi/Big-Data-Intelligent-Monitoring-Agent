package com.algorithm.web.mapper.taskConfig;

import com.algorithm.web.model.entity.taskConfig.ServerList;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 记录所有服务器相关信息(ServerList)表数据库访问层
 *
 * @author makejava
 * @since 2024-11-04 17:23:19
 */
@Mapper
public interface ServerListMapper extends BaseMapper<ServerList> {

	@Select("SELECT url FROM server_list WHERE name = #{serverName}")
	String getUrlByName(@Param("serverName") String serverName);

}
