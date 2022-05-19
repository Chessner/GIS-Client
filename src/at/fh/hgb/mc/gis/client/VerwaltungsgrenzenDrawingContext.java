package at.fh.hgb.mc.gis.client;

import at.fh.hgb.mc.gis.feature.ADrawingContext;
import at.fh.hgb.mc.gis.feature.PresentationSchema;

import java.awt.*;

/**
 * This DrawingContext provides PresentationSchemas for multiple GeoObject types.
 */
public class VerwaltungsgrenzenDrawingContext extends ADrawingContext {
    @Override
    protected void initSchemata() {
        mContext.put(8002,new PresentationSchema(Color.WHITE,Color.GRAY,1.0f));
        mContext.put(0,new PresentationSchema(Color.PINK,Color.PINK,1.0f));
    }
}
