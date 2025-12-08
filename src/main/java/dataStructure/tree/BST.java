package dataStructure.tree;

public class BST<T extends Comparable<T>> {

    private TreeNode<T> root;
    private int size;

    public BST() {
        root = null;
        size = 0;
    }

    public void add(T key) {
        root = addRecursive(root, key);
    }

    private TreeNode<T> addRecursive(TreeNode<T> node, T key) {
        if (node == null) {
            size++;  
            return new TreeNode<>(key);
        }

        if (key.compareTo(node.key) < 0) {
            node.left = addRecursive(node.left, key);
        }
        else if (key.compareTo(node.key) > 0) {
            node.right = addRecursive(node.right, key);
        }

        return node; 
    }

    public boolean contains(T key) {
        return containsRecursive(root, key);
    }

    private boolean containsRecursive(TreeNode<T> node, T key) {
        if (node == null) return false;

        if (key.compareTo(node.key) == 0)
            return true;
        else if (key.compareTo(node.key) < 0)
            return containsRecursive(node.left, key);
        else
            return containsRecursive(node.right, key);
    }

    public void inorder() {
        inorderRecursive(root);
        System.out.println();
    }

    private void inorderRecursive(TreeNode<T> node) {
        if (node == null) return;
        inorderRecursive(node.left);
        System.out.print(node.key + " ");
        inorderRecursive(node.right);
    }

    public void remove(T key) {
        root = removeRecursive(root, key);
    }

    private TreeNode<T> removeRecursive(TreeNode<T> node, T key) {
        if (node == null) return null;

        if (key.compareTo(node.key) < 0) {
            node.left = removeRecursive(node.left, key);
        }
        else if (key.compareTo(node.key) > 0) {
            node.right = removeRecursive(node.right, key);
        }
        else {
            size--; 

            if (node.left == null && node.right == null) {
                return null;
            }
            else if (node.left == null) {
                return node.right;
            }
            else if (node.right == null) {
                return node.left;
            }
            else {
                T minValue = findMin(node.right);
                node.key = minValue;
                node.right = removeRecursive(node.right, minValue);
            }
        }

        return node;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public T getMin() {
        if (root == null) return null;
        TreeNode<T> current = root;
        while (current.left != null) current = current.left;
        return current.key;
    }

    public T getMax() {
        if (root == null) return null;
        TreeNode<T> current = root;
        while (current.right != null) current = current.right;
        return current.key;
    }

    private T findMin(TreeNode<T> node) {
        while (node.left != null)
            node = node.left;
        return node.key;
    }
}
