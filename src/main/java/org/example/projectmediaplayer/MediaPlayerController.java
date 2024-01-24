package org.example.projectmediaplayer;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.media.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.*;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;


public class MediaPlayerController implements Initializable {


    @FXML
    private Button playButton, pauseButton, createPlaylistButton, saveNewPlaylistButton, saveChangesButton, removeMediaButton;
    @FXML
    private VBox addMediaBox;
    @FXML
    private TextField inputArtistTField, inputMediaNameTField, inputPathTField, inputPlaylistNameTField, searchBar;
    @FXML
    private Label mediaAddedLabel, playlistChangeLabel, editPlaylistMessageLabel, currentMediaLabel;
    @FXML
    private ListView mediaLibraryListView, playlistsListView, currentPlaylistListView, editPlaylistListView, artistsListView, searchBarListView;

    //region arrays and global variables
    private ArrayList<String> playlistLibrary = new ArrayList<>();
    private ArrayList<String> currentPlaylist = new ArrayList<>();
    private ArrayList<String> mediaFilePath = new ArrayList<>();
    private ArrayList<String> mediaNameList = new ArrayList<>();
    private ArrayList<String> artistNameList = new ArrayList<>();
    private int currentMediaIndex = 0;
    private String currentSelectedPlaylist;
    @FXML
    private MediaView mediaV;
    private MediaPlayer mp;
    private Media media;

    //endregion

    /**
     * method to establish connection to SQL server
     * @return
     * @throws SQLException
     */
    private Connection connectToSQL() throws SQLException {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=dbMediaPlayer";

        Properties properties = new Properties();
        properties.setProperty("user", "sa");
        properties.setProperty("password", "1234");
        properties.setProperty("encrypt", "false");
        return DriverManager.getConnection(url, properties);
    }

    //region importMethods
    private void importMediaFromSQL() throws SQLException { //imports all the media titles and filepaths from SQL to Arraylists and is called upen launching the program
        Connection connection = connectToSQL();
        Statement stmt = connection.createStatement();

        ResultSet medias = stmt.executeQuery("SELECT * FROM tblMedias");
        while (medias.next()){
            mediaFilePath.add(medias.getString(2).trim());
            mediaNameList.add(medias.getString(4).trim());
        }
        mediaLibraryListView.getItems().addAll(mediaNameList);

    }

    private void importPlaylistsFromSQL() throws SQLException { //imports the different playlists saved in the SQL database, also called when launching the program

        Connection connection = connectToSQL();
        Statement stmt = connection.createStatement();

        ResultSet playlists = stmt.executeQuery("SELECT fldPlaylistName FROM tblPlaylists");
        while (playlists.next()){
            playlistLibrary.add(playlists.getString(1).trim());
        }
        playlistsListView.getItems().addAll(playlistLibrary);
    }
    private void importArtistsFromSQL() throws SQLException{ //imports the list of different artists
        Connection connection = connectToSQL();
        Statement stmt = connection.createStatement();

        ResultSet artistsList = stmt.executeQuery("SELECT DISTINCT fldArtist FROM tblMedias");
        while(artistsList.next()){
            artistNameList.add(artistsList.getString(1).trim());
        }
        artistsListView.getItems().addAll(artistNameList);
        artistsListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                currentPlaylistListView.getItems().clear();
                try {
                    importSpecificArtistMedia(t1);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
    private void importSpecificPlaylist(String playlistName) throws SQLException { //method to get a specific playlist from the database.
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
    private void importSpecificArtistMedia(String artistName) throws SQLException{ //method to get all media with a specific artist
        Connection connection = connectToSQL();
        Statement stmt = connection.createStatement();

        currentPlaylist.clear();
        ResultSet artistPlaylist = stmt.executeQuery("SELECT fldMediaTitle FROM tblMedias WHERE fldArtist='"+artistName+"'");
        while(artistPlaylist.next()){
            currentPlaylist.add(artistPlaylist.getString(1).trim());
        }
        currentPlaylistListView.getItems().addAll(currentPlaylist);
        getSelectedMedia(currentPlaylist.get(0));
    }
    //endregion

    /**
     * This is the main method used when switching between different media. Based on the input of a mediaTitle it finds the filepath and loads the media to the mediaPlayer.
     * Because this method is used everytime a media is played I included the 'setOnEndOfMedia' here to automatically select the next media on the current playlist.
     * @param mediaTitle
     * @throws SQLException
     */
    private void getSelectedMedia(String mediaTitle) throws SQLException {
        int mediaPathIndex = mediaNameList.indexOf(mediaTitle);
        String mediaPath = new File(mediaFilePath.get(mediaPathIndex)).getAbsolutePath();
        media = new Media(new File(mediaPath).toURI().toString());
        mp = new MediaPlayer(media);
        mediaV.setMediaPlayer(mp);
        currentMediaLabel.setText(mediaTitle);
        mp.setOnEndOfMedia(() -> {
            try {
                onNextButtonClick();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
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
        mp.play();
        playButton.setVisible(false);
        pauseButton.setVisible(true);
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
        mp.play();
        playButton.setVisible(false);
        pauseButton.setVisible(true);
    }
    @FXML
    private void onClearQueueButtonClick(){
        currentPlaylist.clear();
        currentPlaylistListView.getItems().clear();
    }
    //endregion
    //region add new media methods

    /**
     * Method to check if the artist name is already in the database, used when added new media to the library.
     * @param artistName this is user input when adding new media.
     * @return the return is an int but could also have been a boolean. The result is the same.
     * @throws SQLException
     */
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
    private void onAddToLibraryButtonClick(){ //the 'add new media-box' is hidden behind the MediaView, and can be toggled on with this method
        if(!addMediaBox.isVisible()) {
            addMediaBox.setVisible(true);
        }
        else{
            addMediaBox.setVisible(false);
        }
    }

    /**
     * Method which adds a new media to the database for further use. It takes three inputs, checks if the artistName already exist because the field in the database table for artistName is a primary key hence must be unigue.
     * If the artist does not exist in the database, the new artist is also added together with the new media. After adding the media, the mediaLibrary is refreshed.
     * @throws SQLException
     */
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
        mediaLibraryListView.getItems().clear();
        importMediaFromSQL();
    }
    @FXML
    private void onCloseAddToLibraryButtonClick(){
        addMediaBox.setVisible(false);
    }
    //endregion
    //region manage playlists window
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

    /**
     * Method similar to the 'add new media' method but this adds a new playlist to the database.
     * @throws SQLException
     */
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
    private void onEditPlaylistButtonClick() throws SQLException { //This changes the view in the 'manage playlists' window to accommodate buttons for editing current playlists.
        editPlaylistMessageLabel.setVisible(true);
        editPlaylistListView.setVisible(true);
        editPlaylistListView.getItems().addAll(currentPlaylist);
        saveChangesButton.setVisible(true);
        inputPlaylistNameTField.setVisible(true);
        removeMediaButton.setVisible(true);
        editPlaylistListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                currentMediaIndex = editPlaylistListView.getSelectionModel().getSelectedIndex();
            }
        });
    }
    @FXML
    private void onRemoveMediaButtonClick(){ //Removes the selected media from a playlist, but needs to be saved afterward to have an effect in the database.
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
    /*
    Method used to save changes when editing a playlist by deleting the playlist and creating a new.
     */
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
        removeMediaButton.setVisible(false);
    }

    @FXML
    private void onDeletePlaylistButtonClick() throws SQLException { //Deletes the selected playlist from the database. Cannot be reversed.
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
    /*
    As I did not want to hide yet another window behind the MediaView I created a separate fxml file for managing playlists (creating, editing and deleting). This method opens the window.
    The same controller class is used in both windows as many of the elements where used again. A separate controller class would in the long run be a better option.
    */
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
    private void onBackToMediaplayerButtonClick(ActionEvent event){ //This method "closes" the manage playlist window and opens the mediaplayer again.
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
    //endregion

    //region searchbar and functionality
    /*
    This method makes it possible to search for media, artists and playlists int the search bar. It shows the results of the current typed letters as a dynamic ListView under the search bar.
     */
    @FXML
    private void searchList(){
        if(!searchBar.getText().isEmpty()) {
            String searchWords = searchBar.getText();
            searchBarListView.getItems().clear();
            searchBarListView.setVisible(true);
            mediaV.toBack();
            ArrayList<String> getSearchResults = new ArrayList<>();
            String artist = "Artist: ";
            String playlist = "Playlist: ";
            for (String results : mediaNameList) {
                if (results.toLowerCase().contains(searchWords.toLowerCase())) {
                    getSearchResults.add(results);
                }
            }
            for (String results : artistNameList){
                if (results.toLowerCase().contains(searchWords.toLowerCase())){
                    getSearchResults.add(artist+results);
                }
            }
            for(String results : playlistLibrary){
                if (results.toLowerCase().contains(searchWords.toLowerCase())){
                    getSearchResults.add(playlist+results);
                }
            }
            searchBarListView.setPrefHeight(getSearchResults.size() * 23.9);
            searchBarListView.getItems().addAll(getSearchResults);
        }
        else{
            searchBarListView.setVisible(false);
            mediaV.toFront();
        }
        /*
        Here is the function to click on a media, artist or playlist in the search ListView and load it to the queue.
         */
        searchBarListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue,String s, String t1) {
                searchBarListView.setVisible(false);
                if(t1!=null && t1.startsWith("Artist")){
                    try {
                        currentPlaylist.clear();
                        currentPlaylistListView.getItems().clear();
                        importSpecificArtistMedia(t1.substring(8));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
                else if(t1!=null && t1.startsWith("Playlist")){
                    try {
                        importSpecificPlaylist(t1.substring(10));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
                else if (t1!=null){
                    try {
                        currentPlaylist.clear();
                        currentPlaylistListView.getItems().clear();
                        currentPlaylist.add(t1);
                        currentPlaylistListView.getItems().addAll(t1);
                        getSelectedMedia(t1);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }
    //endregion
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
            importMediaFromSQL(); //Loads the media from SQL to the media library in the mediaplayer
            importPlaylistsFromSQL(); //Loads the playlists from SQL.
            if(artistsListView!=null) { //This validation is necessary as I do not have an artistsListView in both fxml files.
                importArtistsFromSQL(); //Loads the artists from SQL
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        /*
        Clicking on a media in the mediaLibraryListView adds the media to the queue.
         */
        mediaLibraryListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                currentPlaylist.add(t1);
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
        /*
        Clicking a playlist name in the playlistsListView will clear the current queue and load the playlist to the queue
         */
        playlistsListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue,String s, String t1) {
                if(inputPlaylistNameTField != null) { //Validation because the textfield is not part of the mediaplayer fxml.
                    if (t1 != null) {
                        inputPlaylistNameTField.setText(t1);
                    }
                }
                currentPlaylistListView.getItems().clear();
                if(editPlaylistListView != null) {
                    editPlaylistListView.getItems().clear();
                }
                try {
                    if(t1 != null) { //As I have had problems with the parameters t1 and s I could fix it with these validations.
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
    }
}