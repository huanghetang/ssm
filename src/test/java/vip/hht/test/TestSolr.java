package vip.hht.test;

import org.apache.lucene.search.Sort;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author zhoumo
 * @datetime 2018/7/5 21:09
 * @desc
 */
public class TestSolr {

    /**
     * "shop_price": 4333,
     * "product_name": "黑马手机",
     * "product_catalog_name": "手机数码",
     * "product_description": "黑马【联想新品6月9日0点惊喜抢购，三期免息】6英寸全面屏、3760mAh电池、64GB内存、1600万像素AI双摄更多联想优惠请戳",
     * "market_price": 4444,
     * "id": "09fb55cebfb64c53bbe39bd9e8e7b79b",
     */
    @Test
    public void testAdd() throws IOException, SolrServerException {
        SolrServer solrServer = new HttpSolrServer("http://localhost:8080/solr/collection1");
        SolrInputDocument document = new SolrInputDocument();
        document.addField("id", "10086");
        document.addField("product_name", "人体艺术大师");
        UpdateResponse add = solrServer.add(document);
        System.out.println("add = " + add);
        solrServer.commit();
    }

    @Test
    public void testQuery() throws SolrServerException {
        SolrServer solrServer = new HttpSolrServer("http://localhost:8080/solr/collection1");
//        SolrQuery solrQuery = new SolrQuery("product_catalog_name:手机");
        //查询语法
        //相似度查询,容许编辑距离0-2
//        SolrQuery solrQuery = new SolrQuery("product_name:iphowr~2");
        SolrQuery solrQuery = new SolrQuery("shop_price:[100 TO 9899] AND product_name:未来人类 ");

        //按固定字段排序
        solrQuery.setSort("shop_price", SolrQuery.ORDER.desc);
        //设置分页
        solrQuery.setStart(0);
        solrQuery.setRows(20);
        //设置高亮标签
        solrQuery.setHighlightSimplePost("<em>");
        solrQuery.setHighlightSimplePre("</em>");
        //设置高亮字段
        solrQuery.addHighlightField("product_catalog_name");
        solrQuery.addHighlightField("product_name");
        //此处可以直接查询到对象的,传入class对象
        QueryResponse response = solrServer.query(solrQuery);
        SolrDocumentList results = response.getResults();
        //获取高亮结果集
        Map<String, Map<String, List<String>>> highlighting = response.getHighlighting();
        for (SolrDocument document : results) {
            String id = (String) document.get("id");
            float shop_price = (float) document.get("shop_price");
            String name = (String) document.get("product_catalog_name");
            String product_name = (String) document.get("product_name");
            String hightName = null;
            //第一层是id
            Map<String, List<String>> stringListMap = highlighting.get(id);
            //第二层 key是字段
            if (stringListMap != null && stringListMap.size() > 0) {
                List<String> strings = stringListMap.get("product_catalog_name");
                if (strings != null) {
                    hightName = strings.get(0);
                }
                List<String> strings2 = stringListMap.get("product_name");
                if (strings2 != null) {
                    product_name = strings2.get(0);
                }
            }


            System.out.println("id=" + id + " name = " + hightName+" product_name="+product_name+" shop_price="+shop_price);
        }


    }
}
