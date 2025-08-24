https://neetcode.io/problems/buy-and-sell-crypto?list=neetcode150

```java
class Solution {
    // prices ( prices[i] = price on the ith day )

    // we choose a single day when buy it
    // and choose a single day when sell it

    // return maximum profit

    // we choose day when price is lowest
    // we choose day when price is biggest after choose lowest day

    // EX) 3 100 1 50
    // -> we choose 3, 100
    // EX) 1 2 3 4
    // -> we choose 1, 4
    // EX) 100 5 50 3
    // -> we choose 5, 3
    
    // the simple way
    // -> brute force

    public int maxProfit(int[] prices) {
        int max = 0;
        // first
        if(prices.length == 1){
            // it's equal value;
            return max;
        }
        
        // for(int i = 0; i < prices.length; i++){
        //     // we loop all value in after i index

        //     // 100 1
        //     // 100 50
        //     // ...
        //     for(int j = i + 1; j < prices.length; j++){
        //         max = Math.max(prices[j] - prices[i],max);
        //     }
        // }
        // we start 0,1 index ( we must choose two day )
        // i declare left, right
        // if the, next index is less than left index value
        // that's mean we change the left index. because, it's more possible the answer.
        // and, we change the answer with left, right index


        int left = 0;
        int next = 0;
        // 3 100 1 50

        // 3 100
        while(next < prices.length){
            if(prices[next] < prices[left]){
                left = next;
            }else {
                max = Math.max(prices[next] - prices[left], max);
            }
            next++;
            // 3 100
            // max = 100 - 3 = 97
            
            // 3 1
            // change left is 1
            // continue

            // 1 50
            // max = 50 -1 or 97 = 97
        }

        return max;
    }
}

```