-- Remove short-name dispatch from state-evaluation and fault-diagnosis algorithms.
-- Existing implementations are selected explicitly from database metadata.
UPDATE al_state_evaluation
SET executor_type = 'SPRING_BEAN',
    executor_config = JSON_OBJECT('beanName',
        CASE model_short_name
            WHEN 'LSTMAE' THEN 'LSTMAEService'
            WHEN 'LSTM' THEN 'LSTMService'
            WHEN 'GRU' THEN 'GRUService'
            WHEN 'GRUAE' THEN 'GRUAEService'
        END)
WHERE model_short_name IN ('LSTMAE', 'LSTM', 'GRU', 'GRUAE');

UPDATE al_fault_diagnosis
SET executor_type = 'SPRING_BEAN',
    executor_config = JSON_OBJECT('beanName',
        CASE model_short_name
            WHEN 'Resnet18' THEN 'Resnet18Service'
            WHEN 'wdcnn-based-for-ballbearing' THEN 'WDCNNBasedForBallbearingService'
            WHEN 'WTConv' THEN 'WTConvService'
        END)
WHERE model_short_name IN ('Resnet18', 'wdcnn-based-for-ballbearing', 'WTConv');

ALTER TABLE al_state_evaluation
    MODIFY COLUMN executor_type varchar(64) NOT NULL COMMENT 'HTTP_JSON/ELEVATOR_INFLUX_MONITOR/SPRING_BEAN';

ALTER TABLE al_fault_diagnosis
    MODIFY COLUMN executor_type varchar(64) NOT NULL COMMENT 'HTTP_JSON/ELEVATOR_INFLUX_DIAGNOSIS/SPRING_BEAN';
