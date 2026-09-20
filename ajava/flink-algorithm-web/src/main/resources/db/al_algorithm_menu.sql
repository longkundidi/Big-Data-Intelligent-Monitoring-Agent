CREATE TABLE IF NOT EXISTS `al_algorithm_menu` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '菜单节点主键',
  `parent_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '父节点ID，0表示根节点',
  `menu_code` varchar(64) NOT NULL COMMENT '菜单节点唯一编码',
  `menu_name` varchar(255) NOT NULL COMMENT '菜单显示名称',
  `node_type` varchar(32) NOT NULL COMMENT '节点类型：category或algorithm',
  `algorithm_type` varchar(32) DEFAULT NULL COMMENT '算法类型：evaluation或diagnosis',
  `algorithm_id` bigint unsigned DEFAULT NULL COMMENT '对应算法表主键',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '同级显示顺序',
  `enabled` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '是否启用：1是，0否',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_menu_code` (`menu_code`),
  UNIQUE KEY `uk_algorithm_ref` (`algorithm_type`, `algorithm_id`),
  KEY `idx_parent_sort` (`parent_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基础算法库左侧菜单树';

INSERT INTO `al_algorithm_menu`
  (`parent_id`, `menu_code`, `menu_name`, `node_type`, `algorithm_type`, `algorithm_id`, `sort_order`, `enabled`)
VALUES
  (0, 'state-evaluation', '状态评估算法', 'category', 'evaluation', NULL, 10, 1),
  (0, 'fault-diagnosis', '故障诊断算法', 'category', 'diagnosis', NULL, 20, 1)
ON DUPLICATE KEY UPDATE
  `menu_name` = VALUES(`menu_name`),
  `node_type` = VALUES(`node_type`),
  `algorithm_type` = VALUES(`algorithm_type`),
  `sort_order` = VALUES(`sort_order`),
  `enabled` = VALUES(`enabled`);

INSERT INTO `al_algorithm_menu`
  (`parent_id`, `menu_code`, `menu_name`, `node_type`, `algorithm_type`, `algorithm_id`, `sort_order`, `enabled`)
SELECT
  category.`id`,
  CONCAT('evaluation-', algorithm.`id`),
  algorithm.`model_name`,
  'algorithm',
  'evaluation',
  algorithm.`id`,
  ROW_NUMBER() OVER (ORDER BY algorithm.`id`) * 10,
  1
FROM `al_state_evaluation` algorithm
JOIN `al_algorithm_menu` category ON category.`menu_code` = 'state-evaluation'
WHERE COALESCE(algorithm.`is_service`, 0) = 0
ON DUPLICATE KEY UPDATE
  `parent_id` = VALUES(`parent_id`),
  `menu_name` = VALUES(`menu_name`),
  `node_type` = VALUES(`node_type`),
  `sort_order` = VALUES(`sort_order`),
  `enabled` = VALUES(`enabled`);

INSERT INTO `al_algorithm_menu`
  (`parent_id`, `menu_code`, `menu_name`, `node_type`, `algorithm_type`, `algorithm_id`, `sort_order`, `enabled`)
SELECT
  category.`id`,
  CONCAT('diagnosis-', algorithm.`id`),
  algorithm.`model_name`,
  'algorithm',
  'diagnosis',
  algorithm.`id`,
  ROW_NUMBER() OVER (ORDER BY algorithm.`id`) * 10,
  1
FROM `al_fault_diagnosis` algorithm
JOIN `al_algorithm_menu` category ON category.`menu_code` = 'fault-diagnosis'
WHERE COALESCE(algorithm.`is_service`, 0) = 0
ON DUPLICATE KEY UPDATE
  `parent_id` = VALUES(`parent_id`),
  `menu_name` = VALUES(`menu_name`),
  `node_type` = VALUES(`node_type`),
  `sort_order` = VALUES(`sort_order`),
  `enabled` = VALUES(`enabled`);
