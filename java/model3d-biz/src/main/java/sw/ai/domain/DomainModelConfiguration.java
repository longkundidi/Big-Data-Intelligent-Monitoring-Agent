package sw.ai.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("domain_model_configuration")
@Schema(description = "多模型组态信息")
public class DomainModelConfiguration implements Serializable {

	private static final long serialVersionUID = 2879076459619087618L;

	/**
	 * 主键
	 */
	@TableId(type = IdType.AUTO)
	@Schema(description = "主键")
	private Long id;

	/**
	 * 编码
	 */
	@Schema(description = "编码")
	private String code;

	/**
	 * 组态名称
	 */
	@Schema(description = "组态名称")
	private String modelName;

	/**
	 * 组态类型
	 */
	@Schema(description = "组态类型")
	private String almodelType;

	/**
	 * 针对对象
	 */
	@Schema(description = "针对对象")
	private String modelObject;

	/**
	 * 算法所针对的部套件GBom节点ID
	 */
	@Schema(description = "算法所针对的部套件GBom节点ID")
	private String objectId;

	/**
	 * 多组态流程
	 */
	@Schema(description = "多组态流程")
	private String sequence;

	/**
	 * 创建时间
	 */
	@Schema(description = "创建时间")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;

	/**
	 * 是否通过测试（0是1已测试未通过2未测试）
	 */
	@Schema(description = "是否通过测试（0是1已测试未通过2未测试）")
	private Long isPass;

	@Schema(description = "是否部署（0是1否)")
	private Long isDeployed;

	/**
	 * 被引用次数
	 */
	@Schema(description = "被引用次数")
	private Long modelNum;

	/**
	 * 是否对外服务（0是1否)
	 */
	@Schema(description = "是否对外服务（0是1否)")
	private Integer isService;

	/**
	 * 组态流程图
	 */
	@Schema(description = "组态流程图")
	private String almodelIcon;

	/**
	 * 投票规则
	 */
	@Schema(description = "投票规则")
	private String vote;

}
