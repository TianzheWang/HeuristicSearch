package Maze;

import java.util.ArrayList;
import java.util.NoSuchElementException;

public class BinaryMinHeap {
	
	//0 Variables
	//
    private ArrayList<Cell> a;
    
    //1 Initialization
    //1.1 Constructor
    public BinaryMinHeap() {
    	a = new ArrayList<>();
    }
    
    public void removeAll() {
    	a.removeAll(a);
	}
    
    public Cell get(int index) {
    	return a.get(index);
    }
 
    //Define functions for determining the parent, left, and right
    //node from any given node.
    private int par(int n) { 
    	return n == 0 ? -1 : (n - 1) >>> 1;
    	}
    
    private int left(int n) { 
    	return n * 2 + 1; }
    private int right(int n) { 
    	return n * 2 + 2; }
    public int size() { 
    	return a.size(); }

    //Determine the index of the lesser-value child of a node taking
    //into account that a node may not have children or may just have
    //one child.
    private int minChildIndex(int n) {
        if (left(n) > a.size() - 1) return -1;			//-1 means no child
        if (right(n) > a.size() - 1) return left(n);	//right not exist, just return left
        return a.get(left(n)).f <= a.get(right(n)).f ? left(n) : right(n);	//compare the f of 
    }

    //Add a new element to the end and bubble it up to the appropriate
    //position in the heap.
    public void add(Cell cell) {
        a.add(cell);
        bubbleUp(a.size() - 1);
    }
    
    public int find(Cell cell){
    	for(int i = 0; i < a.size(); i++){
    		if(cell == a.get(i)){
    			return i;
    		}
    	}
    	return -1;
    }

    //Remove the element at the root, move the last element up, and
    //bubble it down to the appropriate position.
    public Cell remove() {
        if (a.size() == 0) throw new NoSuchElementException();
//        if (!isHeap()) {
//            System.err.println("Heap property broken!");
//        }
        Cell result = a.get(0);
        a.set(0, a.get(a.size() - 1));
        a.remove(a.size() - 1);
        bubbleDown(0);
//        if (!isHeap()) {
//            System.err.println("Heap property broken!");
//        }
        return result;
    }

    //Move the element up until it is less than its parent or
    //until it is at the root.
    private void bubbleUp(int n) {
        int parIndex = par(n);
        while (n > 0 && a.get(parIndex).compareTo(a.get(n)) < 0) {
            swap(parIndex, n);
            n = parIndex;
            parIndex = par(n);
        }
    }

    //Move the element down, switching it with its lesser child
    //until it is lower than both of its children.
    private void bubbleDown(int n) {
        int minChildIndex = minChildIndex(n);
        while (minChildIndex != -1 && a.get(minChildIndex).compareTo(a.get(n)) < 0) {
            swap(minChildIndex, n);
            n = minChildIndex;
            minChildIndex = minChildIndex(n);
        }
    }

    //Assert the current structure is a heap.
    public boolean isHeap() {
        for (int i = 1; i < a.size(); ++i) {
            if (par(i) >= 0) {
                if (a.get(par(i)).f > a.get(i).f) {
                    return false;
                }
            }
        }
        return true;
    }

    //Utility function to swap two elements in the array list.
    private void swap(int i, int j) {
        Cell tmp = a.get(i);
        a.set(i, a.get(j));
        a.set(j, tmp);
    }
    public Cell peek(){
    	return a.get(0);
    }

    //Just print out the underlying array list.
    @Override
    public String toString() {
        return a.toString();
    }
}
