package core;

import dataStructure.stack.MyStack;

/**
 * UndoManager - Manages undo operations using Stack data structure.
 * 
 * DATA STRUCTURE: MyStack<UndoAction>
 * Purpose: Implement undo functionality following LIFO (Last-In-First-Out) principle
 * Why Stack: Most recent operations should be undone first. Stack provides perfect LIFO behavior.
 * 
 * COMPLEXITY:
 * - Push: O(1)
 * - Pop/Undo: O(1) + complexity of the specific undo operation
 * 
 * EDGE CASE HANDLING:
 * - Empty stack: Returns false from hasUndo() and prints message in undo()
 */
public class UndoManager {
    
    /**
     * Singleton instance to ensure single undo manager across system.
     */
    private static UndoManager instance = new UndoManager();
    
    /**
     * DATA STRUCTURE: Stack for Undo Operations
     * Stores actions in LIFO order - most recent action is undone first.
     */
    private MyStack<UndoAction> stack = new MyStack<>();

    /**
     * Private constructor for singleton pattern.
     */
    private UndoManager() {
    }
    
    /**
     * Get singleton instance.
     * 
     * @return The single UndoManager instance
     */
    public static UndoManager getInstance() {
        return instance;
    }

    /**
     * Push an action onto the undo stack.
     * Time Complexity: O(1)
     * 
     * @param action The action to record for potential undo
     */
    public void push(UndoAction action) {
        stack.push(action);
    }

    /**
     * Undo the most recent action (LIFO).
     * Time Complexity: O(1) stack pop + operation-specific complexity
     * 
     * EDGE CASE: If stack is empty, prints message and returns.
     */
    public String undo() {
        if (stack.isEmpty()) {
            System.out.println("No actions to undo.");
            return null;
        }

        UndoAction a = stack.pop();
        a.undo();   // Execute the undo operation
        return a.getDescription();
    }

    /**
     * Check if there are actions available to undo.
     * Time Complexity: O(1)
     * 
     * @return true if undo stack is not empty, false otherwise
     */
    public boolean hasUndo() {
        return !stack.isEmpty();
    }
}
