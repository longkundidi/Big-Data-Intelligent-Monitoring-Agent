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
 * @TableName al_fault_diagnosis
 */
@TableName(value = "al_fault_diagnosis")
@Data
public class AlFaultDiagnosis implements Serializable {

	/**
	 * 主键
	 */
	@TableId(type = IdType.AUTO)
	private String id;

	/**
	 * 模型名称
	 */
	private String modelName;

	/**
	 * 模型类型（深度学习、传统机器学习与统计学、信号处理）
	 */
	private String modelType;

	/**
	 * 提供者
	 */
	private String modelProvider;

	/**
	 * 针对对象
	 */
	private String modelObject;

	/**
	 * 模型支持库
	 */
	private String modelLibrary;

	/**
	 * 模型图标地址
	 */
	private String modelIcon;

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
	 * 模型程序包路径（↑模型基本信息配置 ↓模型部署与测试）
	 */
	private String programUrl;

	/**
	 * 测试数据（部署）
	 */
	private String deployInput;

	/**
	 * 输出数据（部署）
	 */
	private String deployOutput;

	/**
	 * 部署要求
	 */
	private String deployRequire;

	/**
	 * 调用方式（↑模型部署与测试 ↓部署测试后填写）
	 */
	private String modelInvoke;

	/**
	 * 是否审核（0是1否）
	 */
	private Long isCheck;

	/**
	 * 是否通过测试（0是1已测试未通过2未测试）
	 */
	private Long isPass;

	/**
	 * 是否部署（0是1否）
	 */
	private Long isDeployed;

	/**
	 * 0代表输入是json模式，接口/altestmodel；1反之
	 */
	private Long isjson;

	/**
	 * 算法简称,只允许字母数字和-
	 */
	private String modelShortName;

	/**
	 * 调用路径
	 */
	private String modelUrl;

	/**
	 * 模型输入
	 */
	private String input;

	/**
	 * 模型输出
	 */
	private String output;

	/**
	 * 被引用次数
	 */
	private Long modelNum;

	/**
	 * 【暂时无用】运维服务
	 */
	private String modelFunction;

	/**
	 * 【废弃】测点的用户编码
	 */
	private String userCode;

	/**
	 * 【废弃】领域模型类型（元数据处理算法...)
	 */
	private String modelTypeFirst;

	/**
	 * 【废弃】原存在登录逻辑获得身份
	 */
	private String creator;

	/**
	 * 【废弃】原存在登录逻辑获得身份
	 */
	private String editor;

	@Schema(description = "算法名称")
	@TableField(exist = false)
	private String label;

	@Schema(description = "算法Id")
	@TableField(exist = false)
	private String value;

	@TableField(exist = false)
	private static final long serialVersionUID = 1L;

}