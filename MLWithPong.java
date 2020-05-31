/*
 * ML With Pong
 * Reinforcement Learning, Dynamic Programming, Deep-Q Learning
 * Devang Patel - January 2019
 */
package mlwithpong;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * MLWithPong<br>
 * -use with PongGame, which performs all Pong Game Functions<br>
 * -use with PongCanvas, which handles screen drawing and keyboard input<br>
 * -supported by GameAttributes, which conveniently enables access to Game Variables 
 *  such as screen size and other dimensions necessary for a Pong Game<br>
 * <p>
 * PongPlayableGame: play 2-player game, 1-player game, or view CPU vs CPU game<br>
 * PongSavedGame: This class will play Python-code ML-trained games
 *                that output frames to view games as video<br>
 * 
 * @author devang
 */
public class MLWithPong {
    /**
     * Main entry point as usual...
     * 
     * @param args not used
     * @throws InterruptedException To Handle Thread sleeping until game ends
     */
    public static void main(String[] args) throws InterruptedException {

        // set to false for CPU controlled player, true for human (A, Z)
        boolean PLAYER_ONE_HUMAN = false;        
        // set to false for CPU controlled player, true for human (UP, DOWN)
        boolean PLAYER_TWO_HUMAN = false;
        
        if (false)
        {
            // USE THIS to start a regular game: Simulated or Human Players
           new PongPlayableGame(PLAYER_ONE_HUMAN,PLAYER_TWO_HUMAN);
        }
        else if (false)
        {
            // USE THIS to simulate a game with no graphics [like Python ML code/games]
            new PongSimulatedGame();            
        }
        else if (true)
        {
            // USE THIS to play video of a Saved Game made with Python ML code
            new PongSavedGame("/Users/devang/Desktop/TrainingOutput_1.txt");
            //new PongSavedGame("/Users/devang/Desktop/TrainingOutput_4.txt");
        }
    }
}
