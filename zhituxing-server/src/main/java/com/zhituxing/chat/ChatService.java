package com.zhituxing.chat;
import com.zhituxing.common.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service @RequiredArgsConstructor
public class ChatService {
 private final JdbcTemplate db;
 public record Person(long id,String name,String avatar) {}
 public record Message(long id,long senderId,long recipientId,String content,String clientId,LocalDateTime createdAt,LocalDateTime readAt) {}
 public record Conversation(Person peer,Message lastMessage,long unread) {}
 private final RowMapper<Message> mapper=(rs,n)->new Message(rs.getLong("id"),rs.getLong("sender_id"),rs.getLong("recipient_id"),rs.getString("content"),rs.getString("client_id"),rs.getTimestamp("created_at").toLocalDateTime(),rs.getTimestamp("read_at")==null?null:rs.getTimestamp("read_at").toLocalDateTime());
 public Person person(long id) {
  var rows=db.query("SELECT id,name,avatar FROM users WHERE id=? AND status='正常' AND phone<>'00000000000'",(rs,n)->new Person(rs.getLong(1),rs.getString(2),rs.getString(3)),id);
  if(rows.isEmpty()) throw new BusinessException(ErrorCode.NOT_FOUND,"用户不存在或当前不可私聊");
  return rows.get(0);
 }
 public List<Person> search(long me,String q) {
  if(q==null||q.trim().length()<2) return List.of();
  if(q.length()>32) throw new BusinessException(ErrorCode.BAD_REQUEST);
  return db.query("SELECT id,name,avatar FROM users WHERE status='正常' AND phone<>'00000000000' AND id<>? AND LOCATE(?,name)>0 ORDER BY id LIMIT 20",(rs,n)->new Person(rs.getLong(1),rs.getString(2),rs.getString(3)),me,q.trim());
 }
 public List<Conversation> conversations(long me) {
  var latest=db.query("SELECT m.* FROM chat_messages m JOIN (SELECT MAX(id) id FROM chat_messages WHERE sender_id=? OR recipient_id=? GROUP BY CASE WHEN sender_id=? THEN recipient_id ELSE sender_id END) c ON m.id=c.id ORDER BY m.id DESC LIMIT 100",mapper,me,me,me);
  List<Conversation> out=new ArrayList<>();
  for(var m:latest) {
   long peer=m.senderId()==me?m.recipientId():m.senderId();
   var p=db.query("SELECT id,name,avatar FROM users WHERE id=?",(rs,n)->new Person(rs.getLong(1),rs.getString(2),rs.getString(3)),peer);
   if(!p.isEmpty())out.add(new Conversation(p.get(0),m,db.queryForObject("SELECT COUNT(*) FROM chat_messages WHERE recipient_id=? AND sender_id=? AND read_at IS NULL",Long.class,me,peer)));
  }
  return out;
 }
 public long unread(long me) {return db.queryForObject("SELECT COUNT(*) FROM chat_messages WHERE recipient_id=? AND read_at IS NULL",Long.class,me);}
 public List<Message> history(long me,long peer,long before) {
  var rows=db.query("SELECT * FROM chat_messages WHERE ((sender_id=? AND recipient_id=?) OR (sender_id=? AND recipient_id=?)) AND id<? ORDER BY id DESC LIMIT 50",mapper,me,peer,peer,me,before);
  Collections.reverse(rows);return rows;
 }
 @Transactional
 public Message send(long me,long to,String content,String clientId) {
  person(me); person(to);
  if(me==to||content==null||content.isBlank()||content.length()>2000||clientId==null||!clientId.matches("[a-zA-Z0-9-]{8,64}")) throw new BusinessException(ErrorCode.BAD_REQUEST,"消息需为1至2000字，且不能给自己发消息");
  db.update("INSERT IGNORE INTO chat_messages(sender_id,recipient_id,content,client_id) VALUES(?,?,?,?)",me,to,content.trim(),clientId);
  var saved=db.query("SELECT * FROM chat_messages WHERE sender_id=? AND client_id=?",mapper,me,clientId).get(0);
  if(saved.recipientId()!=to||!saved.content().equals(content.trim()))throw new BusinessException(ErrorCode.CONFLICT,"消息编号已用于其他内容");
  return saved;
 }
 public void read(long me,long peer,long through) {
  if(through<=0)throw new BusinessException(ErrorCode.BAD_REQUEST);
  db.update("UPDATE chat_messages SET read_at=CURRENT_TIMESTAMP(6) WHERE recipient_id=? AND sender_id=? AND id<=? AND read_at IS NULL",me,peer,through);
 }
}
