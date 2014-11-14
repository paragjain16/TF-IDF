import java.util.*;

/**
 * Created by Parag on 09-11-2014.
 */
public class Document {
    ArrayList<Integer> attrRealIndex;
    ArrayList<Integer> count;
    ArrayList<Double> termFrequency;
    ArrayList<Double> termFrequency1;
    ArrayList<Double> idf;
    ArrayList<Double> tfIdf;
    int totalCount;
    PriorityQueue<Integer> minHeap;
    PriorityQueue<Integer> minHeap1;
    PriorityQueue<Integer> minHeap2;
    PriorityQueue<Integer> minHeap3;
    String docClass;
    String classEntryLine;
    final ArrayList<AttributeReal> attrReal;
    int docEntryLine;
    int maxCountIndex;
    int maxCount;

    public Document(ArrayList<Integer> attrRealIndex, ArrayList<Integer> count, ArrayList<Double> termFrequency,
                    int totalCount, PriorityQueue<Integer> minHeap, PriorityQueue<Integer> minHeap1, String docClass, ArrayList<AttributeReal> attrReal,
                    String classEntryLine, int maxCountIndex, int maxCount, ArrayList<Double> termFrequency1, int docEntryLine){
        this.attrRealIndex = attrRealIndex;
        this.count = count;
        this.termFrequency = termFrequency;
        this.totalCount = totalCount;
        this.minHeap = minHeap;
        this.minHeap1 = minHeap1;
        this.docClass = docClass;
        this.attrReal = attrReal;
        this.classEntryLine = classEntryLine;
        this.docEntryLine = docEntryLine;
        this.maxCountIndex = maxCountIndex;
        this.maxCount = maxCount;
        this.termFrequency1 = termFrequency1;
    }

    public String toTF1String(){
        StringBuilder str = new StringBuilder("{");
        int i=0;
        for(int index: attrRealIndex){
            str.append(index+" "+termFrequency.get(i)+",");
            i++;
        }
        str.append(classEntryLine+" "+docClass+"}");
        return str.toString();
    }

    public String toTF2String(){
        StringBuilder str = new StringBuilder("{");
        int i=0;
        for(int index: attrRealIndex){
            str.append(index+" "+termFrequency1.get(i)+",");
            i++;
        }
        str.append(classEntryLine+" "+docClass+"}");
        return str.toString();
    }

    public String toIDFString(){
        StringBuilder str = new StringBuilder("{");
        int i=0;
        for(int index: attrRealIndex){
            str.append(index+" "+idf.get(i)+",");
            i++;
        }
        str.append(classEntryLine+" "+docClass+"}");
        return str.toString();
    }

    public String toTFIDFString(){
        StringBuilder str = new StringBuilder("{");
        int i=0;
        for(int index: attrRealIndex){
            str.append(index+" "+tfIdf.get(i)+",");
            i++;
        }
        str.append(classEntryLine+" "+docClass+"}");
        return str.toString();
    }

    static Document parse(String line, final ArrayList<AttributeReal> attrReal, int docEntryLine){
        final ArrayList<Integer> attrRealIndex = new ArrayList<Integer>();
        final ArrayList<Integer> count = new ArrayList<Integer>();
        final ArrayList<Double> termFrequency = new ArrayList<Double>();
        final ArrayList<Double> termFrequency1 = new ArrayList<Double>();
        int totalCount = 0;
        String docClass = "-1";
        String classEntryLine = "0";
        int maxCountIndex = -1;
        int maxCount = Integer.MIN_VALUE;
        String[] entries = line.substring(1, line.length()-1).split(",");

        for(int i=0; i < entries.length-1; i++){
            String[] token = entries[i].split(" ");
            int index = Integer.parseInt(token[0]);
            attrRealIndex.add(index);
            attrReal.get(index).incrementDocs();
            int freq = Integer.parseInt(token[1]);
            if(freq > maxCount){
                maxCount = freq;
                maxCountIndex = index;
            }
            count.add(freq);
            totalCount+=freq;
        }
        classEntryLine= entries[entries.length-1].split(" ")[0];
        docClass= entries[entries.length-1].split(" ")[1];
        PriorityQueue<Integer> minHeap = new PriorityQueue<Integer>(10, new Comparator<Integer>(){
            @Override
            public int compare(Integer o1, Integer o2) {
                if(termFrequency.get(o1) > termFrequency.get(o2))
                    return 1;
                else if(termFrequency.get(o1) < termFrequency.get(o2))
                    return -1;
                return 0;
            }
        });
        PriorityQueue<Integer> minHeap1 = new PriorityQueue<Integer>(10, new Comparator<Integer>(){
            @Override
            public int compare(Integer o1, Integer o2) {
                if(termFrequency1.get(o1) > termFrequency1.get(o2))
                    return 1;
                else if(termFrequency1.get(o1) < termFrequency1.get(o2))
                    return -1;
                return 0;
            }
        });

        for(int i=0; i<attrRealIndex.size(); i++){
            double termFreq = (double)count.get(i)/totalCount;
            double termFreq1 = (double)count.get(i)/maxCount;
            termFrequency.add(termFreq);
            termFrequency1.add(termFreq1);
            if(minHeap.size() <= 10) {
                minHeap.add(i);
            }else{
                int index = minHeap.peek();
                if(Double.compare(termFreq, termFrequency.get(index)) > 0){
                    minHeap.poll();
                    minHeap.add(i);
                }
            }
            if(minHeap1.size() <= 10) {
                minHeap1.add(i);
            }else{
                int index = minHeap1.peek();
                if(Double.compare(termFreq1, termFrequency1.get(index)) > 0){
                    minHeap1.poll();
                    minHeap1.add(i);
                }
            }
        }
        return new Document(attrRealIndex, count, termFrequency, totalCount, minHeap, minHeap1, docClass,
                attrReal, classEntryLine, maxCountIndex, maxCount, termFrequency1, docEntryLine);
    }
    ////////////////////////////////////////// Methods below are not used
    public String getTop10(){
        StringBuilder sb = new StringBuilder();
        while(!minHeap.isEmpty()){
            int curr = minHeap.poll();
            int index = attrRealIndex.get(curr);
            sb.append("Index: "+index+" Word: "+attrReal.get(index).attr+" Count: "+count.get(curr)+" Term Freq: "+termFrequency.get(curr));
            sb.append("\n");
        }
        return sb.toString();
    }
    public String getTop10TFStr(){
        StringBuilder sb = new StringBuilder();
        while(!minHeap.isEmpty()){
            int curr = minHeap.poll();
            int index = attrRealIndex.get(curr);
            sb.insert(0, attrReal.get(index).attr+" "+termFrequency.get(curr)+", ");
        }
        return sb.toString().substring(0, sb.length()-2);
    }
    public String getTop10TF1Str(){
        StringBuilder sb = new StringBuilder();
        while(!minHeap1.isEmpty()){
            int curr = minHeap1.poll();
            int index = attrRealIndex.get(curr);
            sb.insert(0, attrReal.get(index).attr+" "+termFrequency1.get(curr)+", ");
        }
        return sb.toString().substring(0, sb.length()-2);
    }
    public String getTop10IDFStr(){
        StringBuilder sb = new StringBuilder();
        while(!minHeap2.isEmpty()){
            int curr = minHeap2.poll();
            int index = attrRealIndex.get(curr);
            sb.insert(0, attrReal.get(index).attr+" "+idf.get(curr)+", ");
        }
        return sb.toString().substring(0, sb.length()-2);
    }
    public String getTop10TFIDFStr(){
        StringBuilder sb = new StringBuilder();
        while(!minHeap3.isEmpty()){
            int curr = minHeap3.poll();
            int index = attrRealIndex.get(curr);
            sb.insert(0, attrReal.get(index).attr+" "+tfIdf.get(curr)+", ");
        }
        return sb.toString().substring(0, sb.length()-2);
    }
}