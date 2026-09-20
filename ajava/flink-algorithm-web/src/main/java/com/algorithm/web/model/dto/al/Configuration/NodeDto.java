package com.algorithm.web.model.dto.al.Configuration;

import lombok.Data;

import java.util.List;

@Data
public class NodeDto {

	private String id; // 节点 ID

	private String type; // 节点类型（parent/child）

	private Boolean isPretreatment; // 是否为预处理算法

	private NodeDataDto data; // 节点数据

	private NodePositionDto position; // 节点位置

	private String parent; // 父节点 ID（仅对 child 类型有效）

	private Boolean draggable; // 是否可拖动

	private List<NodeDto> children;

}