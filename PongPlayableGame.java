package mlwithpong;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.Timer;

/**
 *
 * @author devang
 */
public class PongPlayableGame {

    private Timer     gameRepaintTimer;      // Timer for constant screen refresh
    private Timer     paddleMoveTimer;       // Timer for polling keyboard input
    private final int gameRepaintDelay = 50; // milliseconds - screen refresh
    private final int paddleMoveDelay  = 10; // milliseconds - keybord polling rate
            
    /**
     * PongPlayableGame Constructor: Begins game and main game loop<br>
     *  - sets up a frame<br>
     *  - initializes pongCanvas for drawing and keyboard listening<br>
     *  - sends pongGame to pongCanvas<br>
     *  - starts new game of pongCanvas<br>
     * <p>
     * Starts a PongGame, the actual functioning and mechanics of a pong game<br>
     * 
     * @param playerOneHuman true if left-player is human, false if CPU-controlled
     * @param playerTwoHuman true if right-player is human, false if CPU-controlled
     * @throws InterruptedException To Handle Thread sleeping until game ends
     */
    public PongPlayableGame(boolean playerOneHuman,boolean playerTwoHuman) throws InterruptedException
    {
        PongGame pongGame = new PongGame(playerOneHuman,playerTwoHuman);
        // initialize the game and canvas
        PongCanvas pongCanvas = new PongCanvas(pongGame);
        pongCanvas.setSize(pongGame.getAttribute(GameAttributes.GAME_WIDTH), pongGame.getAttribute(GameAttributes.GAME_HEIGHT));

        // initialize a frame in which to place the canvas
        Frame frame = new Frame("MLWithPong");
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                System.exit(0);
            }
        });
        frame.setLocation(100,100);
        frame.add(pongCanvas);
        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);
        
        // check for end of game, otherwise refresh screen
        ActionListener gameRepaintAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                pongGame.updateBall();
                if (pongGame.isMatchOver()) { paddleMoveTimer.stop(); gameRepaintTimer.stop(); }
                if (pongGame.isGameOver()) pongGame.startNewGame();
                pongCanvas.repaint();
            }
        };

        // refresh position of paddles
        ActionListener paddleMoveAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                pongCanvas.updatePaddles(pongGame.isPlayerOneHuman(), pongGame.isPlayerTwoHuman());
            }
        };
        
        // begin the constant screen refresh and keyboard polling timers
        gameRepaintTimer = new Timer(gameRepaintDelay, gameRepaintAction);
        paddleMoveTimer = new Timer(paddleMoveDelay, paddleMoveAction);
        paddleMoveTimer.start();
        gameRepaintTimer.start();
        
        // begin the game
        pongGame.startNewGame();
        
        // check for end of game and then exit the game
        while(!pongGame.isMatchOver()) Thread.sleep(10000);       
        if (pongGame.getAttribute(GameAttributes.PLAYER_ONE_SCORE) >= pongGame.getAttribute(GameAttributes.WINNING_SCORE)) System.out.println("player 1 wins match");
        if (pongGame.getAttribute(GameAttributes.PLAYER_TWO_SCORE) >= pongGame.getAttribute(GameAttributes.WINNING_SCORE)) System.out.println("player 2 wins match");
        System.out.println("GAME OVER");
        System.exit(0);
    }
}
