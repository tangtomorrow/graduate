package blinking.run;

import blinking.config.Config;
import blinking.model.StationDetail;
import tech.tablesaw.api.FloatColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.util.Selection;

import java.io.IOException;
import java.util.List;

/**
 * Step3. 验证
 *
 * @author tangym
 * @date 2018-04-04 18:15
 */
public class Step3 {

    public static void main(String[] args) throws IOException {
        List<StationDetail> stations = Step2.getStationDetailList();
        boolean flag = stations.stream()
                .filter(stationDetail -> !Config.EX_STATION_NAMES.contains(stationDetail.getStation().getName()))
                .map(StationDetail::getTable)
                .allMatch(Step3::checkDataCols);
        System.out.println(flag);

        stations.stream()
                .filter(stationDetail -> !Config.EX_STATION_NAMES.contains(stationDetail.getStation().getName()))
                .map(StationDetail::getTable)
                .filter(table -> !Step3.checkDataCols(table))
                .forEach(System.out::println);
    }

    private static boolean checkDataCols(Table table) {
        try {
            // check temperature
            FloatColumn temperature = table.floatColumn("temperature");
            // check wind_speed_2m
            FloatColumn wind = table.floatColumn("wind_speed_2m");

            Selection selection1 = temperature.isMissing();
            Selection selection2 = wind.isMissing();
            return table.rowCount() == Config.ROW_COUNT && selection1.size() <= 0 && selection2.size() <= 0;
        } catch (Exception e) {
            System.out.println(table.name() + ", " + e);
            return false;
        }
    }
}
