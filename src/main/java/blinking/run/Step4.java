package blinking.run;

import blinking.config.Config;
import blinking.model.StationDetail;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import tech.tablesaw.api.CategoryColumn;
import tech.tablesaw.api.FloatColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.table.TemporaryView;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * step4. 正题-计算
 *
 * @author tangym
 * @date 2018-04-05 下午11:59
 */
public class Step4 {
    public static void main(String[] args) throws IOException {
        List<StationDetail> stationDetails = Step3.gets();
        a(stationDetails);
        b(stationDetails);
        c(stationDetails);
    }

    private static void a(List<StationDetail> stations) throws IOException {
        String splitColumnName = "day";
        String[] retainColumnNames = {"hour", "temperature"};

        List<Table> tables = stations.stream()
                .map(StationDetail::getTable)
                .map(table -> splitTable(table, splitColumnName, retainColumnNames))
                .reduce(Step4::mergeTablesByName).get();

        Table t = Table.create("day-temperature");
        tables.stream()
                .sorted(Comparator.comparingInt(o -> Integer.parseInt(o.name())))
                .forEach(table -> t.addColumn(table.floatColumn("temperature").setName(table.name())));

        t.write().csv(Config.STEP4_PARENT_PATH + "day-temperature-detail.csv");

        /*
        Table t2 = Table.create("day-tmp");
        ShortColumn day = new ShortColumn("day");
        FloatColumn floats = new FloatColumn("temperature");
        t2.addColumn(day, floats);
        tables.stream()
                .sorted(Comparator.comparingInt(o -> Integer.parseInt(o.name())))
                .forEach(table -> {
                    FloatColumn tmps = table.floatColumn("temperature");
                    tmps.forEach((Consumer<Float>) aFloat -> {
                        day.append(Short.parseShort(table.name()));
                        floats.append(aFloat);
                    });
                });

         t2.write().csv("data/tmp/day-temp.csv");
        */
    }

    /**
     * 97站点*24小时
     * 均值
     *
     * @param stations
     */
    private static void b(List<StationDetail> stations) throws IOException {
        String splitColumnName = "hour";
        String retainColumnName = "temperature";

        Table t = stations.stream()
                .map(StationDetail::getTable)
                .map(table -> {
                    Table tt = Table.create(table.name());
                    table.groupBy(splitColumnName)
                            .getSubTables()
                            .stream()
                            .map(TemporaryView::asTable)
                            .map(table1 -> {
                                FloatColumn floats = new FloatColumn(table1.name());
                                floats.append(table1.floatColumn(retainColumnName).mean());
                                return floats;
                            }).forEach(tt::addColumn);
                    CategoryColumn stationName = new CategoryColumn("name");
                    stationName.append(table.name());
                    tt.addColumn(0, stationName);
                    return tt;
                })
                .reduce((t1, t2) -> {
                    t1.append(t2);
                    return t1;
                })
                .get();

        t.write().csv(Config.STEP4_PARENT_PATH + "station,hour-temperature-mean.csv");
    }

    private static void c(List<StationDetail> stations) throws IOException {
        String splitColumnName = "day";
        String[] retainColumnNames = {"hour", "temperature"};

        List<Integer> types = Lists.newArrayList(1, 2, 3, 4, 5);
        types.forEach(
                type -> {
                    List<Table> tables = stations.stream()
                            .filter(stationDetail -> stationDetail.getStation().getType() == type)
                            .map(StationDetail::getTable)
                            .map(table -> splitTable(table, splitColumnName, retainColumnNames))
                            .reduce(Step4::mergeTablesByName).get();

                    Table t = Table.create("day-temperature");
                    tables.stream()
                            .sorted(Comparator.comparingInt(o -> Integer.parseInt(o.name())))
                            .forEach(table -> t.addColumn(table.floatColumn("temperature").setName(table.name())));

                    try {
                        t.write().csv(Config.STEP4_PARENT_PATH + "type" + type + "-day-temperature-detail.csv");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        );

    }

    /**
     * 拆分表格
     * 按某列名进行拆分，最多返回该列不同值的数目的表格列表
     * 同时按照需要保留的列名列表进行过滤
     *
     * @param table             原始表格
     * @param splitColumnName   拆分表格依赖的列名
     * @param retainColumnNames 拆分后每个表格需要保留的列名列表
     * @return 拆分后获得的表格列表，每个表格名是拆分依赖列的各个取值
     */
    private static List<Table> splitTable(Table table, String splitColumnName, String[] retainColumnNames) {
        return table.groupBy(splitColumnName)
                .getSubTables()
                .stream()
                .map(TemporaryView::asTable)
                .peek(t -> t.retainColumns(retainColumnNames))
                .collect(toList());
    }

    /**
     * 将两个表格列表（各列表中的表格名称不同）按照表格名称进行合并
     * 相同表格名的表格合并成一个表格
     * 返回合并后的表格列表
     *
     * @param tables1 表格列表1
     * @param tables2 表格列表2
     * @return 合并后的表格列表
     */
    private static List<Table> mergeTablesByName(List<Table> tables1, List<Table> tables2) {
        List<Table> tables = Lists.newArrayList();
        Map<String, List<Table>> map = Maps.newHashMap();
        tables1.forEach(t -> map.computeIfAbsent(t.name(), k -> Lists.newArrayList()).add(t));
        tables2.forEach(t -> map.computeIfAbsent(t.name(), k -> Lists.newArrayList()).add(t));
        map.forEach((name, ts) ->
                ts.stream().reduce((t1, t2) -> {
                    t1.append(t2);
                    return t1;
                }).ifPresent(tables::add));
        return tables;
    }
}
