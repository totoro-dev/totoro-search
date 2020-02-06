package top.totoro.note.search.controller;

import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import top.totoro.note.search.bean.Admin;
import top.totoro.note.search.bean.Links;
import top.totoro.note.search.service.SearchService;
import top.totoro.note.search.service.impl.SearchServiceImpl;

import javax.annotation.Resource;

@Controller
@RequestMapping("/")
public class AdminController {

    @Resource
    private SearchService searchService;

    private final String adminName = "totoro";
    private final String adminPwd = "totorodawS";

    @GetMapping("admin")
    public String admin(){
        return "admin";
    }

    @PostMapping("loginUp")
    @ResponseBody
    public Admin loginUp(@RequestParam("up") String data){
        Admin admin = new Admin();
        JSONObject up = JSONObject.fromObject(data);
        String name = up.getString("name");
        String pwd = up.getString("pwd");
        if (adminName.equals(name) && adminPwd.equals(pwd)){
            admin.isAdmin = "true";
            admin.maxResult = SearchServiceImpl.maxResult;
        }
        return admin;
    }

    @PostMapping("changeMaxResult")
    @ResponseBody
    public void changeMaxResult(@RequestParam("data") String data){
        SearchServiceImpl.maxResult = Integer.valueOf(JSONObject.fromObject(data).getString("max"));
    }

    @PostMapping("submit")
    @ResponseBody
    public Links submit(@RequestParam("link") String data){
        JSONObject object = JSONObject.fromObject(data);
        String title = object.getString("title");
        String note = object.getString("note");
        String url = object.getString("url");
        Links links = new Links(new String[]{title},new String[]{note},new String[]{url});
        searchService.putLinks(links);
        return links;
    }

    @PostMapping("shutdown")
    public void shutdown(){
        System.exit(0);
    }
}
