package sw.ai.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @TableName al_state_evaluation
 */
@TableName(value = "al_state_evaluation")
@Data
public class AlStateEvaluation implements Serializable {

	/**
	 * 主键
	 */
	@TableId(type = IdType.AUTO)
	private String id;

	/**
	 * 领域模型类型（元数据处理算法...)
	 */
	private String modelTypeFirst;

	/**
	 * 模型名称
	 */
	private String modelName;

	/**
	 * 算法简称,只允许字母数字和-
	 */
	private String modelShortName;

	/**
	 * 调用地址
	 */
	private String modelUrl;

	/**
	 * 模型图标地址
	 */
	private String modelIcon;

	/**
	 * 提供者
	 */
	private String modelProvider;

	/**
	 * 针对对象
	 */
	private String modelObject;

	/**
	 * 算法类型（深度学习、传统机器学习与统计学、信号处理）
	 */
	private String modelType;

	/**
	 * 调用机制
	 */
	private String modelInvoke;

	/**
	 * 运维服务
	 */
	private String modelFunction;

	/**
	 * 算法支持库
	 */
	private String modelLibrary;

	/**
	 * 适用场景
	 */
	private String modelCondition;

	/**
	 * 模型优点
	 */
	private String modelAdvantage;

	/**
	 * 模型缺点
	 */
	private String modelDisadvantage;

	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 修改时间
	 */
	private Date editTime;

	/**
	 *
	 */
	private String creator;

	/**
	 *
	 */
	private String editor;

	/**
	 * 是否审核（0是1否）
	 */
	private String isCheck;

	/**
	 * 被引用次数
	 */
	private String modelNum;

	/**
	 * 模型输入
	 */
	private String input;

	/**
	 * 模型输出
	 */
	private String output;

	/**
	 * 0代表输入是json模式，接口/altestmodel；1反之
	 */
	private int isjson;

	/**
	 * 测点用户编码
	 */
	private String userCode;

	@Schema(description = "算法名称")
	@TableField(exist = false)
	private String label;

	@Schema(description = "算法Id")
	@TableField(exist = false)
	private String value;

	@TableField(exist = false)
	private static final long serialVersionUID = 1L;

}