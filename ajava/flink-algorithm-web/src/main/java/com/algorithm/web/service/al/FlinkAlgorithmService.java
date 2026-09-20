package com.algorithm.web.service.al;

import com.algorithm.web.model.entity.al.AlDataClean;
import com.algorithm.web.model.entity.flink.FlinkAlgorithm;
import com.algorithm.web.model.entity.flink.FlinkInfo;
import com.algorithm.web.model.vo.flink.FlinkOperationsOverviewVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

public interface FlinkAlgorithmService {

	FlinkInfo getFlinkInfo();

	IPage<FlinkAlgorithm> getPage(Page<FlinkAlgorithm> page, String jobId, String jobName);

	FlinkOperationsOverviewVo getOperationsOverview();

}
