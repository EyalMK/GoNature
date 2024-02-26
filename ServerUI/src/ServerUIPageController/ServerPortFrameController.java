package ServerUIPageController;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import ServerUIPage.ServerUI;
import VisitorsControllers.StudentFormController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;

public class ServerPortFrameController implements Initializable {


    @FXML
    private Button btnExit = null;
    @FXML
    private Button btnStart;
    @FXML
    private Label lbllist;
    @FXML
    private TextArea loggerTextArea;

    @FXML
    private Label lblLogger;
    @FXML
    private Label lblSQLUser;

    @FXML
    private Label lblSQLPassword;

    @FXML
    private Label lblSQLURL;

    @FXML
    private ComboBox URLComboBox;

    @FXML
    private TextField TextfieldUserName;

    @FXML
    private TextField TextFieldPassword;

    @FXML
    private Button BtnStop;
    @FXML
    private TextField portxt;
    @FXML
    private TableView<Map> tableClients;

    @FXML
    private TableColumn<Map, String> colName;

    @FXML
    private TableColumn<Map, String> colIP;
    ObservableList<String> list;

    public String getPort() {
        return portxt.getText();
    }

    public void Done(ActionEvent event) throws Exception {
        String p;
        p = getPort();
        if (p.trim().isEmpty()) {
            addtolog("You must enter a port number");

        } else if (getURLComboBox().isEmpty() || getUserName().isEmpty() || getPassword().isEmpty()) {
            addtolog("You must enter a URL, username and password");
            addtolog("Please try again");
        } else {
            ServerUI.runServer( this);
            toggleControllers(true);
        }
    }

    public void start(Stage primaryStage) throws Exception {
            Parent root = FXMLLoader.load(getClass().getResource("/ServerUIPageController/ServerPort.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/ServerUIPageController/ServerPort.css").toExternalForm());
            primaryStage.setTitle("Server");
            primaryStage.setScene(scene);
            primaryStage.show();
    }

    public void setDefaultValues() {
        this.TextFieldPassword.setText("Aa123456");
        this.TextfieldUserName.setText("Eyal");
        this.portxt.setText("5555");
        // Set default value for the ComboBox to "192.168.194.1" if it exists in the items list
        String defaultURL = "192.168.194.1";
        if (list.contains(defaultURL)) {
            this.URLComboBox.setValue(defaultURL);
        } else {
            // Handle the case where the default value doesn't exist in the items list
            // You might want to set a different default value or handle this case differently
            System.err.println("Default URL not found in the list: " + defaultURL);
        }
    }
    @FXML
    public void getExitBtn(ActionEvent event) throws Exception {
        addtolog("Exit Server");
        System.exit(0);
    }
    @FXML
    void stopServer(ActionEvent event) throws Exception {
        ServerUI.closeServer();
    }
    @FXML
    public synchronized void addtolog(String str) {
        System.out.println(str); // Consider removing or redirecting to a file logger for production
        Platform.runLater(() -> loggerTextArea.appendText(str + "\n"));
    }

    @FXML
    private void setURLComboBox() {
        ArrayList<String> UrlComboList = new ArrayList<>();
        UrlComboList.add("localhost");
        UrlComboList.add("192.168.194.1");
        list = FXCollections.observableArrayList(UrlComboList);
        URLComboBox.setItems(list);
    }
    @FXML
    public String getURLComboBox() {
        return (String) URLComboBox.getValue();
    }
    @FXML
    public String getUserName() {
        return TextfieldUserName.getText();
    }
    @FXML
    public String getPassword() {
        return TextFieldPassword.getText();
    }
    @FXML
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        setURLComboBox();
        setDefaultValues();
        colName.setCellValueFactory(new MapValueFactory<>("name"));
        colIP.setCellValueFactory(new MapValueFactory<>("ip"));
        tableClients.setRowFactory(tv -> {
            TableRow<Map> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                    Map clickedRowData = row.getItem();
                    // Handle the clicked row data, e.g., display it
                    System.out.println("Selected row data: " + clickedRowData);
                }
            });
            return row;
        });
    }
    @FXML
    public void addRow(String name, String ip) {
        Map<String, String> newRow = new HashMap<>();
        newRow.put("name", name);
        newRow.put("ip", ip);

        tableClients.getItems().add(newRow);
    }
    @FXML
    public void removeRowByIP(String ip) {
        // Use removeIf with a predicate to remove rows matching the condition
        tableClients.getItems().removeIf(row -> ip.equals(row.get("ip")));
    }
    @FXML
    public void toggleControllers(boolean flag) {
        btnStart.setDisable(flag);
        TextfieldUserName.setDisable(flag);
        TextFieldPassword.setDisable(flag);
        URLComboBox.setDisable(flag);
        portxt.setDisable(flag);
        BtnStop.setDisable(!flag);
    }
}