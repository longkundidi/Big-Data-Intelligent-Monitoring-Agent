package com.algorithm.web.model.entity.al;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 存储系统的评价指标(SysMetrics)表实体类
 *
 * @author makejava
 * @since 2024-11-06 15:28:33
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_metrics")
public class SysMetrics {

	// 自增id
	@TableId
	private Integer id;

	// 名称
	private String name;

	// 所属类型
	private String alType;

}
