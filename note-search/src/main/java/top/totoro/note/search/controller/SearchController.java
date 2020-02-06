package top.totoro.note.search.controller;

import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import top.totoro.note.search.bean.Links;
import top.totoro.note.search.service.SearchService;

import javax.annotation.Resource;

@Controller
@CrossOrigin
public class SearchController {

    @Resource
    private SearchService searchService;

    /**
     * <code>
     function search(key) {
        var data = {
            'key':key
         }
        $.ajax({
             url:'http://117.48.227.18:8082/search',
            dataType:'json',
            type:'POST',
            data:{
            'data':JSON.stringify(data)
            },
            success:function (data) {
                console.log(data.urls[0]);
            }
        })
     }
     * </code>
     * @param data
     * @return
     */
    @PostMapping("/search")
    @ResponseBody
    public Links search(@RequestParam("data") String data){
        JSONObject object = JSONObject.fromObject(data);
        String key = object.getString("key");
        return searchService.searchLinks(key);
    }

    @GetMapping("/test")
    public String test(){
        return "search";
    }

}
