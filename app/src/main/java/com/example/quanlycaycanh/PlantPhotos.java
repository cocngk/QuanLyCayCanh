package com.example.quanlycaycanh;

import android.content.Context;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Bundled plant photos and the attribution required by their licenses. */
public final class PlantPhotos {

    private static final String PREFIX = "asset:";

    public static final String SEN_DA_MINI = PREFIX + "plant_sen_da_mini";
    public static final String XUONG_RONG_TAI_THO = PREFIX + "plant_xuong_rong_tai_tho";
    public static final String LUOI_HO_VANG = PREFIX + "plant_luoi_ho_vang";
    public static final String KIM_TIEN = PREFIX + "plant_kim_tien";
    public static final String PHAT_TAI = PREFIX + "plant_phat_tai";
    public static final String TRAU_BA_LA_XE = PREFIX + "plant_trau_ba_la_xe";
    public static final String CAU_CANH_DE_BAN = PREFIX + "plant_cau_canh_de_ban";
    public static final String BANG_SINGAPORE = PREFIX + "plant_bang_singapore";
    public static final String HOA_GIAY = PREFIX + "plant_hoa_giay";
    public static final String THIET_MOC_LAN = PREFIX + "plant_thiet_moc_lan";
    public static final String TRUC_MAY_MAN = PREFIX + "plant_truc_may_man";
    public static final String DUONG_XI_BOSTON = PREFIX + "plant_duong_xi_boston";

    private static final Map<String, Credit> CREDITS;

    static {
        Map<String, Credit> credits = new HashMap<>();
        add(credits, SEN_DA_MINI, "Eria Wei", "Echeveria elegans - 1.jpg",
                "CC BY-SA 4.0", "https://creativecommons.org/licenses/by-sa/4.0/",
                "https://commons.wikimedia.org/wiki/File:Echeveria_elegans_-_1.jpg");
        add(credits, XUONG_RONG_TAI_THO, "Stan Shebs", "Opuntia microdasys 3.jpg",
                "CC BY-SA 3.0", "https://creativecommons.org/licenses/by-sa/3.0/",
                "https://commons.wikimedia.org/wiki/File:Opuntia_microdasys_3.jpg");
        add(credits, LUOI_HO_VANG, "Mokkie",
                "Snake Plant (Sansevieria trifasciata 'Laurentii').jpg",
                "CC BY-SA 3.0", "https://creativecommons.org/licenses/by-sa/3.0/",
                "https://commons.wikimedia.org/wiki/File:Snake_Plant_(Sansevieria_trifasciata_%27Laurentii%27).jpg");
        add(credits, KIM_TIEN, "Mokkie", "ZZ Plant (Zamioculcas zamiifolia).jpg",
                "CC BY-SA 3.0", "https://creativecommons.org/licenses/by-sa/3.0/",
                "https://commons.wikimedia.org/wiki/File:ZZ_Plant_(Zamioculcas_zamiifolia).jpg");
        add(credits, PHAT_TAI, "Krzysztof Ziarnek (Kenraiz)",
                "Dracaena fragrans 'Compacta' kz01.jpg", "CC BY-SA 4.0",
                "https://creativecommons.org/licenses/by-sa/4.0/",
                "https://commons.wikimedia.org/wiki/File:Dracaena_fragrans_%27Compacta%27_kz01.jpg");
        add(credits, TRAU_BA_LA_XE, "Forest & Kim Starr",
                "Starr 080731-9571 Monstera deliciosa.jpg", "CC BY 3.0",
                "https://creativecommons.org/licenses/by/3.0/",
                "https://commons.wikimedia.org/wiki/File:Starr_080731-9571_Monstera_deliciosa.jpg");
        add(credits, CAU_CANH_DE_BAN, "Pluume321", "Chamaedorea elegans 02.jpg",
                "CC BY-SA 3.0", "https://creativecommons.org/licenses/by-sa/3.0/",
                "https://commons.wikimedia.org/wiki/File:Chamaedorea_elegans_02.jpg");
        add(credits, BANG_SINGAPORE, "Mokkie", "Fiddle leaf fig 5.jpg",
                "CC BY-SA 3.0", "https://creativecommons.org/licenses/by-sa/3.0/",
                "https://commons.wikimedia.org/wiki/File:Fiddle_leaf_fig_5.jpg");
        add(credits, HOA_GIAY, "Dinesh Valke", "Double Pink (332505440).jpg",
                "CC BY-SA 2.0", "https://creativecommons.org/licenses/by-sa/2.0/",
                "https://commons.wikimedia.org/wiki/File:Double_Pink_(332505440).jpg");
        add(credits, THIET_MOC_LAN, "Mokkie",
                "Corn Plant (Dracaena fragrans 'Massangeana').jpg", "CC BY-SA 3.0",
                "https://creativecommons.org/licenses/by-sa/3.0/",
                "https://commons.wikimedia.org/wiki/File:Corn_Plant_(Dracaena_fragrans_%27Massangeana%27).jpg");
        add(credits, TRUC_MAY_MAN, "ProjectManhattan", "Lucky bamboo plant.jpg",
                "CC BY-SA 3.0", "https://creativecommons.org/licenses/by-sa/3.0/",
                "https://commons.wikimedia.org/wiki/File:Lucky_bamboo_plant.jpg");
        add(credits, DUONG_XI_BOSTON, "Mokkie",
                "Boston Fern (Nephrolepis exaltata) 1.jpg", "CC BY-SA 3.0",
                "https://creativecommons.org/licenses/by-sa/3.0/",
                "https://commons.wikimedia.org/wiki/File:Boston_Fern_(Nephrolepis_exaltata)_1.jpg");
        CREDITS = Collections.unmodifiableMap(credits);
    }

    private PlantPhotos() {
    }

    private static void add(Map<String, Credit> credits, String key, String author, String title,
                            String license, String licenseUrl, String sourceUrl) {
        credits.put(key, new Credit(author, title, license, licenseUrl, sourceUrl));
    }

    public static int drawableResource(Context context, String source) {
        if (source == null || !source.startsWith(PREFIX)) {
            return 0;
        }
        String name = source.substring(PREFIX.length());
        if (!name.matches("[a-z0-9_]+")) {
            return 0;
        }
        return context.getResources().getIdentifier(name, "drawable", context.getPackageName());
    }

    public static Credit credit(String source) {
        return CREDITS.get(source);
    }

    public static final class Credit {
        public final String author;
        public final String title;
        public final String license;
        public final String licenseUrl;
        public final String sourceUrl;

        private Credit(String author, String title, String license, String licenseUrl,
                       String sourceUrl) {
            this.author = author;
            this.title = title;
            this.license = license;
            this.licenseUrl = licenseUrl;
            this.sourceUrl = sourceUrl;
        }
    }
}
