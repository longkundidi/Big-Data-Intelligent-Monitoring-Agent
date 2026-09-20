INSERT INTO al_fault_diagnosis (
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
    'fd-ffcnet',
    '数字孪生应用算法集',
    '电梯制动系统故障诊断(FFCNet)',
    'FFCNet',
    'elevator_system_models',
    '电梯',
    'elevator',
    '深度学习模型',
    '电梯故障诊断',
    '业务调用（微服务）',
    'PyTorch 2.5.1 CPU; FastAPI 0.110.0',
    '电梯制动系统单通道时序信号八分类诊断',
    '单次同步调用，直接返回故障类别、置信度和八类概率',
    '每次诊断必须提供连续且恰好1024个有效采样点',
    'http://192.168.16.219:8874/createTask/',
    'program/2026/7/22/FFCNet.zip',
    '{"monitorPointId":"<point-id>","startTime":"<ISO-8601>","endTime":"<ISO-8601>"}',
    '{"fault_code":0,"fault_name":"正常","is_fault":false,"confidence":0.99,"sample_count":1024}',
    '平台从InfluxDB读取1024个有限数值并通过JSON values数组直接传输',
    '八类故障编号、中文名称、置信度和完整概率分布',
    'Docker 24; x86_64 CPU; 2核CPU; 2GiB内存; 不依赖MinIO',
    0,
    0,
    0,
    0,
    0,
    0,
    '3.33 GB image',
    '2 CPU cores; 2 GiB memory limit',
    DATE_ADD(UTC_TIMESTAMP(), INTERVAL 8 HOUR),
    DATE_ADD(UTC_TIMESTAMP(), INTERVAL 8 HOUR)
WHERE NOT EXISTS (
    SELECT 1
    FROM al_fault_diagnosis
    WHERE model_short_name = 'FFCNet'
);

UPDATE al_fault_diagnosis
SET model_name = '电梯制动系统故障诊断(FFCNet)',
    model_object = '电梯',
    object_id = 'elevator',
    model_url = 'http://192.168.16.219:8874/createTask/',
    program_url = 'program/2026/7/22/FFCNet.zip',
    backup_model_url = NULL,
    input = '{"monitorPointId":"<point-id>","startTime":"<ISO-8601>","endTime":"<ISO-8601>"}',
    output = '{"fault_code":0,"fault_name":"正常","is_fault":false,"confidence":0.99,"sample_count":1024}',
    deploy_input = '平台从InfluxDB读取1024个有限数值并通过JSON values数组直接传输',
    deploy_output = '八类故障编号、中文名称、置信度和完整概率分布',
    deploy_require = 'Docker 24; x86_64 CPU; 2核CPU; 2GiB内存; 不依赖MinIO',
    model_invoke = '业务调用（微服务）',
    model_library = 'PyTorch 2.5.1 CPU; FastAPI 0.110.0',
    is_check = 0,
    is_pass = 0,
    is_deployed = 0,
    isjson = 0,
    is_service = 0,
    jar_size = '3.33 GB image',
    run_size = '2 CPU cores; 2 GiB memory limit',
    edit_time = DATE_ADD(UTC_TIMESTAMP(), INTERVAL 8 HOUR)
WHERE model_short_name = 'FFCNet';

INSERT INTO al_fault_diagnosis_register1to2 (
    model_name, model_framework, device, if_train, if_test,
    train_dataset, test_dataset, if_pretreatment, pretreatment,
    sample_type, sample_size, train_times, train_batch, loss_function,
    regularization, learning_rate, optimizer, train_results, test_results, metrics
)
SELECT
    '电梯制动系统故障诊断(FFCNet)', 'PyTorch', 'CPU', 0, 1,
    '', '', 1, '有限值校验；重塑为[1,1,1024]',
    '45Hz单通道振动信号', '[1,1,1024]', '离线训练', '离线训练', '交叉熵',
    '模型固化', '离线训练', '离线训练', '已完成离线训练',
    '45Hz验证集准确率92.4167%', '准确率、故障类别、置信度、类别概率'
WHERE NOT EXISTS (
    SELECT 1
    FROM al_fault_diagnosis_register1to2
    WHERE model_name = '电梯制动系统故障诊断(FFCNet)'
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
FROM al_fault_diagnosis
WHERE model_short_name = 'FFCNet';
