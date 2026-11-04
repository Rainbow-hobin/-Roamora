"""Import the reviewed public-source catalog into the local development database.
Idempotent, transactional; backs up affected records before any mutation.
MYSQL_PASSWORD must be supplied through the environment.
"""
from pathlib import Path
import os,json,secrets
from datetime import datetime
import pymysql,bcrypt
DATA=Path(__file__).resolve().parents[1]/'content/catalog.json'
data=json.loads(DATA.read_text(encoding='utf-8'))
conn=pymysql.connect(host='127.0.0.1',port=int(os.environ.get('MYSQL_PORT','3307')),user=os.environ.get('MYSQL_USER','root'),password=os.environ['MYSQL_PASSWORD'],database='zhituxing',charset='utf8mb4',autocommit=False,cursorclass=pymysql.cursors.DictCursor)
try:
 with conn.cursor() as cur:
  names=[s['name'] for s in data['spots']]; titles=[g['title'] for g in data['guides']]
  cur.execute('SELECT * FROM spots WHERE name IN ('+','.join(['%s']*len(names))+')',names);before_spots=cur.fetchall()
  cur.execute('SELECT * FROM posts WHERE title IN ('+','.join(['%s']*len(titles))+')',titles);before_posts=cur.fetchall()
  cur.execute('SELECT id,name,phone FROM users WHERE name=%s OR phone=%s',('智途星资料编辑','00000000000'));editors=cur.fetchall()
  if editors and (len(editors)!=1 or editors[0]['name']!='智途星资料编辑'):raise RuntimeError('Editorial identity conflict')
  backup=DATA.parent/('backup-content-'+datetime.now().strftime('%Y%m%d-%H%M%S')+'.json')
  backup.write_text(json.dumps(dict(spots=before_spots,posts=before_posts,editor=editors),ensure_ascii=False,default=str,indent=2),encoding='utf-8')
  if editors:editor=editors[0]['id']
  else:
   password=bcrypt.hashpw(secrets.token_bytes(32),bcrypt.gensalt()).decode()
   cur.execute('INSERT INTO users(name,phone,password,role,status,signature) VALUES(%s,%s,%s,%s,%s,%s)',('智途星资料编辑','00000000000',password,'user','正常','依据公开来源整理的资料与编辑建议；非游客亲历评价。'))
   editor=cur.lastrowid
  for x in data['spots']:
   cur.execute('SELECT id FROM spots WHERE name=%s',(x['name'],));rows=cur.fetchall()
   if len(rows)>1:raise RuntimeError('Duplicate destination: '+x['name'])
   desc=x['description']+'\n\n参观提醒：'+x['tips']
   if rows:
    fields={'description':desc}
    if 'category' in x: fields['category']=x['category']
    for key in ['price','openingHours','address']:
     if key in x:fields[{'openingHours':'opening_hours'}.get(key,key)]=x[key]
    cur.execute('UPDATE spots SET '+','.join(k+'=%s' for k in fields)+' WHERE id=%s',(*fields.values(),rows[0]['id']))
   else:
    if x.get('existing'):raise RuntimeError('Missing existing destination: '+x['name'])
    cur.execute('INSERT INTO spots(name,city,category,price,rating,cover,description,address,opening_hours,status,audit_status) VALUES(%s,%s,%s,%s,NULL,%s,%s,%s,%s,%s,%s)',(x['name'],x['city'],x.get('category','历史文化'),x['price'],'/content/'+x['slug']+'.svg',desc,x['address'],x['openingHours'],'上架','已通过'))
  for g in data['guides']:
   cur.execute('SELECT id,author_id FROM posts WHERE title=%s',(g['title'],));rows=cur.fetchall()
   if rows:
    if len(rows)!=1 or rows[0]['author_id']!=editor:raise RuntimeError('Existing post conflict')
    cur.execute('UPDATE posts SET content=%s,board=%s WHERE id=%s',(g['content'],g['board'],rows[0]['id']))
   else:cur.execute('INSERT INTO posts(title,content,author_id,board,images,views,comments_count) VALUES(%s,%s,%s,%s,%s,0,0)',(g['title'],g['content'],editor,g['board'],'[]'))
 conn.commit()
 print('Catalog synchronized:',len(data['spots']),'destinations,',len(data['guides']),'editorial guides. Backup:',backup.name)
except BaseException:
 conn.rollback();raise
finally:conn.close()
