package sw.model3d.configModel.entity.vo;

import lombok.Data;
import sw.utils.entity.TreeEntity;

import java.io.Serializable;
import java.util.List;

@Data
public class ConfigComponentMatchVo implements Serializable {

	private static final long serialVersionUID = -6578844260641983789L;

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
	 * 项目结构树与元结构树的交集列表
	 */
	private List<TreeEntity> bomList;

}
