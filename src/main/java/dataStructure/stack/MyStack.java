package dataStructure.stack;

import dataStructure.linkedList.MyLinkedList;

/**
 * MyStack - Stack implementation using LinkedList (LIFO - Last In First Out).
 * 
 * PURPOSE: Implement undo functionality - most recent actions undone first.
 * Used in UndoManager for storing undo actions.
 * 
 * DATA STRUCTURE: Stack (implemented using LinkedList)
 * Why Stack: LIFO behavior matches undo semantics - undo most recent first.
 * 
 * COMPLEXITY ANALYSIS:
 * - push: O(1) - add to end of list
 * - pop: O(1) - remove from end of list
 * - peek: O(1) - access last element
 * 
 * EDGE CASE HANDLING:
 * - Empty stack pop: Returns null and prints message
 * - Empty stack peek: Returns null and prints message
 * 
 * OPERATIONS:
 * - push: Add element to top (LIFO)
 * - pop: Remove element from top (LIFO)
 */
public class MyStack <T>{
    /**
     * Underlying LinkedList used to implement stack operations.
     */
    private MyLinkedList<T> list;
    
    /**
     * Constructor: Creates empty stack.
     */
    public MyStack(){
        list = new MyLinkedList<>();
    }
    public void push(T element){
    list.add(element);
    }
    public T pop(){
    if(isEmpty()){
    System.out.println("Undo is not possible, stack is empty!");
    return null;
    }
    T last = list.get(list.size() -1);
    list.remove(last);
    return last;
    }
    public T peek(){
    if(isEmpty()){
    System.out.println("The stack is empty, there are no elements!");
    return null;
    }
    return list.get(list.size()-1);
    }
    public boolean isEmpty(){
    return list.isEmpty();
    }
    public int size(){
    return list.size();
    }
    
}
