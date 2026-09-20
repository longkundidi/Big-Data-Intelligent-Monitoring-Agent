package sw.model3d.configPerceivedVariable.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sw.model3d.common.PageRequest;

@EqualsAndHashCode(callSuper = true)
@Data
public class ConfigPerceivedVariableQueryRequest extends PageRequest {

	/**
	 * 所属表config_failure_mode的节点id
	 */
	private String nodeId;

	/**
	 * 所属节点类型，是GBOM还是SBOM，分别从两张表取感知变量数据
	 */
	private String nodeType;

}
