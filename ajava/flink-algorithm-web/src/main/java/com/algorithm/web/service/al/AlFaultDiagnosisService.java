package com.algorithm.web.service.al;

import com.algorithm.web.model.dto.al.DiagnoseInfoDto;
import com.algorithm.web.model.entity.al.AlDiagnosisRecord;
import com.algorithm.web.model.entity.al.AlFaultDiagnosis;
import com.algorithm.web.model.vo.FaultDiagnosisAllInfoVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface AlFaultDiagnosisService extends IService<AlFaultDiagnosis> {

	String getnamebyid(long id);

	AlFaultDiagnosis getbyname(String modelName);

	IPage<AlFaultDiagnosis> getPage(IPage page, AlFaultDiagnosis alFaultDiagnosis);

	IPage<AlFaultDiagnosis> getPageObj(IPage page, AlFaultDiagnosis alFaultDiagnosis);

	IPage<AlFaultDiagnosis> getPageName(IPage page, AlFaultDiagnosis alFaultDiagnosis);

	AlFaultDiagnosis getbyId(long d);

	Boolean exitName(String name);

	Object operate(DiagnoseInfoDto diagnoseInfoDto);

	Object signalAnalysis(DiagnoseInfoDto diagnoseInfoDto);

	List<AlDiagnosisRecord> listDiagnosisRecords(String turbineCode, String startTime);

	Map<String, Object> getDiagnosisRecord(Long id);

	/**
	 * 注册时更新code
	 * @param modelName
	 * @return
	 */
	boolean updateCodeByName(String modelName);

}
