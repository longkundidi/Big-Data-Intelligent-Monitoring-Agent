package com.algorithm.web.mapper.al;

import com.algorithm.web.model.entity.al.AlTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlTaskMapper extends BaseMapper<AlTask> {

	IPage<AlTask> selectClassPage(IPage page, AlTask alTask);

	List<AlTask> searchTasks(@Param("alId") Integer alId, @Param("alClass") String alClass);

	List<AlTask> selectAll(@Param("serverUrl") String serverUrl);

	AlTask getAutoTest();

	@Select("Select url From server_list Where name = #{serverName}")
	String getUrlByName(@Param("serverName") String serverName);

}
