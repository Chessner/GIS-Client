package at.fh.hgb.mc.gis.server;

import at.fh.hgb.mc.gis.client.OSMDrawingContext;
import at.fh.hgb.mc.gis.feature.ADrawingContext;
import org.postgresql.PGConnection;
import org.postgresql.util.PGobject;

import java.sql.DriverManager;

/**
 * This class extends the OSMServer class to provide the missing IGISServer functionality.
 */
public class OSMHawaii extends OSMServer {

    @Override
    public boolean init() {
        try {
            /* Load the JDBC driver and establish a connection. */
            Class.forName("org.postgresql.Driver");
            String url =
                    "jdbc:postgresql://localhost:5432/OSMHawaii";
            mConnection = DriverManager.getConnection(url, "geo", "geo");
            /* Add the geometry types to the connection. */
            PGConnection c = (PGConnection) mConnection;
            // alternativ org.postgis.PGgeometry.class
            c.addDataType("geometry",
                    (Class<? extends PGobject>) Class.forName("net.postgis.jdbc.PGgeometry"));
            // alterantiv org.postgis.PGbox2d.class
            c.addDataType("box2d",
                    (Class<? extends PGobject>) Class.forName("net.postgis.jdbc.PGbox2d"));
            return true;
        } catch (Exception _e) {
            _e.printStackTrace();
            return false;
        }
    }



    @Override
    public ADrawingContext getDrawingContext() {
        return new OSMDrawingContext();
    }

}
