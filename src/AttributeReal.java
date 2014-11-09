/**
 * Created by Parag on 09-11-2014.
 */
public class AttributeReal {
    String pre = "@attribute";
    String attr;

    AttributeReal(String attr){
        this.attr = attr;
    }

    public String toString(){
        return pre+" T"+attr+" real";
    }
}
