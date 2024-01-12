package org.example.projectmediaplayer;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.media.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.sql.*;
import java.util.*;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;


public class MediaPlayerController implements Initializable {


    @FXML
    private Button playButton, pauseButton, createPlaylistButton, saveNewPlaylistButton, saveChangesButton;
    @FXML
    private VBox addMediaBox;
    @FXML
    private TextField inputArtistTField, inputMediaNameTField, inputPathTField, inputPlaylistNameTField;
    @FXML
    private Label mediaAddedLabel, playlistChangeLabel, editPlaylistMessageLabel;
    @FXML
    private ListView mediaLibraryListView, playlistsListView, currentPlaylistListView, editPlaylistListView;

    private ArrayList<String> playlistLibrary = new ArrayList<>();
    private ArrayList<String> currentPlaylist = new ArrayList<>();
    private ArrayList<String> mediaFilePath = new ArrayList<>();
    private ArrayList<String> mediaLibrary = new ArrayList<>();
    private ArrayList<String> mediaNameList = new ArrayList<>();
    private ArrayList<String> artistNameList = new ArrayList<>();
    private int currentMediaIndex = 0;
    private String currentSelectedPlaylist;
    private String currentSelectedMedia;

    private Connection connectToSQL() throws SQLException {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=dbMediaPlayer";

        Properties properties = new Properties();
        properties.setProperty("user", "sa");
        properties.setProperty("password", "1234");
        properties.setProperty("encrypt", "false");
        return DriverManager.getConnection(url, properties);
    }

    //region importMethods
    private void importMediaFromSQL() throws SQLException {
        Connection connection = connectToSQL();
        Statement stmt = connection.createStatement();

        ResultSet medias = stmt.executeQuery("SELECT * FROM tblMedias");
        while (medias.next()){
            mediaFilePath.add(medias.getString(2).trim());
            mediaNameList.add(medias.getString(4).trim());
            artistNameList.add(medias.getString(3).trim());
        }
        mediaLibraryListView.getItems().addAll(mediaNameList);

    }
    @FXML
    private void importPlaylistsFromSQL() throws SQLException {

        Connection connection = connectToSQL();
        Statement stmt = connection.createStatement();

        ResultSet playlists = stmt.executeQuery("SELECT fldPlaylistName FROM tblPlaylists");
        while (playlists.next()){
            playlistLibrary.add(playlists.getString(1).trim());
        }
        playlistsListView.getItems().addAll(playlistLibrary);
    }
    private void importSpecificPlaylist(String playlistName) throws SQLException {
        Connection connection = connectToSQL();
        Statement stmt = connection.createStatement();

        currentPlaylist.clear();
        ResultSet specificPlaylist = stmt.executeQuery("SELECT fldMediaTitle FROM tblMedias RIGHT JOIN tblPlaylistMedia ON tblMedias.fldMediaID = tblPlaylistMedia.fldMediaID WHERE fldPlaylistName='"+playlistName+"'");
        while (specificPlaylist.next()){
            currentPlaylist.add(specificPlaylist.getString(1).trim());
        }
        currentPlaylistListView.getItems().addAll(currentPlaylist);
        if(editPlaylistListView != null && editPlaylistListView.isVisible()){
            editPlaylistListView.getItems().addAll(currentPlaylist);
        }
    }
    //endregion
    private void getSelectedMedia(String mediaTitle) throws SQLException {
        int mediaPathIndex = mediaNameList.indexOf(mediaTitle);
        String mediaPath = new File(mediaFilePath.get(mediaPathIndex)).getAbsolutePath();
        media = new Media(new File(mediaPath).toURI().toString());
        mp = new MediaPlayer(media);
        mediaV.setMediaPlayer(mp);
    }

    //region control buttons
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
    private void onPreviousButtonClick() throws SQLException {
        if(currentMediaIndex > 0){
            getSelectedMedia(currentPlaylist.get(currentMediaIndex-1));
            currentMediaIndex--;
        }
        else {
            getSelectedMedia(currentPlaylist.get(currentPlaylist.size()-1));
            currentMediaIndex = currentPlaylist.size()-1;
        }
    }
    @FXML
    private void onNextButtonClick() throws SQLException {
        if(currentMediaIndex < currentPlaylist.size()-1){
            getSelectedMedia(currentPlaylist.get(currentMediaIndex+1));
            currentMediaIndex++;
        }
        else {
            getSelectedMedia(currentPlaylist.get(0));
            currentMediaIndex = 0;
        }
    }
    @FXML
    private void onClearQueueButtonClick(){
        currentPlaylistListView.getItems().clear();
    }
    //endregion
    //region add new media methods
    private int checkArtistExist(String artistName) throws SQLException {
        Connection connection = connectToSQL();
        Statement stmt = connection.createStatement();
        ResultSet numOfartists = stmt.executeQuery("SELECT COUNT(*) FROM tblArtists WHERE fldArtist='"+artistName+"'");
        int num = -1;
        while (numOfartists.next()){
            num = numOfartists.getInt(1);
        }
        return num;
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

        if(checkArtistExist(artistName) == 0){
            String addArtistToSQL = "INSERT INTO tblArtists VALUES ('"+artistName+"')";
            stmt.executeUpdate(addArtistToSQL);
        }

        String addMediaToSQL = "INSERT INTO tblMedias VALUES ('"+path+"','"+artistName+"','"+mediaName+"')";
        stmt.executeUpdate(addMediaToSQL);

        mediaAddedLabel.setText("Media successfully added!");
        inputArtistTField.clear();
        inputMediaNameTField.clear();
        inputPathTField.clear();
        mediaLibrary.clear();
        mediaLibraryListView.getItems().clear();
        importMediaFromSQL();
    }
    @FXML
    private void onCloseAddToLibraryButtonClick(){
        addMediaBox.setVisible(false);
    }
    //endregion
    @FXML
    private void onCreatePlaylistButtonClick(){
        createPlaylistButton.setVisible(false);
        saveNewPlaylistButton.setVisible(true);
        inputPlaylistNameTField.clear();
        inputPlaylistNameTField.setVisible(true);
    }
    @FXML
    private void onClearButtonClick(){
        currentPlaylist.clear();
        currentPlaylistListView.getItems().clear();
    }
    @FXML
    private void onSaveNewPlaylistButtonClick() throws SQLException {
        String newPlaylistName = inputPlaylistNameTField.getText();
        ArrayList<Integer> mediaIDs = new ArrayList<Integer>();

        Connection connection = connectToSQL();
        Statement stmt = connection.createStatement();

        stmt.executeUpdate("INSERT INTO tblPlaylists VALUES ('"+newPlaylistName+"')");

        for (int i = 0; i < currentPlaylist.size(); i++) {
            ResultSet getMediaID = stmt.executeQuery("SELECT fldMediaID FROM tblMedias WHERE fldMediaTitle='"+currentPlaylist.get(i)+"'");
            while(getMediaID.next()) {
                mediaIDs.add(getMediaID.getInt(1));
            }
        }
        for (int i = 0; i < mediaIDs.size(); i++) {
            stmt.executeUpdate("INSERT INTO tblPlaylistMedia VALUES ('"+mediaIDs.get(i)+"','"+newPlaylistName+"')");
        }
        currentPlaylist.clear();
        currentPlaylistListView.getItems().clear();
        playlistLibrary.clear();
        playlistsListView.getItems().clear();
        playlistChangeLabel.setText(newPlaylistName+" has been saved to the database");
        playlistChangeLabel.setVisible(true);
        createPlaylistButton.setVisible(true);
        saveNewPlaylistButton.setVisible(false);
        inputPlaylistNameTField.setVisible(false);
        inputPlaylistNameTField.clear();
        importPlaylistsFromSQL();
        playlistChangeLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                playlistChangeLabel.setVisible(false);
            }
        });
    }
    @FXML
    private void onEditPlaylistButtonClick() throws SQLException {
        editPlaylistMessageLabel.setVisible(true);
        editPlaylistListView.setVisible(true);
        editPlaylistListView.getItems().addAll(currentPlaylist);
        System.out.println(currentPlaylist);
        saveChangesButton.setVisible(true);
        inputPlaylistNameTField.setVisible(true);
        editPlaylistListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                currentMediaIndex = editPlaylistListView.getSelectionModel().getSelectedIndex();
            }
        });
    }
    @FXML
    private void onRemoveMediaButtonClick(){
        currentPlaylist.remove(currentMediaIndex);
        playlistChangeLabel.setText("Media removed from playlist");
        playlistChangeLabel.setVisible(true);

        System.out.println(currentPlaylist);
        playlistChangeLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {

                editPlaylistListView.getItems().clear();
                playlistChangeLabel.setVisible(false);
                try {
                    onEditPlaylistButtonClick();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
    @FXML
    private void onSaveChangesButtonClick() throws SQLException {
        Connection connection = connectToSQL();
        Statement stmt = connection.createStatement();

        stmt.executeUpdate("DELETE FROM tblPlaylists WHERE fldPlaylistName='"+currentSelectedPlaylist+"'");
        playlistLibrary.clear();
        playlistsListView.getItems().clear();
        onSaveNewPlaylistButtonClick();
        editPlaylistMessageLabel.setVisible(false);
        editPlaylistListView.setVisible(false);
    }

    @FXML
    private void onDeletePlaylistButtonClick() throws SQLException {
        Connection connection = connectToSQL();
        Statement stmt = connection.createStatement();

        stmt.executeUpdate("DELETE FROM tblPlaylists WHERE fldPlaylistName='"+currentSelectedPlaylist+"'");

        playlistChangeLabel.setText(currentSelectedPlaylist+" has been deleted from the database");
        playlistChangeLabel.setVisible(true);

        playlistChangeLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                playlistChangeLabel.setVisible(false);
                playlistLibrary.clear();
                playlistsListView.getItems().clear();
            }
        });

    }
    //endregion
    @FXML
    private MediaView mediaV;
    private MediaPlayer mp;
    private Media media;

    @FXML
    private void onManagePlaylistButtonClick(ActionEvent event){

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MediaPlayerApplication.class.getResource("managePlaylists.fxml"));
            Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
            Scene scene2 = new Scene(fxmlLoader.load(), 700, 500);
            stage.setTitle("Manage playlists");
            stage.setScene(scene2);
            stage.show();
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
    @FXML
    private void onBackToMediaplayerButtonClick(ActionEvent event){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MediaPlayerApplication.class.getResource("mediaplayer.fxml"));
            Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(fxmlLoader.load(), 900, 500);
            stage.setTitle("Mediaplayer");
            stage.setScene(scene);
            stage.show();
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * This method is invoked automatically in the beginning. Used for initializing, loading data etc.
     *
     * @param location
     * @param resources
     */

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Build the path to the location of the media file
        String path = new File("C:\\Java\\ProjectMediaPlayer\\src\\main\\java\\Medias\\Welcome.mp4").getAbsolutePath();  //video
        //String path = new File("c:\\tmp\\SampleAudio_0.4mb.mp3").getAbsolutePath();   //sound
        // Create new Media object (the actual media content)
        media = new Media(new File(path).toURI().toString());
        // Create new MediaPlayer and attach the media to be played
        mp = new MediaPlayer(media);
        mediaV.setMediaPlayer(mp);
        mp.setAutoPlay(true);

        try {
            importMediaFromSQL();
            importPlaylistsFromSQL();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        mediaLibraryListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                //currentPlaylist.clear();
                currentPlaylist.add(t1);
                //currentPlaylistListView.getItems().clear();
                currentPlaylistListView.getItems().addAll(t1);
                playButton.setVisible(true);
                try {
                    getSelectedMedia(t1);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                if(editPlaylistListView != null) {
                    if (editPlaylistListView.isVisible()) {
                        editPlaylistListView.getItems().addAll(t1);
                    }
                }
            }
        });
        playlistsListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue,String s, String t1) {
                if(inputPlaylistNameTField != null) {
                    if (t1 != null) {
                        inputPlaylistNameTField.setText(t1);
                    }
                }
                currentPlaylistListView.getItems().clear();
                if(editPlaylistListView != null) {
                    editPlaylistListView.getItems().clear();
                }
                try {
                    if(t1 != null) {
                        importSpecificPlaylist(t1);
                        currentSelectedPlaylist = t1;
                        getSelectedMedia(currentPlaylist.get(0));
                    }
                    else{
                        importPlaylistsFromSQL();
                    }
                } catch (SQLException e) {
                    throw new RuntimeException();
                }
            }
        });

        currentPlaylistListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                try {
                    getSelectedMedia(t1);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });


    }
}