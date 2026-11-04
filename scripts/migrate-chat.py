from pathlib import Path
import pymysql,os
sql=Path('zhituxing-server/src/main/resources/db/chat.sql').read_text(encoding='utf-8')
c=pymysql.connect(host='127.0.0.1',port=3307,user='root',password=os.environ['MYSQL_PASSWORD'],database='zhituxing',charset='utf8mb4')
try:
 with c.cursor() as cur:cur.execute(sql)
 c.commit();print('Additive chat table migration applied')
finally:c.close()
