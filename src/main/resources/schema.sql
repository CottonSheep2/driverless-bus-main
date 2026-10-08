-- ============================================================
-- 无人驾驶公交车运行管理系统 数据库初始化脚本
-- 数据库: autonomous_bus
-- ============================================================

CREATE DATABASE IF NOT EXISTS autonomous_bus DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE autonomous_bus;

-- ============================================================
-- 1. 告警类型表
-- ============================================================
DROP TABLE IF EXISTS alarm_handle;
DROP TABLE IF EXISTS alarm_report;
DROP TABLE IF EXISTS alarm_type;
DROP TABLE IF EXISTS bus;
DROP TABLE IF EXISTS route_station;
DROP TABLE IF EXISTS route;
DROP TABLE IF EXISTS station;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS sys_role;

CREATE TABLE alarm_type
(
    id          BIGINT AUTO_INCREMENT COMMENT '主键' PRIMARY KEY,
    type_name   VARCHAR(50)                        NOT NULL COMMENT '类型名称',
    type_code   VARCHAR(50)                        NOT NULL COMMENT '类型编码',
    remark      VARCHAR(255)                       NULL COMMENT '备注',
    status      TINYINT  DEFAULT 1                 NOT NULL COMMENT '状态：1启用 0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT  DEFAULT 0                 NOT NULL COMMENT '逻辑删除：0未删 1已删',
    CONSTRAINT uk_type_code UNIQUE (type_code)
) COMMENT '告警类型表';
CREATE INDEX idx_status ON alarm_type (status);

-- ============================================================
-- 2. 告警上报表
-- ============================================================
CREATE TABLE alarm_report
(
    id            BIGINT AUTO_INCREMENT COMMENT '主键' PRIMARY KEY,
    alarm_type_id BIGINT                             NOT NULL COMMENT '告警类型ID',
    title         VARCHAR(100)                       NOT NULL COMMENT '告警标题',
    content       TEXT                               NOT NULL COMMENT '告警内容',
    reporter_id   BIGINT                             NOT NULL COMMENT '上报人员ID',
    report_time   DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '上报时间',
    status        TINYINT  DEFAULT 0                 NOT NULL COMMENT '状态：0已上报 1已处理',
    deleted       TINYINT  DEFAULT 0                 NOT NULL COMMENT '逻辑删除：0未删 1已删'
) COMMENT '告警上报表';
CREATE INDEX idx_alarm_type_id ON alarm_report (alarm_type_id);
CREATE INDEX idx_report_time ON alarm_report (report_time);
CREATE INDEX idx_reporter_id ON alarm_report (reporter_id);
CREATE INDEX idx_status ON alarm_report (status);

-- ============================================================
-- 3. 告警处理表
-- ============================================================
CREATE TABLE alarm_handle
(
    id             BIGINT AUTO_INCREMENT COMMENT '主键' PRIMARY KEY,
    report_id      BIGINT                             NOT NULL COMMENT '上报表ID',
    handler_id     BIGINT                             NOT NULL COMMENT '处理人员ID',
    handle_content TEXT                               NOT NULL COMMENT '处理内容',
    handle_time    DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '处理时间',
    deleted        TINYINT  DEFAULT 0                 NOT NULL COMMENT '逻辑删除：0未删 1已删'
) COMMENT '告警处理表';
CREATE INDEX idx_handle_time ON alarm_handle (handle_time);
CREATE INDEX idx_handler_id ON alarm_handle (handler_id);
CREATE INDEX idx_report_id ON alarm_handle (report_id);

-- ============================================================
-- 4. 站点表
-- ============================================================
CREATE TABLE station
(
    id           BIGINT AUTO_INCREMENT COMMENT '主键' PRIMARY KEY,
    station_name VARCHAR(100)                       NOT NULL COMMENT '站点名称',
    longitude    DECIMAL(10, 6)                     NULL COMMENT '经度',
    latitude     DECIMAL(10, 6)                     NULL COMMENT '纬度',
    area         VARCHAR(100)                       NULL COMMENT '所属区域',
    status       TINYINT  DEFAULT 1                 NOT NULL COMMENT '状态：1运营中 2停运 3维护中 4规划中',
    remark       VARCHAR(255)                       NULL COMMENT '备注',
    create_time  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      TINYINT  DEFAULT 0                 NOT NULL COMMENT '逻辑删除：0未删 1已删'
) COMMENT '站点表';
CREATE INDEX idx_station_name ON station (station_name);
CREATE INDEX idx_status ON station (status);

-- ============================================================
-- 5. 路线表
-- ============================================================
CREATE TABLE route
(
    id          BIGINT AUTO_INCREMENT COMMENT '主键' PRIMARY KEY,
    route_name  VARCHAR(100)                       NOT NULL COMMENT '路线名称',
    route_code  VARCHAR(50)                        NOT NULL COMMENT '路线编号',
    status      TINYINT  DEFAULT 1                 NOT NULL COMMENT '状态：1运营中 2停运 3维护中 4规划中',
    remark      VARCHAR(255)                       NULL COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT  DEFAULT 0                 NOT NULL COMMENT '逻辑删除：0未删 1已删',
    CONSTRAINT uk_route_code UNIQUE (route_code)
) COMMENT '路线表';
CREATE INDEX idx_status ON route (status);

-- ============================================================
-- 6. 路线站点关联表
-- ============================================================
CREATE TABLE route_station
(
    id          BIGINT AUTO_INCREMENT COMMENT '主键' PRIMARY KEY,
    route_id    BIGINT                             NOT NULL COMMENT '路线ID',
    station_id  BIGINT                             NOT NULL COMMENT '站点ID',
    seq         INT                                NOT NULL COMMENT '站点顺序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    CONSTRAINT uk_route_seq UNIQUE (route_id, seq),
    CONSTRAINT uk_route_station UNIQUE (route_id, station_id)
) COMMENT '路线站点关联表';
CREATE INDEX idx_station_id ON route_station (station_id);

-- ============================================================
-- 7. 公交车表
-- ============================================================
CREATE TABLE bus
(
    id           BIGINT AUTO_INCREMENT COMMENT '主键' PRIMARY KEY,
    plate_number VARCHAR(20)                        NOT NULL COMMENT '车牌号',
    model        VARCHAR(100)                       NULL COMMENT '车辆型号',
    capacity     INT      DEFAULT 0                 NOT NULL COMMENT '载客量',
    route_id     BIGINT                             NULL COMMENT '所属路线ID',
    status       TINYINT  DEFAULT 1                 NOT NULL COMMENT '状态：1在线 2离线 3检修 4运营中',
    remark       VARCHAR(255)                       NULL COMMENT '备注',
    create_time  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      TINYINT  DEFAULT 0                 NOT NULL COMMENT '逻辑删除：0未删 1已删',
    CONSTRAINT uk_plate_number UNIQUE (plate_number)
) COMMENT '公交车表';
CREATE INDEX idx_route_id ON bus (route_id);
CREATE INDEX idx_status ON bus (status);

-- ============================================================
-- 8. 角色表
-- ============================================================
CREATE TABLE sys_role
(
    id          BIGINT AUTO_INCREMENT COMMENT '主键' PRIMARY KEY,
    role_name   VARCHAR(50)                        NOT NULL COMMENT '角色名称',
    role_code   VARCHAR(50)                        NOT NULL COMMENT '角色编码',
    remark      VARCHAR(255)                       NULL COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    CONSTRAINT uk_role_code UNIQUE (role_code)
) COMMENT '角色表';

-- ============================================================
-- 9. 用户表
-- ============================================================
CREATE TABLE sys_user
(
    id          BIGINT AUTO_INCREMENT COMMENT '主键' PRIMARY KEY,
    username    VARCHAR(50)                        NOT NULL COMMENT '登录名',
    password    VARCHAR(255)                       NOT NULL COMMENT '密码',
    phone       VARCHAR(20)                        NULL COMMENT '手机号',
    role_id     BIGINT                             NOT NULL COMMENT '角色ID',
    status      TINYINT  DEFAULT 1                 NOT NULL COMMENT '状态：1启用 0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT uk_phone UNIQUE (phone)
) COMMENT '用户表';

-- ============================================================
-- 初始化数据
-- ============================================================

-- 角色
INSERT INTO sys_role (role_name, role_code, remark) VALUES
('系统管理员', 'ADMIN', '后台系统管理员，拥有全部权限'),
('安全员', 'SAFETY', '安全员，负责告警上报与处置');

-- 用户（密码明文：123456）
INSERT INTO sys_user (username, password, phone, role_id, status) VALUES
('admin', '123456', '13800138000', 1, 1),
('safety', '123456', '13800138001', 2, 1);

-- 告警类型
INSERT INTO alarm_type (type_name, type_code, remark) VALUES
('设备故障', 'DEVICE_FAULT', '车辆设备异常故障'),
('障碍物', 'OBSTACLE', '行驶路径中出现障碍物'),
('恶劣天气', 'BAD_WEATHER', '雨雪雾等恶劣天气影响'),
('乘客异常', 'PASSENGER', '乘客突发状况'),
('系统异常', 'SYSTEM_ERROR', '自动驾驶系统异常');

-- 测试站点
INSERT INTO station (station_name, longitude, latitude, area, status, remark) VALUES
('中心广场站', 106.5516, 29.5630, '渝中区', 1, '市中心枢纽'),
('人民公园站', 106.5620, 29.5680, '渝中区', 1, ''),
('科技园区站', 106.4800, 29.6100, '沙坪坝区', 1, '高新区核心'),
('大学城站', 106.3100, 29.5900, '沙坪坝区', 1, '高校集中区'),
('机场枢纽站', 106.6400, 29.7100, '渝北区', 1, '江北机场'),
('火车站', 106.5800, 29.5800, '渝中区', 1, '重庆北站'),
('滨江路站', 106.5700, 29.5500, '渝中区', 2, '临时停运'),
('新区管委会站', 106.4200, 29.6500, '两江新区', 4, '规划中');

-- 测试线路
INSERT INTO route (route_name, route_code, status, remark) VALUES
('1路环线', 'R001', 1, '中心广场-人民公园-滨江路环线'),
('2路机场快线', 'R002', 1, '火车站-机场直达'),
('3路大学城专线', 'R003', 1, '科技园区-大学城');

-- 线路站点关联
INSERT INTO route_station (route_id, station_id, seq) VALUES
(1, 1, 1), (1, 2, 2), (1, 7, 3),
(2, 6, 1), (2, 5, 2),
(3, 3, 1), (3, 4, 2);

-- 测试公交车
INSERT INTO bus (plate_number, model, capacity, route_id, status, remark) VALUES
('渝A12345', '宇通ZK6128', 50, 1, 4, '1路运营中'),
('渝A12346', '宇通ZK6128', 50, 1, 1, '待命'),
('渝A22345', '比亚迪K9', 45, 2, 4, '2路运营中'),
('渝A22346', '比亚迪K9', 45, 2, 3, '检修中'),
('渝A32345', '金龙XMQ6127', 48, 3, 4, '3路运营中'),
('渝A42345', '宇通ZK6128', 50, NULL, 2, '未分配线路');

-- 测试告警
INSERT INTO alarm_report (alarm_type_id, title, content, reporter_id, report_time, status) VALUES
(1, '车辆制动系统异常', '渝A12345在行驶过程中制动系统报警，需立即检查', 2, '2026-10-01 08:30:00', 1),
(2, '前方道路障碍物', '渝A22345在机场高速段发现前方障碍物，已紧急避让', 2, '2026-10-02 14:20:00', 1),
(3, '大雾天气影响运行', '全市大雾预警，能见度不足50米，建议暂停运营', 2, '2026-10-03 06:00:00', 0),
(4, '乘客突发疾病', '渝A32345上一名乘客突发心脏病，已呼叫120', 2, '2026-10-04 10:15:00', 0),
(5, '自动驾驶系统重启', '渝A12346自动驾驶系统异常重启，已切换人工模式', 2, '2026-10-05 09:00:00', 0);

-- 告警处理记录
INSERT INTO alarm_handle (report_id, handler_id, handle_content, handle_time) VALUES
(1, 2, '已通知维修团队前往检查，更换制动片后恢复正常', '2026-10-01 09:30:00'),
(2, 2, '已联系路政清理障碍物，车辆恢复正常行驶', '2026-10-02 15:00:00');
