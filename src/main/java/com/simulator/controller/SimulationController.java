package com.simulator.controller;

import com.simulator.fxComponent.NumberTextField;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;

public class SimulationController {
    private static final Logger log = LoggerFactory.getLogger(SimulationController.class);

    private TfiModule tfiModule;
    private byte headerByte = (byte) 0X01;

    private byte vehicleClassByte = (byte)  'C';
    private byte tollFareByte = (byte)  'P';
    private byte[] displayMessageByte = {(byte) 0x4D, (byte) 0x50, (byte) 0x46};  // M, P, F

    @FXML private VBox mainBox;
    @FXML private GridPane gridPane;
    @FXML private VBox messageBox;
    @FXML private Text header;
    @FXML private Label errMsgLabel;
    @FXML private Label successMsgLabel;
    @FXML private Label portMsgLabel;
    @FXML private Label classMsgLabel;
    @FXML private Label fareMsgLabel;
    @FXML private Label messageLabel;
    @FXML private ComboBox<String> comPicker;
    @FXML private Button connectBtn;
    @FXML private Button closeBtn;
    @FXML private TextField classPicker;
    @FXML private NumberTextField fareField;
    @FXML private TextField msgLine1;
    @FXML private TextField msgLine2;

    private String selectedClass;
    private String inputFare;
    public static final int TFI_WIDTH = 12;
    private String inputMsg;

    @FXML
    public void initialize() {
        log.info("BEGIN - initialize");
        closeBtn.setDisable(true);
        fareField.setText("0");

        header.setText("TFI Tester");
        header.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 20));
        header.setX(100);
        header.setY(100);

        Enumeration ports = CommPortIdentifier.getPortIdentifiers();
        //String portArray[] = null;
        ArrayList portArray = new ArrayList();
        while (ports.hasMoreElements()) {
            CommPortIdentifier port = (CommPortIdentifier) ports.nextElement();
            portArray.add(port.getName());
        }

        comPicker.setEditable(true);
        comPicker.setItems(FXCollections.observableArrayList(portArray));
        comPicker.getSelectionModel().selectFirst();
        comPicker.setOnAction(action -> {
            log.info(comPicker.getSelectionModel().getSelectedItem());
        });

        int max = 2;
        classPicker.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > max) {
                String copy = newValue.substring(0, max);
                classPicker.setText(copy);
            }
        });

        mainBox.setMargin(gridPane, new Insets(5, 10, 5, 10));
        mainBox.setMargin(messageBox, new Insets(5, 10, 10, 10));
    }

    public void connectPort(){
        String port = comPicker.getSelectionModel().getSelectedItem();
        try {
            tfiModule = TfiModule.getInstance();
            tfiModule.connect(port);

            portMsgLabel.setText("Connected to " + port);
            connectBtn.setDisable(true);
            closeBtn.setDisable(false);
            errMsgLabel.setText("");
        } catch (Exception e) {
            errMsgLabel.setText("Error: "+e.toString());
            System.out.println("Error "+e.toString());
            e.printStackTrace();
        }
    }

    public void closePort(){
        String port = comPicker.getSelectionModel().getSelectedItem();
        try {
            if(tfiModule != null){
                resetDisplay();
                tfiModule.close(port);
                portMsgLabel.setText("Disconnected to " + port);
                connectBtn.setDisable(false);
                closeBtn.setDisable(true);

            } else {
                System.out.println("no open port");
            }
            errMsgLabel.setText("");
        } catch (Exception e) {
            errMsgLabel.setText("Error: "+e.toString());
            e.printStackTrace();
        }
    }

    public void displayClass(){
        try{
            resetDisplay();
            selectedClass = classPicker.getText();
            System.out.println("display class: "+selectedClass);
            tfiModule.sendCommand(toMessageBean("class"));
        } catch (Exception e) {
            resetDisplay();
            successMsgLabel.setText("");
            errMsgLabel.setText("Error Sending to device: "+e.toString());
            e.printStackTrace();
        }
    }

    public void clearClass(){
        try {
            resetDisplay();
            classPicker.setText("");
            selectedClass = "";
            tfiModule.sendCommand(toMessageBean("class"));
        } catch (Exception e){
            successMsgLabel.setText("");
            errMsgLabel.setText("Error Sending to device: "+e.toString());
            e.printStackTrace();
        }

    }

    public void displayTollFare(){
        try{
            resetDisplay();
            inputFare = String.format("%6d", Integer.parseInt(fareField.getText()));
            System.out.println("display toll: "+inputFare);
            tfiModule.sendCommand(toMessageBean("fare"));
            successMsgLabel.setText("Message sent to Device");
            errMsgLabel.setText("");
        }catch (Exception e){
            resetDisplay();
            successMsgLabel.setText("");
            errMsgLabel.setText("Error Sending to device: "+e.toString());
            e.printStackTrace();
        }
    }

    public void clearTollFare(){
        try {
            resetDisplay();
            fareField.setText("0");
            inputFare = "";
            tfiModule.sendCommand(toMessageBean("fare"));
            successMsgLabel.setText("Message sent to Device");
            errMsgLabel.setText("");
        }catch (Exception e){
            resetDisplay();
            successMsgLabel.setText("");
            errMsgLabel.setText("Error Sending to device: "+e.toString());
            e.printStackTrace();
        }
    }

    public void displayMessage(){
        try {
            resetDisplay();
            inputMsg = centerString(msgLine1.getText()) + centerString(msgLine2.getText());
            tfiModule.sendCommand(toMessageBean("message"));
            successMsgLabel.setText("Message sent to Device");
            errMsgLabel.setText("");
        } catch (Exception e){
            resetDisplay();
            successMsgLabel.setText("");
            errMsgLabel.setText("Error Sending to device: "+e.toString());
            e.printStackTrace();
        }

    }

    public void clearMessage(){
        try{
            resetDisplay();
            msgLine1.setText("");
            msgLine2.setText("");
            inputMsg = "";
            tfiModule.sendCommand(toMessageBean("message"));
            successMsgLabel.setText("Message sent to Device");
            errMsgLabel.setText("");
        }catch (Exception e){
            resetDisplay();
            successMsgLabel.setText("");
            errMsgLabel.setText("Error Sending to device: "+e.toString());
            e.printStackTrace();
        }
    }

    public void displayAll(){
        try{
            resetDisplay();
            selectedClass = classPicker.getText();
            inputFare = String.format("%6d", Integer.parseInt(fareField.getText()));
            inputMsg = centerString(msgLine1.getText()) + centerString(msgLine2.getText());
            tfiModule.sendCommand(toMessageBean("all"));
            successMsgLabel.setText("Message sent to Device");
            errMsgLabel.setText("");
        } catch (Exception e){
            resetDisplay();
            successMsgLabel.setText("");
            errMsgLabel.setText("Error Sending to device: "+e.toString());
            e.printStackTrace();
        }

    }
    public void resetDisplay(){
        selectedClass = "";
        inputFare = "";
        inputMsg = "";
        classMsgLabel.setText("");
        fareMsgLabel.setText("");
        messageLabel.setText("");
        successMsgLabel.setText("");
        errMsgLabel.setText("");
    }
    public void resetAll(){
        try{
            resetDisplay();
            tfiModule.sendCommand(toMessageBean("all"));
            successMsgLabel.setText("Message sent to Device");
            errMsgLabel.setText("");
        } catch(Exception e){
            resetDisplay();
            successMsgLabel.setText("");
            errMsgLabel.setText("Error Sending to device: "+e.toString());
            e.printStackTrace();
        }

    }

    public byte[] toMessageBean(String type){
        byte[] byteValue = null;
        if(type == "class" || type=="all"){
            byteValue = new byte[4];
            byteValue[0] = headerByte;
            byteValue[1] = vehicleClassByte;

            String vehicleClass =  org.apache.commons.lang3.StringUtils.leftPad(selectedClass,2).toUpperCase();
            System.arraycopy(vehicleClass.getBytes(StandardCharsets.US_ASCII), 0,byteValue,2, vehicleClass.length());

            StringBuilder sb = printBytes(byteValue);
            classMsgLabel.setText("Displaying: "+selectedClass +"\n" +"Byte: " + sb);
        }

        if(type == "fare" || type=="all"){
            byteValue = new byte[8];
            byteValue[0] = headerByte;
            byteValue[1] = tollFareByte;

            String str =  org.apache.commons.lang3.StringUtils.leftPad(inputFare,6);
            System.arraycopy(str.getBytes(StandardCharsets.US_ASCII), 0,byteValue,2, str.length());

            StringBuilder sb = printBytes(byteValue);
            fareMsgLabel.setText("Displaying: "+inputFare +"\n" +"Byte: " + sb);
        }

        if(type == "message" || type=="all"){
            byteValue = new byte[28];
            byteValue[0] = headerByte;
            byteValue[1] = displayMessageByte[0];
            byteValue[2] = displayMessageByte[1];
            byteValue[3] = displayMessageByte[2];

            System.arraycopy(inputMsg.getBytes(StandardCharsets.US_ASCII), 0,byteValue,4, inputMsg.length() );

            StringBuilder sb = printBytes(byteValue);
            messageLabel.setText("Displaying: "+inputMsg +"\n" + "Byte: " + sb);
        }

        try {
            tfiModule.sendCommand(byteValue);
            successMsgLabel.setText("Message sent to Device");
            errMsgLabel.setText("");
        } catch (Exception e) {
            errMsgLabel.setText("Error Sending to device: "+e.toString());
            e.printStackTrace();
        }

        return byteValue;
    }

    public StringBuilder printBytes(byte[] buffer){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < buffer.length; i++) {
            if (i != 0) sb.append(' ');
            sb.append(String.format("%02X", buffer[i]));
        }
        return sb;
    }

    public String centerString(String input) {
        int inputLength = input == null ? 0 : input.length();
        if (inputLength == 0) {
            return org.apache.commons.lang3.StringUtils.center("", TFI_WIDTH);
        } else if (inputLength > TFI_WIDTH) {
            return input.substring(0, TFI_WIDTH - 1).toUpperCase() + ".";
        } else if (inputLength % 2 == 0) {
            return org.apache.commons.lang3.StringUtils.repeat(" ", TFI_WIDTH / 2 - inputLength / 2)
                    + input.toUpperCase()
                    + org.apache.commons.lang3.StringUtils.repeat(" ", TFI_WIDTH / 2 - inputLength / 2);
        } else {
            return org.apache.commons.lang3.StringUtils.repeat(" ", TFI_WIDTH / 2 - inputLength / 2 - 1)
                    + input.toUpperCase()
                    + org.apache.commons.lang3.StringUtils.repeat(" ", TFI_WIDTH / 2 - inputLength / 2);
        }
    }

}