package sw.model3d.configAlarmInfo.entity.vo;

import cn.hutool.core.date.DateTime;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConfigAlarmInfoVo {

	private Long taskId;

	/**
	 * 风机名称
	 */

	private String turbineName;

	/**
	 * 风机编码
	 */

	private String turbineCode;

	/**
	 * 零部件名称
	 */

	private String nodeName;

	private String nodeId;

	private String nodeCode;

	private String modelName;

	private String modelShortName;

	private LocalDateTime dcTime;

}
