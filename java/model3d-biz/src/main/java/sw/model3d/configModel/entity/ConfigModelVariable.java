package sw.model3d.configModel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @TableName config_model_variable
 */
@Data
@TableName(value = "config_model_variable")
public class ConfigModelVariable implements Serializable {

	/**
	 * 关联表ID
	 */
	@TableId(type = IdType.ASSIGN_ID)
	private String id;

	/**
	 * 感知变量ID
	 */
	private String varId;

	/**
	 * 模型ID
	 */
	private String modelId;

	@TableField(exist = false)
	private static final long serialVersionUID = 1L;

}