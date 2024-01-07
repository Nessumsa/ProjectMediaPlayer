package org.example.projectmediaplayer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.media.*;

import java.sql.*;
import java.util.*;

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
    private Label mediaAddedLabel;
    @FXML
    private TextField inputPlaylistNameTField;
    @FXML
    private ListView mediaLibraryListView;

    private Connection connectToSQL() throws SQLException {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=dbMediaPlayer";

        Properties properties = new Properties();
        properties.setProperty("user", "sa");
        properties.setProperty("password", "1234");
        properties.setProperty("encrypt", "false");
        return DriverManager.getConnection(url, properties);
    }

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
    @FXML
    private void onAddMediaButtonClick() throws SQLException {
        String artistName = inputArtistTField.getText();
        String mediaName = inputMediaNameTField.getText();
        String path = inputPathTField.getText();

        Connection connection = connectToSQL();
        Statement stmt = connection.createStatement();
        String addArtistToSQL = "INSERT INTO tblArtists VALUES ('"+artistName+"')";
        stmt.executeUpdate(addArtistToSQL);
        String addMediaToSQL = "INSERT INTO tblMedias VALUES ('"+path+"','"+artistName+"','"+mediaName+"')";
        stmt.executeUpdate(addMediaToSQL);

        mediaAddedLabel.setText("Media successfully added!");
        inputArtistTField.clear();
        inputMediaNameTField.clear();
        inputPathTField.clear();
    }
    @FXML
    private void onCloseAddToLibraryButtonClick(){
        addMediaBox.setVisible(false);
    }
    @FXML
    private void onCreatePlaylistButtonClick() throws SQLException {

        String playlistName = inputPlaylistNameTField.getText();
        Connection connection = connectToSQL();

        Statement stmt = connection.createStatement();
        String addPlaylistToSQL = "INSERT INTO tblPlaylists VALUES ('"+playlistName+"')";
        stmt.executeUpdate(addPlaylistToSQL);
        /*TBD
        Code for added the selected media
         */
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