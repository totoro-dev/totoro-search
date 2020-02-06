package top.totoro.note.search.service.impl;

import net.sf.json.JSONObject;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;
import top.totoro.file.core.TFile;
import top.totoro.file.core.io.TReader;
import top.totoro.file.core.io.TWriter;
import top.totoro.file.util.Disk;
import top.totoro.note.search.analyzer.MyIKAnalyzer;
import top.totoro.note.search.bean.Link;
import top.totoro.note.search.bean.Links;
import top.totoro.note.search.service.SearchService;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SearchServiceImpl implements SearchService {

    public static int maxResult = 5;

    private final String INDEX_PATH = "note-search,index";
    private final String LINKS_PATH = "note-search,links";

    private final Map<String, Integer> linksWeight = new LinkedHashMap<>();
    private final List<String> links = new ArrayList<>();

    /**
     * 根据给定的关键词串进行搜索所有链接
     *
     * @param keys
     */
    @Override
    public Links searchLinks(String keys) {
        linksWeight.clear();
        links.clear();
        Query[] queries = createQueries(keys);
        try {
            for (Query query :
                    queries) {
                getResult(getIndexSearcher(), query);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getLinks(maxResult);
    }

    /**
     * 向索引表追加索引
     *
     * @param links
     */
    @Override
    public boolean putLinks(Links links) {
        for (int i = 0; i < links.titles.length; i++) {
            try {
                createIndex(links.titles[i], links.notes[i], links.urls[i]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }



    private Links getLinks(int limit) {
        String[] links = sort(limit);
        String titles[] = new String[links.length], notes[] = new String[links.length], urls[] = new String[links.length];
        TFile.builder().toDisk(Disk.TMP);
        for (int i = 0; i < links.length; i++) {
            TFile.builder().toPath(LINKS_PATH).toName(links[i]).toFile();
            TReader reader = new TReader(TFile.getProperty());
            String content = reader.getStringByFile();
            JSONObject object = JSONObject.fromObject(content);
            String title = object.getString("title");
            String note = object.getString("note");
            String url = object.getString("url");
            if (url.startsWith("https")){
                String tmp = url.split("https")[1];
                url = "https:"+tmp;
            }else if (url.startsWith("http")){
                String tmp = url.split("http")[1];
                url = "http:"+tmp;
            }
            titles[i] = title;
            notes[i] = note;
            urls[i] = url;
        }
        TFile.builder().recycle();
        return new Links(titles, notes, urls);
    }

    /**
     * 根据搜索关键词的分词结果创建多个搜索条件
     *
     * @param keys
     * @return
     */
    private Query[] createQueries(String keys) {
        Query[] queries = null;
        QueryParser parser = new QueryParser("", new MyIKAnalyzer(true));
        try {
            String[] allKey = parser.parse(keys).toString().split(" ");
            queries = new Query[allKey.length];
            parser = new MultiFieldQueryParser(new String[]{"title","note"}, new MyIKAnalyzer(true));
            for (int i = 0; i < allKey.length; i++) {
                queries[i] = parser.parse(allKey[i]);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (queries == null) throw new IllegalArgumentException("关键词不能为空");
        return queries;
    }

private IndexSearcher getIndexSearcher() throws Exception {
    TFile.builder().toDisk(Disk.TMP).toPath(INDEX_PATH).toFile();
    //指定索引库存放的路径
    Directory directory = FSDirectory.open(TFile.getProperty().getFile().toPath());
    //创建一个IndexReader对象
    IndexReader indexReader = DirectoryReader.open(directory);
    //创建IndexSearcher对象
    IndexSearcher indexSearcher = new IndexSearcher(indexReader);
    TFile.builder().recycle();
    return indexSearcher;
}

    /**
     * 获取一个query条件的搜索结果
     *
     * @param indexSearcher
     * @param query
     * @return
     * @throws Exception
     */
    private String[] getResult(IndexSearcher indexSearcher, Query query) throws Exception {
        if (!query.toString().startsWith("title:") && !query.toString().startsWith("note:")) throw new IllegalArgumentException("查询条件不正确");
        // 查询索引库
        TopDocs topDocs = indexSearcher.search(query, 100);
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        String links[] = new String[scoreDocs.length];
        // 遍历查询结果
        for (int i = 0; i < scoreDocs.length; i++) {
            int docId = scoreDocs[i].doc;
            // 通过id查询文档对象
            Document document = indexSearcher.doc(docId);
            String link = document.get("link");
            links[i] = link;
            if (this.links.contains(link)) {
                int origin = linksWeight.get(link);
                origin++;
                linksWeight.put(link, origin);
            } else {
                this.links.add(link);
                linksWeight.put(link, 1);
            }
        }
        // 关闭索引库
        indexSearcher.getIndexReader().close();
        return links;
    }

    /**
     * 对搜索结果进行降序排序
     *
     * @param limit 最后返回结果数量最大值
     * @return
     */
    private String[] sort(int limit) {
        if (links.size() == 0) return new String[0];
        String result[] = new String[Math.min(limit, links.size())];
        String link = links.get(0);
        int maxWeight = linksWeight.get(link);
        result[0] = link;
        for (int i = 0; i < Math.min(limit, links.size()); i++) {
            for (int j = 1; j < linksWeight.size(); j++) {
                link = links.get(j);
                int weight = linksWeight.get(link);
                if (maxWeight < weight) {
                    maxWeight = weight;
                    result[i] = link;
                }
            }
            linksWeight.put(result[i], 0);
            link = links.get(0);
            maxWeight = linksWeight.get(link);
        }
        return result;
    }

    public void createIndex(String title, String note, String url) throws Exception {
        if (url.startsWith("https:")){
            String tmp = url.split("https:")[1];
            url = "https"+tmp;
        }else if (url.startsWith("http:")){
            String tmp = url.split("http:")[1];
            url = "http"+tmp;
        }
        TFile.builder().toDisk(Disk.TMP).toPath(INDEX_PATH).toFile();
//指定索引库存放的路径
Directory directory = FSDirectory.open(TFile.getProperty().getFile().toPath());
//参数：分析器对象
IndexWriterConfig config = new IndexWriterConfig(new MyIKAnalyzer(true));
//创建indexwriter对象
IndexWriter indexWriter = new IndexWriter(directory, config);
//第一个参数：域的名称，第二个参数：域的内容，第三个参数：是否存储
Field titleField = new TextField("title", title, Field.Store.YES);
Field noteField = new TextField("note", note, Field.Store.YES);
Field linkField = new StoredField("link", url);
//创建document对象
Document document = new Document();
document.add(titleField);
document.add(noteField);
document.add(linkField);
//创建索引，并写入索引库
indexWriter.addDocument(document);
indexWriter.close();
        TFile.builder().toPath(LINKS_PATH).toName(url).toFile().create();
        Link link = new Link(title, note, url);
        String content = JSONObject.fromObject(link).toString();
        TWriter writer = new TWriter(TFile.getProperty());
        writer.write(content);
        TFile.builder().recycle();
    }

}
