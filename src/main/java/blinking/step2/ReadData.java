package blinking.step2;

import com.google.common.base.Splitter;

import java.io.IOException;

public class ReadData {
    private static final Splitter splitter = Splitter.on(",").trimResults();
    private static final int expectedColNamesSize = 25;
    private static final int expectedColValuesSize = 26;

    public static void main(String[] args) throws IOException {
    }

    /*
    public static void test() throws IOException {
        String path = "data/step2/test.csv";
        File file = new File(path);
        Table table = Table.read().csv(file);
        System.out.println(table.columnNames());
        System.out.println(table);
    }
    */
}
