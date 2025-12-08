/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dataStructure.stack;
import dataStructure.linkedList.MyLinkedList;

public class MyStack <T>{
    private MyLinkedList<T> list;
    public MyStack(){
    list= new MyLinkedList<>();
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
