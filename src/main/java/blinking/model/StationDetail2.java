package blinking.model;

import com.google.common.base.MoreObjects;
import tech.tablesaw.api.Table;

/**
 * @author tangym
 * @date 2018-04-04 18:11
 */
public class StationDetail2 {
    private Station2 station;
    private Table table;

    public StationDetail2(Station2 station, Table table) {
        this.station = station;
        this.table = table;
    }

    public Table getTable() {
        return table;
    }

    public Station2 getStation() {
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
