package rushhour;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MainGame extends JFrame implements ActionListener{

    public static void main(String[] args) {
        new MainGame();
    }

    enum GS {
        PLAYING,
        PAUSED,
        LEVELS
    }

    final static int SCRW = 500, SCRH = 800;
    Font f = new Font("Comic Sans MS", Font.BOLD, 30);
    DrawingPanel DP = new DrawingPanel();
    Timer t = new Timer(1, this);
    int time = 0;
    int moves = 0;
    MouseListener ML = new ML();
    Image pauseButton, restartButton;
    int mx,my;
    GS gamestate = GS.PLAYING;

    MainGame() {
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setTitle("Rush Hour Ripoff");
        // this.setLayout(new BorderLayout());
        this.setResizable(false);
        this.addMouseListener(ML);

        
    
        // JPanel scores = new JPanel();
        // scores.setSize(SCRW, 150);
        // scores.add(createScoring(new Dimension(SCRW/4*2, PANW/4-5), time + ""));
        // scores.add(createScoring(new Dimension(SCRW/4, PANW/4-5), moves + ""));

        // JPanel options = new JPanel();
        // options.setSize(SCRW, 150);
        // options.add(createSelection(new Dimension(SCRW/4, PANW/4-5), "LEVELS"));
        // options.add(createSelection(new Dimension(SCRW/4, PANW/4-5), "PAUSE"));
        // options.add(createSelection(new Dimension(SCRW/4, PANW/4-5), "RESTART"));

        // DP.setPreferredSize(new Dimension(PANW,PANH));

        // scores.setBackground(new Color(168, 119, 50));
        // options.setBackground(new Color(168, 119, 50));
        
        // this.add(scores, BorderLayout.NORTH);
        // this.add(options, BorderLayout.SOUTH);

        pauseButton = loadImage("pause.png").getScaledInstance(100, 100, Image.SCALE_DEFAULT);
        restartButton = loadImage("restart.png").getScaledInstance(65, 65, Image.SCALE_DEFAULT);

        this.add(DP);
        
        this.pack();
        this.setVisible(true);
        this.setLocationRelativeTo(null);
        t.start();
    }

    static BufferedImage loadImage(String filename) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(filename));
        } catch (IOException e) {
            System.out.println(e.toString());
            JOptionPane.showMessageDialog(null, "An image failed to load: " + filename, "ERROR",
                    JOptionPane.ERROR_MESSAGE);
        }

        return img;
    }

    // JPanel createSelection(Dimension size, String name) {
    //     JPanel panel = new JPanel();
    //     panel.setPreferredSize(size);
    //     JButton btn = new JButton(name);
    //     btn.addActionListener(new BtnListener());
    //     btn.setActionCommand(name);
    //     panel.add(btn);
    //     panel.setBackground(new Color(168, 119, 50));
    //     return panel;
    // }

    // JPanel createScoring(Dimension size, String score) {
    //     JPanel panel = new JPanel();
    //     panel.add(new JLabel("" + score));
    //     panel.setPreferredSize(size);
    //     panel.setBackground(new Color(168, 119, 50));
    //     return panel;
    // }

   



    class DrawingPanel extends JPanel {
        DrawingPanel() {
            this.setPreferredSize(new Dimension(SCRW,SCRH));
            this.setBackground(new Color(77,0,19));

        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setFont(f);
            g2.setColor(Color.WHITE);


            //moves, time
            g2.drawString("Time: " + time, 50, 75);
            g2.drawString("Moves: " + moves, 275, 75);

            // Grid
            g2.fillRect(0, 150, 500, 500);
            g2.setColor(Color.BLACK);
            for(int i = 0; i < 6; i++) {
                g2.drawLine(0, 150+SCRW/6*i, SCRW, 150+SCRW/6*i);
                g2.drawLine(SCRW/6*i, 150, SCRW/6*i, 650);
            }
            g2.drawLine(0, 650, SCRW, 650);

            // pause, levels, play
            g2.drawImage(pauseButton, 50, 680, null);

            g2.drawImage(restartButton, 350, 695, null);

            if(gamestate == GS.PAUSED){
                g2.setStroke(new BasicStroke(10));
                g2.setColor(Color.BLACK);
                g2.fillRect(50, 100, 400,600);
                g2.setColor(Color.WHITE);
                g2.drawRect(50, 100, 400,600);
                g2.drawString("PAUSED", 175,140);
                g2.drawString("Level: " + 3, 175, 250);
                g2.drawString("Moves: " + moves, 175, 350);
                g2.drawString("Time: " + time, 175, 450);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DP.repaint();
        if(gamestate == GS.PAUSED) {
            return;
        }


        time++;
    }


    class ML implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            mx = e.getX(); my = e.getY();
            if(mx > 50 && mx < 150 && my > 680 && my < 780) {



                gamestate = GS.PAUSED;
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {}

        @Override
        public void mouseReleased(MouseEvent e) {}

        @Override
        public void mouseEntered(MouseEvent e) {}

        @Override
        public void mouseExited(MouseEvent e) {}

    }
     
}
