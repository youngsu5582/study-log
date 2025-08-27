# Three Sum

- Link : https://neetcode.io/problems/three-integer-sum?list=neetcode150

class Solution {

    // nums : array
    // we must tripletes ( nums[i] + nums[j] + nums[k] = 0 )

    // i,j,k are all distinct.


    // output should not contain any duplicate triplets

    // how do we solve...?

    // inner list : array length 3, nums[i] + nums[j] + nums[k] = 0
    // out list : does not contain duplicate

    // return the out put the triplets in any order.
    public List<List<Integer>> threeSum(int[] nums) {

        // i just writing with brute force.

        int length = nums.length;

        // List<List<Integer>> result = new ArrayList<>();
        // // but... this is not the best
        // for(int i = 0; i < length; i++){
        //     for(int j = i + 1; j < length; j++){
        //         for(int k = j + 1; k < length; k++){
        //             if(nums[i] + nums[j] + nums[k] == 0){
        //                 // returning ordered by nums asc.
        //                 result.add(List.of(nums[i],nums[j],nums[k]).stream().sorted().toList());
        //             }
        //         }
        //     }
        // }
        // return result.stream().distinct().toList();

        // first, time complexity is o^3
        // and then, we just using stream -> that's cause for loop
        // => O^4

        // how about sort?

        // -4, -1, -1, 0, 1, 2

        // -4, -1, 2
        // = -3
        // move left index to right 
        // two pointers
        // sum is bigger? or smaller?
        // and then, move index

        // i think use two pointers
        // is better than upper code
        // left, right is must narrow.

        Arrays.sort(nums);
        Set<List<Integer>> set = new HashSet<>();
        
        // time complexity - O(n * n )
        for(int i = 0; i < length; i++){
            int num = nums[i];
            int left = 0;
            int right = length -1;
            System.out.println("INDEX: " + i);
            System.out.println("============");
            while(left < right){
                if(left == i || right == i){
                    if(left == i){
                        left++;
                        // right is equal i
                    } else{
                        right--;
                    }
                    continue;
                }
                // i + j + k = 0
                // if result is bigger than 0 -> left ++
                // -1 + - ( -1, 0 )
                var result = num + nums[left] + nums[right];

                // we must check num is equal left or right
                if(result == 0){
                    set.add(List.of(num,nums[left],nums[right]).stream().sorted().toList());
                    // 1,2,3
                    // find! and then?
                    left++;
                    // we must value to smaller;
                } else if(result > 0){
                    right--;
                } else{
                    // we must value to bigger;
                    left++;
                }
            }
        }
        return set.stream().toList();
    }
}
