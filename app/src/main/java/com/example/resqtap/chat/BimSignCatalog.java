package com.example.resqtap.chat;

import com.example.resqtap.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Katalog rasmi 112 isyarat ekspresi BIM SignBank.
 * Menggunakan imej terus daripada res/drawable tanpa memerlukan fail JSON/assets.
 */
public final class BimSignCatalog {
    private static List<BimSignItem> cachedList;

    private BimSignCatalog() {}

    public static synchronized List<BimSignItem> getSigns() {
        if (cachedList != null) {
            return cachedList;
        }
        List<BimSignItem> list = new ArrayList<>(112);
        list.add(new BimSignItem(4154, "Amat jelas", "Very clear", R.drawable.img_bim_amat_jelas, "https://youtu.be/GaCKQfy2Oio", "", ""));
        list.add(new BimSignItem(4156, "Amboi kaya", "How wealthy!", R.drawable.img_bim_amboi_kaya, "https://youtu.be/aGUKqY0lET8", "", ""));
        list.add(new BimSignItem(4158, "Apa gunanya", "What for", R.drawable.img_bim_apa_gunanya, "https://youtu.be/1jVd_me1il4", "", ""));
        list.add(new BimSignItem(4160, "Asyik rindu", "Absorbed, keep thinking about something or someone", R.drawable.img_bim_asyik_rindu, "https://youtu.be/dSRMvVUlc90", "", ""));
        list.add(new BimSignItem(4162, "Bagaimana awak tahu?", "How do you know?", R.drawable.img_bim_bagaimana_awak_tahu, "https://youtu.be/sEPD1WBsPws", "", ""));
        list.add(new BimSignItem(4164, "Balik ke rumah", "Going home", R.drawable.img_bim_balik_ke_rumah, "https://youtu.be/e2qZDr-DRkY", "", ""));
        list.add(new BimSignItem(4166, "Beli dulu, bayar kemudian", "Buy first, pay later", R.drawable.img_bim_beli_dulu_bayar_kemudian, "https://youtu.be/fHyJRyjZ02Q", "", ""));
        list.add(new BimSignItem(4168, "Beres!", "Done!", R.drawable.img_bim_beres, "https://youtu.be/ODpSGbi4kw8", "", ""));
        list.add(new BimSignItem(4170, "Bergoyang kaki", "Live easily, loaf around", R.drawable.img_bim_bergoyang_kaki, "https://youtu.be/vjoMsLNkgbA", "", ""));
        list.add(new BimSignItem(4172, "Berlengah-lengah", "Keep delaying", R.drawable.img_bim_berlengah_lengah, "https://youtu.be/lwQfKN4DgJA", "", ""));
        list.add(new BimSignItem(4174, "Bernasib baik!", "Lucky!", R.drawable.img_bim_bernasib_baik, "https://youtu.be/HsKF1MYq9Wo", "", ""));
        list.add(new BimSignItem(4176, "Betapa rumitnya!", "How complicated!", R.drawable.img_bim_betapa_rumitnya, "https://youtu.be/ndcxReHXr8M", "", ""));
        list.add(new BimSignItem(4178, "Buat apa?", "Do what?", R.drawable.img_bim_buat_apa, "https://youtu.be/wFDSCyyxFfg", "", ""));
        list.add(new BimSignItem(4180, "Buat terlebih dulu", "Let me do it first", R.drawable.img_bim_buat_terlebih_dulu, "https://youtu.be/H_Togr4DfwQ", "", ""));
        list.add(new BimSignItem(4182, "Buka rahsia", "Reveal secrets", R.drawable.img_bim_buka_rahsia, "https://youtu.be/hrE8ep-ISM0", "", ""));
        list.add(new BimSignItem(4184, "Cabut lari", "Run away", R.drawable.img_bim_cabut_lari, "https://youtu.be/YLcBaJSB1rQ", "", ""));
        list.add(new BimSignItem(4186, "Cepat (II)", "Hurry", R.drawable.img_bim_cepat_ii, "https://youtu.be/KyrvIEygCTQ", "", ""));
        list.add(new BimSignItem(4188, "Dari pagi hingga malam", "From morning till night", R.drawable.img_bim_dari_pagi_hingga_malam, "https://youtu.be/8h2XfZhaNbw", "", ""));
        list.add(new BimSignItem(4190, "Di luar pengetahuan", "Beyond knowledge", R.drawable.img_bim_di_luar_pengetahuan, "https://youtu.be/MaWgu7xjTIM", "", ""));
        list.add(new BimSignItem(4192, "Diperbodohkan", "Being fooled", R.drawable.img_bim_diperbodohkan, "https://youtu.be/3ya_OSXXXxo", "", ""));
        list.add(new BimSignItem(4194, "Bijak!", "Genius!", R.drawable.img_bim_bijak, "https://youtu.be/5AKToEtlXmE", "", ""));
        list.add(new BimSignItem(4196, "Habislah awak!", "You are damned!", R.drawable.img_bim_habislah_awak, "https://youtu.be/ghCmygFWLpY", "", ""));
        list.add(new BimSignItem(4198, "Habislah saya!", "I am damned!", R.drawable.img_bim_habislah_saya, "https://youtu.be/MO8I_SWoHqU", "", ""));
        list.add(new BimSignItem(4200, "Imej terjejas", "Get a bad image", R.drawable.img_bim_imej_terjejas, "https://youtu.be/hL3LOWj1n1E", "", ""));
        list.add(new BimSignItem(4202, "Ingin melihat / tahu", "Curious to see / know", R.drawable.img_bim_ingin_melihat_tahu, "https://youtu.be/jgH-KOrwPxo", "", ""));
        list.add(new BimSignItem(4204, "Ingin tahu", "Curious", R.drawable.img_bim_ingin_tahu, "https://youtu.be/79j2swmREPI", "", ""));
        list.add(new BimSignItem(4206, "Janji kosong", "Empty promise", R.drawable.img_bim_janji_kosong, "https://youtu.be/iupyQSLEmMY", "", ""));
        list.add(new BimSignItem(4208, "Jatuh nama", "To get a bad name", R.drawable.img_bim_jatuh_nama, "https://youtu.be/_IfKRdsVWM8", "", ""));
        list.add(new BimSignItem(4210, "Karut!", "Bullshit!", R.drawable.img_bim_karut, "https://youtu.be/XelkY2v-S2s", "", ""));
        list.add(new BimSignItem(4212, "Kesuntukan masa", "Not much time", R.drawable.img_bim_kesuntukan_masa, "https://youtu.be/ZJngaKQy7Rg", "", ""));
        list.add(new BimSignItem(4214, "Kulit menjadi perang", "Cause to tan", R.drawable.img_bim_kulit_menjadi_perang, "https://youtu.be/sXyg_MOe0LQ", "", ""));
        list.add(new BimSignItem(4216, "Lain cerita", "Like to change topic", R.drawable.img_bim_lain_cerita, "https://youtu.be/fZpto0d1DS0", "", ""));
        list.add(new BimSignItem(4218, "Laju (bahasa isyarat)", "Too fast (sign language)", R.drawable.img_bim_laju_bahasa_isyarat, "https://youtu.be/C4FmMR_MADI", "", ""));
        list.add(new BimSignItem(4220, "Lebih baik daripada", "Better than", R.drawable.img_bim_lebih_baik_daripada, "https://youtu.be/1sJi_iEUzwI", "", ""));
        list.add(new BimSignItem(4222, "Lepas tangan", "Don't involve oneself", R.drawable.img_bim_lepas_tangan, "https://youtu.be/1aZeZNZNsg0", "", ""));
        list.add(new BimSignItem(4224, "Lupakan saja!", "Forget it!", R.drawable.img_bim_lupakan_saja, "https://youtu.be/Xtmjh9TFFI4", "", ""));
        list.add(new BimSignItem(4226, "Mahal, Sangat mahal", "Expensive, Exorbitant", R.drawable.img_bim_mahal_sangat_mahal, "https://youtu.be/_8Pl9333nK4", "", ""));
        list.add(new BimSignItem(4228, "Main-main saja", "Just kidding", R.drawable.img_bim_main_main_saja, "https://youtu.be/16JMo_z0kds", "", ""));
        list.add(new BimSignItem(4230, "Masuk telinga, keluar telinga", "Not Listening", R.drawable.img_bim_masuk_telinga_keluar_telinga, "https://youtu.be/pbDBWY-5tnI", "", ""));
        list.add(new BimSignItem(4232, "Mata terbeliak", "Wide-open eyes", R.drawable.img_bim_mata_terbeliak, "https://youtu.be/Iz4U8yeMX9Q", "", ""));
        list.add(new BimSignItem(4234, "Membasuh otak", "Brainwash", R.drawable.img_bim_membasuh_otak, "https://youtu.be/AQJiU8Qn-YI", "", ""));
        list.add(new BimSignItem(4236, "Memberi muka", "Give face", R.drawable.img_bim_memberi_muka, "https://youtu.be/B7Z7JHh-HQI", "", ""));
        list.add(new BimSignItem(4238, "Membiasakan diri", "Getting used to", R.drawable.img_bim_membiasakan_diri, "https://youtu.be/VOumaLDkbSQ", "", ""));
        list.add(new BimSignItem(4240, "Membuta tuli", "Reckless", R.drawable.img_bim_membuta_tuli, "https://youtu.be/MtUzHaPv5sc", "", ""));
        list.add(new BimSignItem(4242, "Mempunyai banyak wang", "Have plenty of money", R.drawable.img_bim_mempunyai_banyak_wang, "https://youtu.be/zqi3zonPZLc", "", ""));
        list.add(new BimSignItem(4244, "Mencari nama", "To make a name", R.drawable.img_bim_mencari_nama, "https://youtu.be/32dladF5ORE", "", ""));
        list.add(new BimSignItem(4246, "Mencuci mata", "Enjoy watching something or someone beautiful", R.drawable.img_bim_mencuci_mata, "https://youtu.be/wJjLp4t-jnI", "", ""));
        list.add(new BimSignItem(4248, "Mencuri tulang", "Steal time doing nothing", R.drawable.img_bim_mencuri_tulang, "https://youtu.be/FNCtuH1fDTQ", "", ""));
        list.add(new BimSignItem(4250, "Menebalkan muka", "Thick-skinned", R.drawable.img_bim_menebalkan_muka, "https://youtu.be/F_wcc4skOYw", "", ""));
        list.add(new BimSignItem(4252, "Mengambil kesempatan", "Take advantage of", R.drawable.img_bim_mengambil_kesempatan, "https://youtu.be/qfJZS2aGvpA", "", ""));
        list.add(new BimSignItem(4254, "Menyakitkan mata", "Eyesore", R.drawable.img_bim_menyakitkan_mata, "https://youtu.be/w6HW9aM5DZU", "", ""));
        list.add(new BimSignItem(4256, "Menyibuk", "Busybody", R.drawable.img_bim_menyibuk, "https://youtu.be/6OD93GbOKfQ", "", ""));
        list.add(new BimSignItem(4258, "Muak", "Sick of", R.drawable.img_bim_muak, "https://youtu.be/TBSXX6aYn2k", "", ""));
        list.add(new BimSignItem(4260, "Mudah saja", "It is easy", R.drawable.img_bim_mudah_saja, "https://youtu.be/x1egbtSG5Oc", "", ""));
        list.add(new BimSignItem(4262, "Nampak?", "Did you see?", R.drawable.img_bim_nampak, "https://youtu.be/_kPPxPz9k6o", "", ""));
        list.add(new BimSignItem(4264, "Nasib kamu baik!", "You are lucky!", R.drawable.img_bim_nasib_kamu_baik, "https://youtu.be/LFjyyXqRNo8", "", ""));
        list.add(new BimSignItem(4266, "Nyah!", "Get away!", R.drawable.img_bim_nyah, "https://youtu.be/8JtplRvh1Yg", "", ""));
        list.add(new BimSignItem(4268, "Oh! Begitu rupanya", "Oh! I see", R.drawable.img_bim_oh_begitu_rupanya, "https://youtu.be/B4QLneQyO6o", "", ""));
        list.add(new BimSignItem(4270, "Padan muka!", "Serve you right!", R.drawable.img_bim_padan_muka, "https://youtu.be/AJum1ZR8SFM", "", ""));
        list.add(new BimSignItem(4272, "Perkara kecil", "Small matter", R.drawable.img_bim_perkara_kecil, "https://youtu.be/Vww_busvw0g", "", ""));
        list.add(new BimSignItem(4274, "Prestasi baik", "Good performance", R.drawable.img_bim_prestasi_baik, "https://youtu.be/u_fcGdykVeY", "", ""));
        list.add(new BimSignItem(4276, "Prestasi buruk", "Bad performance", R.drawable.img_bim_prestasi_buruk, "https://youtu.be/VVoZtUG8PtA", "", ""));
        list.add(new BimSignItem(4278, "Putus hubungan", "Break off", R.drawable.img_bim_putus_hubungan, "https://youtu.be/bfaF3FuBj3c", "", ""));
        list.add(new BimSignItem(4280, "Rasa malas", "Feel lazy", R.drawable.img_bim_rasa_malas, "https://youtu.be/z1qM1YL671w", "", ""));
        list.add(new BimSignItem(4282, "Rugi masa", "Waste of time", R.drawable.img_bim_rugi_masa, "https://youtu.be/kbXE5OKBnus", "", ""));
        list.add(new BimSignItem(4284, "Rugilah", "It's a loss!", R.drawable.img_bim_rugilah, "https://youtu.be/-DusbuNzdcs", "", ""));
        list.add(new BimSignItem(4286, "Sambil lewa", "Half-heartedly", R.drawable.img_bim_sambil_lewa, "https://youtu.be/iFbp58siZzA", "", ""));
        list.add(new BimSignItem(4288, "Sangat mudah", "Very easy", R.drawable.img_bim_sangat_mudah, "https://youtu.be/AB7fhfMlnOQ", "", ""));
        list.add(new BimSignItem(4290, "Sebagai balasan", "In return", R.drawable.img_bim_sebagai_balasan, "https://youtu.be/LsFvSy5021U", "", ""));
        list.add(new BimSignItem(4292, "Semakin bertambah", "Increased", R.drawable.img_bim_semakin_bertambah, "https://youtu.be/LGuetBim3ac", "", ""));
        list.add(new BimSignItem(4294, "Semakin menurun", "Decreased", R.drawable.img_bim_semakin_menurun, "https://youtu.be/bKbnGuUJDfE", "", ""));
        list.add(new BimSignItem(4296, "Semua pandang saya", "Everyone looking at me", R.drawable.img_bim_semua_pandang_saya, "https://youtu.be/wxUBrayKJw8", "", ""));
        list.add(new BimSignItem(4298, "Senang sahaja", "It's easy", R.drawable.img_bim_senang_sahaja, "https://youtu.be/Gv4WtzQtcJs", "", ""));
        list.add(new BimSignItem(4300, "Sia-sia sahaja", "In vain", R.drawable.img_bim_sia_sia_sahaja, "https://youtu.be/BBhDouztKFk", "", ""));
        list.add(new BimSignItem(4302, "Siapa beritahu awak?", "Who told you?", R.drawable.img_bim_siapa_beritahu_awak, "https://youtu.be/Yc_9Aqqfd40", "", ""));
        list.add(new BimSignItem(4304, "Awak sudah berubah", "You have changed", R.drawable.img_bim_awak_sudah_berubah, "https://youtu.be/3Q2xnXxHpZ8", "", ""));
        list.add(new BimSignItem(4306, "Sudah biasa", "Used to it", R.drawable.img_bim_sudah_biasa, "https://youtu.be/9SGjsZnPfDk", "", ""));
        list.add(new BimSignItem(4308, "Sudah bosan", "Bored already", R.drawable.img_bim_sudah_bosan, "https://youtu.be/q96zRj05hYU", "", ""));
        list.add(new BimSignItem(4310, "Sudah lama", "Been a long time", R.drawable.img_bim_sudah_lama, "https://youtu.be/pzVCFsyLcMA", "", ""));
        list.add(new BimSignItem(4312, "Sudah terlambat", "It's too late", R.drawable.img_bim_sudah_terlambat, "https://youtu.be/6-GIHM83Jg8", "", ""));
        list.add(new BimSignItem(4314, "Sukar difahami", "Difficult to understand", R.drawable.img_bim_sukar_difahami, "https://youtu.be/S9rutk-P5Zw", "", ""));
        list.add(new BimSignItem(4316, "Tarik perhatian", "Get attention", R.drawable.img_bim_tarik_perhatian, "https://youtu.be/FnhEjjs2APQ", "", ""));
        list.add(new BimSignItem(4318, "Terlalu degil", "Very stubborn", R.drawable.img_bim_terlalu_degil, "https://youtu.be/29lloO0qIQg", "", ""));
        list.add(new BimSignItem(4320, "Terlepas cakap", "Accidentally said (Slip of the tongue)", R.drawable.img_bim_terlepas_cakap, "https://youtu.be/yr_2IEEWiKM", "", ""));
        list.add(new BimSignItem(4322, "Terlepas peluang", "Missed opportunity", R.drawable.img_bim_terlepas_peluang, "https://youtu.be/QeUcc7yCoGE", "", ""));
        list.add(new BimSignItem(4324, "Terpulang kepada (I)", "Up to (I)", R.drawable.img_bim_terpulang_kepada_i, "https://youtu.be/acQQMtKbhlE", "", ""));
        list.add(new BimSignItem(4326, "Terpulang kepada (II)", "Up to (II)", R.drawable.img_bim_terpulang_kepada_ii, "https://youtu.be/7mRIptxlXvk", "", ""));
        list.add(new BimSignItem(4328, "Tersilap", "Mistaken", R.drawable.img_bim_tersilap, "https://youtu.be/9xWu_K55mfQ", "", ""));
        list.add(new BimSignItem(4330, "Teruk!", "Worse!", R.drawable.img_bim_teruk, "https://youtu.be/GBGXls_y-l8", "", ""));
        list.add(new BimSignItem(4332, "Tiada ada kerja", "Nothing to do", R.drawable.img_bim_tiada_ada_kerja, "https://youtu.be/OAig5SY4f1E", "", ""));
        list.add(new BimSignItem(4334, "Tiada perasaan", "No feelings", R.drawable.img_bim_tiada_perasaan, "https://youtu.be/Mp2pdoynFUo", "", ""));
        list.add(new BimSignItem(4336, "Tidak apa", "Never mind", R.drawable.img_bim_tidak_apa, "https://youtu.be/z1dKTagEv_E", "", ""));
        list.add(new BimSignItem(4338, "Tidak beri perhatian", "Don't pay attention", R.drawable.img_bim_tidak_beri_perhatian, "https://youtu.be/6JxT48VVe0w", "", ""));
        list.add(new BimSignItem(4340, "Tidak dapat ingat", "Can't remember", R.drawable.img_bim_tidak_dapat_ingat, "https://youtu.be/CNrlC3Bdtmg", "", ""));
        list.add(new BimSignItem(4342, "Tidak dapat melihat", "Can't see", R.drawable.img_bim_tidak_dapat_melihat, "https://youtu.be/zmw28y_STzQ", "", ""));
        list.add(new BimSignItem(4344, "Tidak dengar apa-apa", "Don't hear anything", R.drawable.img_bim_tidak_dengar_apa_apa, "https://youtu.be/UszlIVzMWVs", "", ""));
        list.add(new BimSignItem(4346, "Tidak elok", "Not nice", R.drawable.img_bim_tidak_elok, "https://youtu.be/drKilTXt2WA", "", ""));
        list.add(new BimSignItem(4348, "Tidak kenal", "Do not know (person(s)", R.drawable.img_bim_tidak_kenal, "https://youtu.be/0c4_dX1qA5g", "", ""));
        list.add(new BimSignItem(4350, "Tidak kisah", "Don't care", R.drawable.img_bim_tidak_kisah, "https://youtu.be/n_2rC3y9DOM", "", ""));
        list.add(new BimSignItem(4352, "Tidak logik", "Don't make sense", R.drawable.img_bim_tidak_logik, "https://youtu.be/6stpEXLniCU", "", ""));
        list.add(new BimSignItem(4354, "Tidak mempunyai wang", "Have no money", R.drawable.img_bim_tidak_mempunyai_wang, "https://youtu.be/BLjdr2yGmQU", "", ""));
        list.add(new BimSignItem(4356, "Tidak menakutkan", "Not fearful", R.drawable.img_bim_tidak_menakutkan, "https://youtu.be/McUhGyN1W3s", "", ""));
        list.add(new BimSignItem(4358, "Tidak mengantuk", "Not sleepy", R.drawable.img_bim_tidak_mengantuk, "https://youtu.be/RX1qqv2bYS8", "", ""));
        list.add(new BimSignItem(4360, "Tidak menumpukan", "Inattentive", R.drawable.img_bim_tidak_menumpukan, "https://youtu.be/JaFhJ16Va2I", "", ""));
        list.add(new BimSignItem(4362, "Tidak pasti", "Unsure", R.drawable.img_bim_tidak_pasti, "https://youtu.be/_MBKRoOY_0Y", "", ""));
        list.add(new BimSignItem(4364, "Tidak perlu buat", "Don't have to do", R.drawable.img_bim_tidak_perlu_buat, "https://youtu.be/M_h26YvXzsU", "", ""));
        list.add(new BimSignItem(4366, "Tidak rasa apa-apa", "Do not feel anything", R.drawable.img_bim_tidak_rasa_apa_apa, "https://youtu.be/q77_UAiaJRA", "", ""));
        list.add(new BimSignItem(4368, "Tidak sedap", "Not delicious", R.drawable.img_bim_tidak_sedap, "https://youtu.be/OqtbDgA4RcI", "", ""));
        list.add(new BimSignItem(4370, "Tidak suka berpanas", "Cannot stand the heat", R.drawable.img_bim_tidak_suka_berpanas, "https://youtu.be/YRZ707fYXCg", "", ""));
        list.add(new BimSignItem(4372, "Tidak tahan", "Can't stand it", R.drawable.img_bim_tidak_tahan, "https://youtu.be/SJ-p7Z8gQdE", "", ""));
        list.add(new BimSignItem(4374, "Tidak tahu apa-apa", "Don't know anything", R.drawable.img_bim_tidak_tahu_apa_apa, "https://youtu.be/tNCSTPyYSmY", "", ""));
        list.add(new BimSignItem(4376, "Ulangi cerita sama", "Repeat the same story", R.drawable.img_bim_ulangi_cerita_sama, "https://youtu.be/dndeUvzPEj4", "", ""));
        cachedList = Collections.unmodifiableList(list);
        return cachedList;
    }
}