-- Unified algorithm-management runtime metadata. Existing tables and IDs remain authoritative
-- so audit, task configuration and diagnosis records continue to work unchanged.
ALTER TABLE al_state_evaluation
    ADD COLUMN executor_type varchar(64) NOT NULL DEFAULT 'HTTP_JSON' COMMENT 'HTTP_JSON/ELEVATOR_INFLUX_MONITOR/ELEVATOR_INFLUX_DIAGNOSIS/LEGACY' AFTER model_short_name,
    ADD COLUMN executor_config json NULL COMMENT 'Executor-specific configuration' AFTER executor_type;

ALTER TABLE al_fault_diagnosis
    ADD COLUMN executor_type varchar(64) NOT NULL DEFAULT 'HTTP_JSON' COMMENT 'HTTP_JSON/ELEVATOR_INFLUX_MONITOR/ELEVATOR_INFLUX_DIAGNOSIS/LEGACY' AFTER model_short_name,
    ADD COLUMN executor_config json NULL COMMENT 'Executor-specific configuration' AFTER executor_type;

UPDATE al_state_evaluation SET executor_type = 'LEGACY';
UPDATE al_fault_diagnosis SET executor_type = 'LEGACY';
UPDATE al_state_evaluation SET executor_type = 'ELEVATOR_INFLUX_MONITOR' WHERE id = 196 OR model_short_name = 'REGTCN';
UPDATE al_fault_diagnosis SET executor_type = 'ELEVATOR_INFLUX_DIAGNOSIS' WHERE id = 256 OR model_short_name = 'FFCNet';

CREATE UNIQUE INDEX uk_state_evaluation_al_code ON al_state_evaluation (al_code);
CREATE UNIQUE INDEX uk_fault_diagnosis_al_code ON al_fault_diagnosis (al_code);
