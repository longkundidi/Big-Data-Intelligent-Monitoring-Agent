package com.algorithm.web.model.entity.al;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 领域模型基本信息表(DomainModel)表实体类
 *
 * @author makejava
 * @since 2024-11-12 17:46:14
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("domain_model")
public class DomainModel {

	// 主键
	@TableId(type = IdType.AUTO)
	private Long id;

	// 名称
	private String modelName;

	// 基础算法
	private String basicAlgorithm;

	// 针对对象
	private String modelObject;

	// 针对对象在gbom中的节点id
	private String objectId;

	// 适用场景
	private String modelUsecase;

	// 访问路径
	private String modelUrl;

	// 模型图标
	private String modelIcon;

	// 程序包大小
	private String jarSize;

	// 运行所需资源
	private String runSize;

	// 训练数据集
	private String trainDataset;

	// 测试数据集
	private String testDataset;

	// 数据集说明
	private String datasetMemo;

	// 评价指标结果
	private String metricsResult;

	// 程序包路径
	private String programUrl;

	// 模型类型
	private String modelType;

	private String backupModelUrl;

	private String modelShortName;

	private Integer isService;

	private Long modelNum;

	private String input;

	private String output;

}
