/*
 * GameAttributes - list of variables that a Frame/Canvas will need
 */
package mlwithpong;

/**
 * GameAttributes<br>
 * - for convenience to access the variables of a game,<br>
 *   such as screen dimensions, ball dimensions, score, speeds, etc<br><br>
 * Without this enum, PongGame could have provided access to its state<br>
 * variables with Getters/Setters, but that would have added a lot<br>
 * of extra/unnecessary code to the PongGame class file,<br>
 * and the intention is to allow easy porting of the PongGame<br>
 * functions to Python.<br>
 * With the getters/setters, there would have been more irrelevant code<br>
 * to sift through when converting PongGame from the Java class to a<br>
 * Python notebook.
 */
public enum GameAttributes {
    GAME_WIDTH(125),        // [  java Simulation: 320 ] [ python ML Training: 80 ]
    GAME_HEIGHT(100),       // [  java Simulation: 240 ] [ python ML Training: 70 ]
    BALL_WIDTH(3),          // [  java Simulation: 3   ] [ python ML Training: 2  ]
    BALL_HEIGHT(3),         // [  java Simulation: 3   ] [ python ML Training: 2  ]
    PADDLE_WIDTH(3),        // [  java Simulation: 3   ] [ python ML Training: 2  ]
    PADDLE_HEIGHT(15),      // [  java Simulation: 39  ] [ python ML Training: 15 ]
    WINNING_SCORE(21),      // [  java Simulation: 21  ] [ python ML Training: 11 ]
    
    PADDLE_DY(1),           // [  java Simulation: 1   ] [ python ML Training: 2? ]
    MAX_SPIN_INC(4),        // [  java Simulation: 6   ] [ python ML Training: 5? ]
    MIN_DY(2),              // [  java Simulation: 6   ] [ python ML Training: 2? ]
    MAX_DY(6),              // [  java Simulation: 16  ] [ python ML Training: 8? ]
    INITIAL_DX(4),          // [  java Simulation: 4   ] [ python ML Training: 6? ]
    INITIAL_DY(4),          // [  java Simulation: 4   ] [ python ML Training: 6? ]
    
    // when a paddle player is CPU controlled, these can be 0 -or- 1
    SIMPLE_PLAYER_1_CPU(1), // 1: simple paddle that follows ball [0 makes the CPU a better player]
    SIMPLE_PLAYER_2_CPU(0), // 0: forecasting collision with ball [0 makes the CPU a better player]
    
    // these values probably do not need to be changed, but they can be modified here
    MIN_DX(2),               // [  java Simulation: 4  ] [ python ML Training: 2? ]
    MAX_DX(6),              // [  java Simulation: 16 ] [ python ML Training: 8? ]
    NUM_VOLLEYS_TO_START(0), // [  java Simulation: 0  ] [ python ML Training: 3? ]
    
    // these values do not need to change
    DIVIDER_WIDTH(2),
    PLAYER_ONE_SCORE(-999),
    PLAYER_TWO_SCORE(-999),
    PLAYER_ONE_Y(-999),
    PLAYER_TWO_Y(-999),
    BALL_X(-999),
    BALL_Y(-999);
    
    private int attributeValue;
    
    private GameAttributes(int value)
    {
        attributeValue = value;
    }
    
    public int get()
    {
        return attributeValue;
    }
}

