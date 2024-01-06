package org.example.projectmediaplayer;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.media.*;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;


public class MediaPlayerController implements Initializable {


    @FXML
    private Button playButton;
    @FXML
    private Button pauseButton;
    @FXML
    private VBox addMediaBox;
    @FXML
    private TextField inputArtistTField;
    @FXML
    private TextField inputMediaNameTField;
    @FXML
    private TextField inputPathTField;
    @FXML
    private Button addMediaButton;

    @FXML
    private void onPlayButtonClick() {
        mp.play();
        playButton.setVisible(false);
        pauseButton.setVisible(true);
    }
    @FXML
    private void onStopButtonClick(){
        mp.stop();
        playButton.setVisible(true);
        pauseButton.setVisible(false);
    }
    @FXML
    private void onPauseButtonClick(){
        mp.pause();
        playButton.setVisible(true);
        pauseButton.setVisible(false);
    }
    @FXML
    private void onPreviousButtonClick(){
        //media = path for previous song

    }
    @FXML
    private void onAddToLibraryButtonClick(){
        if(!addMediaBox.isVisible()) {
            addMediaBox.setVisible(true);
        }
        else{
            addMediaBox.setVisible(false);
        }
    }
    private void onAddMediaButtonClick(){
        String artistName = inputArtistTField.getText();
        String mediaName = inputMediaNameTField.getText();
        String path = inputPathTField.getText();
    }

    @FXML
    private MediaView mediaV;
    private MediaPlayer mp;
    private Media media;

    /**
     * This method is invoked automatically in the beginning. Used for initializing, loading data etc.
     *
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources){
        // Build the path to the location of the media file
        String path = new File("C:\\Java\\ProjectMediaPlayer\\src\\main\\java\\Medias\\20240104_110107.mp4").getAbsolutePath();  //video
        //String path = new File("c:\\tmp\\SampleAudio_0.4mb.mp3").getAbsolutePath();   //sound
        // Create new Media object (the actual media content)
        media = new Media(new File(path).toURI().toString());
        // Create new MediaPlayer and attach the media to be played
        mp = new MediaPlayer(media);
        //
        mediaV.setMediaPlayer(mp);
        // mp.setAutoPlay(true);
        // If autoplay is turned of the method play(), stop(), pause() etc controls how/when medias are played
        mp.setAutoPlay(false);

    }

}