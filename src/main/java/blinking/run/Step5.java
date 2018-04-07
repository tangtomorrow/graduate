package blinking.run;

import tech.tablesaw.api.Table;
import tech.tablesaw.api.plot.Box;

import java.io.IOException;

/**
 * Step5. 正题-绘图
 *
 * @author tangym
 * @date 2018-04-06 下午6:30
 */
public class Step5 {
    public static void main(String[] args) throws IOException {
        Table t = Table.read().csv("data/tmp/day-temp.csv");
        Box.show("day-temperature", t, "temperature", "day");
    }
}
