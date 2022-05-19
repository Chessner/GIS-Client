package at.fh.hgb.mc.gis.client;


import at.fh.hgb.mc.gis.feature.*;
import at.fh.hgb.mc.gis.server.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

/**
 * This class provides the logic of the program and represents the Model part of the implemented MVC-pattern.
 *
 * @see GISView
 * @see GISController
 */
public class GISModel {
    /**
     * Observer observing this object.
     */
    public IDataObserver mObserver = null;
    /**
     * Image containing the polygons/data that should be displayed by the view.
     *
     * @see GISView
     */
    private Image mImage = null;
    /**
     * Current width of mImage.
     */
    private int mWidth = 640;
    /**
     * Current height of mImage.
     */
    private int mHeight = 480;
    /**
     * List containing all GeoObjects that have been created/should be drawn in the mImage.
     */
    private List<GeoObject> mData;
    /**
     * List containing all POIObjects that have been created/should be drawn in the mImage.
     */
    private List<POIObject> mPOIData;
    /**
     * Current transformation matrix used for converting world coordinates into window coordinates for drawing on the canvas.
     */
    protected Matrix mTransformationMatrix;
    /**
     * Drawing context for the currently loaded data.
     */
    protected ADrawingContext mDrawingContext;
    /**
     * Constant defining the ratio of dots per inch for a screen.
     */
    private final double mDotPerInch = 72 / 2.54;
    /**
     * Flag indicating whether the points of interest should be drawn as well.
     */
    private boolean mShowPOI = false;
    /**
     * Current database IGISServer.
     */
    protected IGISServer mServer = new OSMLinz();
    /**
     * Bounding box used for restricting the data fetched from the PostGIS database server.
     * Only objects contained within the bounding box will be fetched.
     * Used together with doExtractionWithRestriction().
     */
    private Rectangle mBBox = null;

    /**
     * This method provides a way for an observer to subscribe to this object.
     *
     * @param _observer Object that wants to observe this object.
     */
    public void addMapObserver(IDataObserver _observer) {
        mObserver = _observer;
    }


    /**
     * This method updates all observers.
     */
    protected void update() {
        mObserver.update((BufferedImage) mImage);
    }

    /**
     * Initiates the mImage with the current mWidth and mHeight.
     *
     * @return The generated Image.
     */
    private Image initCanvas() {
        return new BufferedImage(mWidth, mHeight, BufferedImage.TYPE_INT_RGB);
    }

    /**
     * Method used to draw the polygons saved in mData to the mImage.
     */
    public void repaint() {
        if (mImage == null) {
            mImage = initCanvas();
        }

        if (mTransformationMatrix == null) {
            zoomToFit();
        }

        Graphics2D g2D = (Graphics2D) mImage.getGraphics();
        g2D.clearRect(0, 0, mWidth, mHeight);

        for (GeoObject poly : mData) {
            PresentationSchema schema = mDrawingContext.getSchema(poly.getType());
            if (schema != null) {
                schema.paint(g2D, poly, mTransformationMatrix);
            }
        }

        if (mShowPOI) {
            int poiWidth = 40;
            int poiHeight = 40;
            for (POIObject poi : mPOIData) {
                Point tP = mTransformationMatrix.multiply(poi.mPoint);
                mImage.getGraphics().drawImage(poi.mIcon, tP.x - poiWidth / 2, tP.y - poiHeight / 2, poiWidth, poiHeight, null);
            }
        }

        update();
    }


    /**
     * Set method of the mWidth variable.
     *
     * @param _width Value to set mWidth to.
     */
    public void setWidth(int _width) {
        mWidth = _width;
        mImage = null;
    }

    /**
     * Set method of the mHeight variable.
     *
     * @param _height Value to set mHeight to.
     */
    public void setHeight(int _height) {
        mHeight = _height;
        mImage = null;
    }

    /**
     * This method loads data from the PostGIS-server with the help of the DummyGIS class and saves it to mData.
     *
     * @see DummyGIS
     */
    public void loadData() {
        if (mServer.init()) {
            mDrawingContext = mServer.getDrawingContext();
            doStandardExtraction();
        }
    }
    /**
     * This method loads the points of interest that should be displayed on the map.
     *
     * @see DummyGIS
     */
    public void loadPOIData() {
        if (!mShowPOI) return;

        mPOIData = new ArrayList<>();
        Matrix invers = mTransformationMatrix.invers();

        Point p1 = new Point(invers.multiply(new Point(358, 281)));
        Point p2 = new Point(invers.multiply(new Point(282, 316)));
        Point p3 = new Point(invers.multiply(new Point(318, 332)));
        Point p4 = new Point(invers.multiply(new Point(488, 312)));
        Point p5 = new Point(invers.multiply(new Point(336, 252)));
        mPOIData.add(new POIObject("lebakas", p1,
                "poi_icons/lebakas.PNG"));
        mPOIData.add(new POIObject("linzer", p2,
                "poi_icons/linzer.png"));
        mPOIData.add(new POIObject("freistädter", p3,
                "poi_icons/freistädter.jpg"));
        mPOIData.add(new POIObject("kaiser franz josef", p4,
                "poi_icons/kaiser_franz.jpg"));
        mPOIData.add(new POIObject("wieselburg", p5,
                "poi_icons/wieselburger.jpg"));


    }

    /**
     * Stellt intern eine Transformationsmatrix zur Verfuegung, die so
     * skaliert, verschiebt und spiegelt, dass die zu zeichnenden Polygone
     * komplett in den Anzeigebereich passen
     */
    public void zoomToFit() {
        if (mData == null) return;

        Rectangle world = getMapBounds(mData);
        Rectangle window = new Rectangle(0, 0, mWidth, mHeight - 1);
        mTransformationMatrix = Matrix.zoomToFit(world, window, true);
    }

    /**
     * Veraendert die interne Transformationsmatrix so, dass in das
     * Zentrum des Anzeigebereiches herein- bzw. herausgezoomt wird
     *
     * @param _factor Der Faktor um den herein- bzw. herausgezoomt wird
     */
    public void zoom(double _factor) {
        if (mTransformationMatrix == null) return;

        Point point = new Point(mWidth / 2, mHeight / 2);
        zoom(point, _factor);
    }

    /**
     * Veraendert die interne Transformationsmatrix so, dass an dem
     * uebergebenen Punkt herein- bzw. herausgezoomt wird
     *
     * @param _pt     Der Punkt an dem herein- bzw. herausgezoomt wird
     * @param _factor Der Faktor um den herein- bzw. herausgezoomt wird
     */
    public void zoom(Point _pt, double _factor) {
        if (mTransformationMatrix == null) return;

        Matrix t1 = Matrix.translate(-_pt.x, -_pt.y);
        Matrix s = Matrix.scale(_factor);
        Matrix t2 = Matrix.translate(_pt.x, _pt.y);

        mTransformationMatrix = t2.multiply(s.multiply(t1.multiply(mTransformationMatrix)));
    }

    /**
     * Ermittelt die gemeinsame BoundingBox der uebergebenen Polygone
     *
     * @param _poly Die Polygone, fuer die die BoundingBox berechnet
     *              werden soll
     * @return Die BoundingBox
     */
    public Rectangle getMapBounds(List<GeoObject> _poly) {
        Rectangle rectangle = null;
        for (int i = 0; i < _poly.size(); i++) {
            if (rectangle == null) {
                rectangle = _poly.get(i).getBounds();
            } else {
                rectangle = rectangle.union(_poly.get(i).getBounds());
            }
        }
        return rectangle;
    }

    /**
     * Veraendert die interne Transformationsmatrix so, dass
     * die zu zeichnenden Objekt horizontal verschoben werden.
     *
     * @param _delta Die Strecke, um die horizontal verschoben werden soll
     */
    public void scrollHorizontal(int _delta) {
        if (mTransformationMatrix == null) return;
        Matrix t = Matrix.translate(_delta, 0);
        mTransformationMatrix = t.multiply(mTransformationMatrix);
    }


    /**
     * Veraendert die interne Transformationsmatrix so, dass
     * die zu zeichnenden Objekt vertikal verschoben werden.
     *
     * @param _delta Die Strecke, um die vertikal verschoben werden soll
     */
    public void scrollVertical(int _delta) {
        if (mTransformationMatrix == null) return;
        Matrix t = Matrix.translate(0, _delta);
        mTransformationMatrix = t.multiply(mTransformationMatrix);
    }

    /**
     * Changes the internal transformation matrix in a way, that the objects that should be drawn will be rotated.
     *
     * @param _alpha Degree in rad by which the objects will be rotated.
     */
    public void rotate(double _alpha) {
        if (mTransformationMatrix == null) return;

        Rectangle world = getMapBounds(new Vector<>(mData));
        Matrix translationMatrixA = Matrix.translate(-world.getCenterX(), -world.getCenterY());
        Matrix rotationMatrix = Matrix.rotate(_alpha);
        Matrix translationMatrixB = Matrix.translate(world.getCenterX(), world.getCenterY());

        mTransformationMatrix = mTransformationMatrix.multiply(translationMatrixB.multiply(rotationMatrix.multiply(translationMatrixA)));
    }

    /**
     * Ermittelt die Geo-Objekte, die den Punkt (in Bildschirmkoordinaten)
     * enthalten
     *
     * @param _pt Ein Selektionspunkt im Bildschirmkoordinatensystem
     * @return Ein Vektor von Geo-Objekte, die den Punkt enthalten
     * @see java.awt.Point
     * @see GeoObject
     */
    public List<GeoObject> initSelection(Point _pt) {
        Point point = getMapPoint(_pt);
        List<GeoObject> result = new ArrayList<>();
        for (GeoObject geo : mData) {
            if (geo.getBounds().contains(point)) {
                result.add(geo);
            }
        }
        return result;
    }

    /**
     * Stellt intern eine Transformationsmatrix zur Verfuegung, die so
     * skaliert, verschiebt und spiegelt, dass die zu zeichnenden Polygone
     * innerhalb eines definierten Rechtecks (_winBounds) komplett in den
     * Anzeigebereich (die Zeichenflaeche) passen
     *
     * @param _winBounds Der darzustellende Bereich in Bildschirm-Koordinaten
     */
    public void zoomRect(Rectangle _winBounds) {
        Rectangle window = new Rectangle(0, 0, mWidth, mHeight - 1);
        Matrix m = Matrix.zoomToFit(_winBounds, window, false);
        mTransformationMatrix = m.multiply(mTransformationMatrix);
    }

    /**
     * Liefert zu einem Punkt im Bildschirmkoordinatensystem den passenden
     * Punkt im Kartenkoordinatensystem
     *
     * @param _pt Der umzuwandelnde Punkt im Bildschirmkoordinatensystem
     * @return Der gleiche Punkt im Weltkoordinatensystem
     * @see java.awt.Point
     */
    public Point getMapPoint(Point _pt) {
        return mTransformationMatrix.invers().multiply(_pt);
    }

    /**
     * Berechnet den gerade sichtbaren Massstab der Karte
     *
     * @return der Darstellungsmassstab
     * @see Matrix
     */
    protected double calculateScale() {
        // Aspekt b) in der Maßstabsformel
        // ein künstlicher Vektor/ein Objekt; hier der Länge 1cm
        // (gilt nur für DummyGIS-Koordinaten, die in cm angegeben sind)
        Point2D.Double vector = new Point2D.Double(0, 1.0);
        // Aspekt c) in der Maßstabsformel
        Point2D.Double vector_transformed = mTransformationMatrix.multiply(vector);
        double lengthA = mDotPerInch;// Länge von 1cm auf dem Bildschirm (bei 72 DPI)
        double lengthB = vector.distance(0, 0);// Länge von vector
        double lengthC = vector_transformed.distance(0, 0); // Länge von vector_transformed

        double scale = (lengthA * lengthB) / lengthC;

        return scale;
    }

    /**
     * This method updates all observers with the new scale.
     */
    public void updateScale() {
        double scale = calculateScale();
        mObserver.updateScale((int) scale);
    }

    /**
     * This method is used for zooming to a given scale. For example 1:1000.
     * Always zooms in the middle of the displayed area.
     * @param _scale The new scale for displaying objects on mImage.
     */
    public void zoomToScale(int _scale) {
        double currentScale = calculateScale();
        zoom(currentScale / _scale);
    }

    /**
     * Small method for toggling the points of interest on and off.
     */
    public void togglePOIS() {
        mShowPOI = !mShowPOI;
    }

    /**
     * This method takes the current image displayed on screen and saves it to the folder export/images.
     */
    protected void storeScreen() {
        if (mImage == null) return;

        try {
            ImageIO.write((BufferedImage) mImage, "png", new File("export/images/" + mServer.getClass().getSimpleName() + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method changes the bounding box mBBox and updates the data displayed accordingly.
     */
    protected void stick() {
        if (mServer instanceof DummyGIS) return;

        if (mBBox == null) {
            mBBox = mTransformationMatrix.invers().multiply(new Rectangle(0, 0, mWidth, mHeight));
            if (mServer.init()) {
                doExtractionWithRestriction();
            }
        } else {
            mBBox = null;
            if (mServer.init()) {
                doStandardExtraction();
                repaint();
            }
        }
    }

    /**
     * This method handles the extraction of data from the mServer.
     * It takes the provided bounding box mBBox into account and only fetches data that is contained within this box.
     * mServer.init() has to be called before using this method.
     */
    private void doExtractionWithRestriction() {
        if (mBBox == null) return;

        if (mServer instanceof Verwaltungsgrenzen3857Server || mServer instanceof Verwaltungsgrenzen4326Server) {
            mData = mServer.extractData("SELECT * FROM bundeslaender " +
                    "WHERE ST_Intersects(geom, ST_MakeEnvelope(" + mBBox.getMinX() + ", " + mBBox.getMinY() + ", " + mBBox.getMaxX() + "," + mBBox.getMaxY() + "))");
        } else if (mServer instanceof DummyGIS) {
            //mData = mServer.extractData("select * from data where type in (233, 931, 932, 933, 934, 1101)");
            System.out.println("Cannot do sticky with DummyGIS server... Please use a different server");
        } else if (mServer instanceof OSMHagenberg) {
            List<GeoObject> landuse = mServer.extractData("SELECT * FROM osm_landuse WHERE ST_Intersects(geom, ST_MakeEnvelope(" + mBBox.getMinX() + ", " + mBBox.getMinY() + ", " + mBBox.getMaxX() + "," + mBBox.getMaxY() + "))");
            mServer.init();
            List<GeoObject> natural = mServer.extractData("SELECT * FROM osm_natural WHERE ST_Intersects(geom, ST_MakeEnvelope(" + mBBox.getMinX() + ", " + mBBox.getMinY() + ", " + mBBox.getMaxX() + "," + mBBox.getMaxY() + "))");
            mServer.init();
            List<GeoObject> building = mServer.extractData("SELECT * FROM osm_building WHERE ST_Intersects(geom, ST_MakeEnvelope(" + mBBox.getMinX() + ", " + mBBox.getMinY() + ", " + mBBox.getMaxX() + "," + mBBox.getMaxY() + "))");
            mData = new LinkedList<>();
            mData.addAll(natural);
            mData.addAll(landuse);
            mData.addAll(building);
        } else if (mServer instanceof OSMLinz) {
            List<GeoObject> landuse = mServer.extractData("SELECT * FROM osm_landuse WHERE ST_Intersects(geom, ST_MakeEnvelope(" + mBBox.getMinX() + ", " + mBBox.getMinY() + ", " + mBBox.getMaxX() + "," + mBBox.getMaxY() + "))");
            mServer.init();
            List<GeoObject> natural = mServer.extractData("SELECT * FROM osm_natural WHERE ST_Intersects(geom, ST_MakeEnvelope(" + mBBox.getMinX() + ", " + mBBox.getMinY() + ", " + mBBox.getMaxX() + "," + mBBox.getMaxY() + "))");
            mServer.init();
            List<GeoObject> building = mServer.extractData("SELECT * FROM osm_building WHERE ST_Intersects(geom, ST_MakeEnvelope(" + mBBox.getMinX() + ", " + mBBox.getMinY() + ", " + mBBox.getMaxX() + "," + mBBox.getMaxY() + "))");
            mData = new LinkedList<>();
            mData.addAll(natural);
            mData.addAll(landuse);
            mData.addAll(building);
        }
    }

    /**
     * This method handles the standard extraction of data from the mServer.
     * mServer.init() has to be called before using this method.
     */
    private void doStandardExtraction() {
        if (mServer instanceof Verwaltungsgrenzen3857Server || mServer instanceof Verwaltungsgrenzen4326Server) {
            mData = mServer.extractData("SELECT * FROM bundeslaender");
        } else if (mServer instanceof DummyGIS) {
            mData = new LinkedList<>(mServer.extractData("select * from data where type in (233, 931, 932, 933, 934, 1101)"));
        } else if (mServer instanceof OSMHagenberg) {
            List<GeoObject> landuse = mServer.extractData("SELECT * FROM osm_landuse");
            mServer.init();
            List<GeoObject> natural = mServer.extractData("SELECT * FROM osm_natural");
            mServer.init();
            List<GeoObject> building = mServer.extractData("SELECT * FROM osm_building");
            mData = new LinkedList<>();
            mData.addAll(natural);
            mData.addAll(landuse);
            mData.addAll(building);
        } else if(mServer instanceof OSMLinz){
            List<GeoObject> landuse = mServer.extractData("SELECT * FROM osm_landuse");
            mServer.init();
            List<GeoObject> natural = mServer.extractData("SELECT * FROM osm_natural");
            mServer.init();
            List<GeoObject> building = mServer.extractData("SELECT * FROM osm_building");
            mData = new LinkedList<>();
            mData.addAll(natural);
            mData.addAll(landuse);
            mData.addAll(building);
        }
    }
}
