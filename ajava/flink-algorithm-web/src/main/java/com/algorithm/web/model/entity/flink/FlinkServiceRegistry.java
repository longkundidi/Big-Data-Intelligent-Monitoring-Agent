package com.algorithm.web.model.entity.flink;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("flink_service_registry")
public class FlinkServiceRegistry {

	@TableId(type = IdType.AUTO)
	private Long id;

	private String serviceKey;

	private String displayName;

	private String componentType;

	private String sourceType;

	private String matchType;

	private String matchValue;

	private String description;

	private String inputName;

	private String outputName;

	private Integer expectedInstances;

	private Integer visible;

	private Integer sortOrder;

}
