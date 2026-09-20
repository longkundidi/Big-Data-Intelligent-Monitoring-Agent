package sw.model3d.configFailureHistory.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sw.model3d.common.PageRequest;

@EqualsAndHashCode(callSuper = true)
@Data
public class ConfigFailureHistoryQueryRequest extends PageRequest {

	/**
	 * 所属表config_gbom_tree的节点id
	 */
	private String nodeId;

	/**
	 * 场景id，如：1、23
	 */
	private String sceneId;

	/**
	 * 设备名称，如：电梯#1
	 */
	private String deviceName;

}