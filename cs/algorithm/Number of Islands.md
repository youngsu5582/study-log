# Number of Islands

- Link : https://neetcode.io/problems/count-number-of-islands?list=neetcode150

```java
class Solution {
    // 2D grid that represents 1 or 0
    // count number of islands and return

    // 1 is land
    // 0 is water

    // island is connected with horizontally or vertically that by surrounded by water


    // all land is connected, and surrounded by water
    // -> 1

    // 
    public int numIslands(char[][] grid) {
        boolean[][] visited = new boolean[grid.length][grid[0].length];

        int xLength = visited.length;
        int yLength = visited[0].length;

        int count = 0;
        for(int i = 0; i < xLength; i++){
            for(int j = 0; j < yLength; j++){
                // not visited && land
                if(!visited[i][j] && grid[i][j] == '1'){
                    // do logic
                    visited[i][j] = true;
                    bfs(i,j,grid,visited);
                    count++;
                }
            }
        }
        return count;
    }
    // left, right, up, down
    int[][] directions = {{0,-1},{0,1},{-1,0},{1,0}};

    private void bfs(int i, int j, char[][] grid, boolean[][] visited){
        Deque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[]{i,j});
        // while loop if queue is not empty
        while(!queue.isEmpty()){
            // i, j 
            int[] array = queue.removeFirst();
            for(int[] direction : directions){
                // check position
                int nextX = array[0] + direction[0];
                int nextY = array[1] + direction[1];
                // check out range
                if(nextX < 0 || nextX >= grid.length || nextY < 0 || nextY >= grid[0].length){
                    continue;
                }
                // not visited & land
                if(!visited[nextX][nextY] && grid[nextX][nextY] == '1'){
                    visited[nextX][nextY] = true;
                    queue.add(new int[]{nextX,nextY});
                }
            }
        }
    }
}

// we must find connected land for counting
// we don't matter water.

// up, down, left, right
// ask - are you land and are you not visited?

// if true, -> add next checking
// if false -> we don't matter that point.

// 1 1 0
// 1 1 0
// 0 0

// -> 1

// 0 1
// 0 1
// 0 0

// -> 1

// 2,2 index
//   0
// 0 1  0
//   0

// -> 1

// 3,3 3,4

//  0  0
//0 1 1

// -> 1
// => return 4
```