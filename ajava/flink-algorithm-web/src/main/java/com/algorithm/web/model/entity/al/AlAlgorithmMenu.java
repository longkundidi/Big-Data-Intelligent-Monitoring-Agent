package com.algorithm.web.model.entity.al;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("al_algorithm_menu")
public class AlAlgorithmMenu implements Serializable {

	private static final long serialVersionUID = 1L;

	@TableId(type = IdType.AUTO)
	private Long id;

	private Long parentId;

	private String menuCode;

	private String menuName;

	private String nodeType;

	private String algorithmType;

	private Long algorithmId;

	private Integer sortOrder;

	private Integer enabled;

	private LocalDateTime createTime;

	private LocalDateTime updateTime;

}
