/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dataStructure.queue;
import dataStructure.linkedList.MyLinkedList;

public class MyQueue<T> {
    private MyLinkedList<T> list;
    
    public MyQueue(){
    list = new MyLinkedList<>();
    }
    
    public void add(T element){
    list.add(element);
    }
    public T remove(){
    if(isEmpty())throw new IllegalStateException("Queue is empty.");
    T first = list.get(0); 
    list.remove(first);
    return first;
    }
    public T peek(){
    if(isEmpty())throw new IllegalStateException("Queue is empty.");
    return list.get(0);
    }
    public boolean isEmpty(){
    return list.isEmpty();
    }
    public int size(){
    return list.size();
    }
    
}
