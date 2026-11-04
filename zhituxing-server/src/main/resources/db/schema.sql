-- =============================================================
-- 智途星智能旅游规划平台 数据库初始化脚本（MySQL 8.0）
-- 执行方式：mysql -u root -p < schema.sql
-- 与《数据库设计文档》表结构保持一致，另含接口先行约定的收藏表 favorites
-- =============================================================

CREATE DATABASE IF NOT EXISTS zhituxing
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE zhituxing;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(32)  NOT NULL COMMENT '用户名',
    phone      VARCHAR(11)  NOT NULL COMMENT '手机号',
    password   VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码',
    avatar     VARCHAR(255)          COMMENT '头像 URL',
    signature  VARCHAR(255)          COMMENT '个性签名',
    role       VARCHAR(16)  NOT NULL DEFAULT 'user' COMMENT 'user/admin',
    status     VARCHAR(8)   NOT NULL DEFAULT '正常' COMMENT '正常/禁用',
    created_at DATETIME              DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_phone (phone),
    UNIQUE KEY uk_name (name)
) ENGINE = InnoDB COMMENT = '用户表';

-- 景点表
CREATE TABLE IF NOT EXISTS spots (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    name          VARCHAR(64)   NOT NULL COMMENT '景点名称',
    city          VARCHAR(32)   NOT NULL COMMENT '城市',
    category      VARCHAR(16)   NOT NULL COMMENT '自然风光/历史文化',
    price         DECIMAL(10, 2) NOT NULL COMMENT '门票价格（元）',
    rating        DECIMAL(2, 1)          COMMENT '评分',
    cover         VARCHAR(255)           COMMENT '封面图（/uploads 相对路径）',
    description   TEXT                    COMMENT '详细介绍',
    address       VARCHAR(255)           COMMENT '地址',
    opening_hours VARCHAR(32)            COMMENT '开放时间',
    status        VARCHAR(8)    NOT NULL DEFAULT '上架' COMMENT '上架/下架',
    audit_status  VARCHAR(8)    NOT NULL DEFAULT '已通过' COMMENT '待审核/已通过',
    created_at    DATETIME               DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME               DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_city (city),
    KEY idx_category (category)
) ENGINE = InnoDB COMMENT = '景点表';

-- 热门目的地表
CREATE TABLE IF NOT EXISTS destinations (
    id        BIGINT      NOT NULL AUTO_INCREMENT,
    name      VARCHAR(32) NOT NULL COMMENT '目的地名称',
    trip_days INT         NOT NULL COMMENT '建议游玩天数',
    cover     VARCHAR(255)         COMMENT '封面图',
    PRIMARY KEY (id)
) ENGINE = InnoDB COMMENT = '热门目的地表';

-- 行程表
CREATE TABLE IF NOT EXISTS trips (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    user_id    BIGINT      NOT NULL,
    title      VARCHAR(64) NOT NULL COMMENT '行程名称',
    days       INT         NOT NULL COMMENT '行程天数',
    start_date DATE                 COMMENT '出发日期',
    end_date   DATE                 COMMENT '结束日期',
    status     VARCHAR(8)  NOT NULL DEFAULT '计划中' COMMENT '计划中/进行中/已完成',
    created_at DATETIME             DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_trip_user (user_id)
) ENGINE = InnoDB COMMENT = '行程表';

-- 行程-景点关联表
CREATE TABLE IF NOT EXISTS trip_spots (
    id      BIGINT NOT NULL AUTO_INCREMENT,
    trip_id BIGINT NOT NULL,
    spot_id BIGINT NOT NULL,
    day_no  INT    NOT NULL DEFAULT 1 COMMENT '第几天',
    sort_no INT    NOT NULL DEFAULT 0 COMMENT '当日排序',
    PRIMARY KEY (id),
    UNIQUE KEY uk_trip_spot (trip_id, spot_id)
) ENGINE = InnoDB COMMENT = '行程景点关联表';

-- 社区帖子表
CREATE TABLE IF NOT EXISTS posts (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    title          VARCHAR(100) NOT NULL,
    content        TEXT         NOT NULL,
    author_id      BIGINT       NOT NULL,
    board          VARCHAR(16)           COMMENT '景点分享/人文地理/出行攻略',
    images         TEXT                 COMMENT '配图 URL 列表（JSON）',
    views          INT          NOT NULL DEFAULT 0,
    comments_count INT          NOT NULL DEFAULT 0,
    date           DATETIME             DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    PRIMARY KEY (id),
    KEY idx_post_author (author_id)
) ENGINE = InnoDB COMMENT = '社区帖子表';

-- 评论表（帖子评论与景点评论共用）
CREATE TABLE IF NOT EXISTS comments (
    id      BIGINT        NOT NULL AUTO_INCREMENT,
    post_id BIGINT                 COMMENT '帖子评论（与 spot_id 二选一）',
    spot_id BIGINT                 COMMENT '景点评论（与 post_id 二选一）',
    user_id BIGINT        NOT NULL,
    content VARCHAR(500)  NOT NULL,
    rating  DECIMAL(2, 1)          COMMENT '景点评论评分 1-5',
    status  VARCHAR(8)    NOT NULL DEFAULT '待审' COMMENT '通过/待审/删除',
    date    DATETIME               DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_comment_post (post_id),
    KEY idx_comment_spot (spot_id)
) ENGINE = InnoDB COMMENT = '评论表';

-- 旅游笔记表
CREATE TABLE IF NOT EXISTS notes (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    user_id    BIGINT       NOT NULL,
    title      VARCHAR(100) NOT NULL,
    content    TEXT,
    created_at DATETIME              DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_note_user (user_id)
) ENGINE = InnoDB COMMENT = '旅游笔记表';

-- 图形验证码表
CREATE TABLE IF NOT EXISTS captchas (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    captcha_key VARCHAR(64) NOT NULL,
    code        VARCHAR(8)  NOT NULL,
    expires_at  DATETIME    NOT NULL,
    created_at  DATETIME             DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_captcha_key (captcha_key)
) ENGINE = InnoDB COMMENT = '图形验证码表';

-- 收藏表（接口文档 V1.2 先行约定）
CREATE TABLE IF NOT EXISTS favorites (
    id         BIGINT   NOT NULL AUTO_INCREMENT,
    user_id    BIGINT   NOT NULL,
    spot_id    BIGINT   NOT NULL,
    created_at DATETIME          DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_favorite (user_id, spot_id)
) ENGINE = InnoDB COMMENT = '景点收藏表';

-- =============================================================
-- 演示种子数据（可选执行）
-- 说明：新库执行完建表后可按需导入，便于直接体验与演示。
--   帖子/评论日期采用相对时间表达式（NOW() - INTERVAL N DAY），
--   保证重建后“近 7 日/近 30 日内容趋势”始终有数据可展示；
--   评分与作者已做多样化处理（4.0~5.0，多位演示用户）。
-- 注意：仅适用于空库首次导入；重复导入会因主键冲突报错。
-- =============================================================

-- 演示用户（密码均为 123456 的 BCrypt 密文）
INSERT INTO users (id, name, phone, password, role, status, created_at) VALUES
(1, 'testuser', '18800000001', '$2a$10$PRygPCj1jQYa/KStL9eSyebdMrPdbuV6hTod/IsI2TWusz2Zwwf5e', 'admin', '正常', NOW()),
(2, '王贝贝', '18888888888', '$2a$10$PRygPCj1jQYa/KStL9eSyebdMrPdbuV6hTod/IsI2TWusz2Zwwf5e', 'user', '正常', NOW()),
(3, '李珊珊', '18899999999', '$2a$10$PRygPCj1jQYa/KStL9eSyebdMrPdbuV6hTod/IsI2TWusz2Zwwf5e', 'user', '正常', NOW());

-- 演示景点（评分多样化）
INSERT INTO spots (id, name, city, category, price, rating, cover, description, address, opening_hours, status, audit_status, created_at) VALUES
(1, '金佛山', '重庆', '自然风光', 100.00, 4.8, NULL, '金佛山风景区，集山、林、泉、洞、雪景于一体，是重庆著名的赏雪与避暑胜地，山顶云海与雪原令人惊叹。', '重庆市南川区', '08:00-17:00', '上架', '已通过', NOW()),
(2, '喀斯特', '重庆', '自然风光', 150.00, 4.6, NULL, '世界自然遗产地，集山、水、林、洞、瀑于一体，是重庆最负盛名的喀斯特地貌景区，天生三桥与芙蓉洞尤为壮观。', '重庆市武隆区', '08:00-17:30', '上架', '已通过', NOW()),
(3, '大足石刻', '重庆', '历史文化', 100.00, 4.7, NULL, '世界文化遗产，中国晚期石窟艺术的代表，以宝顶山、北山摩崖造像最为集中，造像精美、题材丰富。', '重庆市大足区', '08:30-18:00', '上架', '已通过', NOW()),
(4, '故宫', '北京', '历史文化', 80.00, 4.9, NULL, '明清两代皇家宫殿，世界现存规模最大的木质结构古建筑群，馆藏文物逾百万件，四季皆宜游览。', '北京市东城区景山前街4号', '08:30-17:00', '上架', '已通过', NOW());

-- 热门目的地
INSERT INTO destinations (id, name, trip_days, cover) VALUES
(1, '重庆', 3, NULL),
(2, '成都', 3, NULL),
(3, '敦煌', 5, NULL);

-- 演示帖子（日期落在近 30 日）
INSERT INTO posts (id, title, content, author_id, board, views, comments_count, date) VALUES
(1, '川渝携手"花式"迎客', '川渝两地文旅部门联合推出系列文旅活动，这个秋冬，一起"花式"打卡成渝双城。', 2, '景点分享', 1024, 2, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, '重庆拟建设大型旅游度假区', '规划中的大型旅游度假区将落地重庆，涵盖主题乐园、度假酒店与生态公园，值得期待。', 2, '人文地理', 856, 1, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(3, '磁器口古镇十二巷', '磁器口古镇全新打造"十二巷"文旅空间，老重庆的烟火气与新业态在此交融。', 2, '出行攻略', 668, 1, DATE_SUB(NOW(), INTERVAL 12 DAY)),
(4, '五一错峰出行指南', '五一假期人从众？收好这份错峰出行指南，提前订票、错峰入园，玩得更从容。', 3, '出行攻略', 1520, 2, DATE_SUB(NOW(), INTERVAL 21 DAY));

-- 演示评论（景点评论带评分、作者多样；帖子评论按帖子归属）
INSERT INTO comments (id, post_id, spot_id, user_id, content, rating, status, date) VALUES
(1,  NULL, 1, 2, '风景绝美，天生三桥特别震撼，建议早上早点去避开人流。', 5.0, '通过', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2,  NULL, 1, 3, '玻璃栈道很刺激，山顶云海超值，门票不贵。', 4.5, '通过', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(3,  NULL, 1, 1, '秋天去的，红叶配雪景，出片率极高。', 4.0, '通过', DATE_SUB(NOW(), INTERVAL 8 DAY)),
(4,  NULL, 2, 2, '喀斯特地貌名不虚传，天生三桥值得专程前往。', 5.0, '通过', DATE_SUB(NOW(), INTERVAL 11 DAY)),
(5,  NULL, 3, 3, '石刻非常精美，讲解员讲得也好，推荐。', 4.5, '通过', DATE_SUB(NOW(), INTERVAL 15 DAY)),
(6,  NULL, 4, 1, '紫禁城的红墙金瓦，随手一拍都是大片。', 5.0, '通过', DATE_SUB(NOW(), INTERVAL 18 DAY)),
(7,  1, NULL, 2, '川渝的美食和风景都太顶了，期待联合线路！', NULL, '通过', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(8,  1, NULL, 3, '已经收藏，打算近期去重庆打卡。', NULL, '通过', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(9,  2, NULL, 2, '支持，重庆旅游要起飞了！', NULL, '通过', DATE_SUB(NOW(), INTERVAL 9 DAY)),
(10, 3, NULL, 2, '十二巷的小店很有味道，推荐晚上去。', NULL, '通过', DATE_SUB(NOW(), INTERVAL 13 DAY)),
(11, 4, NULL, 3, '错峰真的有用，按这份指南订的行程很顺。', NULL, '通过', DATE_SUB(NOW(), INTERVAL 20 DAY)),
(12, 4, NULL, 2, '补充一条：提前两周订酒店最划算。', NULL, '通过', DATE_SUB(NOW(), INTERVAL 25 DAY));

-- Additive private chat migration. Existing user data is retained.
CREATE TABLE IF NOT EXISTS chat_messages (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 sender_id BIGINT NOT NULL,
 recipient_id BIGINT NOT NULL,
 content VARCHAR(2000) NOT NULL,
 client_id VARCHAR(64) NOT NULL,
 created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
 read_at DATETIME(6) NULL,
 UNIQUE KEY uk_chat_sender_client(sender_id,client_id),
 KEY idx_chat_sender_recipient(sender_id,recipient_id,id),
 KEY idx_chat_unread(recipient_id,sender_id,read_at,id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
