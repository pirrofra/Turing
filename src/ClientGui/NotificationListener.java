package ClientGui;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.DatagramChannel;

/**
 * This class extends a threads that listen on a DatagramChannel for notification from the server,
 * and when a notification arrives, it shows a ResultDialog
 * Notification from the server arrives as UDP Datagram
 *
 * @author Francesco Pirrò - Matr.544539
 */
/*package*/ class NotificationListener extends Thread {
    /**
     * DatagramChannel used to listen for notification from the server
     */
    private final DatagramChannel channel;

    /**
     * MainForm that started the server
     */
    private final MainForm main;

    /**
     * class constructor
     * @param c DatagramChannel used for listening
     * @param m MainForm that started the this thread
     */
    /*package*/ NotificationListener(DatagramChannel c, MainForm m){
        super();
        channel=c;
        main=m;
    }

    /**
     * Thread main function
     */
    /*package*/ public void run() {
        while(!Thread.interrupted()) {
            try {
                ByteBuffer msg = ByteBuffer.allocate(1024);
                channel.receive(msg);
                //Channel is in blocking mode, so it waits until a new message arrive
                String notification = new String(msg.array());
                ResultDialog dialog = new ResultDialog(main, notification, false, false); //the content of the message is put in a ResultDialog and shown
                dialog.show(400, 100);
                main.update();
            }
            catch (ClosedByInterruptException e){
                //If an interrupt arrives, it exit the loop
                break;
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
