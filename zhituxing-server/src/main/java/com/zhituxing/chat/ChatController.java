package com.zhituxing.chat;
import com.zhituxing.common.Result;
import com.zhituxing.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/v1/chat") @RequiredArgsConstructor
public class ChatController {
 private final ChatService chat;
 private final ChatSocket socket;
 @GetMapping("/users") public Result<?> users(@RequestParam(defaultValue="")String q){return Result.ok(chat.search(CurrentUser.id(),q));}
 @GetMapping("/users/{id}") public Result<?> user(@PathVariable long id){return Result.ok(chat.person(id));}
 @GetMapping("/conversations") public Result<?> conversations(){return Result.ok(chat.conversations(CurrentUser.id()));}
 @GetMapping("/unread") public Result<?> unread(){return Result.ok(Map.of("count",chat.unread(CurrentUser.id())));}
 @GetMapping("/messages/{peer}") public Result<?> history(@PathVariable long peer,@RequestParam(defaultValue="9223372036854775807")long before){return Result.ok(chat.history(CurrentUser.id(),peer,before));}
 @PostMapping("/read/{peer}") public Result<?> read(@PathVariable long peer,@RequestBody Map<String,Long> body){chat.read(CurrentUser.id(),peer,body.getOrDefault("through",0L));socket.notifyRead(CurrentUser.id());return Result.ok();}
}
