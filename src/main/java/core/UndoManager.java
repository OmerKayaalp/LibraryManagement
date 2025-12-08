package core;

import dataStructure.stack.MyStack;

public class UndoManager {
    private static UndoManager instance;
    private MyStack<UndoAction> stack;

    private UndoManager() {
        stack = new MyStack<>();
    }

    public static UndoManager getInstance() {
        if (instance == null) instance = new UndoManager();
        return instance;
    }

    public void push(UndoAction action) {
        stack.push(action);
    }

    public void undoLast() {
        UndoAction a = stack.pop();
        if (a != null) a.undo();
    }
}
