/**
 * Created by Parag on 09-11-2014.
 */
public class AttributeReal {
    String pre = "@attribute";
    String post;
    String attr;
    String index;
    int docs;
    double idf;

    AttributeReal(String attr, String index, String post){
        this.attr = attr;
        this.index = index;
        docs = 0;
        this.post = post;
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
        return pre+" T"+attr+" "+post;
    }
}
