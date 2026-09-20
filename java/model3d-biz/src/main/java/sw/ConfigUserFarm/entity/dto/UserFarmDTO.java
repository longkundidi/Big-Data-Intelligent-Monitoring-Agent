package sw.ConfigUserFarm.entity.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserFarmDTO {

	private Long userId;

	private List<String> farmNames;

	private Long metaModelId;

	private String metaModelName;

}
