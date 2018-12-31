package ClientGui;

import Message.MessageBuffer;
import Message.Operation;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Date;
import java.util.Set;
import java.util.Vector;

public class ButtonHandler implements ActionListener {

    private final Operation op;
    private final JTextField arg1;
    private final JTextField arg2;
    private final RequestExecutor executor;
    private final JFrame mainFrame;
    private final MainForm main;

    public ButtonHandler(Operation operation, JTextField field1,JTextField field2, MainForm mainForm){
        op=operation;
        arg1=field1;
        arg2=field2;
        main=mainForm;
        executor=main.getExecutor();
        mainFrame=main.getMainFrame();
    }

    private ResultDialog create() throws IOException {
        int value;
        try{
            value=Integer.parseInt(arg2.getText());
        }
        catch (NumberFormatException e){
            deleteText();
            return  new ResultDialog(mainFrame,"Number of Sections in not an integer",false,false);
        }
        MessageBuffer result=executor.createDocument(arg1.getText(),value);
        Date currentTime=new Date(System.currentTimeMillis());
        String log=currentTime.toString()+": ";
        if(result.getOP()==Operation.OK)
            log+="Document "+arg1.getText()+" successfully created with "+value+" sections";
        else
            log+=Operation.getDescription(result.getOP());
        main.addLog(log);
        deleteText();
        return new ResultDialog(mainFrame,result.getOP(),false,false);
    }

    private ResultDialog show() throws IOException{
       MessageBuffer result;
        int value=0;
        if(arg2==null) result=executor.show(arg1.getText());
       else{
           try{
               value=Integer.parseInt(arg2.getText());
           }
           catch (NumberFormatException e){
               deleteText();
               return  new ResultDialog(mainFrame,"Section Number in not an integer",false,false);
           }
           result=executor.show(arg1.getText(),value);
       }
       Vector<byte[]> args=result.getArgs();
       String path;
       if(result.getOP()==Operation.OK) {
           if (arg2 == null) path= saveFile(arg1.getText(), "complete", args.get(0));
           else path=saveFile(arg1.getText(), "." + value, args.get(0));
           main.addLog(new String(args.get(1)));
           main.addLog("File downloaded at "+path);

       }
       else main.addLog(Operation.getDescription(result.getOP()));
        deleteText();
       return new ResultDialog(mainFrame,result.getOP(),false,false);

    }

    private ResultDialog invite() throws IOException{
        MessageBuffer result=executor.invite(arg1.getText(),arg2.getText());
        String log;
        if(result.getOP()==Operation.OK)
            log="User "+arg2.getText()+" successfully invited to edit "+arg1.getText();
        else
            log=Operation.getDescription(result.getOP());
        main.addLog(log);
        deleteText();
        return new ResultDialog(mainFrame,result.getOP(),false,false);
    }

    private ResultDialog edit() throws IOException{
        int section;
        try{
            section=Integer.parseInt(arg2.getText());
        }
        catch (NumberFormatException e){
            deleteText();
            return  new ResultDialog(mainFrame,"Section Number in not an integer",false,false);
        }
        MessageBuffer result=executor.edit(arg1.getText(),section);
        String log;
        if(result.getOP()==Operation.OK){
            Vector<byte[]> args=result.getArgs();
            String path=saveFile(arg1.getText(),"."+section,args.get(0));
            log="User started editing "+arg1.getText()+", section "+section;
            EditorForm editorForm=new EditorForm(main,arg1.getText(),section,Paths.get(path),null,0);
            editorForm.show();
        }
        else log=Operation.getDescription(result.getOP());
        main.addLog(log);
        return new ResultDialog(mainFrame,result.getOP(),false,false);
    }

    private void deleteText(){
        arg1.setText("");
        if(arg2!=null) arg2.setText("");
    }

    public void actionPerformed(ActionEvent e){
        ResultDialog dialog;
        main.disable();
        try{
            switch (op){
                case CREATE:
                    dialog=create();
                    break;
                case SHOW:
                    dialog=show();
                    break;
                case INVITE:
                    dialog=invite();
                    break;
                case EDIT:
                    dialog=edit();
                    break;
                default:
                    dialog=new ResultDialog(mainFrame,Operation.FAIL,false,false);
                    break;
            }
        }
        catch (IOException exception){
            exception.printStackTrace();
            dialog=new ResultDialog( mainFrame,"Connection lost with Server",true,false);
        }
        main.enable();
        dialog.show(400,100);
    }

    private String saveFile(String docName,String filename,byte[] content) throws IOException{
        Path dir;
        String[] split= docName.split("/");
        Files.createDirectories(Paths.get(executor.getFilePath(),split[0],split[1]));
        dir= Paths.get(executor.getFilePath(),split[0],split[1],filename);
        try{
            Files.write(dir,content,StandardOpenOption.TRUNCATE_EXISTING);
        }
        catch (NoSuchFileException e){
            Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rw-------");
            FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);
            Files.createFile(dir,attr);
            Files.write(dir,content,StandardOpenOption.TRUNCATE_EXISTING);
        }
        return dir.toString();

    }

}