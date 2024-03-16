package VisitorsControllers;


import CommonClient.ClientUI;
import CommonClient.controllers.BaseController;
import CommonUtils.CommonUtils;
import CommonUtils.ConfirmationPopup;
import Entities.*;
import client.ClientCommunicator;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.controls.legacy.MFXLegacyComboBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Separator;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import javax.naming.CommunicationException;
import java.awt.*;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class GroupGuideOrderVisitationPageController extends BaseController implements Initializable {
    ObservableList<String> list;
    @FXML
    private Label erorrLbl;
    @FXML
    private MFXButton btnCreateOrder;


    @FXML
    private DatePicker datePicker;

    @FXML
    private MFXLegacyComboBox<String> numOfVisitorsCmbBox;

    @FXML
    private Pane pane;

    @FXML
    private MFXLegacyComboBox<String> parkCmbBox;

    @FXML
    private Separator sepOrder;

    @FXML
    private StackPane stackPane;

    @FXML
    private MFXLegacyComboBox<String> timeOfVisitCmbBox;

    @FXML
    private MFXTextField txtEmail;

    @FXML
    private MFXTextField txtFirstName;

    @FXML
    private MFXTextField txtLastName;

    @FXML
    private MFXTextField txtPhone;

    public void cleanup() {
        txtEmail.clear();
        txtFirstName.clear();
        txtLastName.clear();
        txtPhone.clear();
        numOfVisitorsCmbBox.getSelectionModel().clearSelection();
        parkCmbBox.getSelectionModel().clearSelection();
        timeOfVisitCmbBox.getSelectionModel().clearSelection();
        numOfVisitorsCmbBox.setValue(null);
        parkCmbBox.setValue(null);
        timeOfVisitCmbBox.setValue(null);
        datePicker.setValue(null);
    }


    /**
     * Populates the park selection combo box with available parks.
     * The parks are retrieved from a static data source.
     */
    private void setParkCmbBox() {
        ArrayList<String> al = new ArrayList<String>();
        for (String key : ParkBank.getUnmodifiableMap().keySet()) {
            al.add(key);
        }
        list = FXCollections.observableArrayList(al);
        parkCmbBox.setItems(list);
    }


    /**
     * Populates the time of visit combo box with hourly time slots.
     * Time slots range from 08:00 to 19:00.
     */
    private void setTimeOfVisitCmbBox() {
        ArrayList<String> al = new ArrayList<String>();
        for (int i = 8; i <= 19; i++) {
            if (i < 10) {
                al.add("0" + i + ":00");
            } else {
                al.add("" + i + ":00");
            }
        }
        list = FXCollections.observableArrayList(al);
        timeOfVisitCmbBox.setItems(list);
    }

    /**
     * Disables date selection for past dates and dates more than one year into the future
     * in the {@link DatePicker}.
     */
    private void setDatePicker() {
        datePicker.setDayCellFactory(picker -> new DateCell() {
            LocalDate maxDate = LocalDate.now().plusYears(1); // Setting maximum date to one year from now

            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.compareTo(LocalDate.now()) < 0 || date.compareTo(maxDate) > 0);
            }
        });
    }

    private void setNumOfVisitorsCmbBox() {
        ArrayList<String> al = new ArrayList<String>();
        for (int i = 1; i <= 15; i++) {
            al.add(String.valueOf(i));
        }
        list = FXCollections.observableArrayList(al);
        numOfVisitorsCmbBox.setItems(list);
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the FXML file has been loaded. It initializes the form components
     * and sets up any necessary data bindings or event handlers.
     *
     * @param location  The location used to resolve relative paths for the root object, or null if unknown.
     * @param resources The resources used to localize the root object, or null if not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setParkCmbBox();
        setTimeOfVisitCmbBox();
        setDatePicker();
        setNumOfVisitorsCmbBox();

    }

    @FXML
    void OnClickCreateOrderButton(ActionEvent event) throws CommunicationException {
        if (!validateFields()) {
            System.out.println("one or more details are empty ");
            return;
        }
        if (!(applicationWindowController.getUser() instanceof VisitorGroupGuide)) {
            System.out.println("The user isn't visitor");
            return;
        }

        VisitorGroupGuide guide = (VisitorGroupGuide) applicationWindowController.getUser();
        Timestamp timeOfVisit = CommonClient.Utils.convertStringToTimestamp(datePicker.getValue().toString(), timeOfVisitCmbBox.getValue());
        Order order = new Order(guide.getID(), ParkBank.getUnmodifiableMap().get(parkCmbBox.getValue()), timeOfVisit, txtEmail.getText(), txtPhone.getText(), null, timeOfVisit, timeOfVisit, null, OrderType.ORD_TYPE_GROUP, (CommonUtils.convertStringToInt(numOfVisitorsCmbBox.getValue())));
        Object msg = new Message(OpCodes.OP_CREATE_NEW_VISITATION, guide.getUsername(), order);
        ClientUI.client.accept(msg);
        Message respondMsg = ClientCommunicator.msg;
        OpCodes returnOpCode = respondMsg.getMsgOpcode();
        if (returnOpCode != OpCodes.OP_CREATE_NEW_VISITATION) {
            throw new CommunicationException("Response from server is not appropriate");
        }
        if (respondMsg.getMsgData() instanceof Order) {
            if (((Order) respondMsg.getMsgData()).getOrderID() != null) {
                Order cnfrmorder = (Order) respondMsg.getMsgData();
                String strForPopup = "The order " + cnfrmorder.getOrderID() + " has been created successfully";
                ConfirmationPopup confirmPopup = new ConfirmationPopup(strForPopup, () ->
                {
                    applicationWindowController.loadDashboardPage(applicationWindowController.getUser().getRole());
                    applicationWindowController.loadMenu(applicationWindowController.getUser());
                    clearFields();
                }
                        , 600, 300, false, "OK", false);
                confirmPopup.show(applicationWindowController.getRoot());
            } else {
                String strForPopup = "The park is at full capacity. Do you want to sign up to the waitlist?";
                ConfirmationPopup confirmPopup;
                confirmPopup = new ConfirmationPopup(strForPopup, () ->
                {
                    applicationWindowController.loadVistorsPage("WaitListPage");
                    applicationWindowController.loadMenu(applicationWindowController.getUser());
                    clearFields();
                }, () -> {
                    applicationWindowController.loadDashboardPage(applicationWindowController.getUser().getRole());
                    applicationWindowController.loadMenu(applicationWindowController.getUser());
                    clearFields();
                },
                        300, 150, false, "Yes", "No", false);
                confirmPopup.show(applicationWindowController.getRoot());
            }
        }
    }

    /**
     * Validates the input fields of the form. Checks for empty fields, valid email, phone number,
     * and the number of visitors.
     *
     * @return true if all inputs are valid, false otherwise.
     */
    private boolean validateFields() {
        if (CommonUtils.anyStringEmpty(txtFirstName.getText(), txtLastName.getText(), txtPhone.getText(), txtEmail.getText(), numOfVisitorsCmbBox.getValue()) || parkCmbBox.getValue() == null || datePicker.getValue() == null || timeOfVisitCmbBox.getValue() == null) {
            erorrLbl.setText("One or more fields are empty.");
            return false;
        }
        if (!CommonUtils.isValidPhone(txtPhone.getText())) {
            erorrLbl.setText("Invalid phone. Please check your input.");
            return false;
        }
        if (!CommonUtils.isValidName(txtFirstName.getText()) || !CommonUtils.isValidName(txtLastName.getText()))
            erorrLbl.setText("Please enter a valid first and last name.");
        if (!CommonUtils.isEmailAddressValid(txtEmail.getText())) {
            erorrLbl.setText("Invalid email. Please check your input.");
            return false;
        }
        if (CommonClient.Utils.isOrderTimeValid(datePicker.getValue().toString(), timeOfVisitCmbBox.getValue().toString())) {
            erorrLbl.setText("Invalid visitation date. You cannot book an order less than 24 hours of the chosen visitation date.");
            return false;
        }
        if (CommonUtils.convertStringToInt(numOfVisitorsCmbBox.getValue()) <= 0) {
            erorrLbl.setText("Invalid number of visitors. Please check your input.");
            return false;
        }
        return true;
    }

    /**
     * Clears all user input fields.
     */
    private void clearFields() {
        // Clear text fields
        txtEmail.clear();
        txtFirstName.clear();
        txtLastName.clear();
        txtPhone.clear();

        // Reset combo boxes
        numOfVisitorsCmbBox.getSelectionModel().clearSelection();
        parkCmbBox.getSelectionModel().clearSelection();
        timeOfVisitCmbBox.getSelectionModel().clearSelection();
        numOfVisitorsCmbBox.setValue(null); // Use this if the combo box text does not clear with clearSelection()
        parkCmbBox.setValue(null);
        timeOfVisitCmbBox.setValue(null);

        // Reset the date picker
        datePicker.setValue(null);
    }


}
