package ClientGui;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.channels.SocketChannel;

public class LogForm {

    private SocketChannel server;
    private JFrame form;
    private JTextField username;
    private JPasswordField password;
    private JButton login;
    private JButton register;
    private JLabel connectionStatus;


    public LogForm(SocketChannel socket) throws IOException {
        server=socket;
        form=new JFrame("Turing Client");
    }

    private void createUIComponents() {
        try{
            connectionStatus=new JLabel("Connection with" +server.getRemoteAddress().toString()+" live");
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
        form.add(formPanel,BorderLayout.CENTER);
        formPanel.add(connectionStatus,BorderLayout.SOUTH);
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

    private static void addWhiteCell(JPanel panel, int num){
        for(int i=0;i<num;i++){
            panel.add(new JPanel());
        }
    }

    public void initialize(){
        createUIComponents();
        fillForm();
        form.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        form.setResizable(false);
        form.setSize(600,150);
    }

    public void show(){
        form.setVisible(true);
    }

    public static void main(String[]args) throws IOException{
        SocketChannel sock=null;
        LogForm log=new LogForm(sock);
        log.initialize();
        log.show();
    }

}
