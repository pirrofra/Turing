package ClientGui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * This class extends a JFrame and gives the user a graphical interface for editing the configuration file
 * The graphical interface consists of a series of JTextField from which you can see, and eventually change the values read from the file
 * At the bottom there are two buttons, the OK button makes the client continue with the start-up, the cancel button close it
 *
 * The method initialize create all UI component and fill the form
 * The method open show the form
 * The method connection failed creates and show a JDialog informing the user that it was impossible to connect to the server.
 *
 * @author Francesco Pirrò - Matr.544539
 */
public class ConfigEditor extends JFrame {

    /**
     * Properties object that stores the configuration values
     */
    private Properties config;

    /**
     * lock used to wake the main thread up
     */
    private final Object lock;

    /**
     * Text Field used to edit the value of portTCP
     */
    private JTextField portTCP;

    /**
     * Text Field used to edit the value of dirFiles
     */
    private JTextField dirFiles;

    /**
     * Text Field used to edit the value of portNotifier
     */
    private JTextField portNotifier;

    /**
     * Text Field used to edit the value of serverAddress
     */
    private JTextField serverAddress;

    /**
     * Text Field used to edit the value of portRMI
     */
    private JTextField portRMI;

    /**
     * public class constructor
     * @param c Properties object that stores configuration values
     * @param l Object used to wake the main thread
     */
    public ConfigEditor (Properties c,Object l){
        super("Turing Client");
        config=c;
        lock=l;
        portTCP=new JTextField(config.getProperty("portTCP"));
        dirFiles=new JTextField(config.getProperty("dirFiles"));
        portNotifier=new JTextField(config.getProperty("portNotifier"));
        serverAddress=new JTextField(config.getProperty("serverAddress"));
        portRMI=new JTextField(config.getProperty("portRMI"));
    }

    /**
     * method that initialize all the UI components
     */
    public void initialize(){
        setResizable(false);
        setPreferredSize(new Dimension(600,400));
        Border padding=BorderFactory.createEmptyBorder(5,0,5,0);
        JPanel infoBox=new JPanel();
        infoBox.setLayout(new BoxLayout(infoBox,BoxLayout.Y_AXIS));
        JPanel server=server();
        server.setAlignmentX(infoBox.getAlignmentX());
        JPanel client=client();
        client.setAlignmentX(infoBox.getAlignmentX());
        JPanel buttons=buttons();
        buttons.setAlignmentX(infoBox.getAlignmentX());
        JPanel separator=new JPanel();
        separator.setBorder(padding);
        infoBox.setBorder(padding);
        infoBox.add(server);
        infoBox.add(Box.createVerticalGlue());
        infoBox.add(separator);
        infoBox.add(Box.createVerticalGlue());
        infoBox.add(client);
        infoBox.add(Box.createVerticalGlue());
        infoBox.add(buttons);
        add(infoBox);
    }

    /**
     * public method that make the form show up
     */
    public void open(){
        setVisible(true);
        pack();
    }

    /**
     * private static method that creates a JPanel containing a text field and a label
     * @param label String used for the label
     * @param text JTextField to be added
     * @return new JPanel containing the label and the text field
     */
    private static JPanel textAndLabel(String label, JTextField text ){
        Border padding=BorderFactory.createEmptyBorder(5,20,5,10);
        JPanel panel=new JPanel();
        panel.setBorder(padding);
        panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
        panel.add(new JLabel(label));
        panel.add(text);
        text.setAlignmentX(panel.getAlignmentX());
        return panel;
    }

    /**
     * private method that create a JPanel that contains all the fields related to the server
     * @return a new JPanel
     */
    private JPanel server(){
        JPanel panel=new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
        panel.add(new JLabel("Server:"));
        panel.add(Box.createVerticalGlue());
        panel.add(textAndLabel("Address",serverAddress));
        panel.add(Box.createVerticalGlue());
        panel.add(textAndLabel("TCP Port",portTCP));
        panel.add(Box.createVerticalGlue());
        panel.add(textAndLabel("RMI Port",portRMI));
        return panel;
    }

    /**
     * private method that create a JPanel that contains all the fields related to the client
     * @return a new JPanel
     */
    private JPanel client(){
        JPanel panel=new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
        panel.add(new JLabel("Client:"));
        panel.add(Box.createVerticalGlue());
        panel.add(textAndLabel("Notifier Port",portNotifier));
        panel.add(Box.createVerticalGlue());
        panel.add(textAndLabel("Files Directory",dirFiles));
        return panel;
    }

    /**
     * private method that create a JPanel that contains the two buttons
     * @return a new JPanel
     */
    private JPanel buttons(){
        JPanel panel=new JPanel();
        Border padding=BorderFactory.createEmptyBorder(5,5,5,10);
        panel.setBorder(padding);
        JButton ok=new JButton("OK");
        JButton cancel=new JButton("Cancel");

        cancel.addActionListener(e -> System.exit(0));

        ok.addActionListener(e -> {
            config.setProperty("portTCP",portTCP.getText());
            config.setProperty("dirFiles",dirFiles.getText());
            config.setProperty("portNotifier",portNotifier.getText());
            config.setProperty("serverAddress",serverAddress.getText());
            config.setProperty("portRMI",portRMI.getText());
            try {
                FileOutputStream output=new FileOutputStream("clientConfig.ini",false);
                config.store(output,"");
                output.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            synchronized (lock) {
                setVisible(false);
                lock.notify();
            }
        });
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        panel.setLayout(new BoxLayout(panel,BoxLayout.LINE_AXIS));
        panel.add(cancel);
        panel.add(Box.createHorizontalGlue());
        panel.add(ok);
        return panel;
    }

    /**
     * method that set the form visible again and show a Dialog window informing the user that the connection to the server failed
     */
    public void connectionFailed(){
        setVisible(true);
        ResultDialog dialog=new ResultDialog(this,"Connection to the server failed",true,true);
        dialog.show(400,100);
    }

}
