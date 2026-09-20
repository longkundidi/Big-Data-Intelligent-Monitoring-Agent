package com.algorithm.web.service.al;

import com.algorithm.web.model.entity.al.AlAlgorithmMenu;
import com.algorithm.web.model.vo.AlgorithmMenuTreeNode;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface AlAlgorithmMenuService extends IService<AlAlgorithmMenu> {

	List<AlgorithmMenuTreeNode> getTree();

}
