package com.algorithm.web.model.entity.taskConfig;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 记录所有服务器相关信息(ServerList)表实体类
 *
 * @author makejava
 * @since 2024-11-04 17:05:40
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("server_list")
public class ServerList {

	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;

	// 服务器名称
	private String name;

	// 服务器地址
	private String url;

}
