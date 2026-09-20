package com.algorithm.web.mapper.al;

import com.algorithm.web.model.entity.al.AlStateEvaluation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface AlStateEvaluationMapper extends BaseMapper<AlStateEvaluation> {

	String getnamebyid(long id);

	AlStateEvaluation getbyId(long id);

	IPage<AlStateEvaluation> selectClassPage(IPage page, @Param("modelTypeFirst") String modelTypeFirst,
			@Param("modelFunction") String modelFunction);

	IPage<AlStateEvaluation> selectClassPagenull(IPage page, @Param("modelTypeFirst") String modelTypeFirst);

	IPage<AlStateEvaluation> selectObj(IPage page, @Param("modelTypeFirst") String modelTypeFirst,
			@Param("modelFunction") String modelFunction, @Param("modelObject") String modelObject);

	IPage<AlStateEvaluation> selectName(IPage page, @Param("modelTypeFirst") String modelTypeFirst,
			@Param("modelFunction") String modelFunction, @Param("modelName") String modelName);

	String exitName(String name);

	AlStateEvaluation getbyname(String modelName);

	@Select("SELECT model_short_name FROM al_state_evaluation WHERE model_name = #{modelName}")
	String getShortNameByName(@Param("modelName") String modelName);

	List<AlStateEvaluation> getByNameLike(@Param("modelName") String modelName);

	@Select("SELECT model_type FROM al_state_evaluation WHERE id = #{alId}")
	String getTypeById(@Param("alId") Long alId);

	@Select("SELECT creator FROM al_state_evaluation WHERE id = #{alId}")
	String getCreatorById(@Param("alId") Long alId);

	@Select("SELECT model_num FROM al_state_evaluation WHERE id = #{alId}")
	Long getNumById(@Param("alId") Long alId);

	@Select("SELECT model_object, object_id FROM al_state_evaluation WHERE id = #{alId}")
	Map<String, Object> getCurrentObject(@Param("alId") Long alId);

}
