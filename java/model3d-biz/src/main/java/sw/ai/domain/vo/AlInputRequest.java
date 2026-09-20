package sw.ai.domain.vo;

import lombok.Data;
import sw.model3d.configPerceivedVariable.entity.ConfigPerceivedVariable;

import java.util.List;

@Data
public class AlInputRequest {

	private String alName;

	private String alClass;

	private List<Object> inputVariables;

}