package top.totoro.note.search.service;

import top.totoro.note.search.bean.Links;

public interface SearchService {

    /**
     *  根据给定的关键词串进行搜索所有链接
     */
    Links searchLinks(String keys);

    /**
     * 向索引表追加索引
     */
    boolean putLinks(Links links);

}
