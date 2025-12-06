/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;
import dataStructure.queue.MyQueue;

public class Book {
    private int id;
    private String title;
    private String author;
    private boolean isAvailable;
    private int popularityCount =0;
    private MyQueue<Member> waitList;

    public Book(int id, String title, String author) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isAvailable = true;
        this.waitList = new MyQueue<>();
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public boolean isAvailable() {
        return isAvailable;
    }
    
    public int getPopularityCount(){
    return popularityCount;
    }

    public MyQueue<Member> getWaitList() {
        return waitList;
    }
    public void markBorrowed(){
    this.isAvailable=false;}
    
    public void markReturned(){
    this.isAvailable = true;}
    
    public void incrementPopularity(){
    this.popularityCount++;}
    
    
}
