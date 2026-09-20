package sw.model3d.configBomTreeTemplate.entity.vo;

import lombok.Data;
import sw.model3d.configGbomTree.entity.ConfigGbomTreeDTO;

@Data
public class UploadDataSetVO {

	private ConfigGbomTreeDTO node;

	private String datasetType;

	private String datasetUrl;

	private String provider;

	private String datasetName;

}
