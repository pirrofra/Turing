package ClientGui;

import RemoteClientNotifier.RemoteClientNotifier;

/**
 * This class implements the RemoteClientNotifier used by the server to notify a client that a new invite has been received
 *
 * @author Francesco Pirrò - Matr.544539
 */
/*package*/ class ClientNotifier implements RemoteClientNotifier {
    /**
     * MainForm of the client
     */
    private final MainForm main;

    /**
     * Class constructor
     * @param m MainForm currently used by the client
     */
    /*package*/ ClientNotifier(MainForm m){
        main=m;
    }

    /**
     * Create and show a ResultDialog with a message from the server
     * @param msg message received by the server via RMI parameter
     */
    public void notify(String msg) {
        ResultDialog dialog = new ResultDialog(main, msg, false, false); //the content of the message is put in a ResultDialog and shown
        dialog.show(400, 100);
        main.update();
    }
}
