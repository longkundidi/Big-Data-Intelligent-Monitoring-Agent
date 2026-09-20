package com.algorithm.web.mapper.al;

import com.algorithm.web.model.entity.al.AlFaultDiagnosis;
import com.algorithm.web.model.vo.FaultDiagnosisAllInfoVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface AlFaultDiagnosisMapper extends BaseMapper<AlFaultDiagnosis> {

	String getnamebyid(long id);

	AlFaultDiagnosis getbyId(long id);

	IPage<AlFaultDiagnosis> selectClassPage(IPage page, @Param("modelTypeFirst") String modelTypeFirst,
			@Param("modelFunction") String modelFunction);

	IPage<AlFaultDiagnosis> selectClassPagenull(IPage page, @Param("modelTypeFirst") String modelTypeFirst);

	IPage<AlFaultDiagnosis> selectObj(IPage page, @Param("modelTypeFirst") String modelTypeFirst,
			@Param("modelFunction") String modelFunction, @Param("modelObject") String modelObject);

	IPage<AlFaultDiagnosis> selectName(IPage page, @Param("modelTypeFirst") String modelTypeFirst,
			@Param("modelFunction") String modelFunction, @Param("modelName") String modelName);

	String exitName(String name);

	AlFaultDiagnosis getbyname(String modelName);

	List<AlFaultDiagnosis> getByNameLike(@Param("modelName") String modelName);

	@Select("SELECT model_type FROM al_fault_diagnosis WHERE id = #{alId}")
	String getTypeById(@Param("alId") Long alId);

	@Select("SELECT creator FROM al_fault_diagnosis WHERE id = #{alId}")
	String getCreatorById(@Param("alId") Long alId);

	@Select("SELECT model_num FROM al_fault_diagnosis WHERE id = #{alId}")
	Long getNumById(@Param("alId") Long alId);

	@Select("SELECT model_object, object_id FROM al_fault_diagnosis WHERE id = #{alId}")
	Map<String, Object> getCurrentObject(@Param("alId") Long alId);

	List<FaultDiagnosisAllInfoVo> selectAllInfo();

}
