/*
 * Pong Game - made easy to port to Python to use Keras and SciKitLearn
 */
package mlwithpong;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import static mlwithpong.GameAttributes.*;

/**
 * PongGame: mechanics and functioning of a pong game<br>
 * - intent to port this code to a Python (Jupyter,Keras,SKLearn) notebook,<br>
 *   train a model (with Deep Reinforcement Learning), extract the<br>
 *   parameters of that ML model, and implement the ML model back in this PongGame<br>
 * - supported by GameAttributes, which conveniently enables access to Game Variables<br>
 *   without requiring extra code that would not be relevant for porting to Python<br>
 * @author devang
 */
public class PongGame {
    int tempCount = 0;
    Canvas pCanvas;
    Frame frame;
    
    private boolean gameOver = false;
        
    private final int width         = getParameter(GAME_WIDTH);  // width of game screen
    private final int height        = getParameter(GAME_HEIGHT); // height of game screen
    private int numHistoryFrames    = 4;    // number of most recent frames to
                                            // use per iteration of training
    private int[][] gamePixels      = new int[numHistoryFrames][width*height];
                                            // most recent 4 frames for input
                                            // as training set to Neural Network
                                            // - exact pixel values from a
                                            //   monochrome game
                                            // - this array is size width*height
                                            //   as in raster order
    private int[][] gamePositions   = new int[numHistoryFrames][4];
                                            // most recent 4 game updates
                                            // as training set to Neural Network
                                            // - position data instead of pixel
                                            //   values. The ball x, y and the
                                            //   y values of each paddle
                                            // - this array is size 4
                                            // - compare accuracy and training time
                                            //   with pixel data of gameFrames
    private final int dividerWidth  = getParameter(DIVIDER_WIDTH); // width of center dividing line (3)
    private final int ballWidth     = getParameter(BALL_WIDTH);    // width of ball (ML:2)
    private final int ballHeight    = getParameter(BALL_HEIGHT);   // height of ball (ML:2)
    private final int paddleHeight  = getParameter(PADDLE_HEIGHT); // height of paddle (ML:15)
    private final int paddleWidth   = getParameter(PADDLE_WIDTH);  // width of paddle (ML:2)
    
    private final int winningScore  = getParameter(WINNING_SCORE); // maximum score: first to this score wins
    private int playerOneScore  = 0;        // keeps track of player 1 score
    private int playerTwoScore  = 0;        // keeps track of player 2 score
    private int playerOneY      = height/2; // player one paddle center position
    private int playerTwoY      = height/2; // player two paddle center position
    
    private final int dyPaddle = getParameter(PADDLE_DY); // incremental pixel distance to move paddle
    private final int maxSpinInc = getParameter(MAX_SPIN_INC); // incremental pixel distance to apply spin from a paddle (10)
    
    private int x = width/2;                        // initial ball x position
    private int y = height/2;                       // initial ball y position
    private int dx = getParameter(INITIAL_DX);      // initial ball x speed
    private int dy = getParameter(INITIAL_DY);      // initial ball y speed
                                                    // (initializes to a random value in startNewGame())
    
    private final int mindx = getParameter(MIN_DX); // might need to adjust these (4)
    private final int maxdx = getParameter(MAX_DX); // and keep track of direction of ball (10)
    private final int mindy = getParameter(MIN_DY); // where negative values mean the ball (4)
    private final int maxdy = getParameter(MAX_DY); // is moving left or down (10)

    private int numVolleys = 0;
    
    private int numVolleysToStart = getParameter(NUM_VOLLEYS_TO_START); // start game after this many volleys
                                                         // either 3 -or- 0 works well
                                                         // this ensures the paddles are ready
    
    private boolean playerOneHuman = false;
    private boolean playerTwoHuman = false;
    
    /**
     * PongGame: Default Constructor<br>
     * - sets CPU -vs- human controlled paddles
     */
    public PongGame(boolean playerOneHuman, boolean playerTwoHuman)
    {
        this.playerOneHuman = playerOneHuman;
        this.playerTwoHuman = playerTwoHuman;
    }
    
    /**
     * PongGame: Default Constructor<br>
     * - does nothing
     */
    public PongGame()
    {
        
    }
    
    /**
     * startNewGame<br>
     * - initializes variables, sizes, dimensions and velocities for a new game
     */
    public void startNewGame()
    {
        // initialize game state variables
        gameOver = false;
        x = width/2;
        y = height/2;
        numVolleys = 0;
        playerOneY = height/2;
        playerTwoY = height/2;
        
        // initialize training set of most recent frames to all zeroes
        // - position values:
        for (int f = 0; f < numHistoryFrames; f++)
        {
            for (int p = 0; p < 4; p++)
            {
                gamePositions[f][p] = 0;
            }
        }
        
        // initialize training set of most recent frames to all zeroes
        // - pixel values:
        for (int f = 0; f < numHistoryFrames; f++)
        {
            for (int p = 0; p < width*height; p++)
            {
                gamePixels[f][p] = 0;
            }
        }
        
        // initialize speed of ball at serving
        dx = mindx + (int)((maxdx-mindx)/2);
        dy = (int)(Math.random()*(maxdy-mindy) + mindy);
        
        // initialize direction of ball at serving
        if ((Math.random()-0.5)<=0.0) dx = -1*Math.abs(dx);
        else dx = Math.abs(dx);
        
        if ((Math.random()-0.5)<=0.0) dy = -1*Math.abs(dy);
        else dy = Math.abs(dy);
    }
    
    /**
     * startSimulatedMatch<br>
     * - Main Match/Games Loop for a match, and ends when winning score is reached
     * - Both players are simulated by CPU with a simple algorithm
     */
    public void startSimulatedMatch() throws InterruptedException
    {
        playerOneScore = 0;
        playerTwoScore = 0;
        // int numGames = 0; startNewGame();
        // initDrawingFrame();
        while (!isMatchOver())
        {
            startNewGame();
            while (!isGameOver())
            {
                if (numVolleys >= numVolleysToStart)
                {
                    tempCount++;
                    updateGamePixelFrames();
                    updateGamePositionFrames();
                }
                updateBall();
                 // change [1] to [5] to increase refresh rate of paddles
                 // out-of-proportion with refresh rate of screen-drawings
                for (int m = 0; m < 1 /* 5 */; m++)
                {
                    boolean isPlayerOneSimple = false;
                    if (GameAttributes.SIMPLE_PLAYER_1_CPU.get() == 0) isPlayerOneSimple = true;
                    playerOneAlgorithmicCPUMovePaddle(isPlayerOneSimple);
                    
                    boolean isPlayerTwoSimple = false;
                    if (GameAttributes.SIMPLE_PLAYER_2_CPU.get() == 0) isPlayerTwoSimple = true;
                    playerTwoAlgorithmicCPUMovePaddle(isPlayerTwoSimple);
                }
            }
            System.out.println("Game Over: ");
            System.out.println("  number of volleys in game: " + tempCount);
            System.out.println("  " + playerOneScore + " - " + playerTwoScore);
        }
        if (playerOneScore >= winningScore) System.out.println("Player 1 wins match");
        if (playerTwoScore >= winningScore) System.out.println("Player 2 wins match");
    }
    /**
     * updateGamePixelFrames: updates the most recent pixel values per game frame<br>
     * - These frames are the x-training input into an ML-model<br>
     * - This method puts the most recent frame's values at the top, and
     *   knocks off the oldest frame
     * 
     */
    public void updateGamePixelFrames()
    {
        int numFrames = numHistoryFrames;
        for (int p = numFrames-1; p > 0; p--)
        {
            gamePixels[p] = gamePixels[p-1];
        }
        gamePixels[0] = getPixelArray();
    }
    
    /**
     * updateGamePositionFrames: updates the most recent position values per game frame<br>
     * - This method puts the most recent frame's values at the top, and
     *   knocks off the oldest frame<br>
     * - Training starts with 4 frames (as in the private-int: numHistoryFrames)<br>
     * - the position values are the x,y values of the ball and y-values of the paddles<br>
     * - when most people train an ML model for a pong game, the exact pixel
     *   values of the game are used, after a Convolution filter, to train an
     *   ML model / Neural Network.<br>
     * - Here, I use the pixel values (and not images) to make the training,
     *   quicker. And then I compare that model with a model that
     *   does not use the pixel values, but rather the only properties of the
     *   the game that change: the ball position (x,y) and the paddle positions(y)<br>
     * - I compare the accuracy and training-time of a model using pixel values
     *   and a model using these 4 position values.
     */
    public void updateGamePositionFrames()
    {
        int numFrames = numHistoryFrames;
        for (int p = numFrames-1; p > 0; p--)
        {
            gamePositions[p] = gamePositions[p-1];
        }
        gamePositions[0] = getPositionArray();
    }
    
    /**
     * getGamePositionFrames: returns the most recent positions of frame history<br>
     * 
     * @return int[][] 2-D array of most recent position values
     */
    public int[][] getGamePositionFrames()
    {
        return gamePositions;
    }
    
    /**
     * getGamePixelFrames: returns the most recent pixels of frame history<br>
     * 
     * @return int[][] 2-D array of most recent pixels of frames, in raster order
     */
    public int[][] getGamePixelFrames()
    {
        return gamePixels;
    }
    
    private int[] getPositionArray()
    {   // returns the position parameters of the most recent frame
        // - only the 4 changable parameters:
        //   {x,y,paddle1-y,paddle2-y}
        int[] positionArray = new int[] {x,y,playerOneY,playerTwoY};
        return positionArray;
    }
    
    private int[] getPixelArray()
    {   // returns thes rasterized pixel values of the most recent frame
        // - the game is monochrome, so a pixel is:
        //   1 for the ball or the paddles, and
        //   0 for everything else like the black background
        int[] pixelArray = new int[width*height];
        for (int h = 0; h < height; h++)
        {
            for (int w = 0; w < width; w++)
            {
                pixelArray[width*h + w] = 0;
            }
        }
        pixelArray[width*y + x] = 1;
        for (int p = Math.max(0, playerOneY - paddleHeight/2); p < Math.min(height, playerOneY + paddleHeight/2); p++)
        {
            pixelArray[width*p] = 1;
        }
        for (int p = Math.max(0, playerTwoY - paddleHeight/2); p < Math.min(height, playerTwoY + paddleHeight/2); p++)
        {
            pixelArray[width*p + width-1] = 1;
        }
        
        return pixelArray;
    }
    
    private int[] getMirrorArray(int[] originalArray, int pWidth, int pHeight)
    {   // returns an array of the game frame pixels, but mirrored
        // - this is used to swap the side of a paddle, so that
        //   a trained ML model can be swapped to play from the left
        //   side instead of the right-side
        int[] newArray = new int[pWidth*pHeight];
        for (int h = 0; h < pHeight; h++)
        {
            for (int w = 0; w < pWidth; w++)
            {
                newArray[pWidth*h + (pWidth-1)-w] = originalArray[pWidth*h + w];
            }
        }
        return newArray;
    }

    private void initDrawingFrame()
    {   // draws a single frame,canvas,game-screen from an array of pixels
        // - this is useful to spot-check a frame to make sue the pixel
        //   array representation of a graphics game frame is accurate
        //   and can be properly used when training an ML model
        int pWidth  = width;
        int pHeight = height;
        pCanvas = new Canvas() {
            @Override
            public void paint(Graphics g) {
                int[] pArray = getPixelArray();
                setBackground(Color.DARK_GRAY);
                g.setColor(Color.ORANGE);
                for (int h = 0; h < pHeight; h++)
                {
                    for (int w = 0; w < pWidth; w++)
                    {
                        if (pArray[pWidth*h + w] > 0)
                            g.drawLine(w+1,h,w+1,h);
                    }
                }
            }
        };
        pCanvas.setSize(pWidth, pHeight);

        // initialize a frame in which to place the canvas
        frame = new Frame("SingleFrameFromPixelArray");
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                System.exit(0);
            }
        });

        frame.setLocation(200,200);
        frame.add(pCanvas);
        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);
    }
    
    private void drawSingleFrame(int[] pArray)
    {   // draws a single frame,canvas,game-screen from an array of pixels
        // - this is useful to spot-check a frame to make sue the pixel
        //   array representation of a graphics game frame is accurate
        //   and can be properly used when training an ML model
        int pWidth = width;
        int pHeight = height;
        pCanvas = new Canvas() {
            @Override
            public void paint(Graphics g) {
                // int[] pArray = getPixelArray();
                setBackground(Color.DARK_GRAY);
                g.setColor(Color.ORANGE);
                for (int h = 0; h < pHeight; h++)
                {
                    for (int w = 0; w < pWidth; w++)
                    {
                        if (pArray[pWidth*h + w] > 0)
                            g.drawLine(w+1,h,w+1,h);
                    }
                }
            }
        };
        pCanvas.setSize(pWidth, pHeight);

        // initialize a frame in which to place the canvas
        frame = new Frame("SingleFrameFromPixelArray");
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                System.exit(0);
            }
        });

        frame.setLocation(200,200);
        frame.add(pCanvas);
        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);
    }
    
    /**
     * Player 1 CPU Algorithm: not ML-trained player<br>
     * The paddle always tries to hit the ball at the center of the paddle,<br>
     * and does not attempt to use the top-spin/bottom-spin mechanics<br>
     * if simple algorithm is set to True:<br>
     * - the paddle constantly follows the current position of the ball<br>
     * if simple algorithm is set to False (use the forecasting algorithm):<br>
     * - the paddle predicts the position of the ball when the ball should reach<br>
     *   by calculating the position based on the y-direction speed<br>
     *   and checking when the ball collides with the top or bottom<br>
     * 
     * @param simple True if paddle should follow ball, False if paddle should forecast
     */
    public void playerOneAlgorithmicCPUMovePaddle(boolean simple)
    {
        // int predicted_y = simplePredictYPosition();
        int predicted_y = 0;
        if (simple) {
            if (dx > 0) return;
            predicted_y = simplePredictYPosition();
        }
        else predicted_y = forecastPredictYPosition();
        
        // move paddle up...
        if (playerOneY >= predicted_y)
        {
            movePlayerOnePaddle(-1*dyPaddle);
        } // move paddle down...
        else if (playerOneY <= predicted_y)
        {
            movePlayerOnePaddle(dyPaddle);
        }
    }
    
    /**
     * Player 2 CPU Algorithm: not ML-trained player<br>
     * The paddle always tries to hit the ball at the center of the paddle,<br>
     * and does not attempt to use the top-spin/bottom-spin mechanics<br>
     * if simple algorithm is set to True:<br>
     * - paddle stays in place until the ball starts moving towards the paddle<br>
     * - when ball is approaching, the paddle follows the current position of the ball<br>
     * if simple algorithm is set to False (use the forecasting algorithm):<br>
     * - the paddle predicts the position of the ball when the ball should reach<br>
     *   by calculating the position based on the y-direction speed<br>
     *   and checking when the ball collides with the top or bottom<br>
     * 
     * @param simple True if paddle should follow ball, False if paddle should forecast
     */
    public void playerTwoAlgorithmicCPUMovePaddle(boolean simple)
    {
        int predicted_y = 0;
        if (simple) {
            if (dx < 0) return;
            predicted_y = simplePredictYPosition();
        }
        else predicted_y = forecastPredictYPosition();
        
        // move paddle up...
        if (playerTwoY >= predicted_y)
        {
            movePlayerTwoPaddle(-1*dyPaddle);
        } // move paddle down...
        else if (playerTwoY <= predicted_y)
        {
            movePlayerTwoPaddle(dyPaddle);
        }
    }
    
    private int simplePredictYPosition()
    {
        // constantly moves paddle to the current y-position of the ball
        int predicted_y = y;
        return predicted_y;
    }
    
    private int forecastPredictYPosition()
    {
        // forecast prediction of y-position when the ball will
        // be at the left side (Player 1 paddle side) where
        // player one is the algorithmic CPU
        
        int predicted_y = y;
        
        // ball moving right 
        if (dx > 0)
        {
            // predict where ball will be, and move there
            // x = #pixels from left-side
            // dx = speed the ball is moving
            // forecast the position of the ball when it gets to the left
            int t_x = x;
            int t_y = y;
            int t_dx = dx;
            int t_dy = dy;
            while (t_x < width)
            {
                t_x += t_dx;
                t_y += t_dy;
                
                // forecast: check for collision at bottom
                if ((t_y + ballHeight/2) >= height)
                {
                    t_y = height;
                    t_dy = -1*Math.abs(t_dy);
                }

                // forecast: check for collision at top
                if ((t_y - ballHeight/2) <= 0)
                {
                    t_y = 0;
                    t_dy = Math.abs(t_dy);
                }
            }
            
            // forecasted y position: move paddle to this position
            predicted_y = t_y;
        }
        if (dx < 0)
        {
            // predict where ball will be, and move there
            // x = #pixels from left-side
            // dx = speed the ball is moving
            // forecast the position of the ball when it gets to the left
            int t_x = x;
            int t_y = y;
            int t_dx = dx;
            int t_dy = dy;
            while (t_x >= 0)
            {
                t_x += t_dx;
                t_y += t_dy;
                
                // forecast: check for collision at bottom
                if ((t_y + ballHeight/2) >= height)
                {
                    t_y = height;
                    t_dy = -1*Math.abs(t_dy);
                }

                // forecast: check for collision at top
                if ((t_y - ballHeight/2) <= 0)
                {
                    t_y = 0;
                    t_dy = Math.abs(t_dy);
                }
            }
            
            // forecasted y position: move paddle to this position
            predicted_y = t_y;
        }
        
        return predicted_y;
    }
    
    /**
     * updateBall: moves the ball one unit according to dx, dy<br>
     * - moves ball one screen-refresh unit (dx,dy)<br>
     * - checks for collision at top, bottom and reverses direction accordingly<br>
     * - checks for collision with paddle, and reverses direction accordingly<br>
     * - if there is a collision with a paddle, the y-direction velocity(dy)<br>
     *   is adjusted according to the region of the paddle that the ball hits<br>
     * - When the ball hits the paddle, the y-speed is increased proportional<br>
     *   to the distance from the center of the paddle if the ball hits<br>
     *   the far side of the paddle<br>
     * - the y-speed is decreased proportional to the distance from the center<br>
     *   of the paddle if the ball hits the near side of the paddle
     */
    public void updateBall()
    {
        /*
        if (tempCount == 125)
        {
            drawSingleFrame(getGamePixelFrames()[0]);
            System.out.println(getGamePositionFrames()[0][3]);
            System.out.println(getGamePositionFrames()[1][2]);
            System.out.println(getGamePositionFrames()[2][1]);
            System.out.println(getGamePositionFrames()[3][0]);
        }
        */
        
        // update position
        x += dx;
        y += dy;
        
        // check for collision at bottom
        if (y >= height - 1)
        {
            y = height-1;
            dy = -1*Math.abs(dy);
        }
        
        // check for collision at top
        if (y <= 0)
        {
            y = 0;
            dy = Math.abs(dy);
        }
        
        // check for collision with left paddle (player 1)
        if ((x <= 0) && 
            (y >= (playerOneY - paddleHeight/2)) && 
            (y <= (playerOneY + paddleHeight/2)))
        {
            // calculate an offset that adjusts spin of ball depending 
            // on region of paddle at collision
            int sy = 0;
            int offset = 0;
            
            if (dy < 0)
            {
                offset = (int)((double)(maxSpinInc*(playerOneY-y))/((double)paddleHeight/2.0));
                // sy = playerOneY - y;
                // offset = offsetSpinPaddle*sy/paddleHeight; // (10)
            }
            else if (dy > 0)
            {
                offset = (int)((double)(maxSpinInc*(y-playerOneY))/((double)paddleHeight/2.0));
                // sy = y - playerOneY;
                // offset = -1*offsetSpinPaddle*sy/paddleHeight; // (10)
            }
            
            if (dy <= 0)
            {
                dy += offset;
                if (dy<=(-1*maxdy)) dy = -1*maxdy;
                if (dy>=(-1*mindy)) dy = -1*mindy;
            } else if (dy > 0) {
                dy += offset;
                if (dy<=mindy) dy = mindy;
                if (dy>=maxdy) dy = maxdy;
            }
            
            x = 1;
            dx = -1*dx;
            numVolleys++;
        }

        // check for collision with right paddle (player 2)
        if ((x >= (width - 1)) && 
             (y >= (playerTwoY - paddleHeight/2)) &&
             (y <= (playerTwoY + paddleHeight/2)))
        {            
            // calculate an offset that adjusts spin of ball depending 
            // on the location of the paddle that the ball hits
            int sy = 0;
            int offset = 0;

            if (dy < 0)
            {
                offset = (int)((double)(maxSpinInc*(playerTwoY-y))/((double)paddleHeight/2.0));
                // sy = playerTwoY - y;
                // offset = offsetSpinPaddle*sy/paddleHeight; // (10)
            }
            else if (dy > 0)
            {
                offset = (int)((double)(maxSpinInc*(y-playerTwoY))/((double)paddleHeight/2.0));
                // sy = y - playerTwoY;
                // offset = -1*offsetSpinPaddle*sy/paddleHeight; // (10)
            }
            
            if (dy <= 0)
            {
                dy += offset;
                if (dy<=(-1*maxdy)) dy = -1*maxdy;
                if (dy>=(-1*mindy)) dy = -1*mindy;
            } else if (dy > 0) {
                dy += offset;
                if (dy<=mindy) dy = mindy;
                if (dy>=maxdy) dy = maxdy;
            }

            x = width - 1;
            dx = -1*dx;
            numVolleys++;
        }
    }
    
    /**
     * isGameOver: checks if a player has scored<br>
     * - checks if ball has gone passed a paddle (to the left or right)<br>
     * - also checks if at least 3 volleys has gone by<br>
     *   I required 3 volleys before a game starts
     * 
     * @return boolean True if a player has score, False if ball is still in play
     */
    public boolean isGameOver()
    {        
        // check for at least 3 volleys:
        if (numVolleys < numVolleysToStart)
        {
            if ((x > width) || (x < 0))
            {
                startNewGame();
                return false;
            }
        }
        // did player one score?
        if (x > width)
        { 
            gameOver = true;
            playerOneScore++;
            System.out.println("player one wins game");
        }
        // did player two score?
        if (x < 0)
        {
            gameOver = true;
            playerTwoScore++;
            System.out.println("player two wins game");
        }
        return gameOver;
    }
    
    /**
     * isMatchOver: checks if match is over<br>
     * - match is over if either player has reached the winning score
     * 
     * @return boolean True if match is over, False if match is not over
     */
    public boolean isMatchOver()
    {
        return (playerOneScore >= winningScore || playerTwoScore >= winningScore);
    }
    
    /**
     * movePlayerOnePaddle: moves paddle up or down<br>
     * - number of pixels to move the paddle<br>
     * - negative value to move paddle up<br>
     * - positive value to move paddle down<br>
     * 
     * @param py the number of pixels to move the paddle
     */
    public void movePlayerOnePaddle(int py)
    {
        playerOneY += py;
        if (playerOneY - paddleHeight/2 <= 0) playerOneY = paddleHeight/2;
        if (playerOneY + paddleHeight/2 >= height) playerOneY = height - paddleHeight/2;
    }
    
    /**
     * movePlayerTwoPaddle<br>
     * - number of pixels to move the paddle<br>
     * - negative value to move paddle up<br>
     * - positive value to move paddle down<br>
     * 
     * @param py the number of pixels to move the paddle
     */
    public void movePlayerTwoPaddle(int py)
    {
        playerTwoY += py;
        if (playerTwoY - paddleHeight/2 <= 0) playerTwoY = paddleHeight/2;
        if (playerTwoY + paddleHeight/2 >= height) playerTwoY = height - paddleHeight/2;
    }
    
    /**
     * is the left-player human?
     * 
     * @return true if human, false if CPU AI controlled
     */
    public boolean isPlayerOneHuman()
    {
        return playerOneHuman;
    }
    
    /**
     * is the right-player human?
     * 
     * @return true if human, false if CPU AI controlled
     */
    public boolean isPlayerTwoHuman()
    {
        return playerTwoHuman;
    }
    
    /*
     * returns the user-specified modification to these values
     * as in GameAttributes.java
     */
    private int getParameter(GameAttributes attribute)
    {
        int value = attribute.get();
        switch (attribute) {
            case GAME_WIDTH:
                if (value != -999) return value;
                else return 80;
            case GAME_HEIGHT:
                if (value != -999) return value;
                else return 70;
            case DIVIDER_WIDTH:
                if (value != -999) return value;
                else return 3;
            case BALL_WIDTH:
                if (value != -999) return value;
                else return 3;
            case BALL_HEIGHT:
                if (value != -999) return value;
                else return 3;
            case PADDLE_WIDTH:
                if (value != -999) return value;
                else return 2;
            case PADDLE_HEIGHT:
                if (value != -999) return value;
                else return 39;
            case PADDLE_DY:
                if (value != -999) return value;
                else return 1;
            case MAX_SPIN_INC:
                if (value != -999) return value;
                else return 6;
            case WINNING_SCORE:
                if (value != -999) return value;
                else return 21;
            case NUM_VOLLEYS_TO_START:
                if (value != -999) return value;
                else return 3;
            case INITIAL_DX:
                if (value != -999) return value;
                else return 4;
            case INITIAL_DY:
                if (value != -999) return value;
                else return 4;
            case MIN_DX:
                if (value != -999) return value;
                else return 4;
            case MAX_DX:
                if (value != -999) return value;
                else return 10;
            case MIN_DY:
                if (value != -999) return value;
                else return 4;
            case MAX_DY:
                if (value != -999) return value;
                else return 10;
            case SIMPLE_PLAYER_1_CPU:
                if (value != -999) return value;
                else return 0;
            case SIMPLE_PLAYER_2_CPU:
                if (value != -999) return value;
                else return 0;
            default:
                return 0;
        }
    }
    
    /**
     * getAttribute: returns basic game states and variables<br>
     * Using this data structure of an enumerations because this is not
     * necessary when porting the simple Pong Game to Python.
     * This way, the code is more readable and does not require all the
     * traditional getters/setters to confuse coding when porting to
     * Python to run ML Algorithms.
     * 
     * @param attribute the name of the attribute whose value is being accessed
     * @return int value of requested attribute
     */
    public int getAttribute(GameAttributes attribute)
    {
        switch (attribute) {
            case GAME_WIDTH:
                return width;
            case GAME_HEIGHT:
                return height;
            case DIVIDER_WIDTH:
                return dividerWidth;
            case BALL_WIDTH:
                return ballWidth;
            case BALL_HEIGHT:
                return ballHeight;
            case PADDLE_WIDTH:
                return paddleWidth;
            case PADDLE_HEIGHT:
                return paddleHeight;
            case PADDLE_DY:
                return dyPaddle;
            case MAX_SPIN_INC:
                return maxSpinInc;
            case PLAYER_ONE_Y:
                return playerOneY;
            case PLAYER_TWO_Y:
                return playerTwoY;
            case PLAYER_ONE_SCORE:
                return playerOneScore;
            case PLAYER_TWO_SCORE:
                return playerTwoScore;
            case WINNING_SCORE:
                return winningScore;
            case BALL_X:
                return x;
            case BALL_Y:
                return y;
            default:
                return 0;
        }
    }
}
