package blinking.step2;

import blinking.config.Config;
import blinking.model.Station;
import blinking.parser.StationParser;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.io.Files;
import tech.tablesaw.api.Table;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ReadData {
    private static final Splitter splitter = Splitter.on(",").trimResults();

    public static void main(String[] args) throws IOException {
        StationParser parser = new StationParser();
        List<Station> match = parser.parse(Config.STATION_MATCH_PATH, true);
        // System.out.println(match.size());
        String filePath = Config.STATION_DETAIL_PATH + match.get(0).getName() + ".csv";
        File file = new File(filePath);
        //Table table = Table.read().csv(file);
        //System.out.println(table);

        /*
        List<String> lines = Files.readLines(file, Charsets.UTF_8);
        lines.stream().limit(10).map(str -> {
            List<String> strs = splitter.splitToList(str);
            return strs.size();
        }).forEach(System.out::println);
        */

        test();
    }

    public static void test() throws IOException {
        String path = "data/step2/test.csv";
        File file = new File(path);
        Table table = Table.read().csv(file);
        System.out.println(table.columnNames());
        System.out.println(table);
    }
}
