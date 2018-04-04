package blinking.model;

import com.google.common.base.MoreObjects;
import tech.tablesaw.api.Table;

/**
 * @author tangym
 * @date 2018-04-04 18:11
 */
public class StationDetail {
    private Station station;
    private Table table;

    public StationDetail(Station station, Table table) {
        this.station = station;
        this.table = table;
    }

    public Table getTable() {
        return table;
    }

    public Station getStation() {
        return station;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("station", station)
                .add("table", table)
                .toString();
    }
}
