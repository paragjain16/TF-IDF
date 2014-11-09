import java.util.ArrayList;

/**
 * Created by Parag on 09-11-2014.
 */
public class AttributeClass {

    String pre = "@attribute class";
    ArrayList<String> labels = new ArrayList<String>();

    AttributeClass(ArrayList<String> labels){
        this.labels = labels;
    }

    void add(String label){
        labels.add(label);
    }
    public String toString(){
        String post = "{";
        for(String label:labels)
            post+=label+",";
        post = post.substring(0,post.length()-1);
        post+="}";
        return pre+" "+post;
    }
}
