import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;

/**
 * @author ivan
 */
public class Application {

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            EventQueue.invokeLater(() ->
                    new GUI().setVisible(true));
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
