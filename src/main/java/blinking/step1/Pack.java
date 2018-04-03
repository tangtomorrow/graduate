package blinking.step1;

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
public class Pack {
    private static final Splitter splitter = Splitter.on(",").trimResults();
    private static final int expectedColNamesSize = 25;
    private static final int expectedColValuesSize = 26;

    public static void main(String[] args) throws IOException {
        StationParser parser = new StationParser();
        List<Station> all = parser.parse(Config.STATION_ALL_PATH, false);
        List<Station> miss = parser.parse(Config.STATION_MISS_PATH, false);
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

        // 扫描文件，打印出不符合预期格式的文件
        all.stream().map(station -> new File(Config.STATION_DETAIL_PATH + station.getName() + ".csv"))
                .filter(file -> !validateStationDetail(file)).forEach(System.out::println);

        // write matches places to file
        writeMatch(all);
    }

    public static void writeMatch(List<Station> match) throws IOException {
        FileWriter fw = new FileWriter(Config.STATION_MATCH_PATH);
        BufferedWriter bw = new BufferedWriter(fw);
        fw.write("name,lon,lat,cap,type,svf,district");
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
    }

    /**
     * 验证各匹配到地点名是否均存在对应数据文件
     *
     * @param match
     * @return
     */
    private static boolean validateStations(List<Station> match) {
        File file = new File(Config.STATION_DETAIL_PATH);
        List<String> fileNames = Arrays.asList(Objects.requireNonNull(file.list())).parallelStream()
                .map(str -> str.replace(".csv", "")).collect(Collectors.toList());

        // fileNames.parallelStream().sorted(Comparator.reverseOrder()).forEach(System.out::println);
        // fileNames.parallelStream().filter(s -> s.equals(".DS_Store")).forEach(System.out::println);

        long count = match.parallelStream()
                .filter(station -> !fileNames.contains(station.getName()))
                .count();
        return count == 0;
    }

    private static boolean validateStationDetails(List<Station> match) {
        long count = match.stream().map(station -> new File(Config.STATION_DETAIL_PATH + station.getName() + ".csv"))
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
        long colNamesSize = lines.stream().limit(1).filter(str -> {
            List<String> strs = splitter.splitToList(str);
            return strs.size() != expectedColNamesSize;
        }).count();

        long colValuesSize = lines.stream().skip(1).filter(str -> {
            List<String> strs = splitter.splitToList(str);
            return strs.size() != expectedColValuesSize;
        }).count();

        return colNamesSize == 0 && colValuesSize == 0;
    }
}
