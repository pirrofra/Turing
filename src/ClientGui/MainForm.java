package ClientGui;


import Message.MessageBuffer;
import Message.Operation;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

/**
 * This class extends a JFrame and gives the user a graphical interface for sending request to the server
 * The graphical interface consists of a TextBox where information about documents are shown, a TextBox where a log is stored
 * and at the bottom a JPanel from where you can choose different request to send to the server.
 *
 * The method initialize create all UI component and fill the form
 * The method open show the form and start the NotificationListener thread that listen for UDP notification from the server
 * The method open also initialize and show a LogForm
 *
 * @author Francesco Pirrò - Matr.544539
 */
public class MainForm extends JFrame {

    /**
     * RequestExecutor that communicates with the server
     */
    private final RequestExecutor executor;

    /**
     * JTextArea that keeps track of log from the server
     */
    private JTextArea logFromServer;

    /**
     * JTextArea that show various info about Documents
     */
    private JTextArea list;

    /**
     * Button that updates document information at press
     */
    private JButton updateList;

    /**
     * button that logs out at press
     */
    private JButton logout;

    /**
     * Label that show server address
     */
    private JLabel connectionStatus;

    /**
     * UDP port used to listen for notification from server
     */
    private int UDPPort;

    /**
     * Thread that listen for notification
     */
    private NotificationListener thread;

    /**
     * public class constructor
     * @param exec RequestExecutor that communicate with server
     */
    public MainForm(RequestExecutor exec){
        super("Turing Client");
        executor=exec;
        thread=null;
    }

    /**
     * private method that create basics UI Components
     */
    private void createUIComponents(){
        logFromServer=new JTextArea();
        logFromServer.setEditable(false);
        list=new JTextArea();
        list.setEditable(false);
        updateList=new JButton("update");
        JFrame mainFrame=this;
        updateList.addActionListener(e -> {
            //ActionListener for updateList Button
            MessageBuffer result;
            ResultDialog dialog;
            setEnabled(false);
            try {
                result= executor.list(); //send list request to server
                Vector<byte []> args=result.getArgs();
                if(result.getOP()== Operation.OK && args.size()==1){
                    //Operation Successful
                    list.setText(new String(args.get(0))); //JTextArea updated with data received from server
                    dialog=null;
                }
                else
                    dialog=new ResultDialog(mainFrame,result.getOP(),false,false); //operation has failed

            }
            catch (IOException exception){
                dialog=new ResultDialog(mainFrame,"Connection lost with Server",true,false); //connection lost
            }
            setEnabled(true);
            if(dialog!=null) dialog.show(400,100);
        });
        logout=new JButton("Log out");
        MainForm mainForm=this;
        logout.addActionListener(e -> {
            //ActionListener for log out button
            setEnabled(false);
            ResultDialog dialog;
            try{
                MessageBuffer result=executor.logout(); //send logout request to server
                if(result.getOP()==Operation.OK){
                    //if log out is successful, a new LogForm is opened
                    LogForm logForm=new LogForm(mainForm,UDPPort);
                    logForm.initialize();
                    logFromServer.setText("");
                    list.setText("");
                    //reset log and documents info
                    logForm.setVisible(true);
                    dialog=null;
                }
                else dialog=new ResultDialog(mainForm,result.getOP(),true,false);
            }
            catch (IOException exception){
                dialog=new ResultDialog(mainForm,"Connection lost with Server",true,false);
            }
            if(dialog!=null)dialog.show(400,100);
            setEnabled(true);
        });
        try{
            connectionStatus=new JLabel("Connection with " +executor.getRemoteAddress()+" live");
        }
        catch (IOException |NullPointerException e){
            connectionStatus=new JLabel("Connection with server lost");
        }
    }

    /**
     * private method that Fill the form with all UI components
     */
    private void fillForm(){
        JPanel formPanel=new JPanel(new BorderLayout());
        JPanel infoPanel=new JPanel();
        Border padding= BorderFactory.createEmptyBorder(10,10,0,10);
        formPanel.setBorder(padding);
        add(formPanel,BorderLayout.CENTER);
        add(connectionStatus,BorderLayout.SOUTH);
        initializeBoxLayout(infoPanel);
        formPanel.add(infoPanel);
    }


    /**
     * private method that fill the main Panel
     * @param panel to be filled
     */
    private void initializeBoxLayout(JPanel panel){
        JPanel firstLine=new JPanel();
        JScrollPane listPanel=new JScrollPane(list,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); //panel for the document list
        listPanel.setPreferredSize(new Dimension(760,200));
        JScrollPane logPanel=new JScrollPane(logFromServer,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);//panel for the log
        logPanel.setPreferredSize(new Dimension(760,200));
        JPanel secondLine=new JPanel();
        SelectOperation select=new SelectOperation(this); //JPanel that contains graphical components for sending request
        select.initialize();
        firstLine.setLayout(new BoxLayout(firstLine,BoxLayout.LINE_AXIS));
        secondLine.setLayout(new BoxLayout(secondLine,BoxLayout.LINE_AXIS));
        panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
        firstLine.add(new JLabel("Document List:"));
        firstLine.add(Box.createRigidArea(new Dimension(20,0)));
        firstLine.add(updateList);
        firstLine.add(Box.createGlue());
        firstLine.add(logout);
        secondLine.add(new JLabel("Log:"));
        secondLine.add(Box.createGlue());
        panel.add(firstLine);
        panel.add(Box.createRigidArea(new Dimension(0,5)));
        panel.add(listPanel);
        panel.add(Box.createRigidArea(new Dimension(0,15)));
        panel.add(secondLine);
        panel.add(Box.createRigidArea(new Dimension(0,5)));
        panel.add(logPanel);
        panel.add(Box.createRigidArea(new Dimension(0,5)));
        panel.add(select);
    }

    /**
     * method that initialize the entire MainForm
     */
    public void initialize(){
        createUIComponents();
        fillForm();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        setPreferredSize(new Dimension(800,600));
    }

    /**
     * Method that opens a MainForm
     * @throws IOException if an error occurs while opening the DatagramChannel
     */
    public void open() throws IOException{
        setVisible(true);
        pack();
        DatagramChannel channel=DatagramChannel.open();
        channel.socket().bind(new InetSocketAddress(0));
        UDPPort=channel.socket().getLocalPort();
        LogForm login=new LogForm(this,UDPPort);
        login.initialize();
        login.setVisible(true);
        if(thread==null){
            //Start notification thread
            thread=new NotificationListener(channel,this);
            thread.start();
        }
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                //When the windows is closing it interrupts the thread
                super.windowClosing(e);
                thread.interrupt();
            }
        });
    }

    /**
     * Getter for the RequestExecutor
     * @return RequestExecutor
     */
    /*package*/ RequestExecutor getExecutor(){
        return executor;
    }

    /**
     * Method that adds a new string to the log textArea
     * @param log String to be added
     */
    /*package*/ void addLog(String log){
        Date currentTime=new Date(System.currentTimeMillis());
        SimpleDateFormat format=new SimpleDateFormat("[dd/MM hh:mm:ss] - "); //Calculate the current date to add to the message
        String line=format.format(currentTime);
        line+=log+"\n";
        logFromServer.append(line);
        logFromServer.setCaretPosition(logFromServer.getText().length());
    }

    /**
     * Method that updates the document list
     */
    /*package*/ void update(){
        updateList.doClick();
    }

}
