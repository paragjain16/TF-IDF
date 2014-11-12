import java.io.*;
import java.util.*;

/**
 * Created by Parag on 09-11-2014.
 */
public class ArrfFile {
    ArrayList<AttributeReal> attrReal;
    AttributeClass attrClass;
    ArrayList<String> attrClassList;
    ArrayList<Document> docs;
    //Mapping from word to index in Attribute Real ArrayList
    //HashMap<String, Integer> mapping;
    //Mapping from class to line number
    //HashMap<String, Integer> classMapping;
    int totalDocs;
    String relation;
    String fileName;

    ArrfFile(ArrayList<AttributeReal> attrReal, AttributeClass attrClass, ArrayList<String> attrClassList,
             ArrayList<Document> docs, int totalDocs, String fileName, String relation){
        this.attrReal = attrReal;
        this.attrClass = attrClass;
        this.attrClassList = attrClassList;
        this.docs = docs;
        this.totalDocs = totalDocs;
        this.fileName = fileName;
        this.relation = relation;
    }

    static ArrfFile readFile(String fileName){
        ArrayList<AttributeReal> attrReal = new ArrayList<AttributeReal>();
        ArrayList<String> attrClassList = new ArrayList<String>();
        AttributeClass attrClass = null;
        ArrayList<Document> docs = new ArrayList<Document>();
        //Mapping from word to index in Attribute Real ArrayList
        //HashMap<String, Integer> mapping = new HashMap<String, Integer>();
        //Mapping from class to line number
        //HashMap<String, Integer> classMapping = new HashMap<String, Integer>();
        int totalDocs = 0;
        String relation = "";
        File f = new File(fileName);
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;
            int i = 0;
            int lineNum = 0;
            relation = br.readLine();
            while((line = br.readLine())!=null){
                if(line.startsWith("@attribute T")){
                    String[] tokens = line.split(" ");
                    String attr = tokens[1].substring(1);
                    attrReal.add(new AttributeReal(attr, i+"", tokens[2]));
                    i++;
                }else if(line.startsWith("{")){
                    Document doc = Document.parse(line, attrReal, lineNum);
                    docs.add(doc);
                    totalDocs++;
                }else if(line.startsWith("@attribute class")){
                    String[] tokens = line.split(" ");
                    String[] classes = tokens[2].substring(1, tokens[2].length()-1).split(",");
                    Collections.addAll(attrClassList, classes);
                    attrClass = new AttributeClass(attrClassList);
                }
                lineNum++;
            }
            for(AttributeReal attributeReal: attrReal){
                attributeReal.idf = Math.log((double)totalDocs/(1+attributeReal.docs));
            }
            // For TF IDF
            for(Document document: docs){
                final Document doc = document;
                int j=0;
                doc.idf = new ArrayList<Double>(doc.attrRealIndex.size());
                doc.tfIdf = new ArrayList<Double>(doc.attrRealIndex.size());

                //IDF
                doc.minHeap2 = new PriorityQueue<Integer>(doc.totalCount, new Comparator<Integer>(){
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        if(Double.compare(doc.idf.get(o1), doc.idf.get(o2)) > 0)
                            return 1;
                        else if(Double.compare(doc.idf.get(o1), doc.idf.get(o2)) < 0)
                            return -1;
                        return 0;
                    }
                });
                //TF-IDF
                doc.minHeap3 = new PriorityQueue<Integer>(doc.totalCount, new Comparator<Integer>(){
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        if(Double.compare(doc.tfIdf.get(o1), doc.tfIdf.get(o2)) > 0)
                            return 1;
                        else if(Double.compare(doc.tfIdf.get(o1), doc.tfIdf.get(o2)) < 0)
                            return -1;
                        return 0;
                    }
                });

                for(int index: doc.attrRealIndex){
                    double idf = doc.count.get(j)*attrReal.get(index).idf;
                    double tfIdf = doc.termFrequency.get(j)*attrReal.get(index).idf;
                    doc.idf.add(idf);
                    doc.idf.add(tfIdf);

                    if(doc.minHeap2.size() <= 10) {
                        doc.minHeap2.add(j);
                    }else{
                        int idx = doc.minHeap2.peek();
                        if(Double.compare(idf, doc.idf.get(idx)) > 0){
                            doc.minHeap2.poll();
                            doc.minHeap2.add(j);
                        }
                    }
                    if(doc.minHeap3.size() <= 10) {
                        doc.minHeap3.add(j);
                    }else{
                        int idx = doc.minHeap3.peek();
                        if(Double.compare(tfIdf, doc.tfIdf.get(idx)) > 0){
                            doc.minHeap3.poll();
                            doc.minHeap3.add(j);
                        }
                    }
                    j++;
                }

            }
            br.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ArrfFile(attrReal, attrClass, attrClassList, docs, totalDocs, fileName, relation);
    }
    static void writeTFfile(ArrfFile arrfFile, String tf){
        String file = arrfFile.fileName;
        if(file.split("/").length > 0){
            String[] tokens = file.split("/");
            file = tokens[tokens.length-1];
        }else if(file.split("\\\\").length>0){
            String[] tokens = file.split("\\\\");
            file = tokens[tokens.length-1];
        }
        File f = null;
        if(tf.equals("1"))
            f = new File("pjain11."+file.split("\\.")[0]+".tf1.arff");
        else if(tf.equals("2"))
            f = new File("pjain11."+file.split("\\.")[0]+".tf2.arff");
        else if(tf.equals("3"))
            f = new File("pjain11."+file.split("\\.")[0]+".idf.arff");
        else if(tf.equals("4"))
            f = new File("pjain11."+file.split("\\.")[0]+".tfidf.arff");
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(f)));
            pw.println(arrfFile.relation);
            pw.println();
            for(AttributeReal attributeReal: arrfFile.attrReal){
                pw.println(attributeReal);
            }
            pw.println(arrfFile.attrClass);
            pw.println();
            pw.println("@data");
            for(Document document: arrfFile.docs){
                if(tf.equals("1"))
                    pw.println(document.toTFString());
                else if(tf.equals("2"))
                    pw.println(document.toTF1String());
                else if(tf.equals("3"))
                    pw.println(document.toIDFString());
                else if(tf.equals("4"))
                    pw.println(document.toTFIDFString());
            }
            pw.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String tf = args[1];
        ArrfFile arrfFile = ArrfFile.readFile(args[0]);
        ArrfFile.writeTFfile(arrfFile, tf);
        HashSet<String> hs = new HashSet<String>(arrfFile.attrClassList.size());
        for(String str: arrfFile.attrClassList)
            hs.add(str);
        for(Document doc: arrfFile.docs) {
            if(hs.contains(doc.docClass)) {
                System.out.println("Top 10 words in document at line "+doc.docEntryLine+" with class "+doc.docClass+" -    ");
                if(tf.equals("1"))
                    System.out.println( doc.getTop10TFStr());
                else if(tf.equals("2"))
                    System.out.println( doc.getTop10TF1Str());
                else if(tf.equals("3"))
                    System.out.println( doc.getTop10IDFStr());
                else if(tf.equals("4"))
                    System.out.println( doc.getTop10TFIDFStr());
                //System.out.println( doc.getTop10Str());// + doc.classEntryLine + " " + doc.docClass);
                hs.remove(doc.docClass);
            }
        }
    }
}
