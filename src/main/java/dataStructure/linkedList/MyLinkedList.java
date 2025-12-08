/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dataStructure.linkedList;

public class MyLinkedList <T>{
    private static class Node<T>{
    T data;
    Node<T>next;
    public Node(T data){
    this.data = data;
    this.next=null;}
    }
    
    
    private Node<T> head;
    private int size;
    
    public MyLinkedList(){
    head=null;
    size=0;
    }
    
    public void add(T element){
     Node<T> newNode = new Node<>(element);
     if(head==null){
     head=newNode;}
     else{
     Node<T> current=head;
     while(current.next !=null){
     current=current.next;}
     current.next=newNode;}
     size++;
    }

    public void remove(T element) {
        if (head == null) return;

        if (head.data.equals(element)) {
            head = head.next;
            size--;
            return;
        }

        Node<T> current = head;
        while (current.next != null && !current.next.data.equals(element)) {
            current = current.next;
        }

        if (current.next != null) {
            current.next = current.next.next;
            size--;
        }
    }
    public boolean contains(T element){
        Node<T> current = head;
        while(current!=null){
        if(current.data.equals(element)){
        current = current.next;
        return true;
        }
        current = current.next;
    }
        return false;
    }
   public int size() {
        return size;
    }
    public boolean isEmpty(){
return size == 0;
}
    
    public T get(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException();
        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.data;
    }
    public void clear(){
    head=null;
    size=0;
    }
    public void printList(){
    Node<T>current=head;
    while(current!=null){
    System.out.println(current.data + " -> ");
    current = current.next;
    }
     System.out.println("null");
    }
    public T getFirst(){
    if(head==null){
        throw new IllegalStateException("List is empty!");      
    }
    return head.data;
    }
    public T getLast() {
    if (head == null) {
        throw new IllegalStateException("List is empty!");
    }

    Node<T> current = head;
    while (current.next != null) {
        current = current.next;
    }
    return current.data;
}
   
}
