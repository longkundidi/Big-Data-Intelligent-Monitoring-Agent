package sw.model3d.stateAssessment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * (DcAlarm)表实体类
 *
 * @author makejava
 * @since 2026-04-22 18:10:00
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("dc_alarm")
public class DcAlarm {

	// 主键
	@TableId(type = IdType.AUTO)
	private Long id;

	// 算法结果
	private String dcData;

	// 任务id
	private String taskId;

	// 采集时间
	private LocalDateTime dcTime;

	// 算法名称
	private String algoShortname;

	// 是否报警(1是0否)
	private String anomalyFlag;

}
