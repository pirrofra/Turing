package ClientGui;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * This class implements a KeyListener that press a button when the enter key is pressed
 *
 * @author Francesco Pirrò - Matr.544539
 */
/*package*/ class EnterListener implements KeyListener {

    /**
     * button to be pressed
     */
    private final JButton button;

    /**
     * class constructor
     * @param b button to be pressed
     */
    /*package*/ EnterListener(JButton b){
        button=b;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    /**
     * Method overridden to make
     * @param e key pressed
     */
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
