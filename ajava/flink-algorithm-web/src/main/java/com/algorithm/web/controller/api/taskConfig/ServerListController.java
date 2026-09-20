package com.algorithm.web.controller.api.taskConfig;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.service.taskConfig.ServerListService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;

/**
 * 记录所有服务器相关信息(ServerList)表控制层
 *
 * @author makejava
 * @since 2024-11-04 17:07:09
 */
@RestController
@RequestMapping("/api/serverList")
@CrossOrigin
public class ServerListController {

	/**
	 * 服务对象
	 */
	@Autowired
	private ServerListService serverListService;

	@GetMapping("/list")
	public RestResult getList() {
		return RestResult.success(serverListService.getListDefault());
	}

}
