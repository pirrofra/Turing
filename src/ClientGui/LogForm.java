package ClientGui;


import Message.MessageBuffer;
import Message.Operation;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

public class LogForm {

    private RequestExecutor executor;
    private JFrame form;
    private JTextField username;
    private JPasswordField password;
    private JButton login;
    private JButton register;
    private JLabel connectionStatus;


    public LogForm(RequestExecutor exec) {
        executor=exec;
        form=new JFrame("Turing Client");
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

    //TODO:THIS
    private void addButtonListener(){
        login.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                form.disable();
                String pass=new String(password.getPassword());
                ResultDialog dialog;
                try{
                    MessageBuffer result=executor.login(username.getText(),pass);
                    dialog=new ResultDialog(form,result.getOP(),false);
                }
                catch (IOException exception){
                    dialog=new ResultDialog(form,"Connection lost with Server",true);
                }
                form.enable();
                dialog.show(400,100);
            }
        });

        register.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                form.disable();
                String pass=new String(password.getPassword());
                ResultDialog dialog;
                try{
                    Operation result=executor.register(username.getText(),pass);
                    dialog=new ResultDialog(form,result,false);
                }
                catch (IOException exception){
                    dialog=new ResultDialog(form,"Connection lost with Server",true);
                }
                form.enable();
                dialog.show(400,100);
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
        form.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        form.setResizable(false);
        form.setSize(600,150);
    }

    public void show(){
        form.setVisible(true);
    }

    public static void main(String[]args) throws IOException{
        SocketChannel channel=SocketChannel.open();
        InetAddress addr= InetAddress.getByName("localhost");
        SocketAddress addr2=new InetSocketAddress(addr,55432);
        channel.connect(addr2);
        RequestExecutor exec=new RequestExecutor(channel,"localhost",55431);
        LogForm log=new LogForm(exec);
        log.initialize();
        log.show();
    }

}
