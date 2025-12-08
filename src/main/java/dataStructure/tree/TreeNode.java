/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dataStructure.tree;

public class TreeNode <T extends Comparable<T>>{
    public T key;
    public TreeNode<T> left;
    public TreeNode<T> right;

    public TreeNode(T key) {
        this.key = key;
        this.left = null;
        this.right = null;
    }
    
    
}
