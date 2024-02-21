package org.example.demo1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("mainpage.fxml"));
            AnchorPane root = fxmlLoader.load();
            Scene scene = new Scene(root, 640, 400);
            stage.setTitle("MyAnimeList!");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();

            Button enterButton = (Button) root.lookup("#Enter");
            Button leaveButton = (Button) root.lookup("#leave");

            enterButton.setOnAction(event -> {
                try {
                    FXMLLoader animePageLoader = new FXMLLoader(MainApp.class.getResource("animepage.fxml"));
                    AnchorPane animePageRoot = animePageLoader.load();
                    Scene animePageScene = new Scene(animePageRoot, 640, 400);

                    Button settingsButton = (Button) animePageRoot.lookup("#settings");
                    settingsButton.setOnAction(e -> {
                        try {
                            FXMLLoader editPageLoader = new FXMLLoader(MainApp.class.getResource("editpage.fxml"));
                            AnchorPane editPageRoot = editPageLoader.load();
                            Scene editPageScene = new Scene(editPageRoot, 640, 400);

                            ListView<String> listView = (ListView<String>) editPageRoot.lookup("#listView");
                            TextField researchField = (TextField) editPageRoot.lookup("#researchField");
                            displayDataInListView(listView);

                            listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                                if (newValue != null) {
                                    displaySelectedAnimeInfo(newValue, editPageRoot, listView);
                                }
                            });

                            Button deleteButton = (Button) editPageRoot.lookup("#Delete");
                            deleteButton.setOnAction(deleteEvent -> {
                                String selectedItem = listView.getSelectionModel().getSelectedItem();
                                if (selectedItem != null) {
                                    deleteData(selectedItem);
                                    displayDataInListView(listView);
                                } else {
                                    System.out.println("Please select an item to delete.");
                                }
                            });

                            Button cancelButton = (Button) editPageRoot.lookup("#Cancel");
                            cancelButton.setOnAction(cancelEvent -> {
                                stage.setScene(animePageScene);
                            });

                            researchField.textProperty().addListener((observable, oldValue, newValue) -> {
                                if (newValue != null) {
                                    searchAndDisplay(editPageRoot, listView, newValue);
                                }
                            });

                            Button saveButton = (Button) editPageRoot.lookup("#Save/Update");

                            if (saveButton == null) {
                                System.err.println("Button 'Save/Update' not found in editpage.fxml");
                            } else {
                                saveButton.setOnAction(saveEvent -> {
                                    if (allFieldsFilled(editPageRoot)) {
                                        saveOrUpdateData(editPageRoot, listView);
                                    } else {
                                        System.out.println("Please fill in all fields before saving.");
                                    }
                                });
                            }

                            stage.setScene(editPageScene);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    });

                    stage.setScene(animePageScene);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            leaveButton.setOnAction(event -> stage.close());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean allFieldsFilled(AnchorPane editPageRoot) {
        TextField titleField = (TextField) editPageRoot.lookup("#titleField");
        TextField directorField = (TextField) editPageRoot.lookup("#directorField");
        TextField seasonField = (TextField) editPageRoot.lookup("#seasonField");
        TextField episodeField = (TextField) editPageRoot.lookup("#episodeField");
        TextField ratingField = (TextField) editPageRoot.lookup("#ratingField");
        TextField descriptionField = (TextField) editPageRoot.lookup("#descriptionField");
        TextField linkField = (TextField) editPageRoot.lookup("#linkField");
        TextField reviewField = (TextField) editPageRoot.lookup("#reviewField");

        return !titleField.getText().isEmpty() &&
                !directorField.getText().isEmpty() &&
                !seasonField.getText().isEmpty() &&
                !episodeField.getText().isEmpty() &&
                !ratingField.getText().isEmpty() &&
                !descriptionField.getText().isEmpty() &&
                !linkField.getText().isEmpty() &&
                !reviewField.getText().isEmpty();
    }

    private void saveOrUpdateData(AnchorPane editPageRoot, ListView<String> listView) {
        try {
            String titleToUpdate = null;
            String updatedLine = "";

            BufferedReader reader = new BufferedReader(new FileReader("data.csv"));
            StringBuilder fileContent = new StringBuilder();

            TextField titleField = (TextField) editPageRoot.lookup("#titleField");
            String title = titleField.getText();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                String currentTitle = fields[0].trim();
                if (currentTitle.equalsIgnoreCase(title)) {
                    titleToUpdate = currentTitle;
                    updatedLine = title + "," +
                            ((TextField) editPageRoot.lookup("#directorField")).getText() + "," +
                            ((TextField) editPageRoot.lookup("#seasonField")).getText() + "," +
                            ((TextField) editPageRoot.lookup("#episodeField")).getText() + "," +
                            ((TextField) editPageRoot.lookup("#ratingField")).getText() + "," +
                            ((TextField) editPageRoot.lookup("#descriptionField")).getText() + "," +
                            ((TextField) editPageRoot.lookup("#linkField")).getText() + "," +
                            ((TextField) editPageRoot.lookup("#reviewField")).getText();
                } else {
                    fileContent.append(line).append("\n");
                }
            }
            reader.close();

            if (titleToUpdate != null) {
                fileContent.append(updatedLine).append("\n");
                BufferedWriter writer = new BufferedWriter(new FileWriter("data.csv"));
                writer.write(fileContent.toString());
                writer.close();
                System.out.println("Data updated successfully!");
            } else {
                BufferedWriter writer = new BufferedWriter(new FileWriter("data.csv", true));
                String newLine = title + "," +
                        ((TextField) editPageRoot.lookup("#directorField")).getText() + "," +
                        ((TextField) editPageRoot.lookup("#seasonField")).getText() + "," +
                        ((TextField) editPageRoot.lookup("#episodeField")).getText() + "," +
                        ((TextField) editPageRoot.lookup("#ratingField")).getText() + "," +
                        ((TextField) editPageRoot.lookup("#descriptionField")).getText() + "," +
                        ((TextField) editPageRoot.lookup("#linkField")).getText() + "," +
                        ((TextField) editPageRoot.lookup("#reviewField")).getText() + "\n";
                writer.write(newLine);
                writer.close();
                System.out.println("New data saved to CSV successfully!");
            }

            displayDataInListView(listView);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void deleteData(String selectedItem) {
        try {
            File inputFile = new File("data.csv");
            File tempFile = new File("temp.csv");

            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

            String lineToRemove = selectedItem + ",";
            String currentLine;

            while ((currentLine = reader.readLine()) != null) {
                String trimmedLine = currentLine.trim();
                if (trimmedLine.startsWith(lineToRemove)) continue;
                writer.write(currentLine + System.getProperty("line.separator"));
            }
            writer.close();
            reader.close();
            boolean successful = tempFile.renameTo(inputFile);
            if (!successful) {
                throw new IOException("Could not rename temp file to overwrite the original.");
            }
            System.out.println("Data deleted successfully!");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void displaySelectedAnimeInfo(String selectedItem, AnchorPane editPageRoot, ListView<String> listView) {
        try (BufferedReader reader = new BufferedReader(new FileReader("data.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                String title = fields[0].trim();
                if (title.equalsIgnoreCase(selectedItem)) {
                    TextField titleField = (TextField) editPageRoot.lookup("#titleField");
                    titleField.setText(title);

                    if (fields.length >= 8) {
                        TextField directorField = (TextField) editPageRoot.lookup("#directorField");
                        directorField.setText(fields[1].trim());

                        TextField seasonField = (TextField) editPageRoot.lookup("#seasonField");
                        seasonField.setText(fields[2].trim());

                        TextField episodeField = (TextField) editPageRoot.lookup("#episodeField");
                        episodeField.setText(fields[3].trim());

                        TextField ratingField = (TextField) editPageRoot.lookup("#ratingField");
                        ratingField.setText(fields[4].trim());

                        TextField descriptionField = (TextField) editPageRoot.lookup("#descriptionField");
                        descriptionField.setText(fields[5].trim());

                        TextField linkField = (TextField) editPageRoot.lookup("#linkField");
                        linkField.setText(fields[6].trim());

                        TextField reviewField = (TextField) editPageRoot.lookup("#reviewField");
                        reviewField.setText(fields[7].trim());
                    }

                    listView.getSelectionModel().select(selectedItem);

                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayDataInListView(ListView<String> listView) {
        ObservableList<String> data = FXCollections.observableArrayList();
        try (BufferedReader reader = new BufferedReader(new FileReader("data.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                data.add(fields[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        listView.setItems(data);
    }

    private void searchAndDisplay(AnchorPane editPageRoot, ListView<String> listView, String searchTerm) {
        ObservableList<String> searchResults = FXCollections.observableArrayList();
        try (BufferedReader reader = new BufferedReader(new FileReader("data.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                String title = fields[0].trim();
                if (title.toLowerCase().contains(searchTerm.toLowerCase())) {
                    searchResults.add(title);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        listView.setItems(searchResults);
    }

    public static void main(String[] args) {
        launch();
    }
}
