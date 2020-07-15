package sample;

import static javafx.application.Platform.runLater;

public class ImageTask implements Runnable{


    @Override
    public void run() {
        /*

         * try-catch because of Thread.sleep

         * while (true), because the Diashow Can run forever

         * sleep for X millis, got from diashowDuration

         * set next ImageView:

         *   -> set index ++

         *   -> look if Last Image

         *   -> call updateImage

         */

        try {

            while (true) {

                Thread.sleep( Main.getDiashowDuration() );
                Main.setIndexOfCenterImage( (Main.getIndexOfCenterImage() + 1) % Main.getImages().size() );

                updateImage();

            }

        } catch (InterruptedException e) {

           e.printStackTrace();

        }


    }

    private void updateImage() {

        runLater( new Runnable() {

            @Override
            public void run() {

                Main.getImageView().setImage( Main.getActualImage() );

            }

        } );

    }


}
