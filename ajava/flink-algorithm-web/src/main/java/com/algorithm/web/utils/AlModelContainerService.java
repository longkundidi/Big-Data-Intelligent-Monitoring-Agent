package com.algorithm.web.utils;

import java.util.HashMap;
import java.util.Map;

public class AlModelContainerService {

	private static final Map<String, String> SERVICEMAP = new HashMap<>();

	static {
		SERVICEMAP.put("deduplication_adjacent", "deduplication_adjacent");
		SERVICEMAP.put("deduplication_hash", "deduplication_hash");
		SERVICEMAP.put("median-filling", "median-filling");
		SERVICEMAP.put("mode-filling", "mode-filling");
		SERVICEMAP.put("knn-filling", "knn-filling");
		SERVICEMAP.put("regression-filling", "regression-filling");
		SERVICEMAP.put("min-max-scaling", "min-max-scaling");
		SERVICEMAP.put("z-score-normalization", "z-score-normalization");
		SERVICEMAP.put("decimal-scaling-normalization", "decimal-scaling-normalization");
		SERVICEMAP.put("Bert-Bilstm-CRF-Doccan", "model_bert_g");
		SERVICEMAP.put("CAE", "cae-for-hob");
		SERVICEMAP.put("MultiFeatureIndexFusionAE", "featurefusionae-based-for-hob");
		SERVICEMAP.put("LSTMAE", "lstm-ae");
		SERVICEMAP.put("algorithm-fault-diagnosis-test", "fault_diagnosis_test");
		SERVICEMAP.put("REGTCN", "elevator-anomaly-monitoring");
		SERVICEMAP.put("FFCNet", "elevator-fault-diagnosis");
		SERVICEMAP.put("cnn-based-for-ballbearing", "cnn-based-for-ballbearing");
		SERVICEMAP.put("wdcnn-based-for-ballbearing", "wdcnn-based-for-ballbearing");
		SERVICEMAP.put("Resnet18", "resnet18");
		SERVICEMAP.put("flink", "flink-jobmanager");
	}

	public static String getAlModelContainerName(String algorithmName) {
		return SERVICEMAP.get(algorithmName);
	}

}
