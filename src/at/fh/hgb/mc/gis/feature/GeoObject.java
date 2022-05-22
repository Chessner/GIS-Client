package at.fh.hgb.mc.gis.feature;


import java.awt.*;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * This class acts as a storage class for data extracted from a database server.
 */
public class GeoObject implements Comparable<GeoObject>, Comparator<GeoObject> {
    /**
     * Unique id.
     */
    private String mID;
    /**
     * Type defining the object.
     * e.g. House, Street,...
     */
    private int mType;
    /**
     * Geometric type of the object.
     * e.g. Polygon, Line,...
     */
    private String mGeomType;
    /**
     * All available attributes on the object.
     * e.g. Name, speed limit for streets,...
     */
    private String mAttr;
    /**
     * Polygon containing the actual data of the object.
     */
    private List<GeoObjectPart> mParts = new LinkedList<>();

    /**
     * Konstruktor
     *
     * @param _id    Die Id des Objektes
     * @param _type  Der Typ des Objektes
     * @param _parts Die Geometrie des Objektes
     */
    public GeoObject(String _id, int _type, String _geomType, String _attr ,List<GeoObjectPart> _parts) {
        mID = _id;
        mType = _type;
        mParts = _parts;
        mGeomType = _geomType;
        mAttr = _attr;
    }

    /**
     * Constructs an empty GeoObject.
     */
    public GeoObject(){

    }

    /**
     * Liefert die Id des Geo-Objektes
     *
     * @return Die Id des Objektes
     * @see java.lang.String
     */
    public String getId() {
        return mID;
    }

    /**
     * Liefert den Typ des Geo-Objektes
     *
     * @return Der Typ des Objektes
     */
    public int getType() {
        return mType;
    }

    /**
     * Liefert den Geom-Typ des Geo-Objektes
     *
     * @return Der Geom-Typ des Objektes
     */
    public String getGeomType() {
        return mGeomType;
    }

    /**
     * Liefert die Attribute des Geo-Objektes
     *
     * @return Die Attribute des Objektes
     */
    public String getAttr(){
        return mAttr;
    }

    /**
     * Liefert die Geometrie des Geo-Objektes
     *
     * @return das Polygon des Objektes
     */
    public List<GeoObjectPart> getPart() {
        return mParts;
    }

    /**
     * Liefert die Bounding Box der Geometrie
     *
     * @return die Bounding Box der Geometrie als Rechteckobjekt
     * @see java.awt.Rectangle
     */
    public Rectangle getBounds() {
        if (!mParts.isEmpty()) {
            Rectangle result = mParts.get(0).getBounds();
            for (int i = 1; i < mParts.size(); i++) {
                result = result.union(mParts.get(i).getBounds());
            }
            return result;
        }
        return null;
    }

    /**
     * Gibt die internen Informationen des Geo-Objektes als
     * String zurueck
     *
     * @return Der Inhalt des Objektes in Form eines Strings
     */
    public String toString() {
        if (mID != null) {
            StringBuilder b = new StringBuilder();
            b.append("name: ");
            b.append(mID);
            b.append("\n");
            b.append("type: ");
            b.append(mType);
            for (GeoObjectPart part : mParts) {
                b.append(part.toString());
            }
            return b.toString();
        }
        return "";
    }

    @Override
    public int compare(GeoObject _o1, GeoObject _o2) {

        if(_o1.getType() > _o2.getType()){
            return 1;
        } else if(_o1.getType() < _o2.getType()){
            return -1;
        } else {
            return _o1.getId().compareTo(_o2.getId());
        }
    }

    @Override
    public int compareTo(GeoObject _o) {
        if(getType() > _o.getType()){
            return 1;
        } else if(getType() < _o.getType()){
            return -1;
        } else {
            return getId().compareTo(_o.getId());
        }
    }
}
