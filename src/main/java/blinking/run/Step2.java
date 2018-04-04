package blinking.run;

import blinking.config.Config;
import blinking.model.Station;
import blinking.model.StationDetail;
import tech.tablesaw.api.CategoryColumn;
import tech.tablesaw.api.DateTimeColumn;
import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.Table;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * step2. 解析-整合
 *
 * @author tangym
 * @date 2018-04-02 23:30
 */
public class Step2 {
    private static final DateTimeFormatter srcFormatter = DateTimeFormatter.ofPattern("yyyy-M-d H:mm:ss");
    private static final DateTimeFormatter dstFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) throws IOException {
        List<Station> match = Step1.getMatchStations();
        match.stream()
                .map(station -> station.getName() + ".csv")
                // .filter(s -> new File(Config.STEP1_STATION_DETAIL_PATH + s).exists())
                .forEach(fileName -> {
                    try {
                        convertCsv(Config.STEP1_STATION_DETAIL_PATH + fileName, Config.STEP2_STATION_DETAIL_PATH + fileName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    /**
     * 解析csv文件，整理时间列并增加新列
     *
     * @param srcPath file path
     * @return data table
     */
    private static Table convertCsv(String srcPath, String dstFilePath) throws IOException {
        Table table = Table.read().csv(srcPath);

        // 根据原始的time列计算新列
        DateTimeColumn formatDateTime = new DateTimeColumn("local_date_time");
        table.categoryColumn("time").forEach(s -> formatDateTime.append(LocalDateTime.parse(s, srcFormatter)));

        CategoryColumn formatTimeStr = new CategoryColumn("formatted_time");
        formatDateTime.forEach(localDateTime -> formatTimeStr.append(localDateTime.format(dstFormatter)));

        IntColumn day = new IntColumn("day");
        formatDateTime.forEach(localDateTime -> day.append(localDateTime.minusHours(1).getDayOfMonth()));

        IntColumn hour = new IntColumn("hour");
        formatDateTime.forEach(localDateTime -> hour.append(localDateTime.minusHours(1).getHour() + 1));

        // 将新增的列加入原始表格
        table.addColumn(1, formatTimeStr);
        table.addColumn(2, day);
        table.addColumn(3, hour);

        table.retainColumns("time", "formatted_time", "day", "hour", "temperature", "wind_speed_2m");

        // 删除不关注的列
        /*
        List<String> targetColNames = Lists.newArrayList("time", "formatted_time", "day", "hour", "temperature", "wind_speed_2m");
        table.columnNames().stream()
                .filter(it -> !targetColNames.contains(it))
                .forEach(table::removeColumns);
                */

        // 删除首行，并排序
        table = table.dropRow(0).sortAscendingOn("formatted_time");
        // 写入目标文件路径
        table.write().csv(dstFilePath);

        return table;
    }

    public static List<StationDetail> getStationDetailList() throws IOException {
        return Step1.getMatchStations().stream().map(station -> {
            try {
                return new StationDetail(station, Table.read().csv(Config.STEP2_STATION_DETAIL_PATH + station.getName() + ".csv"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList());
    }
}
