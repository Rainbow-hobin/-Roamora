"""Build frontend catalog and clearly labelled local illustrative covers from reviewed catalog.json."""
from pathlib import Path
import json,html
root=Path(__file__).resolve().parents[1]
data=json.loads((root/'content/catalog.json').read_text(encoding='utf-8'))
(root/'zhituxing-ui/src/data/catalog.json').write_text(json.dumps(data,ensure_ascii=False,indent=2),encoding='utf-8')
for x in data['spots']:
 if not x.get('slug'):continue
 theme=x.get('coverTheme','architecture')
 palettes={'desert':('#f3e9d7','#dbb883','#856743'),'lake':('#e5eee9','#bbd1cc','#527a74'),'mountain':('#e8eddf','#c3cdb0','#67785a'),'river':('#e5ebe6','#bfcec1','#4f776c'),'architecture':('#edf1e7','#dce5d5','#a5b6a3')}
 bg,accent,stroke=palettes[theme]
 if theme=='architecture':shape='M170 445V300L500 160L830 300V445M220 300H780M270 330V440M420 330V440M570 330V440M720 330V440'
 elif theme=='desert':shape='M0 380Q250 100 500 310T1000 250M0 440Q300 230 650 390T1000 360'
 elif theme=='mountain':shape='M0 360L220 170L410 340L620 90L1000 380M500 200L620 90L710 200'
 elif theme=='river':shape='M0 170Q220 80 410 180T1000 120M580 170Q340 230 650 300T480 470M100 270H850'
 else:shape='M0 280Q150 70 340 240T720 180T1000 240M80 310H920M140 340H800M230 370H860'
 title=html.escape(x['name']);city=html.escape(x['city'])
 svg=f'''<svg xmlns="http://www.w3.org/2000/svg" width="1000" height="600" viewBox="0 0 1000 600"><rect width="1000" height="600" fill="{bg}"/><circle cx="820" cy="110" r="160" fill="{accent}"/><path d="{shape}" stroke="{stroke}" stroke-width="4" fill="none"/><rect x="60" y="370" width="880" height="140" fill="{bg}" opacity=".97"/><g fill="#254735" font-family="Microsoft YaHei, sans-serif"><text x="80" y="405" font-size="23">{city} · 目的地手记</text><text x="80" y="468" font-size="42">{title}</text><text x="80" y="555" font-size="19" fill="#647360">智途星公开资料导览 / 示意封面</text></g></svg>'''
 (root/'zhituxing-ui/public/content'/f"{x['slug']}.svg").write_text(svg,encoding='utf-8')
print('Frontend catalog and covers generated')
