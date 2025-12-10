package core;

import core.LibrarySystem;
import core.UndoAction;
import dataStructure.stack.MyStack;

import java.util.Stack;

public class UndoManager {
    
    private static UndoManager instance = new UndoManager();
    private MyStack<UndoAction> stack = new MyStack<>();

    public UndoManager() {
        // public constructor → Main içinde new UndoManager() kullanılabilir
    }
    
    public static UndoManager getInstance() {
        return instance;
    }

    public void push(UndoAction action) {
        stack.push(action);
    }

    public void undo() {
    if (stack.isEmpty()) {
        System.out.println("No actions to undo.");
        return;
    }

    UndoAction a = stack.pop();
    a.undo();   // <-- PARAMETRE YOK
}

    public boolean hasUndo() {
        return !stack.isEmpty();
    }
}
