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

public class LogForm {

    private RequestExecutor executor;
    private JDialog form;
    private JTextField username;
    private JPasswordField password;
    private JButton login;
    private JButton register;
    private JLabel connectionStatus;
    private JFrame mainFrame;
    private boolean loggedIn;
    private MainForm main;


    public LogForm(MainForm father) {
        executor=father.getExecutor();
        mainFrame=father.getMainFrame();
        main=father;
        loggedIn=false;
        form=new JDialog(mainFrame,"Turing Client",true);
        form.addWindowListener(new WindowAdapter() {
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
        form.add(formPanel,BorderLayout.CENTER);
        form.add(connectionStatus,BorderLayout.SOUTH);
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
        login.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                form.setEnabled(false);
                String pass=new String(password.getPassword());
                ResultDialog dialog;
                try{
                    MessageBuffer result=executor.login(username.getText(),pass);
                    if(result.getOP()==Operation.OK){
                        dialog=new ResultDialog(form,"Log in is successful!",false,true);
                        loggedIn=true;
                        main.update();
                    }
                    else dialog=new ResultDialog(form,result.getOP(),false,false);
                }
                catch (IOException exception){
                    dialog=new ResultDialog(form,"Connection lost with Server",true,false);
                }
                form.setEnabled(true);
                dialog.show(400,100);
            }
        });

        register.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                form.setEnabled(false);
                String pass=new String(password.getPassword());
                ResultDialog dialog;
                try{
                    Operation result=executor.register(username.getText(),pass);
                    if(result==Operation.OK) login.doClick();
                    else {
                        dialog=new ResultDialog(form,result,false,false);
                        dialog.show(400,100);
                    }
                }
                catch (IOException exception){
                    dialog=new ResultDialog(form,"Connection lost with Server",true,false);
                    dialog.show(400,100);
                }
                form.setEnabled(true);

            }
        });

    }

    private static void addWhiteCell(JPanel panel, int num){
        for(int i=0;i<num;i++){
            panel.add(new JPanel());
        }
    }

    public void initialize(){
        createUIComponents();
        fillForm();
        addButtonListener();
        form.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        form.setResizable(false);
        form.setPreferredSize(new Dimension(600,150));
        form.pack();
        form.setLocationRelativeTo(mainFrame);
    }

    public void show(){
        form.setVisible(true);
    }

}
