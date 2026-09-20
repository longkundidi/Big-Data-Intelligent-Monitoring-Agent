package sw.model3d.configModel.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sw.model3d.common.PageRequest;

@EqualsAndHashCode(callSuper = true)
@Data
public class ConfigModelPageQueryRequestByModelId extends PageRequest {

	/**
	 * 模型id
	 */
	private String modelId;

}
