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
import java.awt.Point;
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
import java.util.ArrayList;
import java.util.Arrays;

public class MainGame extends JFrame implements ActionListener{

    public static void main(String[] args) {
        new MainGame();
    }

    enum GS {
        PLAYING,
        PAUSED,
        LEVELS,
        WIN
    }

    final static int SCRW = 500, SCRH = 800;
    final static int CELLW = SCRW/6, CELLH = SCRW/6;
    final static int maxLevels = 6;
    Font f = new Font("Monospaced", Font.BOLD, 30);
    Font big = new Font("Monospaced", Font.BOLD, 50);
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
    Image pauseButton, restartButton, logo, eStar, star;
    int mx1,my1, mx2, my2;
    GS gamestate = GS.LEVELS; ///////////////////////////////////////////////////////////////////////////////////////////////
    int[][] board = new int[6][6];
    boolean[][] stars = new boolean[6][3];
    int level = 0; // level starting at 0
    final static int boardOffset = 165;
    boolean vert = false;
    int ix, iy; // selected car
    Button back, levelButton, quit;
    Button[] levels = new Button[6];

    final static Color[] carColors = {Color.RED, Color.BLUE, Color.PINK, Color.ORANGE, Color.GREEN, Color.CYAN, Color.MAGENTA, Color.BLACK};

    MainGame() {
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setTitle("Rush Hour Ripoff");
        this.setResizable(false);
        this.addMouseListener(ML);

        pauseButton = loadImage("pause.png").getScaledInstance(100, 100, Image.SCALE_DEFAULT);
        restartButton = loadImage("restart.png").getScaledInstance(65, 65, Image.SCALE_DEFAULT);
        logo = loadImage("RushHourLogo.png").getScaledInstance(400, 180, Image.SCALE_DEFAULT);
        eStar = loadImage("emptyStar.png").getScaledInstance(50, 50, Image.SCALE_DEFAULT);
        star = loadImage("star.png").getScaledInstance(50, 50, Image.SCALE_DEFAULT);

        back = new Button(80, 490, 340, 60, 20, 20);
        levelButton = new Button(80, 580, 180, 60, 20, 20);
        quit = new Button(280, 580, 140, 60, 20, 20);

        int diff = 1; 
        for(int i = 0; i < 6; i++) {
            if(i%2 ==0 && i!=0) diff++;
            System.out.print(i);
            levels[i] = new Button(((i%2 != 0) ? 270 : 40), 200*diff, 190,150,20,20);
        } 

        this.add(DP);
        this.pack();
        this.setVisible(true);
        this.setLocationRelativeTo(null);
        createBoard();
        t.start();
        counter.start();
    }

    /**
     * loads image to draw
     * @param filename name of file(image)
     * @return image
     */
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

    /**
     * creates board from txt file full of maps
     */
    void createBoard() {

        // loop through lines in file
        for(int i = 0; i < board.length; i++) {
            try {
                // if there are more lines than the level times board length --> ensure that level exists (eg level 4, lines must be greater than 24)
                if(Files.lines(Paths.get("maps.txt")).count() > level*board.length) {
                    
                    // get line from file(piece of board array)
                    String line = Files.readAllLines(Paths.get("maps.txt")).get(level*board.length+i);
                    
                    // change from chars to ints
                    int[] arr = toIntArray(line);

                    // add to array
                    board[i] = arr;
                }
            } catch (IOException e) {
                System.err.println("IO EXCEPTION");
            }
        }
        
    }

    /**
     * changes characters in string to array
     * @param s string for converting
     * @return return array of numbers
     */
    int[] toIntArray(String s) {
        int[] arr = new int[6];
        // loop through new array
        for(int i = 0; i < arr.length; i++) {
            //change char number to int number, must subtract '0'(48) from char
            arr[i] = s.charAt(i)-'0';
        }
        return arr;
    }

    /**
     * checks adjacent blocks --> used for drawing carsa & finding ends
     * @param a first array coordinate --> which array
     * @param b second array coordinate --> which number
     * @return return number representing adjacent blocks 0 = 2 blocks, 1 = block on left, -1 = block on right
     */
    int checkAdj(int a, int b) {
        int num = 0; // adjacent blocks

        // if block is above selected
        if(a > 0 && board[a][b] == board[a-1][b]) {
            num++;
            vert = true;
        }

        // if block is bellow selected
        if(a < board.length-1 && board[a][b] == board[a+1][b]) {
            num--;
            vert = true;
        }

        // if block is to the left of selected
        if(b > 0 && board[a][b] == board[a][b-1]) {
            num++;
            vert = false;
        }

        // if block is right of selected
        if(b < board.length-1 && board[a][b] == board[a][b+1]) {
            num--;
            vert = false;
        }
        
        //0,1,-1
        return num;
    }

    /** 
     * gets the line to move the car
     * @return returns line car is in
     */
    int[] findLine() {
        int[] arr = new int[6];
        // use IX dont do this sillt

        // if the car is horizontal
        if(!vert) {
            // array is the array from the board --> no need to update separately
            arr = board[ix];
        }

        // it is vertical
        else {
            // create an array for vertical
            for(int i = 0; i < board.length; i++) {
                arr[i] = board[i][iy];
            }
        }

        return arr;
    }

    /**
     * checks if red is in the winning position
     * @return returns boolean value
     */
    boolean redWon() {
        boolean b = false;

        // red block(1) in ending position
        if(board[2][5] == 1) b = true;
        return b;
    }

    /**
     * draws level boxes
     * @param g2 graphics plane
     * @param y y value of button
     * @param lvl level number
     */
    void drawBoxes(Graphics2D g2, int y, int l){

        // add to level buttons
        Button b1 = levels[l-1];
        Button b2 = levels[l];
        
        g2.setColor(pink);
        g2.fillRoundRect(b1.x, b1.y, b1.w, b1.h, b1.aW, b1.aH);
        g2.fillRoundRect(b2.x, b2.y, b2.w, b2.h, b2.aW, b2.aH);

        // text background yellow square
        g2.setColor(yellow);
        g2.fillRoundRect(40,y+100, 190,50,20,20);
        g2.fillRoundRect(270,y+100, 190,50,20,20);

        // words, stars
        g2.setColor(blue);
        g2.drawString("Level " + l, 70, y+135);
        drawStars(g2, l, 50, y);
        l++;
        drawStars(g2, l, 280, y);
        g2.drawString("Level " + l, 300, y+135);
    }

    /**
     * draws stars for scoring
     * @param g2 the graphics canvas
     * @param lvl the current level to get stars
     * @param x starting x coord for the stars
     * @param y the starting y coord for the stars
     */
    void drawStars(Graphics2D g2, int lvl, int x, int y) {

        // get stars for level
        boolean[] lvlStars = stars[lvl-1];

        // draw each star
        for(int i = 0; i < 3; i++) {
            g2.drawImage((lvlStars[i]) ? star : eStar, x+(i*60), y+ ((i == 1) ? 20 : 35), null);
        }
    } 

    void drawPopup(Graphics2D g2, String title, String btn1, String btn2, String btn3){
       
        g2.setStroke(new BasicStroke(5));
        g2.setColor(pink);
        g2.fillRect(50, 100, 400,600);
        g2.setColor(blue);
        g2.fillRect(50, 100, 400,100);
        g2.setFont(big);
        g2.setColor(darkBlue);                
        g2.drawLine(50,200,450,200);
        g2.drawString(title, 158,170);
        g2.setColor(yellow);
        g2.fillRoundRect(back.x, back.y, back.w, back.h, back.aW, back.aH); //main btn (btn1)
        g2.setColor(blue);
        g2.fillRoundRect(80, 580, 180, 60, 20, 20);
        g2.fillRoundRect(280, 580, 140, 60, 20, 20);
        g2.setColor(darkYellow);
        g2.drawRoundRect(80, 490, 340, 60, 20, 20);
        g2.setColor(darkBlue);
        g2.drawRoundRect(80, 580, 180, 60, 20, 20);
        g2.drawRoundRect(280, 580, 140, 60, 20, 20);
        g2.setFont(f);
        g2.drawString(btn1,140,530);
        g2.setColor(yellow);

        g2.drawString(btn2,115,620);
        g2.drawString(btn3,312,620);
        g2.setColor(darkBlue);
        g2.drawRect(50, 100, 400,600);
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

            // if they are playing or paused, draw screen
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
                g2.setFont(big);
                g2.drawString(min + ":" + ((sec < 10) ? "0" : "") + sec, 105, 100);
                g2.drawString("" +moves, 385, 100);

                // BOARD
                g2.setColor(yellow);
                g2.fillRect(0, boardOffset, 500, 500);
                g2.setStroke(new BasicStroke(5));
                g2.setColor(darkYellow);

                for(int i = 0; i < board.length; i++) {
                    for(int j = 0; j < board.length; j++) {
                        g2.setColor(darkYellow);
                        g2.drawRect(j*CELLW, boardOffset+i*CELLH, CELLW, CELLH);

                        // drawing cars --> if is not empty
                        if(board[i][j] > 0) {
                            // check adjacent blocks
                            int num = checkAdj(i, j);

                            // create offsetting values
                            int x1= 0,y1= 0,x2 = 0,y2 = 0;
                            
                            // if vertical
                            if(vert) {

                                // change to vertical offsets for centering(both sides have blocks)
                                x1 = 10;
                                y1 = -3;
                                x2 = -20;
                                y2 = 3;

                                // if one adjacent to left
                                if(num == 1) y2 = -10;

                                // if adjacent to right
                                if(num == -1) {
                                    y1 = 10;
                                    y2 = -10;
                                }
                            }

                            // otherwise it is horozontal
                            else {

                                // if both sides have blocks
                                x1 = -3; // spacing between line and car
                                y1 = 10;// spacing from top
                                x2 = 3; // spacing between line and car
                                y2 = -20; // spacing from bottom

                                // one to left
                                if(num == 1) x2 = -10;

                                // one to right
                                if(num == -1) {
                                    x1 = 10;
                                    x2 = -10;
                                }
                            }

                            // draw car with corrosponding color
                            g2.setColor(carColors[board[i][j]-1]);
                            g2.fillRect(j*CELLW+x1, boardOffset+i*CELLH+y1, CELLW+x2, CELLH+y2);                                
                        }
                    }
                }

                // pause, restart
                g2.drawImage(pauseButton, 50, 680, null);
                g2.drawImage(restartButton, 350, 695, null);
            }
            if(gamestate == GS.PAUSED){
                drawPopup(g2, "PAUSED", "Back To Game", "Levels", "Quit");
                g2.setFont(big);
                g2.drawString("PAUSED", 158,170);
                g2.setFont(f);
                g2.setColor(darkBlue);                
                g2.drawString("Level: " + 3, 175, 250);
                g2.drawString("Moves: " + moves, 175, 350);
                g2.drawString("Time: " + min + ":" + sec, 175, 450);

            }

            if(gamestate == GS.LEVELS){
                
                g2.drawImage(logo, 58, 0, null);

                g2.setColor(darkBlue);
                g2.drawString("Easy", 40, 190);
                g2.drawString("Medium", 40, 390);
                g2.drawString("Hard", 40, 590);

                drawBoxes(g2, 200, 1);
                drawBoxes(g2, 400, 3);
                drawBoxes(g2, 600, 5);
            }

            if(gamestate == GS.WIN){
                drawPopup(g2, "Level" + (level+1) + " Complete!", "Levels", "Retry", "Quit");

                // g2.drawString("Level " + (level+1) + " Complete!", 100,50);
                //draw stars
                
                drawStars(g2, level+1, 100, 150);
                g2.setFont(f);
                
                g2.drawString("Moves made: " + moves, 100, 300);
                g2.drawString("Time taken: " + min + ":" + sec, 100, 350);
                
                
            }
        }
    }

    

    

    @Override
    public void actionPerformed(ActionEvent e) {
        DP.repaint();

        // if they are paused dont continue
        if(gamestate == GS.PAUSED) {
            return;
        }

        // if red is in winning position
        if(redWon()) {
            gamestate = GS.WIN;
        }
    }

    class ML implements MouseListener {

        @Override
        public void mousePressed(MouseEvent e) {

            //get mouse coords
            mx1 = e.getX(); my1 = e.getY();

            //CREATE BUTTON CLASS FOR CLICKING

            if(gamestate == GS.LEVELS){}
            // if they hit the pause button
            if(mx1 > 50 && mx1 < 150 && my1 > 680 && my1 < 780 && gamestate == GS.PLAYING) {
                gamestate = GS.PAUSED;
            }

            if(mx1 >350 && mx1 < 415 && my1 > 695 && my1 < 760 && gamestate == GS.PLAYING) {
                moves = 0;
                sec = 0;
                min = 0;
                createBoard();
            }
            
            // if they hit back to game while pasued --> continue
            if(gamestate == GS.PAUSED) {
                
                // if they resume
                if(mx1 > 80 && mx1 < 420 && my1 > 490 && my1 < 580) {
                    gamestate = GS.PLAYING;
                }

                // if they hit levels
                if(mx1 > 80 && mx1 < 260 && my1 > 580 && my1 < 670) {
                    gamestate = GS.LEVELS;
                }

                // if they hit quit
                if(mx1 > 280 && mx1 < 420 && my1 > 580 && my1 < 670) {
                    // QUIT CODE
                    System.exit(1);
                }
            }
            
            // if they click within the board
            if(mx1 > 0 && mx1 < SCRW && my1 > boardOffset && my1 < SCRW+boardOffset) {

                // get selected cell
                ix = (my1-boardOffset-30)/CELLW;
                iy = mx1/(CELLH);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {

            // get all coordinates --> which cell based on whre they let the mouse go
            if(board[ix][iy] ==0 || !(mx1 > 0 && mx1 < SCRW && my1 > boardOffset && my1 < SCRW+boardOffset) || gamestate != GS.PLAYING) return;
            if(mx1 != mx2 && my1 != my2) moves++;
            mx2 = e.getX(); my2 = e.getY();
            int ix2 = (my2-boardOffset-30)/CELLW;
            int iy2 = mx2/(CELLH);

            // if they did not select a car
            // check if vertical or not
            checkAdj(ix, iy);
            
            int dir = 0; // direction it goes, forward or back
            int selectedCar = board[ix][iy]; // which number is selected for the car
            int[] line = findLine(); // find the line the car is on

            // give the direction
            if(mx1-mx2 > 0 && !vert || my1-my2 > 0 && vert) dir = -1;
            else dir = 1;

            // loop through the line with the car
            for(int i = 0; i < line.length; i++) {

                // make it loop forwards or backwards based on which direction --> must meet head first
                int temp = (dir == 1) ? Math.abs(i-5) : i;
                System.out.println(Arrays.toString(line));
                // if the loop meets the car
                
                if(line[temp] == selectedCar) {
                
                    // moving code
                    while(true) {

                        // all break conditions
                        if(temp+dir < 0 || temp+dir >= board.length || line[temp+dir] == selectedCar || line[temp+dir] != 0 
                            || !vert && temp+dir < iy2 && dir == -1 || !vert && temp+dir > iy2 && dir == 1 || vert && temp+dir < ix2 && dir == -1 
                            || vert && temp+dir > ix2 && dir == 1) break;

                        // move the car one in the direction
                        line[temp+dir] = line[temp];
                        //make previous location empty
                        line[temp] = 0;
                        // moves car index
                        temp+=dir;
                    }
                }
                // update array only if vertical(doesnt take an actual array)
                for(int j = 0; vert && j < board.length; j++) {
                    board[j][iy] = line[j];
                }
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}
        public void mouseClicked(MouseEvent e) {}

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
