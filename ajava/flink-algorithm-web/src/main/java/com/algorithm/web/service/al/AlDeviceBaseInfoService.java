package com.algorithm.web.service.al;

import com.algorithm.web.model.entity.al.AlDeviceBaseInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 设备基本信息Service接口
 */
public interface AlDeviceBaseInfoService extends IService<AlDeviceBaseInfo> {

	/**
	 * 根据场景ID查询设备列表
	 * @param sceneId 场景ID
	 * @return 设备列表
	 */
	List<AlDeviceBaseInfo> getDevicesBySceneId(Long sceneId);

	/**
	 * 根据设备编号查询设备列表
	 * @param deviceCode 设备编号
	 * @return 设备列表
	 */
	List<AlDeviceBaseInfo> getDevicesByDeviceCode(String deviceCode);

	/**
	 * 批量保存设备信息
	 * @param deviceList 设备列表
	 * @return 是否成功
	 */
	boolean saveBatchDevices(List<AlDeviceBaseInfo> deviceList);

	/**
	 * 根据设备编号和场景ID查询设备列表
	 * @param deviceCode 设备编号
	 * @param sceneId 场景ID
	 * @return 设备列表
	 */
	List<AlDeviceBaseInfo> getDevicesByDeviceCodeAndSceneId(String deviceCode, Long sceneId);

	/**
	 * 根据使用单位和设备编号查询设备列表
	 * @param usageUnit 使用单位
	 * @param deviceCode 设备编号
	 * @return 设备列表
	 */
	List<AlDeviceBaseInfo> getDevicesByUsageUnitAndDeviceCode(String usageUnit, String deviceCode);

}
