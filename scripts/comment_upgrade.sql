-- 评论功能扩展（在 Navicat 或 mysql 客户端中执行）
USE douyin_clone;

-- 若列已存在会报错，可忽略对应语句
ALTER TABLE comment ADD COLUMN reply_to_user_id VARCHAR(64) NULL COMMENT '被回复用户ID' AFTER parent_id;
ALTER TABLE comment ADD COLUMN status TINYINT NOT NULL DEFAULT 0 COMMENT '0正常 1被折叠' AFTER like_count;

CREATE TABLE IF NOT EXISTS comment_like_record (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '内部ID',
  user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
  comment_id VARCHAR(64) NOT NULL COMMENT '评论ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_comment (user_id, comment_id),
  KEY idx_comment_id (comment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论点赞表';
