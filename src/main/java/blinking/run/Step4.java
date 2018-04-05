package blinking.run;

import blinking.model.StationDetail;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

/**
 * step4. 正题-计算
 *
 * @author tangym
 * @date 2018-04-05 下午11:59
 */
public class Step4 {
    public static void main(String[] args) {
        List<StationDetail> stationDetails = Step3.gets();
        System.out.println(stationDetails);
        Map<Integer, Long> map = stationDetails.stream().collect(groupingBy(o -> o.getStation().getType(), counting()));
        System.out.println(map);
    }
}
