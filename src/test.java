import java.util.ArrayList;

/**
 * Created by Parag on 09-11-2014.
 */
public class test {
    static String pre = "@attribute class";
    static ArrayList<String> labels = new ArrayList<String>(1);
    static String sdf = "c:\\asdf";
    public static void main(String[] args) {
        Double a = 2.0003;
        Double b = 4.00031;
        System.out.println(a<b);
        System.out.println(Double.compare(a, b));
        /*labels.add("fasdf");
        labels.add("fdste");
        labels.add("fds3");
        System.out.println(sdf.split("\\\\")[0]);
        String post = "{";
        for(String label:labels)
            post+=label+",";
        post = post.substring(0,post.length()-1);
        post+="}";
        System.out.print( pre+" "+post);*/
    }
}
