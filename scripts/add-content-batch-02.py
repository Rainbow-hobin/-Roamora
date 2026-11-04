from pathlib import Path
import json
D='2026-11-03'
def source(name,url):return dict(name=name,url=url)
m=source('敦煌研究院·莫高窟参观信息','https://www.dunhuangcaves.org/skxl/mgk.htm')
mb=source('敦煌研究院·门票销售渠道声明','https://dunhuangcaves.org/info/1021/7785.htm')
j=source('九寨沟景区·官方票务信息','https://www.jiuzhai.com/intelligent-service/tickets')
jo=source('九寨沟景区·游客指南','https://en.jiuzhai.com/about/visitor-information')
h=source('松潘县政府·黄龙参观信息','https://www.songpan.gov.cn/spxrmzf/c100134/202409/61ca141c067e4d67b419ea8def5b73e9.shtml')
hu=source('UNESCO·黄龙遗产介绍','https://whc.unesco.org/en/list/638')
d=source('都江堰旅游门户·票务说明','https://www.djy517.com/online.html?channelCode=mpyd')
w=source('杭州网·西湖公共游览区域介绍','https://jrsh.hangzhou.com.cn/content/2026-02/22/content_9180685.html')
wu=source('UNESCO·杭州西湖文化景观','https://whc.unesco.org/en/list/1334')
spots=[]
def add(name,city,category,slug,desc,price,label,hours,address,duration,tips,sources,theme):
 spots.append(dict(name=name,city=city,category=category,slug=slug,description=desc,price=price,priceLabel=label,openingHours=hours,address=address,duration=duration,tips=tips,sources=sources,verifiedAt=D,coverTheme=theme))
add('莫高窟','敦煌','历史文化','mogao-caves','从洞窟艺术与数字展示认识敦煌。不同参观票种对应的数字观影、实体洞窟数量并不相同，应先确定预约产品，再安排到达时间。\n\n编辑建议：把参观预约作为敦煌行程的固定节点，其余活动围绕已确认的时段安排。不要把应急参观产品视作常规参观的等价替代。',238,'旺季常规238元 / 淡季常规140元','旺季08:00–18:00；淡季09:00起','甘肃省敦煌市（按预约指引到达）','半天（编辑建议）','研究院页面列旺季4月至11月、淡季12月至次年3月；首页与分页面可能有不同开放提示，实际以预约订单和最新公告为准。官网声明购票渠道为mgk.org.cn及其官方微信小程序。',[m,mb],'desert')
add('九寨沟','阿坝','自然风光','jiuzhai-valley','在湖泊、瀑布与森林之间感受山地水景。行程需要同时考虑景区游览和内部观光车，不宜只看大门票价格就确定全天预算。\n\n编辑建议：安排完整一天作为游览日。先核对当天可达区域，再按体力挑选步行段，不以走遍所有节点为目标。',190,'门票旺季190元 / 淡季80元；观光车另计','旺季07:30起；淡季08:30起','四川省阿坝州九寨沟县','1天（编辑建议）','官网列4月1日至11月15日为旺季，观光车90元；其余淡季观光车80元。携带实名预约证件，季节关闭区域及当日游览截止时间以景区公告为准。',[j,jo],'lake')
add('黄龙风景名胜区','阿坝','自然风光','huanglong-valley','黄龙的特色在于层叠钙华彩池、森林与山地景观。游览应沿开放栈道展开，避免进入池体与保护区域。\n\n编辑建议：给黄龙单独留出半天以上，把索道、步行和休息分开安排。是否选择索道需结合当日开放和个人情况，不预设每个人都适合走同一条路线。',170,'旺季170元 / 淡季60元；索道等另计','入园与索道时间以当日公告为准','四川省阿坝州松潘县','半天至1天（编辑建议）','县政府指南列6月1日至12月15日旺季、12月16日至次年5月31日淡季。冬季开放、索道与观光车运行可能调整，请核对最新票种；不把不同景区的淡旺季日期混用。',[h,hu],'mountain')
add('都江堰景区','成都','历史文化','dujiangyan','从岷江与古代水利工程的关系理解都江堰。与青城山虽然常被安排在同一趟旅行中，却是需要分别确认门票和入口的景区。\n\n编辑建议：先选定都江堰为半日重点，再判断是否有足够时间加入其他地点；观景与理解工程原理比匆忙跨景区更适合初次参观。',80,'成人门票80元；优惠与附加项目另核对','按季节与官方购票时段执行','四川省成都市都江堰市','3–4小时（编辑建议）','公开票务页要求实名信息与入园证件一致；若安排青城山，另行核对交通和票种。平台不提供未经核验的具体公交班次或固定换乘耗时。',[d],'river')
add('杭州西湖','杭州','自然风光','hangzhou-west-lake','西湖把湖水、堤岸、岛屿与园林融入城市生活。更适合按一段湖岸或一个主题慢慢走，而不是为了完成环湖里程赶路。\n\n编辑建议：半天只选一段步行区域，留时间停留；整日行程再考虑加入展馆、寺庙或游船。整体文化景观与单独收费项目并非同一个票务范围。',0,'公共环湖区域免费；游船及独立项目另计','公共湖岸开放；独立项目另核对','浙江省杭州市西湖周边公共区域','半天至1天（编辑建议）','免费范围指公共环湖步游区域，不代表游船、塔楼、展馆或所有周边景点均免费。参观独立项目之前，请核对其官方预约与营业信息。',[w,wu],'lake')
guides=[]
def guide(title,related,sources,content,board='出行攻略'):
 guides.append(dict(title=title,related=related,sources=sources,content=content,board=board,verifiedAt=D))
guide('敦煌行程先订哪一项：把莫高窟预约放在前面','莫高窟',[m,mb],'先确定莫高窟参观日期、时段和票种，再预订与其衔接的活动。研究院页面区分常规与应急参观，二者覆盖内容不同，不能只用价格判断是否适合自己。\n\n编辑建议：预留到达与核验时间，避免把其他活动排到预约时段前的最后一刻。天气引发的暂停开放需要按研究院公告处理。购票通过研究院声明的官方预约网及微信小程序进行。')
guide('九寨沟预算别漏观光车：淡旺季分开算','九寨沟',[j],'九寨沟官网将景区门票和观光车分别列价。旺季成人门票190元、观光车90元，两项合计每人280元；淡季两项均80元，合计每人160元。以上不包含往返景区交通、住宿和餐饮。\n\n编辑建议：两名成人按旺季两项计算为560元、淡季为320元。优惠资格需另核对，不要把只免门票理解为连观光车也免费。')
guide('九寨沟与黄龙：不要套用同一份季节表','黄龙风景名胜区',[j,h],'九寨沟官方票务页列旺季至11月15日，而松潘县黄龙指南列旺季至12月15日。即使同属一趟阿坝旅行，季节定价也不能照搬。\n\n编辑建议：分别确认两个景区的门票、内部交通和当日开放安排，留出景区之间的转场时间。这里提供安排原则，不给出未经核实的车次、保证衔接时间或包车报价。')
guide('都江堰半日慢看：先理解水，再决定赶不赶路','都江堰景区',[d],'都江堰门票与青城山不是同一张票。以都江堰为重点时，先预留3至4小时参观，再判断余下时间是否适合其他活动。\n\n编辑建议：出发前了解工程与水流的关系，现场边走边看。若加青城山，应单独核对其票种、入口和交通，不把地图上相近的景点自动视作步行可达。')
guide('西湖半天怎么走：选一段湖岸，不追全环线','杭州西湖',[w,wu],'西湖适合分段体验。编辑建议：第一次来可选择白堤与孤山附近的一段步行范围，留出看湖、休息和临时停留的时间，不把走完整圈当作完成旅行的标准。\n\n公共环湖区域免费，游船和独立景点的收费、预约与营业时间另查。雨天或人多时可以减少步行范围；本文不承诺固定步程、船班或日落时刻。')
batch=dict(verifiedAt=D,spots=spots,guides=guides)
Path('content/batch-02.json').write_text(json.dumps(batch,ensure_ascii=False,indent=2),encoding='utf-8')
p=Path('content/catalog.json');catalog=json.loads(p.read_text(encoding='utf-8'))
for key,identity in [('spots','name'),('guides','title')]:
 merged={v[identity]:v for v in catalog[key]}
 for v in batch[key]:merged[v[identity]]=v
 catalog[key]=list(merged.values())
p.write_text(json.dumps(catalog,ensure_ascii=False,indent=2),encoding='utf-8')
print('Catalog:',len(catalog['spots']),'destinations,',len(catalog['guides']),'editorial guides')
