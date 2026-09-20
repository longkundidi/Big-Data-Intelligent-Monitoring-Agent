-- Algorithm short names are stable Kafka routing keys used by dc_guard.
-- Cross-table uniqueness is also enforced by AlgorithmManagementService.
ALTER TABLE al_state_evaluation
    MODIFY COLUMN model_short_name varchar(64) NOT NULL COMMENT 'Unique algorithm short name used for Kafka routing',
    ADD UNIQUE INDEX uk_state_evaluation_short_name (model_short_name);

ALTER TABLE al_fault_diagnosis
    MODIFY COLUMN model_short_name varchar(64) NOT NULL COMMENT 'Unique algorithm short name used for Kafka routing',
    ADD UNIQUE INDEX uk_fault_diagnosis_short_name (model_short_name);
