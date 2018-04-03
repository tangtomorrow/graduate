package blinking;

import blinking.parser.StationParser;
import blinking.model.Station;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class App {
    public static final String PARENT_PTH = "/Users/youkey/Work/Java/data/";
    private static final String filePath = "LOC1-5-SVF.csv";

    public static void main(String[] args) throws IOException {
        StationParser parser = new StationParser();
        List<Station> stations = parser.parse(PARENT_PTH + filePath, false);
        // stations.forEach(System.out::println);

        /*
        File file = new File(parentPath + "8");
        Arrays.asList(file.listFiles()).stream().filter(f -> !f.getName().equalsIgnoreCase(".DS_Store")).forEach(s -> {
            String dst = s.getAbsolutePath().replace("8", "8_rename").replace("_.csv", ".csv");
            try {
                Files.copy(s, new File(dst));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        */

        /*
        stations.stream()
                .filter(station -> {
                            for (String f : files) {
                                if (f.matches(".*" + station.getName() + ".*")) {
                                    return true;
                                }
                            }
                            return false;
                        }
                ).collect(Collectors.toList());

        files.stream().
        */

        File file = new File(PARENT_PTH + "8");
        long a = Arrays.asList(file.listFiles()).stream().map(File::getName).filter(f -> !f.equalsIgnoreCase(".DS_Store"))
                .map(s -> s.replace(".csv", ""))
                .map(s -> {
                    if (!s.contains("_")) {
                        return s;
                    } else {
                        return s.split("_")[1];
                    }
                })
                .filter(s -> {
                    boolean flag = false;
                    for (Station station : stations) {
                        if (station.getName().equalsIgnoreCase(s)) {
                            flag = true;
                            break;
                        }
                    }

                    return flag;
                })
                .count();
        System.out.println(a);
        //.forEach(System.out::println;

        Arrays.asList(file.listFiles()).stream().filter(f -> !f.getName().equalsIgnoreCase(".DS_Store"))
                .filter(h -> {
                    String s;
                    if (!h.getName().contains("_")) {
                        s = h.getName().replace(".csv", "");
                    } else {
                        s = h.getName().split("_")[1].replace(".csv", "");
                    }

                    boolean flag = false;
                    for (Station station : stations) {
                        if (station.getName().equalsIgnoreCase(s)) {
                            flag = true;
                            break;
                        }
                    }

                    return flag;
                })
                .forEach(s -> {
                    String dst = s.getAbsolutePath().replace("8", "8_rename").replace("_.csv", ".csv");
                    try {
                        Files.move(s, new File(dst));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }
}
