package grupp1.othello.controller;

/*------------------------------------------------
 * IMPORTS
 *----------------------------------------------*/

import grupp1.othello.model.DiskPlacement;

/*------------------------------------------------
 * CLASS
 *----------------------------------------------*/

/**
 * A human player controlled through GUI.
 *
 * @author Philip Arvidsson (S133686)
 */
public class GUIHumanPlayer extends Player {

/*------------------------------------------------
 * FIELDS
 *----------------------------------------------*/

/**
 * The displacement. Most likely set from the GUI thread.
 */
private DiskPlacement diskPlacement;

/**
 * Object used for thread synchronization.
 */
private final Object lock = new Object();

/*------------------------------------------------
 * PUBLIC METHODS
 *----------------------------------------------*/

/**
 * Constructor.
 *
 * @param name The player name.
 */
public GUIHumanPlayer(String name) {
    super(name);
}

/**
 * Initializes the player.
 */
@Override
public void initialize() {}

/**
 * Interrupts the player.
 */
public void interrupt() {
    synchronized (lock) {
        diskPlacement = null;
        lock.notify();
    }
}

/**
 * Asks the player to place a disk on the grid.
 *
 * @param gameManager The game manager requesting the move.
 *
 * @return The disk placement.
 */
@Override
public DiskPlacement makeNextMove(GameManager gameManager) {
    synchronized (lock) {
        try {
            lock.wait();
        }
        catch (InterruptedException e) {
            // Not much we can do here.
            return (null);
        }

        DiskPlacement result = diskPlacement;
        diskPlacement = null;

        return (result);
    }
}

/**
 * Notifies a player that the attempted move is invalid.
 *
 * @param diskPlacement The invalid disk placement.
 */
@Override
public void notifyInvalidMove(DiskPlacement diskPlacement) {}

/**
 * Sets the next move (and allows the makeNextMove() method to stop blocking
 * and instead return the move specified by the call to this method).
 *
 * @param x The x-coordinate of the move.
 * @param y The y-coordinate of the move.
 */
public void setNextMove(int x, int y) {
    synchronized (lock) {
        diskPlacement = new DiskPlacement(x, y);
        lock.notify();
    }
}

}
