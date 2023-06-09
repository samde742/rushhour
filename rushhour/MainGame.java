package rushhour;

import javax.imageio.ImageIO;
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
    final static int CELLW = SCRW/6, CELLH = SCRW/6;
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
    final static int boardOffset = 165;
    boolean vert = false;


    final static Color[] carColors = {Color.RED, Color.BLUE, Color.PINK, Color.ORANGE, Color.GREEN};

    MainGame() {
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setTitle("Rush Hour Ripoff");
        this.setResizable(false);
        this.addMouseListener(ML);

        pauseButton = loadImage("pause.png").getScaledInstance(100, 100, Image.SCALE_DEFAULT);
        restartButton = loadImage("restart.png").getScaledInstance(65, 65, Image.SCALE_DEFAULT);
        logo = loadImage("RushHourLogo.png").getScaledInstance(400, 180, Image.SCALE_DEFAULT);

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
                    board[i] = arr;
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

    int checkAdj(int a, int b) {
        int num = 0;
        if(a > 0 && board[a][b] == board[a-1][b]) {
            num++;
            vert = true;
        }
        if(a < board.length-1 && board[a][b] == board[a+1][b]) {
            num--;
            vert = true;
        }

        if(b > 0 && board[a][b] == board[a][b-1]) {
            num++;
            vert = false;
        }
        if(b < board.length-1 && board[a][b] == board[a][b+1]) {
            num--;
            vert = false;

        }
        
        return num;
    }

    class DrawingPanel extends JPanel {
        DrawingPanel() {
            this.setPreferredSize(new Dimension(SCRW,SCRH));
            this.setBackground(new Color(179, 199, 226));

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
                // g2.fillRect(0, boardOffset, 500, 500);
                g2.setStroke(new BasicStroke(5));
                g2.setColor(darkYellow);


                // BOARD
                for(int i = 0; i < board.length; i++) {
                    for(int j = 0; j < board.length; j++) {
                        g2.setColor(darkYellow);
                        g2.drawRect(j*CELLW, boardOffset+i*CELLH, CELLW, CELLH);

                        if(board[i][j] > 0) {
                            int num = checkAdj(i, j);
                            int x1= 0,y1= 0,x2 = 0,y2 = 0;
                            
                            
                            if(vert) {
                                x1 = 10;
                                y1 = -3;
                                x2 = -20;
                                y2 = 3;

                                if(num == 1) y2 = -10;
                                if(num == -1) {
                                    y1 = 10;
                                    y2 = -10;
                                }
                            }
                            else {
                                x1 = -3;
                                y1 = 10;
                                x2 = 3;
                                y2 = -20;

                                if(num == 1) x2 = -10;
                                if(num == -1) {
                                    x1 = 10;
                                    x2 = -10;
                                }
                            }

                            g2.setColor(carColors[board[i][j]-1]);
                            g2.fillRect(j*CELLW+x1, boardOffset+i*CELLH+y1, CELLW+x2, CELLH+y2);                                
                        }
                    }
                }

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
                
                g2.drawImage(logo, 58, 0, null);

                g2.setColor(pink);
                g2.drawString("Easy", 40, 190);
                g2.drawString("Medium", 40, 390);
                g2.drawString("Hard", 40, 590);

                drawBoxes(g2, 200);
                drawBoxes(g2, 400);
                drawBoxes(g2, 600);

            }
        }
    }

    void drawBoxes(Graphics2D g2, int y){
        g2.setColor(pink);
        g2.fillRoundRect(40, y, 190,150,20,20);
        g2.fillRoundRect(270, y, 190, 150, 20, 20);
        g2.setColor(yellow);
        g2.fillRoundRect(40,y+100, 190,50,20,20);
        g2.fillRoundRect(270,y+100, 190,0,20,20);

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
                if(mx > 80 && mx < 260 && my > 580 && my < 670) {
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
        public void mouseReleased(MouseEvent e) {}
        public void mouseEntered(MouseEvent e) {}
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
