package sw.model3d.configModel.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (ConfigPerceivedTaskVariable)表实体类
 *
 * @author makejava
 * @since 2024-05-13 11:37:19
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("config_perceived_task_variable")
public class ConfigPerceivedTaskVariable {

	// 自增id
	@TableId(type = IdType.AUTO)
	private Long id;

	// 状态感知任务id
	private Long taskId;

	// 感知变量id
	private String varId;

	/**
	 * 变量名称
	 */
	private String varName;

	/**
	 * 变量类型
	 */
	private String variableType;

	/**
	 * 数据类型
	 */
	private String dataType;

	/**
	 * 数据单位
	 */
	private String dimension;

	/**
	 * 采集频率
	 */
	private Float collectionFrequency;

	/**
	 * 阈值下限
	 */
	private Float max;

	/**
	 * 阈值上限
	 */
	private Float min;

	/**
	 * 采集用途
	 */
	private String purpose;

}
