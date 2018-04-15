package blinking.model;

import com.google.common.base.MoreObjects;

/**
 * @author tangym
 * @date 2018-04-16 1:24
 */
public class Station2 {
    private String name;
    private short type1;
    private short type2;
    private short type12;
    private String singleSVF;
    private String meanSVF;

    public Station2(String name, short type1, short type2, short type12, String singleSVF, String meanSVF) {
        this.name = name;
        this.type1 = type1;
        this.type2 = type2;
        this.type12 = type12;
        this.singleSVF = singleSVF;
        this.meanSVF = meanSVF;
    }

    public String getName() {
        return name;
    }

    public short getType1() {
        return type1;
    }

    public short getType2() {
        return type2;
    }

    public short getType12() {
        return type12;
    }

    public String getSingleSVF() {
        return singleSVF;
    }

    public String getMeanSVF() {
        return meanSVF;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("type1", type1)
                .add("type2", type2)
                .add("type12", type12)
                .add("singleSVF", singleSVF)
                .add("meanSVF", meanSVF)
                .toString();
    }
}
