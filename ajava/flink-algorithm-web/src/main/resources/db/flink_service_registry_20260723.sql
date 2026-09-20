CREATE TABLE IF NOT EXISTS `flink_service_registry` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `service_key` varchar(64) NOT NULL,
  `display_name` varchar(128) NOT NULL,
  `component_type` varchar(32) NOT NULL,
  `source_type` varchar(32) NOT NULL,
  `match_type` varchar(16) NOT NULL DEFAULT 'EXACT',
  `match_value` varchar(512) NOT NULL,
  `description` varchar(512) DEFAULT NULL,
  `input_name` varchar(255) DEFAULT NULL,
  `output_name` varchar(255) DEFAULT NULL,
  `expected_instances` int NOT NULL DEFAULT 1,
  `visible` tinyint NOT NULL DEFAULT 1,
  `sort_order` int NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `edit_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_flink_service_registry_key` (`service_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务流式服务展示注册表';

INSERT INTO `flink_service_registry`
(`service_key`, `display_name`, `component_type`, `source_type`, `match_type`, `match_value`, `description`, `input_name`, `output_name`, `expected_instances`, `visible`, `sort_order`)
VALUES
('elevator-router', '电梯数据路由', 'ROUTER', 'FLINK_JOB', 'EXACT', 'dc-guard', '校验任务231的1024点数据，写入InfluxDB并路由到监测算法Topic', 'dc_source_231', 'dc_algorithm_REGTCN', 1, 1, 10),
('elevator-monitor', 'REGTCN异常监测', 'ALGORITHM', 'FLINK_JOB', 'EXACT', 'algorithm_REGTCN', '消费完整1024点窗口并执行电梯异常监测', 'dc_algorithm_REGTCN', 'dc_algorithm_sink_REGTCN', 1, 1, 20),
('elevator-result-sink', '监测结果入库', 'SINK', 'FLINK_JOB', 'PREFIX', 'insert-into_default_catalog.default_database.dc_algorithm_mysql', '将监测结果和报警记录写入65.237业务数据库', 'dc_algorithm_sink_*', 'dc_algorithm / dc_alarm', 1, 1, 30),
('elevator-monitor-model', 'REGTCN模型服务', 'MODEL_SERVICE', 'DOCKER_CONTAINER', 'EXACT', 'elevator-anomaly-monitoring', '提供REGTCN单次推理服务', '1024点CMS振动数据', 'rms_hi / threshold / anomaly_flag', 1, 1, 40),
('elevator-diagnosis-model', 'FFCNet诊断服务', 'MODEL_SERVICE', 'DOCKER_CONTAINER', 'EXACT', 'elevator-fault-diagnosis', '报警后按需执行电梯故障类别诊断', '报警时间附近1024点CMS振动数据', '故障类别和置信度', 1, 1, 50)
ON DUPLICATE KEY UPDATE
`display_name` = VALUES(`display_name`),
`component_type` = VALUES(`component_type`),
`source_type` = VALUES(`source_type`),
`match_type` = VALUES(`match_type`),
`match_value` = VALUES(`match_value`),
`description` = VALUES(`description`),
`input_name` = VALUES(`input_name`),
`output_name` = VALUES(`output_name`),
`expected_instances` = VALUES(`expected_instances`),
`visible` = VALUES(`visible`),
`sort_order` = VALUES(`sort_order`);

CREATE TABLE IF NOT EXISTS `job_config_cleanup_backup_20260723` LIKE `job_config`;
INSERT IGNORE INTO `job_config_cleanup_backup_20260723`
SELECT * FROM `job_config` WHERE `id` IN (56,57,58,67,68,69,72,73,75,76,81);

UPDATE `job_config`
SET `is_deleted` = 1, `editor` = 'elevator-stream-refactor', `edit_time` = NOW()
WHERE `id` IN (56,57,58,67,68,69,72,73,75,76,81) AND `is_deleted` = 0;

CREATE TABLE IF NOT EXISTS `job_algorithm_cleanup_backup_20260723` LIKE `job_algorithm`;
INSERT IGNORE INTO `job_algorithm_cleanup_backup_20260723`
SELECT * FROM `job_algorithm` WHERE `id` IN (1,2,3,4,5);

DELETE FROM `job_algorithm` WHERE `id` IN (1,2,3,4,5);
