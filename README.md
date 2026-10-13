# 无人驾驶公交车运行管理系统 - 后端服务

基于 Spring Boot 3 + MyBatis-Plus 的无人驾驶公交车运行管理后端 API。

## 技术栈

| 分类 | 技术 | 版本 |
|------|------|------|
| 框架 | Spring Boot | 3.2.5 |
| ORM | MyBatis-Plus | 3.5.5 |
| 数据库 | MySQL | 8.0 |
| 缓存/Token | Redis | 6.x+ |
| 认证 | JJWT (JWT) | 0.11.5 |
| AOP | spring-boot-starter-aop | — |
| 连接池 | HikariCP | — |
| JDK | OpenJDK | 17+ |

## 快速启动

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0（端口 3306）
- Redis 6+（端口 6379）

### 1. 初始化数据库

```sql
-- 登录 MySQL 后执行
CREATE DATABASE IF NOT EXISTS autonomous_bus DEFAULT CHARACTER SET utf8mb4;
```

然后依次执行项目中的 SQL 脚本（位于 `src/main/resources/`）：

| 脚本 | 说明 |
|------|------|
| `schema.sql` | 建表 + 基础角色、管理员账号 |
| `data_extra.sql` | 测试数据（站点、线路、车辆、告警等） |
| `log_schema.sql` | 操作日志表 |
| `schedule_schema.sql` | 运营调度模块建表（排班表、车次表）+ 车辆电量字段 |
| `schedule_demo.sql` | 运营调度演示数据（排班、车次样例） |

```bash
mysql -u root -p autonomous_bus < src/main/resources/schema.sql
mysql -u root -p autonomous_bus < src/main/resources/data_extra.sql
mysql -u root -p autonomous_bus < src/main/resources/log_schema.sql
mysql -u root -p autonomous_bus < src/main/resources/schedule_schema.sql
mysql -u root -p autonomous_bus < src/main/resources/schedule_demo.sql
```

### 2. 修改配置

编辑 `src/main/resources/application.yml`，确认数据库和 Redis 连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/autonomous_bus?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
  data:
    redis:
      host: 127.0.0.1
      port: 6379
```

### 3. 启动

```bash
# 方式一：Maven 启动
mvn spring-boot:run

# 方式二：打包后运行
mvn clean package -DskipTests
java -jar target/bus-service-*.jar
```

启动后服务运行在 **http://localhost:8080**。

## 默认账号

| 账号 | 密码 | 角色 | 权限 |
|------|------|------|------|
| admin | 123456 | 系统管理员 (ADMIN) | 全部权限，含增删改站点/线路/车辆、管理用户、查看操作日志 |
| safety | 123456 | 安全员 (SAFETY) | 查看数据、上报告警、处理告警；不可增删改基础数据 |

## 项目结构

```
com.autonomous.bus
├── BusApplication.java          # 启动类
├── annotation/
│   └── OperLog.java              # 操作日志注解
├── aspect/
│   └── OperLogAspect.java        # AOP 操作日志切面
├── config/
│   ├── CorsConfig.java           # 跨域配置
│   ├── MybatisPlusConfig.java    # 分页插件配置
│   ├── JwtInterceptor.java       # JWT 认证拦截器
│   ├── WebConfig.java            # 拦截器注册
│   └── RedisConfig.java          # Redis 序列化配置
├── controller/                   # REST 控制器
│   ├── UserController.java       # 登录/登出/用户管理
│   ├── StationController.java    # 站点管理
│   ├── RouteController.java      # 线路管理
│   ├── BusController.java        # 公交车管理
│   ├── AlarmController.java      # 告警上报/处理/删除
│   ├── AlarmTypeController.java  # 告警类型管理
│   ├── OperationLogController.java # 操作日志查询/删除
│   ├── ScheduleController.java   # 排班计划管理
│   ├── DispatchController.java   # 派车/临时调度
│   └── TripController.java       # 车次任务管理
├── dto/                          # 数据传输对象
│   ├── LoginDTO.java
│   ├── RouteDTO.java
│   ├── AlarmHandleDTO.java
│   ├── ScheduleBatchDTO.java     # 批量排班参数
│   └── DispatchDTO.java          # 派车/临时调度参数
├── entity/                       # 数据库实体
│   ├── BusSchedule.java          # 排班计划
│   └── TripRecord.java           # 车次记录
├── mapper/                       # MyBatis-Plus Mapper 接口
├── service/                      # 业务接口
│   ├── ScheduleService.java      # 排班业务（含批量生成）
│   ├── TripService.java          # 车次/派车/临时调度业务
│   └── impl/                     # 业务实现
│       └── TokenServiceImpl.java # Redis Token 管理
├── exception/
│   └── GlobalExceptionHandler.java # 全局异常处理
└── util/
    ├── Result.java               # 统一响应格式
    └── JwtUtil.java              # JWT 工具类
```

## 核心功能

### 认证授权
- 登录成功生成 JWT Token，同时存入 Redis（key: `auth:token:{userId}`，TTL 24 小时）
- 每次请求经 `JwtInterceptor` 校验：JWT 有效性 + Redis 中 Token 是否匹配
- 登出时删除 Redis 中的 Token，立即失效
- RBAC 角色控制：`ADMIN` 可增删改站点/线路/车辆/告警类型/用户；`SAFETY` 仅可查看和处理告警

### 操作审计日志
- 通过自定义注解 `@OperLog(module="模块名", operation="操作描述")` 标注需要记录的方法
- AOP 切面自动记录：操作人、模块、操作类型、请求参数、IP、耗时、执行状态
- 覆盖模块：系统管理（登录/登出）、站点管理、线路管理、公交车管理、告警管理、告警类型管理、排班计划管理、派车调度、临时调度、车次任务管理
- 仅 ADMIN 可删除和清空操作日志

### 运营调度模块（核心业务）

- **排班计划管理**：选择线路、方向（去程/返程）设置发车时间；支持手动单条排班和批量生成（输入首班、末班、发车间隔，自动批量生成多条排班）。排班保存后状态为"待发车"。
- **自动派车**：后端 `@Scheduled` 定时任务（每 5 秒）扫描排班表，到达发车时间且状态为"待发车"时，自动筛选空闲（待命）、归属该线路、电量充足（≥20%）的车辆完成派车：绑定车辆、新建车次、车辆状态改为运营。无空闲车辆时标记为"待派车"。
- **手动派车**：调度员从待发车排班中挑选，手动选择空闲车辆派车，与自动派车执行相同的数据更新逻辑，并记录操作日志。
- **临时调度**：
  - 临时加派：突发客流时不依赖预设排班，直接临时创建车次并派车发车；
  - 车辆下线：选中运营中车辆强制下线，停止自动行驶、车辆改待命、当前车次标记"中断"并写入原因；
  - 线路临时停运：整条线路暂停，该线路所有待发车排班置为"已停运"；
  - 车辆接替（应急调度）：车辆故障告警时，调度员选择备用待命车辆接替后续班次。
- **车次任务管理**：查看所有车次（线路、车辆、发车/预计到达/实际到达、状态）；车次详情包含该趟车途经站点及到站记录；跑完一趟可手动标记完成，定时任务也会自动完成到点的车次。

## API 概览

所有接口前缀 `/api`，除登录外均需在 Header 携带：
```
Authorization: Bearer {token}
```

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 认证 | POST | `/api/user/login` | 登录 |
| 认证 | POST | `/api/user/logout` | 登出 |
| 用户 | GET | `/api/user/page` | 分页查询用户 |
| 用户 | POST/PUT/DELETE | `/api/user` | 增删改用户（ADMIN） |
| 站点 | GET | `/api/station/page` | 分页查询站点 |
| 站点 | POST/PUT/DELETE | `/api/station` | 增删改站点（ADMIN） |
| 线路 | GET | `/api/route/page` | 分页查询线路 |
| 线路 | POST/PUT/DELETE | `/api/route` | 增删改线路（ADMIN） |
| 车辆 | GET | `/api/bus/page` | 分页查询车辆 |
| 车辆 | POST/PUT/DELETE | `/api/bus` | 增删改车辆（ADMIN） |
| 告警 | GET | `/api/alarm/page` | 分页查询告警 |
| 告警 | POST | `/api/alarm/report` | 上报告警 |
| 告警 | POST | `/api/alarm/handle` | 处理告警 |
| 告警 | DELETE | `/api/alarm/{id}` | 删除告警 |
| 告警类型 | GET | `/api/alarm-type/list` | 查询全部告警类型 |
| 告警类型 | POST/PUT/DELETE | `/api/alarm-type` | 增删改（ADMIN） |
| 操作日志 | GET | `/api/operation-log/page` | 分页查询日志 |
| 操作日志 | DELETE | `/api/operation-log/{id}` | 删除日志（ADMIN） |
| 操作日志 | DELETE | `/api/operation-log/clean` | 清空日志（ADMIN） |
| 仪表盘 | GET | `/api/alarm/stats` | 统计数据 |
| 排班 | GET | `/api/schedule/page` | 分页查询排班（支持按线路/方向/状态筛选） |
| 排班 | POST | `/api/schedule/single` | 手动新增单条排班 |
| 排班 | POST | `/api/schedule/batch` | 批量生成排班（首末班+发车间隔） |
| 排班 | DELETE | `/api/schedule/{id}` | 删除排班 |
| 派车 | GET | `/api/dispatch/ready-buses` | 查询空闲（待命）车辆 |
| 派车 | POST | `/api/dispatch/manual` | 手动派车 |
| 派车 | POST | `/api/dispatch/temp` | 临时加派班次 |
| 派车 | POST | `/api/dispatch/offline` | 车辆强制下线 |
| 派车 | POST | `/api/dispatch/suspend` | 线路临时停运 |
| 派车 | POST | `/api/dispatch/replace` | 车辆接替（应急调度） |
| 车次 | GET | `/api/trip/page` | 分页查询车次 |
| 车次 | GET | `/api/trip/{id}` | 车次详情（含途经站点） |
| 车次 | GET | `/api/trip/running` | 查询运行中车次 |
| 车次 | POST | `/api/trip/complete/{id}` | 完成车次 |

### 统一响应格式

```json
{
  "code": 200,
  "msg": "success",
  "data": {}
}
```

## 注意事项

1. **必须启动 Redis**，否则登录和 Token 校验会失败
2. 后端默认端口 8080，前端 Vite 开发服务器代理 `/api` 到 `http://127.0.0.1:8080`
3. 数据库密码在 `application.yml` 中配置，按需修改
4. JWT 密钥和过期时间也在 `application.yml` 中配置
