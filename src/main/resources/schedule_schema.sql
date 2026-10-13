-- ============================================================
-- 运营调度管理模块 数据库脚本
-- 数据库: autonomous_bus
-- 排班计划表 bus_schedule + 车次记录表 trip_record
-- 依赖: route(线路), bus(车辆), sys_user(调度员/安全员)
-- ============================================================

USE autonomous_bus;

-- ============================================================
-- 1. 排班计划表（发车时刻表）
-- ============================================================
DROP TABLE IF EXISTS trip_record;
DROP TABLE IF EXISTS bus_schedule;

CREATE TABLE bus_schedule
(
    id               BIGINT AUTO_INCREMENT COMMENT '主键' PRIMARY KEY,
    route_id         BIGINT                             NOT NULL COMMENT '线路ID',
    direction        TINYINT                            NOT NULL COMMENT '方向：1去程 2返程',
    schedule_date    DATE                               NULL COMMENT '排班日期(留空表示每日通用)',
    departure_time   TIME                               NOT NULL COMMENT '发车时间',
    generate_mode    TINYINT  DEFAULT 0                 NOT NULL COMMENT '生成方式：0手动单条 1批量生成',
    first_time       TIME                               NULL COMMENT '批量模式：首班时间',
    last_time        TIME                               NULL COMMENT '批量模式：末班时间',
    interval_min     INT                                NULL COMMENT '批量模式：发车间隔(分钟)',
    schedule_status  TINYINT  DEFAULT 0                 NOT NULL COMMENT '排班状态：0待发车 1待派车 2运行中 3已完成 4已停运 5已取消',
    vehicle_id       BIGINT                             NULL COMMENT '绑定车辆ID(派车后)',
    create_user_id   BIGINT                             NULL COMMENT '创建人ID(调度员)',
    remark           VARCHAR(255)                       NULL COMMENT '备注',
    create_time      DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time      DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted          TINYINT  DEFAULT 0                 NOT NULL COMMENT '逻辑删除：0未删 1已删'
) COMMENT '排班计划表';
CREATE INDEX idx_route_direction ON bus_schedule (route_id, direction);
CREATE INDEX idx_schedule_date ON bus_schedule (schedule_date);
CREATE INDEX idx_schedule_status ON bus_schedule (schedule_status);
CREATE INDEX idx_departure_time ON bus_schedule (departure_time);

-- ============================================================
-- 2. 车次记录表（一趟往返任务）
-- ============================================================
CREATE TABLE trip_record
(
    id                 BIGINT AUTO_INCREMENT COMMENT '主键' PRIMARY KEY,
    schedule_id        BIGINT                             NULL COMMENT '来源排班ID(临时加派为NULL)',
    route_id           BIGINT                             NOT NULL COMMENT '线路ID',
    vehicle_id         BIGINT                             NOT NULL COMMENT '车辆ID',
    direction          TINYINT                            NOT NULL COMMENT '方向：1去程 2返程',
    depart_time        DATETIME                           NOT NULL COMMENT '计划发车时间',
    actual_depart_time DATETIME                           NULL COMMENT '实际发车时间',
    est_arrive_time    DATETIME                           NULL COMMENT '预计到达时间',
    actual_arrive_time DATETIME                           NULL COMMENT '实际到达时间',
    trip_status        TINYINT  DEFAULT 0                 NOT NULL COMMENT '车次状态：0待发车 1运行中 2已完成 3已中断',
    interrupt_reason   VARCHAR(255)                       NULL COMMENT '中断原因(下线/故障)',
    is_temp            TINYINT  DEFAULT 0                 NOT NULL COMMENT '是否临时加派：0否 1是',
    create_user_id     BIGINT                             NULL COMMENT '创建人ID',
    remark             VARCHAR(255)                       NULL COMMENT '备注',
    create_time        DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time        DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted            TINYINT  DEFAULT 0                 NOT NULL COMMENT '逻辑删除：0未删 1已删'
) COMMENT '车次记录表';
CREATE INDEX idx_schedule_id ON trip_record (schedule_id);
CREATE INDEX idx_route_id ON trip_record (route_id);
CREATE INDEX idx_vehicle_id ON trip_record (vehicle_id);
CREATE INDEX idx_trip_status ON trip_record (trip_status);
CREATE INDEX idx_depart_time ON trip_record (depart_time);

-- ============================================================
-- 3. 车辆表增加电量字段（若不存在）
-- ============================================================
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='autonomous_bus' AND TABLE_NAME='bus' AND COLUMN_NAME='battery');
SET @ddl = IF(@col_exists = 0, 'ALTER TABLE bus ADD COLUMN battery INT DEFAULT 100 COMMENT ''电量百分比''', 'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
