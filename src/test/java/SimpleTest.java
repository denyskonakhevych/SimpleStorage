import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by koxa on 27.07.2015.
 */
public class SimpleTest {

//    public static void main(String[] args) {
//        Gson gson = new Gson();
//        Map<String,Object> map = new HashMap<>();
//
//        List<String> list = new ArrayList<>();
//        list.add("aa");
//        list.add("ab");
//        list.add("ba");
//        list.add("bb");
//
//        Map<String,Object> map2 = new HashMap<>();
//        map2.put("int",0);
//        map2.put("int1",1);
//        map2.put("int2",2);
//
//        map.put("key", list);
//        String json = gson.toJson(map);
//        System.out.println(json);
//    }
    static Gson gson = new Gson();
    static EntityTransformer entityTransformer = new EntityTransformer();

    public static void main(String[] args) {
        Object obj = entityTransformer.transform("String");
        System.out.println(obj);
    }
}
