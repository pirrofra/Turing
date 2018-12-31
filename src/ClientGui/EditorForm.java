package ClientGui;

import ChatRoom.ChatRoom;
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
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class EditorForm {

    private final JDialog form;
    private final MainForm mainFrame;
    private final Path filePath;
    private final RequestExecutor executor;
    private final JTextArea chatBox;
    private final ChatRoom chat;
    private final String docName;
    private final int section;
    private boolean isEditing;

    public EditorForm(MainForm father,String doc,int numSect,Path file,String group,int port){
        mainFrame=father;
        filePath=file;
        executor=father.getExecutor();
        chat=null;
        chatBox=new JTextArea();
        chatBox.setEditable(false);
        form=new JDialog(father.getMainFrame(),"Turing Client",true);
        docName=doc;
        section=numSect;
        isEditing=true;
        form.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                if(isEditing)System.exit(0);
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
        form.add(content,BorderLayout.CENTER);
        form.add(connectionStatus,BorderLayout.SOUTH);


    }

    private JPanel initializeFileInfo(){
        JPanel firstLine=new JPanel();
        firstLine.setLayout(new BoxLayout(firstLine,BoxLayout.LINE_AXIS));
        firstLine.add(new JLabel("Currently Editing file: "+filePath.toString()));
        firstLine.add(Box.createHorizontalGlue());
        JButton end=new JButton("End Edit");
        firstLine.add(end);
        end.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                form.setEnabled(false);
                ResultDialog dialog;
                try{
                    ByteBuffer file=openFile(filePath);
                    MessageBuffer result=executor.endEdit(docName,section,file.array());
                    if(result.getOP()== Operation.OK)
                        isEditing=false;
                    dialog=new ResultDialog(form,result.getOP(),false,true);

                }
                catch (IOException exc){
                    dialog=new ResultDialog(form,"Error while opening file. Retry",false,false);
                }
                form.setEnabled(true);
                dialog.show(400,100);
            }
        });
        return firstLine;
    }

    private JPanel initializeChatBox(){
        JPanel chatBox=new JPanel();
        chatBox.setLayout(new BoxLayout(chatBox,BoxLayout.PAGE_AXIS));
        chatBox.add(scrollableChatText());
        chatBox.add(Box.createVerticalGlue());
        chatBox.add(sendMessageBox());
        return chatBox;
    }

    private JScrollPane scrollableChatText(){
        JScrollPane scrollableChat= new JScrollPane(chatBox,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollableChat.setPreferredSize(new Dimension(780,500));
        return scrollableChat;
    }

    //TODO: ADD BUTTON EVENT
    private JPanel sendMessageBox(){
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

            }
        });
        return messageLine;
    }

    public void show(){
        fillForm();
        startChat();
        form.setPreferredSize(new Dimension(800,600));
        form.pack();
        form.setLocationRelativeTo(mainFrame.getMainFrame());
        form.setVisible(true);
    }

    private void startChat(){

    }

    private static ByteBuffer openFile(Path path) throws IOException{
        FileChannel file=FileChannel.open(path, StandardOpenOption.READ);
        int size=(int)file.size();
        ByteBuffer buffer=ByteBuffer.allocate(size);
        while (size>0){
            int byteRead=file.read(buffer);
            if(byteRead<0) throw  new IOException();
            size-=byteRead;
        }
        buffer.flip();
        return buffer;
    }


}
