package ClientGui;

import ChatRoom.ChatRoom;
import Message.MessageBuffer;
import Message.Operation;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Vector;

/*package*/ class EditorForm extends JDialog{

    private final MainForm mainFrame;
    private final Path filePath;
    private final RequestExecutor executor;
    private final JTextArea chatBox;
    private  ChatRoom chat;
    private final String docName;
    private final int section;
    private boolean isEditing;

    /*package*/ EditorForm(MainForm father,String doc,int numSect,Path file){
        super(father,"Turing Client",true);
        mainFrame=father;
        filePath=file;
        executor=father.getExecutor();
        chat=null;
        chatBox=new JTextArea();
        chatBox.setEditable(false);
        docName=doc;
        section=numSect;
        isEditing=true;
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                if(isEditing){
                    ResultDialog dialog=new ResultDialog(mainFrame,"Editing interrupted. Closing APP",true,false);
                    dialog.show(400,100);
                }
                chat.interrupt();
            }
        });
    }

    private void fillForm(){
        JLabel connectionStatus;
        try{
            connectionStatus=new JLabel("Connection with " +executor.getRemoteAddress()+" live");
        }
        catch (IOException |NullPointerException e){
            connectionStatus=new JLabel("Connection with server lost");
        }
        JPanel content=new JPanel();
        content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));
        Border padding=BorderFactory.createEmptyBorder(10,10,0,10);
        content.setBorder(padding);
        content.add(initializeFileInfo());
        content.add(Box.createRigidArea(new Dimension(0,5)));
        content.add(initializeChatBox());
        add(content,BorderLayout.CENTER);
        add(connectionStatus,BorderLayout.SOUTH);


    }

    private JPanel initializeFileInfo(){
        JDialog me=this;
        JPanel firstLine=new JPanel();
        firstLine.setLayout(new BoxLayout(firstLine,BoxLayout.LINE_AXIS));
        firstLine.add(new JLabel("Currently Editing file: "+filePath.toString()));
        firstLine.add(Box.createHorizontalGlue());
        JButton end=new JButton("End Edit");
        firstLine.add(end);
        end.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setEnabled(false);
                ResultDialog dialog;
                try{
                    ByteBuffer file=readFile(filePath);
                    if (file==null) {
                        dialog=new ResultDialog(me,"Error while opening file. Retry",false,false);
                    }
                    else{
                        MessageBuffer result=executor.endEdit(docName,section,file.array());
                        if(result.getOP()== Operation.OK)
                            isEditing=false;
                        dialog=new ResultDialog(me,result.getOP(),false,true);

                    }
                }
                catch (IOException exception) {
                    exception.printStackTrace();
                    dialog = new ResultDialog(mainFrame, "Connection lost with Server", true, false);
                }

                setEnabled(true);
                dialog.show(400,100);
            }
        });
        return firstLine;
    }

    private JPanel initializeChatBox(){
        JPanel chatPanel=new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel,BoxLayout.PAGE_AXIS));
        chatPanel.add(scrollableChatText());
        chatPanel.add(Box.createVerticalGlue());
        chatPanel.add(sendMessageBox());
        return chatPanel;
    }

    private JScrollPane scrollableChatText(){
        JScrollPane scrollableChat= new JScrollPane(chatBox,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollableChat.setPreferredSize(new Dimension(780,500));
        return scrollableChat;
    }

    private JPanel sendMessageBox(){
        JDialog me=this;
        JPanel messageLine=new JPanel();
        JTextField mex=new JTextField();
        JButton send=new JButton("Send");
        messageLine.setLayout(new BoxLayout(messageLine,BoxLayout.LINE_AXIS));
        messageLine.add(mex);
        messageLine.add(Box.createHorizontalGlue());
        messageLine.add(send);
        send.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    String msg=mex.getText();
                    if(msg.length()>1024){
                        ResultDialog dialog=new ResultDialog(me,"Message is too long",false,false);
                        dialog.show(400,100);
                    }
                    else chat.sendMessage(msg);
                }
                catch (IOException exception){
                    ResultDialog dialog=new ResultDialog(me,"Message couldn't be sent",false,false);
                    dialog.show(400,100);
                }
                mex.setText("");
            }
        });
        EnterListener listener=new EnterListener(send);
        mex.addKeyListener(listener);
        return messageLine;
    }

    /*package*/ void initialize(){
        fillForm();
        startChat();
    }

    /*package*/ void open(){
        setPreferredSize(new Dimension(800,600));
        pack();
        setLocationRelativeTo(mainFrame);
        setVisible(true);
    }

    private void startChat(){
        MessageBuffer result;
        try{
            result=executor.chatRoom(docName);
            Vector<byte[]> Args=result.getArgs();
            if(result.getOP()==Operation.OK){
                String address=new String(Args.get(0));
                int port=ByteBuffer.wrap(Args.get(1)).getInt();
                String user=new String(Args.get(2));
                chat=new ChatRoom(address,port,chatBox,user);
                chatBox.append("Chat on "+address+" started\n");
                chat.start();
            }
            else {
                chatBox.append(Operation.getDescription(result.getOP()));
            }
        }
        catch (IOException e){
            ResultDialog dialog=new ResultDialog(this,"Error while opening chat",false,false);
            dialog.show(400,100);
            chatBox.append("--- CHAT CRASHED ---\n");
        }
    }

    private static ByteBuffer readFile(Path path){
        try{
            FileChannel file=FileChannel.open(path, StandardOpenOption.READ);
            int sizeFile=(int)file.size();
            ByteBuffer buffer=ByteBuffer.allocate(sizeFile);
            while (sizeFile>0){
                int byteRead=file.read(buffer);
                if(byteRead<0) throw  new IOException();
                sizeFile-=byteRead;
            }
            buffer.flip();
            return buffer;
        }
        catch (IOException e){
            return null;
        }
    }

}
