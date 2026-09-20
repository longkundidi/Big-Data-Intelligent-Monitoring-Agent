package com.algorithm.web.service.al;

import com.algorithm.web.model.entity.al.DomainModel;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 领域模型基本信息表(DomainModel)表服务接口
 *
 * @author makejava
 * @since 2024-11-12 17:46:20
 */
public interface DomainModelService extends IService<DomainModel> {

	Boolean exitName(String name);

	List<DomainModel> getlistByBasicAlgorithm(String basicAlgorithm, String modelType);

	Map<String, Object> getCurrentObject(Long alId, String type);

}
