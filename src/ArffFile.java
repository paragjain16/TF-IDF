import java.io.*;
import java.util.*;

/**
 * Created by Parag on 09-11-2014.
 */
public class ArffFile {
    ArrayList<AttributeReal> attrReal;
    AttributeClass attrClass;
    ArrayList<String> attrClassList;
    ArrayList<Document> docs;
    PriorityQueue<Integer> minHeapIDF;
    //Mapping from word to index in Attribute Real ArrayList
    //HashMap<String, Integer> mapping;
    //Mapping from class to line number
    //HashMap<String, Integer> classMapping;
    int totalDocs;
    String relation;
    String fileName;
    HashMap<String, ArrayList<Document>> mappingFromClassToDocs;

    ArffFile(ArrayList<AttributeReal> attrReal, AttributeClass attrClass, ArrayList<String> attrClassList,
             ArrayList<Document> docs, int totalDocs, String fileName, String relation,
             PriorityQueue<Integer> minHeapIDF,
             HashMap<String, ArrayList<Document>> mappingFromClassToDocs){
        this.attrReal = attrReal;
        this.attrClass = attrClass;
        this.attrClassList = attrClassList;
        this.docs = docs;
        this.totalDocs = totalDocs;
        this.fileName = fileName;
        this.relation = relation;
        this.minHeapIDF = minHeapIDF;
        this.mappingFromClassToDocs = mappingFromClassToDocs;
    }

    static ArffFile readFile(String fileName){
        final ArrayList<AttributeReal> attrReal = new ArrayList<AttributeReal>();
        HashMap<String, ArrayList<Document>> mappingFromClassToDocs = new HashMap<String, ArrayList<Document>>();
        ArrayList<String> attrClassList = new ArrayList<String>();
        AttributeClass attrClass = null;
        ArrayList<Document> docs = new ArrayList<Document>();
        //Mapping from word to index in Attribute Real ArrayList
        //HashMap<String, Integer> mapping = new HashMap<String, Integer>();
        //Mapping from class to line number
        //HashMap<String, Integer> classMapping = new HashMap<String, Integer>();
        PriorityQueue<Integer> minHeapIDF = new PriorityQueue<Integer>(10, new Comparator<Integer>(){
            @Override
            public int compare(Integer o1, Integer o2) {
                if(attrReal.get(o1).idf > attrReal.get(o2).idf)
                    return 1;
                else if(attrReal.get(o1).idf < attrReal.get(o2).idf)
                    return -1;
                return 0;
            }
        });

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
                    if(mappingFromClassToDocs.containsKey(doc.docClass)){
                        mappingFromClassToDocs.get(doc.docClass).add(doc);
                    }else{
                        ArrayList<Document> d = new ArrayList<Document>();
                        d.add(doc);
                        mappingFromClassToDocs.put(doc.docClass, d);
                    }
                }else if(line.startsWith("@attribute class")){
                    String[] tokens = line.split(" ");
                    String[] classes = tokens[2].substring(1, tokens[2].length()-1).split(",");
                    Collections.addAll(attrClassList, classes);
                    attrClass = new AttributeClass(attrClassList);
                }
                lineNum++;
            }
            //Calculate IDF
            int k=0;
            for(AttributeReal attributeReal: attrReal){
                //System.out.println("Total docs "+totalDocs);
                //System.out.println("Term "+attributeReal.attr+" is in "+attributeReal.docs+" docs");
                double idf = Math.log((double)totalDocs/(1+attributeReal.docs));
                attributeReal.idf = idf;
                if(attributeReal.docs > 0) {
                    if (minHeapIDF.size() < 10) {
                        minHeapIDF.add(k);
                    } else {
                        int index = minHeapIDF.peek();
                        if (Double.compare(idf, attrReal.get(index).idf) > 0) {
                            minHeapIDF.poll();
                            minHeapIDF.add(k);
                        }
                    }
                }
                k++;
            }
            // For TF IDF
            for(Document document: docs){
                final Document doc = document;

                doc.idf = new ArrayList<Double>(doc.attrRealIndex.size());
                doc.tfIdf = new ArrayList<Double>(doc.attrRealIndex.size());

                //IDF
                doc.minHeap2 = new PriorityQueue<Integer>(10, new Comparator<Integer>(){
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        return Double.compare(attrReal.get(o1).idf, attrReal.get(o1).idf);
                    }
                });
                //TF-IDF
                doc.minHeap3 = new PriorityQueue<Integer>(10, new Comparator<Integer>(){
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        return Double.compare(doc.tfIdf.get(o1), doc.tfIdf.get(o2));
                    }
                });
                int j=0;
                for(int index: doc.attrRealIndex){
                    double idf = doc.count.get(j)*attrReal.get(index).idf;
                    double tfIdf = doc.termFrequency.get(j)*attrReal.get(index).idf;
                    doc.idf.add(idf);
                    doc.tfIdf.add(tfIdf);

                    /*if(doc.minHeap2.size() < 10) {
                        doc.minHeap2.add(j);
                    }else{
                        int idx = doc.minHeap2.peek();
                        if(Double.compare(idf, doc.idf.get(idx)) > 0){
                            doc.minHeap2.poll();
                            doc.minHeap2.add(j);
                        }
                    }*/
                    double normalIDF = attrReal.get(index).idf;
                    if(attrReal.get(index).docs > 0) {
                        if (doc.minHeap2.size() < 10) {
                            doc.minHeap2.add(index);
                        } else {
                            int idx = doc.minHeap2.peek();
                            if (Double.compare(normalIDF, attrReal.get(idx).idf) > 0) {
                                doc.minHeap2.poll();
                                doc.minHeap2.add(index);
                            }
                        }
                    }
                    if(doc.minHeap3.size() < 10) {
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
        return new ArffFile(attrReal, attrClass, attrClassList, docs, totalDocs, fileName, relation, minHeapIDF, mappingFromClassToDocs);
    }

    static void writeTFfile(ArffFile arrfFile, String tf){
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
                    pw.println(document.toTF1String());
                else if(tf.equals("2"))
                    pw.println(document.toTF2String());
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

    public String getTop10IDFStr(){
        StringBuilder sb = new StringBuilder();
        while(!minHeapIDF.isEmpty()){
            int curr = minHeapIDF.poll();
            sb.insert(0, attrReal.get(curr).attr+" "+attrReal.get(curr).idf+", ");
        }
        return sb.toString().substring(0, sb.length()-2);
    }

    public void getTop10TF1Str(){
        for (String str : attrClassList){
            if(!mappingFromClassToDocs.containsKey(str))
                continue;
            final ArrayList<Double> tflist = new ArrayList<Double>();
            final ArrayList<String> attributes = new ArrayList<String>();
            PriorityQueue<Integer> minHeapLocal = new PriorityQueue<Integer>(10, new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    if(Double.compare(tflist.get(o1), tflist.get(o2)) > 0)
                        return 1;
                    else if(Double.compare(tflist.get(o1), tflist.get(o2)) < 0)
                        return -1;
                    else
                        return 0;
                }
            });
            StringBuilder sb = new StringBuilder();


            //HashMap<String, Double> map = new HashMap<String, Double>();
            for(Document doc: mappingFromClassToDocs.get(str)){
                while(!doc.minHeap.isEmpty()){
                    int curr = doc.minHeap.poll();
                    int index = doc.attrRealIndex.get(curr);
                    double tf = doc.termFrequency.get(curr);
                    String attr = doc.attrReal.get(index).attr;
                    tflist.add(tf);
                    attributes.add(attr);
                }

            }
            HashMap<String, Integer> attrInHeap = new HashMap<String, Integer>();
            for(int i =0; i< tflist.size(); i++){
                String currentAttribute = attributes.get(i);
                if(!attrInHeap.containsKey(currentAttribute)) {
                    if (minHeapLocal.size() < 10) {
                        minHeapLocal.offer(i);
                        attrInHeap.put(currentAttribute, i);
                    }else{
                        int index = minHeapLocal.peek();
                        if(Double.compare(tflist.get(i), tflist.get(index)) > 0){
                            minHeapLocal.poll();
                            attrInHeap.remove(attributes.get(index));
                            minHeapLocal.offer(i);
                            attrInHeap.put(currentAttribute, i);
                        }
                    }

                }else{
                    int indexOfDuplicate = attrInHeap.get(attributes.get(i));
                    if(Double.compare(tflist.get(indexOfDuplicate), tflist.get(i)) < 0){
                        minHeapLocal.remove(indexOfDuplicate);
                        minHeapLocal.offer(i);
                        attrInHeap.put(currentAttribute, i);
                    }
                }
            }
            System.out.println("Top 10 TF (1) words for newsgroup " + str + " are - ");
            while(!minHeapLocal.isEmpty()){
                int curr = minHeapLocal.poll();
                sb.insert(0, attributes.get(curr)+" "+tflist.get(curr)+", ");
            }
            System.out.println(sb.toString().substring(0, sb.length() - 2));
        }
    }
    public void getTop10TF2Str(){
        for (String str : attrClassList){
            if(!mappingFromClassToDocs.containsKey(str))
                continue;
            final ArrayList<Double> tflist = new ArrayList<Double>();
            final ArrayList<String> attributes = new ArrayList<String>();
            PriorityQueue<Integer> minHeapLocal = new PriorityQueue<Integer>(10, new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    if(Double.compare(tflist.get(o1), tflist.get(o2)) > 0)
                        return 1;
                    else if(Double.compare(tflist.get(o1), tflist.get(o2)) < 0)
                        return -1;
                    else
                        return 0;
                }
            });
            StringBuilder sb = new StringBuilder();


            //HashMap<String, Double> map = new HashMap<String, Double>();
            for(Document doc: mappingFromClassToDocs.get(str)){
                while(!doc.minHeap1.isEmpty()){
                    int curr = doc.minHeap1.poll();
                    int index = doc.attrRealIndex.get(curr);
                    double tf = doc.termFrequency1.get(curr);
                    String attr = doc.attrReal.get(index).attr;
                    tflist.add(tf);
                    attributes.add(attr);
                }

            }
            HashMap<String, Integer> attrInHeap = new HashMap<String, Integer>();
            for(int i =0; i< tflist.size(); i++){
                String currentAttribute = attributes.get(i);
                if(!attrInHeap.containsKey(currentAttribute)) {
                    if (minHeapLocal.size() < 10) {
                        minHeapLocal.offer(i);
                        attrInHeap.put(currentAttribute, i);
                    }else{
                        int index = minHeapLocal.peek();
                        if(Double.compare(tflist.get(i), tflist.get(index)) > 0){
                            minHeapLocal.poll();
                            attrInHeap.remove(attributes.get(index));
                            minHeapLocal.offer(i);
                            attrInHeap.put(currentAttribute, i);
                        }
                    }

                }else{
                    int indexOfDuplicate = attrInHeap.get(attributes.get(i));
                    if(Double.compare(tflist.get(indexOfDuplicate), tflist.get(i)) < 0){
                        minHeapLocal.remove(indexOfDuplicate);
                        minHeapLocal.offer(i);
                        attrInHeap.put(currentAttribute, i);
                    }
                }
            }
            System.out.println("Top 10 TF (2) words for newsgroup " + str + " are - ");
            while(!minHeapLocal.isEmpty()){
                int curr = minHeapLocal.poll();
                sb.insert(0, attributes.get(curr)+" "+tflist.get(curr)+", ");
            }
            System.out.println(sb.toString().substring(0, sb.length() - 2));
        }
    }

    public void getTop10TFIDFStr(){
        for (String str : attrClassList){
            if(!mappingFromClassToDocs.containsKey(str))
                continue;
            final ArrayList<Double> tfidflist = new ArrayList<Double>();
            final ArrayList<String> attributes = new ArrayList<String>();
            PriorityQueue<Integer> minHeapLocal = new PriorityQueue<Integer>(10, new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return Double.compare(tfidflist.get(o1), tfidflist.get(o2));
                }
            });
            StringBuilder sb = new StringBuilder();

            //HashMap<String, Double> map = new HashMap<String, Double>();
            for(Document doc: mappingFromClassToDocs.get(str)){
                while(!doc.minHeap3.isEmpty()){
                    int curr = doc.minHeap3.poll();
                    int index = doc.attrRealIndex.get(curr);
                    double tfidf = doc.tfIdf.get(curr);
                    String attr = doc.attrReal.get(index).attr;
                    tfidflist.add(tfidf);
                    attributes.add(attr);
                }

            }
            HashMap<String, Integer> attrInHeap = new HashMap<String, Integer>();
            for(int i =0; i< tfidflist.size(); i++){
                String currentAttribute = attributes.get(i);
                if(!attrInHeap.containsKey(currentAttribute)) {
                    if (minHeapLocal.size() < 10) {
                        minHeapLocal.offer(i);
                        attrInHeap.put(currentAttribute, i);
                    }else{
                        int index = minHeapLocal.peek();
                        if(Double.compare(tfidflist.get(i), tfidflist.get(index)) > 0){
                            minHeapLocal.poll();
                            attrInHeap.remove(attributes.get(index));
                            minHeapLocal.offer(i);
                            attrInHeap.put(currentAttribute, i);
                        }
                    }

                }else{
                    int indexOfDuplicate = attrInHeap.get(attributes.get(i));
                    if(Double.compare(tfidflist.get(indexOfDuplicate), tfidflist.get(i)) < 0){
                        minHeapLocal.remove(indexOfDuplicate);
                        minHeapLocal.offer(i);
                        attrInHeap.put(currentAttribute, i);
                    }
                }
            }
            System.out.println("Top 10 TF IDF words for newsgroup " + str + " are - ");
            while(!minHeapLocal.isEmpty()){
                int curr = minHeapLocal.poll();
                sb.insert(0, attributes.get(curr)+" "+tfidflist.get(curr)+", ");
            }
            System.out.println(sb.toString().substring(0, sb.length() - 2));
        }
    }
    public void getTop10IDF1Str(){

        for (String str : attrClassList){
            if(!mappingFromClassToDocs.containsKey(str))
                continue;
            final ArrayList<Double> tflist = new ArrayList<Double>();
            final ArrayList<String> attributes = new ArrayList<String>();
            PriorityQueue<Integer> minHeapLocal = new PriorityQueue<Integer>(10, new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return Double.compare(tflist.get(o1), tflist.get(o2));
                }
            });
            StringBuilder sb = new StringBuilder();


            //HashMap<String, Double> map = new HashMap<String, Double>();
            for(Document doc: mappingFromClassToDocs.get(str)){
                while(!doc.minHeap2.isEmpty()){
                    int index = doc.minHeap2.poll();
                    //int index = doc.attrRealIndex.get(curr);
                    double idf = attrReal.get(index).idf;
                    String attr = attrReal.get(index).attr;
                    tflist.add(idf);
                    attributes.add(attr);
                }

            }
            HashMap<String, Integer> attrInHeap = new HashMap<String, Integer>();
            for(int i =0; i< tflist.size(); i++){
                String currentAttribute = attributes.get(i);
                if(!attrInHeap.containsKey(currentAttribute)) {
                    if (minHeapLocal.size() < 10) {
                        minHeapLocal.offer(i);
                        attrInHeap.put(currentAttribute, i);
                    }else{
                        int index = minHeapLocal.peek();
                        if(Double.compare(tflist.get(i), tflist.get(index)) > 0){
                            minHeapLocal.poll();
                            attrInHeap.remove(attributes.get(index));
                            minHeapLocal.offer(i);
                            attrInHeap.put(currentAttribute, i);
                        }
                    }

                }else{
                    int indexOfDuplicate = attrInHeap.get(attributes.get(i));
                    if(Double.compare(tflist.get(indexOfDuplicate), tflist.get(i)) < 0){
                        minHeapLocal.remove(indexOfDuplicate);
                        minHeapLocal.offer(i);
                        attrInHeap.put(currentAttribute, i);
                    }
                }
            }
            System.out.println("Top 10 IDF words for newsgroup " + str + " are - ");
            while(!minHeapLocal.isEmpty()){
                int curr = minHeapLocal.poll();
                sb.insert(0, attributes.get(curr)+" "+tflist.get(curr)+", ");
            }
            System.out.println(sb.toString().substring(0, sb.length() - 2));
        }
    }
    public static void main(String[] args) {
        String tf = args[1];
        ArffFile arrfFile = ArffFile.readFile(args[0]);
        ArffFile.writeTFfile(arrfFile, tf);
        if(tf.equals("3")) {
            arrfFile.getTop10IDF1Str();
            System.out.println("Top 10 IDF words - "+arrfFile.getTop10IDFStr());
        }
        else if(tf.equals("1"))
            arrfFile.getTop10TF1Str();
        else if(tf.equals("2"))
            arrfFile.getTop10TF2Str();
        else if(tf.equals("4"))
            arrfFile.getTop10TFIDFStr();
    }
}
        /*else {
            HashSet<String> hs = new HashSet<String>();
            for (String str : arrfFile.attrClassList)
                hs.add(str);
            for (Document doc : arrfFile.docs) {
                if (hs.contains(doc.docClass)) {
                    if (tf.equals("1")) {
                        System.out.println("Top 10 TF (1) words in document at line " + doc.docEntryLine + " with class " + doc.docClass + " -    ");
                        System.out.println(doc.getTop10TFStr());
                    }
                    else if (tf.equals("2")) {
                        System.out.println("Top 10 TF (2) words in document at line " + doc.docEntryLine + " with class " + doc.docClass + " -    ");
                        System.out.println(doc.getTop10TF1Str());
                    }
                    else if (tf.equals("4")) {
                        System.out.println("Top 10 TF IDF words in document at line " + doc.docEntryLine + " with class " + doc.docClass + " -    ");
                        System.out.println(doc.getTop10TFIDFStr());
                    }
                    //System.out.println( doc.getTop10Str());// + doc.classEntryLine + " " + doc.docClass);
                    hs.remove(doc.docClass);
                }
            }
        //}*/
