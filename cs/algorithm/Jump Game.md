# Jump Game

- Link : https://neetcode.io/problems/jump-game?list=neetcode150

```java
class Solution {
    public boolean canJump(int[] nums) {
        
        Deque<Integer> queue = new ArrayDeque<>();
        
        queue.add(0);
        // check we visited or not visited
        boolean[] visited = new boolean[nums.length];
        
        while(!queue.isEmpty()){
            int element = queue.pollFirst();
            // if, element is last thing
            // return true;            
            if(element == nums.length - 1){
                return true;
            }
            // we check can move position
            // element is 1
            // element + 1 = 2
            // i <= 2 + 1
            // 2 <= 3
            // 2, 3
            for(int i = element + 1; i <= nums[element] + element; i++){
                
                if(nums.length <= i){
                    break;
                }
                // if not visited
                if(!visited[i]){
                    visited[i] = true;
                    // push first index, second index, fourth index
                    queue.addLast(i);
                }
            }
        }
        // we couldn't receive our destination... 
        return false;
    }

    // if, we can sump to last index return true ( start 0)
    // else return false

    // the simplest way is brute force
    // check all the thing that we could go place ( start 0 )

    // could go 2 position, 3 position
    // and then 3 is zero, so pass
    // and 4 position, value 1
    // and 5 position, finisih so return true
    
}

```