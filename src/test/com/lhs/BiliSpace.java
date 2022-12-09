package com.lhs;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lhs.bot.QqRobotService;
import com.lhs.common.util.HttpRequestUtil;
import com.lhs.common.util.ReadFileUtil;
import com.lhs.common.util.SaveFile;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

@RunWith(SpringRunner.class)   //这两个注解是为了让测试类能拥有同等的spring boot上下文环境
@SpringBootTest
public class BiliSpace {

    @Autowired
    private QqRobotService robotService;

    @Value("${penguin.path}")
    private String penguinFilePath;

    @Test
    public void spaceTest() {
        String url = "https://api.bilibili.com/x/polymer/web-dynamic/v1/feed/space?offset=&host_mid=161775300";
        String con = HttpRequestUtil.doGet(url);
        String cakeInfo = ReadFileUtil.readFile("E:\\Idea_Project\\yituliuBackEnd\\src\\main\\resources\\bot\\cake.json");
        JSONObject cakeInfoJson = JSONObject.parseObject(cakeInfo);
        String cake_id_top = cakeInfoJson.get("id_str_top").toString();
        String cake_id = cakeInfoJson.get("id_str").toString();

        HashMap<Object, Object> cakeMap = new HashMap<>();

        JSONObject biliSpaceJson = JSONObject.parseObject(JSONObject.parseObject(con).get("data").toString());
//        System.out.println(biliSpaceJson);
        JSONArray itemArray = JSONArray.parseArray(biliSpaceJson.get("items").toString());
        SimpleDateFormat simpleDateFormat_today = new SimpleDateFormat("yyyy-MM-dd");// 设置日期格式
        String format_system = simpleDateFormat_today.format(new Date());


        for (Object o : itemArray) {
            JSONObject item = JSONObject.parseObject(o.toString());
            String spaceType = item.get("type").toString();
            String id_str = item.get("id_str").toString();

            String message = "";

            if ("DYNAMIC_TYPE_FORWARD".equals(spaceType)) {
                continue;
            }

            JSONObject modules = JSONObject.parseObject(item.get("modules").toString());
            JSONObject module_dynamic = JSONObject.parseObject(modules.get("module_dynamic").toString());
            JSONObject module_author = JSONObject.parseObject(modules.get("module_author").toString());
            Object pub_ts_str = module_author.get("pub_ts") + "000";
            long pub_ts = Long.parseLong(pub_ts_str.toString());
            String format_space = simpleDateFormat_today.format(new Date(pub_ts));
            if (modules.get("module_tag") != null) {
                cakeMap.put("id_str_top", id_str);
                if (id_str.equals(cake_id_top)) {
                    continue;
                }
            } else {
                cakeMap.putIfAbsent("id_str", id_str);
            }

            if (!format_system.equals(format_space) || id_str.equals(cake_id)) {
                break;
            }

            String spaceDesc = JSONObject.parseObject(module_dynamic.get("desc").toString()).get("text").toString();

            if ("DYNAMIC_TYPE_DRAW".equals(spaceType)) {
                JSONObject module_dynamic_major = JSONObject.parseObject(module_dynamic.get("major").toString());
                JSONObject draw = JSONObject.parseObject(module_dynamic_major.get("draw").toString());
                JSONArray draw_items = JSONArray.parseArray(draw.get("items").toString());
                message = "明日方舟更新了动态\n";
                for(Object draw_item:draw_items){
                    Object src = JSONObject.parseObject(draw_item.toString()).get("src");
                    message=message+ "[CQ:image,file=明日方舟.png,subType=0,url="+src+",cache=0]";
                }
                message = message+spaceDesc;
                robotService.sendMessage(572030857,message);
            }

            if ("DYNAMIC_TYPE_AV".equals(spaceType)) {
                JSONObject module_dynamic_major = JSONObject.parseObject(module_dynamic.get("major").toString());
                JSONObject archive = JSONObject.parseObject(module_dynamic_major.get("archive").toString());
                Object cover = archive.get("cover");
                Object jump_url = archive.get("jump_url");
                message = "明日方舟发布了视频"+jump_url+"\n"+ "[CQ:image,file=明日方舟.png,subType=0,url="+cover+",cache=0]"
                + spaceDesc;
                robotService.sendMessage(572030857,message);
            }

            String cakeMapJson = JSONObject.toJSONString(cakeMap);
            SaveFile.save("E:\\Idea_Project\\yituliuBackEnd\\src\\main\\resources\\bot\\", "cake.json", cakeMapJson);

        }
    }

}
