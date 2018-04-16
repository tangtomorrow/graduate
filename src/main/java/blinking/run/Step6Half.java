package blinking.run;

import blinking.model.Station2;
import blinking.model.StationDetail2;
import blinking.parser.StationParser2;
import com.google.common.base.Function;
import tech.tablesaw.api.*;
import tech.tablesaw.table.TemporaryView;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;

import static tech.tablesaw.api.QueryHelper.column;

/**
 * 计算均温时，使用1-15号，不用16-31号
 *
 * @author tangym
 * @date 2018-04-16 1:20
 */
public class Step6Half {
    private static final String all = "data/x/class1-SVF.csv";

    public static void main(String[] args) throws Exception {
        Table t = combine();
        System.out.println(t);

        Table type53 = type53(t);
        getUHII(type53);

        // 将76个uhii文件拼成一个文件
        Table finalTable = gets().stream()
                .filter(stationDetail2 -> stationDetail2.getStation().getType12() != 53
                        && stationDetail2.getStation().getType2() != 2)
                .map(station -> {
                    Table t1 = Table.create(station.getStation().getName());

                    CategoryColumn stationName = new CategoryColumn("station_name");
                    stationName.append(station.getStation().getName());

                    CategoryColumn singleSVF = new CategoryColumn("single_svf");
                    singleSVF.append(station.getStation().getSingleSVF());

                    CategoryColumn meanSVF = new CategoryColumn("mean_svf");
                    meanSVF.append(station.getStation().getMeanSVF());

                    t1.addColumn(stationName, singleSVF, meanSVF);

                    // 增加27个列
                    try {
                        Table uhiiTable = Table.read().csv("data/x/uhiihalf/" + station.getStation().getName() + ".csv");
                        uhiiTable.forEach((IntConsumer) value -> {
                            String hour = uhiiTable.categoryColumn("hour").get(value);
                            float uhii = uhiiTable.floatColumn("uhii").get(value);
                            FloatColumn hourUhii = new FloatColumn(hour);
                            hourUhii.append(uhii);
                            t1.addColumn(hourUhii);
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return t1;
                })
                .reduce((t1, t2) -> {
                    t1.append(t2);
                    return t1;
                }).get();

        finalTable.write().csv("data/x/total_stations_uhii_half.csv");
    }

    private static void getUHII(Table type53) {
        gets().stream()
                .filter(stationDetail2 -> stationDetail2.getStation().getType12() != 53
                        && stationDetail2.getStation().getType2() != 2)
                .map(stationDetail2 -> {
                    Table t12 = stationDetail2.getTable()
                            .selectWhere(column("day").isLessThanOrEqualTo(15))
                            .groupBy("hour").getSubTables()
                            .stream()
                            .map(TemporaryView::asTable)
                            .sorted(Comparator.comparingInt(o -> Integer.parseInt(o.name())))
                            .peek(t1 -> t1.retainColumns("temperature"))
                            .map(table -> {
                                Table tt = Table.create(stationDetail2.getStation().getName());
                                CategoryColumn hour = new CategoryColumn("hour");
                                DoubleColumn mean = new DoubleColumn("mean_temperature");
                                hour.append(table.name());
                                mean.append(table.floatColumn("temperature").mean());
                                tt.addColumn(hour, mean);
                                return tt;
                            }).reduce((t1, t2) -> {
                                t1.append(t2);
                                return t1;
                            }).get();

                    double mean = t12.doubleColumn("mean_temperature").mean();
                    double day = t12.selectWhere(column("hour").isIn("10", "11", "12", "13", "14", "15")).doubleColumn("mean_temperature").mean();
                    double night = t12.selectWhere(column("hour").isIn("23", "24", "1", "2", "3", "4", "5")).doubleColumn("mean_temperature").mean();
                    t12.append(createTable("mean", mean));
                    t12.append(createTable("day", day));
                    t12.append(createTable("night", night));
                    return t12;
                })
                .map(sx -> {
                    NumericColumn uhii = sx.doubleColumn("mean_temperature").subtract(type53.doubleColumn("mean_temperature"));
                    Table x = Table.create(sx.name());
                    x.addColumn(sx.categoryColumn("hour"), uhii.setName("uhii"));
                    return x;
                }).forEach(table -> {
            try {
                table.write().csv("data/x/uhiihalf/" + table.name() + ".csv");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * uhii
     *
     * @param data
     */
    private static Table type53(Table data) throws IOException {
        Table t = data.selectWhere(column("type12").isEqualTo(53))
                .selectWhere(column("day").isLessThanOrEqualTo(15))
                .groupBy("hour")
                .getSubTables()
                .stream()
                .map(TemporaryView::asTable)
                .sorted(Comparator.comparingInt(o -> Integer.parseInt(o.name())))
                .peek(t1 -> t1.retainColumns("temperature"))
                .map(table -> {
                    Table tt = Table.create(table.name());
                    CategoryColumn hour = new CategoryColumn("hour");
                    DoubleColumn mean = new DoubleColumn("mean_temperature");
                    hour.append(table.name());
                    mean.append(table.floatColumn("temperature").mean());
                    tt.addColumn(hour, mean);
                    return tt;
                }).reduce((t1, t2) -> {
                    t1.append(t2);
                    return t1;
                }).get();
        t.setName("53_temperature_mean");
        double mean = t.doubleColumn("mean_temperature").mean();
        double day = t.selectWhere(column("hour").isIn("10", "11", "12", "13", "14", "15")).doubleColumn("mean_temperature").mean();
        double night = t.selectWhere(column("hour").isIn("23", "24", "1", "2", "3", "4", "5")).doubleColumn("mean_temperature").mean();
        t.append(createTable("mean", mean));
        t.append(createTable("day", day));
        t.append(createTable("night", night));

        return t;
    }

    private static Table createTable(String colName, double value) {
        Table t = Table.create(colName);
        CategoryColumn hour = new CategoryColumn("hour");
        DoubleColumn mean = new DoubleColumn("mean_temperature");
        hour.append(colName);
        mean.append(value);
        t.addColumn(hour, mean);
        return t;
    }

    private static List<StationDetail2> gets() {
        StationParser2 parser = new StationParser2();
        try {
            List<Station2> stations = parser.parse(all, true);
            return stations.stream().map((Function<Station2, StationDetail2>) station -> {
                try {
                    return new StationDetail2(station, Table.read().csv("data/x/stations_detail/" + station.getName() + ".csv").setName(station.getName()));
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

    private static Table combine() throws IOException {
        List<StationDetail2> stationDetails = gets();
        Table table = stationDetails.stream()
                .peek(stationDetail -> {
                    Table t = stationDetail.getTable();
                    int rowCount = t.rowCount();
                    CategoryColumn name = new CategoryColumn("name");
                    ShortColumn lon = new ShortColumn("type1");
                    ShortColumn lat = new ShortColumn("type2");
                    ShortColumn cap = new ShortColumn("type12");
                    CategoryColumn singleSVF = new CategoryColumn("singleSVF");
                    CategoryColumn meanSVF = new CategoryColumn("meanSVF");
                    for (int i = 0; i < rowCount; ++i) {
                        name.append(stationDetail.getStation().getName());
                        lon.append(stationDetail.getStation().getType1());
                        lat.append(stationDetail.getStation().getType2());
                        cap.append(stationDetail.getStation().getType12());
                        singleSVF.append(stationDetail.getStation().getSingleSVF());
                        meanSVF.append(stationDetail.getStation().getSingleSVF());
                    }
                    t.addColumn(0, name);
                    t.addColumn(1, lon);
                    t.addColumn(2, lat);
                    t.addColumn(3, cap);
                    t.addColumn(4, singleSVF);
                    t.addColumn(5, meanSVF);
                })
                .map(StationDetail2::getTable)
                .reduce((t1, t2) -> {
                    t1.append(t2);
                    return t1;
                })
                .get();

        table.write().csv("data/x/dataset.csv");

        return table;
    }
}
