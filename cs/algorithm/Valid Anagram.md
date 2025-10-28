# Valid Anagram

- 문제 링크 : https://leetcode.com/problems/valid-anagram/description/

가장 간단한 초식 : MAP
-> 더 효율적 : ARRAY + SORT

-> 최고 효율적 : 배열에 순회하며 count 증가후, 비교

```java
class Solution {
    public boolean isAnagram(String s, String t) {
        
        // s : just string!
        // t : an anagram of s

        // if t is anagram of s return true
        // else, return false

        // s: anagram
        // t : nagaram

        // -> same
        // return true

        // s : rat
        // t : car

        // -> doesn't same
        // return false

        // s and t consist of lowercase ( we doesn't consider upper case )

        // Map<Character, Integer> sMap = new HashMap<>();
        // for(char c : s.toCharArray()){
        //     // start with 0
        //     int count = sMap.getOrDefault(c, 0);
        //     sMap.put(c, count + 1);
        // }

        // Map<Character, Integer> tMap = new HashMap<>();
        // for(char c : t.toCharArray()){
        //     // start with 0
        //     int count = tMap.getOrDefault(c, 0);
        //     tMap.put(c, count + 1);
        // }

        // return sMap.equals(tMap);

        char[] sArray = s.toCharArray();
        char[] tArray = t.toCharArray();
        Arrays.sort(sArray);
        Arrays.sort(tArray);
        return Arrays.equals(sArray, tArray);
    }
}
```