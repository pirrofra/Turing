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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.Vector;

public class MainForm {

    private final RequestExecutor executor;
    private final JFrame form;
    private JTextArea logFromServer;
    private JTextArea list;
    private JButton updateList;
    private JButton logout;
    private JLabel connectionStatus;
    private final int chatPort;

    public MainForm(RequestExecutor exec,int port){
        executor=exec;
        form=new JFrame("Turing Client");
        chatPort=port;
    }


    private void createUIComponents(){
        logFromServer=new JTextArea();
        logFromServer.setEditable(false);
        list=new JTextArea();
        list.setEditable(false);
        updateList=new JButton("update");
        updateList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MessageBuffer result;
                ResultDialog dialog;
                disable();
                try {
                    result= executor.list();
                    Vector<byte []> args=result.getArgs();
                    if(result.getOP()== Operation.OK && args.size()==1){
                        list.setText(new String(args.get(0)));
                        enable();
                        return;
                    }
                    else
                        dialog=new ResultDialog(form,result.getOP(),false,false);

                }
                catch (IOException exception){
                    dialog=new ResultDialog(form,"Connection lost with Server",true,false);
                }
                enable();
                dialog.show(400,100);
            }
        });
        logout=new JButton("Log out");
        MainForm mainForm=this;
        logout.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                disable();
                ResultDialog dialog;
                try{
                    MessageBuffer result=executor.logout();
                    if(result.getOP()==Operation.OK){
                        LogForm logForm=new LogForm(mainForm);
                        logForm.initialize();
                        logFromServer.setText("");
                        list.setText("");
                        logForm.show();
                        dialog=null;
                    }
                    else dialog=new ResultDialog(form,result.getOP(),true,false);
                }
                catch (IOException exception){
                    dialog=new ResultDialog(form,"Connection lost with Server",true,false);
                }
                if(dialog!=null)dialog.show();
                enable();
            }
        });
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
        JScrollPane listPanel=new JScrollPane(list,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        listPanel.setPreferredSize(new Dimension(760,200));
        JScrollPane logPanel=new JScrollPane(logFromServer,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        logPanel.setPreferredSize(new Dimension(760,200));
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
        panel.add(listPanel);
        panel.add(Box.createRigidArea(new Dimension(0,15)));
        panel.add(secondLine);
        panel.add(Box.createRigidArea(new Dimension(0,5)));
        panel.add(logPanel);
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
        form.setVisible(true);
        LogForm login=new LogForm(this);
        login.initialize();
        login.show();
    }

    public RequestExecutor getExecutor(){
        return executor;
    }

    public JFrame getMainFrame(){
        return form;
    }

    public void addLog(String log){
        Date currentTime=new Date(System.currentTimeMillis());
        SimpleDateFormat format=new SimpleDateFormat("[dd/MM hh:mm:ss] - ");
        String line=format.format(currentTime);
        line+=log+"\n";
        logFromServer.append(line);
    }


    public static void main(String[] args) throws IOException{
        SocketChannel channel=SocketChannel.open();
        InetAddress addr= InetAddress.getByName("localhost");
        SocketAddress addr2=new InetSocketAddress(addr,55432);
        channel.connect(addr2);
        RequestExecutor exec=new RequestExecutor(channel,"localhost",55431,"filesClient/");
        Scanner in=new Scanner(System.in);
        int port=in.nextInt();
        MainForm log=new MainForm(exec,port);
        log.initialize();
        log.show();
    }

    public void disable(){
        form.setEnabled(false);
    }

    public void enable(){
        form.setEnabled(true);
    }

    public void update(){
        updateList.doClick();
    }

    public int getPort(){
        return chatPort;
    }
}
