package com.algorithm.web.model.entity.al;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("resource_usage")
public class ResourceUsage implements Serializable {

	private static final long serialVersionUID = -1924753867394300929L;

	/**
	 * 主键
	 */
	@TableId(type = IdType.AUTO)
	private Long id;

	private String containerId;

	private String containerName;

	private Long cpuUsageTotal;

	private Long systemCpuUsage;

	private Long memoryUsage;

	private Long memoryLimit;

	@TableField(value = "created_time", fill = FieldFill.UPDATE)
	private LocalDateTime createdTime;

}
