package blinking.parser;

import blinking.model.Station2;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author tangym
 * @date 2018-04-16 1:23
 */
public class StationParser2 {
    private static final Splitter DOT_SPLITTER = Splitter.on(",").omitEmptyStrings().trimResults();

    public List<Station2> parse(String path, boolean withHeader) throws IOException {
        File file = new File(path);
        List<String> lines = Files.readLines(file, Charsets.UTF_8);
        Stream<String> stream = withHeader ? lines.stream().skip(1) : lines.stream();
        return stream.map(s -> {
            List<String> strs = DOT_SPLITTER.splitToList(s);
            return strs.size() == 6 ? new Station2(
                    strs.get(0).trim(),
                    Short.parseShort(strs.get(1).trim()),
                    Short.parseShort(strs.get(2).trim()),
                    Short.parseShort(strs.get(3).trim()),
                    strs.get(4).trim(),
                    strs.get(4).trim()) : null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
