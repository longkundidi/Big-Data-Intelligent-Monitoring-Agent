package com.algorithm.web.service.impl.al;

import com.algorithm.web.mapper.al.AuditsManagementMapper;
import com.algorithm.web.model.entity.al.AuditsManagement;
import com.algorithm.web.service.al.AuditsManagementService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditsManagementServiceImpl extends ServiceImpl<AuditsManagementMapper, AuditsManagement>
		implements AuditsManagementService {

	@Autowired
	private AuditsManagementMapper auditsManagementMapper;

	@Override
	public Page<AuditsManagement> getStagePage(Page<AuditsManagement> page, String stage, String alModelName,
			String alModelType) {
		return auditsManagementMapper.getStagePage(page, stage, alModelName, alModelType);
	}

	@Override
	public Page<AuditsManagement> getTestNoPassPage(Page<AuditsManagement> alCheckPage, String alModelName,
			String alModelType) {
		return auditsManagementMapper.getTestNoPassPage(alCheckPage, alModelName, alModelType);
	}

	@Override
	public Page<AuditsManagement> getTestNoDeployPage(Page<AuditsManagement> alCheckPage, String alModelName,
			String alModelType) {
		return auditsManagementMapper.getTestNoDeployPage(alCheckPage, alModelName, alModelType);
	}

	@Override
	public Page<AuditsManagement> getNoTestPage(Page<AuditsManagement> alCheckPage, String alModelName,
			String alModelType) {
		return auditsManagementMapper.getNoTestPage(alCheckPage, alModelName, alModelType);
	}

	@Override
	public Page<AuditsManagement> getDeployedPage(Page<AuditsManagement> alCheckPage, String alModelName,
			String alModelType) {
		return auditsManagementMapper.getDeployedPage(alCheckPage, alModelName, alModelType);
	}

}
