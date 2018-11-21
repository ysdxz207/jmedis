package win.hupubao.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class Test {

    public static void main(String[] args) {
        JSONObject json = new JSONObject();
        JSONObject value = new JSONObject();
        JSONArray arr = new JSONArray();

        value.put("sex", "男");
        value.put("desc", "这家伙很懒，什么也没留下");

        for (int i = 0; i < 3; i ++) {
            JSONObject o = new JSONObject();
            o.put("key" + i, "value" + i);

            arr.add(o);
        }

        json.put("name", "zhangsan");
        json.put("info", value.toJSONString());
        json.put("arr", arr.toJSONString());

        // 实际应用中json值可能是JSONObject，也可能是JSONArray
        System.out.println(JSON.toJSONString(json));
    }
}
