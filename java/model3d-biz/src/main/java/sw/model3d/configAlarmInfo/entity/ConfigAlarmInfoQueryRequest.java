package sw.model3d.configAlarmInfo.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sw.model3d.common.PageRequest;

@Data
public class ConfigAlarmInfoQueryRequest extends PageRequest {

	private String proId;

}
