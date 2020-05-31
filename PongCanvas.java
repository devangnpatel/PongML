/*
 * PongCanvas - handles drawing and keyboard input for a PongGame
 * 2-player human players
 * 1-player with rudimentary CPU controlled Player
 * 1-player with slightly improved (still rudimentary) CPU controlled player
 * 0-player with computer vs. computer
 */
package mlwithpong;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;

import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

/**
 * PongCanvas<br>
 * -handles screen drawing and keyboard input<br>
 * -supported by GameAttributes, which conveniently enables access to Game Variables<br>
 *  such as screen size and other dimensions necessary for a Pong Game
 */
public class PongCanvas extends Canvas implements KeyListener {

    private PongGame pongGame;
    private boolean  keyDownPressed = false;
    private boolean  keyUpPressed   = false;
    private boolean  keyAPressed    = false;
    private boolean  keyZPressed    = false;
    
    private final int gameWidth;
    private final int gameHeight;
    private final int dividerWidth;
    private final int ballWidth;
    private final int ballHeight;
    private final int paddleWidth;
    private final int paddleHeight;

    /**
     * PongCanvas Constructor: Sets up dimensions and listeners<br>
     * 
     * @param pongGame the actual functioning and mechanics of a pong game
     */    
    public PongCanvas(PongGame pongGame)
    {
        this.pongGame = pongGame;
        
        gameWidth     = pongGame.getAttribute(GameAttributes.GAME_WIDTH);
        gameHeight    = pongGame.getAttribute(GameAttributes.GAME_HEIGHT);
        dividerWidth  = pongGame.getAttribute(GameAttributes.DIVIDER_WIDTH);
        ballWidth     = pongGame.getAttribute(GameAttributes.BALL_WIDTH);
        ballHeight    = pongGame.getAttribute(GameAttributes.BALL_HEIGHT);
        paddleWidth   = pongGame.getAttribute(GameAttributes.PADDLE_WIDTH);
        paddleHeight  = pongGame.getAttribute(GameAttributes.PADDLE_HEIGHT);
        
        addKeyListener(this);
    }
    
    @Override // standard Canvas painting method
    public void paint(Graphics g) {
        
        // get the game state variables like positions and scores
        int playerOneY     = pongGame.getAttribute(GameAttributes.PLAYER_ONE_Y);
        int playerTwoY     = pongGame.getAttribute(GameAttributes.PLAYER_TWO_Y);
        int x              = pongGame.getAttribute(GameAttributes.BALL_X);
        int y              = pongGame.getAttribute(GameAttributes.BALL_Y);
        int playerOneScore = pongGame.getAttribute(GameAttributes.PLAYER_ONE_SCORE);
        int playerTwoScore = pongGame.getAttribute(GameAttributes.PLAYER_TWO_SCORE);
        
        int    playerOneScoreFontSize = 25;
        int    playerTwoScoreFontSize = 25;
        String playerScoreFontName    = Font.MONOSPACED; // alternatively: Font.SANS_SERIF
        Color  playerScoreFontColor   = Color.GREEN;     // alternatively: Color.WHITE
        
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
        
        // draw player-1 score
        g.setColor(playerScoreFontColor);
        g.setFont(new Font(playerScoreFontName, Font.PLAIN, playerOneScoreFontSize));
        g.drawString(Integer.toString(playerOneScore), gameWidth/2 - (playerOneScoreFontSize + 10), playerOneScoreFontSize + 5);
        
        // draw player-2 score
        g.setColor(playerScoreFontColor);
        g.setFont(new Font(playerScoreFontName, Font.PLAIN, playerTwoScoreFontSize));
        g.drawString(Integer.toString(playerTwoScore), gameWidth/2 + 20, playerTwoScoreFontSize + 5);
        
        int    fontSize  = 10;
        String fontName  = Font.MONOSPACED;
        Color  fontColor = Color.GREEN;
        if (pongGame.isPlayerOneHuman())
        {
            g.setColor(fontColor);
            g.setFont(new Font(fontName, Font.PLAIN, fontSize));
            g.drawString("A", 5, gameHeight - fontSize*3);
            g.drawString("Z", 5, gameHeight - fontSize*2);
        }
        if (pongGame.isPlayerTwoHuman())
        {
            g.setColor(fontColor);
            g.setFont(new Font(fontName, Font.PLAIN, fontSize));
            g.drawString("UP", gameWidth - fontSize*2 - 5, gameHeight - fontSize*3);
            g.drawString("DN", gameWidth - fontSize*2 - 5, gameHeight - fontSize*2);
        }
    }
    
    /**
     * updatePaddles: updates the positions of both paddles<br>
     * 
     * @param isPlayerOneHuman True if Player-1 is Human, False for CPU controlled player
     * @param isPlayerTwoHuman True if Player-2 is Human, False for CPU controlled player
     */
    public void updatePaddles(boolean isPlayerOneHuman, boolean isPlayerTwoHuman)
    {
        if (isPlayerOneHuman)
        {
            updatePlayerOneY();
        }
        else 
        {
            boolean isCPUSimple = false;
            if (GameAttributes.SIMPLE_PLAYER_1_CPU.get() == 1)
                isCPUSimple = true;
            pongGame.playerOneAlgorithmicCPUMovePaddle(isCPUSimple);
        }
        
        if (isPlayerTwoHuman)
        {
            updatePlayerTwoY();
        }
        else
        {
            boolean isCPUSimple = false;
            if (GameAttributes.SIMPLE_PLAYER_2_CPU.get() == 1)
                isCPUSimple = true;
            pongGame.playerTwoAlgorithmicCPUMovePaddle(isCPUSimple);
        }
    }
    
    private void updatePlayerTwoY()
    {
        // pixel distance to move paddles
        int dy = pongGame.getAttribute(GameAttributes.PADDLE_DY);
        
        // move player two paddle up
        if (keyUpPressed && !keyDownPressed) pongGame.movePlayerTwoPaddle(-1*dy);
        
        // move player two paddle down
        if (!keyUpPressed && keyDownPressed) pongGame.movePlayerTwoPaddle(dy);
    }
   
    private void updatePlayerOneY()
    {
        // pixel distance to move paddles
        int dy = pongGame.getAttribute(GameAttributes.PADDLE_DY);
        
        // move player one paddle up
        if (keyAPressed && !keyZPressed) pongGame.movePlayerOnePaddle(-1*dy);
        
        // move player one paddle down
        if (!keyAPressed && keyZPressed) pongGame.movePlayerOnePaddle(dy);
    }
    
    @Override // standard KeyListener method for KeyEvents
    public void keyPressed(KeyEvent event) {
        int key = event.getKeyCode();
        if (key == KeyEvent.VK_DOWN) keyDownPressed = true;
        if (key == KeyEvent.VK_UP)   keyUpPressed   = true;
        if (key == KeyEvent.VK_A)    keyAPressed    = true;
        if (key == KeyEvent.VK_Z)    keyZPressed    = true;
    }

    @Override // standard KeyListener method for KeyEvents
    public void keyReleased(KeyEvent event) {
        int key = event.getKeyCode();
        if (key == KeyEvent.VK_DOWN) keyDownPressed = false;
        if (key == KeyEvent.VK_UP)   keyUpPressed   = false;
        if (key == KeyEvent.VK_A)    keyAPressed    = false;
        if (key == KeyEvent.VK_Z)    keyZPressed    = false;
    }

    @Override // standard KeyListener method for KeyEvents
    public void keyTyped(KeyEvent event) {
        // do nothing: this demands that the key be pressed
    }
    
}
