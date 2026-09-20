ALTER TABLE config_perceived_task
    ADD COLUMN data_dimension BIGINT NULL COMMENT '单次模型输入的数据维度' AFTER variable_num;

UPDATE config_perceived_task
SET data_dimension = CASE
    WHEN algo_shortname = 'REGTCN' THEN 1024
    ELSE COALESCE(variable_num, 1)
END
WHERE data_dimension IS NULL;

UPDATE config_perceived_task task
LEFT JOIN (
    SELECT task_id, COUNT(*) AS variable_count
    FROM config_perceived_task_variable
    GROUP BY task_id
) relation_count ON relation_count.task_id = task.task_id
SET task.variable_num = COALESCE(relation_count.variable_count, 0);
