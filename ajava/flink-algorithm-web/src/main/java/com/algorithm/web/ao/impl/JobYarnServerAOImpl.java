package com.algorithm.web.ao.impl;

import com.algorithm.common.enums.JobTypeEnum;
import com.algorithm.web.ao.JobBaseServiceAO;
import com.algorithm.web.ao.JobServerAO;
import com.algorithm.web.common.MessageConstants;
import com.algorithm.web.common.SystemConstants;
import com.algorithm.web.exceptions.BizException;
import com.algorithm.web.model.entity.BatchJob;
import com.algorithm.web.enums.JobConfigStatus;
import com.algorithm.web.enums.SysErrorEnum;
import com.algorithm.web.enums.YN;
import com.algorithm.web.model.dto.JobConfigDTO;
import com.algorithm.web.model.dto.JobRunParamDTO;
import com.algorithm.web.quartz.BatchJobManagerScheduler;
import com.algorithm.web.rpc.CommandRpcClinetAdapter;
import com.algorithm.web.rpc.YarnRestRpcAdapter;
import com.algorithm.web.rpc.model.JobInfo;
import com.algorithm.web.service.JobConfigService;
import com.algorithm.web.service.SavepointBackupService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author zhuhuipei
 * @Description:
 * @date 2020-07-20
 * @time 23:11
 */
@Component(SystemConstants.BEANNAME_JOBYARNSERVERAO)
@Slf4j
public class JobYarnServerAOImpl implements JobServerAO {

	// 最大重试次数
	private static final Integer TRY_TIMES = 2;

	@Autowired
	private JobConfigService jobConfigService;

	@Autowired
	private YarnRestRpcAdapter yarnRestRpcAdapter;

	@Autowired
	private CommandRpcClinetAdapter commandRpcClinetAdapter;

	@Autowired
	private SavepointBackupService savepointBackupService;

	@Autowired
	private JobBaseServiceAO jobBaseServiceAO;

	@Autowired
	private BatchJobManagerScheduler batchJobRegister;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void start(Long id, Long savepointId, String userName) {

		JobConfigDTO jobConfigDTO = jobConfigService.getJobConfigById(id);

		// 1、检查jobConfigDTO 状态等参数
		jobBaseServiceAO.checkStart(jobConfigDTO);

		if (StringUtils.isNotEmpty(jobConfigDTO.getJobId())) {
			this.stop(jobConfigDTO);
		}

		// 2、将配置的sql 写入本地文件并且返回运行所需参数
		JobRunParamDTO jobRunParamDTO = jobBaseServiceAO.writeSqlToFile(jobConfigDTO);

		// 3、插一条运行日志数据
		Long jobRunLogId = jobBaseServiceAO.insertJobRunLog(jobConfigDTO, userName);

		// 4、变更任务状态（变更为：启动中） 有乐观锁 防止重复提交
		jobConfigService.updateStatusByStart(jobConfigDTO.getId(), userName, jobRunLogId, jobConfigDTO.getVersion());

		String savepointPath = savepointBackupService.getSavepointPathById(id, savepointId);

		// 异步提交任务
		jobBaseServiceAO.aSyncExecJob(jobRunParamDTO, jobConfigDTO, jobRunLogId, savepointPath);

	}

	@Override
	public void stop(Long id, String userName) {
		log.info("[{}]开始停止任务[{}]", userName, id);
		JobConfigDTO jobConfigDTO = jobConfigService.getJobConfigById(id);
		if (jobConfigDTO == null) {
			throw new BizException(SysErrorEnum.JOB_CONFIG_JOB_IS_NOT_EXIST);
		}
		// 1、停止前做一次savepoint操作
		try {
			this.savepoint(id);
		}
		catch (Exception e) {
			log.error(MessageConstants.MESSAGE_008, e);
		}
		// 2、停止任务
		this.stop(jobConfigDTO);
		JobConfigDTO jobConfig = new JobConfigDTO();
		jobConfig.setStatus(JobConfigStatus.STOP);
		jobConfig.setEditor(userName);
		jobConfig.setId(id);
		jobConfig.setJobId("");
		// 3、变更状态
		jobConfigService.updateJobConfigById(jobConfig);
	}

	@Override
	public void savepoint(Long id) {
		JobConfigDTO jobConfigDTO = jobConfigService.getJobConfigById(id);

		jobBaseServiceAO.checkSavepoint(jobConfigDTO);

		JobInfo jobInfo = yarnRestRpcAdapter.getJobInfoForPerYarnByAppId(jobConfigDTO.getJobId());
		if (jobInfo == null) {
			log.warn(MessageConstants.MESSAGE_007, jobConfigDTO.getJobName());
			throw new BizException(MessageConstants.MESSAGE_007);
		}
		// 1、 执行savepoint
		try {
			commandRpcClinetAdapter.savepointForPerYarn(jobInfo.getId(),
					SystemConstants.DEFAULT_SAVEPOINT_ROOT_PATH + id, jobConfigDTO.getJobId());
		}
		catch (Exception e) {
			log.error(MessageConstants.MESSAGE_008, e);
			throw new BizException(MessageConstants.MESSAGE_008);
		}

		String savepointPath = yarnRestRpcAdapter.getSavepointPath(jobConfigDTO.getJobId(), jobInfo.getId());
		if (StringUtils.isEmpty(savepointPath)) {
			log.warn(MessageConstants.MESSAGE_009, jobConfigDTO);
			throw new BizException(MessageConstants.MESSAGE_009);
		}
		// 2、 执行保存Savepoint到本地数据库
		savepointBackupService.insertSavepoint(id, savepointPath, new Date());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void open(Long id, String userName) {
		JobConfigDTO jobConfigDTO = jobConfigService.getJobConfigById(id);
		if (jobConfigDTO == null) {
			return;
		}
		if (jobConfigDTO.getJobTypeEnum() == JobTypeEnum.SQL_BATCH && StringUtils.isNotEmpty(jobConfigDTO.getCron())) {
			batchJobRegister.registerJob(new BatchJob(id, jobConfigDTO.getJobName(), jobConfigDTO.getCron()));
		}

		jobConfigService.openOrClose(id, YN.Y, userName);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void close(Long id, String userName) {
		JobConfigDTO jobConfigDTO = jobConfigService.getJobConfigById(id);
		jobBaseServiceAO.checkClose(jobConfigDTO);
		jobConfigService.openOrClose(id, YN.N, userName);
		if (jobConfigDTO.getJobTypeEnum() == JobTypeEnum.SQL_BATCH) {
			batchJobRegister.deleteJob(id);

		}

	}

	private void stop(JobConfigDTO jobConfigDTO) {
		Integer retryNum = 1;
		while (retryNum <= TRY_TIMES) {
			JobInfo jobInfo = yarnRestRpcAdapter.getJobInfoForPerYarnByAppId(jobConfigDTO.getJobId());
			log.info("任务[{}]当前状态为：{}", jobConfigDTO.getId(), jobInfo);
			if (jobInfo != null && SystemConstants.STATUS_RUNNING.equals(jobInfo.getStatus())) {
				log.info("执行停止操作 jobYarnInfo={} retryNum={} id={}", jobInfo, retryNum, jobConfigDTO.getJobId());
				yarnRestRpcAdapter.cancelJobForYarnByAppId(jobConfigDTO.getJobId(), jobInfo.getId());
			}
			else {
				log.info("任务已经停止 jobYarnInfo={} id={}", jobInfo, jobConfigDTO.getJobId());
				break;
			}
			retryNum++;
		}
	}

}
