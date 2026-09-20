package sw.model3d.stateAssessment.entity;

import java.time.LocalDateTime;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (DcError)表实体类
 *
 * @author makejava
 * @since 2024-05-29 09:49:12
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("dc_error")
public class DcError {

	// 自增id
	@TableId(type = IdType.AUTO)
	private Integer id;

	// 错误类型
	private String error;

	// 错误详细信息
	private String information;

	// 任务id
	private String taskId;

	// 采集时间
	private LocalDateTime dcTime;

	// 算法名称
	private String algoShortname;

}
