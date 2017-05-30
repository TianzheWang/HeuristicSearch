package Maze;

public class Cell {
	//0.
	public int x;
	public int y;
	public boolean visited = false;
	public boolean blocked = false;
	public boolean robot_blocked = false;
	public boolean generate = false;
	public int g;
	public int h;
	public int f;
	public Cell parent;
	public boolean inPath = false;
	public int genTime = -1;
	
	//1.
	public Cell(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	//2.
	public int compareTo(Cell cell) {
		int result = 0;
		if (f > cell.f) result = 1;
		else if (f < cell.f) result = -1;
		else {
			if (g > cell.g) result = 1;
			else if (g < cell.g) result = -1;
			else {
				if (h > cell.h) result = 1;
				else if (h < cell.h) result = -1;
			}
		}
		return result;
	}
}
