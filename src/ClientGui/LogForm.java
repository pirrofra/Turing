package ClientGui;


import Message.MessageBuffer;
import Message.Operation;
import RequestExecutor.RequestExecutor;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

/**
 * This class extends a JDialog and gives the user a graphical interface for the login/sign up procedure
 * The graphical interface consist of two text box for username and password and two button for login and sign up
 *
 * the method initialize create all UI Components and add  buttons listener
 *
 * @author Francesco Pirrò - Matr.544539
 */
/*package*/ class LogForm extends JDialog{

    /**
     * RequestExecutor used to communicate with the server
     */
    private final RequestExecutor executor;

    /**
     * JTextField that contains the username
     */
    private JTextField username;

    /**
     * JPasswordField that contains the password
     */
    private JPasswordField password;

    /**
     * JButton that send a login request at press
     */
    private JButton login;

    /**
     * JButton that send a sign up request at press
     */
    private JButton register;

    /**
     * JLabel that show connection status with server
     */
    private JLabel connectionStatus;

    /**
     * boolean that keeps track if the user has logged in, or not
     */
    private boolean loggedIn;

    /**
     * MainForm that created the login form
     */
    private final MainForm main;

    private final int RMIport;
    /**
     * class constructor
     * @param father MainForm that created the LogForm
     */
    /*package*/ LogForm(MainForm father, int port) {
        super(father,"Turing Client",true);
        executor=father.getExecutor();
        main=father;
        RMIport=port;
        loggedIn=false;
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                //Event when windows is closed
                super.windowClosed(e);
                if(!loggedIn)System.exit(0); //If the user is not logged in, the app is closed
            }
        });
    }

    /**
     * private method that creates basics UI components
     */
    private void createUIComponents() {
        try{
            //update connectionStatus with server address
            connectionStatus=new JLabel("Connection with " +executor.getRemoteAddress()+" live");
        }
        catch (IOException |NullPointerException e){
            connectionStatus=new JLabel("Connection with server lost");
        }
        username=new JTextField();
        password=new JPasswordField();
        login=new JButton("Log In");
        register=new JButton("Sign Up");
    }

    /**
     * Private method that fill the form
     */
    private void fillForm(){
        JPanel formPanel=new JPanel(new BorderLayout());
        JPanel dataPanel=new JPanel(); //panel that contains username and password
        JPanel buttonPanel=new JPanel(); //Panel that contains buttons
        Border padding=BorderFactory.createEmptyBorder(0,10,0,10);
        formPanel.setBorder(padding);
        add(formPanel,BorderLayout.CENTER);
        add(connectionStatus,BorderLayout.SOUTH);
        formPanel.add(dataPanel,BorderLayout.CENTER);
        formPanel.add(buttonPanel, BorderLayout.EAST);
        dataPanel.setLayout(new GridLayout(4,1));
        dataPanel.add(new JLabel("username"));
        dataPanel.add(username);
        dataPanel.add(new JLabel("password"));
        dataPanel.add(password);
        buttonPanel.setLayout(new GridLayout(4,2,0,0));
        addWhiteCell(buttonPanel,3);
        buttonPanel.add(register);
        addWhiteCell(buttonPanel,3);
        buttonPanel.add(login);
    }

    /**
     * Private method that adds button listener
     */
    private void addButtonListener(){
        EnterListener listener=new EnterListener(login); //key listener that press a button when enter is pressed
        JDialog me=this;
        login.addActionListener(e -> {
            //login action listener
            setEnabled(false); //when the button is pressed, the form is disabled until the operation is completed
            String pass=new String(password.getPassword());
            ResultDialog dialog;
            try{
                MessageBuffer result=executor.login(username.getText(),pass,RMIport); //send login request to the server
                if(result.getOP()==Operation.OK){
                    dialog=new ResultDialog(me,"Log in is successful!",false,true);
                    loggedIn=true;
                    main.update(); //update mainForm document list
                }
                else dialog=new ResultDialog(me,result.getOP(),false,false); //error received
            }
            catch (IOException exception){
                dialog=new ResultDialog(me,"Connection lost with Server",true,false); //connection lost
            }
            setEnabled(true);
            dialog.show(400,100);
        });

        register.addActionListener(e -> {
            //sign up action listener
            setEnabled(false); //when the button is pressed, the form is disabled until the operation is completed
            String pass=new String(password.getPassword());
            ResultDialog dialog;
            try{
                Operation result=executor.register(username.getText(),pass); //sign up operation requested
                if(result==Operation.OK) login.doClick(); //if sign up is successful, a login operation is requested
                else {
                    dialog=new ResultDialog(me,result,false,false); //error has occurred
                    dialog.show(400,100);
                }
            }
            catch (IOException exception){
                dialog=new ResultDialog(me,"Connection lost with Server",true,false);
                dialog.show(400,100);
            }
            setEnabled(true);

        });

        username.addKeyListener(listener);
        password.addKeyListener(listener);

    }

    /**
     * static method that add white cell in a panel with grid layout
     * @param panel JPanel with grid layout
     * @param num number of white cell to add
     */
    private static void addWhiteCell(JPanel panel, int num){
        for(int i=0;i<num;i++){
            panel.add(new JPanel());
        }
    }

    /**
     * method that initialize form
     */
    /*package*/ void initialize(){
        createUIComponents();
        fillForm();
        addButtonListener();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        setPreferredSize(new Dimension(600,150));
        pack();
        setLocationRelativeTo(main);
    }

}
