package com.simulator.controller;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;

@Service
public class TfiModule {
    private static TfiModule INSTANCE;

    private Logger logger = LogManager.getLogger(getClass().getName());
    public InputStream in;
    public OutputStream out;
    public SerialPort serialPort;
    private boolean closed = true;


    public static TfiModule getInstance() throws Exception {
        if (INSTANCE == null) {
            INSTANCE = new TfiModule();
        }
        return INSTANCE;
    }
//
//    private TfiModule(String port) throws Exception {
//        connect(port);
//    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public void connect(String portName) throws Exception {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if (portIdentifier.isCurrentlyOwned()) {
            logger.error("Error: Port is currently in use");
            throw new Exception("Error: Port is currently in use ");
        } else {
            CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);

            if (commPort instanceof SerialPort) {
                serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

                logger.info("TFI connect ok on port: " + portName);
                serialPort.addEventListener((SerialPortEvent serialPortEvent) -> {
                    try {
                        logger.info("serialPortEvent:" + serialPortEvent.toString());
                        if (serialPortEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
                            try {
                                byte singleData = (byte) in.read();
                            } catch (Exception e) {
                                logger.error("Problème lors de la gestion des evenements", e);
                            }
                        }
                    } catch (Throwable e) {
                        logger.error("Problème lors de la gestion des evenements", e);
                    }
                });
                in = serialPort.getInputStream();
                out = serialPort.getOutputStream();
                closed = false;
            } else {
                logger.error("Error: Only serial ports are handled ");
                //throw new Exception(Labels.ERROR_TFI + Labels.ERROR_SERIAL_PORTS);
            }
        }
    }

    public void close(String portName) throws Exception {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if (portIdentifier.isCurrentlyOwned()) {
            serialPort.removeEventListener();
            in.close();
            out.close();
            serialPort.close();
        }
    }

    public void sendCommand(byte[] command) throws Exception {
        out.write(command);
        out.flush();
    }
}