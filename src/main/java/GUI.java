import jssc.SerialPortException;
import jssc.SerialPortList;
import keyboard.KeyConfigurator;
import serial.Connection;
import serial.ConnectionImpl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class GUI extends JFrame implements Observer {

    private static final Logger LOG = Logger.getLogger(GUI.class.getName());
    private static final int[] CODE = {
            1,
            2,
            4,
            8,
            16,
            32,
            64,
            128,
            256,
            512,
            1024,
            2048
    };
    private static final Long PORT_SCAN_INTERVAL = 1000 * 5L;

    private JComboBox<String> comboBox1;
    private JButton setKeysButton;
    private JButton connectButton;
    private JPanel jPanel;
    private JToggleButton tButtonPad1;
    private JToggleButton tButtonPad2;

    private Connection connection;
    private final Set<String> ports = new HashSet<>();

    private final KeyConfigurator keyConfigurator;
    private final Robot robot;

    GUI() throws HeadlessException {
        setContentPane(jPanel);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Sega controls");
        setResizable(false);
        setMinimumSize(new Dimension(877, 430));
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                uiLog("Exit...");

                if (connection != null) {
                    connection.close();
                }
            }
        });

        Robot r;
        try {
            r = new Robot();
        } catch (AWTException e) {
            r = null;
            LOG.log(Level.SEVERE, "Can't create robot.\n" + e.getMessage(), e);
            System.exit(1);
        }
        robot = r;

        keyConfigurator = new KeyConfigurator(this);

        tButtonPad1.addActionListener(e -> {
            if (tButtonPad1.isSelected()) {
                uiLog("Gamepad 1 enabled");
            } else {
                uiLog("Gamepad 1 disabled");
            }
        });

        tButtonPad2.addActionListener(e -> {
            if (tButtonPad2.isSelected()) {
                uiLog("Gamepad 2 enabled");
            } else {
                uiLog("Gamepad 2 disabled");
            }
        });

        connectButton.addActionListener(e -> {
            try {
                if (connection == null) {
                    uiLog("No connection");
                    return;
                }

                if (!connection.isOpen()) {
                    connection.addObserver(this);
                    connection.open();
                    uiLog("Connected");
                } else {
                    uiLog("Connected already");
                }
                SwingUtilities.invokeLater(() -> {
                    try {
                        Thread.sleep(2000L);
                        connection.send(ConnectionImpl.RQ_START);
                    } catch (Exception ignored) {
                    }
                });
            } catch (SerialPortException ex) {
                ex.printStackTrace();
                uiLog("Unexpected error..");
            }
        });

        setKeysButton.addActionListener(e -> keyConfigurator.setVisible(true));
        comboBox1.addActionListener(e -> {
            String selectedPort = (String) comboBox1.getSelectedItem();
            connection = selectedPort == null ? null : new ConnectionImpl(selectedPort);
        });

        Timer portScanTimer = new Timer("Port Scanner");
        portScanTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                scanPorts();
            }
        }, 0, PORT_SCAN_INTERVAL);
    }

    @Override
    public void update(Observable o, Object arg) {
        String className = o.getClass().getName();
        if (className.equals(ConnectionImpl.class.getName())) {

            if (arg.equals(Connection.RPL_STAT)) {
//            connection = (Connection) arg;
//            connection.addObserver(this);
                uiLog("Gamepad connected");
            } else if (arg.equals(Connection.RPL_START)) {
                uiLog("Handle started");
            } else {
                //System.out.print("Button data! Response: " + arg);

                String[] raw;
                int code_1, code_2;
                int i;

                try {
                    if (arg.toString().contains(" ")) {
                        raw = arg.toString().split(" ");

                        if (tButtonPad1.isSelected()) {
                            code_1 = raw[0].isEmpty() ? 0 : Integer.parseInt(raw[0]);
                        } else {
                            code_1 = 0;
                        }

                        if (tButtonPad2.isSelected()) {
                            code_2 = raw[1].isEmpty() ? 0 : Integer.parseInt(raw[1].trim());
                        } else {
                            code_2 = 0;
                        }

                        for (i = 0; i < CODE.length; i++) {
                            if ((code_1 & CODE[i]) == CODE[i]) {
                                robot.keyPress(keyConfigurator.getMap().getKey(i, 0));
//                                System.out.println("Joy 1: press-" + keyConfigurator.getMap().getKey(i, 0));
//                                ((Display) jPanel).log("Joy 1: press-" + keyConfigurator.getMap().getKey(i, 0));
                                ((Display) jPanel).press(0, i);
                            } else if (((Display) jPanel).isPressed(0, i)) {
                                robot.keyRelease(keyConfigurator.getMap().getKey(i, 0));
//                                System.out.println("Joy 1: release-" + keyConfigurator.getMap().getKey(i, 0));
//                                ((Display) jPanel).log("Joy 1: press-" + keyConfigurator.getMap().getKey(i, 0));
                                ((Display) jPanel).release(0, i);
                            }

                            if ((code_2 & CODE[i]) == CODE[i]) {
                                robot.keyPress(keyConfigurator.getMap().getKey(i, 1));
//                                System.out.println("Joy 2: press-" + keyConfigurator.getMap().getKey(i, 1));
//                                ((Display) jPanel).log("Joy 2: press-" + keyConfigurator.getMap().getKey(i, 1));
                                ((Display) jPanel).press(1, i);
                            } else if (((Display) jPanel).isPressed(1, i)) {
                                robot.keyRelease(keyConfigurator.getMap().getKey(i, 1));
//                                System.out.println("Joy 2: release-" + keyConfigurator.getMap().getKey(i, 1));
//                                ((Display) jPanel).log("Joy 2: press-" + keyConfigurator.getMap().getKey(i, 1));
                                ((Display) jPanel).release(1, i);
                            }
                        }
                        jPanel.repaint();
                    }
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void  scanPorts() {
        uiLog("scanning ports...");

        String[] portsFound = SerialPortList
                .getPortNames("/dev/", Pattern.compile("^ttyUSB[0-9]+$"));
        uiLog("ports found: " + Arrays.toString(portsFound));

        for (String port : portsFound) {
            boolean contains = ports.contains(port);
            if (!contains) {
                ports.add(port);
                comboBox1.addItem(port);
            }
        }
    }

    private void uiLog(String msg) {
        LOG.log(Level.INFO, msg);
        ((Display) jPanel).log(msg);
    }

    // There is a custom component creation. It calls in $$$setupUI$$$ method which is generated by GUI Designer
    private void createUIComponents() {
        jPanel = new Display();
    }
}
