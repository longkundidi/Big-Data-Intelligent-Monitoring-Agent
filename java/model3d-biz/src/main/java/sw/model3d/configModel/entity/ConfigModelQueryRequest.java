package sw.model3d.configModel.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sw.model3d.common.PageRequest;

@Data
public class ConfigModelQueryRequest {

	/**
	 * 所属表config_failure_mode的节点id
	 */
	private String nodeId;

	/**
	 * 查询的类型 0-状态感知模型 1-故障诊断模型
	 */
	private String modelType;

}
