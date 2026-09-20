package com.algorithm.web.utils;

import java.util.HashMap;
import java.util.Map;

public class TaskMsgServices {

	private static final Map<String, String> SERVICEMAP = new HashMap<>();

	static {
		SERVICEMAP.put("algorithm-test", "TestBuildTaskMsgService");
		SERVICEMAP.put("algorithm-fault-diagnosis-test", "TestFaultDiagnosisBuildTaskMsgService");
		SERVICEMAP.put("REGTCN", "ElevatorAnomalyMonitoringService");
		SERVICEMAP.put("FFCNet", "ElevatorFaultDiagnosisService");
		SERVICEMAP.put("deduplication_adjacent", "DeduplicationAdjacentService");
		SERVICEMAP.put("deduplication_hash", "DeduplicationHashService");
		SERVICEMAP.put("median-filling", "MedianFillingService");
		SERVICEMAP.put("mode-filling", "ModeFillingService");
		SERVICEMAP.put("knn-filling", "KNNFillingService");
		SERVICEMAP.put("regression-filling", "RegressionFillingService");
		SERVICEMAP.put("min-max-scaling", "MinMaxScalingService");
		SERVICEMAP.put("z-score-normalization", "ZScoreNormalizationService");
		SERVICEMAP.put("decimal-scaling-normalization", "DecimalScalingNormalizationService");
		SERVICEMAP.put("fp-growth", "FPGrowthService");
		SERVICEMAP.put("apriori", "AprioriService");
		SERVICEMAP.put("CAE", "CAEForHobService");
		SERVICEMAP.put("LSTMAE", "LSTMAEService");
		SERVICEMAP.put("LSTM", "LSTMService");
		SERVICEMAP.put("GRUAE", "GRUAEService");
		SERVICEMAP.put("GRU", "GRUService");
		SERVICEMAP.put("Resnet18", "Resnet18Service");
		SERVICEMAP.put("Resnet18Test", "Resnet18TestService");
		SERVICEMAP.put("wdcnn_based_for_bear", "WDCNNBASEDFORBEARService");
		SERVICEMAP.put("WTConv", "WTConvService");
		SERVICEMAP.put("swtbigru-based-for-ballbearing", "SWTBiGRUBasedForBallbearingService");
		SERVICEMAP.put("swtbilstm-based-for-ballbearing", "SWTBiLSTMBasedForBallbearingService");
		SERVICEMAP.put("swtcnn-based-for-ballbearing", "SWTCNNBasedForBallbearingService");
		SERVICEMAP.put("swtdgru-based-for-ballbearing", "SWTDGRUBasedForBallbearingService");
		SERVICEMAP.put("swtgru-based-for-ballbearing", "SWTGRUBasedForBallbearingService");
		SERVICEMAP.put("swtbigru-based-for-millingtool", "SWTBiGRUBasedForMillingtoolService");
		SERVICEMAP.put("swtbilstm-based-for-millingtool", "SWTBiLSTMBasedForMillingtoolService");
		SERVICEMAP.put("MultiFeatureIndexFusionAE", "FeatureFusionAEBasedForService");
		SERVICEMAP.put("rms-based-for-windpower", "RMSBasedForWindpowerService");
		SERVICEMAP.put("rmsbilstm-based-for-hob", "RMSBiLSTMBasedForHobService");
		SERVICEMAP.put("rmsbilstm-based-for-windpower", "RMSBiLSTMBasedForWindpowerService");
		SERVICEMAP.put("rmsgru-based-for-windpower", "RMSGRUBasedForWindpowerService");
		SERVICEMAP.put("rmsgru-based-for-ballbearing", "RMSGRUBasedForBallbearingService");
		SERVICEMAP.put("rmsgru", "RMSGRUBasedForHobService");
		SERVICEMAP.put("cosine-based-for-ballbearing", "CosineBasedForBallbearingService");
		SERVICEMAP.put("pearson-based-for-ballbearing", "PearsonBasedForBallbearingService");
		SERVICEMAP.put("rms-based-for-ballbearing", "RMSBasedForBallbearingService");
		SERVICEMAP.put("cnn-based-for-ballbearing", "CNNBasedForBallbearingService");
		SERVICEMAP.put("wdcnn-based-for-ballbearing", "WDCNNBasedForBallbearingService");
		SERVICEMAP.put("autoTest", "AutoTestService");
		SERVICEMAP.put("autoGetMetrics", "AutoGetMetricsService");
		SERVICEMAP.put("onlineTrain", "OnlineTrainService");
		SERVICEMAP.put("TFDExtract", "TFDExtractService");
		SERVICEMAP.put("signalAnalysis", "SignalAnalysisService");
		SERVICEMAP.put("config-median-filling", "ConfigMedianFillingService");
		SERVICEMAP.put("config-knn-filling", "ConfigKNNFillingService");
		SERVICEMAP.put("config-WavePacket", "ConfigWavePacketService");
		SERVICEMAP.put("config-LSTMAE", "ConfigLSTMAEService");
		SERVICEMAP.put("config-LSTM", "ConfigLSTMService");
		SERVICEMAP.put("config-GRUAE", "ConfigGRUAEService");
		SERVICEMAP.put("config-GRU", "ConfigGRUService");
		SERVICEMAP.put("config-Resnet18", "ConfigResnet18Service");
		SERVICEMAP.put("config-WTConv", "ConfigWTConvService");
		SERVICEMAP.put("config-wdcnn-based-for-ballbearing", "ConfigWDCNNBasedForBallbearingService");
		SERVICEMAP.put("pearson_correlation", "PearsonCorrelationService");
		SERVICEMAP.put("config-pearson_correlation", "ConfigPearsonCorrelationService");
		SERVICEMAP.put("train-median-filling", "ConfigMedianFillingService");
		SERVICEMAP.put("train-WavePacket", "TrainWavePacketService");
		SERVICEMAP.put("train-LSTMAE", "TrainLSTMAEService");
		SERVICEMAP.put("train-LSTM", "TrainLSTMService");
		SERVICEMAP.put("train-GRUAE", "TrainGRUAEService");
		SERVICEMAP.put("train-GRU", "TrainGRUService");
		SERVICEMAP.put("train-Resnet18", "TrainResnet18Service");
		SERVICEMAP.put("train-WTConv", "TrainWTConvService");
		SERVICEMAP.put("train-wdcnn-based-for-ballbearing", "TrainWDCNNBasedForBallbearingService");
		SERVICEMAP.put("train-knn-filling", "ConfigKNNFillingService");
	}

	public static String getTaskMsgService(String algorithmName) {
		return SERVICEMAP.get(algorithmName);
	}

}
