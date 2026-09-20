package sw.model3d.configModel.entity.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ConfigVariableMatchVo implements Serializable {

	private static final long serialVersionUID = 4653816532115216153L;

	/**
	 * 风机编码
	 */
	private String turbineCode;

	/**
	 * 风机名
	 */
	private String turbineName;

	/**
	 * 风机结构树是否通过匹配
	 */
	private Integer isBomPassed;

	/**
	 * 风机感知变量是否通过匹配
	 */
	private Integer isValPassed;

	/**
	 * 当前风机缺少的感知变量
	 */
	private List<VariableNotMatch> variableNotMatches;

}
