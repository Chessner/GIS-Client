package at.fh.hgb.mc.gis.server;


import at.fh.hgb.mc.gis.client.VerwaltungsgrenzenDrawingContext;
import at.fh.hgb.mc.gis.feature.*;
import net.postgis.jdbc.PGgeometry;
import net.postgis.jdbc.geometry.Geometry;
import net.postgis.jdbc.geometry.LinearRing;
import net.postgis.jdbc.geometry.Point;
import org.postgresql.PGConnection;
import org.postgresql.util.PGobject;

import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

/**
 * This class implements the IGISServer interface to be used as a client for a database server.
 */
public class Verwaltungsgrenzen4326Server implements IGISServer {
    /**
     * List used as a buffer for all the extracted GeoObjects, while the extraction is still ongoing.
     */
    private List<GeoObject> mList = new LinkedList();
    /**
     * Connection to the database server.
     */
    private Connection mConnection;


    @Override
    public boolean init() {
        try {
            /* Load the JDBC driver and establish a connection. */
            Class.forName("org.postgresql.Driver");
            String url =
                    "jdbc:postgresql://localhost:5432/osm";
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
    public List<GeoObject> extractData(String _statement) {
        mList = new LinkedList<>();
        try {
            /* Create a statement and execute a select query. */
            Statement s = mConnection.createStatement();
            ResultSet r = s.executeQuery(_statement);
            while (r.next()) {
                String id = r.getString("id");
                int type = r.getInt("type");
                String attr = r.getString("attr");

                PGgeometry geom = (PGgeometry) r.getObject("geom");
                switch (geom.getGeoType()) {
                    case Geometry.POLYGON: {
                        String wkt = geom.toString();
                        net.postgis.jdbc.geometry.Polygon p = new net.postgis.jdbc.geometry.Polygon(wkt);
                        if (p.numRings() >= 1) {
                            Polygon poly = new Polygon();
                            LinkedList<GeoObjectPart> list = new LinkedList<>();

                            // Ring 0 --> main polygon ... rest should be holes
                            LinearRing ring = p.getRing(0);
                            for (int i = 0; i < ring.numPoints(); i++) {
                                Point pPG = ring.getPoint(i);
                                poly.addPoint((int) (pPG.x * 1000), (int) (pPG.y * 1000));
                            }
                            GeoArea area = new GeoArea(poly);
                            list.add(area);

                            if (p.numRings() > 1) {
                                for (int j = 1; j < p.numRings(); j++) {
                                    ring = p.getRing(j);
                                    poly = new Polygon();
                                    for (int i = 0; i < ring.numPoints(); i++) {
                                        net.postgis.jdbc.geometry.Point pPG = ring.getPoint(i);
                                        poly.addPoint((int) (pPG.x * 1000), (int) (pPG.y * 1000));
                                    }
                                    area.addHole(new GeoArea(poly));
                                }
                            }
                            mList.add(new GeoObject(id, type,"POLYGON",attr, list));

                        }
                    }
                    break;
                    case Geometry.MULTIPOLYGON: {
                        String wkt = geom.toString();
                        net.postgis.jdbc.geometry.MultiPolygon p = new net.postgis.jdbc.geometry.MultiPolygon(wkt);
                        if (p.numPolygons() >= 1) {
                            Polygon poly;
                            LinkedList<GeoObjectPart> list = new LinkedList<>();
                            for (int j = 0; j < p.numPolygons(); j++) { // start going through all polygons of this multipolygon
                                poly = new Polygon();

                                net.postgis.jdbc.geometry.Polygon mainPoly = p.getPolygon(j);
                                LinearRing ring = mainPoly.getRing(0);
                                for (int i = 0; i < ring.numPoints(); i++) { // Start going through the first ring (->mainpoly) of the polygon
                                    net.postgis.jdbc.geometry.Point pPG = ring.getPoint(i);
                                    poly.addPoint((int) (pPG.x * 1000), (int) (pPG.y * 1000));
                                } // Stop going through the first ring (->mainpoly) of the polygon

                                GeoArea area = new GeoArea(poly); //Save the main poly as area
                                list.add(area);

                                if (mainPoly.numRings() > 1) {
                                    for (int l = 1; l < mainPoly.numRings(); l++) { // Go through the other rings (holes in mainpoly)
                                        ring = mainPoly.getRing(l);
                                        poly = new Polygon();
                                        for (int i = 0; i < ring.numPoints(); i++) {
                                            net.postgis.jdbc.geometry.Point pPG = ring.getPoint(i);
                                            poly.addPoint((int) (pPG.x * 1000), (int) (pPG.y * 1000));
                                        }
                                        area.addHole(new GeoArea(poly));
                                    }
                                }
                            }// stop going through all polygons of this multipolygon

                            mList.add(new GeoObject(id, type, "MULTIPOLYGON",attr,list));

                        }
                    }
                    break;
                    case Geometry.POINT:
                        String wkt = geom.toString();
                        net.postgis.jdbc.geometry.Point p = new net.postgis.jdbc.geometry.Point(wkt);
                        LinkedList<GeoObjectPart> list = new LinkedList<>();
                        list.add(new GeoPoint(new java.awt.Point((int) (p.x * 1000), (int) (p.y * 1000))));
                        mList.add(new GeoObject(id, type,"POINT",attr, list));
                        break;

                }
            }
            s.close();
            mConnection.close();
        } catch (
                Exception _e) {
            _e.printStackTrace();
        }
        return mList;
    }

    @Override
    public ADrawingContext getDrawingContext() {
        return new VerwaltungsgrenzenDrawingContext();
    }


}
