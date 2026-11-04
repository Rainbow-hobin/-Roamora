"""Deterministic checks for dates and explicitly labelled total-cost tables."""
import re
from datetime import date
from decimal import Decimal


def check_answer(answer: str) -> str:
    def calendar(match):
        try:
            day = date(*map(int, match.group(1, 2, 3)))
        except ValueError:
            return match.group(0)
        return f"{day.year}年{day.month}月{day.day}日（周{'一二三四五六日'[day.weekday()]}）"

    answer = re.sub(r"(\d{4})年(\d{1,2})月(\d{1,2})日[（(](?:周|星期)[一二三四五六日天][）)]", calendar, answer)
    totals = []
    column = None
    warnings = []
    for line in answer.splitlines():
        if not line.strip().startswith('|'):
            column = None
            totals = []
            continue
        cells = [c.strip().replace('**', '') for c in line.strip().strip('|').split('|')]
        header = next((i for i, c in enumerate(cells) if re.search(r'(?:\d+人|总)费用|总价', c)), None)
        if header is not None:
            column, totals = header, []
            continue
        if column is None or column >= len(cells):
            continue
        amounts = re.fullmatch(r'[¥￥]?\s*(\d+(?:\.\d+)?)(?:\s*[–—~～-]\s*[¥￥]?\s*(\d+(?:\.\d+)?))?\s*元?', cells[column])
        if not amounts:
            continue
        lo = Decimal(amounts[1])
        hi = Decimal(amounts[2] or amounts[1])
        if any('合计' in c for c in cells):
            if totals and (lo != sum(t[0] for t in totals) or hi != sum(t[1] for t in totals)):
                warnings.append('费用表合计与明细不一致，请勿据此确认预算；请要求重新核算人数和往返次数。')
        else:
            totals.append((lo, hi))
    if warnings:
        answer = '> ⚠️ 核算提醒：' + '；'.join(dict.fromkeys(warnings)) + '\n\n' + answer
    return answer
