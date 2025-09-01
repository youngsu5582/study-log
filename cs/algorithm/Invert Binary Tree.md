# Invert Binary Tree

- Link : https://neetcode.io/problems/invert-a-binary-tree?list=neetcode150

```java
/**
 * Definition for a binary tree node.
 * public class TreeNode {
 *     int val;
 *     TreeNode left;
 *     TreeNode right;
 *     TreeNode() {}
 *     TreeNode(int val) { this.val = val; }
 *     TreeNode(int val, TreeNode left, TreeNode right) {
 *         this.val = val;
 *         this.left = left;
 *         this.right = right;
 *     }
 * }
 */

class Solution {
    public TreeNode invertTree(TreeNode root) {
        // we invert  tree to reversed.

        // and then return its root

        // so, we change all the child node iterate.

        // we have two way.
        // 1. we made a clone that mean put reversed tree
        // 2. modify tree itself

        // root is the same
        // and, it mean
        // if, left is null and right is not null
        // -> that's mean reverted : left not null and right is null
        // is it okay?
    
        // if node is last? ( last mean left, and right is null )
        // finish inverting
        
        // or, invert left and right
        if(root == null){
            return root;
        }
        dfs(root);
        return root;
    }

    // O(logn)
    private void dfs(TreeNode node){
        // it's mean last
        if(node.left == null && node.right == null){
            return;
        }

        // we change left and right
        TreeNode temp = node.left;
        node.left = node.right;
        node.right = temp;

        // and recursive with childe node.
        if(node.left != null){
            dfs(node.left);
        }
        if(node.right != null){
            dfs(node.right);
        }
    }
}

```