/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

/**
 *
 * @author EXCALIBUR
 */
public class Member {
    private  int memberID;
    private  String name;
    private LinkedList<book> currentLoans;

    public Member (int memberID, String name) {
        this.memberID = memberID;
        this.name = name;
        this.currentLoans = new LinkedList<>();

    }

    public int getMemberID() {
        return memberID;
    }

    public String getName() {
        return name;
    }

    public LinkedList<book> getCurrentLoans() {
        return currentLoans;
    }
}
