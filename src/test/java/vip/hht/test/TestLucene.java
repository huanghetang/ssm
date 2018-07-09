package vip.hht.test;


import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.io.IOException;

/**
 * @author zhoumo
 * @datetime 2018/7/4 16:57
 * @desc
 */
public class TestLucene {

    /**
     * 使用lucene写入索引库
     * @throws IOException
     */
    @Test
    public void writeIndex() throws IOException {
        //创建索引目标目录
        FSDirectory fsDirectory = FSDirectory.open(new File("indexDir"));
        //创建文档对象
        Document document = new Document();
        //设置文档需要保存的域
            //StringField 索引,不分词
        document.add(new LongField("id",6L, Field.Store.YES));
        document.add(new TextField("title","测试666黑马程序", Field.Store.YES));
        TextField contentField = new TextField("content", "黑马吊炸天666程序", Field.Store.YES);
        //激励因子
        contentField.setBoost(1000);
        document.add(contentField);
        //创建写入对象配置对象
        IndexWriterConfig conf= new IndexWriterConfig(Version.LATEST, new IKAnalyzer());
//        conf.setOpenMode(IndexWriterConfig.OpenMode.APPEND);
        //创建写入对象
        IndexWriter indexWriter = new IndexWriter(fsDirectory, conf);
        //写入索引
        indexWriter.addDocument(document);
        //提交,关闭资源
        indexWriter.commit();;
        indexWriter.close();
    }

    @Test
    public void deleteIndexByQuery() throws IOException {
        FSDirectory fsDirectory = FSDirectory.open(new File("indexDir"));
        IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, new IKAnalyzer());
        IndexWriter writer = new IndexWriter(fsDirectory, config);
        //创建词条
        Term term = new Term("id","002");
        //按词条删除
//        writer.deleteDocuments(term);
        writer.deleteAll();
        writer.commit();
        writer.close();
    }

    @Test
    public void deleteIndexbyTerm() throws IOException {
        FSDirectory fsDirectory = FSDirectory.open(new File("indexDir"));
        IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, new IKAnalyzer());
        IndexWriter writer = new IndexWriter(fsDirectory, config);
        //创建词条
        Term term = new Term("id","001");
        //加入查询条件
        Query query  = new TermQuery(term);
        //按条件删除
        writer.deleteDocuments(query);
        writer.commit();
        writer.close();
    }

    @Test
    public void query1() throws IOException, ParseException, InvalidTokenOffsetsException {
        //获取磁盘资源
        FSDirectory fsDirectory = FSDirectory.open(new File("indexDir"));
        //创建流
        IndexReader indexReader = DirectoryReader.open(fsDirectory);
        //创建查询对象
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        //创建查询条件
        QueryParser queryParser = new QueryParser("title", new IKAnalyzer());
        //词条查询
        //Query query = queryParser.parse("程序员");
       // Query query = new TermQuery(new Term("title","程序员"));
        //通配符查询
        //Query query = new WildcardQuery(new Term("title","*员*"));
        //模糊查询(可以指定编辑距离)
        //Query query = new FuzzyQuery(new Term("title","员"),1);
        //还有数值查询,组合查询
        BooleanQuery query = new BooleanQuery();
        Query query1 = NumericRangeQuery.newLongRange("id", 5L, 5L, true, true);
        Query query2 = NumericRangeQuery.newLongRange("id", 5L, 6L, true, true);
        query.add(query1, BooleanClause.Occur.MUST_NOT);
        query.add(query2, BooleanClause.Occur.MUST);
        //查询结果
        //创建排序对象
       // SortField field = new SortField("id", SortField.Type.STRING,false);
        //Sort sort = new Sort(field);

        //需要加高亮条件的记录对象(分词记录部分的条件和查询分词的条件可以不一致,但是一般都一致)
        Scorer fragmentScorer = new QueryScorer(queryParser.parse("程序"));
        //高亮标签
        Formatter formatter = new SimpleHTMLFormatter("<em>","</em>");
        //创建高亮工具
        Highlighter highlighter = new Highlighter(formatter,fragmentScorer);
        //查询  (sort带排序,默认是按照激励因子降序排序)
        // TopDocs topDocs = indexSearcher.search(query, 10,sort);
        TopDocs topDocs = indexSearcher.search(query, 10);
        //命中
        int hits = topDocs.totalHits;
        System.out.println("hits = " + hits);
        //遍历评分文档,
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc scoreDoc : scoreDocs) {
            //评分
            float score = scoreDoc.score;
            //文档对象id
            int docId = scoreDoc.doc;
            Document document = indexSearcher.doc(docId);
            String id = document.get("id");
            String titleResult = document.get("title");
            //获取高亮字段(对字段继续分词)
            String highLightTitle = highlighter.getBestFragment(new IKAnalyzer(), "title", titleResult);
            System.out.println("score="+score+"  id = " + id+"  title="+highLightTitle);
        }
        indexSearcher.getIndexReader().close();


    }


    public static void main(String[] args) {
        File file = new File("/test");
        String path = file.getAbsolutePath();
        System.out.println("path = " + path);
    }
}
