/*
 * PongSaved - draws a saved game stored in a txt file
 */
package mlwithpong;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * PongSaved: loads txt file and animates the pong game stored in it<br>
 * @author devang
 */
public class PongSavedGame {

    private int gameWidth = 320;
    private int gameHeight = 240; 
    
    private int x = gameWidth/2;
    private int y = gameHeight/2;
    private int playerOneY = gameHeight/2;
    private int playerTwoY = gameHeight/2;
    
    private int paddleHeight = 39;
    
    private int dataCount;
    
    private Canvas pongCanvas;
    
    private final int dividerWidth = 3;
    private final int ballWidth = 3;
    private final int ballHeight = 3;
    private final int paddleWidth = 3;
    private final int playerOneScoreFontSize = 25;
    private final int playerTwoScoreFontSize = 25;
             
    
    /**
     * PongSaved constructor: starts playing game with data saved in txt file
     * 
     * @param filename filename of txt file of game data header,x,y,p1y,p2y...
     */
    public PongSavedGame(String filename)
    {
        try {
            Scanner scanner = openGameDatafile(filename);
            initDisplay();
            animateSavedGame(scanner);
        }
        catch (FileNotFoundException fe)
        {
            System.err.println("File Not Found Exception Thrown");
            fe.printStackTrace();
            System.exit(1);
        }
        catch (InterruptedException ie)
        {
            System.err.println("Interrupted Exception Thrown");
            ie.printStackTrace();
            System.exit(1);
        }
        catch (Exception e)
        {
            System.err.println("User Exception Thrown");
            System.err.println("(probably an error in the header");
            System.exit(1);
        }
        System.exit(0);
    }
    
    private Scanner openGameDatafile(String filename) throws Exception
    {
        File gameFile = new File(filename);
        Scanner scanner = new Scanner(gameFile);
        // read header: # width height paddleHeight dataCount ...data...
        if (!scanner.hasNextLine())
        {
            throw new Exception();
        }
        
        StringTokenizer tokenizer = new StringTokenizer(scanner.nextLine()," ");
        System.out.println(tokenizer.nextToken()); // #
        gameWidth = Integer.valueOf(tokenizer.nextToken());
        gameHeight = Integer.valueOf(tokenizer.nextToken());
        paddleHeight = Integer.valueOf(tokenizer.nextToken());
        dataCount = Integer.valueOf(tokenizer.nextToken());

        System.out.println("width: " + gameWidth);
        System.out.println("height: " + gameHeight);
        System.out.println("paddle-height: " + paddleHeight);
        System.out.println("dataCount: " + dataCount);

        return scanner;
    }
    
    private void initDisplay()
    {
        // initialize the game and canvas
       pongCanvas = new Canvas() {
            @Override
            public void paint(Graphics g) {
                // draw game board Background
                setBackground(Color.BLACK);

                // draw center dividing line (rect: x,y,width,height)
                g.setColor(Color.WHITE);
                g.fillRect(gameWidth/2 - dividerWidth/2, 0, dividerWidth, gameHeight);

                // draw player-1 paddle (left-side player)
                g.setColor(Color.WHITE);
                g.fillRect(0, playerOneY - paddleHeight/2, paddleWidth, paddleHeight);

                // draw player-2 paddle (right-side player)
                g.setColor(Color.WHITE);
                g.fillRect(gameWidth - paddleWidth, playerTwoY -  paddleHeight/2, paddleWidth, paddleHeight);

                // draw ball
                g.setColor(Color.WHITE);
                g.fillRect(x - ballWidth/2, y - ballHeight/2, ballWidth, ballHeight);

                /*
                // draw player-1 score
                g.setColor(Color.WHITE);
                g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, playerOneScoreFontSize));
                g.drawString(Integer.toString(playerOneScore), gameWidth/2 - (playerOneScoreFontSize + 10), playerOneScoreFontSize + 5);

                // draw player-2 score
                g.setColor(Color.WHITE);
                g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, playerTwoScoreFontSize));
                g.drawString(Integer.toString(playerTwoScore), gameWidth/2 + 20, playerTwoScoreFontSize + 5);
                */
            }
        };
        pongCanvas.setSize(gameWidth, gameHeight);

        // initialize a frame in which to place the canvas
        Frame frame = new Frame("Animates a saved game");
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                System.exit(0);
            }
        });
        frame.setLocation(300,300);
        frame.add(pongCanvas);
        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);
    }
    
    private void animateSavedGame(Scanner scanner) throws InterruptedException
    {   
        int counter = 0;
        while (counter < dataCount)
        {
            x = Integer.valueOf(scanner.nextLine());
            y = Integer.valueOf(scanner.nextLine());
            playerOneY   = Integer.valueOf(scanner.nextLine());
            playerTwoY   = Integer.valueOf(scanner.nextLine());
            counter+=4;

            pongCanvas.repaint();
            Thread.sleep(10);
        }
        System.out.println(counter);
    }
}
