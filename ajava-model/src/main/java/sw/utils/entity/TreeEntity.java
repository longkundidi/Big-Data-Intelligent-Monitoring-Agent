package sw.utils.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TreeEntity {

	private static final long serialVersionUID = 1L;

	@Schema(description = "节点Id") // 实际保存的是节点编码
	private String id;

	@Schema(description = "节点编码")
	private String nodeCode;

	@Schema(description = "节点名称")
	private String name;

	@Schema(description = "节点类型")
	private String nodeType;

	@Schema(description = "节点层级(1-n)")
	private Integer nodeLevel;

	private boolean leaf = false; // 默认都是叶子节点

	private boolean myshow = false; // 默认不显示节点操作按钮

	// 节点添加后，更新父节点的nodeType属性
	public static String setParentNodeTypeAfterAdd(String pNodeType) {
		String nodeType = null;
		if (!(pNodeType == null || pNodeType.equals(""))) {
			if (pNodeType.equals("Root-Leaf"))
				nodeType = "Root";
			else if (pNodeType.equals("Leaf"))
				nodeType = "Mid";
		}
		return nodeType;
	}

}
