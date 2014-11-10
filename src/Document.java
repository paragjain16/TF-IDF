import java.util.*;

/**
 * Created by Parag on 09-11-2014.
 */
public class Document {
    ArrayList<Integer> attrRealIndex;
    ArrayList<Integer> count;
    ArrayList<Double> termFrequency;
    int totalCount;
    PriorityQueue<Integer> minHeap;
    String docClass;
    String docEntry;
    ArrayList<AttributeReal> attrReal;

    public Document(ArrayList<Integer> attrRealIndex, ArrayList<Integer> count, ArrayList<Double> termFrequency,
                    int totalCount, PriorityQueue<Integer> minHeap, String docClass, ArrayList<AttributeReal> attrReal, String docEntry){
        this.attrRealIndex = attrRealIndex;
        this.count = count;
        this.termFrequency = termFrequency;
        this.totalCount = totalCount;
        this.minHeap = minHeap;
        this.docClass = docClass;
        this.attrReal = attrReal;
        this.docEntry = docEntry;
    }
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

    public String toTFString(){
        StringBuilder str = new StringBuilder("{");
        int i=0;
        for(int index: attrRealIndex){
            str.append(index+" "+termFrequency.get(i)+",");
            i++;
        }
        str.append(docEntry+" "+docClass+"}");
        return str.toString();
    }

    static Document parse(String line, ArrayList<AttributeReal> attrReal){
        ArrayList<Integer> attrRealIndex = new ArrayList<Integer>();
        final ArrayList<Integer> count = new ArrayList<Integer>();
        final ArrayList<Double> termFrequency = new ArrayList<Double>();
        int totalCount = 0;
        String docClass = "-1";
        String docEntry = "0";

        String[] entries = line.substring(1, line.length()-1).split(",");

        for(int i=0; i < entries.length-1; i++){
            String[] token = entries[i].split(" ");
            int index = Integer.parseInt(token[0]);
            attrRealIndex.add(index);
            attrReal.get(index).incrementDocs();
            int freq = Integer.parseInt(token[1]);
            count.add(freq);
            totalCount+=freq;
        }
        docEntry= entries[entries.length-1].split(" ")[0];
        docClass= entries[entries.length-1].split(" ")[1];
        PriorityQueue<Integer> minHeap = new PriorityQueue<Integer>(totalCount, new Comparator<Integer>(){
            @Override
            public int compare(Integer o1, Integer o2) {
                if(termFrequency.get(o1) > termFrequency.get(o2))
                    return 1;
                else if(termFrequency.get(o1) < termFrequency.get(o2))
                    return -1;
                return 0;
            }
        });

        for(int i=0; i<attrRealIndex.size(); i++){
            double termFreq = (double)count.get(i)/totalCount;
            termFrequency.add(termFreq);
            if(minHeap.size() <= 10) {
                minHeap.add(i);
            }else{
                int index = minHeap.peek();
                if(Double.compare(termFreq, termFrequency.get(index)) > 0){
                    minHeap.poll();
                    minHeap.add(i);
                }
            }
        }
        return new Document(attrRealIndex, count, termFrequency, totalCount, minHeap, docClass, attrReal, docEntry);
    }

}
