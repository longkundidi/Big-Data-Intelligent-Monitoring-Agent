package com.algorithm.web.mapper.al;

import com.algorithm.web.model.entity.al.AlConditionCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface AlConditionCategoryMapper extends BaseMapper<AlConditionCategory> {

	String getnamebyid(long id);

	AlConditionCategory getbyId(long id);

	IPage<AlConditionCategory> selectClassPage(IPage page, @Param("modelTypeFirst") String modelTypeFirst,
			@Param("modelFunction") String modelFunction);

	IPage<AlConditionCategory> selectClassPagenull(IPage page, @Param("modelTypeFirst") String modelTypeFirst);

	IPage<AlConditionCategory> selectObj(IPage page, @Param("modelTypeFirst") String modelTypeFirst,
			@Param("modelFunction") String modelFunction, @Param("modelObject") String modelObject);

	IPage<AlConditionCategory> selectName(IPage page, @Param("modelTypeFirst") String modelTypeFirst,
			@Param("modelFunction") String modelFunction, @Param("modelName") String modelName);

	String exitName(String name);

	AlConditionCategory getbyname(String modelName);

	List<AlConditionCategory> getByNameLike(@Param("modelName") String modelName);

	@Select("SELECT model_type FROM al_condition_category WHERE id = #{alId}")
	String getTypeById(@Param("alId") Long alId);

	@Select("SELECT creator FROM al_condition_category WHERE id = #{alId}")
	String getCreatorById(@Param("alId") Long alId);

	@Select("SELECT model_num FROM al_condition_category WHERE id = #{alId}")
	Long getNumById(@Param("alId") Long alId);

	@Select("SELECT model_object, object_id FROM al_condition_category WHERE id = #{alId}")
	Map<String, Object> getCurrentObject(@Param("alId") Long alId);

}
