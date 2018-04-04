package blinking.run;

import blinking.config.Config;
import blinking.model.Station;
import blinking.parser.StationParser;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.io.Files;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * step1. 整理
 *
 * @author tangym
 * @date 2018-04-01 15:30
 */
public class Step1 {
    private static final Splitter splitter = Splitter.on(",").trimResults();
    private static final int expectedColNamesSize = 25;
    private static final int expectedColValuesSize = 26;

    public static void main(String[] args) throws IOException {
        StationParser parser = new StationParser();
        List<Station> all = parser.parse(Config.ORIGIN_STATION_ALL_PATH, false);
        List<Station> miss = parser.parse(Config.ORIGIN_STATION_MISS_PATH, false);
        // remove miss
        all.removeAll(miss);

        boolean flag1 = validateStations(all);
        if (!flag1) {
            throw new RuntimeException("some station doesn't match any detail file");
        }

        boolean flag2 = validateStationDetails(all);
        if (!flag2) {
            throw new RuntimeException("some station detail file isn't correct");
        }

        // write matches places to file
        writeStationMatch(all);

        rewriteStationDetails(all);
    }

    /**
     * 验证各匹配到地点名是否均存在对应数据文件
     *
     * @param match
     * @return
     */
    private static boolean validateStations(List<Station> match) {
        File file = new File(Config.ORIGIN_STATION_DETAIL_PATH);
        List<String> fileNames = Arrays.asList(Objects.requireNonNull(file.list())).parallelStream()
                .map(str -> str.replace(".csv", "")).collect(Collectors.toList());

        long count = match.parallelStream()
                .filter(station -> !fileNames.contains(station.getName()))
                .count();
        return count == 0;
    }

    /**
     * 扫描文件，打印出不符合预期格式的文件
     */
    private static boolean validateStationDetails(List<Station> match) {
        long count = match.stream().map(station -> new File(Config.ORIGIN_STATION_DETAIL_PATH + station.getName() + ".csv"))
                .filter(file -> !validateStationDetail(file)).count();
        return count == 0;
    }

    /**
     * 验证当前数据文件是否符合预期要求
     * 即头行25个元素，数据行26个元素
     *
     * @param file
     * @return
     */
    private static boolean validateStationDetail(File file) {
        List<String> lines = null;
        try {
            lines = Files.readLines(file, Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        long colNamesSize = Objects.requireNonNull(lines.stream()).limit(1).filter(str -> {
            List<String> strs = splitter.splitToList(str);
            return strs.size() != expectedColNamesSize;
        }).count();

        long colValuesSize = lines.stream().skip(1).filter(str -> {
            List<String> strs = splitter.splitToList(str);
            return strs.size() != expectedColValuesSize;
        }).count();

        return colNamesSize == 0 && colValuesSize == 0;
    }

    /**
     * 将已匹配的station信息写入文件
     *
     * @param match
     * @throws IOException
     */
    private static void writeStationMatch(List<Station> match) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(Config.STEP1_STATION_MATCH_PATH));
            bw.write("name,lon,lat,cap,type,svf,district");
            bw.newLine();
            match.forEach(station -> {
                try {
                    bw.write(station.toString());
                    bw.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            bw.flush();
            bw.close();
        } catch (IOException e) {
            throw new RuntimeException("error in writeStationMatch", e);
        }
    }

    private static void rewriteStationDetails(List<Station> match) {
        match.stream().map(station -> station.getName() + ".csv")
                .forEach(Step1::rewrite);
    }

    /**
     * 将每个detail文件首行新增
     *
     * @param src src file path
     */
    private static void rewrite(String src) {
        try {
            File srcFile = new File(Config.ORIGIN_STATION_DETAIL_PATH + src);
            BufferedWriter bw = new BufferedWriter(new FileWriter(Config.STEP1_STATION_DETAIL_PATH + src));

            Files.readLines(srcFile, Charsets.UTF_8).stream().limit(1).forEach(header -> {
                try {
                    bw.write(header + ",miss_col");
                    bw.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            Files.readLines(srcFile, Charsets.UTF_8).stream().skip(1).forEach(str -> {
                try {
                    bw.write(str);
                    bw.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            bw.flush();
            bw.close();
        } catch (IOException e) {
            throw new RuntimeException("error in rewrite file " + src, e);
        }
    }

    /**
     * 获取匹配好的Station信息
     *
     * @return
     * @throws IOException
     */
    static List<Station> getMatchStations() throws IOException {
        StationParser parser = new StationParser();
        return parser.parse(Config.STEP1_STATION_MATCH_PATH, true);
    }
}
