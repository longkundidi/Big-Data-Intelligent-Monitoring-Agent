package com.algorithm.web.service.al;

import com.algorithm.web.model.entity.al.ResourceUsage;
import com.algorithm.web.model.vo.docker.ContainerStatsResponse;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ResourceUsageService extends IService<ResourceUsage> {

	ContainerStatsResponse containerStats(String serverName, String containerName);

}
