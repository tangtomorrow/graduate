package blinking.run;

import blinking.config.Config;
import blinking.model.StationDetail;
import com.google.common.collect.Lists;
import tech.tablesaw.aggregate.AggregateFunctions;
import tech.tablesaw.api.*;
import tech.tablesaw.table.TemporaryView;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.function.IntConsumer;

import static tech.tablesaw.api.QueryHelper.column;

/**
 * step4. new 正题-计算
 * 先拼凑成大数据集，再统一围绕大数据集计算
 *
 * @author tangym
 * @date 2018-04-06 下午9:55
 */
public class Step4New {
    private static final String DATA_SET_CSV = Config.STEP4_NEW_PARENT_PATH + "dataset.csv";

    public static void main(String[] args) throws IOException {
        Table data = conbine();
        a(data);
        b(data);
        c(data);
    }

    /**
     * @param data
     */
    private static void a(Table data) throws IOException {
        Table table = Table.create("day-temperature-detail");
        data.groupBy("day")
                .getSubTables()
                .stream()
                .map(TemporaryView::asTable)
                //.peek(t -> t.retainColumns("name", "hour", "temperature"))
                .peek(t -> t.retainColumns("temperature"))
                .sorted(Comparator.comparingInt(o -> Integer.parseInt(o.name())))
                .reduce(table, (t1, t2) -> {
                    t1.addColumn(t2.floatColumn("temperature").setName(t2.name()));
                    return t1;
                });
        table.write().csv(Config.STEP4_NEW_PARENT_PATH + "day-temperature-detail.csv");
        System.out.println(table);
    }

    private static void b(Table data) throws IOException {
        Table ts = data.groupBy("name")
                .getSubTables().stream()
                .map(TemporaryView::asTable)
                .peek(t -> t.retainColumns("hour", "temperature"))
                .map(table -> {
                    Table t = Table.create(table.name());
                    CategoryColumn name = new CategoryColumn("name");
                    name.append(table.name());
                    t.addColumn(name);
                    Table summary = table.summarize("temperature", AggregateFunctions.mean).by("hour");
                    summary.forEach((IntConsumer) value -> {
                        short hour = summary.shortColumn(0).get(value);
                        double meanTmp = summary.doubleColumn(1).get(value);

                        DoubleColumn tmp = new DoubleColumn(String.valueOf(hour));
                        tmp.append(meanTmp);

                        t.addColumn(tmp);
                    });
                    return t;
                })
                .reduce((t1, t2) -> {
                    t1.append(t2);
                    return t1;
                })
                .get();
        ts.write().csv(Config.STEP4_NEW_PARENT_PATH + "station|hour-temperature-mean.csv");
        System.out.println(ts);
    }

    private static void c(Table data) {
        List<Integer> types = Lists.newArrayList(1, 2, 3, 4, 5);
        types.forEach(
                type -> {
                    Table table = Table.create("day-temperature-detail");
                    data.selectWhere(column("type").isEqualTo(type))
                            .groupBy("day")
                            .getSubTables()
                            .stream()
                            .map(TemporaryView::asTable)
                            //.peek(t -> t.retainColumns("name", "hour", "temperature"))
                            .peek(t -> t.retainColumns("temperature"))
                            .sorted(Comparator.comparingInt(o -> Integer.parseInt(o.name())))
                            .reduce(table, (t1, t2) -> {
                                t1.addColumn(t2.floatColumn("temperature").setName(t2.name()));
                                return t1;
                            });
                    try {
                        table.write().csv(Config.STEP4_NEW_PARENT_PATH + "type" + type + "-day-temperature-detail.csv");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    private static Table conbine() throws IOException {
        List<StationDetail> stationDetails = Step3.gets();
        Table table = stationDetails.stream()
                .peek(stationDetail -> {
                    Table t = stationDetail.getTable();
                    int rowCount = t.rowCount();
                    CategoryColumn name = new CategoryColumn("name");
                    DoubleColumn lon = new DoubleColumn("lon");
                    DoubleColumn lat = new DoubleColumn("lat");
                    DoubleColumn cap = new DoubleColumn("cap");
                    IntColumn type = new IntColumn("type");
                    FloatColumn svf = new FloatColumn("svf");
                    for (int i = 0; i < rowCount; ++i) {
                        name.append(stationDetail.getStation().getName());
                        lon.append(stationDetail.getStation().getLon());
                        lat.append(stationDetail.getStation().getLat());
                        cap.append(stationDetail.getStation().getCap());
                        type.append(stationDetail.getStation().getType());
                        svf.append(stationDetail.getStation().getSvf().doubleValue() / 100);
                    }
                    t.addColumn(0, name);
                    t.addColumn(1, lon);
                    t.addColumn(2, lat);
                    t.addColumn(3, cap);
                    t.addColumn(4, type);
                    t.addColumn(5, svf);
                })
                .map(StationDetail::getTable)
                .reduce((t1, t2) -> {
                    t1.append(t2);
                    return t1;
                })
                .get();

        table.write().csv(DATA_SET_CSV);

        return table;
    }
}