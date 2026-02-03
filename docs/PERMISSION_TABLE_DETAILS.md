# 权限系统表结构详细说明

## 表结构速查

| 表名 | 用途 | 关键字段 | 主要查询 |
|-----|------|--------|--------|
| `permission_type` | 权限类型字典 | permission_code, permission_name | 系统初始化时加载 |
| `topic` | Topic元数据 | topic_name, cluster_name, owner_id | 创建Topic时记录 |
| `permission_request` | 权限申请历史 | user_id, topic_id, permission_code, status | 用户申请查询、审批流程 |
| `user_permission` | 最终权限映射 | user_id, topic_id, permission_code, status | **权限检查（最常用）** |
| `permission_audit_log` | 审计日志 | operation_type, operator_id, created_time | 权限操作追踪 |

---

## 详细表结构说明

### 1. permission_type（权限类型表）

**作用**：定义系统支持的所有权限种类，类似于一个权限字典

**关键字段**：

| 字段名 | 类型 | 说明 | 示例 |
|-------|------|-----|------|
| id | INT | 主键 | 1, 2, 3... |
| permission_code | VARCHAR(50) | 权限代码（唯一） | `PRODUCE`, `CONSUME` |
| permission_name | VARCHAR(100) | 权限名称 | "生产消息", "消费消息" |
| description | VARCHAR(300) | 详细描述 | "允许向Topic生产消息" |
| status | VARCHAR(20) | 权限状态 | `ACTIVE`, `INACTIVE` |

**示例数据**：
```
id | permission_code | permission_name | description
1  | CREATE_TOPIC   | 创建Topic      | 允许创建新的Topic
2  | DELETE_TOPIC   | 删除Topic      | 允许删除Topic
3  | PRODUCE        | 生产消息      | 允许向Topic生产消息
4  | CONSUME        | 消费消息      | 允许从Topic消费消息
5  | MODIFY_CONFIG  | 修改配置      | 允许修改Topic配置
```

---

### 2. topic（Topic表）

**作用**：记录RocketMQ中存在的所有Topic元数据，用于权限管理

**关键字段**：

| 字段名 | 类型 | 说明 | 示例 |
|-------|------|-----|------|
| id | BIGINT | 主键 | 1, 2, 3... |
| topic_name | VARCHAR(255) | Topic名称（唯一） | `Order-Topic`, `Payment-Topic` |
| cluster_name | VARCHAR(255) | 所属集群 | `DefaultCluster` |
| owner_id | BIGINT | 所有者用户ID | 1（admin） |
| owner_name | VARCHAR(100) | 所有者名称 | `admin` |
| is_system_topic | TINYINT | 是否系统Topic | 0=普通, 1=系统 |
| status | VARCHAR(20) | 状态 | `ACTIVE`, `DELETED` |

**示例数据**：
```
id | topic_name    | cluster_name    | owner_id | owner_name | status
1  | Order-Topic   | DefaultCluster  | 1        | admin      | ACTIVE
2  | Payment-Topic | DefaultCluster  | 1        | admin      | ACTIVE
3  | User-Topic    | DefaultCluster  | 1        | admin      | ACTIVE
```

---

### 3. permission_request（权限申请表）

**作用**：记录用户的权限申请、审批过程和结果

**关键字段**：

| 字段名 | 类型 | 说明 | 示例 |
|-------|------|-----|------|
| id | BIGINT | 主键 | 1, 2, 3... |
| user_id | BIGINT | 申请用户ID | 2（system_a） |
| user_name | VARCHAR(100) | 申请用户名 | `system_a` |
| topic_id | BIGINT | Topic ID | 1（Order-Topic） |
| topic_name | VARCHAR(255) | Topic名称 | `Order-Topic` |
| permission_code | VARCHAR(50) | 申请的权限 | `PRODUCE`, `CONSUME` |
| request_reason | VARCHAR(500) | 申请理由 | "系统A需要生产订单消息" |
| status | VARCHAR(20) | 申请状态 | `PENDING` → `APPROVED`/`REJECTED` |
| approver_id | BIGINT | 审批人ID | 1（admin） |
| approval_reason | VARCHAR(500) | 审批意见 | "同意" |
| approval_time | TIMESTAMP | 审批时间 | 2026-02-04 10:00:00 |
| expire_time | TIMESTAMP | 权限过期时间 | NULL（永久）或具体时间 |

**示例数据**：
```
id | user_id | user_name | topic_id | topic_name  | permission_code | status   | approver_id | approval_time
1  | 2       | system_a  | 1        | Order-Topic | PRODUCE         | APPROVED | 1           | 2026-02-04 10:00:00
2  | 3       | system_b  | 1        | Order-Topic | CONSUME         | APPROVED | 1           | 2026-02-04 10:05:00
3  | 2       | system_a  | 2        | Payment-Topic | PRODUCE       | PENDING  | NULL        | NULL
```

**申请状态流转**：
```
PENDING (申请中) 
  ├─→ APPROVED (已批准) → 权限生效
  ├─→ REJECTED (已拒绝) → 权限不生效
  ├─→ EXPIRED (已过期) → 自动标记过期
  └─→ WITHDRAWN (已撤回) → 用户主动撤回
```

---

### 4. user_permission（用户权限关系表）✨ **最重要**

**作用**：存储**最终的、已生效的权限**，权限检查时直接查询此表

**关键字段**：

| 字段名 | 类型 | 说明 | 示例 |
|-------|------|-----|------|
| id | BIGINT | 主键 | 1, 2, 3... |
| user_id | BIGINT | 用户ID | 2（system_a） |
| user_name | VARCHAR(100) | 用户名 | `system_a` |
| topic_id | BIGINT | Topic ID | 1（Order-Topic） |
| topic_name | VARCHAR(255) | Topic名称 | `Order-Topic` |
| permission_code | VARCHAR(50) | 权限代码 | `PRODUCE` |
| granted_time | TIMESTAMP | 授权时间 | 2026-02-04 10:00:00 |
| expire_time | TIMESTAMP | 过期时间 | NULL=永久 |
| is_expired | TINYINT | 是否过期 | 0=未过期, 1=已过期 |
| status | VARCHAR(20) | 权限状态 | `ACTIVE`, `REVOKED`, `EXPIRED` |

**示例数据**：
```
id | user_id | user_name | topic_id | topic_name  | permission_code | status | expire_time | is_expired
1  | 2       | system_a  | 1        | Order-Topic | PRODUCE         | ACTIVE | NULL        | 0
2  | 3       | system_b  | 1        | Order-Topic | CONSUME         | ACTIVE | NULL        | 0
```

**权限检查SQL**：
```sql
-- 检查用户user_id是否有对topic_id的PRODUCE权限
SELECT * FROM user_permission 
WHERE user_id = 2 
  AND topic_id = 1 
  AND permission_code = 'PRODUCE'
  AND status = 'ACTIVE' 
  AND (expire_time IS NULL OR expire_time > NOW())
LIMIT 1;
```

**关键特性**：
- ✓ 使用 **UNIQUE约束** `(user_id, topic_id, permission_type_id)` 防止权限重复
- ✓ 支持权限过期时间自动更新 `is_expired` 标志
- ✓ 支持权限撤销（REVOKED）
- ✓ 查询性能最优化（建议加缓存）

---

### 5. permission_audit_log（审计日志表）

**作用**：记录所有权限相关操作的日志，用于审计和追踪

**关键字段**：

| 字段名 | 类型 | 说明 | 示例 |
|-------|------|-----|------|
| id | BIGINT | 主键 | 1, 2, 3... |
| operation_type | VARCHAR(50) | 操作类型 | `REQUEST`, `APPROVE`, `REJECT`, `GRANT`, `REVOKE` |
| operator_id | BIGINT | 操作人ID | 1（admin） |
| operator_name | VARCHAR(100) | 操作人名称 | `admin` |
| target_user_id | BIGINT | 目标用户ID | 2（system_a） |
| target_user_name | VARCHAR(100) | 目标用户名 | `system_a` |
| topic_id | BIGINT | Topic ID | 1 |
| permission_code | VARCHAR(50) | 权限代码 | `PRODUCE` |
| operation_result | VARCHAR(20) | 操作结果 | `SUCCESS`, `FAILED` |
| created_time | TIMESTAMP | 操作时间 | 2026-02-04 10:00:00 |

**示例数据**：
```
id | operation_type | operator_id | operator_name | target_user_id | target_user_name | permission_code | created_time
1  | REQUEST        | 2           | system_a      | 2              | system_a         | PRODUCE         | 2026-02-04 09:00:00
2  | APPROVE        | 1           | admin         | 2              | system_a         | PRODUCE         | 2026-02-04 10:00:00
3  | GRANT          | 1           | admin         | 2              | system_a         | PRODUCE         | 2026-02-04 10:00:01
```

---

## 核心业务流程

### 流程1：用户申请权限

```
用户提交申请
  ↓
创建 permission_request 行
  - status = PENDING
  - approver_id = NULL
  ↓
记录 permission_audit_log
  - operation_type = REQUEST
  ↓
等待管理员审批
```

### 流程2：管理员批准权限

```
管理员审批申请
  ↓
更新 permission_request
  - status = APPROVED
  - approver_id = 管理员ID
  - approval_time = NOW()
  ↓
创建 user_permission 行
  - status = ACTIVE
  - granted_time = NOW()
  ↓
记录 permission_audit_log
  - operation_type = APPROVE
  - operation_type = GRANT
  ↓
权限立即生效
```

### 流程3：权限检查（最常用）

```
用户执行操作（如生产消息）
  ↓
检查权限：
  SELECT * FROM user_permission
  WHERE user_id = ?
    AND topic_id = ?
    AND permission_code = ?
    AND status = 'ACTIVE'
    AND (expire_time IS NULL OR expire_time > NOW())
  ↓
权限存在 ✓ → 允许操作
权限不存在 ✗ → 拒绝操作，返回权限不足错误
  ↓
记录 permission_audit_log
  - operation_type = 实际的操作类型
```

---

## 字段设计说明

### 为什么需要冗余字段（如 topic_name）？

```sql
-- 冗余字段的好处：
-- 1. 即使Topic被删除，申请记录仍可显示Topic名称
-- 2. 避免多表JOIN查询，提升查询性能
-- 3. 审计日志中保留原始信息
```

### 为什么 expire_time 在多个表中出现？

- `permission_request.expire_time` - 申请时指定的过期时间
- `user_permission.expire_time` - 权限的过期时间
- 分离的原因：申请可能被拒绝，只有已授予的权限才真正有过期概念

---

## 性能优化建议

### 索引策略

```sql
-- 权限检查最常用的查询
ALTER TABLE user_permission ADD INDEX idx_user_active (user_id, status);
ALTER TABLE user_permission ADD INDEX idx_permission_active (permission_code, status);

-- 权限申请查询
ALTER TABLE permission_request ADD INDEX idx_user_status (user_id, status, created_time);
ALTER TABLE permission_request ADD INDEX idx_approver_status (approver_id, status);

-- 审计日志查询
ALTER TABLE permission_audit_log ADD INDEX idx_operation_time (operation_type, created_time DESC);
```

### 缓存策略

```
建议：缓存 user_permission 表到 Redis
- Key: user:{user_id}:permissions
- Value: List<{topic_id, permission_code}>
- TTL: 5 分钟或权限变更时立即更新
```

---

## 常用SQL查询示例

### 查询用户的所有权限

```sql
SELECT up.*, t.topic_name, pt.permission_name
FROM user_permission up
LEFT JOIN topic t ON up.topic_id = t.id
LEFT JOIN permission_type pt ON up.permission_type_id = pt.id
WHERE up.user_id = 2
  AND up.status = 'ACTIVE'
  AND (up.expire_time IS NULL OR up.expire_time > NOW());
```

### 查询Topic的所有权限持有者

```sql
SELECT up.*, u.username
FROM user_permission up
LEFT JOIN user u ON up.user_id = u.id
WHERE up.topic_id = 1
  AND up.status = 'ACTIVE';
```

### 查询待审批的申请

```sql
SELECT pr.*, pt.permission_name
FROM permission_request pr
LEFT JOIN permission_type pt ON pr.permission_type_id = pt.id
WHERE pr.status = 'PENDING'
ORDER BY pr.created_time ASC;
```

### 检查用户是否有权限（权限检查的核心SQL）

```sql
SELECT COUNT(*) > 0 AS has_permission
FROM user_permission
WHERE user_id = 2
  AND topic_id = 1
  AND permission_code = 'PRODUCE'
  AND status = 'ACTIVE'
  AND (expire_time IS NULL OR expire_time > NOW());
```

---

## 下一步行动

1. ✓ **表结构设计完成** ← 当前
2. **创建Entity/Model类** - 根据表结构创建Java实体类
3. **实现PermissionService** - 权限业务逻辑服务
4. **集成权限检查** - 在各Service操作前检查权限
5. **创建PermissionController** - REST API接口
6. **编写单元测试** - 权限检查的各种场景

