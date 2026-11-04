-- =============================================================
-- 智途星 演示种子数据修复（现有库执行，2026-11-02）
-- 目的：① 评论/帖子日期移到近 7/30 日窗口，让看板趋势有数据；
--       ② 景点评分与评论评分、作者多样化，提升演示真实感。
-- 注意：仅对既有演示库执行一次；新库初始化请直接使用 schema.sql 种子段。
-- =============================================================

USE zhituxing;

-- 1) 景点评分多样化（与对应评论评分呼应）
UPDATE spots SET rating = 4.8 WHERE id = 1; -- 金佛山
UPDATE spots SET rating = 4.6 WHERE id = 2; -- 喀斯特
UPDATE spots SET rating = 4.7 WHERE id = 3; -- 大足石刻
UPDATE spots SET rating = 4.9 WHERE id = 4; -- 故宫

-- 2) 帖子日期移到近 30 日（近 7 日窗口内保留 1-2 条）
UPDATE posts SET date = '2026-11-01 10:00:00' WHERE id = 1; -- 川渝携手"花式"迎客（近7日）
UPDATE posts SET date = '2026-10-27 11:00:00' WHERE id = 2; -- 重庆拟建设大型旅游度假区（近7日）
UPDATE posts SET date = '2026-10-21 14:00:00' WHERE id = 3; -- 磁器口古镇十二巷
UPDATE posts SET date = '2026-10-12 09:00:00' WHERE id = 4; -- 五一错峰出行指南

-- 3) 评论日期移到近 7/30 日 + 作者/评分多样化
-- 景点评论（spot_id 1-4）
UPDATE comments SET date = '2026-11-01 18:00:00', user_id = 2, rating = 5.0 WHERE id = 1; -- 金佛山·王贝贝（近7日）
UPDATE comments SET date = '2026-10-29 15:20:00', user_id = 3, rating = 4.5 WHERE id = 2; -- 金佛山·李珊珊（近7日）
UPDATE comments SET date = '2026-10-25 10:10:00', user_id = 1, rating = 4.0 WHERE id = 3; -- 金佛山·testuser
UPDATE comments SET date = '2026-10-22 14:00:00', user_id = 2, rating = 5.0 WHERE id = 4; -- 喀斯特·王贝贝
UPDATE comments SET date = '2026-10-18 11:00:00', user_id = 3, rating = 4.5 WHERE id = 5; -- 大足石刻·李珊珊
UPDATE comments SET date = '2026-10-15 16:30:00', user_id = 1, rating = 5.0 WHERE id = 6; -- 故宫·testuser
-- 帖子评论（post_id 1-4）
UPDATE comments SET date = '2026-11-01 15:00:00', user_id = 2 WHERE id = 7; -- 川渝·王贝贝（近7日）
UPDATE comments SET date = '2026-10-28 16:00:00', user_id = 3 WHERE id = 8; -- 川渝·李珊珊（近7日）
UPDATE comments SET date = '2026-10-24 17:00:00', user_id = 2 WHERE id = 9; -- 度假区·王贝贝
UPDATE comments SET date = '2026-10-20 10:00:00', user_id = 2 WHERE id = 10; -- 磁器口·王贝贝
UPDATE comments SET date = '2026-10-13 10:30:00', user_id = 3 WHERE id = 11; -- 错峰·李珊珊
UPDATE comments SET date = '2026-10-08 09:30:00', user_id = 2 WHERE id = 12; -- 错峰·王贝贝
