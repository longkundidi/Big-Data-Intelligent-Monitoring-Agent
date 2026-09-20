package sw.model3d.modelBaseTree.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import sw.utils.entity.TreeEntity;

@Data
public class M3ModelBaseTreeVo extends TreeEntity {

	@Schema(description = "模型文件的缩略图地址")
	private String urlImg;

	@Schema(description = "模型文件在服务器（Nginx、fastDFS）上存储的路径、文件名")
	private String modelFileurl;

	@Schema(description = "模型文件大小(字节数)")
	private Integer modelSize;

	@Schema(description = "模型文件类型，glb、obj")
	private String modelType;

	@Schema(description = "xCoordinate")
	private Double xcoordinate;

	@Schema(description = "yCoordinate")
	private Double ycoordinate;

	@Schema(description = "zCoordinate")
	private Double zcoordinate;

	@Schema(description = "xRotationAngle")
	private Double xrotationAngle;

	@Schema(description = "yRotationAngle")
	private Double yrotationAngle;

	@Schema(description = "zRotationAngle")
	private Double zrotationAngle;

	@Schema(description = "更新时间")
	private String updateTime;

	@Schema(description = "模型备注说明")
	private String memo;

}
