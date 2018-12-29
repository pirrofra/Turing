package ClientGui;

import Message.Operation;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

/*package*/ class ResultDialog extends JDialog implements ActionListener {

    private JFrame father;
    private boolean closeAppAtExit;

    /*package*/ ResultDialog(JFrame f, String message,boolean close){
        super(f,"Attention");
        father=f;
        closeAppAtExit=close;
        JButton ok=new JButton("OK");
        ok.addActionListener(this);
        JPanel panel=new JPanel(new BorderLayout());
        Border padding=BorderFactory.createEmptyBorder(10,10,10,10);
        panel.setBorder(padding);
        add(panel);
        panel.add(new JLabel(message), BorderLayout.CENTER);
        panel.add(ok,BorderLayout.SOUTH);
    }

    /*package*/ ResultDialog(JFrame f, Operation OP,boolean close){
        this(f,getDescription(OP),close);
    }

    /*package*/ void show(int width,int height){
        setSize(width,height);
        setLocationRelativeTo(father);
        show();
    }

    private static String getDescription(Operation OP){
        String result;
        switch (OP){
            case OK:
                result= "Operation Successful";
                break;
            case FAIL:
                result=  "Operation has failed";
                break;
            case NAME_NOT_AVAILABLE:
                result=  "This name is not available";
                break;
            case ALREADY_LOGGED_IN:
                result=  "This user is already logged in";
                break;
            case PASSWORD_INCORRECT:
                result=  "Username and/or password are incorrect";
                break;
            case DOCUMENT_NOT_FOUND:
                result=  "The document requested couldn't be found";
                break;
            case SECTION_BUSY:
                result=  "This section is already being edited";
                break;
            case EDITING_NOT_REQUESTED:
                result=  "Editing for this document not requested";
                break;
            case USER_ALREADY_EDITING:
                result=  "An editing request was already sent";
                break;
            case USER_NOT_FOUND:
                result=  "This username doesn't exist";
                break;
            case USER_ALREADY_INVITED:
                result=  "This user was already invited to edit this document";
                break;
            case PERMISSION_DENIED:
                result=  "You don't have permission to this operation";
                break;
            case FILE_TOO_BIG:
                result=  "This file is too big";
                break;
            case REQUEST_INCOMPLETE:
                result=  "The request was incomplete";
                break;
            case CLIENT_NOT_LOGGED_IN:
                result=  "This client isn't logged in yet";
                break;
            case INVALID_REQUEST:
                result=  "An invalid request was sent to the server";
                break;
            default:
                result=  "An invalid response was sent by the server";
                break;
        }
        return result;
    }

    public void actionPerformed(ActionEvent e) {
        dispose();
        if(closeAppAtExit) father.dispatchEvent(new WindowEvent(father,WindowEvent.WINDOW_CLOSING));
    }
}
