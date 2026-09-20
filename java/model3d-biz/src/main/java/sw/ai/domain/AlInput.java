package sw.ai.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("al_input")
public class AlInput implements Serializable {

	private static final long serialVersionUID = 7292915245321829523L;

	/**
	 * 主键
	 */
	@TableId(type = IdType.AUTO)
	private Integer id;

	private String alName;

	private String varId;

	private String alClass;

}
