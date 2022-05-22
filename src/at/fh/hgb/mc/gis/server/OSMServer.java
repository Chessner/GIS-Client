package at.fh.hgb.mc.gis.server;

import at.fh.hgb.mc.gis.feature.*;
import net.postgis.jdbc.PGgeometry;
import net.postgis.jdbc.geometry.Geometry;
import net.postgis.jdbc.geometry.LinearRing;

import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * This class implements the IGISServer interface and acts as an intermediary so other OSMServers only have to
 * initialize themselves.
 */
public abstract class OSMServer implements IGISServer {
    /**
     * List used as a buffer for all the extracted GeoObjects, while the extraction is still ongoing.
     */
    protected List<GeoObject> mList = new LinkedList();
    /**
     * Connection to the database server.
     */
    protected Connection mConnection;

    @Override
    public List<GeoObject> extractData(String _statement, boolean _keepConnectionOpen) {
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
                                net.postgis.jdbc.geometry.Point pPG = ring.getPoint(i);
                                poly.addPoint((int) pPG.x, (int) pPG.y);
                            }

                            GeoArea area = new GeoArea(poly);
                            list.add(area);

                            if (p.numRings() > 1) {
                                for (int j = 1; j < p.numRings(); j++) {
                                    ring = p.getRing(j);
                                    poly = new Polygon();
                                    for (int i = 0; i < ring.numPoints(); i++) {
                                        net.postgis.jdbc.geometry.Point pPG = ring.getPoint(i);
                                        poly.addPoint((int) pPG.x, (int) pPG.y);
                                    }
                                    area.addHole(new GeoArea(poly));
                                }
                            }

                            mList.add(new GeoObject(id, type, "POLYGON", attr, list));

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
                                    poly.addPoint((int) pPG.x, (int) pPG.y);
                                } // Stop going through the first ring (->mainpoly) of the polygon

                                GeoArea area = new GeoArea(poly); //Save the main poly as area
                                list.add(area);

                                if (mainPoly.numRings() > 1) {
                                    for (int l = 1; l < mainPoly.numRings(); l++) { // Go through the other rings (holes in mainpoly)
                                        ring = mainPoly.getRing(l);
                                        poly = new Polygon();
                                        for (int i = 0; i < ring.numPoints(); i++) {
                                            net.postgis.jdbc.geometry.Point pPG = ring.getPoint(i);
                                            poly.addPoint((int) pPG.x, (int) pPG.y);
                                        }
                                        area.addHole(new GeoArea(poly));
                                    }
                                }
                            }// stop going through all polygons of this multipolygon

                            mList.add(new GeoObject(id, type, "MULTIPOLYGON", attr, list));

                        }
                    }
                    break;
                    case Geometry.POINT: {
                        String wkt = geom.toString();
                        net.postgis.jdbc.geometry.Point p = new net.postgis.jdbc.geometry.Point(wkt);
                        LinkedList<GeoObjectPart> list = new LinkedList<>();
                        list.add(new GeoPoint(new Point((int) p.x, (int) p.y)));
                        mList.add(new GeoObject(id, type, "POINT", attr, list));
                    }
                    break;
                    case Geometry.LINESTRING:
                        String wkt = geom.toString();
                        net.postgis.jdbc.geometry.LineString lineString = new net.postgis.jdbc.geometry.LineString(wkt);
                        List<Point> list = new ArrayList<>();
                        for (int i = 0; i < lineString.numPoints(); i++) {
                            net.postgis.jdbc.geometry.Point p = lineString.getPoint(i);
                            list.add(new Point((int) p.getX(), (int) p.getY()));
                        }
                        GeoLine line = new GeoLine(list);
                        ArrayList<GeoObjectPart> geoList = new ArrayList();
                        geoList.add(line);
                        mList.add(new GeoObject(id, type, "LINE", attr, geoList));
                        break;
                }
            }
            s.close();
            if(!_keepConnectionOpen) mConnection.close();
        } catch (
                Exception _e) {
            _e.printStackTrace();
        }
        return mList;
    }

    @Override
    public void closeConnection() {
        try {
            mConnection.close();
        } catch (SQLException _e) {
            _e.printStackTrace();
        }
    }
}
