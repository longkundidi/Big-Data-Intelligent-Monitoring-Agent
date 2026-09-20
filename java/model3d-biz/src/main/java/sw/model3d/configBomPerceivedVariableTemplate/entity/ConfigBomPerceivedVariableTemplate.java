package sw.model3d.configBomPerceivedVariableTemplate.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 项目结构树节点对应的感知变量信息(ConfigBomPerceivedVariableTemplate)表实体类
 *
 * @author makejava
 * @since 2024-06-27 18:37:00
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("config_bom_perceived_variable_template")
public class ConfigBomPerceivedVariableTemplate implements Serializable {

	// 主键
	@TableId(type = IdType.ASSIGN_ID)
	private String id;

	// 变量Id
	private String varId;

	// 变量名称
	private String varName;

	// 模板变量是否被启用（1启用0不启用）
	private String usable;

	private Long proId;

	// 变量类型
	private String variableType;

	// 数据类型
	private String dataType;

	// 数据单位
	private String dimension;

	// 采集频率
	private Float collectionFrequency;

	// 阈值下限
	private Float max;

	// 阈值上限
	private Float min;

	// 采集用途
	private String purpose;

	// 变量所属的零部件id
	private String nodeId;

}
