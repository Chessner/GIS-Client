package at.fh.hgb.mc.gis.feature;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * This class extends GeoObject to provide a way to draw icons(images) at specific Points.
 * This class is used as a representation for points of interest.
 */
public class POIObject extends GeoObject {
    /**
     * Image to draw.
     */
    public BufferedImage mIcon;
    /**
     * Point specifying where the icon should be drawn.
     */
    public java.awt.Point mPoint;

    /**
     * Constructs a new POIObject.
     *
     * @param _id       The id of this object.
     * @param _point    The point of this object.
     * @param _filepath Filepath containing the icon that should be used.
     */
    public POIObject(String _id, java.awt.Point _point, String _filepath) {
        super(_id, 0, null, null, null);
        mPoint = _point;
        try {
            File file = new File(_filepath);
            mIcon = ImageIO.read(file);
        } catch (IOException _e) {
            _e.printStackTrace();
        }
    }


}
