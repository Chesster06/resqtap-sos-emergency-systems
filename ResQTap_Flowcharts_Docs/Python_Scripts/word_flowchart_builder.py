import os
import win32com.client

class WordFlowchartBuilder:
    def __init__(self, doc, sec, W, H, page_size="A4", anchor=None):
        self.doc = doc
        self.sec = sec
        self.W = W
        self.H = H
        self.page_size = page_size
        
        # Setup page orientation and size
        self.sec.PageSetup.Orientation = 1 # 1 = Landscape
        if page_size == "A3":
            # A3 Landscape: 1190.55 x 841.89 pt (420 x 297 mm)
            self.sec.PageSetup.PageWidth = 1191
            self.sec.PageSetup.PageHeight = 842
            self.margin = 15
        else:
            # A4 Landscape: 841.89 x 595.28 pt (297 x 210 mm)
            self.sec.PageSetup.PageWidth = 842
            self.sec.PageSetup.PageHeight = 595
            self.margin = 12
            
        self.sec.PageSetup.TopMargin = self.margin
        self.sec.PageSetup.BottomMargin = self.margin
        self.sec.PageSetup.LeftMargin = self.margin
        self.sec.PageSetup.RightMargin = self.margin
        
        self.cw = self.sec.PageSetup.PageWidth - (self.margin * 2)
        self.ch = self.sec.PageSetup.PageHeight - (self.margin * 2)
        
        # Calculate scale factor
        self.s = min((self.cw - 6) / float(W), (self.ch - 6) / float(H))
        
        # Center canvas horizontally and vertically
        self.off_x = self.margin + (self.cw - W * self.s) / 2.0
        self.off_y = self.margin + (self.ch - H * self.s) / 2.0
        
        # Add Canvas to section
        if anchor:
            self.canvas = self.doc.Shapes.AddCanvas(self.off_x, self.off_y, W * self.s, H * self.s, anchor)
        else:
            self.canvas = self.doc.Shapes.AddCanvas(self.off_x, self.off_y, W * self.s, H * self.s)
        self.canvas.Line.Visible = 0

    def sc(self, val):
        return float(val) * self.s

    def add_header_banner(self, width, title, subtitle):
        h_pt = self.sc(92)
        w_pt = self.sc(width)
        banner = self.canvas.CanvasItems.AddTextbox(1, 0, 0, w_pt, h_pt)
        banner.Fill.Solid()
        banner.Fill.ForeColor.RGB = 0xFFFFFF
        banner.Line.ForeColor.RGB = 0x000000
        banner.Line.Weight = 1.0
        
        tr = banner.TextFrame.TextRange
        tr.Text = f"{title}\r{subtitle}"
        
        p1 = tr.Paragraphs(1).Range
        p1.Font.Name = "Segoe UI"
        p1.Font.Size = 10.5 if self.page_size == "A3" else 8.5
        p1.Font.Bold = True
        p1.Font.Color = 0x000000
        
        p2 = tr.Paragraphs(2).Range
        p2.Font.Name = "Segoe UI"
        p2.Font.Size = 7.5 if self.page_size == "A3" else 6.2
        p2.Font.Bold = False
        p2.Font.Color = 0x404040
        
        tr.ParagraphFormat.Alignment = 0 # Left
        tr.ParagraphFormat.SpaceBefore = 0
        tr.ParagraphFormat.SpaceAfter = 1
        banner.TextFrame.MarginLeft = 8
        banner.TextFrame.MarginTop = 4

    def add_terminal(self, rect, text, is_start=True):
        x1, y1, x2, y2 = rect
        shape = self.canvas.CanvasItems.AddShape(5, self.sc(x1), self.sc(y1), self.sc(x2 - x1), self.sc(y2 - y1))
        shape.Fill.Solid()
        if is_start:
            shape.Fill.ForeColor.RGB = 0x000000
            shape.Line.ForeColor.RGB = 0x000000
            font_color = 0xFFFFFF
        else:
            shape.Fill.ForeColor.RGB = 0xFFFFFF
            shape.Line.ForeColor.RGB = 0x000000
            shape.Line.Weight = 1.2
            font_color = 0x000000
            
        tr = shape.TextFrame.TextRange
        tr.Text = text
        tr.Font.Name = "Segoe UI"
        tr.Font.Size = 9.0 if self.page_size == "A3" else 7.0
        tr.Font.Bold = True
        tr.Font.Color = font_color
        tr.ParagraphFormat.Alignment = 1 # Center
        tr.ParagraphFormat.SpaceBefore = 0
        tr.ParagraphFormat.SpaceAfter = 0
        
        shape.TextFrame.MarginTop = 0
        shape.TextFrame.MarginBottom = 0
        shape.TextFrame.MarginLeft = 1
        shape.TextFrame.MarginRight = 1
        return shape

    def add_process(self, rect, title, lines=None):
        x1, y1, x2, y2 = rect
        shape = self.canvas.CanvasItems.AddShape(5, self.sc(x1), self.sc(y1), self.sc(x2 - x1), self.sc(y2 - y1))
        shape.Fill.Solid()
        shape.Fill.ForeColor.RGB = 0xFFFFFF
        shape.Line.ForeColor.RGB = 0x000000
        shape.Line.Weight = 1.0
        
        all_text = title + ("\r" + "\r".join(lines) if lines else "")
        tr = shape.TextFrame.TextRange
        tr.Text = all_text
        
        p1 = tr.Paragraphs(1).Range
        p1.Font.Name = "Segoe UI"
        p1.Font.Size = 8.0 if self.page_size == "A3" else 6.3
        p1.Font.Bold = True
        p1.Font.Color = 0x000000
        
        if lines:
            for i in range(2, len(lines) + 2):
                p = tr.Paragraphs(i).Range
                p.Font.Name = "Segoe UI"
                p.Font.Size = 6.8 if self.page_size == "A3" else 5.2
                p.Font.Bold = False
                p.Font.Color = 0x303030
                
        tr.ParagraphFormat.Alignment = 1 # Center
        tr.ParagraphFormat.SpaceBefore = 0
        tr.ParagraphFormat.SpaceAfter = 0
        tr.ParagraphFormat.LineSpacing = 8.5 if self.page_size == "A3" else 6.5
        
        shape.TextFrame.MarginTop = 1
        shape.TextFrame.MarginBottom = 1
        shape.TextFrame.MarginLeft = 2
        shape.TextFrame.MarginRight = 2
        return shape

    def add_decision(self, cx, cy, w, h, title, lines=None):
        adj_h = h * 1.15
        shape = self.canvas.CanvasItems.AddShape(63, self.sc(cx - w/2.0), self.sc(cy - adj_h/2.0), self.sc(w), self.sc(adj_h))
        shape.Fill.Solid()
        shape.Fill.ForeColor.RGB = 0xFFFFFF
        shape.Line.ForeColor.RGB = 0x000000
        shape.Line.Weight = 1.0
        
        all_text = title + ("\r" + "\r".join(lines) if lines else "")
        tr = shape.TextFrame.TextRange
        tr.Text = all_text
        
        p1 = tr.Paragraphs(1).Range
        p1.Font.Name = "Segoe UI"
        p1.Font.Size = 7.5 if self.page_size == "A3" else 5.8
        p1.Font.Bold = True
        p1.Font.Color = 0x000000
        
        if lines:
            for i in range(2, len(lines) + 2):
                p = tr.Paragraphs(i).Range
                p.Font.Name = "Segoe UI"
                p.Font.Size = 6.5 if self.page_size == "A3" else 5.0
                p.Font.Bold = False
                p.Font.Color = 0x303030
                
        tr.ParagraphFormat.Alignment = 1
        tr.ParagraphFormat.SpaceBefore = 0
        tr.ParagraphFormat.SpaceAfter = 0
        tr.ParagraphFormat.LineSpacing = 8.0 if self.page_size == "A3" else 6.0
        
        shape.TextFrame.MarginTop = 0
        shape.TextFrame.MarginBottom = 0
        shape.TextFrame.MarginLeft = 1
        shape.TextFrame.MarginRight = 1
        return shape

    def add_io_box(self, rect, title, lines=None):
        x1, y1, x2, y2 = rect
        shape = self.canvas.CanvasItems.AddShape(64, self.sc(x1), self.sc(y1), self.sc(x2 - x1), self.sc(y2 - y1))
        shape.Fill.Solid()
        shape.Fill.ForeColor.RGB = 0xFFFFFF
        shape.Line.ForeColor.RGB = 0x000000
        shape.Line.Weight = 1.0
        
        all_text = title + ("\r" + "\r".join(lines) if lines else "")
        tr = shape.TextFrame.TextRange
        tr.Text = all_text
        
        p1 = tr.Paragraphs(1).Range
        p1.Font.Name = "Segoe UI"
        p1.Font.Size = 8.0 if self.page_size == "A3" else 6.2
        p1.Font.Bold = True
        p1.Font.Color = 0x000000
        
        if lines:
            for i in range(2, len(lines) + 2):
                p = tr.Paragraphs(i).Range
                p.Font.Name = "Segoe UI"
                p.Font.Size = 6.8 if self.page_size == "A3" else 5.2
                p.Font.Bold = False
                p.Font.Color = 0x303030
                
        tr.ParagraphFormat.Alignment = 1
        tr.ParagraphFormat.SpaceBefore = 0
        tr.ParagraphFormat.SpaceAfter = 0
        tr.ParagraphFormat.LineSpacing = 8.5 if self.page_size == "A3" else 6.5
        
        shape.TextFrame.MarginTop = 1
        shape.TextFrame.MarginBottom = 1
        shape.TextFrame.MarginLeft = 3
        shape.TextFrame.MarginRight = 3
        return shape

    def add_subroutine(self, rect, title, lines=None):
        x1, y1, x2, y2 = rect
        shape = self.canvas.CanvasItems.AddShape(66, self.sc(x1), self.sc(y1), self.sc(x2 - x1), self.sc(y2 - y1))
        shape.Fill.Solid()
        shape.Fill.ForeColor.RGB = 0xFFFFFF
        shape.Line.ForeColor.RGB = 0x000000
        shape.Line.Weight = 1.0
        
        all_text = title + ("\r" + "\r".join(lines) if lines else "")
        tr = shape.TextFrame.TextRange
        tr.Text = all_text
        
        p1 = tr.Paragraphs(1).Range
        p1.Font.Name = "Segoe UI"
        p1.Font.Size = 8.0 if self.page_size == "A3" else 6.2
        p1.Font.Bold = True
        p1.Font.Color = 0x000000
        
        if lines:
            for i in range(2, len(lines) + 2):
                p = tr.Paragraphs(i).Range
                p.Font.Name = "Segoe UI"
                p.Font.Size = 6.8 if self.page_size == "A3" else 5.2
                p.Font.Bold = False
                p.Font.Color = 0x303030
                
        tr.ParagraphFormat.Alignment = 1
        tr.ParagraphFormat.SpaceBefore = 0
        tr.ParagraphFormat.SpaceAfter = 0
        tr.ParagraphFormat.LineSpacing = 8.5 if self.page_size == "A3" else 6.5
        
        shape.TextFrame.MarginTop = 1
        shape.TextFrame.MarginBottom = 1
        shape.TextFrame.MarginLeft = 3
        shape.TextFrame.MarginRight = 3
        return shape

    def add_arrow(self, p1, p2, label=None, label_side="right"):
        x1, y1 = self.sc(p1[0]), self.sc(p1[1])
        x2, y2 = self.sc(p2[0]), self.sc(p2[1])
        conn = self.canvas.CanvasItems.AddConnector(1, x1, y1, x2, y2)
        conn.Line.ForeColor.RGB = 0x000000
        conn.Line.Weight = 1.0
        conn.Line.EndArrowheadStyle = 2 # msoArrowheadTriangle
        if label:
            self.add_label(p1, p2, label, label_side)
        return conn

    def add_poly_arrow(self, points, label=None, label_idx=0, label_side="right"):
        for i in range(len(points) - 1):
            p1 = points[i]
            p2 = points[i+1]
            x1, y1 = self.sc(p1[0]), self.sc(p1[1])
            x2, y2 = self.sc(p2[0]), self.sc(p2[1])
            conn = self.canvas.CanvasItems.AddConnector(1, x1, y1, x2, y2)
            conn.Line.ForeColor.RGB = 0x000000
            conn.Line.Weight = 1.0
            if i == len(points) - 2:
                conn.Line.EndArrowheadStyle = 2
            else:
                conn.Line.EndArrowheadStyle = 1 # none
                
            if i == label_idx and label:
                self.add_label(p1, p2, label, label_side)

    def add_label(self, p1, p2, label, label_side="right"):
        x1, y1 = self.sc(p1[0]), self.sc(p1[1])
        x2, y2 = self.sc(p2[0]), self.sc(p2[1])
        mx = (x1 + x2) / 2.0
        my = (y1 + y2) / 2.0
        
        char_w = 4.0 if self.page_size == "A3" else 3.2
        lw = len(label) * char_w + 8.0
        lh = 11.0
        
        margin_dist = 4.0
        if label_side == "right":
            lx = mx + margin_dist
            ly = my - lh / 2.0
        elif label_side == "left":
            lx = mx - lw - margin_dist
            ly = my - lh / 2.0
        elif label_side == "top":
            lx = mx - lw / 2.0
            ly = my - lh - margin_dist
        else: # bottom
            lx = mx - lw / 2.0
            ly = my + margin_dist
            
        lbl = self.canvas.CanvasItems.AddTextbox(1, lx, ly, lw, lh)
        lbl.Fill.Solid()
        lbl.Fill.ForeColor.RGB = 0xFFFFFF
        lbl.Line.Visible = 0
        
        tr = lbl.TextFrame.TextRange
        tr.Text = label
        tr.Font.Name = "Segoe UI"
        tr.Font.Size = 6.5 if self.page_size == "A3" else 5.2
        tr.Font.Bold = True
        tr.Font.Color = 0x000000
        tr.ParagraphFormat.Alignment = 1
        tr.ParagraphFormat.SpaceBefore = 0
        tr.ParagraphFormat.SpaceAfter = 0
        
        lbl.TextFrame.MarginTop = 0
        lbl.TextFrame.MarginBottom = 0
        lbl.TextFrame.MarginLeft = 1
        lbl.TextFrame.MarginRight = 1

    def add_line(self, p1, p2, color=0xB4B4B4, weight=1.0):
        x1, y1 = self.sc(p1[0]), self.sc(p1[1])
        x2, y2 = self.sc(p2[0]), self.sc(p2[1])
        conn = self.canvas.CanvasItems.AddConnector(1, x1, y1, x2, y2)
        conn.Line.ForeColor.RGB = color
        conn.Line.Weight = weight
        conn.Line.EndArrowheadStyle = 1 # none
