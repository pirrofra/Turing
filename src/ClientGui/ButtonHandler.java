package ClientGui;

import Message.MessageBuffer;
import Message.Operation;
import RequestExecutor.RequestExecutor;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.*;
import java.util.Vector;

/**
 * This class implements the ActionListener for the various operations required for the client
 * It execute a different request depending on the Operation type
 *
 * Request can have at most 2 arguments, each argument is passed as a JTextField
 * When a request is completed, and a result is received a JDialog is showed with the result
 *
 * @author Francesco Pirrò - Matr.544539
 */
/*package*/ class ButtonHandler implements ActionListener {

    /**
     * Operation that this ActionListener should execute
     */
    private final Operation op;

    /**
     * first argument
     */
    private final JTextField arg1;

    /**
     * second argument, could be null if the request only has one argument
     */
    private final JTextField arg2;

    /**
     * RequestExecutor that communicate with the server
     */
    private final RequestExecutor executor;

    /**
     * MainForm that contains the log
     */
    private final MainForm main;


    /**
     * Public class constructor
     * @param operation type of operation this listener should execute
     * @param field1 first argument
     * @param field2 second argument, could be null
     * @param mainForm MainForm that contains the log
     */
    /*package*/ ButtonHandler(Operation operation, JTextField field1,JTextField field2, MainForm mainForm){
        op=operation;
        arg1=field1;
        arg2=field2;
        main=mainForm;
        executor=main.getExecutor();
    }

    /**
     * private method that execute a Create Document operation
     * @return ResultDialog containing the result
     * @throws IOException if an error occurs while communicating with the server
     */
    private ResultDialog create() throws IOException {
        int value;
        try{
            value=Integer.parseInt(arg2.getText()); //get int from JTextField text
        }
        catch (NumberFormatException e){
            //The value is not an integer
            deleteText();
            return  new ResultDialog(main,"Number of Sections in not an integer",false,false);
        }
        MessageBuffer result=executor.createDocument(arg1.getText(),value); //execute request
        String log="";
        if(result.getOP()==Operation.OK){
            log+="Document "+arg1.getText()+" successfully created with "+value+" sections";
        }
        else
            log+=Operation.getDescription(result.getOP());
        main.addLog(log); //add log to mainForm
        deleteText();
        return new ResultDialog(main,result.getOP(),false,false);
    }

    /**
     * private Method that execute a show request
     * @return ResultDialog containing the result
     * @throws IOException if an error occurs while communicating with the server
     */
    private ResultDialog show() throws IOException{
       MessageBuffer result;
       int value=0;
       if(arg2==null) result=executor.show(arg1.getText()); //Show request is for an entire document
       else{
           //Show request is for a section
           try{
               value=Integer.parseInt(arg2.getText()); //Getting integer value from a JTextField
           }
           catch (NumberFormatException e){
               //JTextField doesn't contain an integer value
               deleteText();
               return  new ResultDialog(main,"Section Number in not an integer",false,false);
           }
           result=executor.show(arg1.getText(),value); //execute show section request
       }

       Vector<byte[]> args=result.getArgs(); //Get Message arguments
       String path;
       if(result.getOP()==Operation.OK) {
           //If operation successful
           if (arg2 == null) path= saveFile(arg1.getText(), "complete", args.get(0));
           else path=saveFile(arg1.getText(), "" + value, args.get(0));
           main.addLog(new String(args.get(1))); //Second argument contains message from the server
           main.addLog("File downloaded at "+path);

       }
       else main.addLog(Operation.getDescription(result.getOP()));
        deleteText();
       return new ResultDialog(main,result.getOP(),false,false);

    }

    /**
     * private method that execute an invite request
     * @return ResultDialog containing the result
     * @throws IOException if an error occurs while communicating with the server
     */
    private ResultDialog invite() throws IOException{
        MessageBuffer result=executor.invite(arg1.getText(),arg2.getText()); //execute request
        String log;
        if(result.getOP()==Operation.OK)
            log="User "+arg2.getText()+" successfully invited to edit "+arg1.getText();
        else
            log=Operation.getDescription(result.getOP());
        main.addLog(log); //append log
        deleteText();
        return new ResultDialog(main,result.getOP(),false,false); //result dialog containing the result
    }

    /**
     * private method that execute an edit request
     * @return ResultDialog containing the result, null if successful
     * @throws IOException if an error occurs while communicating with the server
     */
    private ResultDialog edit() throws IOException{
        int section;
        try{
            //getting integer value from JTextField
            section=Integer.parseInt(arg2.getText());
        }
        catch (NumberFormatException e){
            deleteText();
            return  new ResultDialog(main,"Section Number in not an integer",false,false);
        }
        MessageBuffer result=executor.edit(arg1.getText(),section); //execute edit request
        String log;
        if(result.getOP()==Operation.OK){
            //If an edit request is successful, it opens a EditorForm
            Vector<byte[]> args=result.getArgs();
            String path=saveFile(arg1.getText(),"."+section,args.get(0)); //file is saved
            log="User started editing "+arg1.getText()+", section "+section;
            EditorForm editorForm=new EditorForm(main,arg1.getText(),section,Paths.get(path));
            deleteText();
            editorForm.initialize();
            editorForm.open();
        }
        else log=Operation.getDescription(result.getOP());
        main.addLog(log);
        if(result.getOP()==Operation.OK)
            return null;
        else
            return new ResultDialog(main,result.getOP(),false,false);

    }

    /**
     * private method that delete the text from arg1 and arg2 (if args2 is not null)
     */
    private void deleteText(){
        arg1.setText("");
        if(arg2!=null) arg2.setText("");
    }

    /**
     * Method of the interface ActionListener
     * @param e action event
     */
    public void actionPerformed(ActionEvent e){
        ResultDialog dialog;
        main.setEnabled(false);
        //Main form is disabled until the request is complete
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
                    dialog=new ResultDialog(main,Operation.FAIL,false,false);
                    break;
            }
        }
        catch (IOException exception){
            //An error has occurred while communicating with the server
            exception.printStackTrace();
            dialog=new ResultDialog( main,"Connection lost with Server",true,false);
        }
        main.setEnabled(true);
        main.update();
        if(dialog!=null) dialog.show(400,100);
    }

    /**
     * private method that save a file received as a byte[]
     * @param docName document name
     * @param filename file name
     * @param content byte array containing the file
     * @return path of the new file
     * @throws IOException an error has occurred while saving the file
     */
    private String saveFile(String docName,String filename,byte[] content) throws IOException{
        Path dir;
        String[] split= docName.split("/");
        Files.createDirectories(Paths.get(executor.getFilePath(),split[0],split[1]));
        dir= Paths.get(executor.getFilePath(),split[0],split[1],filename);
        try{
            Files.write(dir,content,StandardOpenOption.TRUNCATE_EXISTING);
        }
        catch (NoSuchFileException e){
            Files.createFile(dir);
            Files.write(dir,content,StandardOpenOption.TRUNCATE_EXISTING);
        }
        return dir.toString();

    }

}
