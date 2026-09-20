package sw.ai.model.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class GroupedConfigDTO {

	private String code;

	private String modelName;

	private String modelObject;

	private String alModelIcon;

	private Integer isService;

	private Integer isDeployed;

	private List<TaskProcessDTO> taskProcess;

	@Schema(description = "算法名称")
	@TableField(exist = false)
	private String label;

	@Schema(description = "算法code")
	@TableField(exist = false)
	private String value;

	@Data
	public static class TaskProcessDTO {

		private String modelName;

		private Integer sequence;

	}

}