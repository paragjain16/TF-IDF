import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

/**
 * Created by Parag on 09-11-2014.
 */
public class Document {
    ArrayList<Integer> attrRealIndex;
    ArrayList<Integer> count;
    ArrayList<Float> termFrequency;
    int totalCount;
    PriorityQueue<Integer> minHeap;
    String docClass;

    public Document(ArrayList<Integer> attrRealIndex, ArrayList<Integer> count, ArrayList<Float> termFrequency,
                    int totalCount, PriorityQueue<Integer> minHeap, String docClass){
        this.attrRealIndex = attrRealIndex;
        this.count = count;
        this.termFrequency = termFrequency;
        this.totalCount = totalCount;
        this.minHeap = minHeap;
        this.docClass = docClass;
    }

    public String toString(){

    }

    static Document parse(String line, HashMap<String, Integer> mapping){
        ArrayList<Integer> attrRealIndex = new ArrayList<Integer>();
        final ArrayList<Integer> count = new ArrayList<Integer>();
        final ArrayList<Float> termFrequency = new ArrayList<Float>();
        int totalCount = 0;
        String docClass = "-1";

        String[] entries = line.substring(1, line.length()-1).split(",");

        for(int i=0; i < entries.length-1; i++){
            String[] token = entries[i].split(" ");
            attrRealIndex.add(mapping.get(token[0]));
            int freq = Integer.parseInt(token[1]);
            count.add(freq);
            totalCount+=freq;
        }
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
            termFrequency.add((float)count.get(i)/totalCount);
            minHeap.add(i);
        }
        return new Document(attrRealIndex, count, termFrequency, totalCount, minHeap, docClass);
    }

}
