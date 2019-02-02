package ClientGui;

import Message.Operation;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;

/*package*/ class ResultDialog extends JDialog implements ActionListener {

    private final Window father;
    private final boolean closeAppAtExit;
    private final boolean closeFormAtExit;

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

    /*package*/ ResultDialog(Window f, Operation OP,boolean closeApp,boolean closeForm){
        this(f,Operation.getDescription(OP),closeApp,closeForm);
    }

    /*package*/ void show(int width,int height){
        setPreferredSize(new Dimension(width,height));
        pack();
        setLocationRelativeTo(father);
        setVisible(true);
    }


    public void actionPerformed(ActionEvent e) {
        dispose();
        if(closeAppAtExit) System.exit(0);
        else if(closeFormAtExit) father.dispose();
    }

}
