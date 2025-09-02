# Maximum Depth of Binary Tree

- Link : https://neetcode.io/problems/depth-of-binary-tree?list=neetcode150

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
    public int maxDepth(TreeNode root) {
        
        // we find
        // left or right max depth

        // return current depth 4
        // i return 4!

        // if, root is nul
        if(root == null){
            return 0;
        }

        return dfs(root,1);
    }
    private int dfs(TreeNode root, int height){
        // doesn't have child noe

        // if, root is null 
        if(root.left == null && root.right == null){
            return height;
        }
        // recursive doing depth search
        var left = root.left != null ? dfs(root.left, height + 1) : height;
        var right = root.right != null ? dfs(root.right, height + 1) : height;
        return Math.max(left, right);
    }
}

```