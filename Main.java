package sample;

import javafx.application.Application;


import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Pos;;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;


public class Main extends Application {

    private Scene scene;

    private Label label;

    private final Dimension2D DIMENSION = new Dimension2D(1000, 600);

    private BorderPane borderPane;  //root

    //MenuBar (TOP)

    private MenuBar menuBar;        //menuBar

    private Menu menuAction;

    private MenuItem menuItemOpenFiles;

    private MenuItem menuItemExit;

    private Menu menuDiashow;

    private MenuItem menuItemDiashow;

    private MenuItem menuItemDiashowDuration;

    private MenuItem menuItemPauseDiashow;

    private MenuItem menuItemStopDiashow;

    //for Thumbnaillist (LEFT)

    private ListView<ImageView> imageViewListView;

    //for CenterImage (CENTER)

    private static Pane centerPane;    //for shownImage

    //for Navigation (BOTTOM)

    private Button buttonLeftArrow; //previous Image

    private Button buttonRightArrow;//next Image

    private static ImageView imageView;

    private static List<Image> images = null;

    private static long diashowDuration;

    private static int indexOfCenterImage;

    //Image Task and Thread

    ImageTask imageTask;

    private Thread imageThread  = null;

    ContextMenu contextMenu;

    MenuItem contextmenuitem;

    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage primaryStage) throws Exception{

        // create MenuBar



        borderPane=new BorderPane(  );

        menuItemOpenFiles = new MenuItem("Open Files");

        menuItemExit = new MenuItem("Exit");

        menuItemDiashow = new MenuItem("Start Diashow");

        menuItemDiashowDuration = new MenuItem("Set Duration");

        menuItemPauseDiashow=new MenuItem( "pause" );

        menuItemStopDiashow=new MenuItem( "stop" );

        menuAction = new Menu("open");

        menuDiashow = new Menu("Diashow");

        Menu diashowmenu=new Menu( "Action Diashow" );

        diashowmenu.getItems().addAll( menuItemPauseDiashow,menuItemStopDiashow );

        menuAction.getItems().addAll(menuItemOpenFiles, menuItemExit);

        menuDiashow.getItems().addAll(menuItemDiashow, menuItemDiashowDuration);

        menuBar = new MenuBar();

        menuBar.getMenus().addAll(menuAction, menuDiashow,diashowmenu);

        borderPane.setTop(menuBar);

        // Left Side for Pictures

        imageViewListView = new ListView<ImageView>();

        imageViewListView.setPadding(new Insets(10));

        borderPane.setLeft(imageViewListView);

        // Bottom for Navigation and comment

        HBox hBoxButtons = new HBox(20);

        buttonLeftArrow = new Button("<-");

        buttonRightArrow = new Button("->");

        hBoxButtons.setAlignment( Pos.CENTER);

        label=new Label( "comment" );

        hBoxButtons.getChildren().addAll(buttonLeftArrow, buttonRightArrow,label);

        borderPane.setBottom(hBoxButtons);

        contextMenu=new ContextMenu(  );

        contextmenuitem =new MenuItem( "Comment" );

        contextMenu.getItems().add( contextmenuitem );

        // Center for chosen Picture

        centerPane = new Pane();

        borderPane.setCenter(centerPane);


        imageView=new ImageView(  );

        images = new ArrayList<Image>();

        diashowDuration = 2000;

        indexOfCenterImage = 0;




        menuItemOpenFiles.setOnAction( actionEvent ->  {


                FileChooser fileChooser = new FileChooser();

                List<File> files = fileChooser.showOpenMultipleDialog(primaryStage);



                try{

                    for (File file : files) {

                        String imagePath = file.toURI().toString();                                                             //get image Path as String

                        System.out.println("Loading Image from Path: " + imagePath);                                            //Message

                        Image image = new Image(imagePath, true);

                        getImages().add(image);

                    }

                } catch (NullPointerException e){return;}                                                                       //return if no image is load

                setIndexOfCenterImage(0);                                                                           //Center Image is First Image

                loadImages();
        } );

        menuItemExit.setOnAction( actionEvent ->  {

                System.exit( 0 );

        } );

        menuItemDiashow.setOnAction( actionEvent ->  {

                imageTask = new ImageTask();

                //Binding
                getImageView().fitWidthProperty().bind(centerPane.widthProperty());

                getImageView().fitHeightProperty().bind(centerPane.heightProperty());

                showDiashow();
        } );

        menuItemPauseDiashow.setOnAction( actionEvent ->  {
               if (imageThread.getState().equals(Thread.State.TERMINATED)) {
                   handleStartDiashow(new ActionEvent( ) );
               }


                 else {

                       interruptDiashow();


               }
       } );

        menuItemStopDiashow.setOnAction( actionEvent ->  {
                interruptDiashow();

                menuItemPauseDiashow.setText( "pause" );

        } );

        buttonLeftArrow.setOnAction( actionEvent ->  {
                if (getImages().size() <= 0) return;                                                                      //if no Images load yet

                int index = getIndexOfCenterImage();

                   if(index <= 0)                                                                                                 //first image get previous => last image

                    index = getImageViewListView().getItems().size();

                setIndexOfCenterImage(--index);

                setCenterImage(getActualImage());

        } );

        buttonRightArrow.setOnAction( actionEvent ->  {
                if (getImages().size() <= 0) return;                                                                      //if no Images load yet

                int index = getIndexOfCenterImage();

                if (index >= getImageViewListView().getItems().size()-1)                                              //last image get next => first image

                    index = -1;

                setIndexOfCenterImage(++index);

                setCenterImage(getActualImage());

        } );


      imageViewListView.setOnMouseClicked(event -> {

              setCenterImage(getImageViewListView().getSelectionModel().getSelectedItem().getImage());



      } );

      menuItemDiashowDuration.setOnAction( actionEvent ->  {
              TextInputDialog inputDialog = new TextInputDialog(Long.toString(getDiashowDuration()));

              inputDialog.setTitle("Diashow Duration");

              inputDialog.setHeaderText("");

              inputDialog.setContentText("Set the Duration of the Diashow (in millis):");



              Optional<String> result = inputDialog.showAndWait();

              if(!result.isPresent())
                  return;

              try{

                  setDiashowDuration(Long.parseLong(result.get()));

              } catch (NumberFormatException e) {

                  System.out.println( result.get() + " is Not a number" );


          }
      } );

        imageViewListView.setOnContextMenuRequested( new EventHandler<ContextMenuEvent>() {
            @Override
            public void handle(ContextMenuEvent contextMenuEvent) {
                contextMenu.show( imageViewListView, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY() );
            }
        } );

      contextmenuitem.setOnAction( new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent actionEvent) {

              TextInputDialog dialog = new TextInputDialog("Comment");

              dialog.setTitle("Comment");
              dialog.setContentText("Comment hier:");

              Optional<String> result = dialog.showAndWait();

            //  result.ifPresent( comment-> {
            //      label.setText(comment);
             // }
              result.ifPresent( new Consumer<String>() {
                  @Override
                  public void accept(String comment) {
                      label.setText(comment);
                  }
              } );
          //{

           //   };
            //  );
          }
      } );


        //set Scene

        scene = new Scene(borderPane, DIMENSION.getWidth(),DIMENSION.getHeight());
        primaryStage.setTitle("PhotoViewer Kamga");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void setDiashowDuration(long parseLong) {
        this.diashowDuration = diashowDuration;
    }


    private void handleStartDiashow(ActionEvent event){

        //creates and starts a new Thread

        imageThread = new Thread(imageTask);

        imageThread.start();

        getMenuItemPauseDiashow().setText("Pause");

    }

    public void showDiashow() {
         /*

         *  Start Diashow Thread -> via handleStartDiashow()

         * set Image (get number of Image in the List from indexOfCenterImage

         */

       handleStartDiashow( new ActionEvent(  ) );

        getMenuItemPauseDiashow().setText("Pause");

       getImageView().setImage(getActualImage());


    }

    private void interruptDiashow(){

        imageThread.interrupt();

        getMenuItemPauseDiashow().setText("restart");

    }


    public MenuItem getMenuItemPauseDiashow() {

        return menuItemPauseDiashow;

    }

        private void loadImages() {


        if (getImages().size() <= 0) return;

        getImageViewListView().getItems().clear();

        for (Image image: getImages()){

            ImageView imageView = new ImageView(image);

            imageView.setFitWidth(200);

            imageView.setFitHeight(200);                                                                                    //set max width and height

            imageView.setPreserveRatio(true);

            getImageViewListView().getItems().add(imageView);


        }

        setCenterImage(getActualImage());

    }

    private void setCenterImage(Image image){



        /*

         * for-Schleife: locate index of center Image

         * possible source of error:

         * if you load an image twice with the same URL the for-loop will only gets the index of the first one

         *

         * create new ImageView and set given Image

         * set fit width and height got from CenterPaneSize and preserveRatio(true)

         * Clear Pane and set new ImageView

         */

        for (int i = 0; i < getImages().size(); i++){

            if (!getImages().get(i).equals(image))continue;

            setIndexOfCenterImage(i);

            break;

        }



        imageView= new ImageView(image);

        imageView.fitWidthProperty().bind(getCenterPane().widthProperty());

        imageView.fitHeightProperty().bind(getCenterPane().heightProperty());

        imageView.setPreserveRatio(true);

        getCenterPane().getChildren().clear();

        getCenterPane().getChildren().add(imageView );

    }


        public static ImageView getImageView() {

            return imageView;

        }



    public static List<Image> getImages() {

        return images;

    }



    public static long getDiashowDuration() {

        return diashowDuration;

    }

     public static int getIndexOfCenterImage() {

        return indexOfCenterImage;

    }

    public static void setIndexOfCenterImage(int indexOfCenterImage1) {

        indexOfCenterImage = indexOfCenterImage1;

    }

    public static Image getActualImage(){

        return images.get(indexOfCenterImage);

    }

    public ListView<ImageView> getImageViewListView() {

        return imageViewListView;

    }

    public Pane getCenterPane() {

        return centerPane;

    }


}
