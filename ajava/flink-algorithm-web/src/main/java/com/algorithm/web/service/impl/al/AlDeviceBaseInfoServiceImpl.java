package com.algorithm.web.service.impl.al;

import com.algorithm.web.mapper.al.AlDeviceBaseInfoMapper;
import com.algorithm.web.model.entity.al.AlDeviceBaseInfo;
import com.algorithm.web.service.al.AlDeviceBaseInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 设备基本信息Service实现类
 */
@Service
public class AlDeviceBaseInfoServiceImpl extends ServiceImpl<AlDeviceBaseInfoMapper, AlDeviceBaseInfo>
		implements AlDeviceBaseInfoService {

	@Override
	public List<AlDeviceBaseInfo> getDevicesBySceneId(Long sceneId) {
		return baseMapper.selectBySceneId(sceneId);
	}

	@Override
	public List<AlDeviceBaseInfo> getDevicesByDeviceCode(String deviceCode) {
		return baseMapper.selectByDeviceCode(deviceCode);
	}

	@Override
	public boolean saveBatchDevices(List<AlDeviceBaseInfo> deviceList) {
		return this.saveBatch(deviceList);
	}

	@Override
	public List<AlDeviceBaseInfo> getDevicesByDeviceCodeAndSceneId(String deviceCode, Long sceneId) {
		return baseMapper.selectByDeviceCodeAndSceneId(deviceCode, sceneId);
	}

	@Override
	public List<AlDeviceBaseInfo> getDevicesByUsageUnitAndDeviceCode(String usageUnit, String deviceCode) {
		return baseMapper.selectByUsageUnitAndDeviceCode(usageUnit, deviceCode);
	}

}
