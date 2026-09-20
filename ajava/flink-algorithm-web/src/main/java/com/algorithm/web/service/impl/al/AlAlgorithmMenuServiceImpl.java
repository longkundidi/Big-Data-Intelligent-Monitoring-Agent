package com.algorithm.web.service.impl.al;

import com.algorithm.web.mapper.al.AlAlgorithmMenuMapper;
import com.algorithm.web.model.entity.al.AlAlgorithmMenu;
import com.algorithm.web.model.entity.al.AlFaultDiagnosis;
import com.algorithm.web.model.entity.al.AlStateEvaluation;
import com.algorithm.web.model.vo.AlgorithmMenuTreeNode;
import com.algorithm.web.service.al.AlAlgorithmMenuService;
import com.algorithm.web.service.al.AlFaultDiagnosisService;
import com.algorithm.web.service.al.AlStateEvaluationService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlAlgorithmMenuServiceImpl extends ServiceImpl<AlAlgorithmMenuMapper, AlAlgorithmMenu>
		implements AlAlgorithmMenuService {

	private static final String NODE_TYPE_CATEGORY = "category";

	private static final String NODE_TYPE_ALGORITHM = "algorithm";

	private static final String TYPE_EVALUATION = "evaluation";

	private static final String TYPE_DIAGNOSIS = "diagnosis";

	private final AlStateEvaluationService alStateEvaluationService;

	private final AlFaultDiagnosisService alFaultDiagnosisService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public List<AlgorithmMenuTreeNode> getTree() {
		List<AlAlgorithmMenu> categories = list(Wrappers.<AlAlgorithmMenu>lambdaQuery()
			.eq(AlAlgorithmMenu::getParentId, 0L)
			.eq(AlAlgorithmMenu::getNodeType, NODE_TYPE_CATEGORY)
			.eq(AlAlgorithmMenu::getEnabled, 1)
			.in(AlAlgorithmMenu::getAlgorithmType, TYPE_EVALUATION, TYPE_DIAGNOSIS)
			.orderByAsc(AlAlgorithmMenu::getSortOrder, AlAlgorithmMenu::getId));

		List<AlgorithmMenuTreeNode> tree = new ArrayList<>();
		for (AlAlgorithmMenu category : categories) {
			AlgorithmMenuTreeNode categoryNode = toTreeNode(category, null);
			if (TYPE_EVALUATION.equals(category.getAlgorithmType())) {
				List<AlStateEvaluation> algorithms = alStateEvaluationService.list()
					.stream()
					.filter(algorithm -> algorithm.getIsService() == null || algorithm.getIsService() == 0)
					.sorted(Comparator.comparing(AlStateEvaluation::getId))
					.collect(Collectors.toList());
				categoryNode.setChildren(synchronizeChildren(category, algorithms, AlStateEvaluation::getId,
						AlStateEvaluation::getModelName));
			}
			else if (TYPE_DIAGNOSIS.equals(category.getAlgorithmType())) {
				List<AlFaultDiagnosis> algorithms = alFaultDiagnosisService.list()
					.stream()
					.filter(algorithm -> algorithm.getIsService() == null || algorithm.getIsService() == 0)
					.sorted(Comparator.comparing(AlFaultDiagnosis::getId))
					.collect(Collectors.toList());
				categoryNode.setChildren(synchronizeChildren(category, algorithms, AlFaultDiagnosis::getId,
						AlFaultDiagnosis::getModelName));
			}
			tree.add(categoryNode);
		}
		return tree;
	}

	private <T> List<AlgorithmMenuTreeNode> synchronizeChildren(AlAlgorithmMenu category, List<T> algorithms,
			Function<T, Long> idExtractor, Function<T, String> nameExtractor) {
		List<AlAlgorithmMenu> existingRows = list(Wrappers.<AlAlgorithmMenu>lambdaQuery()
			.eq(AlAlgorithmMenu::getParentId, category.getId())
			.eq(AlAlgorithmMenu::getNodeType, NODE_TYPE_ALGORITHM)
			.eq(AlAlgorithmMenu::getAlgorithmType, category.getAlgorithmType()));

		Map<Long, AlAlgorithmMenu> existingByAlgorithmId = existingRows.stream()
			.collect(Collectors.toMap(AlAlgorithmMenu::getAlgorithmId, Function.identity(), (left, right) -> left,
					LinkedHashMap::new));
		List<AlgorithmMenuTreeNode> children = new ArrayList<>();

		for (int index = 0; index < algorithms.size(); index++) {
			T algorithm = algorithms.get(index);
			Long algorithmId = idExtractor.apply(algorithm);
			String algorithmName = nameExtractor.apply(algorithm);
			AlAlgorithmMenu menu = existingByAlgorithmId.remove(algorithmId);

			if (menu == null) {
				menu = new AlAlgorithmMenu();
				menu.setParentId(category.getId());
				menu.setMenuCode(category.getAlgorithmType() + "-" + algorithmId);
				menu.setMenuName(algorithmName);
				menu.setNodeType(NODE_TYPE_ALGORITHM);
				menu.setAlgorithmType(category.getAlgorithmType());
				menu.setAlgorithmId(algorithmId);
				menu.setSortOrder((index + 1) * 10);
				menu.setEnabled(1);
				save(menu);
			}
			else if (!Objects.equals(menu.getMenuName(), algorithmName)
					|| !Objects.equals(menu.getParentId(), category.getId())) {
				menu.setMenuName(algorithmName);
				menu.setParentId(category.getId());
				updateById(menu);
			}

			if (Integer.valueOf(1).equals(menu.getEnabled())) {
				children.add(toTreeNode(menu, algorithm));
			}
		}

		if (!existingByAlgorithmId.isEmpty()) {
			removeByIds(
					existingByAlgorithmId.values().stream().map(AlAlgorithmMenu::getId).collect(Collectors.toList()));
		}

		children
			.sort(Comparator.comparing(AlgorithmMenuTreeNode::getSortOrder, Comparator.nullsLast(Integer::compareTo))
				.thenComparing(AlgorithmMenuTreeNode::getId));
		return children;
	}

	private AlgorithmMenuTreeNode toTreeNode(AlAlgorithmMenu menu, Object algorithm) {
		AlgorithmMenuTreeNode node = new AlgorithmMenuTreeNode();
		node.setId(menu.getId());
		node.setCode(menu.getMenuCode());
		node.setName(menu.getMenuName());
		node.setNodeType(menu.getNodeType());
		node.setAlgorithmType(menu.getAlgorithmType());
		node.setAlgorithmId(menu.getAlgorithmId());
		node.setSortOrder(menu.getSortOrder());
		node.setAlgorithm(algorithm);
		return node;
	}

}
