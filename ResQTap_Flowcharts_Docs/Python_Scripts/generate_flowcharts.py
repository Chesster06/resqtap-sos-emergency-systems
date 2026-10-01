import os
import math
from PIL import Image, ImageDraw, ImageFont

# Directory for output
OUTPUT_DIR = os.path.join(os.path.dirname(__file__), "docs_flowcharts")
os.makedirs(OUTPUT_DIR, exist_ok=True)

# Fonts
FONT_BOLD_PATH = r"C:\Windows\Fonts\segoeuib.ttf"
FONT_REG_PATH = r"C:\Windows\Fonts\segoeui.ttf"

def get_font(size, bold=False):
    path = FONT_BOLD_PATH if bold else FONT_REG_PATH
    try:
        return ImageFont.truetype(path, size)
    except:
        return ImageFont.load_default()

# Colors Palette - Professional Executive Modern Theme
BG_COLOR = (248, 250, 252)       # Light slate
CARD_BG = (255, 255, 255)        # Pure white
BORDER_COLOR = (203, 213, 225)   # Slate 300
SHADOW_COLOR = (226, 232, 240)   # Slate 200

# Node Type Colors
C_START_END_BG = (16, 185, 129)   # Emerald 500
C_START_END_TXT = (255, 255, 255)
C_START_END_BD = (5, 150, 105)

C_STOP_BG = (225, 29, 72)         # Rose 600
C_STOP_TXT = (255, 255, 255)
C_STOP_BD = (190, 18, 60)

C_PROC_BG = (255, 255, 255)
C_PROC_TXT_TITLE = (15, 23, 42)   # Slate 900
C_PROC_TXT_BODY = (71, 85, 105)   # Slate 600
C_PROC_BD = (59, 130, 246)        # Blue 500
C_PROC_ACCENT = (239, 246, 255)   # Blue 50

C_DEC_BG = (255, 251, 235)        # Amber 50
C_DEC_BD = (217, 119, 6)          # Amber 600
C_DEC_TXT_TITLE = (120, 53, 15)   # Amber 900
C_DEC_TXT_BODY = (180, 83, 9)     # Amber 700

C_IO_BG = (245, 243, 255)         # Purple 50
C_IO_BD = (124, 58, 237)          # Purple 600
C_IO_TXT_TITLE = (76, 29, 149)
C_IO_TXT_BODY = (109, 40, 217)

C_SUB_BG = (240, 253, 250)        # Teal 50
C_SUB_BD = (13, 148, 136)         # Teal 600
C_SUB_TXT_TITLE = (19, 78, 74)
C_SUB_TXT_BODY = (15, 118, 110)

C_ARROW = (51, 65, 85)            # Slate 700
C_LABEL_BG = (241, 245, 249)      # Slate 100
C_LABEL_TXT = (30, 41, 59)

def draw_header_banner(d, width, title, subtitle):
    # Banner background
    d.rectangle([0, 0, width, 90], fill=(15, 23, 42))
    d.rectangle([0, 86, width, 90], fill=(225, 29, 72)) # Crimson accent line
    
    font_t = get_font(24, bold=True)
    font_s = get_font(14, bold=False)
    
    d.text((40, 20), title, fill=(255, 255, 255), font=font_t)
    d.text((40, 55), subtitle, fill=(148, 163, 184), font=font_s)

def draw_pill(d, rect, text, is_start=True):
    x1, y1, x2, y2 = rect
    bg = C_START_END_BG if is_start else C_STOP_BG
    bd = C_START_END_BD if is_start else C_STOP_BD
    radius = (y2 - y1) // 2
    
    # Shadow
    d.rounded_rectangle([x1+2, y1+2, x2+2, y2+2], radius=radius, fill=SHADOW_COLOR)
    # Body
    d.rounded_rectangle([x1, y1, x2, y2], radius=radius, fill=bg, outline=bd, width=2)
    
    font = get_font(16, bold=True)
    bbox = font.getbbox(text)
    tw = bbox[2] - bbox[0]
    th = bbox[3] - bbox[1]
    tx = x1 + ((x2 - x1) - tw) // 2
    ty = y1 + ((y2 - y1) - th) // 2 - 2
    d.text((tx, ty), text, fill=(255, 255, 255), font=font)

def draw_process(d, rect, title, lines=None, border_color=C_PROC_BD, bg_color=CARD_BG, accent_color=C_PROC_ACCENT):
    x1, y1, x2, y2 = rect
    # Shadow
    d.rounded_rectangle([x1+3, y1+3, x2+3, y2+3], radius=10, fill=SHADOW_COLOR)
    # Box
    d.rounded_rectangle([x1, y1, x2, y2], radius=10, fill=bg_color, outline=border_color, width=2)
    # Accent top bar
    d.rounded_rectangle([x1+1, y1+1, x2-1, y1+28], radius=8, fill=accent_color)
    d.line([(x1+1, y1+28), (x2-1, y1+28)], fill=border_color, width=1)
    
    font_t = get_font(14, bold=True)
    font_b = get_font(12, bold=False)
    
    # Title
    d.text((x1 + 14, y1 + 7), title, fill=border_color, font=font_t)
    
    # Lines
    if lines:
        cur_y = y1 + 36
        for line in lines:
            d.text((x1 + 14, cur_y), line, fill=C_PROC_TXT_BODY, font=font_b)
            cur_y += 18

def draw_decision(d, center_x, center_y, width, height, title, lines=None):
    w2 = width // 2
    h2 = height // 2
    points = [
        (center_x, center_y - h2),       # Top
        (center_x + w2, center_y),       # Right
        (center_x, center_y + h2),       # Bottom
        (center_x - w2, center_y)        # Left
    ]
    # Shadow
    shadow_points = [(p[0]+3, p[1]+3) for p in points]
    d.polygon(shadow_points, fill=SHADOW_COLOR)
    # Diamond
    d.polygon(points, fill=C_DEC_BG, outline=C_DEC_BD)
    
    font_t = get_font(13, bold=True)
    font_b = get_font(11, bold=False)
    
    # Text calculation
    total_lines = [title] + (lines if lines else [])
    line_height = 16
    total_h = len(total_lines) * line_height
    start_y = center_y - total_h // 2 - 2
    
    for idx, l in enumerate(total_lines):
        f = font_t if idx == 0 else font_b
        col = C_DEC_TXT_TITLE if idx == 0 else C_DEC_TXT_BODY
        bbox = f.getbbox(l)
        tw = bbox[2] - bbox[0]
        tx = center_x - tw // 2
        d.text((tx, start_y + idx * line_height), l, fill=col, font=f)

def draw_io_box(d, rect, title, lines=None):
    x1, y1, x2, y2 = rect
    offset = 16
    points = [
        (x1 + offset, y1),
        (x2, y1),
        (x2 - offset, y2),
        (x1, y2)
    ]
    shadow_points = [(p[0]+3, p[1]+3) for p in points]
    d.polygon(shadow_points, fill=SHADOW_COLOR)
    d.polygon(points, fill=C_IO_BG, outline=C_IO_BD)
    
    font_t = get_font(14, bold=True)
    font_b = get_font(12, bold=False)
    
    d.text((x1 + offset + 10, y1 + 10), title, fill=C_IO_TXT_TITLE, font=font_t)
    if lines:
        cur_y = y1 + 32
        for line in lines:
            d.text((x1 + offset + 10, cur_y), line, fill=C_IO_TXT_BODY, font=font_b)
            cur_y += 18

def draw_subroutine(d, rect, title, lines=None, border_color=C_SUB_BD, bg_color=C_SUB_BG):
    x1, y1, x2, y2 = rect
    # Shadow
    d.rounded_rectangle([x1+3, y1+3, x2+3, y2+3], radius=8, fill=SHADOW_COLOR)
    # Box
    d.rounded_rectangle([x1, y1, x2, y2], radius=8, fill=bg_color, outline=border_color, width=2)
    # Inner side stripes (standard subroutine representation)
    d.line([(x1 + 12, y1), (x1 + 12, y2)], fill=border_color, width=2)
    d.line([(x2 - 12, y1), (x2 - 12, y2)], fill=border_color, width=2)
    
    font_t = get_font(14, bold=True)
    font_b = get_font(12, bold=False)
    
    d.text((x1 + 22, y1 + 10), title, fill=border_color, font=font_t)
    if lines:
        cur_y = y1 + 34
        for line in lines:
            d.text((x1 + 22, cur_y), line, fill=C_SUB_TXT_BODY, font=font_b)
            cur_y += 18

def draw_arrow(d, p1, p2, label=None, label_side="right"):
    # Draw line from p1 to p2
    x1, y1 = p1
    x2, y2 = p2
    d.line([(x1, y1), (x2, y2)], fill=C_ARROW, width=2)
    
    # Arrow head
    size = 7
    if x1 == x2: # Vertical
        if y2 > y1: # Downward
            d.polygon([(x2-size, y2-size*2), (x2+size, y2-size*2), (x2, y2)], fill=C_ARROW)
        else: # Upward
            d.polygon([(x2-size, y2+size*2), (x2+size, y2+size*2), (x2, y2)], fill=C_ARROW)
    elif y1 == y2: # Horizontal
        if x2 > x1: # Rightward
            d.polygon([(x2-size*2, y2-size), (x2-size*2, y2+size), (x2, y2)], fill=C_ARROW)
        else: # Leftward
            d.polygon([(x2+size*2, y2-size), (x2+size*2, y2+size), (x2, y2)], fill=C_ARROW)
            
    # Label
    if label:
        font = get_font(11, bold=True)
        bbox = font.getbbox(label)
        lw = bbox[2] - bbox[0]
        lh = bbox[3] - bbox[1]
        
        mid_x = (x1 + x2) // 2
        mid_y = (y1 + y2) // 2
        
        if label_side == "right":
            lx = mid_x + 8
            ly = mid_y - lh // 2
        elif label_side == "left":
            lx = mid_x - lw - 8
            ly = mid_y - lh // 2
        elif label_side == "top":
            lx = mid_x - lw // 2
            ly = mid_y - lh - 6
        else: # bottom
            lx = mid_x - lw // 2
            ly = mid_y + 6
            
        d.rounded_rectangle([lx-3, ly-2, lx+lw+3, ly+lh+2], radius=4, fill=C_LABEL_BG, outline=BORDER_COLOR)
        d.text((lx, ly), label, fill=C_LABEL_TXT, font=font)

def draw_poly_arrow(d, points, label=None, label_idx=0, label_side="right"):
    # Polyline arrow
    for i in range(len(points) - 1):
        p1 = points[i]
        p2 = points[i+1]
        is_last = (i == len(points) - 2)
        if is_last:
            draw_arrow(d, p1, p2, label if i == label_idx else None, label_side)
        else:
            d.line([p1, p2], fill=C_ARROW, width=2)
            if i == label_idx and label:
                font = get_font(11, bold=True)
                bbox = font.getbbox(label)
                lw = bbox[2] - bbox[0]
                lh = bbox[3] - bbox[1]
                mid_x = (p1[0] + p2[0]) // 2
                mid_y = (p1[1] + p2[1]) // 2
                if label_side == "right":
                    lx = mid_x + 8
                    ly = mid_y - lh // 2
                elif label_side == "left":
                    lx = mid_x - lw - 8
                    ly = mid_y - lh // 2
                elif label_side == "top":
                    lx = mid_x - lw // 2
                    ly = mid_y - lh - 6
                else:
                    lx = mid_x - lw // 2
                    ly = mid_y + 6
                d.rounded_rectangle([lx-3, ly-2, lx+lw+3, ly+lh+2], radius=4, fill=C_LABEL_BG, outline=BORDER_COLOR)
                d.text((lx, ly), label, fill=C_LABEL_TXT, font=font)

print("Flowchart helper functions defined successfully.")
