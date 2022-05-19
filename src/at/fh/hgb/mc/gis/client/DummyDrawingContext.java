package at.fh.hgb.mc.gis.client;

import at.fh.hgb.mc.gis.feature.ADrawingContext;
import at.fh.hgb.mc.gis.feature.PresentationSchema;

import java.awt.*;

/**
 * This DrawingContext provides PresentationSchemas for multiple GeoObject types.
 */
public class DummyDrawingContext extends ADrawingContext {
    @Override
    protected void initSchemata() {
        mContext.put(233,new PresentationSchema(Color.BLACK,Color.WHITE,1.0f));
        mContext.put(931,new PresentationSchema(Color.BLACK,Color.RED,1.0f));
        mContext.put(932,new PresentationSchema(Color.RED,Color.ORANGE,1.0f));
        mContext.put(1101,new PresentationSchema(Color.GREEN,Color.MAGENTA,1.0f));
        mContext.put(0,new PresentationSchema(Color.PINK,Color.PINK,1.0f));
    }
}
