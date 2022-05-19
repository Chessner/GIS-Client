package at.fh.hgb.mc.gis.feature;

import java.awt.*;

/**
 * This class provides storage for multiple variables used for drawing GeoObjectParts.
 */
public class PresentationSchema {
    /**
     * Color used for drawing lines of GeoObjectParts.
     */
    private Color mLineColor;
    /**
     * Color used for filling GeoObjectParts
     */
    private Color mFillColor;
    /**
     * LineWidth used for drawing lines.
     */
    private float mLineWidth;

    /**
     * Constructs new PresentationSchema with given variables.
     * @param _lineColor LineColor of this PresentationSchema.
     * @param _fillColor FillColor of this PresentationSchema.
     * @param _lineWidth LineWidth of this PresentationSchema.
     */
    public PresentationSchema(Color _lineColor, Color _fillColor, float _lineWidth){
        mFillColor = _fillColor;
        mLineColor = _lineColor;
        mLineWidth = _lineWidth;
    }

    /**
     * This method lets all GeoObjectParts of the given GeoObject draw themselves with its variables.
     * @param _g Graphics2D the GeoObjectParts should draw themselves on.
     * @param _obj Given GeoObject.
     * @param _m Matrix used for adjusting the GeoObjectParts coordinates.
     */
    public void paint(Graphics2D _g, GeoObject _obj, Matrix _m) {
        for(GeoObjectPart part: _obj.getPart()){
            _g.setStroke(new BasicStroke(mLineWidth));
            part.draw(_g,_m,mLineColor,mFillColor);
        }
    }
}
