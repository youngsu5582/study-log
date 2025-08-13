# Searcy a 2D Matrix

- Link : https://leetcode.com/problems/search-a-2d-matrix/

```java
class Solution {
    // m * n matrix
    // int target

    // row in matrix is sorted in increasing
    // every row is greater than last ineger of the previous row.

    // target exits, return true
    // else, return false

    // we must solve in log ( m * n )

    // m == matrix.length, n == matrix[i].length
    // 1 <= m,n <= 100
    // -10000 <= matrix[i][j], target <= 10000

    // 
    public boolean searchMatrix(int[][] matrix, int target) {
        // 2d array -> 1d array
        // it's sorted in increasing
        int[] matrix_converted = convert(matrix);
        int index = findBinarySearch(matrix_converted,target);

        // that's it! Finish!!
        // Arrays.binarySearch(matrix_converted,target);
        if(index >=0) {
            return true;
        } else {
            return false;
        }

        
    }

    private int findBinarySearch(int[] array,int target){
        int left = 0;
        int right = array.length -1;
        
        while(left<=right){
            int mid = (left + right) / 2;
            if(array[mid]== target){
                return mid;
                // mid is bigger than target
            } else if (array[mid] > target){
                right = mid -1;
            } else {
                left = mid + 1;
            }
        }
        // we can't find answer
        return -1;
    }

    private int[] convert(int[][] matrix){
        // m * n
        int[] temp = new int[matrix.length * matrix[0].length];
        int index = 0;
        for(int[] matrix_row : matrix){
            for(int matrix_value : matrix_row){
                temp[index++] = matrix_value;
            }
        }
        return temp;
    }
    // O ^ O
    // solution want log m * n
}
```