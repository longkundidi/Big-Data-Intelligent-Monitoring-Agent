package sw.model3d.configModel.entity;

import java.time.LocalDateTime;
import java.util.Date;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (ConfigPerceivedTask)表实体类
 *
 * @author makejava
 * @since 2024-05-13 11:51:39
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("config_perceived_task")
public class ConfigPerceivedTask {

	// 状态感知任务编号
	@TableId(type = IdType.AUTO)
	private Long taskId;

	// 结构树节点id
	private String nodeId;

	// 风机项目id
	private String proId;

	// 模型id
	private String modelId;

	// 模型名称
	private String modelName;

	// 时间窗口
	private LocalDateTime timeWindow;

	// 模型类别
	private String modelType;

	// 调用算法id
	private String algoId;

	// 调用算法简称
	private String algoShortname;

	private Long variableNum;

	// 单次模型输入的数据维度
	private Long dataDimension;

	// 服务类型，1：故障报警:2：故障预警，3：加速退化提示
	private String serviceType;

	// 评估方法，1：状态阈值评估；2：服务阶段评估
	private String assessType;

	// 指标要求,1：原始信号；2：单统计学指标；3：多统计学指标
	private String indexRequirement;

	// 评估指标，1：原始残差；2：自编码器残差；3：BiLSTM残差
	private String assessIndex;

	// 阈值类型，1：专家阈值，2：数据阈值
	private String thresholdType;

	// 连续超限次数
	private Integer limitNumber;

	// 预测故障失效判据, 0: 温度残差超过80
	private String failureCriterion;

	// 趋势预测算法, 0: BLSTM
	private String trendPrediction;

	// 预测结果连续超限次数
	private Integer resultLimitNum;

	// 服务简介
	private String intro;

	// 是否是默认模板
	private Integer isDefaultModel;

	// 是否执行中
	private Integer status;

}
