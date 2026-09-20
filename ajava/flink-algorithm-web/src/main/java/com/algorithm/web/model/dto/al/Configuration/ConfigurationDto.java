package com.algorithm.web.model.dto.al.Configuration;

import lombok.Data;

import java.util.List;

@Data
public class ConfigurationDto {

	private String modelName;

	private String modelObject;

	private String objectId;

	private String imageUrl;

	private List<NodeDto> nodes; // 单组态用

	private String dataUrl; // 单组态用

	private String modelType; // 多模型组态用

	private String modelSequence; // 多模型组态用

	private String modelVote; // 多模型组态用

}
