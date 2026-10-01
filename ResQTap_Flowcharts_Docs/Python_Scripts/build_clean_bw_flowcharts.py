import os
from PIL import Image, ImageDraw, ImageFont

OUTPUT_DIR = os.path.join(os.path.dirname(__file__), "docs_flowcharts_bw")
os.makedirs(OUTPUT_DIR, exist_ok=True)

FONT_BOLD_PATH = r"C:\Windows\Fonts\segoeuib.ttf"
FONT_REG_PATH = r"C:\Windows\Fonts\segoeui.ttf"

def get_font(size, bold=False):
    path = FONT_BOLD_PATH if bold else FONT_REG_PATH
    try:
        return ImageFont.truetype(path, size)
    except:
        return ImageFont.load_default()

# PURE MONOCHROME (BLACK & WHITE)
COLOR_BLACK = (0, 0, 0)
COLOR_DARK_GRAY = (35, 35, 35)
COLOR_WHITE = (255, 255, 255)
COLOR_LIGHT_GRAY = (245, 245, 245)
COLOR_LINE_GRAY = (180, 180, 180)

def draw_header_banner(d, width, title, subtitle):
    # Clean monochrome header with crisp double line
    d.rectangle([0, 0, width, 92], fill=COLOR_WHITE)
    d.line([(0, 90), (width, 90)], fill=COLOR_BLACK, width=2)
    d.line([(0, 93), (width, 93)], fill=COLOR_BLACK, width=1)
    
    font_t = get_font(21, bold=True)
    font_s = get_font(13, bold=False)
    
    d.text((50, 20), title, fill=COLOR_BLACK, font=font_t)
    d.text((50, 54), subtitle, fill=COLOR_DARK_GRAY, font=font_s)

def draw_terminal(d, rect, text, is_start=True):
    x1, y1, x2, y2 = rect
    # Standard ISO rounded rectangle (radius 10) - NO exaggerated capsule
    radius = 10
    if is_start:
        d.rounded_rectangle([x1, y1, x2, y2], radius=radius, fill=COLOR_BLACK, outline=COLOR_BLACK, width=2)
        font = get_font(15, bold=True)
        bbox = font.getbbox(text)
        tw = bbox[2] - bbox[0]
        th = bbox[3] - bbox[1]
        tx = x1 + ((x2 - x1) - tw) // 2
        ty = y1 + ((y2 - y1) - th) // 2 - 2
        d.text((tx, ty), text, fill=COLOR_WHITE, font=font)
    else:
        d.rounded_rectangle([x1, y1, x2, y2], radius=radius, fill=COLOR_WHITE, outline=COLOR_BLACK, width=2)
        font = get_font(15, bold=True)
        bbox = font.getbbox(text)
        tw = bbox[2] - bbox[0]
        th = bbox[3] - bbox[1]
        tx = x1 + ((x2 - x1) - tw) // 2
        ty = y1 + ((y2 - y1) - th) // 2 - 2
        d.text((tx, ty), text, fill=COLOR_BLACK, font=font)

def draw_process(d, rect, title, lines=None):
    x1, y1, x2, y2 = rect
    header_h = 26
    # Clean box
    d.rounded_rectangle([x1, y1, x2, y2], radius=6, fill=COLOR_WHITE, outline=COLOR_BLACK, width=2)
    # Header bar
    d.rounded_rectangle([x1+1, y1+1, x2-1, y1+header_h], radius=5, fill=COLOR_LIGHT_GRAY)
    d.line([(x1+1, y1+header_h), (x2-1, y1+header_h)], fill=COLOR_BLACK, width=1)
    
    font_t = get_font(13, bold=True)
    font_b = get_font(11, bold=False)
    
    d.text((x1 + 14, y1 + 5), title, fill=COLOR_BLACK, font=font_t)
    if lines:
        cur_y = y1 + 33
        for line in lines:
            d.text((x1 + 14, cur_y), line, fill=COLOR_DARK_GRAY, font=font_b)
            cur_y += 19

def draw_decision(d, center_x, center_y, width, height, title, lines=None):
    w2 = width // 2
    h2 = height // 2
    points = [
        (center_x, center_y - h2),
        (center_x + w2, center_y),
        (center_x, center_y + h2),
        (center_x - w2, center_y)
    ]
    # Clean Single Outline Diamond (NO double line!)
    d.polygon(points, fill=COLOR_WHITE, outline=COLOR_BLACK, width=2)
    
    font_t = get_font(12, bold=True)
    font_b = get_font(11, bold=False)
    
    total_lines = [title] + (lines if lines else [])
    line_height = 17
    total_h = len(total_lines) * line_height
    start_y = center_y - total_h // 2 - 2
    
    for idx, l in enumerate(total_lines):
        f = font_t if idx == 0 else font_b
        col = COLOR_BLACK if idx == 0 else COLOR_DARK_GRAY
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
    d.polygon(points, fill=COLOR_LIGHT_GRAY, outline=COLOR_BLACK)
    
    font_t = get_font(13, bold=True)
    font_b = get_font(11, bold=False)
    
    d.text((x1 + offset + 12, y1 + 10), title, fill=COLOR_BLACK, font=font_t)
    if lines:
        cur_y = y1 + 34
        for line in lines:
            d.text((x1 + offset + 12, cur_y), line, fill=COLOR_DARK_GRAY, font=font_b)
            cur_y += 20

def draw_subroutine(d, rect, title, lines=None):
    # Delegate cleanly to draw_process: removes vertical side stripes for a completely uniform, clean look!
    draw_process(d, rect, title, lines)

def draw_label_text(d, p1, p2, label, label_side="right"):
    if not label:
        return
    font = get_font(11, bold=True)
    bbox = font.getbbox(label)
    lw = bbox[2] - bbox[0]
    lh = bbox[3] - bbox[1]
    
    x1, y1 = p1
    x2, y2 = p2
    mid_x = (x1 + x2) // 2
    mid_y = (y1 + y2) // 2
    
    is_vertical = (x1 == x2)
    is_horizontal = (y1 == y2)
    
    # Strictly ensure labels are OUTSIDE lines, never centered or inside lines
    if is_vertical:
        if label_side not in ["left", "right"]:
            label_side = "right"
    elif is_horizontal:
        if label_side not in ["top", "bottom"]:
            label_side = "top"
            
    # Generous distance from the line (12px) to ensure clear spacing
    MARGIN = 12
    if label_side == "right":
        lx = mid_x + MARGIN
        ly = mid_y - lh // 2
    elif label_side == "left":
        lx = mid_x - lw - MARGIN
        ly = mid_y - lh // 2
    elif label_side == "top":
        lx = mid_x - lw // 2
        ly = mid_y - lh - MARGIN
    else: # bottom
        lx = mid_x - lw // 2
        ly = mid_y + MARGIN
        
    d.rectangle([lx-3, ly-2, lx+lw+3, ly+lh+2], fill=COLOR_WHITE)
    d.text((lx, ly), label, fill=COLOR_BLACK, font=font)

# CLEAN ARROW DRAWING: NO CAPSULES! Plain text next to arrow with white background mask to prevent collisions
def draw_arrow(d, p1, p2, label=None, label_side="right"):
    x1, y1 = p1
    x2, y2 = p2
    d.line([(x1, y1), (x2, y2)], fill=COLOR_BLACK, width=2)
    
    size = 7
    if x1 == x2: # Vertical
        if y2 > y1: # Down
            d.polygon([(x2-size, y2-size*2), (x2+size, y2-size*2), (x2, y2)], fill=COLOR_BLACK)
        else: # Up
            d.polygon([(x2-size, y2+size*2), (x2+size, y2+size*2), (x2, y2)], fill=COLOR_BLACK)
    elif y1 == y2: # Horizontal
        if x2 > x1: # Right
            d.polygon([(x2-size*2, y2-size), (x2-size*2, y2+size), (x2, y2)], fill=COLOR_BLACK)
        else: # Left
            d.polygon([(x2+size*2, y2-size), (x2+size*2, y2+size), (x2, y2)], fill=COLOR_BLACK)
            
    if label:
        draw_label_text(d, p1, p2, label, label_side)

def draw_poly_arrow(d, points, label=None, label_idx=0, label_side="right"):
    for i in range(len(points) - 1):
        p1 = points[i]
        p2 = points[i+1]
        is_last = (i == len(points) - 2)
        if is_last:
            draw_arrow(d, p1, p2, label if i == label_idx else None, label_side)
        else:
            d.line([p1, p2], fill=COLOR_BLACK, width=2)
            if i == label_idx and label:
                draw_label_text(d, p1, p2, label, label_side)

# ==============================================================================
# 1. FLOWCHART 1: OVERALL SYSTEM ARCHITECTURE (B&W REBUILT)
# ==============================================================================
def build_flowchart_1():
    W, H = 1680, 1260
    img = Image.new("RGB", (W, H), color=COLOR_WHITE)
    d = ImageDraw.Draw(img)
    
    draw_header_banner(d, W, 
        "FLOWCHART 1: OVERALL SYSTEM WORKFLOW & ARCHITECTURE", 
        "End-to-End System Navigation, Security Verification Gateways & Core Subsystems")
    
    draw_terminal(d, [740, 115, 940, 160], "START", is_start=True)
    draw_process(d, [670, 190, 1010, 270], "1.0 App Launch & Splash Screen", [
        "User launches ResQTap application",
        "SplashActivity checks permissions & Firebase config"
    ])
    draw_decision(d, 840, 345, 290, 85, "Is User Session", ["Active & Authenticated?"])
    
    draw_io_box(d, [160, 305, 510, 385], "2.0 Onboarding & Authentication", [
        "Login, Register, or Forgot Password flows",
        "Firebase Auth credential validation"
    ])
    
    draw_decision(d, 840, 480, 300, 85, "Is App Lock or", ["Biometrics Enabled?"])
    
    draw_process(d, [1180, 440, 1540, 520], "3.0 App Lock Gateway", [
        "Enter 4-Digit Security PIN or Fingerprint Scan",
        "Duress PIN: Covertly triggers Silent SOS"
    ])
    
    draw_process(d, [660, 585, 1020, 665], "4.0 Main Dashboard (MainActivity)", [
        "Displays Medical Profile, Battery Status & Alerts",
        "User selects emergency or community service"
    ])
    
    draw_decision(d, 840, 740, 280, 80, "User Action", ["Selection?"])
    
    # 5 Functional Modules
    draw_process(d, [70, 835, 340, 935], "ONETAP SOS", [
        "Instant SOS Trigger",
        "3-sec countdown & alarm",
        "Broadcasts to RTDB & FCM"
    ])
    draw_process(d, [375, 835, 655, 935], "EMERGENCY ROOMS", [
        "Create / Join Room (Code / QR)",
        "LiveRoomTrackingService (GPS)",
        "Google Maps & Room Chat"
    ])
    draw_process(d, [690, 835, 990, 935], "WEBRTC CALLS", [
        "Voice & Video Intercom",
        "WebRTC Signaling via RTDB",
        "Low-latency P2P Media Stream"
    ])
    draw_process(d, [1025, 835, 1315, 935], "NEARBY HOSPITALS", [
        "GPS location scan",
        "Geospatial medical facility query",
        "Emergency dial & navigation"
    ])
    draw_process(d, [1350, 835, 1610, 935], "REPORTS & NEWS", [
        "Submit incident report + photo",
        "Upload to Cloud Storage",
        "Verified medical news feeds"
    ])
    
    draw_process(d, [680, 1010, 1000, 1085], "5.0 Session Lifecycle & Logout", [
        "User completes activity or signs out",
        "Background services & session tokens synchronized"
    ])
    draw_terminal(d, [750, 1130, 930, 1175], "END", is_start=False)
    
    # Arrows with clean text labels (NO capsules)
    draw_arrow(d, (840, 160), (840, 190))
    draw_arrow(d, (840, 270), (840, 302))
    
    draw_arrow(d, (695, 345), (510, 345), label="No", label_side="top")
    draw_poly_arrow(d, [(335, 305), (335, 230), (670, 230)], label="Auth Success", label_idx=1, label_side="top")
    draw_arrow(d, (840, 387), (840, 437), label="Yes", label_side="right")
    
    draw_arrow(d, (990, 480), (1180, 480), label="Yes", label_side="top")
    draw_poly_arrow(d, [(1360, 520), (1360, 625), (1020, 625)], label="Unlocked", label_idx=1, label_side="top")
    draw_arrow(d, (840, 522), (840, 585), label="No", label_side="right")
    
    draw_arrow(d, (840, 665), (840, 700))
    
    draw_poly_arrow(d, [(700, 740), (205, 740), (205, 835)])
    draw_poly_arrow(d, [(788, 765), (515, 765), (515, 835)])
    draw_arrow(d, (840, 780), (840, 835))
    draw_poly_arrow(d, [(892, 765), (1170, 765), (1170, 835)])
    draw_poly_arrow(d, [(980, 740), (1480, 740), (1480, 835)])
    
    # 5 SEPARATE INDEPENDENT PATHS (NO MERGING, NO COMMON BUS LINE!)
    # 4.1 Far Left: Drops at x=205, enters left side of 5.0 at y=1060
    draw_poly_arrow(d, [(205, 935), (205, 1060), (670, 1060)])
    # 4.2 Mid Left: Drops at x=515, enters left side of 5.0 at y=1030
    draw_poly_arrow(d, [(515, 935), (515, 1030), (670, 1030)])
    # 4.3 Center: Direct vertical arrow into top center of 5.0
    draw_arrow(d, (840, 935), (840, 1010))
    # 4.4 Mid Right: Drops at x=1170, enters right side of 5.0 at y=1030
    draw_poly_arrow(d, [(1170, 935), (1170, 1030), (1010, 1030)])
    # 4.5 Far Right: Drops at x=1480, enters right side of 5.0 at y=1060
    draw_poly_arrow(d, [(1480, 935), (1480, 1060), (1010, 1060)])
    
    draw_arrow(d, (840, 1085), (840, 1130))
    
    path = os.path.join(OUTPUT_DIR, "flowchart_1_overall_architecture_bw.png")
    img.save(path)
    print("Saved:", path)

# ==============================================================================
# 2. FLOWCHART 2: AUTH & DURESS PIN (REBUILT - NO CUTOFFS & NO CAPSULES)
# ==============================================================================
def build_flowchart_2():
    # Widened to 1750 and Height 1320 with generous margins
    W, H = 1750, 1320
    img = Image.new("RGB", (W, H), color=COLOR_WHITE)
    d = ImageDraw.Draw(img)
    
    draw_header_banner(d, W, 
        "FLOWCHART 2: AUTHENTICATION, APP LOCK & DURESS PIN SAFEGUARD", 
        "Session Verification, 4-Digit Passkey / Biometric Authentication & Covert Silent SOS Protocol")
        
    draw_terminal(d, [775, 115, 975, 160], "START", is_start=True)
    draw_process(d, [705, 190, 1045, 270], "1.0 Launch SplashActivity", [
        "Initialize Firebase App & Check Auth State",
        "FirebaseAuth.getInstance().getCurrentUser()"
    ])
    draw_decision(d, 875, 345, 290, 80, "Is User Session", ["Valid & Active?"])
    
    # Left Branch: Onboarding
    draw_io_box(d, [180, 305, 540, 385], "2.0 GetStarted & Auth Options", [
        "Login, Register New Account, or Reset Password",
        "Email & Password verification via Firebase"
    ])
    draw_process(d, [180, 440, 540, 520], "2.1 Save User Profile Data", [
        "Save Demographics & Medical Card to RTDB",
        "Store FCM Device Token under /users/{uid}"
    ])
    
    # Center Branch: App Lock Decision
    draw_decision(d, 875, 480, 300, 80, "Is App Lock or", ["Fingerprint Enabled?"])
    
    draw_process(d, [705, 585, 1045, 665], "3.0 Display AppLockActivity", [
        "Presents secure 4-digit PIN keypad screen",
        "Provides BiometricPrompt fingerprint option"
    ])
    
    draw_decision(d, 875, 750, 290, 90, "Credential Evaluation", ["Input Match Type?"])
    
    # 1. Normal PIN / Bio OK (Center)
    draw_process(d, [735, 875, 1015, 955], "4.1 Access Granted (Normal)", [
        "AppLockManager.setUnlocked(true)",
        "App session unlocked for user"
    ])
    
    # 2. DURESS PIN (Right Branch) - SPACIOUS 130px HEIGHT FOR GENEROUS BOTTOM PADDING
    draw_process(d, [1180, 700, 1640, 830], "4.2 DURESS PIN PROTOCOL (SILENT SOS)", [
        "Matches UserPrefs.getDuressPin()",
        "Immediately writes Silent SOS Alert to RTDB",
        "NO sirens, NO strobe lights, NO on-screen alerts",
        "Unlocks standard app camouflage to protect user"
    ])
    
    # 3. Invalid PIN (Left Branch)
    draw_process(d, [150, 705, 530, 785], "4.3 Invalid PIN / Failed", [
        "Increment failed attempts counter",
        "Display haptic error feedback"
    ])
    
    draw_decision(d, 340, 850, 260, 80, "Failed Attempts", ["> 5 Consecutive?"])
    
    # Ample box height: 95px so text never touches border
    draw_process(d, [150, 945, 530, 1040], "4.4 Temporary Cooldown Lockout", [
        "Locks app keypad for 30 seconds",
        "Mitigates brute-force passkey guessing"
    ])
    
    # Navigation to Main
    draw_process(d, [705, 1060, 1045, 1140], "5.0 Navigate to MainActivity", [
        "Loads ResQTap Main Dashboard",
        "Emergency services ready in foreground"
    ])
    draw_terminal(d, [785, 1195, 965, 1240], "END", is_start=False)
    
    # --- ARROWS: 100% COLLISION-FREE & ZERO CROSSING GUARANTEED ---
    # Top trunk
    draw_arrow(d, (875, 160), (875, 190))
    draw_arrow(d, (875, 270), (875, 305))
    
    # Session decision
    draw_arrow(d, (730, 345), (540, 345), label="No", label_side="top")
    draw_arrow(d, (360, 385), (360, 440))
    # Direct horizontal entry to Lock decision: ZERO CROSSINGS!
    draw_arrow(d, (540, 480), (725, 480), label="Auth Success", label_side="top")
    draw_arrow(d, (875, 385), (875, 440), label="Yes", label_side="right")
    
    # App Lock decision -> 3.0 or Disabled
    draw_arrow(d, (875, 520), (875, 585), label="Yes", label_side="right")
    # Disabled bypass: travels on the outer perimeter (x = 1690) around 4.2 -> enters 5.0 at y = 1115
    draw_poly_arrow(d, [(1025, 480), (1690, 480), (1690, 1115), (1045, 1115)], label="Disabled", label_side="top")
    
    # 3.0 to Credential Evaluation
    draw_arrow(d, (875, 665), (875, 705))
    
    # Credential Evaluation: 3 Clean, Non-Crossing Branches
    # Center: Normal PIN
    draw_arrow(d, (875, 795), (875, 875), label="Valid PIN / Bio", label_side="right")
    draw_arrow(d, (875, 955), (875, 1060))
    
    # Right: Duress PIN
    draw_arrow(d, (1020, 750), (1180, 750), label="Duress Match", label_side="top")
    # From 4.2: drops at x = 1350, enters 5.0 at y = 1075 (Above y=1115 and inside x=1690: ZERO CROSSINGS!)
    draw_poly_arrow(d, [(1350, 830), (1350, 1075), (1045, 1075)], label="Camouflage App", label_side="right")
    
    # Left: Wrong PIN
    draw_arrow(d, (730, 750), (530, 750), label="Wrong PIN", label_side="top")
    draw_arrow(d, (340, 785), (340, 810))
    draw_arrow(d, (340, 890), (340, 945), label="Yes (>= 5)", label_side="right")
    
    # Left return loops: routed along outer rails on the left (x = 110 and x = 50) -> ZERO CROSSINGS!
    # Loop A: Tries left (< 5) -> returns via x = 110, y = 645 into 3.0
    draw_poly_arrow(d, [(210, 850), (110, 850), (110, 645), (705, 645)], label="No (Tries left)", label_side="top")
    # Loop B: Cooldown Expired (30s) -> returns via x = 50, y = 615 into 3.0
    draw_poly_arrow(d, [(150, 992), (50, 992), (50, 615), (705, 615)], label="Cooldown Expired", label_idx=1, label_side="right")
    
    # Main to END
    draw_arrow(d, (875, 1140), (875, 1195))
    
    path = os.path.join(OUTPUT_DIR, "flowchart_2_auth_security_bw.png")
    img.save(path)
    print("Saved:", path)

# ==============================================================================
# 3. FLOWCHART 3: ONETAP SOS DISPATCH (B&W REBUILT)
# ==============================================================================
def build_flowchart_3():
    W, H = 1700, 1620
    img = Image.new("RGB", (W, H), color=COLOR_WHITE)
    d = ImageDraw.Draw(img)
    
    draw_header_banner(d, W, 
        "FLOWCHART 3: ONETAP SOS EMERGENCY TRIGGER & INCIDENT DISPATCH", 
        "Rapid Response Workflow: 3-Second Countdown, Hardware Alarms, Firebase RTDB/FCM Blasting & Live Dispatch Timeline")
        
    draw_terminal(d, [750, 115, 950, 160], "START", is_start=True)
    draw_process(d, [660, 190, 1040, 270], "1.0 Tap OneTap SOS Button", [
        "User taps central SOS button on MainActivity",
        "Triggers modal SosBottomSheetController"
    ])
    draw_decision(d, 850, 345, 320, 85, "3-Second Countdown", ["(CountDownTimer Active)"])
    
    # Slide to Cancel
    draw_process(d, [1240, 310, 1580, 385], "2.0 Slide to Cancel", [
        "User slides cancellation bar (Progress >= 95%)",
        "Timer stopped & no alert dispatched to cloud"
    ])
    draw_terminal(d, [1360, 425, 1470, 465], "END", is_start=False)
    
    # Hardware Alarms
    draw_process(d, [650, 450, 1050, 550], "3.0 Local Hardware Alarms Activated", [
        "Blasts Maximum Decibel Siren (SosAudioManager)",
        "Pulses Camera Flashlight Strobe continuously",
        "Triggers Rhythmic Haptic Vibration (VibrateManager)"
    ])
    
    draw_io_box(d, [650, 585, 1050, 670], "4.0 Fetch GPS Location & User Rooms", [
        "fusedLocationClient.getLastLocation() -> Lat, Lng",
        "Query user's active rooms from /userRooms/{uid}"
    ])
    
    draw_decision(d, 850, 745, 290, 80, "Does User Have", ["Active Rooms?"])
    
    draw_subroutine(d, [370, 835, 770, 920], "5.1 Group Room Blasting", [
        "Publish alert to each /rooms/{code}/sosAlerts",
        "Include GPS coordinates, battery level & timestamp"
    ])
    draw_subroutine(d, [930, 835, 1330, 920], "5.2 DIRECT Fallback Channel", [
        "Publish to fallback /rooms/DIRECT/sosAlerts",
        "Register alert in global /sos_alerts queue"
    ])
    
    draw_subroutine(d, [620, 960, 1080, 1060], "6.0 Cloud Functions Automated Dispatch", [
        "Triggers onRoomSosCreated & onSosAlertCreated",
        "Collects FCM device tokens of all members & admins",
        "Blasts High-Priority FCM Push Notifications"
    ])
    
    draw_process(d, [120, 1105, 580, 1205], "7.1 Room Members / Responders", [
        "Receive FCM -> Launch SosAlarmActivity over Lockscreen",
        "Full-screen siren, strobe & vibration alert",
        "Options: Snooze Alarm, View Live Map, Call Victim"
    ])
    
    draw_process(d, [1120, 1105, 1580, 1205], "7.2 Web Admin Dashboard", [
        "Flashing emergency alert banner on console",
        "Admin clicks 'Reserve Case' (served = true, servedBy)",
        "Updates Timeline: Dispatched -> En Route -> On Scene"
    ])
    
    draw_process(d, [620, 1245, 1080, 1345], "8.0 Live Response Timeline (SosProgressActivity)", [
        "watchSosCaseReservation() detects admin reservation",
        "Auto-launches 4-step interactive progress screen:",
        "1. Dispatched -> 2. En Route -> 3. On Scene -> 4. Resolved"
    ])
    
    draw_decision(d, 850, 1415, 290, 75, "Incident Resolved /", ["Cancelled?"])
    draw_terminal(d, [760, 1485, 940, 1525], "END", is_start=False)
    
    # Arrows
    draw_arrow(d, (850, 160), (850, 190))
    draw_arrow(d, (850, 270), (850, 302))
    
    draw_arrow(d, (1010, 345), (1240, 345), label="Slide Cancel", label_side="top")
    draw_arrow(d, (1410, 385), (1410, 425))
    draw_arrow(d, (850, 387), (850, 450), label="Countdown Expired (0s)", label_side="right")
    
    draw_arrow(d, (850, 550), (850, 585))
    draw_arrow(d, (850, 670), (850, 705))
    
    draw_poly_arrow(d, [(705, 745), (570, 745), (570, 835)], label="Yes (Has Rooms)", label_side="top")
    draw_poly_arrow(d, [(995, 745), (1130, 745), (1130, 835)], label="No (No Rooms)", label_side="top")
    
    draw_poly_arrow(d, [(570, 920), (570, 940), (760, 940), (760, 960)])
    draw_poly_arrow(d, [(1130, 920), (1130, 940), (940, 940), (940, 960)])
    
    # EXIT DIRECTLY FROM LEFT AND RIGHT EDGES OF BOX 6.0
    draw_poly_arrow(d, [(620, 1010), (350, 1010), (350, 1105)], label="FCM to Members", label_side="top")
    draw_poly_arrow(d, [(1080, 1010), (1350, 1010), (1350, 1105)], label="RTDB Sync to Admin", label_side="top")
    
    # From 7.1 and 7.2 to 8.0
    draw_poly_arrow(d, [(350, 1205), (350, 1295), (620, 1295)])
    draw_poly_arrow(d, [(1350, 1205), (1350, 1295), (1080, 1295)], label="Admin Reserved", label_idx=1, label_side="top")
    
    draw_arrow(d, (850, 1345), (850, 1377))
    draw_arrow(d, (850, 1452), (850, 1485), label="Yes (Resolved)", label_side="right")
    
    path = os.path.join(OUTPUT_DIR, "flowchart_3_onetap_sos_bw.png")
    img.save(path)
    print("Saved:", path)

# ==============================================================================
# 4. FLOWCHART 4: EMERGENCY ROOMS & GPS (B&W REBUILT)
# ==============================================================================
def build_flowchart_4():
    W, H = 1680, 1420
    img = Image.new("RGB", (W, H), color=COLOR_WHITE)
    d = ImageDraw.Draw(img)
    
    draw_header_banner(d, W, 
        "FLOWCHART 4: EMERGENCY ROOM HUB & LIVE GPS TRACKING SERVICE", 
        "Room Coordination, 6-Char Code / QR Code Sharing, Foreground Background Tracking & Google Maps SDK")
        
    draw_terminal(d, [740, 115, 940, 160], "START", is_start=True)
    draw_process(d, [650, 190, 1030, 270], "1.0 Open Emergency Room Hub", [
        "User navigates to RoomListActivity",
        "Lists all active emergency groups the user belongs to"
    ])
    draw_decision(d, 840, 345, 270, 80, "Room Action", ["Selection?"])
    
    draw_process(d, [180, 425, 560, 525], "2.1 Create New Room", [
        "User specifies Room Name",
        "System generates unique 6-char code (e.g. RT9482)",
        "Registers current user as Room Owner"
    ])
    
    draw_process(d, [1110, 425, 1500, 520], "2.2 Join Existing Room", [
        "Option A: Manually enter 6-character room code",
        "Option B: Scan room QR Code (ScanQrActivity)"
    ])
    draw_decision(d, 1305, 585, 260, 80, "Verify Code", ["in Firebase RTDB?"])
    
    draw_process(d, [650, 615, 1030, 700], "3.0 Access RoomHubActivity", [
        "Displays member roster, live battery levels & online status",
        "Options: Send Bell ping, Open Room Chat, or View Live Map"
    ])
    
    draw_decision(d, 840, 770, 270, 80, "Member Interaction", ["Option?"])
    
    draw_subroutine(d, [180, 855, 560, 960], "4.1 Instant Bell / Ping Alert", [
        "Writes record to /rooms/{code}/bells/{toUid}",
        "Cloud Function onBellCreated triggers FCM vibration",
        "Target member receives high-priority haptic ping"
    ])
    
    draw_process(d, [650, 855, 1030, 960], "4.2 Group Chat (RoomChatActivity)", [
        "Secure end-to-end incident communication channel",
        "Exchange text messages and incident photos in real time",
        "Real-time database synchronization"
    ])
    
    draw_process(d, [1110, 855, 1500, 960], "4.3 Live Map (RoomMapActivity)", [
        "Launches interactive Google Maps SDK interface",
        "Requests ACCESS_FINE_LOCATION permissions"
    ])
    
    draw_subroutine(d, [1110, 1005, 1500, 1105], "5.0 LiveRoomTrackingService (Foreground)", [
        "Android Foreground Service (location | dataSync)",
        "FusedLocationProviderClient captures GPS updates",
        "Publishes Lat/Lng, speed, accuracy & battery to RTDB"
    ])
    
    draw_process(d, [1110, 1145, 1500, 1245], "6.0 Interactive Google Maps Interface", [
        "Renders custom avatar markers for all room members",
        "Tracks real-time movement and location history",
        "Click member to calculate distance & start navigation"
    ])
    
    draw_terminal(d, [750, 1325, 930, 1370], "END", is_start=False)
    
    # Arrows
    draw_arrow(d, (840, 160), (840, 190))
    draw_arrow(d, (840, 270), (840, 305))
    
    draw_poly_arrow(d, [(705, 345), (370, 345), (370, 425)], label="Create Room", label_side="top")
    draw_poly_arrow(d, [(975, 345), (1305, 345), (1305, 425)], label="Join Room", label_side="top")
    
    draw_poly_arrow(d, [(370, 525), (370, 655), (650, 655)], label="Room Created", label_idx=1, label_side="top")
    
    draw_arrow(d, (1305, 520), (1305, 545))
    draw_poly_arrow(d, [(1305, 625), (1305, 655), (1030, 655)], label="Valid (Found)", label_idx=1, label_side="top")
    draw_poly_arrow(d, [(1435, 585), (1550, 585), (1550, 472), (1500, 472)], label="Invalid Code", label_idx=1, label_side="right")
    
    draw_arrow(d, (840, 700), (840, 730))
    
    # Member Interaction Branches (100% straight central Room Chat!)
    draw_poly_arrow(d, [(705, 770), (370, 770), (370, 855)], label="Bell Ping", label_side="top")
    draw_arrow(d, (840, 810), (840, 855), label="Room Chat", label_side="right")
    draw_poly_arrow(d, [(975, 770), (1305, 770), (1305, 855)], label="Live Map", label_side="top")
    
    draw_arrow(d, (1305, 960), (1305, 1005))
    draw_arrow(d, (1305, 1105), (1305, 1145))
    
    # Clean, symmetrical, unmerged termination paths into END:
    # 4.1 enters Left side of END (symmetrical 380px arm)
    draw_poly_arrow(d, [(370, 960), (370, 1347), (750, 1347)])
    # 4.2 drops Straight Down into Top Center (100% pure vertical)
    draw_arrow(d, (840, 960), (840, 1325))
    # 6.0 enters Right side of END (symmetrical 375px arm)
    draw_poly_arrow(d, [(1305, 1245), (1305, 1347), (930, 1347)])
    
    path = os.path.join(OUTPUT_DIR, "flowchart_4_emergency_room_map_bw.png")
    img.save(path)
    print("Saved:", path)

# ==============================================================================
# 5. FLOWCHART 5: WEBRTC CALLS (B&W REBUILT)
# ==============================================================================
def build_flowchart_5():
    W, H = 1700, 1400
    img = Image.new("RGB", (W, H), color=COLOR_WHITE)
    d = ImageDraw.Draw(img)
    
    draw_header_banner(d, W, 
        "FLOWCHART 5: WEBRTC VOICE & VIDEO EMERGENCY INTERCOM", 
        "Firebase RTDB Signaling Pipeline, Peer-to-Peer (P2P) Handshake & Bi-directional Media Stream Management")
        
    draw_terminal(d, [750, 115, 950, 160], "START", is_start=True)
    draw_process(d, [650, 190, 1050, 270], "1.0 Caller Initiates Call", [
        "Selects mode: Voice Call (Audio) or Video Call (Camera)",
        "Generates unique Call ID (/calls/{callId}) with status 'ringing'"
    ])
    draw_subroutine(d, [640, 305, 1060, 410], "2.0 Signaling Client & SDP Offer Creation", [
        "Starts CallService (Foreground: microphone | camera)",
        "WebRTCClient generates local SDP Offer",
        "Uploads SDP Offer to /calls/{callId}/offer"
    ])
    draw_process(d, [640, 440, 1060, 545], "3.0 Receiver Detects Incoming Call", [
        "IncomingCallManager intercepts FCM push / RTDB event",
        "Launches IncomingCallActivity over Lockscreen",
        "Plays full-screen emergency ringtone & vibration"
    ])
    
    draw_decision(d, 850, 620, 290, 85, "Receiver Decision?", ["Action Selected"])
    
    draw_process(d, [150, 580, 500, 655], "4.1 Call Declined", [
        "Receiver taps 'Decline'",
        "Updates status to 'declined' in RTDB"
    ])
    
    draw_process(d, [1200, 580, 1550, 655], "4.2 Call Timeout (30s)", [
        "No response after 30 seconds of ringing",
        "Updates status to 'timeout' in RTDB"
    ])
    
    draw_process(d, [660, 715, 1040, 795], "4.3 Call Accepted", [
        "Receiver taps 'Accept'",
        "Updates status to 'connected' in RTDB"
    ])
    
    draw_subroutine(d, [620, 835, 1080, 940], "5.0 WebRTC Signaling Handshake (SDP Answer & ICE)", [
        "Receiver generates SDP Answer & writes to /calls/{callId}/answer",
        "Both endpoints exchange ICE Candidates via /calls/{callId}/iceCandidates",
        "Establishes direct Peer-to-Peer (P2P Mesh) connection"
    ])
    
    draw_process(d, [640, 975, 1060, 1080], "6.0 Active Low-Latency Media Session", [
        "Real-time bi-directional Audio & Video streaming",
        "User Controls: Mute Microphone, Speakerphone Toggle,",
        "and Front / Rear Camera Switch (video mode)"
    ])
    
    draw_decision(d, 850, 1165, 290, 75, "Call Terminated?", ["(Either Party)"])
    
    draw_process(d, [650, 1250, 1050, 1325], "7.0 Teardown & Resource Disposal", [
        "Updates status to 'ended' in Firebase RTDB",
        "Stops CallService & releases camera / microphone drivers"
    ])
    draw_terminal(d, [760, 1355, 940, 1390], "END", is_start=False)
    
    # Arrows
    draw_arrow(d, (850, 160), (850, 190))
    draw_arrow(d, (850, 270), (850, 305))
    draw_arrow(d, (850, 410), (850, 440))
    draw_arrow(d, (850, 545), (850, 577))
    
    draw_arrow(d, (705, 620), (500, 620), label="Decline", label_side="top")
    draw_arrow(d, (995, 620), (1200, 620), label="Timeout", label_side="top")
    draw_arrow(d, (850, 663), (850, 715), label="Accept", label_side="right")
    
    draw_arrow(d, (850, 795), (850, 835))
    draw_arrow(d, (850, 940), (850, 975))
    draw_arrow(d, (850, 1080), (850, 1127))
    
    draw_arrow(d, (850, 1202), (850, 1250), label="Yes (End)", label_side="right")
    draw_arrow(d, (850, 1325), (850, 1355))
    
    draw_poly_arrow(d, [(325, 655), (325, 1287), (650, 1287)])
    draw_poly_arrow(d, [(1375, 655), (1375, 1287), (1050, 1287)])
    
    path = os.path.join(OUTPUT_DIR, "flowchart_5_webrtc_call_bw.png")
    img.save(path)
    print("Saved:", path)

# ==============================================================================
# 6. FLOWCHART 6: INCIDENT REPORT & HOSPITAL (B&W REBUILT)
# ==============================================================================
def build_flowchart_6():
    W, H = 1700, 1280
    img = Image.new("RGB", (W, H), color=COLOR_WHITE)
    d = ImageDraw.Draw(img)
    
    draw_header_banner(d, W, 
        "FLOWCHART 6: COMMUNITY INCIDENT REPORTING & NEARBY HOSPITAL FINDER", 
        "Incident Submission with Photo Upload to Firebase Storage & Haversine Distance Geospatial Navigation")
        
    d.line([(850, 115), (850, 1230)], fill=COLOR_LINE_GRAY, width=2)
    
    # Left: Incident Report
    draw_terminal(d, [270, 115, 470, 160], "START (REPORT)", is_start=True)
    draw_process(d, [160, 190, 580, 270], "L1.0 Open ReportActivity", [
        "User accesses community incident form",
        "Camera & Location permissions validated"
    ])
    draw_io_box(d, [160, 300, 580, 380], "L2.0 Select Incident Category", [
        "Road Accident / Violent Crime / Medical Emergency",
        "Fire Hazard / Natural Disaster / Other"
    ])
    draw_io_box(d, [160, 410, 580, 490], "L3.0 Incident Details & Location", [
        "User enters detailed description of incident",
        "Current GPS coordinates captured automatically"
    ])
    draw_decision(d, 370, 570, 270, 75, "Is Photo Attachment", ["Included?"])
    
    draw_subroutine(d, [140, 650, 600, 735], "L4.1 Upload to Firebase Storage", [
        "Compress image & upload to /incident_reports/{id}.jpg",
        "Retrieve secure public download URL"
    ])
    
    draw_subroutine(d, [140, 775, 600, 860], "L5.0 Save Record to Firebase RTDB", [
        "Write complete JSON payload to /reports/{reportId}",
        "Include user UID, timestamp, category, GPS & photo URL"
    ])
    draw_process(d, [160, 900, 580, 975], "L6.0 Confirmation & Ticket Generation", [
        "Display submission receipt & ticket ID to user",
        "Notify emergency dispatch admin console"
    ])
    draw_terminal(d, [280, 1030, 460, 1070], "END", is_start=False)
    
    draw_arrow(d, (370, 160), (370, 190))
    draw_arrow(d, (370, 270), (370, 300))
    draw_arrow(d, (370, 380), (370, 410))
    draw_arrow(d, (370, 490), (370, 532))
    
    draw_arrow(d, (370, 607), (370, 650), label="Yes (With Photo)", label_side="right")
    draw_poly_arrow(d, [(505, 570), (640, 570), (640, 815), (600, 815)], label="No Photo", label_side="top")
    
    draw_arrow(d, (370, 735), (370, 775))
    draw_arrow(d, (370, 860), (370, 900))
    draw_arrow(d, (370, 975), (370, 1030))
    
    # Right: Nearby Hospitals
    draw_terminal(d, [1210, 115, 1410, 160], "START (HOSPITAL)", is_start=True)
    draw_process(d, [1100, 190, 1520, 270], "H1.0 Open NearbyHospitalActivity", [
        "User chooses 'Nearby Hospitals & Clinics' from menu",
        "Validates GPS hardware status & permissions"
    ])
    draw_subroutine(d, [1100, 305, 1520, 390], "H2.0 Acquire User GPS Coordinates", [
        "fusedLocationClient.getCurrentLocation()",
        "Retrieves accurate user Latitude & Longitude"
    ])
    draw_subroutine(d, [1100, 425, 1520, 510], "H3.0 Query Medical Geospatial API", [
        "Submits geospatial query to medical facilities API",
        "Fetches hospitals, trauma centers & clinics within radius"
    ])
    draw_process(d, [1100, 545, 1520, 630], "H4.0 Calculate Distance & Sort", [
        "Computes direct distance using Haversine formula",
        "Sorts facilities ascending from nearest to farthest"
    ])
    draw_io_box(d, [1100, 665, 1520, 755], "H5.0 Render Hospital Directory", [
        "Displays facility name, address, distance (km),",
        "operational status, and emergency telephone number"
    ])
    draw_decision(d, 1310, 830, 270, 75, "User Action", ["Selection?"])
    
    draw_process(d, [980, 915, 1270, 995], "H6.1 Emergency Phone Call", [
        "Launches Android Dialer with",
        "hospital emergency hotline"
    ])
    
    draw_process(d, [1350, 915, 1640, 995], "H6.2 Google Maps Navigation", [
        "Launches Google Maps Intent with",
        "turn-by-turn driving route"
    ])
    
    draw_terminal(d, [1220, 1045, 1400, 1085], "END", is_start=False)
    
    draw_arrow(d, (1310, 160), (1310, 190))
    draw_arrow(d, (1310, 270), (1310, 305))
    draw_arrow(d, (1310, 390), (1310, 425))
    draw_arrow(d, (1310, 510), (1310, 545))
    draw_arrow(d, (1310, 630), (1310, 665))
    draw_arrow(d, (1310, 755), (1310, 792))
    
    draw_poly_arrow(d, [(1175, 830), (1125, 830), (1125, 915)], label="Phone Call", label_side="top")
    draw_poly_arrow(d, [(1445, 830), (1495, 830), (1495, 915)], label="Navigation", label_side="top")
    
    draw_poly_arrow(d, [(1125, 995), (1125, 1065), (1220, 1065)])
    draw_poly_arrow(d, [(1495, 995), (1495, 1065), (1400, 1065)])
    
    path = os.path.join(OUTPUT_DIR, "flowchart_6_report_hospital_bw.png")
    img.save(path)
    print("Saved:", path)

# ==============================================================================
# 7. FLOWCHART 7: ADMIN PORTAL (B&W REBUILT)
# ==============================================================================
def build_flowchart_7():
    W, H = 1700, 1280
    img = Image.new("RGB", (W, H), color=COLOR_WHITE)
    d = ImageDraw.Draw(img)
    
    draw_header_banner(d, W, 
        "FLOWCHART 7: WEB ADMIN PORTAL & CLOUD BROADCAST SYSTEM", 
        "Admin Authentication, Real-Time SOS Incident Control, Targeted FCM Broadcasts & Database Maintenance")
        
    draw_terminal(d, [750, 115, 950, 160], "START", is_start=True)
    draw_process(d, [640, 190, 1060, 270], "1.0 Access Web Admin Portal", [
        "Admin navigates to web portal (server.js /admin)",
        "Authentication handled via Firebase Auth"
    ])
    draw_decision(d, 850, 345, 310, 85, "Does User Have", ["Admin Privileges?"])
    
    draw_process(d, [150, 305, 500, 385], "2.0 Access Denied (403)", [
        "Email domain is not @resqtap.com and no /admins/{uid} entry",
        "System rejects access to administration console"
    ])
    draw_terminal(d, [270, 430, 380, 470], "END", is_start=False)
    
    draw_process(d, [640, 460, 1060, 545], "3.0 Admin Dashboard (admin.html)", [
        "Live system statistics: Total Users, Active Rooms & Active Alerts",
        "Operator selects management action"
    ])
    
    draw_decision(d, 850, 635, 300, 80, "Admin Operation", ["Selection?"])
    
    draw_process(d, [80, 730, 500, 855], "4.1 Live SOS Incident Control", [
        "Listens to incoming alerts on /rooms/*/sosAlerts",
        "Click 'Reserve Case' (served = true, servedBy)",
        "Update progress timeline (Dispatched -> Resolved)",
        "Direct emergency support chat with victim"
    ])
    
    draw_process(d, [610, 730, 1090, 840], "4.2 Targeted Broadcast System", [
        "Select target audience: All / Live Room Members / Admins",
        "Input alert title and notification message",
        "Invoke Callable Cloud Function: sendAdminNotification"
    ])
    
    draw_subroutine(d, [610, 880, 1090, 975], "4.2b Cloud Function FCM Blaster", [
        "Collects target user device FCM tokens",
        "Simultaneously dispatches High-Priority notifications",
        "Logs delivery audit to /admin_notifications/{id}"
    ])
    
    draw_process(d, [1180, 730, 1620, 840], "4.3 Maintenance & User Purge", [
        "Trigger user purge via /admin_user_deletions/{uid}",
        "Cloud Function purges user from Auth & RTDB completely",
        "Trigger DB account reset via /api/admin/clear-database"
    ])
    
    draw_process(d, [650, 1050, 1050, 1125], "5.0 Database Synchronized", [
        "Operation completed and synced to Firebase",
        "Dashboard statistics refreshed"
    ])
    draw_terminal(d, [760, 1170, 940, 1210], "END", is_start=False)
    
    # Arrows
    draw_arrow(d, (850, 160), (850, 190))
    draw_arrow(d, (850, 270), (850, 302))
    
    draw_arrow(d, (695, 345), (500, 345), label="No (Non-Admin)", label_side="top")
    draw_arrow(d, (325, 385), (325, 430))
    draw_arrow(d, (850, 387), (850, 460), label="Yes (Admin Granted)", label_side="right")
    
    draw_arrow(d, (850, 545), (850, 595))
    
    draw_poly_arrow(d, [(700, 635), (290, 635), (290, 730)], label="Manage SOS", label_side="top")
    draw_arrow(d, (850, 675), (850, 730), label="Broadcast", label_side="right")
    draw_poly_arrow(d, [(1000, 635), (1400, 635), (1400, 730)], label="Maintenance", label_side="top")
    
    draw_arrow(d, (850, 840), (850, 880))
    draw_arrow(d, (850, 975), (850, 1050))
    
    draw_poly_arrow(d, [(290, 855), (290, 1087), (650, 1087)])
    draw_poly_arrow(d, [(1400, 840), (1400, 1087), (1050, 1087)])
    
    draw_arrow(d, (850, 1125), (850, 1170))
    
    path = os.path.join(OUTPUT_DIR, "flowchart_7_admin_portal_cloud_bw.png")
    img.save(path)
    print("Saved:", path)

if __name__ == "__main__":
    print("Rebuilding all 7 flowcharts without capsules and with generous padding...")
    build_flowchart_1()
    build_flowchart_2()
    build_flowchart_3()
    build_flowchart_4()
    build_flowchart_5()
    build_flowchart_6()
    build_flowchart_7()
    print("Successfully rebuilt all 7 clean B&W flowcharts!")
