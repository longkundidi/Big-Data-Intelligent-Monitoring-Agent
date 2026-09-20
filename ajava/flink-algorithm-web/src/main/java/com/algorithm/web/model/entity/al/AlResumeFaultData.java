package com.algorithm.web.model.entity.al;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("al_resume_fault_data")

public class AlResumeFaultData implements Serializable {

	private static final long serialVersionUID = 337361634075102452L;

	@TableId(type = IdType.AUTO)
	private Long id;

	private Long projectId;

	private Long deviceId;

	private String faultTime;

	private String faultName;

}
