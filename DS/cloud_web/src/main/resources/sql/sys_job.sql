

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_job
-- ----------------------------
DROP TABLE IF EXISTS `sys_job`;
CREATE TABLE `job`  (
   `job_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务ID',
   `job_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '任务名称',
   `job_group` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'DEFAULT' COMMENT '任务组名',
   `invoke_target` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调用目标字符串',
   `cron` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT 'cron执行表达式',
   `policy` int(1) NOT NULL COMMENT '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
   `concurrent` int(1) NOT NULL  COMMENT '是否并发执行（0允许 1禁止）',
   `status` int(1) NOT NULL COMMENT '状态（0正常 1暂停）',
   `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
   `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
   `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
   `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
   `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '备注信息',
   PRIMARY KEY (`job_id`, `job_group`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 107 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '定时任务调度表' ROW_FORMAT = Dynamic;


SET FOREIGN_KEY_CHECKS = 1;
