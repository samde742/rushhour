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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

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
    Font f = new Font("Monospaced", Font.BOLD, 30);
    Font title = new Font("Monospaced", Font.BOLD, 50);
    Color blue = new Color(167, 199, 231);
    Color yellow = new Color(248, 241, 174);
    Color darkBlue = new Color(61, 66, 107);
    Color pink = new Color(242, 196, 229);
    Color darkYellow = new Color(201, 183, 81);
    Color darkPink = new Color(209, 148, 192);
    DrawingPanel DP = new DrawingPanel();
    Timer t = new Timer(1, this);
    Timer counter = new Timer(1000, new Counter());
    int sec = 0;
    int min = 0;
    int moves = 0;
    MouseListener ML = new ML();
    Image pauseButton, restartButton, logo;
    int mx,my;
    GS gamestate = GS.PLAYING;
    int[][] board = new int[6][6];
    int level = 0; // level starting at 0


    final static int RED = 1, GREEN = 2, BLUE = 3, ORANGE = 4, PINK = 5;

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
        logo = loadImage("logo.png").getScaledInstance(500, 150, Image.SCALE_DEFAULT);

        this.add(DP);
        
        this.pack();
        this.setVisible(true);
        this.setLocationRelativeTo(null);
        createBoard();
        t.start();
        counter.start();
    }


    void createBoard() {
        for(int i = 0; i < board.length; i++) {
            try {
                if(Files.lines(Paths.get("maps.txt")).count() > level*board.length ) {
                    
                    String line = Files.readAllLines(Paths.get("maps.txt")).get(level*board.length+i);
                    
                    int[] arr = toIntArray(line);
                    System.out.println(Arrays.toString(arr));
                }
            } catch (IOException e) {
            }
        }
        
    }

    int[] toIntArray(String s) {
        int[] arr = new int[6];
        for(int i = 0; i < arr.length; i++) {
            arr[i] = s.charAt(i)-'0';
        }

        return arr;
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
            this.setBackground(blue);

        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setFont(f);

            if(gamestate == GS.PLAYING || gamestate == GS.PAUSED) {
                g2.setColor(pink);

                //moves, time
                g2.fillRoundRect(20,20,280, 110, 20, 20);
                g2.fillRoundRect(320,20,160, 110, 20, 20);
                g2.setColor(darkPink);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(20,20,280, 110, 20, 20);
                g2.drawRoundRect(320,20,160, 110, 20, 20);

                g2.setColor(darkBlue);
                g2.drawString("Time:", 30, 50);
                g2.drawString("Moves: ", 330, 50);
                g2.setFont(title);
                g2.drawString(min + ":" + ((sec < 10) ? "0" : "") + sec, 105, 100);
                g2.drawString("" +moves, 385, 100);

                // Grid
                g2.setColor(yellow);
                g2.fillRect(0, 150, 500, 500);
                g2.setStroke(new BasicStroke(3));
                g2.setColor(darkYellow);
                for(int i = 0; i < board.length; i++) {
                    // g2.draw
                    // g2.drawLine(0, 150+SCRW/6*i, SCRW, 150+SCRW/6*i);
                    // g2.drawLine(SCRW/6*i, 150, SCRW/6*i, 650);
                }
                g2.drawLine(0, 650, SCRW, 650);

                // pause, levels, play
                g2.drawImage(pauseButton, 50, 680, null);

                g2.drawImage(restartButton, 350, 695, null);
            }
            if(gamestate == GS.PAUSED){

                g2.setStroke(new BasicStroke(5));
                g2.setColor(pink);
                g2.fillRect(50, 100, 400,600);
                g2.setColor(blue);
                g2.fillRect(50, 100, 400,100);
                g2.setFont(title);
                g2.setColor(darkBlue);                
                g2.drawLine(50,200,450,200);
                g2.drawString("PAUSED", 158,170);
                g2.setFont(f);
                g2.setColor(darkBlue);                
                g2.drawString("Level: " + 3, 175, 250);
                g2.drawString("Moves: " + moves, 175, 350);
                g2.drawString("Time: " + min + ":" + sec, 175, 450);
                g2.setColor(yellow);
                g2.fillRoundRect(80, 490, 340, 60, 20, 20); // 340 X 60 BACK TO GAME
                g2.setColor(blue);
                g2.fillRoundRect(80, 580, 180, 60, 20, 20);
                g2.fillRoundRect(280, 580, 140, 60, 20, 20);
                g2.setColor(darkYellow);
                g2.drawRoundRect(80, 490, 340, 60, 20, 20);
                g2.setColor(darkBlue);
                g2.drawRoundRect(80, 580, 180, 60, 20, 20);
                g2.drawRoundRect(280, 580, 140, 60, 20, 20);
                g2.drawString("Back To Game",140,530);
                g2.setColor(yellow);

                g2.drawString("Levels",115,620);
                g2.drawString("Quit",312,620);
                g2.setColor(darkBlue);
                g2.drawRect(50, 100, 400,600);

            }

            if(gamestate == GS.LEVELS){
                g2.setColor(pink);
                g2.drawString("Easy", 40, 190);
                g2.drawString("Medium", 40, 390);
                g2.drawString("Hard", 40, 590);

                drawBoxes(g2, 200);
                drawBoxes(g2, 400);
                drawBoxes(g2, 600);
                g2.drawImage(logo, 0, 0, null);

            }
        }
    }

    void drawBoxes(Graphics2D g2, int y){
        g2.fillRoundRect(40, y, 190,150,20,20);
        g2.fillRoundRect(270, y, 190, 150, 20, 20);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DP.repaint();
        if(gamestate == GS.PAUSED) {
            return;
        }

    }


    class ML implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            mx = e.getX(); my = e.getY();


            // if they hit the pause button
            if(mx > 50 && mx < 150 && my > 680 && my < 780) {
                gamestate = GS.PAUSED;
            }
            
            // if they hit back to game while pasued --> continue
            if(gamestate == GS.PAUSED) {
                
                // if they resume
                if(mx > 80 && mx < 420 && my > 490 && my < 580) {
                    gamestate = GS.PLAYING;
                }

                // if they hit levels
                if(mx > 80 && mx < 260 && my > 580 && my < 640) {
                    gamestate = GS.LEVELS;
                }

                // if they hit quit
                if(mx > 280 && mx < 420 && my > 580 && my < 640) {
                    // QUIT CODE
                }
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


    class Counter implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if(gamestate == GS.PLAYING) sec++;
            if(sec == 60) {
                min++;
                sec = 0;
            }
        }

    }
     
}
