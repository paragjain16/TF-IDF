import java.util.ArrayList;

/**
 * Created by Parag on 09-11-2014.
 */
public class test {
    static String pre = "@attribute class";
    static ArrayList<String> labels = new ArrayList<String>(1);

    public static void main(String[] args) {
        labels.add("fasdf");
        labels.add("fdste");
        labels.add("fds3");

        String post = "{";
        for(String label:labels)
            post+=label+",";
        post = post.substring(0,post.length()-1);
        post+="}";
        System.out.print( pre+" "+post);
    }
}
