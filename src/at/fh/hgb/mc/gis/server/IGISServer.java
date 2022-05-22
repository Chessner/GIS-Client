package at.fh.hgb.mc.gis.server;

import at.fh.hgb.mc.gis.feature.ADrawingContext;
import at.fh.hgb.mc.gis.feature.GeoObject;

import java.util.List;

/**
 * A class implements the IGISServer interface in order to be used as a GIS-Server.
 */
public interface IGISServer {

    /**
     * Initializes the object, including the connection to the database server.
     *
     * @return Boolean, whether the initialization was successful.
     */
    boolean init();

    /**
     * This method fetches the data, specified by the given statement, from the database server.
     *
     * @param _statement SQL Statement in String format.
     * @param _keepConnectionOpen Flag indicating whether the connection should be closed or not.
     * @return List of GeoObjects containing the fetched data.
     */
    List<GeoObject> extractData(String _statement, boolean _keepConnectionOpen);

    /**
     * This method provides a ADrawingContext for the different GeoObject
     * types that can be fetched from the database server.
     *
     * @return ADrawingContext for the types.
     */
    ADrawingContext getDrawingContext();

    /**
     * This method is used to close the connection to the server.
     */
    void closeConnection();
}
