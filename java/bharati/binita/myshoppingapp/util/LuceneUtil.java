package bharati.binita.myshoppingapp.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.standard.builders.PhraseQueryNodeBuilder;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.SortField.Type;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

/**
 * 
 * @author binita.bharati@gmail.com
 * DEmonstartes use of Lucene's generic QueryParser 
 * 
 * Ref : http://lucene.apache.org/core/6_6_2/queryparser/org/apache/lucene/queryparser/classic/package-summary.html
 *
 */
public class LuceneUtil {
	
	private static Map<String, String> actualToCalculatedRankMap;
	
	static {
		actualToCalculatedRankMap = new HashMap<String, String>();
		actualToCalculatedRankMap.put("5", "1");
		actualToCalculatedRankMap.put("4", "2");
		actualToCalculatedRankMap.put("3", "3");
		actualToCalculatedRankMap.put("2", "4");
		actualToCalculatedRankMap.put("1", "5");
	}
	
	public static void createProductIndex(List<Map<String, Object>> productList, String indexDir) {
		System.out.println("createProductIndex: emtered with productList = "+productList);
		FSDirectory dir = null;
		IndexWriter writer = null;
		try {
			 dir = FSDirectory.open(new File(indexDir).toPath());
			 IndexWriterConfig config = new IndexWriterConfig( new StandardAnalyzer());
			 writer = new IndexWriter(dir, config);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		 int originalNumDocs = writer.numRamDocs();
		    for (Map<String, Object> eachProduct : productList)  {
		      System.out.println("Handling product = "+eachProduct);
		      try {
		        Document doc = new Document();

		        //===================================================
		        // add contents of file
		        //===================================================
		       
		        doc.add(new TextField("searchTag", eachProduct.get("searchTag")+"", Field.Store.YES));	        
		        doc.add(new StringField("productId", eachProduct.get("productId")+"", Field.Store.YES));
		        doc.add(new StringField("name", eachProduct.get("name")+"", Field.Store.YES));
		        doc.add(new StringField("price", eachProduct.get("price")+"", Field.Store.YES));
		        doc.add(new StringField("pictures", eachProduct.get("pictures")+"", Field.Store.YES));
		        doc.add(new StringField("actualRank", eachProduct.get("rank")+"", Field.Store.YES));
		        //actualRank to calculatedRank mapping 5 -> 1, 4 -> 2, 3 -> 3, 2 -> 4, 1 -> 5
		        Iterator<String> mapItr = eachProduct.keySet().iterator();
		        while (mapItr.hasNext()) {
		        	String key = mapItr.next();		        	
		        	String value = eachProduct.get(key) + "";
		        	System.out.println("key = "+key + ", value = "+value);
		        	
		        }
		        System.out.println("extracting rank1 = "+eachProduct.get("rank")+", rank2 =" + actualToCalculatedRankMap.get(eachProduct.get("rank")+""));
		        doc.add(new SortedDocValuesField("calculatedRank", new BytesRef(actualToCalculatedRankMap.get(eachProduct.get("rank")+""))));
		        //No need to display calculatedRank in search results.Only, actualRank should be displayed.
		        //doc.add(new StoredField("calculatedRank", actualToCalculatedRankMap.get(eachProduct.get("rank")+"")));

		        writer.addDocument(doc);
		        System.out.println("Added: " + eachProduct);
		      } catch (Exception e) {
		    	e.printStackTrace();
		        System.out.println("Could not add: " + eachProduct);
		      } finally {
		        
		      }
		    }
		    
		    int newNumDocs = writer.numRamDocs();
		    System.out.println("");
		    System.out.println("************************");
		    System.out.println((newNumDocs - originalNumDocs) + " documents added.");
		    System.out.println("************************");
		    try {
		    	writer.close();
		    } catch (Exception ex) {
		    	ex.printStackTrace();
		    }
		    
	}
	
	
	public static List<Document> search(String searchStr, String indexDir) {
		TopScoreDocCollector collector = TopScoreDocCollector.create(5, 5);
		List<Document> matchedDocList = new ArrayList<Document>();
	      try {
	        QueryParser qp = new QueryParser("searchTag", new StandardAnalyzer());
	        Sort sort = new Sort(SortField.FIELD_SCORE,
                    new SortField("calculatedRank", Type.STRING));
 
	        Query q = qp.parse(searchStr);  
	        
	        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexDir).toPath()));
	        IndexSearcher searcher = new IndexSearcher(reader);
				//searcher.setSimilarity(new MySimilarity());
				
			
	        //searcher.search(q, collector);
	        //ScoreDoc[] hits = collector.topDocs().scoreDocs;
	        TopDocs docs = searcher.search(q, 10, sort, true);
	        ScoreDoc[] hits = docs.scoreDocs;

	        // 4. display results
	        System.out.println("Found " + hits.length + " hits.");
	        for(int i=0;i<hits.length;++i) {
	          int docId = hits[i].doc;
	          Document d = searcher.doc(docId);
	          System.out.println( "ProductId = " + d.get("productId") + " searchTag = "+d.get("searchTag")  + " actualRank = "+d.get("actualRank") );
	          //System.out.println("Explaination = "+searcher.explain(q, docId));
	          matchedDocList.add(d);
	        }

	      } catch (Exception e) {
	    	e.printStackTrace();
	        System.out.println("Error searching " + searchStr + " : " + e.getMessage());
	      }
	      System.out.println("exiting with searchResult = "+matchedDocList);
	      return matchedDocList;
	}
	
	
	public static void main(String[] args) {
		//LuceneUtil.search("gold bangles", "C:\\binita\\QuickTest\\myshoppingapp\\lucene\\index");
		LuceneUtil.search("searchTag:\"bangles gold\" OR searchTag:\"gold bangles\"", "C:\\binita\\QuickTest\\myshoppingapp\\lucene\\index");
	}

}

