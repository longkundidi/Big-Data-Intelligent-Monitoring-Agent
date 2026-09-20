package com.algorithm.web.model.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AlgorithmMenuTreeNode {

	private Long id;

	private String code;

	private String name;

	private String nodeType;

	private String algorithmType;

	private Long algorithmId;

	private Integer sortOrder;

	private Object algorithm;

	private List<AlgorithmMenuTreeNode> children = new ArrayList<>();

}
