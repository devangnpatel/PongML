package mlwithpong;

/**
 * PongSimulatedGame: plays a game without graphics<br>
 * - this is used to simulate thousands of games at high-speed, because there
 *   is no frame-rate, so the games are played as fast as the computer can go<br>
 * - the functionality from here is exactly ported to Python for 
 *   Reinforcement Learning (with Neural Networks) ML Training
 * 
 * @author devang
 */
public class PongSimulatedGame {
    
    public PongSimulatedGame() throws InterruptedException
    {
        PongGame pongGame = new PongGame();
        pongGame.startSimulatedMatch();
    }
}
