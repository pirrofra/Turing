package ClientGui;

import Message.Operation;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * This class extends a JPanel and gives the user a graphical interface that let him choose between different request to be sent to the server
 * This is made possible thanks to a tabbed pane, where in each tab there are text field and a button for a specific request
 *
 * @author  Francesco Pirrò - Matr.544539
 */
/*package*/ class SelectOperation extends JPanel {

    /**
     * MainForm that uses this panel
     */
    private final MainForm main;

    /**
     * JTabbedPane that store different tabs
     */
    private final JTabbedPane selector;

    /**
     * class constructor
     * @param mainForm MainForm that uses this panel
     */
    /*package*/ SelectOperation(MainForm mainForm ){
        super();
        main=mainForm;
        setLayout(new BoxLayout(this,BoxLayout.LINE_AXIS));
        selector=new JTabbedPane();
    }

    /**
     * private method that adds all the tabs to selector
     */
    private void addToSelector(){
        JPanel createPanel=initializeCreatePanel();
        JPanel showAllPanel=initializeShowDocumentPanel();
        JPanel showSectionPanel=initializeShowSectionPanel();
        JPanel invitePanel=initializeInviteUserPanel();
        JPanel editPanel=initializeEditDocumentPanel();
        selector.add("Create Document",createPanel);
        selector.add("Show document",showAllPanel);
        selector.add("Show section",showSectionPanel);
        selector.add("Invite User",invitePanel);
        selector.add("Edit Document",editPanel);
    }

    /**
     * Method that initialize the panel that contains ui components to send a create request
     * @return new panel
     */
    private JPanel initializeCreatePanel(){
        JButton create=new JButton("Create");
        JTextField docName=new JTextField();
        JTextField numSection=new JTextField();
        JPanel panel=initializeBody("New Document:",docName,"Number of Section:",numSection);
        panel.add(textAndLabel(" ",create));
        create.addActionListener(new ButtonHandler(Operation.CREATE,docName,numSection,main));
        EnterListener listener=new EnterListener(create);
        docName.addKeyListener(listener);
        numSection.addKeyListener(listener);
        return panel;
    }

    /**
     * Method that initialize the panel that contains ui components to send a show document request
     * @return new panel
     */
    private JPanel initializeShowDocumentPanel(){
        JPanel panel=new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
        JButton show=new JButton("Show");
        JTextField docName=new JTextField();
        JPanel body=initializeBody("Document Name",docName);
        body.add(textAndLabel(" ",show));
        panel.add(body);
        show.addActionListener(new ButtonHandler(Operation.SHOW,docName,null,main));
        EnterListener listener=new EnterListener(show);
        docName.addKeyListener(listener);
        return panel;
    }

    /**
     * Method that initialize the panel that contains ui components to send a show section request
     * @return new panel
     */
    private JPanel initializeShowSectionPanel(){
        JButton show=new JButton("Show");
        JTextField docName=new JTextField();
        JTextField numSection=new JTextField();
        JPanel panel=initializeBody("Document Name",docName,"Section nr:",numSection);
        panel.add(textAndLabel(" ",show));
        show.addActionListener(new ButtonHandler(Operation.SHOW,docName,numSection,main));
        EnterListener listener=new EnterListener(show);
        docName.addKeyListener(listener);
        numSection.addKeyListener(listener);
        return panel;
    }

    /**
     * Method tha initialize the panel that contains ui components to send a invite request
     * @return new panel
     */
    private JPanel initializeInviteUserPanel(){
        JButton invite=new JButton("Invite");
        JTextField docName=new JTextField();
        JTextField user=new JTextField();
        JPanel panel=initializeBody("Document Name",docName,"User Invited",user);
        panel.add(textAndLabel(" ",invite));
        invite.addActionListener(new ButtonHandler(Operation.INVITE,docName,user,main));
        EnterListener listener=new EnterListener(invite);
        docName.addKeyListener(listener);
        user.addKeyListener(listener);
        return panel;
    }

    /**
     * Method that initialize the panel that contains ui components to send a edit request
     * @return new panel
     */
    private JPanel initializeEditDocumentPanel(){
        JButton edit=new JButton("Edit");
        JTextField docName=new JTextField();
        JTextField numSection=new JTextField();
        JPanel panel=initializeBody("Document Name",docName,"Section nr:",numSection);
        panel.add(textAndLabel(" ",edit));
        edit.addActionListener(new ButtonHandler(Operation.EDIT,docName,numSection,main));
        EnterListener listener=new EnterListener(edit);
        docName.addKeyListener(listener);
        numSection.addKeyListener(listener);
        return panel;
    }

    /**
     * Method that initialize the panel that contains 2 textfield and 2 text label
     * @return new panel
     */
    private static JPanel initializeBody(String label1,JTextField arg1,String label2,JTextField arg2){
        JPanel panel=new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.LINE_AXIS));
        Border padding=BorderFactory.createEmptyBorder(5,5,5,5);
        panel.setBorder(padding);
        JPanel firstColumn=textAndLabel(label1,arg1);
        JPanel secondColumn=textAndLabel(label2,arg2);
        firstColumn.setPreferredSize(new Dimension(400,55));
        panel.add(firstColumn);
        panel.add(Box.createHorizontalGlue());
        panel.add(secondColumn);
        return panel;
    }

    /**
     * Method that initialize the panel that contains 1 label and 1 textfield
     * @return new panel
     */
    private static JPanel initializeBody(String label,JTextField arg){
        JPanel panel=new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.LINE_AXIS));
        Border padding=BorderFactory.createEmptyBorder(5,5,5,5);
        panel.setBorder(padding);
        JPanel column=textAndLabel(label,arg);
        panel.add(column);
        return panel;
    }

    /**
     * Method that initialize the panel that a label and a component
     * @return new panel
     */
    private static JPanel textAndLabel(String label, Component text){
        Border padding=BorderFactory.createEmptyBorder(5,5,5,5);
        JPanel panel=new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
        panel.setBorder(padding);
        panel.add(new JLabel(label));
        panel.add(text);
        return panel;

    }

    /**
     * Method that initialize the selector
     */
    /*package*/ void initialize(){
        add(selector);
        addToSelector();
    }


}
