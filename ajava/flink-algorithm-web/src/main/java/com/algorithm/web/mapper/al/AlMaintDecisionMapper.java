package com.algorithm.web.mapper.al;

import com.algorithm.web.model.entity.al.AlMaintDecision;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface AlMaintDecisionMapper extends BaseMapper<AlMaintDecision> {

	String getnamebyid(long id);

	AlMaintDecision getbyId(long id);

	IPage<AlMaintDecision> selectClassPage(IPage page, @Param("modelTypeFirst") String modelTypeFirst,
			@Param("modelFunction") String modelFunction);

	IPage<AlMaintDecision> selectClassPagenull(IPage page, @Param("modelTypeFirst") String modelTypeFirst);

	IPage<AlMaintDecision> selectObj(IPage page, @Param("modelTypeFirst") String modelTypeFirst,
			@Param("modelFunction") String modelFunction, @Param("modelObject") String modelObject);

	IPage<AlMaintDecision> selectName(IPage page, @Param("modelTypeFirst") String modelTypeFirst,
			@Param("modelFunction") String modelFunction, @Param("modelName") String modelName);

	String exitName(String name);

	AlMaintDecision getbyname(String modelName);

	List<AlMaintDecision> getByNameLike(@Param("modelName") String modelName);

	@Select("SELECT model_type FROM al_maint_decision WHERE id = #{alId}")
	String getTypeById(@Param("alId") Long alId);

	@Select("SELECT creator FROM al_maint_decision WHERE id = #{alId}")
	String getCreatorById(@Param("alId") Long alId);

	@Select("SELECT model_num FROM al_maint_decision WHERE id = #{alId}")
	Long getNumById(@Param("alId") Long alId);

	@Select("SELECT model_object, object_id FROM al_maint_decision WHERE id = #{alId}")
	Map<String, Object> getCurrentObject(@Param("alId") Long alId);

}
