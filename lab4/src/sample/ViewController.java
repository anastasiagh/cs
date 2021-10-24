package sample;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import static java.util.regex.Pattern.DOTALL;

public class ViewController implements Initializable {
    private static final Logger LOG = Logger.getLogger(ViewController.class.getName());
    @FXML
    private static JSONArray jsonArr;
    @FXML
    Label selectedLbl = new Label();
    ArrayList<Integer> indexesArray = new ArrayList<>();
    JSONArray finalAudit = new JSONArray();
    JSONArray selectedPolicies = new JSONArray();
    String textFuture = "";
    ObservableList<String> observableList = FXCollections.observableArrayList();

    @FXML
    private TextField textListViewField;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private TextFileReader reader = new TextFileReader();
    @FXML
    private ListView<String> listView = new ListView<String>();
    @FXML
    private List<Integer> selectedIndices;
    private String initialFileName;

    public void setListView(List<String> list) {
        if (listView.getItems() != null)
            listView.getItems().clear();
        listView.refresh();

        observableList = FXCollections.observableArrayList(list);
        listView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(item);
                        setStyle("-fx-control-inner-background: white;");
                    }
                };
            }
        });
        listView.setItems(observableList);

        MultipleSelectionModel<String> langsSelectionModel = listView.getSelectionModel();

        langsSelectionModel.setSelectionMode(SelectionMode.MULTIPLE);

        langsSelectionModel.getSelectedIndices().addListener((ListChangeListener<? super Number>) (observableValue) -> {
            selectedIndices = (List<Integer>) observableValue.getList();
        });

        filter();
    }

    public void initialize(URL url, ResourceBundle rb) {
    }

    public void filter() {
        FilteredList<String> filteredData = new FilteredList<>(observableList, s -> true);
        textListViewField.textProperty().addListener(obs -> {
            String filter = textListViewField.getText();
            if (filter == null || filter.length() == 0) {
                indexesArray.clear();
                filteredData.setPredicate(s -> true);

                listView.setItems(observableList);
            } else {
                indexesArray.clear();
                for (int index = 0; index < observableList.size() - 1; index++) {
                    if (observableList.get(index).contains(filter)) {
                        indexesArray.add(index);
                    }
                }
                filteredData.setPredicate(s -> s.contains(filter));
                ObservableList<String> currentList = FXCollections.observableArrayList(filteredData);
                listView.setItems(currentList);
            }
        });
    }

    public void saveFile() {
        List<String> descriptionsList = new ArrayList<>();
        for (int i = 0; i < jsonArr.length(); i++) {
            JSONObject jsonObject = jsonArr.getJSONObject(i);
            descriptionsList.add(jsonObject.get(" description ").toString());
        }

        setListView(descriptionsList);
    }

    public void exportItems(ActionEvent event) throws IOException {
        event.consume();
        if (indexesArray.size() > 0) {
            selectedIndices.forEach(index -> {
                finalAudit.put(jsonArr.get(indexesArray.get(index)));
            });
        } else {
            selectedIndices.forEach(index -> {
                finalAudit.put(jsonArr.get(index));
            });
        }

        Map<String, Object> map = new HashMap<String, Object>();
        String finalResult = "";
        for (int i = 0; i < finalAudit.length(); i++) {
            map = finalAudit.getJSONObject(i).toMap();
            finalResult += "<Custom item>\n" + map.entrySet().stream().map((entry) -> //stream each entry, map it to string value
                    entry.getKey() + ":" + entry.getValue() + "\n")
                    .collect(Collectors.joining(" ")) + "</Custom item>\n\n";
        }
        String pattern = "yyMMddHHmmssZ";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String filename = initialFileName.substring(0, initialFileName.length() - 6) + "_" + simpleDateFormat.format(new Date()) + "_copy.audit";
        FileWriter file = new FileWriter(filename);
        file.write(finalResult);
        file.close();

    }

    public void scanPolicies(ActionEvent event) throws IOException {
        event.consume();

        if (indexesArray.size() > 0) {
            selectedIndices.forEach(index -> {
                finalAudit.put(jsonArr.get(indexesArray.get(index)));
            });
        } else {
            selectedIndices.forEach(index -> {
                finalAudit.put(jsonArr.get(index));
            });
        }
        selectedPolicies = finalAudit;
        observableList = scan(finalAudit);
        listView.setItems(observableList);
        setColor(listView);
        finalAudit = new JSONArray();
        filter();
    }

    public void setColor(ListView listView){
        listView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(item);
                        if (item != null && item.contains("Success!")) {
                            setStyle("-fx-control-inner-background: derive(palegreen, 50%);");
                        } else if (item != null && item.contains("-Skipped-")) {
                            setStyle("-fx-control-inner-background: derive(yellow, 50%);");
                        } else if (item != null && item.contains("Failure!")) {
                            setStyle("-fx-control-inner-background: derive(#ff0000, 100%);");
                        }
                    }
                };
            }
        });
    }

    public ObservableList<String> scan(JSONArray finalAudit) throws IOException {
        List<String> updatedList = new ArrayList<>();
        StringBuilder cmdOutput = new StringBuilder();
        Map<String, Object> map = new HashMap<String, Object>();
        for (int i = 0; i < finalAudit.length(); i++) {
            map = finalAudit.getJSONObject(i).toMap();
            if (map.get(" reg_key ") != null && map.get(" reg_item ") != null) {
                ProcessBuilder builder = new ProcessBuilder(
                        "cmd.exe", "/c", "reg query" + map.get(" reg_key ").toString() + " /v " + map.get(" reg_item ").toString());
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
                try {
                    if (cmdOutput.toString().contains("ERROR:")) {
                        updatedList.add("--Skipped-- " + map.get(" description ").toString());
                    } else {
                        String[] actualOutput = cmdOutput.toString().trim().split("  +");
                        Pattern pattern = Pattern.compile(map.get(" value_data ").toString().replaceAll("\"","").trim(), DOTALL);
                        Matcher m = pattern.matcher(actualOutput[actualOutput.length - 1]);
                        if(!selectedPolicies.isEmpty()) {
                            selectedPolicies.getJSONObject(i).put("actual_data", actualOutput[actualOutput.length - 1]);
                        }
                        if (m.find())
                            updatedList.add("Success! " + map.get(" description ").toString());
                        else
                            updatedList.add("Failure! " + map.get(" description ").toString());
                    }
                } catch (Exception e) {
                    updatedList.add("--Skipped-- " + map.get(" description ").toString());
                }
            } else updatedList.add("--Skipped-- " + map.get(" description ").toString());
        }
        return FXCollections.observableArrayList(updatedList);
    }

    @FXML
    public void changePolicies(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("EnforcePolicies.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("EnforcePolicies.fxml"));
        EnforcePoliciesController controller = new EnforcePoliciesController();
        loader.setController(controller);
        loader.setRoot(root);

        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.show();

        List<String> selectedScannedItems = new ArrayList<>();
        List<String> actualList = new ArrayList<>();
        List<String> expectedList = new ArrayList<>();
        JSONArray scannedItems = new JSONArray();
        if (indexesArray.size() > 0) {
            selectedIndices.forEach(index -> {
                JSONObject selectedItem = selectedPolicies.getJSONObject(indexesArray.get(index));
                selectedScannedItems.add(selectedItem.get(" description ").toString());
                expectedList.add( selectedItem.get(" value_data ").toString());
                actualList.add(selectedItem.get("actual_data").toString());
                scannedItems.put(selectedItem);
            });
        } else {
            selectedIndices.forEach(index -> {
                JSONObject selectedItem = selectedPolicies.getJSONObject(index);
                selectedScannedItems.add(selectedItem.get(" description ").toString());
                expectedList.add( selectedItem.get(" value_data ").toString());
                actualList.add(selectedItem.get("actual_data").toString());
                scannedItems.put(selectedItem);
            });
        }
        EnforcePoliciesController.observableListActual = FXCollections.observableArrayList(actualList);
        EnforcePoliciesController.observableListExpected = FXCollections.observableArrayList(expectedList);
        EnforcePoliciesController.selectedPolicies = scannedItems;


    }

    @FXML
    public void readAndConvertFile() throws IOException {
        jsonArr = new JSONArray();
        File chosedFile = PickAFile();
        initialFileName = chosedFile.getName();

        textFuture = Collections.singletonList(reader.read(new File(chosedFile.getPath()))).get(0);
        executorService.shutdown();
        Pattern pattern = Pattern.compile("<custom_item>(.*?)</custom_item>", DOTALL);
        Matcher m = pattern.matcher(textFuture.replaceAll(" +", " ").trim());

        String line;
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        String tempKey = null;
        while (m.find()) {
            String text = m.group(1).trim();
            Reader inputString = new StringReader(text);
            BufferedReader reader = new BufferedReader(inputString);
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 2);
                if (parts.length >= 2) {
                    String key = parts[0];
                    String value = parts[1];
                    map.put(key, value);
                    tempKey = key;
                } else if (!line.contains(" : ")) {
                    map.put(tempKey, map.get(tempKey) + parts[0]);
                }
            }
            JSONObject jsonObject = new JSONObject(map);
            map.clear();
            jsonArr.put(jsonObject);
        }

        saveFile();
    }

    public File PickAFile() {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                ".audit files", "audit");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }
}
