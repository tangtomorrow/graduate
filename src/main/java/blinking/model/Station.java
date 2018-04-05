package blinking.model;

import java.math.BigDecimal;
import java.util.Objects;

public class Station {
    private String name; // 站点名
    private double lon; // 经度
    private double lat; // 纬度
    private double cap;
    private int type;
    private BigDecimal svf;
    private String district;

    public Station(String name, double lon, double lat, double cap, int type, BigDecimal svf, String district) {
        this.name = name;
        this.lon = lon;
        this.lat = lat;
        this.cap = cap;
        this.type = type;
        this.svf = svf;
        this.district = district;
    }

    public String getName() {
        return name;
    }

    public double getLon() {
        return lon;
    }

    public double getLat() {
        return lat;
    }

    public double getCap() {
        return cap;
    }

    public int getType() {
        return type;
    }

    public BigDecimal getSvf() {
        return svf;
    }

    public String getDistrict() {
        return district;
    }

    @Override
    public String toString() {
        return name +
                "," + lon +
                "," + lat +
                "," + cap +
                "," + type +
                "," + svf +
                "," + district;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Station station = (Station) o;
        return Double.compare(station.lon, lon) == 0 &&
                Double.compare(station.lat, lat) == 0 &&
                Double.compare(station.cap, cap) == 0 &&
                type == station.type &&
                Objects.equals(name, station.name) &&
                Objects.equals(svf, station.svf) &&
                Objects.equals(district, station.district);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, lon, lat, cap, type, svf, district);
    }
}
