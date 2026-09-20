package com.algorithm.web.ao;

import com.algorithm.web.model.dto.JobConfigDTO;

/**
 * @author zhuhuipei
 * @Description:
 * @date 2021/2/28
 * @time 11:25
 */
public interface JobConfigAO {

	/**
	 * 新增
	 *
	 * @author zhuhuipei
	 * @date 2021/2/28
	 * @time 11:26
	 */
	void addJobConfig(JobConfigDTO jobConfigDTO);

	/**
	 * 修改参数
	 *
	 * @author zhuhuipei
	 * @date 2021/2/28
	 * @time 11:26
	 */
	void updateJobConfigById(JobConfigDTO jobConfigDTO);

}
