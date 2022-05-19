package at.fh.hgb.mc.gis.feature;

import java.awt.*;
/**
 * This class provides an implementation of the GeoObjectPart interface.
 * This class acts as a representation of a Point within the GeoObject.
 */
public class GeoPoint implements GeoObjectPart {
    /**
     * Point defining this GeoPoint.
     */
    public java.awt.Point mGeometry;

    /**
     * Constructs a new GeoPoint with a given Point.
     * @param _p Point this GeoPoint should be initialized with.
     */
    public GeoPoint(Point _p) {
        mGeometry = _p;
    }

    public void draw(Graphics2D _g, Matrix _m, Color _lineColor, Color _fillColor) {

        if (mGeometry == null) return;

        Point transformedPoint = _m.multiply(mGeometry);

        _g.setColor(_fillColor);
        _g.fillOval(transformedPoint.x, transformedPoint.y, 5, 5);
        _g.setColor(_lineColor);
        _g.drawOval(transformedPoint.x, transformedPoint.y, 5, 5);
    }


    public Rectangle getBounds() {
        if (mGeometry != null) {
            Rectangle bound = new Rectangle(mGeometry.x-10, mGeometry.y-10, 20, 20);
            return bound;
        }
        return null;
    }

    @Override
    public String toString() {
        return mGeometry.toString();
    }
}
