package com.algorithm.web.model.dto.al.Configuration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class NodeDataDto {

	private String label; // 节点标签

	private String configUrl;

	private String saveUrl;

	private String shortName;

	private String uniformHeight; // 统一高度

	private String pretreatment;

	private String trainEpoch;

	private String trainBatch;

	private String learningRate;

	private String optimizer;

	private String maxlevel;

	private String sliceLength;

	private String mode;

	private String wavelet;

	private String trainResult;

	private String n;

}