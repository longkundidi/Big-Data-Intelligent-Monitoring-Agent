INSERT INTO al_state_evaluation (
    al_code,
    model_type_first,
    model_name,
    model_short_name,
    model_provider,
    model_object,
    object_id,
    model_type,
    model_function,
    model_invoke,
    model_library,
    model_condition,
    model_advantage,
    model_disadvantage,
    model_url,
    program_url,
    input,
    output,
    deploy_input,
    deploy_output,
    deploy_require,
    is_check,
    is_pass,
    is_deployed,
    isjson,
    is_service,
    model_num,
    jar_size,
    run_size,
    create_time,
    edit_time
)
SELECT
    'se-regtcn',
    '数字孪生应用算法集',
    '电梯异常监测(REGTCN)',
    'REGTCN',
    'elevator_system_models',
    '电梯',
    'elevator',
    '深度学习模型',
    '电梯运行状态评估',
    '业务调用（微服务）',
    'PyTorch 1.11; FastAPI 0.95.1',
    '电梯单通道时序数据异常监测',
    '单次同步调用，可返回异常分数和判定结果',
    '每次调用必须提供连续且恰好1024个有效采样点',
    'http://192.168.16.219:8873/createTask/',
    'program/2026/7/22/REGTCN.zip',
    '{"monitorPointId":"<point-id>","startTime":"<ISO-8601>","endTime":"<ISO-8601>"}',
    '{"sample_count":1024,"status":"normal","is_anomaly":false,"anomaly_score":0.0,"threshold":1.02470964}',
    '从InfluxDB读取单通道连续窗口，恰好1024个有限数值',
    '异常标志、归一化异常分数、阈值和重构误差',
    'Docker 24; x86_64 CPU; 2核CPU; 2GiB内存',
    0,
    0,
    0,
    0,
    0,
    0,
    '2.74 GiB image',
    '2 CPU cores; 2 GiB memory limit',
    DATE_ADD(UTC_TIMESTAMP(), INTERVAL 8 HOUR),
    DATE_ADD(UTC_TIMESTAMP(), INTERVAL 8 HOUR)
WHERE NOT EXISTS (
    SELECT 1
    FROM al_state_evaluation
    WHERE model_short_name = 'REGTCN'
);

UPDATE al_state_evaluation
SET model_name = '电梯异常监测(REGTCN)',
    model_short_name = 'REGTCN',
    model_object = '电梯',
    object_id = 'elevator',
    model_url = 'http://192.168.16.219:8873/createTask/',
    program_url = 'program/2026/7/22/REGTCN.zip',
    backup_model_url = NULL,
    input = '{"monitorPointId":"<point-id>","startTime":"<ISO-8601>","endTime":"<ISO-8601>"}',
    output = '{"sample_count":1024,"status":"normal","is_anomaly":false,"anomaly_score":0.0,"threshold":1.02470964}',
    deploy_input = '从InfluxDB读取单通道连续窗口，恰好1024个有限数值',
    deploy_output = '异常标志、归一化异常分数、阈值和重构误差',
    deploy_require = 'Docker 24; x86_64 CPU; 2核CPU; 2GiB内存',
    model_invoke = '业务调用（微服务）',
    model_library = 'PyTorch 1.11; FastAPI 0.95.1',
    is_check = 0,
    is_pass = 0,
    is_deployed = 0,
    isjson = 0,
    is_service = 0,
    jar_size = '2.74 GiB image',
    run_size = '2 CPU cores; 2 GiB memory limit',
    edit_time = DATE_ADD(UTC_TIMESTAMP(), INTERVAL 8 HOUR)
WHERE model_short_name = 'REGTCN';

INSERT INTO al_state_evaluation_register1to2 (
    model_name, model_framework, device, if_train, if_test,
    train_dataset, test_dataset, if_pretreatment, pretreatment,
    sample_type, sample_size, train_times, train_batch, loss_function,
    regularization, learning_rate, optimizer, train_results, test_results, metrics
)
SELECT
    '电梯异常监测(REGTCN)', 'PyTorch', 'CPU', 0, 1,
    '', '', 1, '有限值校验；重塑为[1,1,1024]',
    '45Hz单通道时序信号', '[1,1,1024]', '离线训练', '离线训练', '重构误差',
    '模型固化', '离线训练', '离线训练', '已完成离线训练与阈值标定',
    '验证准确率94.9167%；F1 97.0602%', '异常分数、阈值、准确率、F1'
WHERE NOT EXISTS (
    SELECT 1
    FROM al_state_evaluation_register1to2
    WHERE model_name = '电梯异常监测(REGTCN)'
);

SELECT id,
       model_name,
       model_short_name,
       model_url,
       is_check,
       is_pass,
       is_deployed,
       is_service,
       isjson,
       deploy_input,
       deploy_output,
       deploy_require
FROM al_state_evaluation
WHERE model_short_name = 'REGTCN';
