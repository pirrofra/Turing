package ClientGui;

import ChatRoom.ChatRoom;
import Message.MessageBuffer;
import Message.Operation;
import RequestExecutor.RequestExecutor;

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

/**
 * This class extends a JDialog and gives the user a graphical interface while he's currently editing a document
 * The graphical interface consists of a chat box, a button to end editing process and a button to send a message
 *
 * When the JDialog is initialized it also star a ChatRoom thread that listen for messages on a Multicast Address given by the server
 * When the JDialog is closing, the chatRoom thread is interrupted
 *
 * the method initialize creates all UIComponent and start the thread,
 * the method open sets dimensions and shows the JDialog
 *
 * @author Francesco Pirrò - Matr.544539
 */
/*package*/ class EditorForm extends JDialog{

    /**
     * MainForm that opened the EditorForm
     */
    private final MainForm mainFrame;

    /**
     * Path of the file currently editing
     */
    private final Path filePath;

    /**
     * RequestExecutor used to communicate with the server
     */
    private final RequestExecutor executor;

    /**
     * JTextArea that functions as a chatBox
     */
    private final JTextArea chatBox;

    /**
     * ChatRoom Thread that listens for messages
     */
    private  ChatRoom chat;

    /**
     * name of the document currently editing
     */
    private final String docName;

    /**
     * section of the document currently editing
     */
    private final int section;

    /**
     * boolean that keeps track if the user is currently editing or not
     */
    private boolean isEditing;


    /**
     * public class constructor
     * @param father MainForm that generated the EditorForm
     * @param doc document currently editing
     * @param numSect section currently editing
     * @param file Path that links to the file currently editing
     */
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
                //Windows Listener that close the entire client if the user hasn't stopped editing
                super.windowClosing(e);
                if(isEditing){
                    ResultDialog dialog=new ResultDialog(mainFrame,"Editing interrupted. Closing APP",true,false);
                    dialog.show(400,100);
                }
                chat.interrupt();
            }
        });
    }

    /**
     * private method that fill the entire Form
     */
    private void fillForm(){
        JLabel connectionStatus; //Label that show connection with the server
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
        content.add(initializeFileInfo()); //Information about file currently editing are added
        content.add(Box.createRigidArea(new Dimension(0,5)));
        content.add(initializeChatBox()); //ChatBox is added
        add(content,BorderLayout.CENTER);
        add(connectionStatus,BorderLayout.SOUTH);

    }

    /**
     * Private Method that create the panel that contains information about file currently editing
     * @return JPanel that contains file informations
     */
    private JPanel initializeFileInfo(){
        JDialog me=this;
        JPanel firstLine=new JPanel();
        firstLine.setLayout(new BoxLayout(firstLine,BoxLayout.LINE_AXIS));
        firstLine.add(new JLabel("Currently Editing file: "+filePath.toString())); //Location of the file is added to the panel
        firstLine.add(Box.createHorizontalGlue());
        JButton end=new JButton("End Edit");
        firstLine.add(end);
        //end edit button listener
        end.addActionListener(e -> {
            setEnabled(false); //When the operation is executing the entire form is disabled
            ResultDialog dialog;
            try{
                ByteBuffer file=readFile(filePath); //File is read
                if (file==null) {
                    dialog=new ResultDialog(me,"Error while opening file. Retry",false,false);
                }
                else{
                    MessageBuffer result=executor.endEdit(docName,section,file.array()); //file is sent to the server
                    if(result.getOP()== Operation.OK)
                        isEditing=false; //If the operation is successful, the user has stopped editing this file
                    dialog=new ResultDialog(me,result.getOP(),false,true);

                }
            }
            catch (IOException exception) {
                exception.printStackTrace();
                dialog = new ResultDialog(mainFrame, "Connection lost with Server", true, false);
            }

            setEnabled(true);
            dialog.show(400,100);
        });


        return firstLine;
    }

    /**
     * private method that create a panel that contains the chatBox
     * @return JPanel that contains the chatBox
     */
    private JPanel initializeChatBox(){
        JPanel chatPanel=new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel,BoxLayout.PAGE_AXIS));
        chatPanel.add(scrollableChatText());
        chatPanel.add(Box.createVerticalGlue());
        chatPanel.add(sendMessageBox());
        return chatPanel;
    }

    /**
     * private method that create a JScrollPane that contains the chat text
     * @return JScrollPane that contains the chat text
     */
    private JScrollPane scrollableChatText(){
        JScrollPane scrollableChat= new JScrollPane(chatBox,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollableChat.setPreferredSize(new Dimension(780,500));
        return scrollableChat;
    }

    /**
     * private method that create a JPanel that contains the message box
     * @return JPanel that creates the message box
     */
    private JPanel sendMessageBox(){
        JDialog me=this;
        JPanel messageLine=new JPanel();
        JTextField mex=new JTextField();
        JButton send=new JButton("Send");
        messageLine.setLayout(new BoxLayout(messageLine,BoxLayout.LINE_AXIS));
        messageLine.add(mex); //message that needs to be sent
        messageLine.add(Box.createHorizontalGlue());
        messageLine.add(send); //button to sent
        //ActionLister for the send button
        send.addActionListener(e -> {
            try{
                String msg=mex.getText();
                if(msg.length()>1024){
                    //If the message is too long, it returns an error
                    ResultDialog dialog=new ResultDialog(me,"Message is too long",false,false);
                    dialog.show(400,100);
                }
                else chat.sendMessage(msg); //send message
            }
            catch (IOException exception){
                ResultDialog dialog=new ResultDialog(me,"Message couldn't be sent",false,false);
                dialog.show(400,100);
            }
            mex.setText(""); //reset JTextField
        });
        EnterListener listener=new EnterListener(send); //KeyListener that press the send button when the enter key is pressed
        mex.addKeyListener(listener);
        return messageLine;
    }

    /**
     * method that initialize the editor form
     */
    /*package*/ void initialize(){
        fillForm();
        startChat();
    }

    /**
     * Method that set the form as visible and set the size
     */
    /*package*/ void open(){
        setPreferredSize(new Dimension(800,600));
        pack();
        setLocationRelativeTo(mainFrame);
        setVisible(true);
    }

    /**
     * Method that start the chatRoom Thread
     */
    private void startChat(){
        MessageBuffer result;
        try{
            result=executor.chatRoom(docName); //Ask the server for the multicast address
            Vector<byte[]> Args=result.getArgs();
            if(result.getOP()==Operation.OK){
                String address=new String(Args.get(0));//multicast address
                int port=ByteBuffer.wrap(Args.get(1)).getInt(); //port for the multicast group
                String user=new String(Args.get(2)); //Username
                chat=new ChatRoom(address,port,chatBox,user); //Thread is created
                chatBox.append("Chat on "+address+" started\n");
                chat.start(); //Thread started
            }
            else {
                //Operation has failed, chat couldn't start
                chatBox.append(Operation.getDescription(result.getOP()));
            }
        }
        catch (IOException e){
            //Chat couldn't start
            ResultDialog dialog=new ResultDialog(this,"Error while opening chat",false,false);
            dialog.show(400,100);
            chatBox.append("--- CHAT CRASHED ---\n");
        }
    }

    /**
     * Method that read content from a file
     * @param path file to be read
     * @return ByteBuffer containing file, null if an error has occurred
     */
    private ByteBuffer readFile(Path path){
        try{
            FileChannel file=FileChannel.open(path, StandardOpenOption.READ);
            if(file.size()>Integer.MAX_VALUE) return null;
            int sizeFile=(int)file.size();
            ByteBuffer buffer=ByteBuffer.allocate(sizeFile);
            while (sizeFile>0){
                //File content is read and added in the buffer
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
