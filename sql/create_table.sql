/*
 Navicat Premium Data Transfer

 Source Server         : localhost3306
 Source Server Type    : MySQL
 Source Server Version : 80042
 Source Host           : localhost:3306
 Source Schema         : yu_picture

 Target Server Type    : MySQL
 Target Server Version : 80042
 File Encoding         : 65001

 Date: 02/03/2026 19:13:57
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for picture
-- ----------------------------
DROP TABLE IF EXISTS `picture`;
CREATE TABLE `picture`  (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
                            `url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图片 url',
                            `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图片名称',
                            `introduction` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '简介',
                            `category` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类',
                            `tags` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签（JSON 数组）',
                            `picSize` bigint NULL DEFAULT NULL COMMENT '图片体积',
                            `picWidth` int NULL DEFAULT NULL COMMENT '图片宽度',
                            `picHeight` int NULL DEFAULT NULL COMMENT '图片高度',
                            `picScale` double NULL DEFAULT NULL COMMENT '图片宽高比例',
                            `picFormat` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图片格式',
                            `userId` bigint NOT NULL COMMENT '创建用户 id',
                            `spaceId` bigint NULL DEFAULT NULL COMMENT '空间 id（null 表示公共图库）',
                            `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `editTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
                            `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            `isDelete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
                            `reviewStatus` int NOT NULL DEFAULT 0 COMMENT '审核状态：0-待审核; 1-通过; 2-拒绝',
                            `reviewMessage` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审核信息',
                            `reviewerId` bigint NULL DEFAULT NULL COMMENT '审核人 ID',
                            `reviewTime` datetime NULL DEFAULT NULL COMMENT '审核时间',
                            PRIMARY KEY (`id`) USING BTREE,
                            INDEX `idx_name`(`name` ASC) USING BTREE,
                            INDEX `idx_introduction`(`introduction` ASC) USING BTREE,
                            INDEX `idx_category`(`category` ASC) USING BTREE,
                            INDEX `idx_tags`(`tags` ASC) USING BTREE,
                            INDEX `idx_userId`(`userId` ASC) USING BTREE,
                            INDEX `idx_spaceId`(`spaceId` ASC) USING BTREE,
                            INDEX `idx_reviewStatus`(`reviewStatus` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1971113529569923073 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '图片' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of picture
-- ----------------------------
INSERT INTO `picture` VALUES (1964934738824830978, 'https://your-bucket.cos.your-region.myqcloud.com//public/1948972395805130754/2025-09-08_AfKkk2TMWDUGcWrf.png', 'appxz', NULL, NULL, NULL, 2389, 70, 70, 1, 'PNG', 1948972395805130754, '2025-09-08 14:12:03', '2025-09-08 14:12:03', '2025-09-08 14:12:03', 0, 0, NULL, NULL, NULL);
INSERT INTO `picture` VALUES (1971113529569923073, 'https://your-bucket.cos.your-region.myqcloud.com//public/1971109763051376642/2025-09-25_0fuDQttNESOzrsas.png', '警徽', '警徽', '警徽', '[\"警徽\"]', 782, 14, 14, 1, 'PNG', 1971109763051376642, '2025-09-25 15:24:21', '2025-09-25 15:24:42', '2025-09-25 15:24:42', 0, 0, NULL, NULL, NULL);
INSERT INTO `picture` VALUES (2028389517122772993, 'https://your-bucket.cos.your-region.myqcloud.com//public/1948972395805130754/2026-03-02_60JBbqEo37tFzi1e.jpg', '微信图片_20251225160857_10044_3', NULL, '素材', '[\"艺术\"]', 880144, 1440, 2160, 0.67, 'JPEG', 1948972395805130754, '2026-03-02 16:38:41', '2026-03-02 16:39:01', '2026-03-02 16:39:01', 0, 1, '管理员自动过审', 1948972395805130754, '2026-03-02 16:39:01');
INSERT INTO `picture` VALUES (2028391611879825410, 'https://your-bucket.cos.your-region.myqcloud.com//public/2028391468669509634/2026-03-02_H73LAPp5DNThrgQk.jpg', '微信图片_20251225160856_10043_3', NULL, '素材', '[\"艺术\"]', 1124654, 1440, 1920, 0.75, 'JPEG', 2028391468669509634, '2026-03-02 16:47:00', '2026-03-02 16:47:11', '2026-03-02 16:47:11', 0, 0, NULL, NULL, NULL);
INSERT INTO `picture` VALUES (2028392260537327618, 'https://your-bucket.cos.your-region.myqcloud.com//public/2028391468669509634/2026-03-02_VexquBM0reWEY2MJ.jpg', '微信图片_20251225160856_10043_3', NULL, '素材', '[\"高清\",\"艺术\"]', 1124654, 1440, 1920, 0.75, 'JPEG', 2028391468669509634, '2026-03-02 16:49:35', '2026-03-02 16:49:52', '2026-03-02 16:49:51', 0, 0, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
                         `userAccount` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '账号',
                         `userPassword` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
                         `userName` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户昵称',
                         `userAvatar` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户头像',
                         `userProfile` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户简介',
                         `userRole` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'user' COMMENT '用户角色：user/admin',
                         `editTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
                         `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                         `isDelete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
                         PRIMARY KEY (`id`) USING BTREE,
                         UNIQUE INDEX `uk_userAccount`(`userAccount` ASC) USING BTREE,
                         INDEX `idx_userName`(`userName` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1971109763051376642 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1948972395805130754, 'doudou', 'b0dd3697a192885d7c055db46155b26a', 'doudou', NULL, NULL, 'admin', '2025-07-26 13:03:23', '2025-07-26 13:03:23', '2025-09-05 09:38:34', 0);
INSERT INTO `user` VALUES (1971109763051376642, 'ceshi', '52862e8392390e23077f63b1adf44d11', '无名', NULL, NULL, 'user', '2025-09-25 15:09:23', '2025-09-25 15:09:23', '2025-09-25 15:09:23', 0);
INSERT INTO `user` VALUES (2028391468669509634, 'ceshi2', 'b0dd3697a192885d7c055db46155b26a', '无名', NULL, NULL, 'user', '2026-03-02 16:46:26', '2026-03-02 16:46:26', '2026-03-02 16:46:26', 0);

SET FOREIGN_KEY_CHECKS = 1;
