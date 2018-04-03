package blinking.step1;

import blinking.config.Config;
import blinking.model.Station;
import blinking.parser.StationParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Pack {
    public static void main(String[] args) throws IOException {
        StationParser parser = new StationParser();
        List<Station> all = parser.parse(Config.STATION_ALL_PATH, false);
        List<Station> miss = parser.parse(Config.STATION_MISS_PATH, false);
        // System.out.println(all.size());
        // System.out.println(miss.size());

        // remove miss
        all.removeAll(miss);

        File file = new File(Config.STATION_DETAIL_PATH);
        List<String> fileNames = Arrays.asList(Objects.requireNonNull(file.list())).parallelStream()
                .map(str -> str.replace(".csv", "")).collect(Collectors.toList());

        // fileNames.parallelStream().sorted(Comparator.reverseOrder()).forEach(System.out::println);
        // fileNames.parallelStream().filter(s -> s.equals(".DS_Store")).forEach(System.out::println);
        System.out.println(fileNames.size());
        System.out.println(all.size());

        all.parallelStream()
                .filter(station -> !fileNames.contains(station.getName()))
                .forEach(System.out::println);

        // write to file
        FileWriter fw = new FileWriter(Config.STATION_MATCH_PATH);
        BufferedWriter bw = new BufferedWriter(fw);
        fw.write("name,lon,lat,cap,type,svf,district");
        bw.newLine();
        all.forEach(station -> {
            try {
                bw.write(station.toString());
                bw.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        bw.flush();
        bw.close();
    }
}
