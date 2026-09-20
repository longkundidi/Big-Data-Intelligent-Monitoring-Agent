package com.algorithm.web.mapper.al;

import com.algorithm.web.model.entity.al.AlDataClean;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

//@Mapper
@Repository
public interface AlDataCleanMapper extends BaseMapper<AlDataClean> {

	IPage<AlDataClean> selectClassPage(IPage page, @Param("alType") String alType);

	IPage<AlDataClean> selectClassPagenull(IPage page);

	String exitName(String name);

	AlDataClean getbyname(String alName);

	int gettype1();

	int gettype2();

	int gettype3();

	int gettype4();

	int gettype5();

	int gettype6();

	int gettype7();

	int gettype8();

	int gettype9();

	int gettype10();

	@Select("SELECT al_short_name FROM al_data_clean WHERE al_name = #{alName}")
	String getShortNameByName(@Param("alName") String alName);

	List<AlDataClean> getByNameLike(@Param("alName") String alName);

	@Select("SELECT al_type FROM al_data_clean WHERE id = #{alId}")
	String getTypeById(@Param("alId") Long alId);

	@Select("SELECT creator FROM al_data_clean WHERE id = #{alId}")
	String getCreatorById(@Param("alId") Long alId);

	@Select("SELECT al_num FROM al_data_clean WHERE id = #{alId}")
	Long getNumById(@Param("alId") Long alId);

}
