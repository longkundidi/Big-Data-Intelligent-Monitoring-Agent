package sw.model3d.configFailureMode.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sw.model3d.common.PageRequest;

@EqualsAndHashCode(callSuper = true)
@Data
public class ConfigFailureModeQueryRequest extends PageRequest {

	/**
	 * 所属表config_failure_mode的节点id
	 */
	private String nodeId;

}
