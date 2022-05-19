package at.fh.hgb.mc.gis.client;

import at.fh.hgb.mc.gis.feature.*;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.w3c.dom.css.Rect;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * This class provides an entrypoint into a javafx program. It represents the View part of the implemented MVC-pattern.
 * Implements the IDataObserver to get updates about the data to display.
 *
 * @see GISController
 * @see GISModel
 * @see IDataObserver
 */
public class GISView extends Application implements IDataObserver {
    /**
     * Unique id to identify and find the canvas in the mScene.
     */
    private static final String CANVAS_ID = "this_is_a_unique_id";
    /**
     * Unique id to identify and find the overlay canvas in the mScene.
     */
    private static final String OVERLAY_ID = "OVERLAY";
    /**
     * Unique id to identify and find the DummyGIS item from the server menu.
     */
    protected static final String SERVER_MENU_DUMMY_GIS = "SERVER_MENU_DUMMY_GIS";
    /**
     * Unique id to identify and find the VWTG_3857 item from the server menu.
     */
    protected static final String SERVER_MENU_VWTG_3857 = "SERVER_MENU_VWTG_3857";
    /**
     * Unique id to identify and find the VWTG_4326 item from the server menu.
     */
    protected static final String SERVER_MENU_VWTG_4326 = "SERVER_MENU_VWTG_4326";
    /**
     * Unique id to identify and find the OSM_HAGENBERG item from the server menu.
     */
    protected static final String SERVER_MENU_OSM_HAGENBERG = "SERVER_MENU_OSM_HAGENBERG";
    /**
     * Unique id to identify and find the OSM_LINZ item from the server menu.
     */
    protected static final String SERVER_MENU_OSM_LINZ = "SERVER_MENU_OSM_LINZ";
    /**
     * Reference to the corresponding controller from the MVC-pattern.
     */
    private GISController mController;
    private GISModel mModel;
    /**
     * Image containing every polygon that should be drawn to the canvas.
     */
    private BufferedImage mImage;
    /**
     * Primary scene in the javafx program.
     */
    protected Scene mScene;

    private Scene mDialogScene;
    /**
     * Flag indicating whether the displayed image is currently being dragged across the screen.
     */
    private boolean mStartDrag;

    private DialogController mDialogController;

    public static void main(String[] _argv) {
        launch(_argv);
    }

    /**
     * Test method for Matrix.zoomToFit(). Outputs result to the console.
     */
    public static void testZTF() {
        Rectangle world = new Rectangle(47944531, 608091485, 234500, 213463);
        Rectangle window = new Rectangle(0, 0, 640, 480);

        Matrix transMatrix = Matrix.zoomToFit(world, window, true);
        System.out.println("transformation matrix: \n" + transMatrix);
        Rectangle projection = transMatrix.multiply(world);
        System.out.println("projection: \n" + projection.toString());
        Matrix backTransMatrix = Matrix.zoomToFit(projection, world, true);
        System.out.println("backwards transformation matrix: \n" + backTransMatrix);
        Rectangle backProjection = backTransMatrix.multiply(projection);
        System.out.println("backwards projection: \n" + backProjection.toString());
    }

    /**
     * Entry point method for javafx where the graphics are set up.
     *
     * @param _stage Primary stage provided by javafx.
     * @throws Exception
     */
    @Override
    public void start(Stage _stage) throws Exception {

        BorderPane root = new BorderPane();
        mScene = new Scene(root, 700, 566);

        // Construct and add FlowPane (Buttons)
        FlowPane buttonPane = new FlowPane();
        buttonPane.setStyle("-fx-background-color: blue; -fx-padding: 5;");
        root.setBottom(buttonPane);

        // Construct and add StackPane (Canvas)
        StackPane canvasPane = new StackPane();
        canvasPane.setMinSize(0, 0);
        canvasPane.setStyle("-fx-background-color: red;");
        root.setCenter(canvasPane);

        // Construct and add Canvas to StackPane for drawing
        Canvas canvas = new Canvas();
        canvas.setId(CANVAS_ID);
        canvas.widthProperty().bind(canvasPane.widthProperty());
        canvas.heightProperty().bind(canvasPane.heightProperty());

        // Construct and add Overlay Canvas to StackPane
        Canvas overlay = new Canvas();
        overlay.setId(OVERLAY_ID);
        overlay.widthProperty().bind(canvasPane.widthProperty());
        overlay.heightProperty().bind(canvasPane.heightProperty());
        canvasPane.getChildren().addAll(canvas, overlay);


        // Create Menu A
        Menu menuA = new Menu("A");
        MenuItem menuAItem01 = new MenuItem("Test");
        menuAItem01.setOnAction(mController.getActionHandler());
        menuA.getItems().addAll(menuAItem01);

        // Create Menu B
        Menu serverMenu = new Menu("Server");
        RadioMenuItem serverMenuDummyGIS = new RadioMenuItem("DummyGIS");
        serverMenuDummyGIS.setId(SERVER_MENU_DUMMY_GIS);
        serverMenuDummyGIS.setOnAction(mController.getActionHandler());

        RadioMenuItem serverMenuVWTG3857 = new RadioMenuItem("Vwtg 3857");
        serverMenuVWTG3857.setId(SERVER_MENU_VWTG_3857);
        serverMenuVWTG3857.setOnAction(mController.getActionHandler());

        RadioMenuItem serverMenuVWTG4326 = new RadioMenuItem("Vwtg 4326");
        serverMenuVWTG4326.setId(SERVER_MENU_VWTG_4326);
        serverMenuVWTG4326.setOnAction(mController.getActionHandler());

        RadioMenuItem serverMenuOSMHagenberg = new RadioMenuItem("OSM Hagenberg");
        serverMenuOSMHagenberg.setId(SERVER_MENU_OSM_HAGENBERG);
        serverMenuOSMHagenberg.setOnAction(mController.getActionHandler());

        RadioMenuItem serverMenuOSMLinz = new RadioMenuItem("OSM Linz");
        serverMenuOSMLinz.setId(SERVER_MENU_OSM_LINZ);
        serverMenuOSMLinz.setOnAction(mController.getActionHandler());

        serverMenu.getItems().addAll(serverMenuDummyGIS, serverMenuVWTG3857, serverMenuVWTG4326, serverMenuOSMHagenberg, serverMenuOSMLinz);

        ToggleGroup tg = new ToggleGroup();
        serverMenuDummyGIS.setToggleGroup(tg);
        serverMenuVWTG3857.setToggleGroup(tg);
        serverMenuVWTG4326.setToggleGroup(tg);
        serverMenuOSMLinz.setToggleGroup(tg);
        serverMenuOSMHagenberg.setToggleGroup(tg);
        serverMenuOSMLinz.setSelected(true);

        // Create MenuBar and add menus
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(menuA, serverMenu);

        // Add menuBar to BorderPane (root)
        root.setTop(menuBar);

        //Create Button containers
        HBox hBox = new HBox(10);
        buttonPane.getChildren().addAll(hBox);
        GridPane gridPane = new GridPane();

        //Create buttons
        Button loadData = new Button("Load");
        loadData.setId("LoadData");

        Button zoomToFit = new Button("ZoomToFit");
        zoomToFit.setId("ZoomToFit");

        Button zoomIn = new Button("+");
        zoomIn.setId("ZoomIn");

        Button zoomOut = new Button("-");
        zoomOut.setId("ZoomOut");

        Button scrollUp = new Button("N");
        scrollUp.setId("ScrollUp");
        GridPane.setConstraints(scrollUp, 1, 0);

        Button scrollDown = new Button("S");
        scrollDown.setId("ScrollDown");
        GridPane.setConstraints(scrollDown, 1, 1);

        Button scrollLeft = new Button("W");
        scrollLeft.setId("ScrollLeft");
        GridPane.setConstraints(scrollLeft, 0, 1);

        Button scrollRight = new Button("E");
        scrollRight.setId("ScrollRight");
        GridPane.setConstraints(scrollRight, 2, 1);

        Button rotateRight = new Button("RR");
        rotateRight.setId("RotateRight");

        Button rotateLeft = new Button("RL");
        rotateLeft.setId("RotateLeft");

        Button poiButton = new Button("POI");
        poiButton.setId("POI");

        Button storeButton = new Button("Store!");
        storeButton.setId("Store");

        Button stickyButton = new Button("Sticky!");
        stickyButton.setId("Sticky");

        //Add buttons
        gridPane.getChildren().addAll(scrollDown, scrollLeft, scrollRight, scrollUp);
        hBox.getChildren().addAll(loadData, zoomToFit, zoomIn, zoomOut, gridPane, rotateLeft, rotateRight, poiButton, storeButton, stickyButton);

        // Create Text field
        TextField scaleField = new TextField("1 : unknown");
        scaleField.setId("ScaleField");
        scaleField.setOnKeyPressed(mController.getKeyHandler());
        hBox.getChildren().addAll(scaleField);

        // Add listeners
        // Add EventListener for buttons
        loadData.setOnAction(mController.getActionHandler());
        zoomToFit.setOnAction(mController.getActionHandler());
        zoomIn.setOnAction(mController.getActionHandler());
        zoomOut.setOnAction(mController.getActionHandler());
        scrollUp.setOnAction(mController.getActionHandler());
        scrollDown.setOnAction(mController.getActionHandler());
        scrollLeft.setOnAction(mController.getActionHandler());
        scrollRight.setOnAction(mController.getActionHandler());
        rotateLeft.setOnAction(mController.getActionHandler());
        rotateRight.setOnAction(mController.getActionHandler());
        poiButton.setOnAction(mController.getActionHandler());
        storeButton.setOnAction(mController.getActionHandler());
        stickyButton.setOnAction(mController.getActionHandler());

        // Add ChangeHandler for resizing
        canvasPane.widthProperty().addListener(mController.getChangeHandler());
        canvasPane.heightProperty().addListener(mController.getChangeHandler());
        // Add MouseHandler
        overlay.setOnMouseReleased(mController.getMouseHandler());
        overlay.setOnMousePressed(mController.getMouseHandler());
        overlay.setOnMouseDragged(mController.getMouseHandler());
        overlay.setOnScroll(mController.getScrollHandler());
        overlay.setOnScrollStarted(mController.getScrollHandler());
        overlay.setOnScrollFinished(mController.getScrollHandler());
        mScene.setOnKeyPressed(mController.getKeyHandler());
        mScene.setOnKeyReleased(mController.getKeyHandler());
        mScene.setOnKeyTyped(mController.getKeyHandler());


        _stage.setTitle("GIS-Client");
        _stage.setScene(mScene);
        _stage.show();
    }

    /**
     * Init method for the javafx program.
     */
    @Override
    public void init() {
        mModel = new GISModel();
        mController = new GISController(mModel, this);
        mModel.addMapObserver(this);
    }


    @Override
    public void update(BufferedImage _image) {
        mImage = _image;
        repaint();
    }

    @Override
    public void updateScale(int _scale) {
        TextField field = (TextField) mScene.lookup("#ScaleField");
        field.setText("1 : " + _scale);
    }

    /**
     * Method used for painting mImage to the canvas, indicated by CANVAS_ID, in the mScene.
     */
    public void repaint() {
        if(mImage == null) return;

        Canvas c = (Canvas) mScene.lookup("#" + CANVAS_ID);
        GraphicsContext gc = c.getGraphicsContext2D();
        if (mStartDrag) {
            gc.restore();
            mStartDrag = false;
        }
        WritableImage writable = SwingFXUtils.toFXImage(mImage, null);
        gc.clearRect(0, 0, c.getWidth(), c.getHeight());
        gc.drawImage(writable, 0, 0);
    }

    /**
     * This method draws the given Rectangle on the overlay canvas.
     *
     * @param _rect Rectangle to be drawn.
     */
    public void drawXOR(Rectangle2D _rect) {
        Canvas c = (Canvas) mScene.lookup("#" + OVERLAY_ID);
        GraphicsContext g = c.getGraphicsContext2D();
        g.clearRect(0, 0, mScene.getWidth(), mScene.getHeight());
        g.setStroke(Color.BLUE);
        g.strokeRect(_rect.getMinX(), _rect.getMinY(),
                _rect.getWidth(), _rect.getHeight());
    }

    /**
     * This method clears the current overlay canvas.
     */
    public void clearXOR() {
        Canvas c = (Canvas) mScene.lookup("#" + OVERLAY_ID);
        GraphicsContext g = c.getGraphicsContext2D();
        g.clearRect(0, 0, mScene.getWidth(), mScene.getHeight());
    }

    /**
     * Method used for moving the canvas in different directions.
     *
     * @param _dx Amount by which the canvas should be moved in x direction.
     * @param _dy Amount by which the canvas should be moved in y direction.
     */
    public void translate(double _dx, double _dy) {
        if(mImage == null) return;

        Canvas c = (Canvas) mScene.lookup("#" + CANVAS_ID);
        GraphicsContext gc = c.getGraphicsContext2D();
        // clean up bitblt errors …
        int width = (int) c.getWidth();
        int height = (int) c.getHeight();
        int delta = 2;
        gc.clearRect(0, delta, width, height); // top
        gc.clearRect(0, height - delta, width, height); // bottom

        if (!mStartDrag) {
            mStartDrag = true;
            gc.save();
        }

        gc.translate(_dx, _dy);
        WritableImage writable = SwingFXUtils.toFXImage(mImage, null);
        gc.drawImage(writable, 0, 0);
    }

    public void createSelectionDialog(java.util.List<GeoObject> _data) {
        mDialogController = new DialogController(_data, this);
        Stage stage = new Stage();
        Parent root;
        try {
            root = FXMLLoader.load(getClass().getResource("selection_dialog.fxml"));
        } catch (IOException _e) {
            _e.printStackTrace();
            return;
        }

        mDialogScene = new Scene(root, 440, 440);

        stage.setTitle("Selection Dialog");
        stage.setScene(mDialogScene);
        stage.show();

        ArrayList<MenuButton> menuButtons = new ArrayList<>();
        int currentType = -1;
        MenuButton currentButton = null;
        _data.sort(new GeoObject());
        for(int i = 0; i < _data.size(); i++){
            if(_data.get(i).getType() != currentType){
                currentButton = new MenuButton(Integer.toString(_data.get(i).getType()));
                menuButtons.add(currentButton);
                currentType = _data.get(i).getType();
            }
            MenuItem item = new MenuItem(_data.get(i).getId());
            item.setId(_data.get(i).getType() + "," + _data.get(i).getId());
            item.setOnAction(mDialogController.getDialogActionHandler());

            if(currentButton != null) currentButton.getItems().addAll(item);
        }


        ListView listView = (ListView) mDialogScene.lookup("#listView");
        ObservableList<MenuButton> list = FXCollections.observableArrayList(menuButtons);
        listView.setItems(list);
    }

    public void displayDialogItem(GeoObject _item){
        Text idText = (Text) mDialogScene.lookup("#idText");
        idText.setText(_item.getId());
        Text typeText = (Text) mDialogScene.lookup("#typeText");
        typeText.setText(String.valueOf(_item.getType()));
        Text geomTypeText = (Text) mDialogScene.lookup("#geomTypeText");
        geomTypeText.setText(_item.getGeomType());
        TextArea textArea = (TextArea) mDialogScene.lookup("#textArea");
        textArea.setText(_item.getAttr());

        Canvas canvas = (Canvas) mDialogScene.lookup("#canvas");

        Rectangle world = _item.getBounds();
        Rectangle window = new Rectangle((int) canvas.getWidth(), (int) canvas.getHeight());
        Matrix transformationMatrix = Matrix.zoomToFit(world,window,true);

        BufferedImage image = new BufferedImage((int)canvas.getWidth(),(int)canvas.getHeight(),BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) image.getGraphics();

        ADrawingContext context = mModel.mDrawingContext;
        PresentationSchema schema = context.getSchema(_item.getType());
        schema.paint(g2d,_item,transformationMatrix);

        GraphicsContext gc = canvas.getGraphicsContext2D();
        WritableImage writable = SwingFXUtils.toFXImage(image, null);
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.drawImage(writable, 0, 0);
    }
}
