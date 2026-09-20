package sw.model3d.configModel.entity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ConfigPerceivedVariableVo {

	/**
	 * 变量Id
	 */
	@Schema(description = "变量Id")
	private String varId;

	/**
	 * 变量名称
	 */
	@Schema(description = "变量名称")
	private String varName;

	/**
	 * 变量类型
	 */
	@Schema(description = "变量类型")
	private String variableType;

	/**
	 * 数据类型
	 */
	@Schema(description = "数据类型")
	private String dataType;

	/**
	 * 数据单位
	 */
	@Schema(description = "数据单位")
	private String dimension;

	/**
	 * 采集频率
	 */
	@Schema(description = "采集频率")
	private Float collectionFrequency;

	/**
	 * 阈值下限
	 */
	@Schema(description = "阈值下限")
	private Float max;

	/**
	 * 阈值上限
	 */
	@Schema(description = "阈值上限")
	private Float min;

	/**
	 * 采集用途
	 */
	@Schema(description = "采集用途")
	private String purpose;

	/**
	 * 所属表config_failure_mode的节点id
	 */
	@Schema(description = "所属表config_failure_mode的节点id")
	private String nodeId;

}
