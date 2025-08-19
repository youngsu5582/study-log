https://neetcode.io/problems/task-scheduling?list=neetcode150

class Solution {
public int leastInterval(char[] tasks, int n) {

        // we use heap

        // alphabet ( 26 ) -> constant time O(1)
        // push remain count and need index
        Map<Character,Integer> map = new HashMap<>();
        for(char task : tasks){
            var count = map.getOrDefault(task,0);
            count++;
            map.put(task,count);
        }
        PriorityQueue<Integer> pq = new PriorityQueue<>(Comparator.reverseOrder());
        Deque<Cycle> queue = new ArrayDeque<>();
        for(Integer count : map.values()){
            pq.add(count);
        }

        int index = 0;
        while(!pq.isEmpty() || !queue.isEmpty()){
            index++;
            // get out max element
            if(!pq.isEmpty()){
                var remain = pq.poll();
                remain--;
                if(remain > 0){
                    queue.addLast(new Cycle(remain,index + n));
                }
            }
            if(!queue.isEmpty() && queue.peekFirst().possibleIndex == index){
                var cycle = queue.pollFirst();
                pq.add(cycle.remain);
            }
        }
        
        return index;
    }
    private static class Cycle{
        int remain;
        int possibleIndex;
        public Cycle(int remain, int possibleIndex){
            this.remain = remain;
            this.possibleIndex = possibleIndex;
        }
    }
}
