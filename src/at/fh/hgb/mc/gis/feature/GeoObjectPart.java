package at.fh.hgb.mc.gis.feature;

import java.awt.*;

/**
 * A class implements the GeoObjectPart interface to indicate that it can be used within GeoObjects.
 */
public interface GeoObjectPart {

    /**
     * This method lets the implementing class draw itself.
     * @param _g Graphics2D object the object should draw itself on.
     * @param _m Matrix for adjusting the coordinates of the object.
     * @param _lineColor Color used for drawing the outside line of the object.
     * @param _fillColor Color used for filling the object.
     */
    void draw(Graphics2D _g, Matrix _m, Color _lineColor, Color _fillColor);

    /**
     * This method provides the boundaries of this GeoObjectPart.
     * @return Boundaries as rectangle. Null if this GeoObjectPart has no geometry.
     */
    Rectangle getBounds();

    /**
     * Returns a string representation of the object.
     * @return String representation.
     */
    String toString();
}
