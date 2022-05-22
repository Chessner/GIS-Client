package at.fh.hgb.mc.gis.feature;

import java.awt.*;
import java.util.List;

/**
 * This class provides an implementation of the GeoObjectPart interface.
 * This class acts as a representation for Lines within the GeoObject.
 */
public class GeoLine implements GeoObjectPart {
    /**
     * List of points defining a line.
     */
    List<java.awt.Point> mGeometry;

    /**
     * Constructs a new GeoLine.
     * @param _points List of points defining the new line.
     */
    public GeoLine(List<Point> _points) {
        mGeometry = _points;
    }


    @Override
    public void draw(Graphics2D _g, Matrix _m, Color _lineColor, Color _fillColor) {
        if (mGeometry == null) return;

        int[] xPoints = new int[mGeometry.size()];
        int[] yPoints = new int[mGeometry.size()];
        int nPoints = mGeometry.size();
        for (int i = 0; i < nPoints; i++) {
            Point p = mGeometry.get(i);
            p = _m.multiply(p);
            xPoints[i] = p.x;
            yPoints[i] = p.y;
        }
        BasicStroke stroke = (BasicStroke) _g.getStroke();
        _g.setStroke(new BasicStroke(stroke.getLineWidth()*2));
        _g.setColor(_lineColor);
        _g.drawPolyline(xPoints, yPoints, nPoints);
        _g.setStroke(stroke);
    }

    @Override
    public Rectangle getBounds() {
        if (mGeometry != null) {
            if (!mGeometry.isEmpty()) {
                Point p = mGeometry.get(0);
                Rectangle result = new Rectangle(p.x-10, p.y-10, 20, 20).getBounds();
                for (int i = 1; i < mGeometry.size(); i++) {
                    p = mGeometry.get(i);
                    Rectangle bound = new Rectangle(p.x-10, p.y-10, 20, 20).getBounds();
                    result = result.union(bound);
                }
                return result;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return mGeometry.toString();
    }
}
