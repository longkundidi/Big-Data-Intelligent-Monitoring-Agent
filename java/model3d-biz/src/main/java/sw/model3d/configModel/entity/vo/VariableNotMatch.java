package sw.model3d.configModel.entity.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class VariableNotMatch implements Serializable {

	private static final long serialVersionUID = -8001603995889775126L;

	/**
	 * 零部件编码
	 */
	private String nodeId;

	/**
	 * 零部件的前两级部件
	 */
	private String nodeParent;

	/**
	 * 零部件名称
	 */
	private String nodeName;

	/**
	 * 当前节点与元感知变量相比缺少的感知变量
	 */
	private List<String> variables;

}
