package grupp1.othello.controller;

/*------------------------------------------------
 * IMPORTS
 *----------------------------------------------*/

import grupp1.othello.exception.InvalidMoveException;
import grupp1.othello.model.DiskPlacement;
import grupp1.othello.model.GameGrid;
import grupp1.othello.model.GameResult;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.BiConsumer;

/*------------------------------------------------
 * CLASS
 *----------------------------------------------*/

/**
 * Represents the main game class. This class handles game logic etc.
 *
 * @author Philip Arvidsson (S133686)
 */
public class GameManager {

/*------------------------------------------------
 * CONSTANTS
 *----------------------------------------------*/

/**
 * Indicates an empty cell.
 */
public static final int EMPTY = 0;

/**
 * Indicates that a cell is occupied by player one.
 */
public static final int PLAYER_ONE = 1;

/**
 * Indicates that a cell is occupied by player two.
 */
public static final int PLAYER_TWO = 2;

/*------------------------------------------------
 * FIELDS
 *----------------------------------------------*/

/**
 * The current player index (1 or 2).
 */
private int currentPlayerIndex;

/**
 * Indicates whether the game is done.
 */
private boolean exitGame;

/**
 * The game grid to play on.
 */
private final GameGrid gameGrid;

/**
 * First player.
 */
private final Player player1;

/**
 * Second player.
 */
private final Player player2;

/**
 * The functions to call after a disk has been placed.
 */
private ArrayList<BiConsumer<Player, DiskPlacement>> diskPlacedCallbacks =
    new ArrayList<>();

/**
 * The functions to call after a game has ended.
 */
private ArrayList<Consumer<GameResult>> gameOverCallbacks = new ArrayList<>();

/**
 * The functions to call after an attempt has been made to do an invalid move.
 */
private ArrayList<BiConsumer<Player, DiskPlacement>> invalidMoveCallbacks =
    new ArrayList<>();

/*------------------------------------------------
 * PUBLIC METHODS
 *----------------------------------------------*/

/**
 * Constructor.
 *
 * @param gameGrid The game grid to use.
 * @param player1  Player one.
 * @param player2  Player two.
 */
public GameManager(GameGrid gameGrid, Player player1, Player player2) {
    this.gameGrid = gameGrid;
    this.player1  = player1;
    this.player2  = player2;
}

/**
 * Gets the current player.
 *
 * @return The current player.
 */
public Player getCurrentPlayer() {
    switch (currentPlayerIndex) {

    case PLAYER_ONE: return (player1);
    case PLAYER_TWO: return (player2);

    }

    // No current player (game hasn't begun).
    return (null);
}

/**
 * Gets the index of the current player.
 *
 * @return The current player index.
 */
public int getCurrentPlayerIndex() {
    return (currentPlayerIndex);
}

/**
 * Gets the score for the player with the specified index (1 or 2).
 *
 * @param player The index of the player to find the score for.
 *
 * @return The score for the specified player.
 */
public int getCurrentScore(int player) {
    assert(player == 1 || player == 2);

    int score = 0;

    for (int x = 0; x < gameGrid.getSize(); x++) {
        for (int y = 0; y < gameGrid.getSize(); y++) {
            if (gameGrid.getCellData(x, y) == player)
                score++;
        }
    }

    return (score);
}

/**
 * Gets the game grid.
 *
 * @return The game grid.
 */
public GameGrid getGameGrid() {
    return (gameGrid);
}

/**
 * Gets the first player.
 *
 * @return The first player.
 */
public Player getPlayer1() {
    return (player1);
}

/**
 * Gets the second player.
 *
 * @return The second player.
 */
public Player getPlayer2() {
    return (player2);
}

/**
 * Checks whether it is considered a valid move to place a disk for the
 * specified player on the board at the given coordinates.
 *
 * @param x      The x-coordinate of the cell to check.
 * @param y      The x-coordinate of the cell to check.
 * @param player The player (player one or player two).
 *
 * @return True if the specified move is considered valid.
 */
public boolean isValidMove(int x, int y, int player) {
    if (player != PLAYER_ONE && player != PLAYER_TWO)
        return (false);

    // Check if we're out of bounds.
    if (x < 0 || x >= gameGrid.getSize() || y < 0 || y >= gameGrid.getSize())
        return (false);

    // New disks can only be placed on empty cells.
    if (gameGrid.getCellData(x, y) != EMPTY)
        return (false);

    return (checkMove(x, y, player));
}

/**
 * Finds all valid disk placements and returns them in an array.
 *
 * @param player The player to find valid disk placements for (1 or 2).
 *
 * @return An array containing all valid moves for the specified player.
 */
public DiskPlacement[] findValidDiskPlacements(int player) {
    ArrayList<DiskPlacement> a = new ArrayList<>();

    for (int x = 0; x < gameGrid.getSize(); x++) {
        for (int y = 0; y < gameGrid.getSize(); y++) {
            if (isValidMove(x, y, player))
                a.add(new DiskPlacement(x, y));
        }
    }

    // Java type inference sucks. lol
    return (a.toArray(new DiskPlacement[0]));
}

/**
 * Initializes the game.
 */
public void initialize() {
    initGameGrid();

    // @To-do: We can probably remove these?
    player1.initialize();
    player2.initialize();

    diskPlaced(null, null);
}

/**
 * Adds a function to be called when a disk has been placed on the grid.
 *
 * @param cb The callback function.
 *
 * @return The game manager instance.
 */
public GameManager onDiskPlaced(BiConsumer<Player, DiskPlacement> cb) {
    diskPlacedCallbacks.add(cb);
    return (this);
}

/**
 * Adds a function to be called when the game has ended.
 *
 * @param cb The callback function.
 *
 * @return The game manager instance.
 */
public GameManager onGameOver(Consumer<GameResult> cb) {
    gameOverCallbacks.add(cb);
    return (this);
}

/**
 * Adds a function to be called when an attempt has been made to do an invalid
 * move.
 *
 * @param cb The callback function.
 *
 * @return The game manager instance.
 */
public GameManager onInvalidMove(BiConsumer<Player, DiskPlacement> cb) {
    invalidMoveCallbacks.add(cb);
    return (this);
}

/**
 * Plays the game and returns when the game is over.
 *
 * @return The game result.
 */
public GameResult play() {
    exitGame = false;
    while (!exitGame) {
        playOneTurn(player1, PLAYER_ONE);
        if (exitGame) break;

        playOneTurn(player2, PLAYER_TWO);
        if (exitGame) break;

        if (findValidDiskPlacements(PLAYER_ONE).length == 0
         && findValidDiskPlacements(PLAYER_TWO).length == 0)
        {
            exitGame = true;
        }
    }

    GameResult result = new GameResult();

    int player1Score = getCurrentScore(PLAYER_ONE);
    int player2Score = getCurrentScore(PLAYER_TWO);

         if (player1Score > player2Score) result.setWinner(player1);
    else if (player2Score > player1Score) result.setWinner(player2);
    else                                  result.setWinner(null);

    result.setPlayers(player1, player2);

    gameOver(result);

    return (result);
}

/**
 * Quits the game on the next turn.
 */
public void quit() {
    exitGame = true;
}

/*------------------------------------------------
 * PRIVATE METHODS
 *----------------------------------------------*/

/**
 * Calls the disk placed handler functions.
 *
 * @param player        The player that placed the disk.
 * @param diskPlacement The disk placement.
 */
private void diskPlaced(Player player, DiskPlacement diskPlacement) {
    for (BiConsumer<Player, DiskPlacement> cb : diskPlacedCallbacks)
        cb.accept(player, diskPlacement);
}

/**
 * Calls the game over handler functions.
 *
 * @param gameResult The game result.
 */
private void gameOver(GameResult gameResult) {
    for (Consumer<GameResult> cb : gameOverCallbacks)
        cb.accept(gameResult);
}

/**
 * Calls the invalid move handler functions.
 *
 * @param player        The player that attempted the move.
 * @param diskPlacement The disk placement.
 */
private void invalidMove(Player player, DiskPlacement diskPlacement) {
    player.notifyInvalidMove(diskPlacement);

    for (BiConsumer<Player, DiskPlacement> cb : invalidMoveCallbacks)
        cb.accept(player, diskPlacement);
}

/**
 * Initializes the game grid to its starting state.
 */
private void initGameGrid() {
    gameGrid.clear();

    int n = gameGrid.getSize() / 2;

    // Place the starting disks on the grid...
    gameGrid.setCellData(n-1, n-1, PLAYER_TWO);
    gameGrid.setCellData(n-1, n  , PLAYER_ONE);
    gameGrid.setCellData(n  , n-1, PLAYER_ONE);
    gameGrid.setCellData(n  , n  , PLAYER_TWO);
}

/**
 * Plays one turn.
 *
 * @param player      The player that should make a move.
 * @param playerIndex The index of the player that should make a move.
 */
private void playOneTurn(Player player, int playerIndex) {
    currentPlayerIndex = playerIndex;

    if (findValidDiskPlacements(playerIndex).length == 0)
        return;

    boolean validMoveMade = false;
    while (!validMoveMade) {
        DiskPlacement diskPlacement = player.makeNextMove(this);

        if (exitGame)
            return;

        try {
            placeDisk(diskPlacement, playerIndex);
            diskPlaced(player, diskPlacement);
            validMoveMade = true;
        }
        catch (InvalidMoveException e) {
            invalidMove(player, diskPlacement);
        }
    }

    // -
}

/**
 * Places a disk on the grid at the specified coordinates.
 *
 * @param diskPlacement The disk placement.
 * @param player        The player (player one or player two).
 *
 * @throws InvalidMoveException The specified disk placement is invalid.
 */
private void placeDisk(DiskPlacement diskPlacement, int player)
    throws InvalidMoveException
{
    if (!isValidMove(diskPlacement.getX(), diskPlacement.getY(), player))
        throw (new InvalidMoveException("Invalid move specified."));

    checkDirections(diskPlacement.getX(), diskPlacement.getY(), player, true);

    // Place final disk to complete the move.
    gameGrid.setCellData(diskPlacement.getX(), diskPlacement.getY(), player);
}

/**
 * Checks whether the specified move is valid.
 *
 * @param x      The x-coordinate of the disk placement.
 * @param y      The y-coordinate of the disk placement.
 * @param player The index of the player making the move.
 *
 * @return True if the specified move is valid.
 */
private boolean checkMove(int x, int y, int player) {
    return (checkDirections(x, y, player, false));
}

/**
 * Performs a move by placing a disk at the specified position.
 *
 * @param x      The x-coordinate of the disk placement.
 * @param y      The y-coordinate of the disk placement.
 * @param player The index of player making the move.
 */
private void makeMove(int x, int y, int player) {
    checkDirections(x, y, player, true);
}

/**
 * Checks in all (eight) directions from the specified coordinate for flippable
 * disks.
 *
 * @param x      The x-coordinate of the starting point on the grid.
 * @param y      The y-coordinate of the starting point on the grid.
 * @param player The player index. (1 or 2).
 * @param flip   True to actually flip disks (and mutate the grid state).
 *
 * @return True if any flippable disks were found.
 */
private boolean checkDirections(int x, int y, int player, boolean flip) {
    // This variable indicates whether any viable direction contains flippable
    // disks.
    boolean result = false;

    // We loop through all possible directions...
    for (int i = -1; i <= 1; i++) {
        for (int j = -1; j <= 1; j++) {
            // ...ignoring (0, 0) since it technically isn't even a direction.
            if (i == 0 && j == 0)
                continue;

            // Then we recurse in the current direction and store the result.
            result |= recurseDirection(x, y, i, j, player, flip, false);

            // If we're not flipping disks, we can return as soon as we know we
            // found flippable disks since we're only checking whether the move
            // is valid.
            if (result && !flip)
                return (result);
        }
    }

    return (result);
}

/**
 * Recurses the direction specified by (dx, dy) and checks for flippable disks
 * according to the Othello game logic.
 *
 * @param x      The x-coordinate of the starting point on the grid.
 * @param y      The y-coordinate of the starting point on the grid.
 * @param dx     The x-coordinate of the direction to recurse towards.
 * @param dy     The y-coordinate of the direction to recurse towards.
 * @param player The player index. (1 or 2).
 * @param flip   True if disks should be flipped when found flippable.
 * @param flag   MUST be set to false when called externally.
 *
 * @return True if the direction gives a valid move for the specified
 *         coordinates.
 */
private boolean recurseDirection(int x, int y, int dx, int dy, int player,
                                 boolean flip, boolean flag)
{
    // Move one step in the specified direction. This is the first thing we do
    // since we don't want to check the cell we're starting on.
    x += dx;
    y += dy;

    // Check if we're out of bounds.
    if (x < 0 || x >= gameGrid.getSize() || y < 0 || y >= gameGrid.getSize())
        return (false);

    // An empty cell invalidates this direction.
    if (gameGrid.getCellData(x, y) == EMPTY)
        return (false);

    // This is a special case: If we encounter the same color disk that we
    // started with, it can mean one of two things:
    //
    //   1. The direction is invalid because the first disk we encountered was
    //      the same color as our first.
    //
    //   2. The direction is VALID because the last disk we encountered was the
    //      same color as our first, AND we encountered at least one disk of
    //      opposing color between them.
    //
    // To solve this in an algorithmic manner, we use the flag parameter and set
    // it to true AFTER passing the first disk. This means that the first same-
    // color disk would invalidate the direction, but any same-color disks after
    // opposing color disks validates it, thus completing the evaluation of the
    // direction that was specified.
    if (gameGrid.getCellData(x, y) == player)
        return (flag);

    // We have - by necessity - passed at least one disk of opposing color when
    // reacing this point, hence we set the flag parameter to true and keep
    // recursing, now looking for a same-color disk.
    flag = true;
    boolean result = recurseDirection(x, y, dx, dy, player, flip, flag);

    // On the way back (out of the recursion nesting), we mutate the grid if
    // we've been told to flip disks.
    if (result && flip)
        gameGrid.setCellData(x, y, player);

    return (result);
}

}
