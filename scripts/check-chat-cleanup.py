import os,pymysql,requests
c=pymysql.connect(host='127.0.0.1',port=3307,user='root',password=os.environ['MYSQL_PASSWORD'],database='zhituxing')
with c.cursor() as cur:
 cur.execute("SELECT COUNT(*) FROM users WHERE name LIKE 'chat_it_%'")
 print('Remaining temporary integration users:',cur.fetchone()[0])
c.close()
r=requests.get('http://127.0.0.1:23306/messages',timeout=5)
print('Frontend message entry HTTP:',r.status_code)
