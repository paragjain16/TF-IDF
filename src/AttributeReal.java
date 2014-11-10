/**
 * Created by Parag on 09-11-2014.
 */
public class AttributeReal {
    String pre = "@attribute";
    String attr;
    String index;
    int docs;
    double idf;

    AttributeReal(String attr, String index){
        this.attr = attr;
        this.index = index;
        docs = 0;
    }
    void setDocs(int docs){
        this.docs = docs;
    }
    int getDocs(){
       return docs;
    }
    void incrementDocs(){
        docs++;
    }
    public String toString(){
        return pre+" T"+attr+" real";
    }
}
