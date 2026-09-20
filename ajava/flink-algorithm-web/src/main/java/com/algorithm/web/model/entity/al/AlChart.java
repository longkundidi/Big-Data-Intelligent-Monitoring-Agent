package com.algorithm.web.model.entity.al;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlChart implements Serializable {

	private static final long serialVersionUID = 337361630075002457L;

	private Long algorithmId;

	private String startTime;

	private String endTime;

	private String monitorCode;

}
