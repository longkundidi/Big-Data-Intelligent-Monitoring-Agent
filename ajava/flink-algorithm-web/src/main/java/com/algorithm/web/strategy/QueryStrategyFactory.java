package com.algorithm.web.strategy;

import com.algorithm.web.model.entity.al.AlTask;
import com.algorithm.web.model.entity.al.AlTaskVo;
import com.algorithm.web.service.al.AlStateEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class QueryStrategyFactory {

	private final Map<String, QueryStrategy> strategies = new HashMap<>();

	@Autowired
	public QueryStrategyFactory(AlDataCleanQueryStrategy alDataCleanQueryStrategy,
			AlKnowledgeQueryStrategy alKnowledgeQueryStrategy,
			AlStateEvaluationQueryStrategy alStateEvaluationQueryStrategy,
			AlFaultDiagnosisQueryStrategy alFaultDiagnosisQueryStrategy) {
		strategies.put("alDataCleaning", alDataCleanQueryStrategy);
		strategies.put("alKnowledgeExtraction", alKnowledgeQueryStrategy);
		strategies.put("alStateEvaluation", alStateEvaluationQueryStrategy);
		strategies.put("alFaultDiagnosis", alFaultDiagnosisQueryStrategy);
	}

	public QueryStrategy getStrategy(String alClass) {
		return strategies.getOrDefault(alClass, new QueryStrategy() {
			@Override
			public String getType(AlTaskVo alTask) {
				return "模型-算法审核";
			}

			@Override
			public String getCreator(AlTaskVo alTask) {
				return "张英楠";
			}

			@Override
			public Long getNum(AlTaskVo alTask) {
				return (long) 0;
			}
		});
	}

}
