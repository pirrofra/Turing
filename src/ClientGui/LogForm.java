package ClientGui;


import Message.MessageBuffer;
import Message.Operation;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

/*package*/ class LogForm extends JDialog{

    private final RequestExecutor executor;
    private JTextField username;
    private JPasswordField password;
    private JButton login;
    private JButton register;
    private JLabel connectionStatus;
    private boolean loggedIn;
    private final MainForm main;


    /*package*/ LogForm(MainForm father) {
        super(father,"Turing Client",true);
        executor=father.getExecutor();
        main=father;
        loggedIn=false;
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                if(!loggedIn)System.exit(0);
            }
        });
    }

    private void createUIComponents() {
        try{
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

    private void fillForm(){
        JPanel formPanel=new JPanel(new BorderLayout());
        JPanel dataPanel=new JPanel();
        JPanel buttonPanel=new JPanel();
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

    private void addButtonListener(){
        JDialog me=this;
        login.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setEnabled(false);
                String pass=new String(password.getPassword());
                ResultDialog dialog;
                try{
                    MessageBuffer result=executor.login(username.getText(),pass);
                    if(result.getOP()==Operation.OK){
                        dialog=new ResultDialog(me,"Log in is successful!",false,true);
                        loggedIn=true;
                        main.update();
                    }
                    else dialog=new ResultDialog(me,result.getOP(),false,false);
                }
                catch (IOException exception){
                    dialog=new ResultDialog(me,"Connection lost with Server",true,false);
                }
                setEnabled(true);
                dialog.show(400,100);
            }
        });

        register.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setEnabled(false);
                String pass=new String(password.getPassword());
                ResultDialog dialog;
                try{
                    Operation result=executor.register(username.getText(),pass);
                    if(result==Operation.OK) login.doClick();
                    else {
                        dialog=new ResultDialog(me,result,false,false);
                        dialog.show(400,100);
                    }
                }
                catch (IOException exception){
                    dialog=new ResultDialog(me,"Connection lost with Server",true,false);
                    dialog.show(400,100);
                }
                setEnabled(true);

            }
        });

    }

    private static void addWhiteCell(JPanel panel, int num){
        for(int i=0;i<num;i++){
            panel.add(new JPanel());
        }
    }

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
