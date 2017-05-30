package Maze;

public class Start {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Maze maze = new Maze(50, 50);
		maze.initial();
		maze.generate();
		maze.forwardSearch();
		//maze.forwardAdaptiveSearch();
		//System.out.println("Expanded nodes: " + maze.printVisited());
		maze.clearPath();
		maze.adaptiveSearch();
		//System.out.println("Expanded nodes: " + maze.printVisited());
		//maze.backwardRepeatedSearch();
		//maze.drawMaze();
		maze.drawPath();
	}
}
