package sw.model3d.configGbomTree.entity;

import lombok.Data;

import java.util.List;

@Data
public class ConfigGbomTreeDTO {

	private String id;

	private String nodeCode;

	private String name;

	private String nodeType;

	private Integer nodeLevel;

	private Boolean leaf;

	private Boolean myshow;

	private String sceneId;

	private String updateTime;

	private String memo;

	// 子节点
	private List<ConfigGbomTreeDTO> children;

}
