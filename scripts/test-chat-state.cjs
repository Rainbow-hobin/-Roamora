/* Regression checks against real TS/Vue script modules. No browser or database required. */
const fs=require('node:fs'),path=require('node:path'),vm=require('node:vm'),assert=require('node:assert/strict');
const root=path.resolve(__dirname,'..'), ts=require(path.join(root,'zhituxing-ui/node_modules/typescript')),vue=require(path.join(root,'zhituxing-ui/node_modules/vue'));
const checks=[];function pass(name){checks.push(name);console.log('PASS:',name)}
const tick=async()=>{for(let i=0;i<8;i++)await Promise.resolve()};
function deferred(){let resolve;const promise=new Promise(r=>resolve=r);return {promise,resolve}}
function moduleFrom(file,modules={},globals={},extra=''){
 let source=fs.readFileSync(path.join(root,file),'utf8');if(file.endsWith('.vue'))source=source.match(/<script setup lang="ts">([\s\S]*?)<\/script>/)[1];
 const code=ts.transpileModule(source+'\n'+extra,{compilerOptions:{module:ts.ModuleKind.CommonJS,target:ts.ScriptTarget.ES2022}}).outputText;
 const mod={exports:{}};vm.runInNewContext(code,{module:mod,exports:mod.exports,require:n=>{if(n==='vue')return vue;if(n in modules)return modules[n];throw Error('Unexpected module '+n)},console,setTimeout:()=>1,clearTimeout:()=>{},setInterval:()=>1,clearInterval:()=>{},...globals},{filename:file});return mod.exports;
}
async function identity(){
 const data=new Map(),storageCallbacks=[],sockets=[];let delayed;
 const storage={getItem:k=>data.get(k)||null,setItem:(k,v)=>data.set(k,v),removeItem:k=>data.delete(k)};
 class FakeSocket{static OPEN=1;constructor(){this.readyState=0;this.frames=[];this.closed=false;sockets.push(this)}send(frame){this.frames.push(JSON.parse(frame))}close(){this.closed=true;this.readyState=3}}
 const globals={localStorage:storage,window:{addEventListener:(type,cb)=>{if(type==='storage')storageCallbacks.push(cb)}},WebSocket:FakeSocket,location:{protocol:'http:',host:'localhost:23306'},fetch:async url=>url.endsWith('/stale-test')?delayed.promise:{ok:true,status:200,json:async()=>({code:0,data:{count:0}})}};
 const http=moduleFrom('zhituxing-ui/src/api/http.ts',{},globals);
 const chat=moduleFrom('zhituxing-ui/src/stores/chat.ts',{'@/api/http':http},globals);
 const user=moduleFrom('zhituxing-ui/src/stores/user.ts',{'@/api/http':http,'@/api':{getMe:async()=>({name:http.getToken(),phone:'',role:'user'})}},globals);
 moduleFrom('zhituxing-ui/src/App.vue',{'@/api/http':http,'@/stores/chat':chat,'@/components/layout/AppLayout.vue':{}},globals);
 const ready=(socket,id)=>{socket.readyState=1;socket.onopen();socket.onmessage({data:JSON.stringify({type:'ready',userId:id})})};
 user.applyLogin('A','user-A','','user');const first=sockets.at(-1);ready(first,1);await tick();
 first.onclose({code:1008});const count=sockets.length;user.applyLogin('A-renewed','user-A','','user');assert.equal(sockets.length,count+1);ready(sockets.at(-1),1);assert.equal(chat.chatState.connected,true);pass('Expired connection restarts after re-login while loggedIn was already true');
 let resets=0;chat.onChat(e=>{if(e.type==='reset')resets++});const old=sockets.at(-1);
 storage.setItem('zhituxing_token','B');storageCallbacks.forEach(cb=>cb({key:'zhituxing_token',storageArea:storage}));assert.equal(old.closed,true);assert.equal(chat.chatState.userId,0);assert.ok(resets>0);ready(sockets.at(-1),2);assert.equal(sockets.at(-1).frames[0].token,'B');await tick();assert.equal(user.userState.name,'B');pass('Cross-tab token change closes old socket and restores new user identity');
 old.onmessage({data:JSON.stringify({type:'ready',userId:1})});assert.equal(chat.chatState.userId,2);pass('Late frames from old account socket ignored');
 storage.setItem('zhituxing_token','C');assert.throws(()=>chat.sendChat(3,'hello','test-id-123'));assert.equal(chat.chatState.connected,false);pass('Send detects credential change even before storage event');
 ready(sockets.at(-1),3);delayed=deferred();const result=http.request('/stale-test').catch(e=>e);http.setToken('D');delayed.resolve({ok:false,status:401,json:async()=>({code:401,message:'expired'})});assert.equal((await result).message,'登录身份已变更，请重新操作');assert.equal(http.getToken(),'D');pass('Old account 401 does not clear new account credentials');
 storage.removeItem('zhituxing_token');storageCallbacks.forEach(cb=>cb({key:null,storageArea:storage}));assert.equal(chat.chatState.userId,0);assert.equal(user.userState.loggedIn,false);pass('Cross-tab storage clear logs out and resets chat');chat.stopChat();
}
async function view(){
 const callbacks=[],cleanups=[],route=vue.reactive({query:{}}),chatState=vue.reactive({userId:1,connected:true});
 let listImpl=async()=>[],historyImpl=async()=>[];const cursors=[];
 const api={getConversations:()=>listImpl(),getMessages:(id,before)=>{cursors.push(before);return historyImpl(id,before)},getPerson:async id=>({id,name:'peer-'+id,avatar:null}),searchPeople:async()=>[],readMessages:async()=>{}};
 const globals={document:{visibilityState:'hidden',addEventListener:()=>{},removeEventListener:()=>{}},crypto:{randomUUID:()=> 'client-id'}};
 const mods={'vue':{...vue,onMounted:()=>{},onUnmounted:fn=>cleanups.push(fn)},'vue-router':{useRoute:()=>route,useRouter:()=>({replace:async value=>{route.query=value.query||{}}})},'@/api/chat':api,'@/stores/chat':{chatState,onChat:fn=>{callbacks.push(fn);return ()=>{}},sendChat:()=>{},refreshUnread:async()=>{}}};
 // Inject lifecycle stubs while preserving Vue refs/watch behavior.
 const realVue=vue;
 const source=fs.readFileSync(path.join(root,'zhituxing-ui/src/views/MessagesView.vue'),'utf8').match(/<script setup lang="ts">([\s\S]*?)<\/script>/)[1];
 const extra='export const test={list,openPeer,older,resetAccount,conversations,messages,historyBefore,hasMore,people,pending,draft,query};';
 const code=ts.transpileModule(source+extra,{compilerOptions:{module:ts.ModuleKind.CommonJS,target:ts.ScriptTarget.ES2022}}).outputText;
 const mod={exports:{}};vm.runInNewContext(code,{module:mod,exports:mod.exports,require:n=>mods[n]||realVue,...globals,console,setTimeout:()=>1,clearTimeout:()=>{},setInterval:()=>1,clearInterval:()=>{}},{filename:'MessagesView.vue'});const t=mod.exports.test;
 const emit=event=>callbacks.forEach(fn=>fn(event));const message=id=>({id,senderId:2,recipientId:1,clientId:'msg-'+id,content:String(id),createdAt:'2026-11-03',readAt:null});
 historyImpl=async()=>[message(1),message(2)];await t.openPeer(2);emit({type:'message',message:message(2)});
 historyImpl=async()=>Array.from({length:50},(_,i)=>message(i+62));await t.openPeer(2);assert.equal(t.messages.value[0].id,62);assert.equal(t.historyBefore.value,62);
 historyImpl=async(id,before)=>Array.from({length:50},(_,i)=>message(i+12));await t.older();assert.equal(cursors.at(-1),62);assert.equal(t.messages.value[0].id,12);pass('Reconnect discards stale buffer and keeps server history cursor reachable');
 const waiting=deferred();historyImpl=()=>waiting.promise;const loading=t.openPeer(2);emit({type:'message',message:message(112)});waiting.resolve([message(110),message(111)]);await loading;assert.deepEqual(Array.from(t.messages.value,m=>m.id),[110,111,112]);assert.equal(t.historyBefore.value,110);pass('Messages arriving during history fetch merged without moving history cursor');
 const first=deferred(),second=deferred();let request=0;listImpl=()=>++request===1?first.promise:second.promise;const old=t.list(),fresh=t.list();second.resolve([{unread:0}]);await fresh;first.resolve([{unread:5}]);await old;assert.equal(t.conversations.value[0].unread,0);pass('Late conversation response cannot restore stale unread count');
 const staleList=deferred(),staleHistory=deferred();listImpl=()=>staleList.promise;historyImpl=()=>staleHistory.promise;const list=t.list(),history=t.openPeer(2);t.draft.value='private draft';t.pending.value.push({to:2,clientId:'pending-1',content:'private',error:''});emit({type:'reset'});staleList.resolve([{unread:99}]);staleHistory.resolve([message(113)]);await Promise.all([list,history]);assert.equal(t.messages.value.length,0);assert.equal(t.conversations.value.length,0);assert.equal(t.pending.value.length,0);assert.equal(t.draft.value,'');pass('Account reset clears private state and rejects old async responses');
 cleanups.forEach(fn=>fn());
}
(async()=>{await identity();await view();console.log(`ALL ${checks.length} CHAT STATE REGRESSIONS PASSED`);})().catch(e=>{console.error(e);process.exitCode=1});
