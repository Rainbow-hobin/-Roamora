"""Live local integration check: real login, three isolated users, real WebSocket clients.
Creates only temporary chat_it_* users; removes their messages and accounts in finally.
Run with MYSQL_PASSWORD set, after chat.sql and server startup.
"""
import os,json,time,secrets,uuid
import requests,pymysql,bcrypt,websocket
BASE='http://127.0.0.1:8080/api/v1'
session=requests.Session();session.trust_env=False
conn=pymysql.connect(host='127.0.0.1',port=3307,user='root',password=os.environ['MYSQL_PASSWORD'],database='zhituxing',charset='utf8mb4',autocommit=True,cursorclass=pymysql.cursors.DictCursor)
users=[];sockets=[];checks=[]
def check(value,label):
 assert value,label
 checks.append(label);print('PASS:',label,flush=True)
def call(token,path,method='GET',body=None):
 res=session.request(method,BASE+path,headers={'Authorization':'Bearer '+token} if token else {},json=body,timeout=10)
 return res,res.json()
def connect(token,port=8080):
 ws=websocket.create_connection(f'ws://127.0.0.1:{port}/ws/chat',origin='http://localhost:23306',timeout=5,http_no_proxy=['127.0.0.1','localhost']);sockets.append(ws)
 ws.send(json.dumps(dict(type='auth',token=token)))
 ready=json.loads(ws.recv());assert ready['type']=='ready',ready
 return ws,ready['userId']
def event(ws,kind):
 for _ in range(15):
  data=json.loads(ws.recv())
  if data['type']==kind:return data
 raise AssertionError('Missing event '+kind)
def send(ws,to,text,cid=None,**extra):
 cid=cid or str(uuid.uuid4());ws.send(json.dumps(dict(type='send',to=to,content=text,clientId=cid,**extra)));return cid
try:
 for _ in range(3):
  name='chat_it_'+uuid.uuid4().hex[:12];password=secrets.token_urlsafe(16)
  with conn.cursor() as cur:
   cur.execute('INSERT INTO users(name,phone,password,role,status) VALUES(%s,%s,%s,%s,%s)',(name,'199'+''.join(str(secrets.randbelow(10)) for _ in range(8)),bcrypt.hashpw(password.encode(),bcrypt.gensalt()).decode(),'user','正常'))
   uid=cur.lastrowid;users.append(dict(id=uid,name=name))
  captcha=session.get(BASE+'/captchas',timeout=10).json()['data']
  with conn.cursor() as cur:cur.execute('SELECT code FROM captchas WHERE captcha_key=%s',(captcha['captchaKey'],));code=cur.fetchone()['code']
  login=session.post(BASE+'/auth/token',json=dict(account=name,password=password,captchaKey=captcha['captchaKey'],captcha=code),timeout=10).json()
  assert login['code']==0,login
  users[-1]['token']=login['data']['accessToken']
 a,b,c=users
 check(call(None,'/chat/conversations')[0].status_code==401,'REST authentication required')
 try:
  bad=websocket.create_connection('ws://127.0.0.1:8080/ws/chat',origin='https://untrusted.invalid',timeout=3,http_no_proxy=['127.0.0.1']);bad.close();raise AssertionError('Foreign origin accepted')
 except websocket.WebSocketBadStatusException as exc:check(exc.status_code==403,'Foreign WebSocket origin rejected')
 bad=websocket.create_connection('ws://127.0.0.1:8080/ws/chat',origin='http://localhost:23306',timeout=3,http_no_proxy=['127.0.0.1']);sockets.append(bad)
 bad.send(json.dumps(dict(type='auth',token='invalid-token')));check(json.loads(bad.recv())['type']=='error','Invalid token rejected on WebSocket')
 wa,aid=connect(a['token'],23306);wb,bid=connect(b['token']);wc,cid=connect(c['token']);wa2,_=connect(a['token'])
 check(aid==a['id'] and bid==b['id'],'Authenticated sender identity over Vite proxy')
 res=call(a['token'],'/chat/users?q='+b['name'])[1]['data'];check(any(p['id']==bid and 'phone' not in p for p in res),'User search without private profile fields')
 client=send(wa,bid,'integration hello',senderId=c['id']);ma=event(wa,'message')['message'];mb=event(wb,'message')['message'];mirror=event(wa2,'message')['message']
 check(ma['id']==mb['id']==mirror['id'] and ma['senderId']==aid,'Two-user live delivery, sender spoof ignored, same-user second connection synced')
 wc.settimeout(.5)
 try:wc.recv();raise AssertionError('Third user received private message')
 except websocket.WebSocketTimeoutException:check(True,'Third connection receives no private message')
 check(call(c['token'],f'/chat/messages/{bid}')[1]['data']==[],'Third user cannot read another pair history')
 send(wa,bid,'integration hello',client);event(wa,'message');event(wb,'message');event(wa2,'message')
 check(len(call(a['token'],f'/chat/messages/{bid}')[1]['data'])==1,'Retried clientId stored exactly once')
 wb.close();send(wa,bid,'offline delivery');offline=event(wa,'message')['message'];event(wa2,'message')
 history=call(b['token'],f'/chat/messages/{aid}')[1]['data'];check(len(history)==2 and history[-1]['content']=='offline delivery','Offline message persisted and history ordered')
 check(call(b['token'],'/chat/unread')[1]['data']['count']==2,'Unread count includes offline messages')
 call(b['token'],f'/chat/read/{c["id"]}','POST',dict(through=offline['id']))
 check(call(b['token'],'/chat/unread')[1]['data']['count']==2,'Cannot mark unrelated conversation read')
 call(b['token'],f'/chat/read/{aid}','POST',dict(through=ma['id']))
 check(call(b['token'],'/chat/unread')[1]['data']['count']==1,'Read cursor preserves later unread messages')
 check(len(call(b['token'],f'/chat/messages/{aid}?before={offline["id"]}')[1]['data'])==1,'History cursor pagination')
 send(wa,bid,'x'*2001);check(event(wa,'error')['type']=='error','Oversized message rejected')
 send(wa,aid,'self');check(event(wa,'error')['type']=='error','Self-message rejected')
 with conn.cursor() as cur:cur.execute("UPDATE users SET status='禁用' WHERE id=%s",(a['id'],))
 send(wa,bid,'blocked');check(event(wa,'error')['type']=='error','Disabled account cannot continue sending')
 check(call(a['token'],'/chat/conversations')[0].status_code==401,'Disabled account REST token rejected')
 print('ALL',len(checks),'CHAT INTEGRATION CHECKS PASSED')
finally:
 for ws in sockets:
  try:ws.close()
  except Exception:pass
 if users:
  ids=[u['id'] for u in users];placeholders=','.join(['%s']*len(ids))
  with conn.cursor() as cur:
   cur.execute('DELETE FROM chat_messages WHERE sender_id IN ('+placeholders+') OR recipient_id IN ('+placeholders+')',ids+ids)
   cur.execute('DELETE FROM users WHERE id IN ('+placeholders+') AND name LIKE %s',ids+['chat_it_%'])
 conn.close()
