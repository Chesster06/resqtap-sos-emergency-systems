import re

with open('build_clean_bw_flowcharts.py', 'r', encoding='utf-8') as f:
    content = f.read()

pattern = re.compile(r'(?:draw_process|draw_subroutine|draw_io_box)\(\s*d,\s*\[(\d+),\s*(\d+),\s*(\d+),\s*(\d+)\],\s*("[^"]+"),\s*(\[[^\]]*\])', re.DOTALL)

for match in pattern.finditer(content):
    x1, y1, x2, y2 = int(match.group(1)), int(match.group(2)), int(match.group(3)), int(match.group(4))
    title = match.group(5)
    lines_str = match.group(6)
    # count lines
    raw_lines = [l.strip() for l in lines_str.strip('[]').split('\n') if l.strip().startswith('"')]
    num_lines = len(raw_lines)
    box_h = y2 - y1
    text_end = 36 + (num_lines - 1) * 20 + 14
    margin = box_h - text_end
    if margin < 18:
        print(f"TIGHT BOX: {title:55} | Height: {box_h:3} | TextEnd: {text_end:3} | Margin: {margin:2}px")
