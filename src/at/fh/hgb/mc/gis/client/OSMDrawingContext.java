package at.fh.hgb.mc.gis.client;

import at.fh.hgb.mc.gis.feature.ADrawingContext;
import at.fh.hgb.mc.gis.feature.PresentationSchema;

import java.awt.*;

/**
 * This DrawingContext provides PresentationSchemas for multiple GeoObject types.
 */
public class OSMDrawingContext extends ADrawingContext {
    @Override
    protected void initSchemata() {
        //DEFAULT
        mContext.put(0,new PresentationSchema(Color.PINK,Color.PINK,1.0f));

        //LANDUSE
        mContext.put(5001,new PresentationSchema(Color.WHITE,Color.GREEN,1f)); //residential
        mContext.put(5002,new PresentationSchema(Color.WHITE,Color.YELLOW,1f)); //industrial
        mContext.put(5003,new PresentationSchema(Color.WHITE,Color.BLUE,1f)); //commercial
        mContext.put(5004,new PresentationSchema(Color.ORANGE,Color.GREEN,1f)); //forest
        mContext.put(5005,new PresentationSchema(Color.YELLOW,Color.GREEN,1f)); //grass
        mContext.put(5006,new PresentationSchema(Color.ORANGE,Color.GREEN,1f)); //meadow
        mContext.put(5018,new PresentationSchema(Color.darkGray,Color.GRAY,1f)); //railway

        //NATURAL
        mContext.put(6001,new PresentationSchema(Color.GREEN,Color.GREEN,1f)); //grassland
        mContext.put(6002,new PresentationSchema(Color.ORANGE,Color.ORANGE,1f)); //wood
        mContext.put(6005,new PresentationSchema(Color.BLUE,Color.BLUE,1f)); //water
        mContext.put(6014,new PresentationSchema(Color.GREEN, Color.GREEN,1f)); //trees

        for(int i = 9001; i < 9100; i++) {
            if(i == 9029) i = 9099;

            mContext.put(i, new PresentationSchema(Color.darkGray, Color.GRAY, 1.0f));
        }


        //WATERWAY
        mContext.put(2001,new PresentationSchema(Color.ORANGE,Color.BLUE,1f)); // river
        mContext.put(2002, new PresentationSchema(Color.GRAY, Color.BLUE,1f)); // canal
        mContext.put(2006, new PresentationSchema(Color.ORANGE, Color.BLUE,1f)); //should be a riverbank but the donau has this type, sooooooo... idk

        //HIGHWAY
        mContext.put(1010, new PresentationSchema(Color.WHITE, Color.darkGray,1f));  //motorway
        mContext.put(1011, new PresentationSchema(Color.WHITE, Color.darkGray,1f));  //motorway link
        mContext.put(1020, new PresentationSchema(Color.WHITE, Color.darkGray,1f));  //trunk
        mContext.put(1021, new PresentationSchema(Color.WHITE, Color.darkGray,1f));  //trunk link
        mContext.put(1030, new PresentationSchema(Color.WHITE, Color.darkGray,1f));  //primary
        mContext.put(1031, new PresentationSchema(Color.WHITE, Color.darkGray,1f));  //primary link

        //PLACE
        mContext.put(7070, new PresentationSchema(Color.DARK_GRAY,Color.GRAY,0.5f)); //main square
    }
}
