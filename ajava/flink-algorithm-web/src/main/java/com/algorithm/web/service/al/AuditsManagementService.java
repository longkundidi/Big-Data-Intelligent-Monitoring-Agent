package com.algorithm.web.service.al;

import com.algorithm.web.model.entity.al.AuditsManagement;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AuditsManagementService extends IService<AuditsManagement> {

	Page<AuditsManagement> getStagePage(Page<AuditsManagement> page, String stage, String alModelName,
			String alModelType);

	Page<AuditsManagement> getTestNoPassPage(Page<AuditsManagement> alCheckPage, String alModelName,
			String alModelType);

	Page<AuditsManagement> getTestNoDeployPage(Page<AuditsManagement> alCheckPage, String alModelName,
			String alModelType);

	Page<AuditsManagement> getNoTestPage(Page<AuditsManagement> alCheckPage, String alModelName, String alModelType);

	Page<AuditsManagement> getDeployedPage(Page<AuditsManagement> alCheckPage, String alModelName, String alModelType);

}
