package sw.model3d.stateAssessment.entity;

import java.time.LocalDateTime;

import java.io.Serializable;
import java.util.Date;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (DcAlgorithm)表实体类
 *
 * @author makejava
 * @since 2024-05-28 17:38:12
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("dc_algorithm")
public class DcAlgorithm {

	// 主键
	@TableId(type = IdType.AUTO)
	private Integer id;

	// 算法结果
	private String dcData;

	// 任务id
	private String taskId;

	// 采集时间
	private String dcTime;

	// 算法名称
	private String algoShortname;

}
