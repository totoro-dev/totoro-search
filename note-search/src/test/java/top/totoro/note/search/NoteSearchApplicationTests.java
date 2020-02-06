package top.totoro.note.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import top.totoro.note.search.bean.Links;
import top.totoro.note.search.service.SearchService;

import javax.annotation.Resource;
import java.io.File;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NoteSearchApplicationTests {

	@Resource
	private SearchService searchService;

	@Test
	public void testPutLinks() {
		Links links = new Links(new String []{"黄龙淼是程序员"},new String []{"note1"},new String []{"link1"});
//		Links links = new Links(new String []{"黄龙淼是程序员","黄龙淼是大学生","黄龙淼是个人"},new String []{"note1","note2","note3"},new String []{"link1","link2","link3"});
//		Links links = new Links(new String []{"黄龙淼c++"},new String []{"note1"},new String []{"http://blog.csdn.net/hlm2016/article/details/73603819"});
		searchService.putLinks(links);
	}

	@Test
	public void testSearchLinks(){
		Links links = searchService.searchLinks("黄龙淼是谁？");
		for (int i = 0; i < links.titles.length; i++) {
			System.out.println("关键字：黄龙淼是谁？\n搜索结果："+links.urls[i]);
		}
		links = searchService.searchLinks("关于note1的文章");
		for (int i = 0; i < links.titles.length; i++) {
			System.out.println("关键字：关于note1的文章\n搜索结果："+links.urls[i]);
		}
		links = searchService.searchLinks("黄龙淼关于note1的文章");
		for (int i = 0; i < links.titles.length; i++) {
			System.out.println("关键字：黄龙淼关于note1的文章\n搜索结果："+links.urls[i]);
		}
	}

	@Test
	public void createIndex() throws Exception {
		//指定索引库存放的路径//D:\temp\0108\index
		Directory directory = FSDirectory.open(new File("C:\\lucene\\index").toPath());
		//创建一个标准分析器
//		Analyzer analyzer = new StandardAnalyzer();
		//创建indexwriterCofig对象，第一个参数： Lucene的版本信息，可以选择对应的lucene版本也可以使用LATEST
		//第二根参数：分析器对象
		IndexWriterConfig config = new IndexWriterConfig(new MyIKAnalyzer(true));
		//创建indexwriter对象
		IndexWriter indexWriter = new IndexWriter(directory, config);
		//原始文档的路径
//		File dir = new File("D:\\data\\lucene\\资料");
//		for (File f : dir.listFiles()) {
//			//文件名
//			String fileName = f.getName();
//			//文件内容
//			String fileContent = FileUtils.readFileToString(f);
//			//文件路径
//			String filePath = f.getPath();
//			//文件的大小
//			long fileSize = FileUtils.sizeOf(f);
			//创建文件名域
			//第一个参数：域的名称，第二个参数：域的内容，第三个参数：是否存储
			Field fileNameField = new TextField("filename", "lucene", Field.Store.YES);
			//文件内容域
			Field fileContentField = new TextField("content", "hlm is a totoro!", Field.Store.YES);
			//文件路径域（不分析、不索引、只存储）
//			Field filePathField = new StoredField("path", filePath);
			//文件大小域
//			Field fileSizeField = new LongField("size", fileSize, Field.Store.YES);

			//创建document对象
			Document document = new Document();
			document.add(fileNameField);
			document.add(fileContentField);
//			document.add(filePathField);
//			document.add(fileSizeField);
			//创建索引，并写入索引库
			indexWriter.addDocument(document);
//		}
		//关闭indexwriter
		indexWriter.close();
	}

	@Test
	public void testMatchAllDocsQuery() throws Exception {
		//指定索引库存放的路径
		Directory directory = FSDirectory.open(new File("C:\\lucene\\index").toPath());
		//创建一个IndexReader对象
		IndexReader indexReader = DirectoryReader.open(directory);
		//创建IndexSearcher对象
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		//创建一个Query对象
		Query query = new MatchAllDocsQuery();
		System.out.println(query);
		//查询索引库
		TopDocs topDocs = indexSearcher.search(query, 100);
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		System.out.println("查询结果总记录数：" + topDocs.totalHits);
		//遍历查询结果
		for (ScoreDoc scoreDoc : scoreDocs) {
			int docId = scoreDoc.doc;
			//通过id查询文档对象
			Document document = indexSearcher.doc(docId);
			//取属性
			System.out.println(document.get("filename"));
			System.out.println(document.get("content"));
		}
		//关闭索引库
		indexReader.close();
	}

	private IndexSearcher getIndexSearcher() throws Exception {
		//指定索引库存放的路径
		Directory directory = FSDirectory.open(new File("C:\\lucene\\index").toPath());
		//创建一个IndexReader对象
		IndexReader indexReader = DirectoryReader.open(directory);
		//创建IndexSearcher对象
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		return indexSearcher;
	}

	private void printResult(IndexSearcher indexSearcher, Query query) throws Exception {
		//查询索引库
		TopDocs topDocs = indexSearcher.search(query, 100);
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		System.out.println("查询结果总记录数：" + topDocs.totalHits);
		//遍历查询结果
		for (ScoreDoc scoreDoc : scoreDocs) {
			int docId = scoreDoc.doc;
			//通过id查询文档对象
			Document document = indexSearcher.doc(docId);
			//取属性
			System.out.println(document.get("name"));
			System.out.println(document.get("content"));
		}
		//关闭索引库
		indexSearcher.getIndexReader().close();
	}

	@Test
	public void testQueryParser(){
		//创建一个QueryParser对象。参数1：默认搜索域 参数2：分析器对象。
		QueryParser queryParser = new QueryParser("content", new MyIKAnalyzer(true));
		//调用parse方法可以获得一个Query对象
		//参数：要查询的内容，可以是一句话。先分词在查询
		Query query = null;
		try {
			query = queryParser.parse("is a");
//		Query query = queryParser.parse("name:lucene OR name:apache");
			System.out.println(query.toString());
			printResult(getIndexSearcher(), query);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void parse() throws Exception{
		QueryParser queryParser = new QueryParser("",new MyIKAnalyzer(true));
		String content = queryParser.parse("黄龙淼是程序员").toString();
		String parse[] = content.split(" ");
		for (String p :
				parse) {
			System.out.println(p+"\n");
		}
	}
}
