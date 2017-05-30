package Maze;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.*;

import javax.swing.*;

public class Maze extends JFrame {
	
	//0.Variables
	//
	public int x_length;
	public int y_length;
	public BinaryMinHeap openList;
	Cell[][] cells;
	public Random random = new Random();
	Stack<Cell> pathlist = new Stack<>();
	public JPanel grid;
	public long startTime;
	public long endTime;
	public long elapsedTime;
	private ArrayList<Cell> globalPath= new ArrayList<>();
	public int costAstar;
	public int cost;
	
	Cell current;
	Cell start;
	Cell goal;
	
	//1 Initialization
	//1.1 Constructor
	public Maze(int x, int y) {
		this.x_length = x;
		this.y_length = y;
	}
	
	
	//1.2 Initialization
	public void initial() {
		cells = new Cell[x_length][y_length];
		
		//
		for (int i = 0; i < x_length; i++) {
			for (int j = 0; j < y_length; j++) {
				cells[i][j] = new Cell(i, j);
			}
		}
		
		start = cells[0][0];
		goal = cells[x_length - 1][y_length - 1];
		current = start;
		
		cells[x_length - 1][y_length - 1].visited = true;
		openList = new BinaryMinHeap();
	}
		
	//1.3 Set all cells as unvisited after generating the maze
	public void setVisited() {
		for (int i = 0; i < x_length; i++) {
			for (int j = 0; j < y_length; j++) {
				cells[i][j].visited = false;
			}
		}
	}
		
	//1.4 Clear path of previous search
	public void clearPath() {
		openList.removeAll();
		current = start;
		for (int i = 0; i < x_length; i++) {
			for (int j = 0; j < y_length; j++) {
				cells[i][j].inPath = false;
				cells[i][j].parent = null;
				cells[i][j].visited = false;
			}
		}
	}

	
	//2 Set up environment
	//2.1 Get cell by x and y coordinates
	public Cell getCell(int x, int y) {
		try {
			return cells[x][y];
		}
		catch(ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}
		
	//2.2 Generate all the cells in maze
	public void generate() {
		for (int i = 0; i < x_length; i++) {
			for (int j = 0; j < y_length; j++) {
				if (cells[i][j].visited == false && cells[i][j].blocked == false) {
					gen_path(cells[i][j]);
				}
			}
		}
	}
		
	//2.3 Generate path by traveling through all the maze
	private void gen_path(Cell start){
		pathlist.push(start);
		while (!pathlist.isEmpty()){			
			searchNeighbor(pathlist.peek());
		}
	}
		
	//2.4 Search all the neighbors of current cell
	//if a neighbor is neither blocked nor visited, then it 
	//can be visited(add to "path list" for next move)
	//This function is used in generate path
	private void searchNeighbor(Cell current){
		current.visited = true;
		ArrayList<Cell> valid_neigh = new ArrayList<>();
			
		//Add potential neighbors here
		if(getCell(current.x,current.y - 1) != null && getCell(current.x,current.y - 1).visited == false
				&& getCell(current.x,current.y - 1).blocked == false)
		valid_neigh.add(getCell(current.x,current.y - 1));		
		if(getCell(current.x,current.y + 1) != null && getCell(current.x,current.y + 1).visited == false
				&& getCell(current.x,current.y + 1).blocked == false)
		valid_neigh.add(getCell(current.x,current.y+1));
		if(getCell(current.x - 1,current.y) != null && getCell(current.x - 1,current.y).visited == false
				&&getCell(current.x - 1,current.y).blocked == false)
		valid_neigh.add(getCell(current.x - 1,current.y));
		if(getCell(current.x + 1,current.y) != null && getCell(current.x + 1,current.y).visited == false
				&&getCell(current.x + 1,current.y).blocked == false)
		valid_neigh.add(getCell(current.x + 1,current.y));

		if(valid_neigh.size() > 0) {
			int ran = random.nextInt(valid_neigh.size());	//random number to decide which one to access
			Cell choose = valid_neigh.get(ran);
				if (random.nextInt(10) >= 3) {				//0.7 set it as unblocked
					pathlist.push(choose);					//start from "choose" point
				}
				else {
					choose.blocked = true;
				}			
			}
		else pathlist.pop();
	}
	
	
	//3 Search
	//3.1 Run forward search algorithm
	public void forwardSearch() {
		ArrayList<Cell> presumePath = new ArrayList<>();
		setVisited();
		startTime = System.nanoTime();
		current.g = 0;
		int counter = 0;
		while(current != goal){
			setGenerate();				//set all grids can be generated again
			presumePath = findPath(current, counter);
			counter++;
			if(presumePath.size() == 0){
				System.out.println("Can't reach!");
				break;
			}
			current = goAlong(presumePath, counter);
		}
		endTime = System.nanoTime();
		elapsedTime = endTime - startTime;
		System.out.println("Running time of Forward Search:" + elapsedTime + "ns");
	}
		
	//3.2 Go through a certain path
	private Cell goAlong(ArrayList<Cell> path, int counter){
		Cell current;
		for(int i = path.size() - 1; i >= 0; i--){
			if(path.get(i).blocked == true){
				path.get(i).robot_blocked = true;
				if((i + 1) <= path.size() - 1){
					current = path.get(i + 1);
					return current;					//return the last available cell
				}
				else return path.get(i);
			}
			else {
				//walk through the cell
				lookAround(path.get(i));
				path.get(i).inPath = true;
				globalPath.add(path.get(i));
				path.get(i).genTime = counter;
			}
		}
		return current = path.get(0);
	}
		
	//3.3 "open list" is the potential path
	//To get a presumed path, the process of "repeated"
	public ArrayList<Cell> findPath(Cell cell, int counter) {
		BinaryMinHeap CurrentOpenList = new BinaryMinHeap();//presumed openlist
		ArrayList<Cell> presumePath = new ArrayList<>();
		//current.visited = true;
		current.h = x_length + y_length - current.x - current.y;
		current.f = current.g + current.h;
		CurrentOpenList.add(current);
		Cell temp = current;
		lookAround(temp);
		//Look around the environment and set robot_version block
		while (CurrentOpenList.size() != 0) {
			calculateNeighbours(CurrentOpenList.remove(), CurrentOpenList);
			costAstar++;
			if(CurrentOpenList.size() != 0){
				if (CurrentOpenList.peek() == cells[x_length - 1][y_length - 1]) {
					System.out.println("Repeated: Generate Presumed Path " + counter);
					presumePath = recordPath(current,CurrentOpenList.peek());
					return presumePath;
				}
			}
		}
		return presumePath;
	}
	
	//3.4 Search neighbors and see if they are "legal" to add to list
	public void calculateNeighbours(Cell cell, BinaryMinHeap openlist) {
		cell.generate = true;
		ArrayList<Cell> neighbours = new ArrayList<>();
		int x = cell.x, y = cell.y;
		if (getCell(x - 1, y) != null && getCell(x - 1, y).generate == false && getCell(x - 1, y).robot_blocked == false) {
			neighbours.add(getCell(x - 1, y));
			getCell(x - 1, y).g = cell.g + 1;
			getCell(x - 1, y).h = calculateH(getCell(x - 1, y));
		}
		if (getCell(x, y - 1) != null && getCell(x, y - 1).generate == false && getCell(x, y - 1).robot_blocked == false) {
			neighbours.add(getCell(x, y - 1));
			getCell(x, y - 1).g = cell.g + 1;
			getCell(x, y - 1).h = calculateH(getCell(x, y - 1));
		}
		if (getCell(x + 1, y) != null && getCell(x + 1, y).generate == false && getCell(x + 1, y).robot_blocked == false) {
			neighbours.add(getCell(x + 1, y));
			getCell(x + 1, y).g = cell.g + 1;
			getCell(x + 1, y).h = calculateH(getCell(x + 1, y));
		}
		if (getCell(x, y + 1) != null && getCell(x, y + 1).generate == false && getCell(x, y + 1).robot_blocked == false) {
			neighbours.add(getCell(x, y + 1));
			getCell(x, y + 1).g = cell.g + 1;
			getCell(x, y + 1).h = calculateH(getCell(x, y + 1));
		}
		if (neighbours.size() != 0) {
			for (int i = 0; i < neighbours.size(); i++) {
				
				neighbours.get(i).f = neighbours.get(i).g + neighbours.get(i).h;
				if (openlist.find(neighbours.get(i)) == -1) {
					neighbours.get(i).visited = true;
					openlist.add(neighbours.get(i));
					neighbours.get(i).parent = cell;														//first initialize, first be the parent
				}
			}
		}
	}
	
	//3.5 Record the presumed path
	private ArrayList<Cell> recordPath(Cell start, Cell temp){
		ArrayList<Cell> path = new ArrayList<>();
		int cnt = 0;
		while(temp != start){
			path.add(temp);
			temp = temp.parent;
			cnt++;
			if (cnt >= 10000) {
				System.out.println("Path");
				break;
			}
		}
		return path;
	}
		
	//3.6 Check surrounding, if all blocked or visited, this path ends
	private void lookAround(Cell temp1){
		int x = temp1.x, y = temp1.y;
		if (getCell(x - 1, y) != null && getCell(x - 1, y).blocked == true) {
			getCell(x - 1, y).robot_blocked = true;
		}
		if (getCell(x, y - 1) != null && getCell(x, y - 1).blocked == true) {
			getCell(x, y - 1).robot_blocked = true;
		}
		if (getCell(x + 1, y) != null && getCell(x + 1, y).blocked == true) {
			getCell(x + 1, y).robot_blocked = true;
		}
		if (getCell(x, y + 1) != null && getCell(x, y + 1).blocked == true) {
			getCell(x, y + 1).robot_blocked = true;
		}
	}
	
	//3.7 Run a new method implemented by us (not the required method)
	//3.
	public void forwardAdaptiveSearch() {
		setVisited();
		startTime = System.nanoTime();
		findForwardAdaptivePath(cells[0][0]);
		endTime = System.nanoTime();
		elapsedTime = endTime - startTime;
		System.out.println("Running time of Forward Repeated Search:" + elapsedTime + "ns");
	}
	
	//3.8 Find path for our new method
	//3.
	public void findForwardAdaptivePath(Cell cell) {
		cell.g = 0;
		cell.h = x_length + y_length;
		cell.f = cell.g + cell.h;
		openList.add(cell);
		
		while (openList.size() != 0) {
			Cell element = openList.remove();
			element.inPath = true;
			calculateAdaptiveNeighbours(element);
			if (openList.size() != 0) {
				if (openList.peek() == cells[x_length - 1][y_length - 1]) {
					System.out.println("Reach Target by Forward Repeated Search!");
					setPath();
					return;
				}
			}
		}
		System.out.println("Can't Reach!");
	}
	
	//3.9 Calculate neighbors, every time add new cell to
	//open list, increase original elements in list by 2
	//3.
	public void calculateAdaptiveNeighbours(Cell cell) {
		//cell.visited = true;
		ArrayList<Cell> neighbours = new ArrayList<>();
		int x = cell.x, y = cell.y;
		
		for (int i = 0; i < openList.size(); i++) {
			openList.get(i).f += 2;
		}
		
		if (getCell(x - 1, y) != null && getCell(x - 1, y).visited == false && getCell(x - 1, y).blocked == false)
		{
			neighbours.add(getCell(x - 1, y));
			getCell(x - 1, y).g = cell.g + 1;
			getCell(x - 1, y).h = calculateH(getCell(x - 1, y));
			getCell(x - 1, y).parent = cell;
		}
		if (getCell(x, y - 1) != null && getCell(x, y - 1).visited == false && getCell(x, y - 1).blocked == false)
		{
			neighbours.add(getCell(x, y - 1));
			getCell(x, y - 1).g = cell.g + 1;
			getCell(x, y - 1).h = calculateH(getCell(x, y - 1));
			getCell(x, y - 1).parent = cell;
		}
		if (getCell(x + 1, y) != null && getCell(x + 1, y).visited == false && getCell(x + 1, y).blocked == false)
		{
			neighbours.add(getCell(x + 1, y));
			getCell(x + 1, y).g = cell.g + 1;
			getCell(x + 1, y).h = calculateH(getCell(x + 1, y));
			getCell(x + 1, y).parent = cell;
		}
		if (getCell(x, y + 1) != null && getCell(x, y + 1).visited == false && getCell(x, y + 1).blocked == false)
		{
			neighbours.add(getCell(x, y + 1));
			getCell(x, y + 1).g = cell.g + 1;
			getCell(x, y + 1).h = calculateH(getCell(x, y + 1));
			getCell(x, y + 1).parent = cell;
		}
		if (neighbours.size() != 0) {
			for (int i = 0; i < neighbours.size(); i++) {
				neighbours.get(i).f = neighbours.get(i).g + neighbours.get(i).h;
				neighbours.get(i).visited = true;
				openList.add(neighbours.get(i));
			}
		}
	}

	//3.10 Run adaptive search
	//3.
	public void adaptiveSearch() {
		ArrayList<Cell> presumePath = new ArrayList<>();
		setVisited();
		startTime = System.nanoTime();
		current.g = 0;
		cost = 0;
		int counter = 0;
		while (current != goal) {
			setGenerate();
			presumePath = findAdaptivePath(current, counter);
			counter++;
			if(presumePath.size() == 0){
				System.out.println("Can't reach!");
				break;
			}
			current = goAlong(presumePath, counter);
		}
		endTime = System.nanoTime();
		elapsedTime = endTime - startTime;
		System.out.println("Running time of Adaptive Search:" + elapsedTime + "ns");
	}
	
	//3.11 Find adaptive path, here we update h value for each cell h_new
	//3.
	public ArrayList<Cell> findAdaptivePath(Cell cell, int counter) {
		BinaryMinHeap CurrentOpenList = new BinaryMinHeap();//presumed openlist
		ArrayList<Cell> presumePath = new ArrayList<>();
		//current.visited = true;
		current.h = costAstar - cost;
		current.f = current.g + current.h;
		CurrentOpenList.add(current);
		//System.out.println("Cost is:" + cost);
		Cell temp = current;
		lookAround(temp);
		//Look around the environment and set robot_version block
		while (CurrentOpenList.size() != 0) {
			calculateNeighbours(CurrentOpenList.remove(), CurrentOpenList);
			cost++;
			if (CurrentOpenList.size() != 0) {
				if (CurrentOpenList.peek() == cells[x_length - 1][y_length - 1]) {
					System.out.println("Adaptive: Generate Presumed Path " + counter);
					presumePath = recordPath(current, CurrentOpenList.peek());
					return presumePath;
				}
			}
		}
		return presumePath;
	}
	
	//3.12 This function is same with the calculate neighbors(3.4)
	//3.
	public void adaptiveNeighbours(Cell cell, BinaryMinHeap openlist) {
		cell.generate = true;
		ArrayList<Cell> neighbours = new ArrayList<>();
		int x = cell.x, y = cell.y;
		if (getCell(x - 1, y) != null && getCell(x - 1, y).generate == false && getCell(x - 1, y).robot_blocked == false) {
			neighbours.add(getCell(x - 1, y));
			getCell(x - 1, y).g = cell.g + 1;
			getCell(x - 1, y).h = calculateH(getCell(x - 1, y));
		}
		if (getCell(x, y - 1) != null && getCell(x, y - 1).generate == false && getCell(x, y - 1).robot_blocked == false) {
			neighbours.add(getCell(x, y - 1));
			getCell(x, y - 1).g = cell.g + 1;
			getCell(x, y - 1).h = calculateH(getCell(x, y - 1));
		}
		if (getCell(x + 1, y) != null && getCell(x + 1, y).generate == false && getCell(x + 1, y).robot_blocked == false) {
			neighbours.add(getCell(x + 1, y));
			getCell(x + 1, y).g = cell.g + 1;
			getCell(x + 1, y).h = calculateH(getCell(x + 1, y));
		}
		if (getCell(x, y + 1) != null && getCell(x, y + 1).generate == false && getCell(x, y + 1).robot_blocked == false) {
			neighbours.add(getCell(x, y + 1));
			getCell(x, y + 1).g = cell.g + 1;
			getCell(x, y + 1).h = calculateH(getCell(x, y + 1));
		}
		if (neighbours.size() != 0) {
			for (int i = 0; i < neighbours.size(); i++) {
				neighbours.get(i).f = neighbours.get(i).g + neighbours.get(i).h;
				if (openlist.find(neighbours.get(i)) == -1) {
					openlist.add(neighbours.get(i));
					neighbours.get(i).parent = cell;
				}
			}
		}
	}
	
	//3.13 Run backward search
	public void backwardSearch() {
		setVisited();
		startTime = System.nanoTime();
		findPathBackward(cells[x_length - 1][y_length - 1]);
		endTime = System.nanoTime();
		elapsedTime = endTime - startTime;
		System.out.println("Running time of Backward Search:" + elapsedTime + "ns");
	}
	
	//3.13 Find backward search, the only difference is starting and ending point
	//3.
	public void findPathBackward(Cell cell) {
		cell.visited = true;
		cell.g = 0;
		cell.h = (x_length - cell.x) + (y_length - cell.y);
		cell.f = cell.g + cell.h;
		openList.add(cell);
		
		try {
			while (openList.size() != 0) {
				calculateNeighbours(openList.remove());
				if (openList.size() != 0) {
					if (openList.peek() == cells[0][0]) {
						System.out.println("Reach Target by Backward Search!");
						setBackwardPath();
						return;
					}
				}
			}
		}
		catch (OutOfMemoryError e) {
			System.out.println("Can't Reach!");
		}
	}
	
	//3.14 Other backward search, result turns out to be bad either
	//3.
	public void backwardRepeatedSearch() {
		setVisited();
		startTime = System.nanoTime();
		findBackwardRepeatedPath(cells[x_length - 1][y_length - 1]);
		endTime = System.nanoTime();
		elapsedTime = endTime - startTime;
		System.out.println("Running time of Backward Repeated Search:" + elapsedTime + "ns");
	}
	
	//3.15 Find path for backward search, similar except staring and ending point
	//3.
	public void findBackwardRepeatedPath(Cell cell) {
		cell.g = 0;
		cell.h = (x_length - cell.x) + (y_length - cell.y);
		cell.f = cell.g + cell.h;
		openList.add(cell);
		
		while (openList.size() != 0) {
			Cell element = openList.remove();
			element.inPath = true;
			calculateNeighbours(element);
			if (openList.size() != 0) {
				if (openList.peek() == cells[0][0]) {
					System.out.println("Reach Target by Backward Repeated Search!");
					setBackwardPath();
					return;
				}
			}
		}		
		System.out.println("Can't Reach!");
	}
	
	//3.16 Explore all neighbors, return a list that contains
	//all the legal(not null) neighbours.
	public ArrayList<Cell> explore(Cell currCell) {
		ArrayList<Cell> neighbours = new ArrayList<>();
		int x = currCell.x, y = currCell.y;
		
		neighbours.add(getCell(x - 1, y));
		neighbours.add(getCell(x, y - 1));
		neighbours.add(getCell(x + 1, y));
		neighbours.add(getCell(x, y + 1));
		
		for (int i = 0; i < neighbours.size(); i++) {
			if (neighbours.get(i) == null) neighbours.remove(i);
		}
		
		return neighbours;
	}
	
	//3.17 The oldest version of seeing neighbors and add legal ones into open list
	//3.
	public void calculateNeighbours(Cell cell) {
		cell.visited = true;
		ArrayList<Cell> neighbours = new ArrayList<>();
		int x = cell.x, y = cell.y;
		if (getCell(x - 1, y) != null && getCell(x - 1, y).visited == false && getCell(x - 1, y).blocked == false)
		{
			neighbours.add(getCell(x - 1, y));
			getCell(x - 1, y).g = cell.g + 1;
			getCell(x - 1, y).h = calculateH(getCell(x - 1, y));
			getCell(x - 1, y).parent = cell;
		}
		if (getCell(x, y - 1) != null && getCell(x, y - 1).visited == false && getCell(x, y - 1).blocked == false)
		{
			neighbours.add(getCell(x, y - 1));
			getCell(x, y - 1).g = cell.g + 1;
			getCell(x, y - 1).h = calculateH(getCell(x, y - 1));
			getCell(x, y - 1).parent = cell;
		}
		if (getCell(x + 1, y) != null && getCell(x + 1, y).visited == false && getCell(x + 1, y).blocked == false)
		{
			neighbours.add(getCell(x + 1, y));
			getCell(x + 1, y).g = cell.g + 1;
			getCell(x + 1, y).h = calculateH(getCell(x + 1, y));
			getCell(x + 1, y).parent = cell;
		}
		if (getCell(x, y + 1) != null && getCell(x, y + 1).visited == false && getCell(x, y + 1).blocked == false)
		{
			neighbours.add(getCell(x, y + 1));
			getCell(x, y + 1).g = cell.g + 1;
			getCell(x, y + 1).h = calculateH(getCell(x, y + 1));
			getCell(x, y + 1).parent = cell;
		}
		if (neighbours.size() != 0) {
			for (int i = 0; i < neighbours.size(); i++) {
				neighbours.get(i).f = neighbours.get(i).g + neighbours.get(i).h;
				openList.add(neighbours.get(i));
			}
		}
	}
	
	//3.18 Mark all cells in path as visited for drawing
	private void setPath() {
		Cell pointer = cells[x_length - 1][y_length - 1];
		while(pointer != cells[0][0]) {
			if (pointer == null) return;
			pointer.inPath = true;
			pointer = pointer.parent;
		}
	}
	
	//3.19 Same as 3.18, but in a reverse direction
	private void setBackwardPath() {
		Cell pointer = cells[0][0];
		while (pointer != cells[x_length - 1][y_length - 1]) {
			if (pointer == null) return;
			pointer.inPath = true;
			pointer = pointer.parent;
		}
	}
	
	//3.20 Set presumed path blocked as unblocked
	//since we don't know what's ahead of us
	public void setGenerate() {
		for (int i = 0; i < x_length; i++) {
			for (int j = 0; j < y_length; j++) {
				cells[i][j].generate = false;
			}
		}
	}
	
	//3.21 Calculate Manhattan distance
	public int calculateH(Cell cell) {
		return (x_length - cell.x) + (y_length - cell.y);
	}

	//3.22 Calculate expanded cell numbers
	public int printVisited() {
		int count = 0;
		for (int i = 0; i < x_length; i++) {
			for (int j = 0; j < y_length; j++) {
				if (cells[i][j].visited) count++;
			}
		}
		return count;
	}
	
	
	//4 Draw
	//4.1 Draw the maze only
	public void drawMaze() {
		//final Maze that = this;
		if (grid != null) grid.removeAll();
		else grid = new JPanel();
		
		getContentPane().add(grid, BorderLayout.CENTER);
		grid.setLayout(new GridLayout(x_length, y_length));
		for (int i = 0; i < x_length; i++) {
			for (int j = 0; j < y_length; j++) {
				JPanel draw = new JPanel();
				draw.setBorder(BorderFactory.createLineBorder(Color.black));
				Cell current = cells[i][j];
				if (current.blocked) draw.setBackground(Color.black);
				else draw.setBackground(Color.white);
				if (i == 0 && j == 0) draw.setBackground(Color.green);
				if (i == x_length - 1 && j == y_length - 1) draw.setBackground(Color.red);
				grid.add(draw);
			}
		}
		this.setContentPane(getContentPane());
		this.setVisible(true);
		this.setSize(800, 800);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	//4.2 Draw the path
	public void drawPath() {
		//final Maze that = this;
		if (grid != null) grid.removeAll();
		else grid = new JPanel();
		
		getContentPane().add(grid, BorderLayout.CENTER);
		grid.setLayout(new GridLayout(x_length, y_length));
		for (int i = 0; i < x_length; i++) {
			for (int j = 0; j < y_length; j++) {
				JPanel draw = new JPanel();
				draw.setBorder(BorderFactory.createLineBorder(Color.black));
				Cell current = cells[i][j];
				if (current.blocked) draw.setBackground(Color.black);
				else if (current.visited) draw.setBackground(Color.gray);
				else draw.setBackground(Color.white);
				if (current.inPath) draw.setBackground(Color.yellow);
				if (i == 0 && j == 0) draw.setBackground(Color.green);
				if (i == x_length - 1 && j == y_length - 1) draw.setBackground(Color.red);
				grid.add(draw);
			}
		}
		this.setContentPane(getContentPane());
		this.setVisible(true);
		this.setSize(800, 800);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
}
