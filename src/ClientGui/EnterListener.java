package ClientGui;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/*package*/ class EnterListener implements KeyListener {

    private final JButton button;

    /*package*/ EnterListener(JButton b){
        button=b;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key=e.getKeyCode();
        if(key==KeyEvent.VK_ENTER){
            button.doClick();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
