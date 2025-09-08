/**
* Definition for singly-linked list.
* public class ListNode {
*     int val;
*     ListNode next;
*     ListNode() {}
*     ListNode(int val) { this.val = val; }
*     ListNode(int val, ListNode next) { this.val = val; this.next = next; }
* }
  */

class Solution {
// given linked list head
// return true if there is a cycle in the linked list

    // cycle : loop the value
    // 2 -> 3 -> 4 -> 2 -> 3 -> 4 -> 2 ...

    // index is not given our parameter!
    // then, tail node points to null and no cycle exists.
    public boolean hasCycle(ListNode head) {
        // our linked list is cycle or non-cycle

        // if this value, visit prev?(again?)

        // set collection
        // and, we used with hashset

        // hashset mean, O(1) time complexity
        Set<ListNode> visited = new HashSet<>();
        ListNode pointer = head;
        while(pointer != null){
            if(visited.contains(pointer)){
                return true;
            }
            // forward next pointer
            visited.add(pointer);
            pointer = pointer.next;
        }
        return false;
    }

    // 1 2 3 4 5
    // 2 3
    // we don't know where is the cycle...
}
