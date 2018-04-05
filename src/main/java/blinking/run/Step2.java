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
import java.util.List;
import java.util.stream.Collectors;

/**
 * step2. 解析-整合
 *
 * @author tangym
 * @date 2018-04-02 23:30
 */
public class Step2 {
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
        table.categoryColumn("time").forEach(s -> formatDateTime.append(LocalDateTime.parse(s, Config.FORMATTER1)));

        DateTimeColumn formatTimeColumn = new DateTimeColumn("formatted_time");
        IntColumn dayColumn = new IntColumn("day");
        IntColumn hourColumn = new IntColumn("hour");
        IntColumn indexColumn = new IntColumn("index");
        formatDateTime.forEach(
                localDateTime -> {
                    int day = localDateTime.minusHours(1).getDayOfMonth();
                    int hour = localDateTime.minusHours(1).getHour() + 1;
                    int index = (day - 1) * 24 + hour;
                    dayColumn.append(day);
                    hourColumn.append(hour);
                    indexColumn.append(index);
                    formatTimeColumn.append(localDateTime);
                });

        // 将新增的列加入原始表格
        table.addColumn(0, indexColumn);
        table.addColumn(2, formatTimeColumn);
        table.addColumn(3, dayColumn);
        table.addColumn(4, hourColumn);

        table.retainColumns("index", "time", "formatted_time", "day", "hour", "temperature", "wind_speed_2m");

        // 删除首行，并排序
        table = table.dropRow(0).sortAscendingOn("index");
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
