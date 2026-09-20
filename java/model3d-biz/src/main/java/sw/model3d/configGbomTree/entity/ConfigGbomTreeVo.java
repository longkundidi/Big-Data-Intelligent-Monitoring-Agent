package sw.model3d.configGbomTree.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import sw.utils.entity.TreeEntity;

@Data
public class ConfigGbomTreeVo extends TreeEntity {

	@Schema(description = "元结构树更新时间")
	private String updateTime;

	@Schema(description = "元结构树备注说明")
	private String memo;

}
