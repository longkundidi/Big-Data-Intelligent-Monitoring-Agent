package com.algorithm.web.mapper.al;

import com.algorithm.web.model.entity.al.AlResumeFaultData;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface AlResumeFaultDataMapper extends BaseMapper<AlResumeFaultData> {

	List<Map<String, Object>> getFaultInfo(@Param("project_id") Long projectId, @Param("device_id") Long deviceId);

}
