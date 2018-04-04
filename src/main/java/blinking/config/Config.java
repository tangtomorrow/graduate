package blinking.config;

import com.google.common.collect.Lists;

import java.io.File;
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

    // 完整的数据行数
    public static final int ROW_COUNT = 31 * 24;
    // 由于数据缺失，需要排除的地点
    public static final List<String> EX_STATION_NAMES = Lists.newArrayList("凌桥", "石洞口");

    public static void init() {
        File f1 = new File(STEP1_STATION_DETAIL_PATH);
        if (!f1.exists()) {
            f1.mkdirs();
        }

        File f2 = new File(STEP2_STATION_DETAIL_PATH);
        if (!f2.exists()) {
            f2.mkdirs();
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
