package sw.model3d.configModel.entity.vo;

import lombok.Data;
import sw.model3d.ConfigBomPerceivedVariable.entity.ConfigBomPerceivedVariable;

import java.util.List;

@Data
public class ConfigPerceivedTaskSaveRequest {

	private Long taskId;

	private String nodeId;

	private String modelId;

	private String modelName;

	private String algoId;

	private Long dataDimension;

	private List<ConfigBomPerceivedVariable> variables;

}
