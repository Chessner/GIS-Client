package at.fh.hgb.mc.gis.feature;


import java.awt.image.BufferedImage;

/**
 * A class can implement this Observer interface when it wants to be informed of changes in observable objects.
 */
public interface IDataObserver {
    /**
     * This method is called whenever the observed object is changed.
     * @param _image BufferedImage containing the data the observer should be updated with.
     */
    public void update(BufferedImage _image);

    /**
     * This method is called whenever the observed object is changed.
     * @param _scale Integer containing the data the observer should be updated with.
     */
    public void updateScale(int _scale);

}
