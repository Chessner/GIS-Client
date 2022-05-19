package at.fh.hgb.mc.gis.client;

import at.fh.hgb.mc.gis.feature.GeoLine;
import at.fh.hgb.mc.gis.feature.GeoObject;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;

import java.util.Collections;
import java.util.List;

public class DialogController {
    private List<GeoObject> mModel;
    private GISView mView;
    private DialogActionHandler mDialogActionHandler;

    public DialogController(List<GeoObject> _mModel, GISView _view){
        mModel = _mModel;
        mView = _view;
    }

    public DialogActionHandler getDialogActionHandler(){
        if(mDialogActionHandler == null){
            mDialogActionHandler = new DialogActionHandler();
        }
        return mDialogActionHandler;
    }

    public class DialogActionHandler implements EventHandler<ActionEvent>{
        private DialogActionHandler(){

        }

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
                int listIndex = Collections.binarySearch(mModel,searcher);
                GeoObject itemObject = mModel.get(listIndex);

                mView.displayDialogItem(itemObject);
            }
        }
    }
}
