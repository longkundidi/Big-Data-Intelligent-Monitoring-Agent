CREATE TABLE IF NOT EXISTS `al_audit_status_backup_20260716` (
  `source_type` varchar(32) NOT NULL,
  `algorithm_id` bigint unsigned NOT NULL,
  `algorithm_name` varchar(255) DEFAULT NULL,
  `is_check` bigint DEFAULT NULL,
  `is_pass` bigint DEFAULT NULL,
  `is_deployed` bigint DEFAULT NULL,
  `backup_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`source_type`, `algorithm_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='审核状态迁移前备份';

INSERT IGNORE INTO `al_audit_status_backup_20260716`
  (`source_type`, `algorithm_id`, `algorithm_name`, `is_check`, `is_pass`, `is_deployed`)
SELECT 'data_clean', id, al_name, is_check, is_pass, is_deployed FROM `al_data_clean`
UNION ALL
SELECT 'knowledge_extraction', id, al_name, is_check, is_pass, is_deployed FROM `al_knowledge_extraction`
UNION ALL
SELECT 'state_evaluation', id, model_name, is_check, is_pass, is_deployed FROM `al_state_evaluation`
UNION ALL
SELECT 'fault_diagnosis', id, model_name, is_check, is_pass, is_deployed FROM `al_fault_diagnosis`;

ALTER TABLE `al_data_clean`
  MODIFY COLUMN `is_check` bigint unsigned NULL DEFAULT 0,
  MODIFY COLUMN `is_pass` bigint unsigned NULL DEFAULT 0,
  MODIFY COLUMN `is_deployed` bigint unsigned NULL DEFAULT 0;

ALTER TABLE `al_knowledge_extraction`
  MODIFY COLUMN `is_check` bigint unsigned NULL DEFAULT 0,
  MODIFY COLUMN `is_pass` bigint NULL DEFAULT 0,
  MODIFY COLUMN `is_deployed` bigint NULL DEFAULT 0;

ALTER TABLE `al_state_evaluation`
  MODIFY COLUMN `is_check` bigint unsigned NULL DEFAULT 1,
  MODIFY COLUMN `is_pass` bigint unsigned NULL DEFAULT 2,
  MODIFY COLUMN `is_deployed` bigint NULL DEFAULT 1;

ALTER TABLE `al_fault_diagnosis`
  MODIFY COLUMN `is_check` bigint unsigned NULL DEFAULT 1,
  MODIFY COLUMN `is_pass` bigint NULL DEFAULT 2,
  MODIFY COLUMN `is_deployed` bigint NULL DEFAULT 1;

START TRANSACTION;

UPDATE `al_data_clean`
SET `is_check` = 0, `is_pass` = 0, `is_deployed` = 0;

UPDATE `al_knowledge_extraction`
SET `is_check` = 0, `is_pass` = 0, `is_deployed` = 0;

UPDATE `al_state_evaluation`
SET `is_check` = 0, `is_pass` = 0, `is_deployed` = 0;

UPDATE `al_fault_diagnosis`
SET `is_check` = 0, `is_pass` = 0, `is_deployed` = 0;

COMMIT;
