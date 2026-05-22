# 图片表添加 spaceId 字段 - 修改总结

## 📋 修改概述

为 `picture` 表添加了 `spaceId` 字段，用于区分公共图库和私人空间。
- **spaceId = null**：表示公共图库
- **spaceId = 具体值**：表示属于某个私人空间

## 🔧 已完成的修改

### 1. 数据库层面

#### 1.1 实体类更新
**文件**: [Picture.java](file:///E:/code/yu-picture-backend/src/main/java/com/example/yupicturebackend/model/entity/Picture.java)
```java
/**
 * 空间 id（null 表示公共图库）
 */
private Long spaceId;
```

#### 1.2 SQL 建表脚本更新
**文件**: [create_table.sql](file:///E:/code/yu-picture-backend/sql/create_table.sql)
- 添加了 `spaceId` 字段定义
- 添加了 `idx_spaceId` 索引

#### 1.3 数据库迁移脚本
**文件**: [migration_add_spaceId_to_picture.sql](file:///E:/code/yu-picture-backend/sql/migration_add_spaceId_to_picture.sql)

用于在现有数据库中执行迁移：
```sql
ALTER TABLE `picture` 
ADD COLUMN `spaceId` bigint NULL DEFAULT NULL COMMENT '空间 id（null 表示公共图库）' AFTER `userId`;

ALTER TABLE `picture` 
ADD INDEX `idx_spaceId`(`spaceId` ASC) USING BTREE;
```

### 2. VO 和 DTO 层面

#### 2.1 PictureVO 更新
**文件**: [PictureVO.java](file:///E:/code/yupicture-backend/src/main/java/com/example/yupicturebackend/model/vo/PictureVO.java)
- 添加了 `spaceId` 字段
- `BeanUtils.copyProperties` 会自动处理字段拷贝

#### 2.2 PictureEditRequest 更新
**文件**: [PictureEditRequest.java](file:///E:/code/yu-picture-backend/src/main/java/com/example/yupicturebackend/model/dto/picture/PictureEditRequest.java)
- 添加了 `spaceId` 字段
- 用户可以在编辑图片时修改所属空间

#### 2.3 PictureQueryRequest 更新
**文件**: [PictureQueryRequest.java](file:///E:/code/yu-picture-backend/src/main/java/com/example/yupicturebackend/model/dto/picture/PictureQueryRequest.java)
- 添加了 `spaceId` 字段
- 支持按空间 ID 查询图片

#### 2.4 PictureUploadRequest 更新
**文件**: [PictureUploadRequest.java](file:///E:/code/yu-picture-backend/src/main/java/com/example/yupicturebackend/model/dto/picture/PictureUploadRequest.java)
- 添加了 `spaceId` 字段
- 上传时可以指定图片所属空间

### 3. Service 层面

#### 3.1 查询逻辑更新
**文件**: [PictureServiceImpl.java](file:///E:/code/yu-picture-backend/src/main/java/com/example/yupicturebackend/service/Impl/PictureServiceImpl.java)

在 `getQueryWrapper` 方法中添加了 spaceId 查询条件：
```java
Long spaceId = pictureQueryRequest.getSpaceId();
queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
```

#### 3.2 上传逻辑更新
在 `uploadPicture` 方法中添加了 spaceId 设置：
```java
// 设置空间 id（如果请求中指定了）
if (pictureUploadRequest != null) {
    picture.setSpaceId(pictureUploadRequest.getSpaceId());
}
```

#### 3.3 编辑逻辑
编辑接口已通过 `BeanUtils.copyProperties` 自动处理 spaceId 字段，无需额外修改。

## 📊 字段特性

| 属性 | 值 |
|------|-----|
| 字段名 | spaceId |
| 类型 | bigint |
| 默认值 | NULL |
| 可空 | 是 |
| 索引 | idx_spaceId |
| 注释 | 空间 id（null 表示公共图库） |

## 🎯 使用场景

### 1. 公共图库
```java
// 上传图片到公共图库
PictureUploadRequest request = new PictureUploadRequest();
request.setSpaceId(null); // 或不设置，默认为 null
```

### 2. 私人空间
```java
// 上传图片到指定空间
PictureUploadRequest request = new PictureUploadRequest();
request.setSpaceId(spaceId); // 设置具体的空间 ID
```

### 3. 查询公共图库
```java
// 查询公共图库的图片
PictureQueryRequest queryRequest = new PictureQueryRequest();
queryRequest.setSpaceId(null);
```

### 4. 查询私人空间
```java
// 查询指定空间的图片
PictureQueryRequest queryRequest = new PictureQueryRequest();
queryRequest.setSpaceId(spaceId);
```

### 5. 移动图片到不同空间
```java
// 编辑图片，更改所属空间
PictureEditRequest editRequest = new PictureEditRequest();
editRequest.setId(pictureId);
editRequest.setSpaceId(newSpaceId); // 设置为新的空间 ID
```

## 🚀 部署步骤

### 方式一：新建数据库
如果是全新部署，直接使用更新后的 `create_table.sql` 创建数据库即可。

### 方式二：现有数据库迁移
如果数据库中已有数据，执行迁移脚本：

```bash
# 连接到 MySQL
mysql -u root -p yu_picture

# 执行迁移脚本
source sql/migration_add_spaceId_to_picture.sql;
```

或者手动执行：
```sql
ALTER TABLE `picture` 
ADD COLUMN `spaceId` bigint NULL DEFAULT NULL COMMENT '空间 id（null 表示公共图库）' AFTER `userId`;

ALTER TABLE `picture` 
ADD INDEX `idx_spaceId`(`spaceId` ASC) USING BTREE;
```

## ✅ 验证清单

- [x] Picture 实体类添加 spaceId 字段
- [x] PictureVO 添加 spaceId 字段
- [x] PictureEditRequest 添加 spaceId 字段
- [x] PictureQueryRequest 添加 spaceId 字段
- [x] PictureUploadRequest 添加 spaceId 字段
- [x] SQL 建表脚本更新
- [x] 数据库迁移脚本创建
- [x] 查询逻辑支持 spaceId 过滤
- [x] 上传逻辑支持设置 spaceId
- [x] 编辑逻辑支持修改 spaceId
- [x] 添加数据库索引优化查询性能

## 💡 注意事项

1. **向后兼容**：由于 spaceId 默认为 NULL，现有的图片数据会自动归类为公共图库
2. **权限控制**：建议在业务逻辑中添加权限校验，确保用户只能访问自己有权限的空间
3. **索引优化**：已添加 idx_spaceId 索引，查询性能有保障
4. **数据迁移**：如果需要将现有图片分配到特定空间，需要编写额外的数据迁移脚本

## 🔍 后续建议

1. **权限校验**：在查询和编辑接口中添加空间权限校验
2. **空间关联**：考虑在 PictureVO 中关联返回 Space 信息
3. **批量操作**：支持批量移动图片到不同空间
4. **数据统计**：统计每个空间的图片数量

---

**修改完成时间**: 2026-05-15  
**影响范围**: 图片相关的上传、查询、编辑功能
