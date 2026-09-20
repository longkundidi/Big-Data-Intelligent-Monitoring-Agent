package com.algorithm.web.model.dto.al;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class AlResumeDataDto implements Serializable {

	/* 项目名称 */
	private String project;

	/* 产品机型 */
	private String productModel;

	private String bomModel;

}
