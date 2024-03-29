package EmployeesControllers;

import CommonClient.ClientUI;
import CommonClient.controllers.BaseController;
import CommonUtils.CommonUtils;
import CommonUtils.ConfirmationPopup;
import Entities.*;
import client.ClientCommunicator;
import io.github.palexdev.materialfx.controls.legacy.MFXLegacyComboBox;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

import static CommonUtils.CommonUtils.convertStringToTimestamp;
import static CommonUtils.CommonUtils.convertTimestampToMinutes;

public class RequestSettingParkParametersController extends BaseController implements Initializable {

    @FXML
    private Label lblErrorMsg;

    @FXML
    private Label lblSuccessMsg;

    @FXML
    private TextField txtDifferenceOrdersVisitors;

    @FXML
    private MFXLegacyComboBox<String> cmbMaxVisitation;

    @FXML
    private TextField txtParkCapacity;

    private Park park;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        for (int i = 1; i <= 8; i++) {
            cmbMaxVisitation.getItems().add(String.valueOf(i));
        }
    }

    public void cleanup() {
        lblErrorMsg.setText("");
        lblSuccessMsg.setText("");
        txtDifferenceOrdersVisitors.setText("");
        cmbMaxVisitation.setValue("");
        txtParkCapacity.setText("");
    }

    public void getParkParameters() {
        ParkManager user = (ParkManager) applicationWindowController.getUser();
        Message msg, response;

        // Populate Park for the department ID, in order to create a request for the department.
        String ParkID = user.getParkID();
        msg = new Message(OpCodes.OP_GET_PARK_DETAILS_BY_PARK_ID, applicationWindowController.getUser().getUsername(), ParkID);

        ClientUI.client.accept(msg);

        response = ClientCommunicator.msg;
        OpCodes returnOpCode = response.getMsgOpcode();
        if (returnOpCode == OpCodes.OP_DB_ERR) {
            ConfirmationPopup confirmationPopup = new ConfirmationPopup(CommonUtils.DB_ERROR, applicationWindowController, 800, 400, true, "OK", true);
            confirmationPopup.show(applicationWindowController.getRoot());
            return;
        }
        // Checking if the response from the server is inappropriate.
        if (returnOpCode != OpCodes.OP_GET_PARK_DETAILS_BY_PARK_ID) {
            ConfirmationPopup confirmationPopup = new ConfirmationPopup(CommonUtils.SERVER_ERROR, applicationWindowController, 800, 400, true, "OK", true);
            confirmationPopup.show(applicationWindowController.getRoot());
            return;
        }
        if (!(response.getMsgData() instanceof Park)) {
            ConfirmationPopup confirmationPopup = new ConfirmationPopup(CommonUtils.SERVER_ERROR, applicationWindowController, 800, 400, true, "OK", true);
            confirmationPopup.show(applicationWindowController.getRoot());
            return;
        }

        park = (Park) response.getMsgData();
        // Might as well set the park to the user.
        user.setPark(park);


        int localDateTime = park.getDefaultVisitationTime().toLocalDateTime().getHour();
        cmbMaxVisitation.setValue(String.valueOf(localDateTime));
        txtParkCapacity.setText(String.valueOf(park.getCapacity()));
        txtDifferenceOrdersVisitors.setText(String.valueOf(park.getGapVisitorsCapacity()));
    }

    @FXML
    void OnClickSubmitButton(ActionEvent ignoredEvent) {
        lblSuccessMsg.setText("");
        lblErrorMsg.setText("");
        boolean maxVisitRequest = !Objects.equals(cmbMaxVisitation.getValue(), "");
        String gapOrdersAndVisitors = txtDifferenceOrdersVisitors.getText();
        Timestamp maxVisitationLongevity = null;

        Date tmpDate = new Date(System.currentTimeMillis());

        if (maxVisitRequest) {
            maxVisitationLongevity = convertStringToTimestamp(tmpDate.toString(), String.format("%02d:00", Integer.parseInt(cmbMaxVisitation.getValue())));
        }
        String parkCapacity = txtParkCapacity.getText();

        if (gapOrdersAndVisitors.isEmpty() && !maxVisitRequest && parkCapacity.isEmpty()) {
            lblErrorMsg.setText("Please fill at least one of the fields");
            return;
        }

        if ((!gapOrdersAndVisitors.isEmpty() && !gapOrdersAndVisitors.matches("[0-9]+")) ||
                (!parkCapacity.isEmpty() && !parkCapacity.matches("[0-9]+"))) {
            lblErrorMsg.setText("Please enter only numbers");
            return;
        }

        if ((!gapOrdersAndVisitors.isEmpty() && Integer.parseInt(gapOrdersAndVisitors) < 0) ||
                (!parkCapacity.isEmpty() && Integer.parseInt(parkCapacity) < 0)) {
            lblErrorMsg.setText("Please enter only positive numbers");
            return;
        }

        if (!gapOrdersAndVisitors.isEmpty() && Integer.parseInt(gapOrdersAndVisitors) > Integer.parseInt(parkCapacity)) {
            lblErrorMsg.setText("Difference between the number of orders and visitors capacity must be lesser than the park capacity.");
            return;
        }


        Message msg, response;
        // Create a map of the requests to be submitted to the department.
        Map<ParkParameters, RequestChangingParkParameters> requestMap = new HashMap<>();
        if (!gapOrdersAndVisitors.isEmpty() && park.getGapVisitorsCapacity() != Double.parseDouble(gapOrdersAndVisitors)) {
            requestMap.put(ParkParameters.PARK_GAP_VISITORS_CAPACITY, new RequestChangingParkParameters(park, ParkParameters.PARK_GAP_VISITORS_CAPACITY, Double.parseDouble(gapOrdersAndVisitors)));
        }
        if (maxVisitRequest && !convertTimestampToMinutes(maxVisitationLongevity).equals(convertTimestampToMinutes(park.getDefaultVisitationTime()))) {
            requestMap.put(ParkParameters.PARK_DEFAULT_MAX_VISITATION_LONGEVITY, new RequestChangingParkParameters(park, ParkParameters.PARK_DEFAULT_MAX_VISITATION_LONGEVITY, Double.parseDouble(convertTimestampToMinutes(maxVisitationLongevity).toString())));
        }
        if (!parkCapacity.isEmpty() && park.getCapacity() != Double.parseDouble(parkCapacity)) {
            requestMap.put(ParkParameters.PARK_CAPACITY, new RequestChangingParkParameters(park, ParkParameters.PARK_CAPACITY, Double.parseDouble(parkCapacity)));
        }

        if (requestMap.isEmpty()) {
            return;
        }

        msg = new Message(OpCodes.OP_SUBMIT_REQUESTS_TO_DEPARTMENT, applicationWindowController.getUser().getUsername(), requestMap);
        ClientUI.client.accept(msg);

        response = ClientCommunicator.msg;
        OpCodes returnOpCode = response.getMsgOpcode();
        if (returnOpCode == OpCodes.OP_DB_ERR) {
            ConfirmationPopup confirmationPopup = new ConfirmationPopup(CommonUtils.DB_ERROR, applicationWindowController, 800, 400, true, "OK", true);
            confirmationPopup.show(applicationWindowController.getRoot());
            return;
        }
        // Checking if the response from the server is inappropriate.
        if (returnOpCode != OpCodes.OP_SUBMIT_REQUESTS_TO_DEPARTMENT) {
            ConfirmationPopup confirmationPopup = new ConfirmationPopup(CommonUtils.SERVER_ERROR, applicationWindowController, 800, 400, true, "OK", true);
            confirmationPopup.show(applicationWindowController.getRoot());
            return;
        }

        lblSuccessMsg.setText("Your requests have been submitted successfully!");
    }



}
