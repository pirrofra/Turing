package ClientGui;

import Message.Operation;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;

/**
 * Class that extends a JDialog used to get the result of Operations
 * ResultDialog can show a custom message given by a String, or use the Operation enum and get a standard message
 *
 * This class also gives the possibility to close the form that generated it at exit, or close the entire app entirely
 * This class also implements ActionLister so it can dispose itself when the button is pressed
 *
 *  @author  Francesco Pirrò - Matr.544539
 */
/*package*/ class ResultDialog extends JDialog implements ActionListener {

    /**
     * Window that generated the Result Dialog
     */
    private final Window father;

    /**
     * boolean used to check if the app should be close at exit
     */
    private final boolean closeAppAtExit;

    /**
     * boolean used to check if "father" should be closed at exit
     */
    private final boolean closeFormAtExit;

    /**
     * class constructor
     * @param f window that generated this message
     * @param message string that contains the message
     * @param closeApp if true, when closed this window will close the entire app
     * @param closeForm if true, when closed this window will close his father too
     */
    /*package*/ ResultDialog( Window f, String message,boolean closeApp,boolean closeForm){
        super(f,"Attention");
        father=f;
        closeAppAtExit=closeApp;
        closeFormAtExit=closeForm;
        JButton ok=new JButton("OK");
        ok.addActionListener(this);
        JPanel panel=new JPanel(new BorderLayout());
        Border padding=BorderFactory.createEmptyBorder(10,10,10,10);
        panel.setBorder(padding);
        add(panel);
        panel.add(new JLabel(message), BorderLayout.CENTER);
        panel.add(ok,BorderLayout.SOUTH);
        getRootPane().setDefaultButton(ok);
    }

    /**
     *
     class constructor
     * @param f window that generated this message
     * @param OP message given in the form of an Operation enum
     * @param closeApp if true, when closed this window will close the entire app
     * @param closeForm if true, when closed this window will close his father too
     */
    /*package*/ ResultDialog(Window f, Operation OP,boolean closeApp,boolean closeForm){
        this(f,Operation.getDescription(OP),closeApp,closeForm);
    }

    /**
     * Method that show the result dialog
     * @param width width of the window
     * @param height height of the window
     */
    /*package*/ void show(int width,int height){
        setPreferredSize(new Dimension(width,height));
        pack();
        setLocationRelativeTo(father);
        setVisible(true);
    }

    /**
     * Implementation of the actionPerformed method of the interface ActionListener
     * @param e
     */
    public void actionPerformed(ActionEvent e) {
        dispose(); //close this form
        if(closeAppAtExit) System.exit(0); //close the entire app
        else if(closeFormAtExit) father.dispose(); //close the father
    }

}
