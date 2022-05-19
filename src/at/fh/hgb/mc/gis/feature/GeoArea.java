package at.fh.hgb.mc.gis.feature;

import java.awt.*;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides an implementation of the GeoObjectPart interface.
 * This class acts as a representation of an area within the GeoObject.
 */
public class GeoArea implements GeoObjectPart{
    /**
     * Polygon defining the main part of the area.
     */
    Polygon mGeometry;
    /**
     * List of GeoAreas that act as a hole in the main part of the area (mGeometry).
     */
    public List<GeoArea> mHoles;

    /**
     * This method adds a new GeoArea to the list of holes in the mGeometry Polygon.
     * @param _hole GeoArea defining the hole.
     */
    public void addHole(GeoArea _hole){
        if(mHoles == null){
            mHoles = new ArrayList<>();
        }
        mHoles.add(_hole);
    }

    /**
     * This method combines the main polygon (mGeometry) and the list of holes to create an Area object.
     * This Area object is then drawn.
     * @param _g Graphics2D object the object should draw itself on.
     * @param _m Matrix for adjusting the coordinates of the implementing class.
     * @param _lineColor Color used for drawing the outside line of the object.
     * @param _fillColor Color used for filling the object.
     */
    public void draw(Graphics2D _g, Matrix _m, Color _lineColor, Color _fillColor) {

        if(mGeometry == null) return;

        Polygon transformedPoly = _m.multiply(mGeometry);
        Area area = new Area(transformedPoly);
        if(mHoles != null) {
            for (GeoArea hole : mHoles) {
                Polygon tpoly = _m.multiply(hole.mGeometry);
                area.subtract(new Area(tpoly));
            }
        }
        _g.setColor(_fillColor);
        _g.fill(area);
        _g.setColor(_lineColor);
        _g.draw(area);
    }


    public Rectangle getBounds() {
        if(mGeometry != null){
            return mGeometry.getBounds();
        }
        return null;
    }

    @Override
    public String toString() {
        if(mGeometry != null){
            StringBuilder b = new StringBuilder();
            //int[] xArray = mGeometry.xpoints;
            //int[] yArray = mGeometry.ypoints;
            //for (int j = 0; j < mGeometry.npoints; j++) {
            //    b.append("[").append(xArray[j]).append(" ; ").append(yArray[j]).append("]");
            //}
            b.append(mGeometry);
            return b.toString();
        }
        return "";
    }

    /**
     * Constructs a new GeoArea.
     */
    public GeoArea(){
        mGeometry = new Polygon();
    }

    /**
     * Constructs a new GeoArea with a given Polygon.
     * @param _poly Polygon this GeoArea should be initialized with.
     */
    public GeoArea(Polygon _poly){
        mGeometry = _poly;
    }

}
