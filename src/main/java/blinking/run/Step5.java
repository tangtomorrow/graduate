package blinking.run;

import blinking.config.Config;
import blinking.model.StationDetail;
import tech.tablesaw.api.CategoryColumn;
import tech.tablesaw.api.Table;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Step5. 数据重新整理
 *
 * @author tangym
 * @date 2018-04-06 下午6:30
 */
public class Step5 {
    public static void main(String[] args) throws IOException {
        /*
        Table t = Table.read().csv("data/tmp/day-temp.csv");
        Box.show("day-temperature", t, "temperature", "day");
        */

        List<StationDetail> stationDetails = Step3.gets();
        //writeMatch(stationDetails);
        //writeDetails(stationDetails);
        combine(stationDetails);
    }

    private static void writeDetails(List<StationDetail> stationDetails) {
        stationDetails.stream()
                .peek(stationDetail -> {
                    Table t = stationDetail.getTable();
                    CategoryColumn timeStr = new CategoryColumn("datetime");
                    t.dateTimeColumn("formatted_time").forEach(localDateTime -> {
                        String str = localDateTime.format(Config.Formatter2);
                        timeStr.append(str);
                    });
                    t.addColumn(0, timeStr);
                    t.retainColumns("datetime", "temperature", "wind_speed_2m");
                })
                .forEach(stationDetail -> {
                            String dstPath = Config.STEP5_STATION_DETAIL_PATH + stationDetail.getTable().name() + ".csv";
                            try {
                                stationDetail.getTable().write().csv(dstPath);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                );
    }

    private static void writeMatch(List<StationDetail> stationDetails) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(Config.STEP5_PARENT_PATH + "stations.csv"));
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

    private static void combine(List<StationDetail> stationDetails) {
        // wind_speed_2m temperature
        Arrays.stream(new String[]{"temperature", "wind_speed_2m"})
                .forEach(
                        col -> {
                            Table table = stationDetails.stream()
                                    .map(stationDetail -> stationDetail.getStation().getName())
                                    .map(name -> {
                                        try {
                                            return Table.read().csv(Config.STEP5_STATION_DETAIL_PATH + name + ".csv").setName(name);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        return null;
                                    })
                                    .peek(t -> t.retainColumns("datetime", col))
                                    .reduce((t1, t2) -> {
                                        check(t1, t2, "datetime");
                                        t1.addColumn(t2.floatColumn(col).setName(t2.name()));
                                        return t1;
                                    })
                                    .get();
                            table.floatColumn(col).setName("徐家汇公园");
                            table.dateTimeColumn("datetime").setName("formatted_time");

                            CategoryColumn timeStr = new CategoryColumn("datetime");
                            table.dateTimeColumn("formatted_time").forEach(localDateTime -> {
                                String str = localDateTime.format(Config.Formatter2);
                                timeStr.append(str);
                            });
                            table.addColumn(0, timeStr);
                            table.removeColumns("formatted_time");

                            try {
                                table.write().csv(Config.STEP5_PARENT_PATH + col + ".csv");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            System.out.println(table);
                        }
                );
    }

    /**
     * 检查t1/t2的表结构是否相同
     *
     * @param t1
     * @param t2
     */
    private static void check(Table t1, Table t2, String... colNames) {
        if (t1.rowCount() != t2.rowCount()) {
            throw new RuntimeException("error rowCount");
        }

        int rowCount = t1.rowCount();
        for (int i = 0; i < rowCount; i++) {
            for (String colName : colNames) {
                String str1 = t1.column(colName).getString(i);
                String str2 = t2.column(colName).getString(i);
                if (!str1.equals(str2)) {
                    throw new RuntimeException("error col value");
                }
            }
        }
    }
}
