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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import javax.swing.JOptionPane;

public class MainGame extends JFrame implements ActionListener{

    public static void main(String[] args) {
        new MainGame();
    }

    //gamestates, used in if statements thoughout program
    enum GS {
        PLAYING,
        PAUSED,
        LEVELS,
        WIN,
        CREDITS
    }

    //global variables
    final static int SCRW = 500, SCRH = 800;
    final static int CELLW = SCRW/6, CELLH = SCRW/6;
    final static int maxLevels = 6;
    //fonts
    Font f = new Font("Monospaced", Font.BOLD, 30);
    Font big = new Font("Monospaced", Font.BOLD, 50);
    Font creditFont = new Font("Monospaced", Font.BOLD, 25);

    //colors
    Color blue = new Color(167, 199, 231);
    Color yellow = new Color(248, 241, 174);
    Color darkBlue = new Color(61, 66, 107);
    Color pink = new Color(242, 196, 229);
    Color darkYellow = new Color(201, 183, 81);
    Color darkPink = new Color(209, 148, 192);
    DrawingPanel DP = new DrawingPanel();
    //timers and time related
    Timer t = new Timer(1, this); //used for updating paintcomponent and checking win
    Timer counter = new Timer(1000, new Counter()); //used for counting the time passing in each level
    Timer creditsTimer = new Timer(50, new CreditsTimer()); //used for moving text on the credits

    int sec = 0, min = 0;
    int credTimer = 0;
    int credY = 0;
    int moves = 0; //this will be used for scoring (decides how many stars user gets for each level)
    MouseListener ML = new ML();
    Image pauseButton, restartButton, logo, eStar, star; //Images
    //ints used to store the location of the mouse when it is first clicked(mx1, my1), and then where it is let go(mx2, my2)
    int mx1, my1, mx2, my2;
    GS gamestate = GS.LEVELS; //setting the starting screen
    int[][] board = new int[6][6]; //creates board CANNOT BE CHANGED!!
    boolean[][] levelStars = new boolean[6][3]; //holds true and false for each level (true meaning they earned a star, false meaning empty star)
    boolean[] currentStars = new boolean[3]; //stars earned on the current level (used to display what they got on the level so that they can retry and see how they did)
    int level = 5; // level starting at 0
    final static int boardOffset = 165; //how far down the board is from top of screen
    boolean vert = false; //if the car being worked on is vertical or horizontal (true = vertical, false = horizontal)
    int ix, iy, ix2, iy2; //selected car (finds the cell when you first click, and cell when you let go)
    Button large, medium, quit, credits, exit, boardBackground; //types of buttons
    Button[] levels = new Button[6]; //for the level buttons on the levels/home/title screen

    //the color of each car number from 1 - 9
    final static Color[] carColors = {Color.RED, Color.BLUE, Color.PINK, Color.ORANGE, Color.GREEN, Color.CYAN, Color.MAGENTA, Color.BLACK};
    int[] minMoves = {5, 8, 11, 14, 20, 29}; // the minimum moves for each level, index corrosponding to level

    MainGame() {
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setTitle("Rush Hour Ripoff");
        this.setResizable(false);
        this.addMouseListener(ML);

        //loading images
        pauseButton = loadImage("pause.png").getScaledInstance(100, 100, Image.SCALE_DEFAULT);
        restartButton = loadImage("replay.png").getScaledInstance(110, 110, Image.SCALE_DEFAULT);
        logo = loadImage("RushHourLogo.png").getScaledInstance(400, 180, Image.SCALE_DEFAULT);
        
        //creating button objects
        large = new Button(80, 490, 340, 60, 20, 20);
        medium = new Button(80, 580, 180, 60, 20, 20);
        quit = new Button(280, 580, 140, 60, 20, 20);
        boardBackground = new Button(0, boardOffset, 500, 500);
        credits = new Button(375, 225, 50, 50);
        exit = new Button(450, 10, 40, 40, 20, 20);

        //temp variable to help change the y value of the level buttons in the level screen. It will times the y value to place the buttons in the right row
        int row = 1; 
        //for loop is referring i to the level number
        for(int i = 0; i < 6; i++) {
            if(i%2 ==0 && i!=0) row++; //the levels draw two in each row. Therefore, every two levels needs to be on the next line, row is increased
            levels[i] = new Button(((i%2 != 0) ? 270 : 40), 200*row, 190,150,20,20); //creating identical buttons for each level in rows
        } 
        this.add(DP);
        this.pack();
        this.setVisible(true);
        this.setLocationRelativeTo(null);
        createBoardArray(); //method to read board from the file
        t.start();
        counter.start();
        JOptionPane.showMessageDialog (null, "GOAL: \n    - Get the red car to the very left of the board\n    - Exit is bolded\n" +
                                                            "RULES: \n    - Cars must move parallel to where they are facing\n    - Cars may only move until they hit an object" + 
                                                            "\nCONTROLS: \n    - Click and hold car -> drag to desired location, Release"+
                                                            "\nUI: \n    - Pausing -> You are able to select your level, quit, or continue\n    - Pausing will pause all activities\n    - The restart button will restart your level", "How To Play", JOptionPane.INFORMATION_MESSAGE);
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
    void createBoardArray() {
        Scanner sc;
        try {
            // scan the maps file
            sc = new Scanner(new File("maps.txt"));

            // needed counting variables
            int map = 0;
            int lineNumber = 0;

            
            while(sc.hasNextLine()) {
                // get the line
                String s = sc.nextLine();

                // if it is #, the next 6 are a new map
                if(s.charAt(0) == '#'){
                    // next map
                    map++;
                    continue;
                }
                
                // if the map and level are the same, create the board
                if(map == level) {

                    // change to ints
                    int[] arr = toIntArray(s);

                    // add to board
                    board[lineNumber] = arr;

                    // go to next line
                    lineNumber++;
                }
                if(map > level) break;
            }
        } catch (FileNotFoundException e) {
            System.err.println("CANT FIND FILE");
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

        // get buttons
        Button b1 = levels[l-1];
        Button b2 = levels[l];
        
        // pink boxes
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
        drawStars(g2, l, 50, y, 50,50, levelStars[l-1]);
        l++;
        drawStars(g2, l, 280, y, 50, 50, levelStars[l-1]);
        g2.drawString("Level " + l, 300, y+135);
    }

    /**
     * draws stars for scoring
     * @param g2 the graphics canvas
     * @param lvl the current level to get stars
     * @param x starting x coord for the stars
     * @param y the starting y coord for the stars
     */
    void drawStars(Graphics2D g2, int lvl, int x, int y, int w, int h, boolean[] strs) {

        // loading star images and size
        eStar = loadImage("emptyStar.png").getScaledInstance(w, h, Image.SCALE_DEFAULT);
        star = loadImage("star.png").getScaledInstance(w, h, Image.SCALE_DEFAULT);

        // draw each star
        for(int i = 0; i < 3; i++) {
            // draws full star or empty star based on value in array, if star is the middle star it will draw slightly higher --> format and draws stars
            g2.drawImage(((strs[i]) ? star : eStar), x+(i*w+10), y+ ((i == 1) ? 20 : 35), null); 
        }
    } 

    /**
     * Used to draw pause and win popups to save writing code twice
     * @param title title of popup (PAUSED or LEVEL WIN)
     * @param btn1 the first and biggest popup that is coloured yellow
     * @param btn2 smaller button on left
     * @param btn3 quit button on right
     */
    void drawPopup(Graphics2D g2, String title, String btn1, String btn2, String btn3){
       
        // HEADER
        g2.setStroke(new BasicStroke(5));
        g2.setColor(pink);
        g2.fillRect(50, 100, 400,600);
        g2.setColor(blue);
        g2.fillRect(50, 100, 400,100);
        g2.setFont(big);
        g2.setColor(darkBlue);                
        g2.drawLine(50,200,450,200);
        g2.drawString(title, 158,170);

        // BUTTONS
        g2.setColor(yellow);
        g2.fillRoundRect(large.x, large.y, large.w, large.h, large.aW, large.aH); //main btn (btn1)
        g2.setColor(blue);
        g2.fillRoundRect(medium.x, medium.y, medium.w, medium.h, medium.aW, medium.aH); //  levels
        g2.fillRoundRect(quit.x, quit.y, quit.w, quit.h, quit.aW,quit.aH); // quit

        // OUTLINES
        g2.setColor(darkYellow);
        g2.drawRoundRect(80, 490, 340, 60, 20, 20);
        g2.setColor(darkBlue);
        g2.drawRoundRect(80, 580, 180, 60, 20, 20);
        g2.drawRoundRect(280, 580, 140, 60, 20, 20);

        // TITLES
        g2.setFont(f);
        g2.drawString(btn1,140,530);
        g2.setColor(yellow);
        g2.drawString(btn2,115,620);
        g2.drawString(btn3,312,620);
        g2.setColor(darkBlue);
        g2.drawRect(50, 100, 400,600);
    }

    /**
     * calculate the stars earned by the player
     */
    void calculateStars() {
        int stars = 0; // star vairble, total stars
        
        // if their moves are within 3 of the minimum, they get 3 stars
        if(moves <= minMoves[level]+3) stars = 3;
        // between +3 and +6 gives 2 stars
        if(moves >= minMoves[level]+3 && moves < minMoves[level] + 6) stars = 2;
        //between +6 and +9 of minimum gives 1 star --> any higher is 0
        if(moves >= minMoves[level]+6 && moves < minMoves[level] + 9) stars = 1;

        // loop from 0 to the amount of earned stars and make that amount of booleans in the array true
        for(int i = 0; i < stars; i ++) {
            currentStars[i] = true;
        }

        int lastStars = 0; // previously stored amount of stars for the level
        for(int i = 0; i < 3; i++) {
            // if there is a star(true), add to the previous amount
            if(levelStars[level][i]) lastStars++; 
        }

        // if their new score is higher, update the old score
        if(lastStars < stars) levelStars[level] = currentStars;
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
            if(gamestate == GS.PLAYING || gamestate == GS.PAUSED || gamestate == GS.WIN) { //because PAUSED and WIN are popups, they share the same background as PLAYING
                g2.setColor(pink);
                g2.setStroke(new BasicStroke(3));

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
                g2.drawString(min + ":" + ((sec < 10) ? "0" : "") + sec, 105, 100); //displays the secs and mins
                g2.drawString("" + moves, 385, 100);

                // BOARD BACKGROUND
                g2.setColor(yellow);
                g2.fillRect(boardBackground.x, boardBackground.y, boardBackground.w, boardBackground.h);
                g2.setStroke(new BasicStroke(5));
                g2.setColor(darkYellow);
                
                // draw the cells of the board & cars
                for(int i = 0; i < board.length; i++) {
                    for(int j = 0; j < board.length; j++) {
                        g2.setColor(darkYellow);
                        g2.drawRect(j*CELLW, boardOffset+i*CELLH, CELLW, CELLH); //draws each cell as a dark yellow rect

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

                // pause, restart images
                g2.drawImage(pauseButton, 50, 680, null);
                g2.drawImage(restartButton, 340, 680, null);
                g2.setStroke(new BasicStroke(20));
                g2.setColor(darkYellow);
                g2.drawLine(SCRW, CELLW*2+boardOffset, SCRW, CELLW*3+boardOffset);
            }
            
            if(gamestate == GS.PAUSED){
                //Uses drawPopup method to create the frame and buttons
                drawPopup(g2, "PAUSED", "Back To Game", "Levels", "Quit");
                g2.setFont(f);
                g2.setColor(darkBlue);

                //credits
                g2.setColor(blue);
                g2.fillOval(credits.x, credits.y, credits.w, credits.h);
                g2.setColor(darkBlue);
                g2.drawOval(credits.x, credits.y, credits.w, credits.h);
                g2.setColor(yellow);
                g2.drawString("C", credits.x+15, credits.y+35);

                //Statistics         
                g2.setColor(darkBlue);        
                g2.drawString("Level: " + (level+1), 175, 250);
                g2.drawString("Moves: " + moves, 175, 350);
                g2.drawString("Time: " + min + ":" + ((sec < 10) ? "0" : "") + sec, 175, 450);
            }

            if(gamestate == GS.WIN){
                String lrgBtn; // string for large button

                // create message, if its the last level there is no next level
                if(level < 5) lrgBtn = "Play Level " + (level+2);
                else lrgBtn = "Go To Levels";

                //draw
                drawPopup(g2, "Level " + (level+1), lrgBtn, "Replay", "Quit");
                
                // add stars
                drawStars(g2, level+1, 90, 200, 100, 100, currentStars);
                g2.setFont(f);
                
                // moves and time
                g2.drawString("Moves: " + moves, 175, 380);
                g2.drawString("Time: " + min + ":" + ((sec < 10) ? "0" : "") + sec, 175, 450);
            }
            
             if(gamestate == GS.LEVELS){
                
                // Rush Hour logo
                g2.drawImage(logo, 58, 0, null);

                // level section
                g2.setColor(darkBlue);
                g2.drawString("Easy", 40, 190);
                g2.drawString("Medium", 40, 390);
                g2.drawString("Hard", 40, 590);
                
                // draw box groups
                drawBoxes(g2, 200, 1);
                drawBoxes(g2, 400, 3);
                drawBoxes(g2, 600, 5);
            }

            if(gamestate == GS.CREDITS) {
                creditsTimer.start();
                g2.setBackground(Color.BLACK);
                g2.setColor(yellow);
                g2.setFont(creditFont);
                g2.drawString("RUSH HOUR", (int) centerFont("RUSH HOUR"), 100-credY);                
                g2.drawString("By Sammy and Arielle", (int) centerFont("By Sammy and Arielle"), 150-credY);
                
                g2.drawString("Special thanks", (int) centerFont("Special thanks"), 800-credY);
                g2.drawString("to Mr. Harwood", (int) centerFont("to Mr. Harwood"), 850-credY);     
                g2.drawString("The best teacher at central!", (int) centerFont("The best teacher at central!"), 900-credY);
                g2.drawString("Good luck!!!", (int) centerFont("Good luck!!!"), 950-credY);

                g2.setColor(Color.RED);
                g2.fillRoundRect(exit.x, exit.y, exit.w, exit.h, exit.aW, exit.aH);
                if(credTimer >= 1000) {
                    gamestate = GS.PAUSED;
                    credTimer = 0;
                    credY = 0;
                    creditsTimer.stop();
                }
            }
            
        } 
    }

    double centerFont(String s) {
        double xVal = 0;
        int space = creditFont.getSize()/2 * s.length();
        xVal = (SCRW-space)/2.0;
        return xVal-creditFont.getSize()-s.length()/creditFont.getSize()/2;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DP.repaint();
        // if red is in winning position
        if(redWon() && gamestate == GS.PLAYING) {
            calculateStars();
            gamestate = GS.WIN;
        }
    }
    
    /**
     * get direction the car will move
     * @return return the direction 1 = down or right, -1 = up or left
     */
    int getDirection() {
        int dir = 0;

        // find if it goes left right, up down
        if(mx1-mx2 > 0 && !vert || my1-my2 > 0 && vert) dir = -1;
        else dir = 1;
        return dir;
    }

    /**
     * move the car
     * @param dir the direction of the car
     */
    void moveCar(int dir) {

        int selectedCar = board[ix][iy]; // which number is selected for the car
        int[] line = findLine(); // find the line the car is on
        ix2 = (my2-boardOffset-30)/CELLW; // cell x coordinate
        iy2 = mx2/(CELLH); // cell y coordinate
        boolean moved = false; // if the car has been moved

        for(int i = 0; i < line.length; i++) {

            // make it loop forwards or backwards based on which direction --> must meet head first
            int temp = (dir == 1) ? Math.abs(i-5) : i;
            // if the loop meets the car
            
            if(line[temp] == selectedCar) {
            
                // moving code
                while(true) {

                    // all break conditions
                    if(checkMove(line, temp, dir, selectedCar)) break;

                    // show it was a valid move
                    if(!moved) {
                        moved = true;
                        moves++;
                    }

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

    /**
     * check whether the move is valid
     * @param line the line the car will move on
     * @param temp the index 
     * @param dir direction
     * @param car selected car
     */
    boolean checkMove(int[] line, int temp, int dir, int car) {
        // if the temp index moves out of the board
        if(temp+dir < 0 || temp+dir >= board.length) return true;

        // if the next move is occupied by any car
        if(line[temp+dir] != 0) return true;

        // if the next index is past where their move went (vertical and horizonatal)
        if(!vert && temp+dir < iy2 && dir == -1 || !vert && temp+dir > iy2 && dir == 1) return true;
        if(vert && temp+dir < ix2 && dir == -1 || vert && temp+dir > ix2 && dir == 1) return true;

        return false;
    }

    // set all values to default for reset
    void setDefault() {
        moves = 0;
        sec = 0;
        min = 0;
        createBoardArray();
    }

    class ML implements MouseListener {

        @Override
        public void mousePressed(MouseEvent e) {

            //get mouse coords
            mx1 = e.getX(); my1 = e.getY();
            //CREATE BUTTON CLASS FOR CLICKING
            
            if(gamestate == GS.PLAYING) {
                // if they hit the pause button
                if(mx1 > 50 && mx1 < 150 && my1 > 680 && my1 < 780 && gamestate == GS.PLAYING) gamestate = GS.PAUSED;
                
                // if they hit the restart button --> set everything to default
                if(mx1 >350 && mx1 < 415 && my1 > 695 && my1 < 760 && gamestate == GS.PLAYING) setDefault();
            }

            // x and y coordinates for the mouse, -30 to account for offset
            int x = mx1-30;
            int y = my1-30;
            
            // if they hit back to game while pasued --> continue
            if(gamestate == GS.LEVELS) {
                // loop through the level button array
                for(int i = 0; i < levels.length; i++) {
                    Button b = levels[i]; // get the current buttons
                    
                    // if their mouse coordinates are in the button
                    if(b.contains(x, y)) {

                        // set level to the current button index
                        level = i;

                        // reset values and set playing
                        setDefault();
                        gamestate = GS.PLAYING;
                    }
                }
            }

            // if they are paused(button clicking)
            if(gamestate == GS.PAUSED) {
                //if they click the back to game button
                if(large.contains(x, y)) gamestate = GS.PLAYING;
                // if they click the levels button
                if(medium.contains(x, y)) gamestate = GS.LEVELS;
                //if they hit the quit button
                if(quit.contains(x, y)) System.exit(1);
                if(credits.contains(x,y)) gamestate = GS.CREDITS;
            }
            
            // if they win                
            if(gamestate == GS.WIN) {

                // if they click the next level button
                if(large.contains(x, y) && level < 5) {

                    // go to next
                    level++;

                    //reset defaults
                    setDefault();
                    currentStars = new boolean[3];
                    gamestate = GS.PLAYING;
                }
                // if they are on the last level --> it is a go to levels button
                if(level == 5) gamestate = GS.LEVELS;
                
                // if they click replay
                if(medium.contains(x, y)) {

                    // reset to defaults
                    setDefault();
                    currentStars = new boolean[3];
                    gamestate = GS.PLAYING;
                }

                // quit button
                if(quit.contains(x, y)) System.exit(1);
            }

            if(gamestate == GS.CREDITS) {
                if(exit.contains(x, y)) gamestate = GS.PAUSED;
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
            
            // if they dont click the board or dont click a car
            if(board[ix][iy] == 0 || !(boardBackground.contains(mx1,my1)) 
            || gamestate != GS.PLAYING) return;

            // mouse coordinates
            mx2 = e.getX(); my2 = e.getY();

            // check if vertical or not
            checkAdj(ix, iy);

            // move the car, give direction
            moveCar(getDirection());
        }


        // unused methods
        @Override
        public void mouseEntered(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}
        public void mouseClicked(MouseEvent e) {}
    }


    //used for keeping track of time
    class Counter implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

            // if they are playing add seconds
            if(gamestate == GS.PLAYING) sec++;
            // if seconds is 60(minute) set seconds to 0 and add to minutes
            if(sec == 60) {
                min++;
                sec = 0;
            }
            
        }

    }

    class CreditsTimer implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if(gamestate == GS.CREDITS) {
                credTimer++;
                credY+=1;
            }
        }

    }
     
}