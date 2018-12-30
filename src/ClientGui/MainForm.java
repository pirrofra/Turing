package ClientGui;


import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

public class MainForm {

    private RequestExecutor executor;
    private JFrame form;
    private JTextArea logFromServer;
    private JTextArea list;
    private JButton updateList;
    private JButton logout;
    private JLabel connectionStatus;

    public MainForm(RequestExecutor exec){
        executor=exec;
        form=new JFrame("Turing Client");
    }

    private void createUIComponents(){
        logFromServer=new JTextArea();
        logFromServer.setEditable(false);
        list=new JTextArea();
        list.setEditable(false);
        updateList=new JButton("update");
        logout=new JButton("Log out");
        try{
            connectionStatus=new JLabel("Connection with " +executor.getRemoteAddress()+" live");
        }
        catch (IOException |NullPointerException e){
            connectionStatus=new JLabel("Connection with server lost");
        }
    }

    private void fillForm(){
        JPanel formPanel=new JPanel(new BorderLayout());
        JPanel infoPanel=new JPanel();
        Border padding= BorderFactory.createEmptyBorder(10,10,0,10);
        formPanel.setBorder(padding);
        form.add(formPanel,BorderLayout.CENTER);
        form.add(connectionStatus,BorderLayout.SOUTH);
        initializeBoxLayout(infoPanel);
        formPanel.add(infoPanel);


    }

    private void initializeBoxLayout(JPanel panel){
        JPanel firstLine=new JPanel();
        JPanel secondLine=new JPanel();
        SelectOperation select=new SelectOperation(this);
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
        panel.add(list);
        panel.add(Box.createRigidArea(new Dimension(0,15)));
        panel.add(secondLine);
        panel.add(Box.createRigidArea(new Dimension(0,5)));
        panel.add(logFromServer);
        panel.add(Box.createRigidArea(new Dimension(0,5)));
        panel.add(select);
    }

    public void initialize(){
        createUIComponents();
        fillForm();
        form.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        form.setResizable(false);
        form.setPreferredSize(new Dimension(800,600));
    }

    public void show(){
        form.pack();
        form.show();
    }

    public void addLog(String log){
        logFromServer.append(log);
    }

    public String getPath(){
        return executor.getFilePath();
    }


    public static void main(String[] args) throws IOException{
        SocketChannel channel=SocketChannel.open();
        InetAddress addr= InetAddress.getByName("localhost");
        SocketAddress addr2=new InetSocketAddress(addr,55432);
        channel.connect(addr2);
        RequestExecutor exec=new RequestExecutor(channel,"localhost",55431,"files/");
        MainForm log=new MainForm(exec);
        log.initialize();
        log.show();
    }

    public void disable(){
        form.disable();
    }

    public void enable(){
        form.enable();
    }
}
