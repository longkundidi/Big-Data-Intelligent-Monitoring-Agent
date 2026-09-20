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
public class AlSceneModel implements Serializable {

	private static final long serialVersionUID = 2931847845503994180L;

	/**
	 * 主键
	 */
	@TableId(type = IdType.ASSIGN_ID)
	@Schema(description = "主键")
	private Long id;

	/**
	 * 场景主键
	 */
	@Schema(description = "场景表主键")
	private Long sceneId;

	/**
	 * 型号
	 */
	@Schema(description = "型号")
	private String productModel;

}
