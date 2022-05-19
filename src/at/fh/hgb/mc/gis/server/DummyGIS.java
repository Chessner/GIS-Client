package at.fh.hgb.mc.gis.server;

import at.fh.hgb.mc.gis.client.DummyDrawingContext;
import at.fh.hgb.mc.gis.feature.*;
import de.intergis.JavaClient.comm.*;
import de.intergis.JavaClient.gui.IgcConnection;

import java.awt.*;
import java.util.LinkedList;

/**
 * This class provides functionality to request data from a Geo-Server.
 */
public class DummyGIS implements IGISServer {
    // die Verbindung zum Geo-Server
    CgGeoConnection m_geoConnection = null;
    // das Anfrage-Interface des Geo-Servers
    CgGeoInterface m_geoInterface = null;

    /**
     * Constructs an empty DummyGIS object.
     */
    public DummyGIS() {
    }

    /**
     * Initializes the object and establishes a connection to the server.
     *
     * @return Boolean regarding the success of the initialization.
     */
    public boolean init() {
        try {
            // der Geo-Server wird initialisiert
            m_geoConnection =
                    new IgcConnection(new CgConnection("admin",
                            "admin",
                            "T:localhost:4949",
                            null));
            // das Anfrage-Interface des Servers wird abgeholt
            m_geoInterface = m_geoConnection.getInterface();
            return true;
        } catch (Exception _e) {
            _e.printStackTrace();
        }
        return false;
    }

    /**
     * Extrahiert einige Geoobjekte aus dem Server
     */
    public java.util.List<GeoObject> extractData(String _stmt) {
        try {
            CgStatement stmt = m_geoInterface.Execute(_stmt);
            CgResultSet cursor = stmt.getCursor();
            LinkedList<GeoObject> objectContainer = new LinkedList<>();
            while (cursor.next()) {
                CgIGeoObject obj = cursor.getObject();
                CgIGeoPart[] parts = obj.getParts();
                for (int i = 0; i < parts.length; i++) {
                    //System.out.println("PART " + i);
                    int pointCount = parts[i].getPointCount();
                    int[] xArray = parts[i].getX();
                    int[] yArray = parts[i].getY();
                    Polygon poly = new Polygon(xArray, yArray, pointCount);

                    LinkedList<GeoObjectPart> list = new LinkedList<>();
                    list.add(new GeoArea(poly));
                    objectContainer.add(new GeoObject(obj.getName(), obj.getCategory(), "POLYGON","", list));
                } // for i
                System.out.println();
            } // while cursor
            return objectContainer;
        } catch (Exception _e) {
            _e.printStackTrace();
        }
        return null;
    }

    public ADrawingContext getDrawingContext() {
        return new DummyDrawingContext();
    }
}
