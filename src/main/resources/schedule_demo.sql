USE autonomous_bus;
DELETE FROM trip_record;
DELETE FROM bus_schedule;
UPDATE bus SET status = 4 WHERE id IN (1,3,5);
UPDATE bus SET status = 1 WHERE id IN (2,6);
UPDATE bus SET status = 3 WHERE id = 4;
UPDATE bus SET battery = 85 WHERE id = 1;
UPDATE bus SET battery = 92 WHERE id = 2;
UPDATE bus SET battery = 45 WHERE id = 3;
UPDATE bus SET battery = 60 WHERE id = 4;
UPDATE bus SET battery = 30 WHERE id = 5;
UPDATE bus SET battery = 100 WHERE id = 6;
-- 排班演示：日期设为未来(2026-10-15)，保持待发车状态供手动派车演示
INSERT INTO bus_schedule (route_id, direction, schedule_date, departure_time, generate_mode, first_time, last_time, interval_min, schedule_status, vehicle_id, create_user_id, remark) VALUES
(1, 1, '2026-10-15', '08:00:00', 1, '08:00:00', '12:00:00', 30, 0, NULL, 1, '1路批量排班'),
(1, 1, '2026-10-15', '08:30:00', 1, '08:00:00', '12:00:00', 30, 0, NULL, 1, '1路批量排班'),
(1, 1, '2026-10-15', '09:00:00', 1, '08:00:00', '12:00:00', 30, 0, NULL, 1, '1路批量排班'),
(1, 1, '2026-10-15', '09:30:00', 1, '08:00:00', '12:00:00', 30, 0, NULL, 1, '1路批量排班'),
(1, 2, '2026-10-15', '10:00:00', 1, '10:00:00', '13:00:00', 30, 0, NULL, 1, '1路返程批量排班'),
(2, 1, '2026-10-15', '07:30:00', 1, '07:30:00', '11:30:00', 40, 0, NULL, 1, '2路机场快线'),
(2, 1, '2026-10-15', '08:10:00', 1, '07:30:00', '11:30:00', 40, 0, NULL, 1, '2路机场快线'),
(3, 1, '2026-10-15', '08:20:00', 0, NULL, NULL, NULL, 0, NULL, 1, '3路手动单条排班');
-- 车次演示：已运行/已完成/中断各若干
INSERT INTO trip_record (schedule_id, route_id, vehicle_id, direction, depart_time, actual_depart_time, est_arrive_time, actual_arrive_time, trip_status, interrupt_reason, is_temp, create_user_id, remark) VALUES
(NULL, 1, 1, 1, '2026-10-13 08:00:00', '2026-10-13 08:00:00', '2026-10-13 09:00:00', NULL, 1, NULL, 0, 1, '1路首班运营中'),
(NULL, 3, 5, 1, '2026-10-13 07:40:00', '2026-10-13 07:40:00', '2026-10-13 08:40:00', NULL, 1, NULL, 1, 1, '临时加派车次'),
(NULL, 2, 3, 1, '2026-10-13 08:00:00', '2026-10-13 08:00:00', '2026-10-13 09:00:00', '2026-10-13 08:55:00', 2, NULL, 0, 1, '已完成车次'),
(NULL, 1, 2, 1, '2026-10-13 06:50:00', '2026-10-13 06:50:00', '2026-10-13 07:50:00', NULL, 3, '车辆临时下线', 0, 1, '已中断车次');
