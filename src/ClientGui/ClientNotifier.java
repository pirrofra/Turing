package ClientGui;

import RemoteClientNotifier.RemoteClientNotifier;

import java.rmi.RemoteException;

/*package*/ class ClientNotifier implements RemoteClientNotifier {

    private final MainForm main;

    /*package*/ ClientNotifier(MainForm m){
        main=m;
    }

    public void notify(String msg) {
        ResultDialog dialog = new ResultDialog(main, msg, false, false); //the content of the message is put in a ResultDialog and shown
        dialog.show(400, 100);
        main.update();
    }
}
