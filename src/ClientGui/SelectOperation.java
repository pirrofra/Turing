package ClientGui;

import Message.Operation;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.text.NumberFormat;

/*package*/ class SelectOperation extends JPanel {


    private final MainForm main;
    private final JTabbedPane selector;

    /*package*/ SelectOperation(MainForm mainForm ){
        super();
        main=mainForm;
        setLayout(new BoxLayout(this,BoxLayout.LINE_AXIS));
        selector=new JTabbedPane();
    }

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

    private JPanel initializeCreatePanel(){
        JButton create=new JButton("Create");
        JTextField docName=new JTextField();
        NumberFormat format=NumberFormat.getIntegerInstance();
        format.setMaximumFractionDigits(0);
        JFormattedTextField numSection=new JFormattedTextField(format);
        JPanel panel=initializeBody("New Document:",docName,"Number of Section:",numSection);
        panel.add(textAndLabel(" ",create));
        create.addActionListener(new ButtonHandler(Operation.CREATE,docName,numSection,main));
        EnterListener listener=new EnterListener(create);
        docName.addKeyListener(listener);
        numSection.addKeyListener(listener);
        return panel;
    }

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

    private JPanel initializeShowSectionPanel(){
        JButton show=new JButton("Show");
        JTextField docName=new JTextField();
        NumberFormat format=NumberFormat.getIntegerInstance();
        format.setMaximumFractionDigits(0);
        JFormattedTextField numSection=new JFormattedTextField(format);
        JPanel panel=initializeBody("Document Name",docName,"Section nr:",numSection);
        panel.add(textAndLabel(" ",show));
        show.addActionListener(new ButtonHandler(Operation.SHOW,docName,numSection,main));
        EnterListener listener=new EnterListener(show);
        docName.addKeyListener(listener);
        numSection.addKeyListener(listener);
        return panel;
    }

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

    private JPanel initializeEditDocumentPanel(){
        JButton edit=new JButton("Edit");
        JTextField docName=new JTextField();
        NumberFormat format=NumberFormat.getIntegerInstance();
        format.setMaximumFractionDigits(0);
        JFormattedTextField numSection=new JFormattedTextField(format);
        JPanel panel=initializeBody("Document Name",docName,"Section nr:",numSection);
        panel.add(textAndLabel(" ",edit));
        edit.addActionListener(new ButtonHandler(Operation.EDIT,docName,numSection,main));
        EnterListener listener=new EnterListener(edit);
        docName.addKeyListener(listener);
        numSection.addKeyListener(listener);
        return panel;
    }

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
    private static JPanel initializeBody(String label,JTextField arg){
        JPanel panel=new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.LINE_AXIS));
        Border padding=BorderFactory.createEmptyBorder(5,5,5,5);
        panel.setBorder(padding);
        JPanel column=textAndLabel(label,arg);
        panel.add(column);
        return panel;
    }

    private static JPanel textAndLabel(String label, Component text){
        Border padding=BorderFactory.createEmptyBorder(5,5,5,5);
        JPanel panel=new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
        panel.setBorder(padding);
        panel.add(new JLabel(label));
        panel.add(text);
        return panel;

    }

    /*package*/ void initialize(){
        add(selector);
        addToSelector();
    }


}
