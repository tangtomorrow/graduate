package blinking.config;

import com.google.common.collect.Lists;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Config {
    private static final String PARENT_PTH = "data/";
    public static final String ORIGIN_STATION_ALL_PATH = PARENT_PTH + "origin/stations_all.csv";
    public static final String ORIGIN_STATION_MISS_PATH = PARENT_PTH + "origin/stations_miss.csv";
    public static final String ORIGIN_STATION_DETAIL_PATH = PARENT_PTH + "origin/stations_detail/";

    public static final String STEP1_STATION_MATCH_PATH = PARENT_PTH + "step1/stations_match.csv";
    public static final String STEP1_STATION_DETAIL_PATH = PARENT_PTH + "step1/stations_detail/";

    public static final String STEP2_STATION_DETAIL_PATH = PARENT_PTH + "step2/stations_detail/";

    public static final String STEP3_STATION_MATCH_PATH = PARENT_PTH + "step3/stations.csv";
    public static final String STEP3_STATION_DETAIL_PATH = PARENT_PTH + "step3/stations_detail/";

    public static final String STEP4_PARENT_PATH = PARENT_PTH + "step4/";
    public static final String STEP4_NEW_PARENT_PATH = PARENT_PTH + "step4new/";
    public static final String STEP5_PARENT_PATH = PARENT_PTH + "step5/";

    // 完整的数据行数
    public static final int ROW_COUNT = 31 * 24;
    public static final DateTimeFormatter FORMATTER1 = DateTimeFormatter.ofPattern("yyyy-M-d H:mm:ss");
    public static final DateTimeFormatter Formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 由于数据缺失，需要排除的地点
    public static final List<String> EX_STATION_NAMES = Lists.newArrayList(
            // 风速均为0
            "东滩公园",
            // 缺失太多，无法插值
            "真新", "黄兴公园", "世博公园", "朱桥", "水上运动场", "毛桥", "浦江光继村", "九段沙",
            // 部分只能靠7/9月份的数据进行插值，感觉跨天不太准确
            "安亭", "奉贤", "四团中学", "浏河",
            // 需要靠前后两天同一时刻均值做插值的站点
            "曹路", "济阳公园", "中山公园", "凌桥", "侯家镇", "石洞口", "五厍", "体育场", "西部渔村");

    // 近水站点名,先随便定义的
    // TODO
    public static final List<String> NEAR_WATER_STATIONS_NAMES = Lists.newArrayList(
            "朱家角", "五四农场", "罗店度假村"
    );

    public static void init() {
        File f1 = new File(STEP1_STATION_DETAIL_PATH);
        if (!f1.exists()) {
            f1.mkdirs();
        }

        File f2 = new File(STEP2_STATION_DETAIL_PATH);
        if (!f2.exists()) {
            f2.mkdirs();
        }

        File f3 = new File(STEP3_STATION_DETAIL_PATH);
        if (!f3.exists()) {
            f3.mkdirs();
        }

        File f4 = new File(STEP4_PARENT_PATH);
        if (!f4.exists()) {
            f4.mkdirs();
        }

        File f4new = new File(STEP4_NEW_PARENT_PATH);
        if (!f4new.exists()) {
            f4new.mkdirs();
        }

        File f5 = new File(STEP5_PARENT_PATH);
        if (!f5.exists()) {
            f5.mkdirs();
        }
    }

    public static void clear() {
        File file = new File(PARENT_PTH);
        Arrays.stream(Objects.requireNonNull(file.listFiles())).filter(f ->
                f.getName().startsWith("step")).forEach(Config::deleteDir);
    }

    private static void deleteDir(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteDir(files[i]);
            }
        }
        dir.delete();
    }
}
