package sw.AlSceneData.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 场景信息
 *
 * @author qxq
 * @date 2025-05-19 15:04:49
 */
@Data
@TableName("al_scene_data")
@Schema(description = "场景信息")
public class AlSceneData implements Serializable {

	private static final long serialVersionUID = 4631847345503992180L;

	/**
	 * 主键
	 */
	@TableId(type = IdType.ASSIGN_ID)
	@Schema(description = "主键")
	private Long id;

	/**
	 * 场景名称
	 */
	@Schema(description = "场景名称")
	private String sceneName;

	/**
	 * 描述
	 */
	@Schema(description = "描述")
	private String description;

	/**
	 * 图标地址
	 */
	@Schema(description = "图标地址")
	private String iconUrl;

	/**
	 * BOM信息
	 */
	@Schema(description = "BOM信息")
	private String bomModel;

}
