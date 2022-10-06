package conwaygame;
import java.util.ArrayList;

/**
 * Conway's Game of Life Class holds various methods that will
 * progress the state of the game's board through it's many iterations/generations.
 *
 * Rules 
 * Alive cells with 0-1 neighbors die of loneliness.
 * Alive cells with >=4 neighbors die of overpopulation.
 * Alive cells with 2-3 neighbors survive.
 * Dead cells with exactly 3 neighbors become alive by reproduction.

 * @author Seth Kelley 
 * @author Maxwell Goldberg
 */
public class GameOfLife {

    // Instance variables
    private static final boolean ALIVE = true;
    private static final boolean  DEAD = false;

    private boolean[][] grid;    // The board has the current generation of cells
    private int totalAliveCells; // Total number of alive cells in the grid (board)

    /**
    * Default Constructor which creates a small 5x5 grid with five alive cells.
    * This variation does not exceed bounds and dies off after four iterations.
    */
    public GameOfLife() {
        grid = new boolean[5][5];
        totalAliveCells = 5;
        grid[1][1] = ALIVE;
        grid[1][3] = ALIVE;
        grid[2][2] = ALIVE;
        grid[3][2] = ALIVE;
        grid[3][3] = ALIVE;
    }

    /**
    * Constructor used that will take in values to create a grid with a given number
    * of alive cells
    * @param file is the input file with the initial game pattern formatted as follows:
    * An integer representing the number of grid rows, say r
    * An integer representing the number of grid columns, say c
    * Number of r lines, each containing c true or false values (true denotes an ALIVE cell)
    */
    public GameOfLife (String file) {
        StdIn.setFile(file);

        int r = StdIn.readInt();
        int c = StdIn.readInt();
        grid = new boolean[r][c];
        int counter = 0;

        for(int i = 0; i < r; i++){
            for(int j = 0; j<c; j++){
                String temp = StdIn.readString();
                boolean temp2 = false;

                if(temp.equals("true")){
                    temp2 = true;
                    counter++;
                }

                grid[i][j] = temp2;
            }
        }

        totalAliveCells = counter;
    }

    /**
     * Returns grid
     * @return boolean[][] for current grid
     */
    public boolean[][] getGrid () {
        return grid;
    }
    
    /**
     * Returns totalAliveCells
     * @return int for total number of alive cells in grid
     */
    public int getTotalAliveCells () {
        return totalAliveCells;
    }

    /**
     * Returns the status of the cell at (row,col): ALIVE or DEAD
     * @param row row position of the cell
     * @param col column position of the cell
     * @return true or false value "ALIVE" or "DEAD" (state of the cell)
     */
    public boolean getCellState (int row, int col) {
        return grid[row][col];
    }

    /**
     * Returns true if there are any alive cells in the grid
     * @return true if there is at least one cell alive, otherwise returns false
     */
    public boolean isAlive () {
        return (getTotalAliveCells() > 0);
    }

    /**
     * Determines the number of alive cells around a given cell.
     * Each cell has 8 neighbor cells which are the cells that are 
     * horizontally, vertically, or diagonally adjacent.
     * 
     * @param col column position of the cell
     * @param row row position of the cell
     * @return neighboringCells, the number of alive cells (at most 8).
     */
    public int numOfAliveNeighbors (int row, int col) {
        int counter = 0;

        int rowLength = grid.length;
        int colLength = grid[0].length;

        //Check all adjacent cells
        for(int i = -1; i<2; i++){
            for(int j = -1; j<2; j++){
                //Mod used to wrap around array
                int checkRow = (row + i) % rowLength;
                int checkCol = (col + j) % colLength;

                //EDGE: negative row -> add rowLength to wrap correctly
                if(checkRow < 0){
                    checkRow += rowLength;
                }

                //EDGE: negative col -> add rowLength to wrap correctly
                if(checkCol < 0){
                    checkCol += colLength;
                }

                //If alive, and isn't the original cell, count as an alive neighbor
                if(grid[checkRow][checkCol] && !((i == 0) && (j == 0))){
                    counter++;
                }
            }
        }
        return counter;
    }

    /**
     * Creates a new grid with the next generation of the current grid using 
     * the rules for Conway's Game of Life.
     * 
     * @return boolean[][] of new grid (this is a new 2D array)
     */
    public boolean[][] computeNewGrid () {
        boolean[][] newGrid = new boolean[grid.length][grid[0].length];

        int rowLength = grid.length;
        int colLength = grid[0].length;

        for(int i=0; i<rowLength; i++){
            for(int j=0; j<colLength; j++){
                int numAliveNeighbors = numOfAliveNeighbors(i,j);
                boolean updatedState = false;

                //Rule 1 and 4: Cells die from loneliness if only 0/1 neighbouring cells are alive. Cells die from overpopulation if 4+ neighbouring cells are alive.
                if(numAliveNeighbors == 0 || numAliveNeighbors == 1 || numAliveNeighbors >= 4){
                    updatedState = false;
                }

                //Rule 2: Dead cells with 3 neighbors become alive
                if(!grid[i][j] && numAliveNeighbors == 3){
                    updatedState = true;
                }

                //Rule 3: Cells with 2/3 neighbors are alive
                if(grid[i][j] && (numAliveNeighbors == 2 || numAliveNeighbors == 3)){
                    updatedState = true;
                }

                newGrid[i][j] = updatedState;
            }
        }
        return newGrid;
    }

    /**
     * Updates the current grid (the grid instance variable) with the grid denoting
     * the next generation of cells computed by computeNewGrid().
     * 
     * Updates totalAliveCells instance variable
     */
    public void nextGeneration () {
        grid = computeNewGrid();

        int counter = 0;

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if(grid[i][j]){
                    counter++;
                }
            }
        }

        totalAliveCells = counter;
    }

    /**
     * Updates the current grid with the grid computed after multiple (n) generations. 
     * @param n number of iterations that the grid will go through to compute a new grid
     */
    public void nextGeneration (int n) {
        while(n > 0){
            nextGeneration();
            n--;
        }
    }

    /**
     * Determines the number of separate cell communities in the grid
     * @return the number of communities in the grid, communities can be formed from edges
     */
    public int numOfCommunities() {
        //UF used to connect adjacent cells -> Community
        //uniqueParents -> store only the index of the alive, unique parents
        WeightedQuickUnionUF cellTreeConnection = new WeightedQuickUnionUF(grid.length, grid[0].length);
        ArrayList<Integer> uniqueParents = new ArrayList<>();

        int rowLength = grid.length;
        int colLength = grid[0].length;

        for(int i=0; i<rowLength; i++) {
            for (int j = 0; j < colLength; j++) {

                int row = i;
                int col = j;

                //If the cell is alive, check all neighbors
                if (grid[row][col]) {
                    for (int a = -1; a < 2; a++) {
                        for (int b = -1; b < 2; b++) {
                            int checkRow = (row + a) % rowLength;
                            int checkCol = (col + b) % colLength;

                            if (checkRow < 0) {
                                checkRow += rowLength;
                            }

                            if (checkCol < 0) {
                                checkCol += colLength;
                            }

                            //If there is an adj neighbor, call union
                            if (grid[checkRow][checkCol] && !((a == 0) && (b == 0))) {
                                cellTreeConnection.union(row, col, checkRow, checkCol);
                            }
                        }
                    }
                }
            }
        }

        for(int i=0; i<rowLength; i++) {
            for (int j = 0; j < colLength; j++) {
                //Add parent if the cell is alive and is not in the set uniqueParents
                if(grid[i][j] && !(uniqueParents.contains(cellTreeConnection.find(i, j)))){
                    System.out.println(cellTreeConnection.find(i, j));
                    uniqueParents.add(cellTreeConnection.find(i, j));
                }
            }
        }

        return uniqueParents.size();
    }
}
