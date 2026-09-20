package com.algorithm.web.model.entity.al;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 设备基本信息实体类
 */
@Data
@TableName("al_device_base_info")
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "设备基本信息")
public class AlDeviceBaseInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 主键
	 */
	@TableId(type = IdType.AUTO)
	@Schema(description = "主键ID")
	private Long id;

	/**
	 * 场景ID，关联al_resume_data的id
	 */
	@Schema(description = "场景ID")
	private Long sceneId;

	/**
	 * 设备名称
	 */
	@Schema(description = "设备名称")
	private String deviceName;

	/**
	 * 设备编号，对应al_resume_data的product_model字段
	 */
	@Schema(description = "设备编号")
	private String deviceCode;

	/**
	 * 制造厂商
	 */
	@Schema(description = "制造厂商")
	private String manufacturer;

	/**
	 * 制动器类型
	 */
	@Schema(description = "制动器类型")
	private String brakeType;

	/**
	 * 额定载重
	 */
	@Schema(description = "额定载重")
	private String ratedLoad;

	/**
	 * 额定速度
	 */
	@Schema(description = "额定速度")
	private String ratedSpeed;

	/**
	 * 额定功率
	 */
	@Schema(description = "额定功率")
	private String ratedPower;

	/**
	 * 生产日期
	 */
	@Schema(description = "生产日期")
	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
	private Date productionDate;

	/**
	 * 使用单位
	 */
	@Schema(description = "使用单位")
	private String usageUnit;

	/**
	 * 安装日期
	 */
	@Schema(description = "安装日期")
	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
	private Date installDate;

	/**
	 * 管理人员
	 */
	@Schema(description = "管理人员")
	private String manager;

	/**
	 * 创建时间
	 */
	@TableField(fill = FieldFill.INSERT)
	@Schema(description = "创建时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date createTime;

	/**
	 * 更新时间
	 */
	@TableField(fill = FieldFill.INSERT_UPDATE)
	@Schema(description = "更新时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date updateTime;

}
