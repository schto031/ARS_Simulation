package common;

import java.awt.*;

public class DashedStroke extends BasicStroke {

    public DashedStroke(float width) {
        super(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
    }

    public DashedStroke() {
        this(3);
    }
}
