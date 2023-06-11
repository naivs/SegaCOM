import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 *
 * @author ivan
 */
public class Display extends JPanel {

    private static final String GROUND = "img/back.gif";
    private static final String CAGE = "img/cage.gif";
    private static final String L_GAMEPAD = "img/gamepad_l.gif";
    private static final String R_GAMEPAD = "img/gamepad_r.gif";
    private static final String[][] BUTTONS = {{
        "img/l_a.gif",
        "img/l_b.gif",
        "img/l_c.gif",
        "img/l_x.gif",
        "img/l_y.gif",
        "img/l_z.gif",
        "img/l_up.gif",
        "img/l_down.gif",
        "img/l_left.gif",
        "img/l_right.gif",
        "img/l_start.gif",
        "img/l_mode.gif"
    }, {
        "img/r_a.gif",
        "img/r_b.gif",
        "img/r_c.gif",
        "img/r_x.gif",
        "img/r_y.gif",
        "img/r_z.gif",
        "img/r_up.gif",
        "img/r_down.gif",
        "img/r_left.gif",
        "img/r_right.gif",
        "img/r_start.gif",
        "img/r_mode.gif"
    }};
    private Graphics graphic;
    private final boolean[][] keyStates;
    private final LogPool logPool;

    public Display() {
        keyStates = new boolean[2][12];
        logPool = new LogPool(7);
    }

    public void press(int pad, int key) {
        keyStates[pad][key] = true;
    }

    public void release(int pad, int key) {
        keyStates[pad][key] = false;
    }

    public boolean isPressed(int pad, int key) {
        return keyStates[pad][key];
    }

    public void log(String message) {
        logPool.addLine(message);
        updateUI();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        graphic = g;
        super.paintComponent(graphic);

        graphic.drawImage(new ImageIcon(Display.class.getResource(GROUND)).getImage(), 0, 0, this);
        graphic.drawImage(new ImageIcon(Display.class.getResource(L_GAMEPAD)).getImage(), 0, 0, this);
        graphic.drawImage(new ImageIcon(Display.class.getResource(R_GAMEPAD)).getImage(), 0, 0, this);
        graphic.drawImage(new ImageIcon(Display.class.getResource(CAGE)).getImage(), 0, 0, this);

        for(int i = 0; i < 2; i++) {
            for(int j = 0; j < 12; j++) {
                if(keyStates[i][j])
                    graphic.drawImage(new ImageIcon(Display.class.getResource(BUTTONS[i][j])).getImage(), 0, 0, this);
            }
        }

        graphic.setColor(Color.GREEN);
        Font font = new Font("Arial", Font.PLAIN, 12);
        graphic.setFont(font);
        
        int xPos = 350, yPos = 40;
        for (int i = 0; i < logPool.size(); i++) {
            graphic.drawString(logPool.get(i), xPos, yPos + (graphic.getFontMetrics().getHeight() * i + 4));
        }
    }

    private static class LogPool {

        private final List<String> logPul;
        private final int size;

        public LogPool(int size) {
            logPul = new ArrayList<>(size);
            this.size = size;
        }

        public void addLine(String line) {
            if (logPul.size() == size) {
                logPul.remove(0);
            }
            logPul.add(line);
        }

        public String get(int index) {
            return logPul.get(index);
        }

        @Override
        public String toString() {
            String out = "";
            out = logPul.stream().map((s) -> s + "\n").reduce(out, String::concat);
            return out;
        }

        public int size() {
            return logPul.size();
        }
    }
}
