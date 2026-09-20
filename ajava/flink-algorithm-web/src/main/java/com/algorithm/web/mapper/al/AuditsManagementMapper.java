package com.algorithm.web.mapper.al;

import com.algorithm.web.model.entity.al.AuditsManagement;
import com.algorithm.web.model.vo.AlgorithmDashboardCountVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AuditsManagementMapper extends BaseMapper<AuditsManagement> {

	Page<AuditsManagement> getStagePage(Page<AuditsManagement> page, @Param("stage") String stage,
			@Param("alModelName") String alModelName, @Param("alModelType") String alModelType);

	AuditsManagement getBySourceAndId(@Param("sourceType") String sourceType, @Param("algorithmId") Long algorithmId);

	int updateWorkflowStatus(AuditsManagement auditsManagement);

	AlgorithmDashboardCountVo getStateEvaluationDashboardCounts();

	AlgorithmDashboardCountVo getFaultDiagnosisDashboardCounts();

	Page<AuditsManagement> getTestNoPassPage(Page<AuditsManagement> alCheckPage,
			@Param("alModelName") String alModelName, @Param("alModelType") String alModelType);

	Page<AuditsManagement> getTestNoDeployPage(Page<AuditsManagement> alCheckPage,
			@Param("alModelName") String alModelName, @Param("alModelType") String alModelType);

	Page<AuditsManagement> getNoTestPage(Page<AuditsManagement> alCheckPage, @Param("alModelName") String alModelName,
			@Param("alModelType") String alModelType);

	Page<AuditsManagement> getDeployedPage(Page<AuditsManagement> alCheckPage, @Param("alModelName") String alModelName,
			@Param("alModelType") String alModelType);

	// 已测试
	@Select("SELECT COUNT(*) FROM al_data_clean WHERE is_pass=0")
	Long getTestCountByDataClean();

	@Select("SELECT COUNT(*) FROM al_knowledge_extraction WHERE is_pass=0")
	Long getTestCountByKnowledgeExtraction();

	@Select("SELECT COUNT(*) FROM al_fault_diagnosis WHERE is_pass=0")
	Long getTestCountByFaultDiagnose();

	@Select("SELECT COUNT(*) FROM al_state_evaluation WHERE is_pass=0")
	Long getTestCountByStateEvaluation();

	// 待测试
	@Select("SELECT COUNT(*) FROM al_data_clean WHERE is_pass=2")
	Long getNoTestCountByDataClean();

	@Select("SELECT COUNT(*) FROM al_knowledge_extraction WHERE is_pass=2")
	Long getNoTestCountByKnowledgeExtraction();

	@Select("SELECT COUNT(*) FROM al_fault_diagnosis WHERE is_pass=2")
	Long getNoTestCountByFaultDiagnose();

	@Select("SELECT COUNT(*) FROM al_state_evaluation WHERE is_pass=2")
	Long getNoTestCountByStateEvaluation();

	// 已审核
	@Select("SELECT COUNT(*) FROM al_data_clean WHERE is_check=0")
	Long getDeployedCountByDataClean();

	@Select("SELECT COUNT(*) FROM al_knowledge_extraction WHERE is_check=0")
	Long getDeployedCountByKnowledgeExtraction();

	@Select("SELECT COUNT(*) FROM al_fault_diagnosis WHERE is_check=0")
	Long getDeployedCountByFaultDiagnose();

	@Select("SELECT COUNT(*) FROM al_state_evaluation WHERE is_check=0")
	Long getDeployedCountByStateEvaluation();

	// 待审核
	@Select("SELECT COUNT(*) FROM al_data_clean WHERE is_check=1")
	Long getNoDeployedCountByDataClean();

	@Select("SELECT COUNT(*) FROM al_knowledge_extraction WHERE is_check=1")
	Long getNoDeployedCountByKnowledgeExtraction();

	@Select("SELECT COUNT(*) FROM al_fault_diagnosis WHERE is_check=1")
	Long getNoDeployedCountByFaultDiagnose();

	@Select("SELECT COUNT(*) FROM al_state_evaluation WHERE is_check=1")
	Long getNoDeployedCountByStateEvaluation();

}
