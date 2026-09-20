package com.algorithm.web.mapper.al;

import com.algorithm.web.model.entity.al.AlFaultPropagation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface AlFaultPropagationMapper extends BaseMapper<AlFaultPropagation> {

	String getnamebyid(long id);

	AlFaultPropagation getbyId(long id);

	IPage<AlFaultPropagation> selectClassPage(IPage page, @Param("modelTypeFirst") String modelTypeFirst,
			@Param("modelFunction") String modelFunction);

	IPage<AlFaultPropagation> selectClassPagenull(IPage page, @Param("modelTypeFirst") String modelTypeFirst);

	IPage<AlFaultPropagation> selectObj(IPage page, @Param("modelTypeFirst") String modelTypeFirst,
			@Param("modelFunction") String modelFunction, @Param("modelObject") String modelObject);

	IPage<AlFaultPropagation> selectName(IPage page, @Param("modelTypeFirst") String modelTypeFirst,
			@Param("modelFunction") String modelFunction, @Param("modelName") String modelName);

	String exitName(String name);

	AlFaultPropagation getbyname(String modelName);

	List<AlFaultPropagation> getByNameLike(@Param("modelName") String modelName);

	@Select("SELECT model_type FROM al_fault_propagation WHERE id = #{alId}")
	String getTypeById(@Param("alId") Long alId);

	@Select("SELECT creator FROM al_fault_propagation WHERE id = #{alId}")
	String getCreatorById(@Param("alId") Long alId);

	@Select("SELECT model_num FROM al_fault_propagation WHERE id = #{alId}")
	Long getNumById(@Param("alId") Long alId);

	@Select("SELECT model_object, object_id FROM al_fault_propagation WHERE id = #{alId}")
	Map<String, Object> getCurrentObject(@Param("alId") Long alId);

}
