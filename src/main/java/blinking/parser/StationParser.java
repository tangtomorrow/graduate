package blinking.parser;

import blinking.model.Station;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StationParser {
    private static final Splitter DOT_SPLITTER = Splitter.on(",").omitEmptyStrings().trimResults();

    public List<Station> parse(String path, boolean withHeader) throws IOException {
        File file = new File(path);
        List<String> lines = Files.readLines(file, Charsets.UTF_8);
        Stream<String> stream = withHeader ? lines.stream().skip(1) : lines.stream();
        return stream.map(s -> {
            List<String> strs = DOT_SPLITTER.splitToList(s);
            return strs.size() == 7 ? new Station(
                    strs.get(0).trim(),
                    Double.parseDouble(strs.get(1).trim()),
                    Double.parseDouble(strs.get(2).trim()),
                    Double.parseDouble(strs.get(3).trim()),
                    Integer.parseInt(strs.get(4).trim()),
                    Double.parseDouble(strs.get(5).trim().replace("%", "")) / 100.0,
                    strs.get(6).trim()) : null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
