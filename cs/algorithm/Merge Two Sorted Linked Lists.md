# Merge Two Sorted Linked Lists

- Link : https://neetcode.io/problems/merge-two-sorted-linked-lists?list=neetcode150

```java
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

 // merge two lists into one sorted linked list
 
 // 1 2 4
 // 1 3 5
 // 1 -> 1 -> 2 -> 3 -> 4 -> 5
 // check and put value.

 // push, then next is only null
 
 // list1 are you last? ( next is null )
 // -> if not, check the value, and next to next

 // 1 -> 2 -> 4
// if you last? -> no

 // 1 -> 3 -> 
 // if you last? -> no

// we push first list, if equal value.

// so, if the push first list, first list node is next to next
// 2

// 

class Solution {
    public ListNode mergeTwoLists(ListNode list1, ListNode list2) {
        // just syntax
        // are you last? is not needed
        // because we just check listnode is null?
        ListNode result = new ListNode();
        // zero, and null
        // if, the we return answer, we just next value of result
        // two list
        // O(2n) -> O(n)
        ListNode index = result;
        while(list1 != null || list2 != null){

            //list2 != null
            if(list1 == null){
                ListNode next = list2;
                index.next = next;
                index = index.next;
                list2 = list2.next;
            }
            //list1 != null
            else if(list2 == null){
                // change later
                ListNode next = list1;
                index.next = next;
                index = index.next;
                list1 = list1.next;
            }
            // list1 != null, list2 != null
            else {
                // list1.val is put index
                if(list1.val <= list2.val){
                    ListNode next = list1;
                    index.next = next;
                    index = index.next;
                    list1 = list1.next;
                } else {
                    ListNode next = list2;
                    index.next = next;
                    index = index.next;
                    list2 = list2.next;                  
                }
            }
        }
        // finish loop
        // first(zero,null) is passed
        return result.next;
    }
}
```