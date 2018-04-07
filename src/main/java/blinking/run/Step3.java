package blinking.run;

import blinking.config.Config;
import blinking.model.Station;
import blinking.model.StationDetail;
import blinking.parser.StationParser;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import tech.tablesaw.api.FloatColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.util.Selection;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static tech.tablesaw.api.QueryHelper.column;

/**
 * Step3. 验证-插值
 *
 * @author tangym
 * @date 2018-04-04 18:15
 */
public class Step3 {

    public static void main(String[] args) throws IOException {
        List<StationDetail> stations = Step2.getStationDetailList();
        // 检查station是否均满足要求
        boolean flag = stations.stream()
                .filter(stationDetail -> !Config.EX_STATION_NAMES.contains(stationDetail.getStation().getName()))
                .map(StationDetail::getTable)
                .allMatch(Step3::checkDataCols);
        if (flag) {
            return;
        }

        // 将满足条件的站点数据拷贝到step3文件夹
        stations.stream()
                .filter(stationDetail -> !Config.EX_STATION_NAMES.contains(stationDetail.getStation().getName()))
                .map(StationDetail::getTable)
                .filter(Step3::checkDataCols)
                .map(Table::name)
                .forEach(s -> copy(Config.STEP2_STATION_DETAIL_PATH + s, Config.STEP3_STATION_DETAIL_PATH + s));

        // 找出不满足要求的station文件名，插值并同样拷贝到step3文件夹
        System.out.println("-----------------需要插值的站点如下----------------");
        List<String> missStationNames = stations.stream()
                .filter(stationDetail -> !Config.EX_STATION_NAMES.contains(stationDetail.getStation().getName()))
                .map(StationDetail::getTable)
                .filter(table -> !Step3.checkDataCols(table))
                .peek(Step3::viewMiss)
                .map(Table::name)
                .collect(Collectors.toList());

        System.out.println("-------------------开始进行插值--------------");
        missStationNames.forEach(s -> fillAndCopy(Config.STEP2_STATION_DETAIL_PATH + s, Config.STEP3_STATION_DETAIL_PATH + s));

        System.out.println("-------------------校验并生成最终匹配信息表--------------");
        boolean flag2 = stations.stream()
                .filter(stationDetail -> !Config.EX_STATION_NAMES.contains(stationDetail.getStation().getName()))
                .filter(stationDetail ->
                        !new File(Config.STEP3_STATION_DETAIL_PATH + stationDetail.getStation().getName() + ".csv").exists()
                )
                .map(StationDetail::getTable)
                .allMatch(Step3::checkDataCols);
        System.out.println("check " + flag2);

        List<StationDetail> stationDetails = stations.stream()
                .filter(stationDetail -> !Config.EX_STATION_NAMES.contains(stationDetail.getStation().getName()))
                .collect(Collectors.toList());
        writeMatch(stationDetails);
    }

    private static void writeMatch(List<StationDetail> stationDetails) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(Config.STEP3_STATION_MATCH_PATH));
            bw.write("name,lon,lat,cap,type,svf,district");
            bw.newLine();
            stationDetails.forEach(station -> {
                try {
                    bw.write(station.getStation().toString());
                    bw.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            bw.flush();
            bw.close();
        } catch (IOException e) {
            throw new RuntimeException("error in writeMatch", e);
        }
    }

    private static void copy(String srcPath, String dstPath) {
        try {
            Files.copy(new File(srcPath), new File(dstPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean checkDataCols(Table table) {
        try {
            // check temperature
            FloatColumn temperature = table.floatColumn("temperature");
            // check wind_speed_2m
            FloatColumn wind = table.floatColumn("wind_speed_2m");

            Selection selection1 = temperature.isMissing();
            Selection selection2 = wind.isMissing();
            return table.rowCount() == Config.ROW_COUNT && selection1.isEmpty() && selection2.isEmpty();
        } catch (Exception e) {
            System.out.println(table.name() + ", " + e);
            return false;
        }
    }

    private static void fillAndCopy(String srcPath, String dstPath) {
        try {
            Table table = Table.read().csv(srcPath);
            for (short day = 1; day <= 31; day++) {
                for (short hour = 1; hour <= 24; hour++) {
                    Table t = table.selectWhere(column("day").isEqualTo(day)).selectWhere(column("hour").isEqualTo(hour));
                    if (t.isEmpty()) {
                        boolean flag = fillWithHours(table, day, hour);
                        if (!flag) {
                            //fillWithDays(table, day, hour);
                        }
                    }
                }
            }

            if (table.rowCount() != Config.ROW_COUNT) {
                viewMiss(table);
            } else {
                table.write().csv(dstPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据前后两日的同一时刻
     *
     * @param table
     * @param day
     * @param hour
     */
    private static boolean fillWithDays(Table table, short day, short hour) {
        short index = (short) ((day - 1) * 24 + hour);
        Table before = table.selectWhere(column("day").isEqualTo(day - 1)).selectWhere(column("hour").isEqualTo(hour));
        Table after = table.selectWhere(column("day").isEqualTo(day + 1)).selectWhere(column("hour").isEqualTo(hour));
        if (before.isEmpty() || after.isEmpty()) {
            //System.out.println(table.name() + ":" + day + "," + hour);
            return false;
        }

        float temperature = (before.floatColumn("temperature").get(0) + after.floatColumn("temperature").get(0)) / 2;
        float wind = (before.floatColumn("wind_speed_2m").get(0) + after.floatColumn("wind_speed_2m").get(0)) / 2;
        LocalDateTime dt = before.dateTimeColumn("formatted_time").get(0).plusDays(1);
        table.shortColumn("index").append(index);
        table.categoryColumn("time").append(dt.format(Config.FORMATTER1));
        table.dateTimeColumn("formatted_time").append(dt);
        table.shortColumn("day").append(day);
        table.shortColumn("hour").append(hour);
        table.floatColumn("temperature").append(temperature);
        table.floatColumn("wind_speed_2m").append(wind);
        return true;
    }

    /**
     * 根据当日的前后两小时（包括跨日）
     *
     * @param table
     * @param day
     * @param hour
     */
    private static boolean fillWithHours(Table table, short day, short hour) {
        short index = (short) ((day - 1) * 24 + hour);
        Table before = table.selectWhere(column("index").isEqualTo(index - 1));
        Table after = table.selectWhere(column("index").isEqualTo(index + 1));

        if (before.isEmpty() || after.isEmpty()) {
            //System.out.println(table.name() + ":" + day + "," + hour);
            return false;
        }

        float temperature = (before.floatColumn("temperature").get(0) + after.floatColumn("temperature").get(0)) / 2;
        float wind = (before.floatColumn("wind_speed_2m").get(0) + after.floatColumn("wind_speed_2m").get(0)) / 2;
        LocalDateTime dt = before.dateTimeColumn("formatted_time").get(0).plusHours(1);
        table.shortColumn("index").append(index);
        table.categoryColumn("time").append(dt.format(Config.FORMATTER1));
        table.dateTimeColumn("formatted_time").append(dt);
        table.shortColumn("day").append(day);
        table.shortColumn("hour").append(hour);
        table.floatColumn("temperature").append(temperature);
        table.floatColumn("wind_speed_2m").append(wind);
        return true;
    }

    private static void viewMiss(Table table) {
        if (table.rowCount() != Config.ROW_COUNT) {
            StringBuilder sb = new StringBuilder();
            sb.append(table.name().replace(".csv", ""));
            Joiner joiner = Joiner.on(",");
            List<String> list = Lists.newArrayList();
            for (short day = 1; day <= 31; day++) {
                for (short hour = 1; hour <= 24; hour++) {
                    Table t = table.selectWhere(column("day").isEqualTo(day)).selectWhere(column("hour").isEqualTo(hour));
                    if (t.isEmpty()) {
                        list.add(day + "-" + hour);
                    }
                }
            }
            sb.append("[").append(joiner.join(list)).append("]");
            System.out.println(sb.toString());
        }
    }

    public static List<StationDetail> gets() {
        StationParser parser = new StationParser();
        try {
            List<Station> stations = parser.parse(Config.STEP3_STATION_MATCH_PATH, true);
            return stations.stream().map((Function<Station, StationDetail>) station -> {
                try {
                    return new StationDetail(station, Table.read().csv(Config.STEP3_STATION_DETAIL_PATH + station.getName() + ".csv").setName(station.getName()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
