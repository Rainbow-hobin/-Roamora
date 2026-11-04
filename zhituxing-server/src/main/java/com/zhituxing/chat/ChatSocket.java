package com.zhituxing.chat;
import com.fasterxml.jackson.databind.*;
import com.zhituxing.security.JwtUtil;
import com.zhituxing.common.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component @RequiredArgsConstructor
public class ChatSocket extends TextWebSocketHandler {
 private final ChatService chat; private final JwtUtil jwt; private final ObjectMapper json;
 private static class Client {
  final WebSocketSession socket; final long opened=System.currentTimeMillis();
  Long user; long expires; long window=System.currentTimeMillis(); int sent;
  Client(WebSocketSession s){socket=new ConcurrentWebSocketSessionDecorator(s,5000,65536);}
 }
 private final Map<String,Client> clients=new ConcurrentHashMap<>();
 @Override public void afterConnectionEstablished(WebSocketSession s) {s.setTextMessageSizeLimit(16384);clients.put(s.getId(),new Client(s));}
 private void emit(Client c,Object event) {try {if(c.socket.isOpen())c.socket.sendMessage(new TextMessage(json.writeValueAsString(event)));}catch(Exception e){close(c);}}
 private void close(Client c){clients.remove(c.socket.getId());try{c.socket.close(CloseStatus.POLICY_VIOLATION);}catch(Exception ignored){}}
 private void broadcast(long user,Object event){clients.values().stream().filter(c->c.user!=null&&c.user==user).forEach(c->emit(c,event));}
 @Override protected void handleTextMessage(WebSocketSession session,TextMessage message) {
  Client c=clients.get(session.getId());if(c==null)return;
  String clientId="";
  try {
   JsonNode event=json.readTree(message.getPayload());String type=event.path("type").asText();clientId=event.path("clientId").asText();
   if(c.user==null) {
    if(!type.equals("auth"))throw new IllegalArgumentException("请先登录");
    var claims=jwt.parse(event.path("token").asText());
    c.user=chat.person(Long.parseLong(claims.getSubject())).id();c.expires=claims.getExpiration().getTime();
    emit(c,Map.of("type","ready","userId",c.user));return;
   }
   if(System.currentTimeMillis()>=c.expires){close(c);return;}
   chat.person(c.user);
   if(type.equals("ping")){emit(c,Map.of("type","pong"));return;}
   if(!type.equals("send"))throw new IllegalArgumentException("不支持的消息类型");
   if(System.currentTimeMillis()-c.window>60000){c.window=System.currentTimeMillis();c.sent=0;}
   if(++c.sent>60)throw new IllegalArgumentException("发送过于频繁，请稍后再试");
   var saved=chat.send(c.user,event.path("to").asLong(),event.path("content").asText(),clientId);
   var out=Map.of("type","message","message",saved);
   broadcast(c.user,out);broadcast(saved.recipientId(),out);
  } catch(Exception e) {
   String detail=e instanceof BusinessException||e instanceof IllegalArgumentException?e.getMessage():"消息发送失败，请重试";
   emit(c,Map.of("type","error","clientId",clientId,"message",detail==null?"消息无效":detail));
   if(c.user==null||e instanceof io.jsonwebtoken.JwtException)close(c);
  }
 }
 public void notifyRead(long user){broadcast(user,Map.of("type","read"));}
 @Override public void afterConnectionClosed(WebSocketSession s,CloseStatus status){clients.remove(s.getId());}
 @Override public void handleTransportError(WebSocketSession s,Throwable e){Client c=clients.get(s.getId());if(c!=null)close(c);}
 @Scheduled(fixedRate=5000) public void expire(){long now=System.currentTimeMillis();clients.values().forEach(c->{if(c.user==null?now-c.opened>5000:now>=c.expires)close(c);});}
}
