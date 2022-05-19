package at.fh.hgb.mc.gis.client;

import at.fh.hgb.mc.gis.feature.GeoObject;
import at.fh.hgb.mc.gis.server.*;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.*;


import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import static at.fh.hgb.mc.gis.client.GISView.*;
import static javafx.scene.input.KeyEvent.KEY_PRESSED;
import static javafx.scene.input.KeyEvent.KEY_RELEASED;
import static javafx.scene.input.ScrollEvent.*;

/**
 * This class provides a controller between GISView and GISModel in the implemented MVC-pattern.
 *
 * @see GISModel
 * @see GISView
 */
public class GISController {
    /**
     * Singleton instance of ActionHandler.
     */
    private ActionHandler mActionHandler;
    /**
     * Singleton instance of ChangeHandler.
     */
    private ChangeHandler mChangeHandler;
    /**
     * Singleton instance of MouseHandler.
     */
    private MouseHandler mMouseHandler;
    /**
     * Reference to the corresponding Model in the implemented MVC-pattern.
     */
    public GISModel mModel;
    /**
     * Singleton instance of ScrollHandler.
     */
    private ScrollHandler mScrollHandler;
    /**
     * Singleton instance of KeyHandler.
     */
    private KeyHandler mKeyHandler;
    /**
     * Reference to the corresponding View in the implemented MVC-pattern.
     */
    public GISView mView;
    /**
     * Flag indicating whether the user is currently dragging the mouse.
     */
    private boolean mDragStarted;
    /**
     * Point that stores the start coordinates of a drag. Primarily used for the mouse zoom function.
     */
    private Point2D.Double mZStart;
    /**
     * Point that stores the start coordinates of a drag. Primarily used for the mouse drag function.
     */
    private Point2D.Double mDStart;
    /**
     * List used as buffer for when the user holds down the control key and clicks at multiple points on the screen.
     */
    private ArrayList<Point2D.Double> mPoints;
    /**
     * Flag indicating whether the control key is currently being pressed.
     */
    private boolean mControlKeyDown = false;

    /**
     * Constructs a controller with a given model.
     *
     * @param _m Corresponding GISModel.
     */
    public GISController(GISModel _m, GISView _v) {
        mModel = _m;
        mView = _v;
    }

    /**
     * Provides singleton instance of KeyHandler.
     *
     * @return Instance of KeyHandler.
     */
    public KeyHandler getKeyHandler() {
        if (mKeyHandler == null) {
            mKeyHandler = new KeyHandler();
        }
        return mKeyHandler;
    }

    /**
     * Implementation of an EventHandler for KeyEvents.
     */
    public class KeyHandler implements EventHandler<KeyEvent> {
        /**
         * Constructs an empty KeyHandler.
         */
        private KeyHandler() {
        }

        /**
         * Invoked when an event of the type KeyEvent occurs.
         */
        @Override
        public void handle(KeyEvent keyEvent) {
            if (KEY_PRESSED == keyEvent.getEventType()) {
                if (keyEvent.getCode() == KeyCode.CONTROL) {
                    mControlKeyDown = true;
                } else if(keyEvent.getCode() == KeyCode.ENTER){
                    controlEnterKey();
                }
            } else if (KEY_RELEASED == keyEvent.getEventType()) {
                if (keyEvent.getCode() == KeyCode.CONTROL) {
                    mControlKeyDown = false;
                    if (mPoints != null) {
                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                        StringBuilder b = new StringBuilder();
                        for (Point2D point : mPoints) {
                            b.append("(").append(point.getX()).append(", ").append(point.getY()).append(") \n");
                        }
                        clipboard.setContents(new StringSelection(b.toString()), new StringSelection(b.toString()));
                        mPoints = null;
                    }
                }
            }
        }

        /**
         * Helper function dealing with reading from a TextField after the Enter key was pressed.
         * Also handles the result of the reading operation.
         */
        private void controlEnterKey(){
            TextField field = (TextField) mView.mScene.lookup("#ScaleField");
            String text = field.getText();
            if (text.contains(":")) {
                int index = text.indexOf(":");
                String scaleString = text.substring(index + 2);
                int scale = -1;
                try {
                    scale = Integer.parseInt(scaleString);
                } catch (Exception _exception) {
                    System.out.println("Error: Couldn't convert scale. Please make sure to use this format: '1 : XXXXX' with XXXXX being some integer.");
                    return;
                }
                if (scale > -1) {
                    mModel.zoomToScale(scale);
                    mModel.repaint();
                } else {
                    System.out.println("Error: No negative numbers allowed for scale.");
                }
            }
        }
    }

    /**
     * Provides singleton instance of ActionHandler.
     *
     * @return Instance of ActionHandler.
     */
    public ActionHandler getActionHandler() {
        if (mActionHandler == null) mActionHandler = new GISController.ActionHandler();

        return mActionHandler;
    }

    /**
     * Implementation of an EventHandler for ActionEvents.
     */
    public class ActionHandler implements EventHandler<ActionEvent> {

        /**
         * Constructs an empty ActionHandler.
         */
        private ActionHandler() {
        }

        /**
         * Invoked when an event of the type ActionEvent happens.
         */
        @Override
        public void handle(ActionEvent _e) {

            Object source = _e.getSource();
            if (source instanceof MenuItem) {
                MenuItem item = (MenuItem) source;
                switchMenuItems(item);

            } else if (source instanceof Button) {
                Button item = (Button) source;
                String id = item.getId();
                switch (id) {
                    case "Scale": {
                        TextField field = (TextField) mView.mScene.lookup("#ScaleField");
                        String text = field.getText();
                        if (text.contains(":")) {
                            int index = text.indexOf(":");
                            String scaleString = text.substring(index + 2);
                            int scale = -1;
                            try {
                                scale = Integer.parseInt(scaleString);
                            } catch (Exception _exception) {
                                System.out.println("Error: Couldn't convert scale. Please make sure to use this format: '1 : XXXXX' with XXXXX being some integer.");
                                return;
                            }
                            if (scale > -1) {
                                mModel.zoomToScale(scale);
                                mModel.repaint();
                            } else {
                                System.out.println("Error: No negative numbers allowed for scale.");
                            }

                        }
                    }
                    break;
                    case "POI": {
                        mModel.togglePOIS();
                        mModel.repaint();
                    }
                    break;
                    case "LoadData": {
                        mModel.loadData();
                        mModel.zoomToFit();
                        mModel.loadPOIData();
                        mModel.repaint();
                        mModel.updateScale();
                    }
                    break;
                    case "ZoomToFit": {
                        mModel.zoomToFit();
                        mModel.repaint();
                        mModel.updateScale();
                    }
                    break;
                    case "ZoomIn": {
                        mModel.zoom(1.3);
                        mModel.repaint();
                        mModel.updateScale();
                    }
                    break;
                    case "ZoomOut": {
                        mModel.zoom(1 / 1.3);
                        mModel.repaint();
                        mModel.updateScale();
                    }
                    break;
                    case "ScrollUp": {
                        mModel.scrollVertical(20);
                        mModel.repaint();
                    }
                    break;
                    case "ScrollDown": {
                        mModel.scrollVertical(-20);
                        mModel.repaint();
                    }
                    break;
                    case "ScrollLeft": {
                        mModel.scrollHorizontal(20);
                        mModel.repaint();
                    }
                    break;
                    case "ScrollRight": {
                        mModel.scrollHorizontal(-20);
                        mModel.repaint();
                    }
                    break;
                    case "RotateRight": {
                        mModel.rotate(-22.5 * (Math.PI / 180));
                        mModel.repaint();
                    }
                    break;
                    case "RotateLeft": {
                        mModel.rotate(22.5 * (Math.PI / 180));
                        mModel.repaint();
                    }
                    break;
                    case "Store": {
                        mModel.storeScreen();
                    }
                    break;
                    case "Sticky": {
                        mModel.stick();
                    }
                    break;
                    default:
                        break;
                } // switch id
            }
        }

        /**
         * Helper function for processing MenuItems.
         * Called by ActionHandler.handle().
         * @param item MenuItem that should be processed.
         */
        private void switchMenuItems(MenuItem item) {
            boolean serverChanged = false;
            switch (item.getId()) {
                case SERVER_MENU_DUMMY_GIS: {
                    if (!(mModel.mServer instanceof DummyGIS)) {
                        mModel.mServer = new DummyGIS();
                        serverChanged = true;
                    }
                }
                break;
                case SERVER_MENU_VWTG_3857: {
                    if (!(mModel.mServer instanceof Verwaltungsgrenzen3857Server)) {
                        mModel.mServer = new Verwaltungsgrenzen3857Server();
                        serverChanged = true;
                    }
                }
                break;

                case SERVER_MENU_VWTG_4326: {
                    if (!(mModel.mServer instanceof Verwaltungsgrenzen4326Server)) {
                        mModel.mServer = new Verwaltungsgrenzen4326Server();
                        serverChanged = true;
                    }
                }
                break;
                case SERVER_MENU_OSM_HAGENBERG: {
                    if (!(mModel.mServer instanceof OSMHagenberg)) {
                        mModel.mServer = new OSMHagenberg();
                        serverChanged = true;
                    }
                }
                break;

                case SERVER_MENU_OSM_LINZ: {
                    if (!(mModel.mServer instanceof OSMLinz)) {
                        mModel.mServer = new OSMLinz();
                        serverChanged = true;
                    }
                }
                break;
            }
            if (serverChanged) {
                mModel.loadData();
                mModel.zoomToFit();
                mModel.loadPOIData();
                mModel.repaint();
                mModel.updateScale();
            }
        }

    }

    /**
     * Provides singleton instance of ChangeHandler.
     *
     * @return Instance of ChangeHandler.
     */
    public ChangeHandler getChangeHandler() {
        if (mChangeHandler == null) mChangeHandler = new GISController.ChangeHandler();

        return mChangeHandler;
    }

    /**
     * Implementation of a ChangeListener. It gets notified whenever a specific value changes.
     */
    public class ChangeHandler implements ChangeListener<Number> {
        /**
         * Constructs an empty ChangeHandler.
         */
        private ChangeHandler() {
        }

        /**
         * Method that handles the notification, that a value that this object listens to, has changed.
         *
         * @param _observable Observable value this object listens to.
         * @param _oldValue   Previous value.
         * @param _newValue   Value the variable has changed to.
         */
        @Override
        public void changed(ObservableValue<? extends Number> _observable, Number _oldValue, Number _newValue) {
            if (_observable instanceof ReadOnlyDoubleProperty) {
                ReadOnlyDoubleProperty dProp = (ReadOnlyDoubleProperty) _observable;
                double val = dProp.doubleValue();
                String name = dProp.getName();
                if (name.equalsIgnoreCase("width")) {
                    mModel.setWidth((int) val);
                } else if (name.equalsIgnoreCase("height")) {
                    mModel.setHeight((int) val);
                }
            }
        }
    }

    /**
     * Provides singleton instance of MouseHandler.
     *
     * @return Instance of MouseHandler.
     */
    public MouseHandler getMouseHandler() {
        if (mMouseHandler == null) mMouseHandler = new MouseHandler();

        return mMouseHandler;
    }

    /**
     * Implementation of an EventHandler for MouseEvents.
     */
    public class MouseHandler implements EventHandler<MouseEvent> {
        /**
         * Constructs an empty MouseHandler.
         */
        private MouseHandler() {
        }

        /**
         * Invoked when an event of the type MouseEvent happens.
         */
        @Override
        public void handle(MouseEvent _e) {
            EventType<MouseEvent> type =
                    (EventType<MouseEvent>) _e.getEventType();
            switch (type.toString()) {
                case "MOUSE_PRESSED": {
                    mZStart = new java.awt.geom.Point2D.Double(_e.getX(), _e.getY());
                    mDStart = new java.awt.geom.Point2D.Double(_e.getX(), _e.getY());

                    if(mModel.mTransformationMatrix == null) return;
                    if(_e.getClickCount() == 2){
                        java.util.List<GeoObject> geos = mModel.initSelection(new Point((int) _e.getX(), (int) _e.getY()));
                        mView.createSelectionDialog(geos);
                    }

                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    if (mPoints == null) mPoints = new ArrayList<>();

                    if (mControlKeyDown) {
                        mPoints.add(mModel.mTransformationMatrix.invers().multiply(new Point2D.Double(_e.getX(), _e.getY())));
                    } else {
                        mPoints.add(mModel.mTransformationMatrix.invers().multiply(new Point2D.Double(_e.getX(), _e.getY())));
                        StringBuilder b = new StringBuilder();
                        for (Point2D point : mPoints) {
                            b.append("(").append(point.getX()).append(", ").append(point.getY()).append(") \n");
                        }
                        clipboard.setContents(new StringSelection(b.toString()), new StringSelection(b.toString()));
                        mPoints = null;
                    }

                }
                break;
                case "MOUSE_DRAGGED": {
                    switch (_e.getButton()) {
                        case PRIMARY: {
                            mView.mScene.setCursor(Cursor.CROSSHAIR);

                            mDragStarted = true;

                            double dx = _e.getX() - mZStart.x;
                            double dy = _e.getY() - mZStart.y;
                            if (Math.abs(dx) >= 40 && Math.abs(dy) >= 40) {
                                double x = mZStart.x;
                                double y = mZStart.y;
                                if (dx < 0) {
                                    x = mZStart.x + dx;
                                }
                                if (dy < 0) {
                                    y = mZStart.y + dy;
                                }
                                mView.drawXOR(new Rectangle2D.Double(x, y, Math.abs(dx), Math.abs(dy)));
                            }
                            break;
                        }
                        case SECONDARY: {
                            mView.mScene.setCursor(Cursor.CLOSED_HAND);
                            mDragStarted = true;
                            double dx = _e.getX() - mDStart.x;
                            double dy = _e.getY() - mDStart.y;
                            mView.translate(dx, dy);
                            mDStart = new Point2D.Double(_e.getX(), _e.getY());

                        }
                        break;
                    }

                }
                break;
                case "MOUSE_RELEASED": {
                    mView.mScene.setCursor(Cursor.DEFAULT);
                    if (mDragStarted) {
                        if (_e.getButton() == MouseButton.SECONDARY) {
                            double dx = _e.getX() - mZStart.x;
                            double dy = _e.getY() - mZStart.y;
                            mView.repaint();
                            mModel.scrollHorizontal((int) dx);
                            mModel.scrollVertical((int) dy);
                            mModel.repaint();
                        } else if (_e.getButton() == MouseButton.PRIMARY) {
                            mView.clearXOR();

                            double dx = _e.getX() - mZStart.x;
                            double dy = _e.getY() - mZStart.y;
                            if (Math.abs(dx) >= 40 && Math.abs(dy) >= 40) {
                                double x = mZStart.x;
                                double y = mZStart.y;
                                if (dx < 0) {
                                    x = mZStart.x + dx;
                                }
                                if (dy < 0) {
                                    y = mZStart.y + dy;
                                }
                                mModel.zoomRect(new Rectangle((int) x, (int) y, (int) Math.abs(dx), (int) Math.abs(dy)));
                                mModel.updateScale();
                                mModel.repaint();
                            }

                        }
                        mDragStarted = false;
                    }
                }
                break;
                default:
                    break;
            }
        }
    }

    /**
     * Provides singleton instance of ScrollHandler.
     *
     * @return Instance of ScrollHandler.
     */
    public ScrollHandler getScrollHandler() {
        if (mScrollHandler == null) {
            mScrollHandler = new ScrollHandler();
        }
        return mScrollHandler;
    }

    /**
     * Implementation of an EventHandler for ScrollEvents.
     */
    public class ScrollHandler implements EventHandler<ScrollEvent> {

        /**
         * Constructs an empty ScrollHandler.
         */
        private ScrollHandler() {

        }
        /**
         * Invoked when an event of the type ScrollEvent happens.
         */
        @Override
        public void handle(ScrollEvent scrollEvent) {
            if (scrollEvent.getEventType() == SCROLL) {
                double x = scrollEvent.getX();
                double y = scrollEvent.getY();
                Point p = new Point((int) x, (int) y);
                if (scrollEvent.getDeltaY() > 0) {
                    mModel.zoom(p, 1.3);
                } else {
                    mModel.zoom(p, 1 / 1.3);
                }
                mModel.repaint();
                mModel.updateScale();
            }
        }
    }


}
