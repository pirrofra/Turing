package ClientGui;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.DatagramChannel;

/*package*/ class NotificationListener extends Thread {
    private final DatagramChannel channel;
    private final MainForm main;

    /*package*/ NotificationListener(DatagramChannel c, MainForm m){
        super();
        channel=c;
        main=m;
    }

    /*package*/ public void run() {
        while(!Thread.interrupted()) {
            try {
                ByteBuffer msg = ByteBuffer.allocate(1024);
                channel.receive(msg);
                String notification = new String(msg.array());
                ResultDialog dialog = new ResultDialog(main, notification, false, false);
                dialog.show(400, 100);
                main.update();
            }
            catch (ClosedByInterruptException e){
                break;
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
