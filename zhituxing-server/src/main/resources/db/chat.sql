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
