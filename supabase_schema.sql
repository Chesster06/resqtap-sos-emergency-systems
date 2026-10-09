-- ==============================================================================
-- ResQTap Supabase Database Schema Migration
-- Project Ref: umcxapvojtoxqlpnrwdw
-- Salin dan tampal skrip ini ke dalam Supabase Dashboard -> SQL Editor -> Klik "Run"
-- ==============================================================================

-- 1. Aktifkan sambungan PostGIS untuk carian geografi & koordinat GPS
CREATE EXTENSION IF NOT EXISTS postgis;

-- ==============================================================================
-- 2. JADUAL: profiles (Profil Pengguna & Tetapan)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.profiles (
    id TEXT PRIMARY KEY, -- UID daripada Firebase Auth atau Supabase Auth
    email TEXT,
    full_name TEXT,
    phone TEXT,
    photo_url TEXT,
    role TEXT DEFAULT 'user',
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    is_online BOOLEAN DEFAULT false,
    battery_pct INTEGER,
    last_seen TIMESTAMPTZ DEFAULT NOW(),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Migrasi sekiranya jadual profiles sudah wujud:
ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS latitude DOUBLE PRECISION;
ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS longitude DOUBLE PRECISION;
ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS is_online BOOLEAN DEFAULT false;
ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS battery_pct INTEGER;
ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS last_seen TIMESTAMPTZ DEFAULT NOW();

-- ==============================================================================
-- 3. JADUAL: medical_cards (Kad Perubatan Pengguna)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.medical_cards (
    user_id TEXT PRIMARY KEY REFERENCES public.profiles(id) ON DELETE CASCADE,
    blood_type TEXT,
    allergies TEXT,
    medical_conditions TEXT,
    medications TEXT,
    emergency_contacts JSONB DEFAULT '[]'::jsonb,
    notes TEXT,
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ==============================================================================
-- 4. JADUAL: hospitals (Senarai Hospital & Pusat Bantuan Berdekatan)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.hospitals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    address TEXT,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    category TEXT DEFAULT 'hospital', -- 'hospital', 'fire', 'police'
    phone TEXT,
    location GEOGRAPHY(Point, 4326),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Index Spatial untuk carian pantas PostGIS
CREATE INDEX IF NOT EXISTS idx_hospitals_location ON public.hospitals USING GIST (location);

-- Trigger automatik jana geometry Point daripada lat/lng semasa insert/update
CREATE OR REPLACE FUNCTION update_hospital_geom()
RETURNS TRIGGER AS $$
BEGIN
    NEW.location := ST_SetSRID(ST_MakePoint(NEW.longitude, NEW.latitude), 4326)::geography;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_update_hospital_geom ON public.hospitals;
CREATE TRIGGER trg_update_hospital_geom
BEFORE INSERT OR UPDATE ON public.hospitals
FOR EACH ROW EXECUTE FUNCTION update_hospital_geom();

-- Fungsi Carian Jarak Radius Hospital (PostGIS Function)
CREATE OR REPLACE FUNCTION get_nearby_hospitals(
    user_lat DOUBLE PRECISION,
    user_lng DOUBLE PRECISION,
    radius_meters DOUBLE PRECISION DEFAULT 10000,
    filter_category TEXT DEFAULT 'all'
)
RETURNS TABLE (
    id UUID,
    name TEXT,
    address TEXT,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    category TEXT,
    phone TEXT,
    distance_meters DOUBLE PRECISION
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        h.id,
        h.name,
        h.address,
        h.latitude,
        h.longitude,
        h.category,
        h.phone,
        ST_Distance(h.location, ST_SetSRID(ST_MakePoint(user_lng, user_lat), 4326)::geography) AS distance_meters
    FROM public.hospitals h
    WHERE 
        (filter_category = 'all' OR h.category = filter_category)
        AND ST_DWithin(h.location, ST_SetSRID(ST_MakePoint(user_lng, user_lat), 4326)::geography, radius_meters)
    ORDER BY distance_meters ASC;
END;
$$ LANGUAGE plpgsql STABLE;

-- ==============================================================================
-- 5. JADUAL: reports (Laporan Insiden & Kecemasan)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id TEXT,
    user_name TEXT,
    title TEXT NOT NULL,
    description TEXT,
    category TEXT, -- 'accident', 'medical', 'fire', 'crime'
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    address TEXT,
    image_url TEXT,
    status TEXT DEFAULT 'pending', -- 'pending', 'in_progress', 'resolved'
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ==============================================================================
-- 6. JADUAL: rooms & room_members (Bilik Kecemasan)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.rooms (
    id TEXT PRIMARY KEY, -- Kod bilik cth: 'ABC123'
    name TEXT NOT NULL,
    creator_uid TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.room_members (
    room_id TEXT REFERENCES public.rooms(id) ON DELETE CASCADE,
    user_uid TEXT NOT NULL,
    user_name TEXT,
    role TEXT DEFAULT 'member', -- 'creator', 'member', 'admin'
    joined_at TIMESTAMPTZ DEFAULT NOW(),
    PRIMARY KEY (room_id, user_uid)
);

-- ==============================================================================
-- 7. ROW LEVEL SECURITY (RLS) POLICIES (HARDENED)
-- ==============================================================================
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.medical_cards ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.hospitals ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.reports ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.rooms ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.room_members ENABLE ROW LEVEL SECURITY;

-- 7.0 Fungsi Pembantu Admin Selamat (Mengelakkan Recursive Policy / Error 42P17)
CREATE OR REPLACE FUNCTION public.is_app_admin()
RETURNS boolean
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = public
AS $$
    SELECT EXISTS (
        SELECT 1 FROM public.profiles 
        WHERE id = auth.uid() AND (role = 'admin' OR is_admin = true)
    );
$$;

-- 7.1 Hospitals: Awam boleh membaca hospital terdekat, hanya admin boleh sunting
CREATE POLICY "Public Read Hospitals" ON public.hospitals FOR SELECT USING (true);
CREATE POLICY "Admin Manage Hospitals" ON public.hospitals FOR ALL 
    USING (public.is_app_admin());

-- 7.2 Profiles: Pengguna hanya boleh melihat dan menyunting profil sendiri (atau dibaca oleh admin)
CREATE POLICY "Read Profiles" ON public.profiles FOR SELECT 
    USING (auth.uid() = id OR public.is_app_admin());
CREATE POLICY "Update Own Profile" ON public.profiles FOR UPDATE 
    USING (auth.uid() = id);
CREATE POLICY "Insert Own Profile" ON public.profiles FOR INSERT 
    WITH CHECK (auth.uid() = id);

-- 7.3 Medical Cards: Data perubatan peribadi HANYA boleh diakses oleh pemilik atau responden/admin kecemasan
CREATE POLICY "Read Medical Cards" ON public.medical_cards FOR SELECT 
    USING (auth.uid() = user_id OR public.is_app_admin());
CREATE POLICY "Modify Own Medical Card" ON public.medical_cards FOR ALL 
    USING (auth.uid() = user_id);

-- 7.4 Reports: Laporan boleh dibaca oleh pengirim atau admin; boleh dicipta oleh pengguna sah
CREATE POLICY "Read Reports" ON public.reports FOR SELECT 
    USING (auth.uid() = reporter_uid OR public.is_app_admin());
CREATE POLICY "Insert Reports" ON public.reports FOR INSERT 
    WITH CHECK (auth.uid() = reporter_uid OR auth.uid() IS NOT NULL);
CREATE POLICY "Admin Update Reports" ON public.reports FOR UPDATE 
    USING (public.is_app_admin());

-- 7.5 Rooms & Members: Hanya ahli atau pencipta bilik boleh membaca dan mengurus
CREATE POLICY "Read Rooms" ON public.rooms FOR SELECT 
    USING (auth.uid() = creator_uid OR EXISTS (SELECT 1 FROM public.room_members WHERE room_id = public.rooms.id AND user_uid = auth.uid()));
CREATE POLICY "Manage Own Rooms" ON public.rooms FOR ALL 
    USING (auth.uid() = creator_uid);
CREATE POLICY "Read Room Members" ON public.room_members FOR SELECT 
    USING (EXISTS (SELECT 1 FROM public.room_members rm WHERE rm.room_id = public.room_members.room_id AND rm.user_uid = auth.uid()));
CREATE POLICY "Join Or Leave Room" ON public.room_members FOR ALL 
    USING (auth.uid() = user_uid OR EXISTS (SELECT 1 FROM public.rooms r WHERE r.id = public.room_members.room_id AND r.creator_uid = auth.uid()));

-- ==============================================================================
-- 8. DATA CONTOH: Hospital Utama Malaysia (Initial Seed)
-- ==============================================================================
INSERT INTO public.hospitals (name, address, latitude, longitude, category, phone) VALUES
('Hospital Kuala Lumpur (HKL)', 'Jalan Pahang, 50586 Kuala Lumpur', 3.1714, 101.7025, 'hospital', '03-26155555'),
('Hospital Selayang', 'Lebuhraya Selayang - Kepong, 68100 Batu Caves, Selangor', 3.2423, 101.6469, 'hospital', '03-61263333'),
('Hospital Sungai Buloh', 'Jalan Hospital, 47000 Sungai Buloh, Selangor', 3.2201, 101.5815, 'hospital', '03-61454333'),
('Hospital Putrajaya', 'Pusat Pentadbiran Kerajaan Persekutuan, Presint 7, 62250 Putrajaya', 2.9292, 101.6742, 'hospital', '03-83124200'),
('Balai Bomba & Penyelamat Hang Tuah', 'Jalan Hang Tuah, 55200 Kuala Lumpur', 3.1415, 101.7061, 'fire', '03-21484444'),
('Balai Polis Dang Wangi', 'Jalan Dang Wangi, 50100 Kuala Lumpur', 3.1565, 101.6993, 'police', '03-26010222')
ON CONFLICT DO NOTHING;
