import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

class testing {
    public static void main(String[] args) throws Exception {
        HashMap<String, Integer> map = new HashMap<>();
        map.put("apple", 3);        // insert
        map.put("apple", 5);
        for (String key : map.keySet()) {
            System.out.println(key + ": " + map.get(key)); // apple: 5
        }


        ArrayList<Integer> list = new ArrayList<>();
        list.add(1);              // insert
        list.add(2);
        list.add(4);
        list.remove(Integer.valueOf(2));  // remove by value — need valueOf for integers to avoid ambiguity
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
        }
        System.out.println(list.contains(1)); // true
        System.out.println(list.size()); // 2


        int[] nums = {5, 3, 1};
        Arrays.sort(nums);    
        for (int num : nums) {
            System.out.println(num); // 1, 3, 5
        }
    }

    
    
}
