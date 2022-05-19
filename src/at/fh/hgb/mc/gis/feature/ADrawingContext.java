package at.fh.hgb.mc.gis.feature;

import java.util.Hashtable;

/**
 * Abstract class providing partial implementation for a drawing context.
 * A drawing context provides PresentationSchemas for different GeoObject types.
 */
public abstract class ADrawingContext {
    /**
     * Hashtable storing all PresentationSchemas of this DrawingContext.
     */
    protected Hashtable<Integer, PresentationSchema> mContext = null;

    /**
     * Constructs a new ADrawingContext.
     */
    protected ADrawingContext() {
        mContext = new Hashtable<>();
        initSchemata();
    }

    /**
     * This method gets a PresentationSchema from the internal storage.
     *
     * @param _type Key to specify the PresentationSchema.
     * @return PresentationSchema pointed to by _type
     * or a default PresentationSchema if the type doesn't exist.
     */
    public PresentationSchema getSchema(int _type) {
        PresentationSchema schema = mContext.get(_type);
        if (schema == null) {
            schema = getDefaultSchema();
        }
        return schema;
    }

    /**
     * Method providing a default PresentationSchema.
     *
     * @return Default PresentationSchema
     */
    public PresentationSchema getDefaultSchema() {
        return mContext.get(0);
    }

    /**
     * This method initializes all PresentationSchemas provided by this DrawingContext.
     */
    protected abstract void initSchemata();
}

