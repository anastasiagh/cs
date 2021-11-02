package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

public class EnforcePoliciesController implements Initializable {
    public static Stage stage;
    public static JSONArray selectedPolicies;
    public static JSONArray actualPolicies;
    public static ObservableList<String> observableListSelected = FXCollections.observableArrayList();
    public static ObservableList<String> observableListActual = FXCollections.observableArrayList();
    public static ObservableList<String> observableListExpected = FXCollections.observableArrayList();
    public ViewController view = new ViewController();
    @FXML
    public ListView<String> selectedView = new ListView<String>();
    @FXML
    public ListView<String> actualView = new ListView<String>();
    @FXML
    public ListView<String> expectedView = new ListView<String>();

    public void initialize(URL url, ResourceBundle rb) {
    }

    public void setSelectedListView() {
        selectedView.setItems(observableListSelected);
        view.setColor(selectedView);
    }

    public void setActualListView() {
        actualView.setItems(observableListActual);
        actualView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(item);
                        if (item != null) {
                            setStyle("-fx-control-inner-background: derive(yellow, 50%);");
                        }
                    }
                };
            }
        });
    }

    public void setExpectedView() {
        expectedView.setItems(observableListExpected);
        expectedView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(item);
                        if (item != null) {
                            setStyle("-fx-control-inner-background: derive(palegreen, 50%);");
                        }
                    }
                };
            }
        });
    }

    public void downloadPolicies(ActionEvent event) throws IOException {
        actualPolicies = new JSONArray (selectedPolicies.toList());
        view.selectedPolicies = selectedPolicies;
        observableListSelected = view.scan(selectedPolicies);
        List<String> actualList = new ArrayList<>();
        List<String> expectedList = new ArrayList<>();
        for (int i = 0; i < selectedPolicies.length(); i++) {
            JSONObject selectedItem = selectedPolicies.getJSONObject(i);
            expectedList.add(selectedItem.get(" value_data ").toString());
            actualList.add(selectedItem.get("actual_data").toString());
        }
        observableListExpected = FXCollections.observableArrayList(expectedList);
        observableListActual = FXCollections.observableArrayList(actualList);
        setSelectedListView();
        setActualListView();
        setExpectedView();

    }

    public void enforcePolicies(ActionEvent event) throws IOException {
        StringBuilder cmdOutput = new StringBuilder();
        Map<String, Object> map = new HashMap<String, Object>();
        for (int i = 0; i < selectedPolicies.length(); i++) {
            map = selectedPolicies.getJSONObject(i).toMap();
            if (map.get(" reg_key ") != null && map.get(" reg_item ") != null) {
                ProcessBuilder builder = new ProcessBuilder(
                        "cmd.exe", "/c", "reg add" + map.get(" reg_key ").toString() + " /v " + map.get(" reg_item ").toString() + " /d " + map.get(" value_data ") + " /f ");
                builder.redirectErrorStream(true);
                Process p = builder.start();
                BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                cmdOutput = new StringBuilder();
                while (true) {
                    line = r.readLine();
                    if (line == null) {
                        break;
                    }
                    cmdOutput.append(line);
                }

            }
        }
        downloadPolicies(event);
    }

    public void rollbackPolicies(ActionEvent event) throws IOException {
        StringBuilder cmdOutput = new StringBuilder();
        Map<String, Object> map = new HashMap<String, Object>();
        for (int i = 0; i < actualPolicies.length(); i++) {
            map = actualPolicies.getJSONObject(i).toMap();
            if (map.get(" reg_key ") != null && map.get(" reg_item ") != null) {
                ProcessBuilder builder = new ProcessBuilder(
                        "cmd.exe", "/c", "reg add" + map.get(" reg_key ").toString() + " /v " + map.get(" reg_item ").toString() + " /d " + map.get("actual_data") + " /f ");
                builder.redirectErrorStream(true);
                Process p = builder.start();
                BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                cmdOutput = new StringBuilder();
                while (true) {
                    line = r.readLine();
                    if (line == null) {
                        break;
                    }
                    cmdOutput.append(line);
                }

            }
        }
        downloadPolicies(event);
    }

}