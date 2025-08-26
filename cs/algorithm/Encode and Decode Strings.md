# Encode and Decode Strings

https://neetcode.io/problems/string-encode-and-decode?list=neetcode150

```java
class Solution {

    // to encode a list of strings to a single string
    // array -> single

    // encoded string back to the original when decoded

    // decide how to encode / decode

    // strs only contain UTF-8
    // single string's length is 0 < 200
    // string array length is 0 < 100
    // strs : zero, one, two
    // public String encode(List<String> strs) {
    //     // 0:
    //     if(strs.isEmpty()){
    //         return null;
    //     }
    //     System.out.println(strs);
    //     // array is emtpy
    //     // returning empty("")
    //     var format = strs.stream().collect(Collectors.joining("___"));
        
    //     return format;
    // }
    // // single string contains comma(,) -> that's not work!

    // public List<String> decode(String str) {
    //     if(str == null){
    //         return Collections.emptyList();
    //     }

    //     if(str.isEmpty()){
    //         return List.of(str);
    //     }
    //     // if str is empty("")
    //     // we just return array with empty("")

    //     // if the array is emtpy
    //     // we return empty("")
        
    //     return Arrays.stream(str.split("___")).toList();
    // }
    // is it the best way...?
    // i don't think so...

    public String encode(List<String> strs) {
        // 0:
        if(strs.isEmpty()){
            return null;
        }
        System.out.println(strs);
        for(int i = 0; i < strs.size(); i++){
            strs.set(i,"\""+ strs.get(i) + "\"");
        }
        // array is emtpy
        // returning empty("")
        // var format = strs.stream().collect(Collectors.joining("___"));
        
        return strs.toString();
    }
    // single string contains comma(,) -> that's not work!

    // ["neet","code","love","you"]
    public List<String> decode(String str) {
        if(str == null){
            return Collections.emptyList();
        }

        if(str.isEmpty()){
            return List.of(str);
        }

        var format = str.substring(1,str.length());
        
        // just let finish if meet right bracket(])
        int i = 0;
        // check double quote
        int count = 0;
        String temp = "";
        System.out.println(format);
        List<String> ary = new ArrayList<>();
        while(i < format.length()){
            char c = format.charAt(i++);
            // ignore comma
            // "Another, String With, Commas"
            // -> "Another String With Commas"
            // this thing is must be ", -> so, array string must have this pattern that split elements.
            if(c == ',' && format.charAt(i-2) == '\"'){
                continue;
            }
            // do you have double quote?
            if(c == '\"'){
                count++;
                continue;
            }
            // oh, we have double qoute!
            if(count == 2){
                ary.add(temp);
                temp = "";
                count = 0;
                continue;
            }
            temp += c;
        }
        return ary;
    }
}

```