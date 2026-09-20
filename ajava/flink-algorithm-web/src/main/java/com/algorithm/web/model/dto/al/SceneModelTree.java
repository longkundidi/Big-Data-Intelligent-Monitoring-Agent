package com.algorithm.web.model.dto.al;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SceneModelTree {

	private Long id;

	private String label;

	private String bomModel;

	private List<SceneModelTree> children = new ArrayList<>();

}
