package com.algorithm.web.mapper.al;

import com.algorithm.web.model.entity.al.AlDeviceBaseInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 设备基本信息Mapper接口
 */
@Repository
public interface AlDeviceBaseInfoMapper extends BaseMapper<AlDeviceBaseInfo> {

	/**
	 * 根据场景ID查询设备列表
	 * @param sceneId 场景ID
	 * @return 设备列表
	 */
	@Select("SELECT * FROM al_device_base_info WHERE scene_id = #{sceneId}")
	List<AlDeviceBaseInfo> selectBySceneId(@Param("sceneId") Long sceneId);

	/**
	 * 根据设备编号查询设备列表
	 * @param deviceCode 设备编号
	 * @return 设备列表
	 */
	@Select("SELECT * FROM al_device_base_info WHERE device_code = #{deviceCode}")
	List<AlDeviceBaseInfo> selectByDeviceCode(@Param("deviceCode") String deviceCode);

	/**
	 * 根据设备编号和场景ID查询设备列表
	 * @param deviceCode 设备编号
	 * @param sceneId 场景ID
	 * @return 设备列表
	 */
	@Select("SELECT * FROM al_device_base_info WHERE device_code = #{deviceCode} AND scene_id = #{sceneId}")
	List<AlDeviceBaseInfo> selectByDeviceCodeAndSceneId(@Param("deviceCode") String deviceCode,
			@Param("sceneId") Long sceneId);

	/**
	 * 根据使用单位和设备编号查询设备列表
	 * @param usageUnit 使用单位
	 * @param deviceCode 设备编号
	 * @return 设备列表
	 */
	@Select("SELECT * FROM al_device_base_info WHERE usage_unit = #{usageUnit} AND device_code = #{deviceCode}")
	List<AlDeviceBaseInfo> selectByUsageUnitAndDeviceCode(@Param("usageUnit") String usageUnit,
			@Param("deviceCode") String deviceCode);

}
