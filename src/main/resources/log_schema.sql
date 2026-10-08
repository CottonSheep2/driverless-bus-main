USE autonomous_bus;

CREATE TABLE IF NOT EXISTS operation_log (
    id          BIGINT AUTO_INCREMENT COMMENT '主键' PRIMARY KEY,
    user_id     BIGINT                             NULL COMMENT '操作人ID',
    username    VARCHAR(50)                        NOT NULL COMMENT '操作人用户名',
    module      VARCHAR(50)                        NOT NULL COMMENT '操作模块',
    operation   VARCHAR(50)                        NOT NULL COMMENT '操作类型',
    method      VARCHAR(200)                       NULL COMMENT '请求方法',
    params      TEXT                               NULL COMMENT '请求参数',
    ip          VARCHAR(50)                        NULL COMMENT 'IP地址',
    status      TINYINT  DEFAULT 1                 NOT NULL COMMENT '操作状态：1成功 0失败',
    error_msg   VARCHAR(500)                       NULL COMMENT '失败原因',
    cost_time   BIGINT   DEFAULT 0                 NOT NULL COMMENT '耗时(ms)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '操作时间'
) COMMENT '操作审计日志表';
CREATE INDEX idx_user_id ON operation_log (user_id);
CREATE INDEX idx_module ON operation_log (module);
CREATE INDEX idx_create_time ON operation_log (create_time);
