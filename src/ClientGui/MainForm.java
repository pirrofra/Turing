package ClientGui;


import Message.MessageBuffer;
import Message.Operation;
import RemoteClientNotifier.RemoteClientNotifier;
import RequestExecutor.RequestExecutor;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
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

    private final int RMIport;

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

    private ClientNotifier notifier;
    private Registry reg;

    /**
     * public class constructor
     * @param exec RequestExecutor that communicate with server
     */
    public MainForm(RequestExecutor exec, int port){
        super("Turing Client");
        executor=exec;
        notifier =null;
        RMIport=port;
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
                    LogForm logForm=new LogForm(mainForm,RMIport);
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
     * @throws RemoteException if an error occurs while exporting the RemoteClientNotifier
     */
    public void open() throws RemoteException {
        setVisible(true);
        pack();
        if (notifier ==null){
            notifier =new ClientNotifier(this);
            RemoteClientNotifier stub= (RemoteClientNotifier) UnicastRemoteObject.exportObject(notifier,0);
            try{
                LocateRegistry.createRegistry(RMIport);
            }
            catch (ExportException e){
                String msg="Port "+RMIport+" cannot be used for the Notifier. Try again with a new port";
                ResultDialog dialog=new ResultDialog(this,msg,true,true);
                dialog.show(400,100);
                return;
            }

            reg=LocateRegistry.getRegistry(RMIport);
            reg.rebind("NOTIFIER-TURING",stub);
        }

        LogForm login=new LogForm(this,RMIport);
        login.initialize();
        login.setVisible(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                //When the windows is closing it interrupts the thread
                super.windowClosing(e);
                try{
                    UnicastRemoteObject.unexportObject(reg,false);
                }
                catch (NoSuchObjectException ex){
                    //do nothing
                }

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
