package at.fh.hgb.mc.gis.client;

import at.fh.hgb.mc.gis.feature.GeoObject;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;

import java.util.Collections;
import java.util.List;

/**
 * This class provides an MVC Controller for the selection dialog window.
 * The selection dialog window is created when the user double-clicks and shows all
 * GeoObjects which contain the point at which the user clicked.
 */
public class DialogController {
    /**
     * List containing all GeoObjects displayed in the selection dialog window.
     */
    private List<GeoObject> mData;
    /**
     * Reference to the view displaying the GeoObjects.
     */
    private GISView mView;
    /**
     * Singleton instance of DialogActionHandler.
     */
    private DialogActionHandler mDialogActionHandler;

    /**
     * Constructs a controller with the given model and view.
     */
    public DialogController(List<GeoObject> _mModel, GISView _view){
        mData = _mModel;
        mView = _view;
    }

    /**
     * Provides access to the singleton instance of the DialogActionHandler.
     * @return Instance of DialogActionHandler.
     */
    public DialogActionHandler getDialogActionHandler(){
        if(mDialogActionHandler == null){
            mDialogActionHandler = new DialogActionHandler();
        }
        return mDialogActionHandler;
    }

    /**
     * Implementation of an EventHandler for ActionEvents.
     */
    public class DialogActionHandler implements EventHandler<ActionEvent>{
        /**
         * Constructs an empty DialogActionHandler.
         */
        private DialogActionHandler(){

        }

        /**
         * Invoked when an event of the type ActionEvent occurs.
         */
        @Override
        public void handle(ActionEvent _actionEvent) {
            Object source = _actionEvent.getSource();
            if (source instanceof MenuItem) {
                MenuItem item = (MenuItem) source;
                String itemId = item.getId();
                int stringIndex = itemId.indexOf(",");
                String typeS = itemId.substring(0,stringIndex);
                int type = Integer.parseInt(typeS);
                String id = itemId.substring(stringIndex+1);

                GeoObject searcher = new GeoObject(id,type,null,null,null);
                int listIndex = Collections.binarySearch(mData,searcher);
                GeoObject itemObject = mData.get(listIndex);

                mView.displayDialogItem(itemObject);
            } else if(source instanceof Button){
                Button button = (Button) source;
                switch(button.getId()){
                    case "okButton":{
                        mView.closeSelectionDialog();
                        break;
                    }
                }
            }
        }
    }
}
