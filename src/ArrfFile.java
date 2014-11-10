import org.w3c.dom.Attr;

import javax.print.Doc;
import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by Parag on 09-11-2014.
 */
public class ArrfFile {
    ArrayList<AttributeReal> attrReal;
    AttributeClass attrClass;
    ArrayList<String> attrClassList;
    ArrayList<Document> docs;
    //Mapping from word to index in Attribute Real ArrayList
    //HashMap<String, Integer> mapping;
    //Mapping from class to line number
    //HashMap<String, Integer> classMapping;
    int totalDocs;
    String relation;
    String fileName;

    ArrfFile(ArrayList<AttributeReal> attrReal, AttributeClass attrClass, ArrayList<String> attrClassList,
             ArrayList<Document> docs, int totalDocs, String fileName, String relation){
        this.attrReal = attrReal;
        this.attrClass = attrClass;
        this.attrClassList = attrClassList;
        this.docs = docs;
        this.totalDocs = totalDocs;
        this.fileName = fileName;
        this.relation = relation;
    }

    static ArrfFile readFile(String fileName){
        ArrayList<AttributeReal> attrReal = new ArrayList<AttributeReal>();
        ArrayList<String> attrClassList = new ArrayList<String>();
        AttributeClass attrClass = null;
        ArrayList<Document> docs = new ArrayList<Document>();
        //Mapping from word to index in Attribute Real ArrayList
        //HashMap<String, Integer> mapping = new HashMap<String, Integer>();
        //Mapping from class to line number
        //HashMap<String, Integer> classMapping = new HashMap<String, Integer>();
        int totalDocs = 0;
        String relation = "";
        File f = new File(fileName);
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;
            int i = 0;
            relation = br.readLine();
            while((line = br.readLine())!=null){
                if(line.startsWith("@attribute T")){
                    String[] tokens = line.split(" ");
                    String attr = tokens[1].substring(1);
                    attrReal.add(new AttributeReal(attr, i+""));
                    i++;
                }else if(line.startsWith("{")){
                    Document doc = Document.parse(line, attrReal);
                    docs.add(doc);
                    totalDocs++;
                }else if(line.startsWith("@attribute class")){
                    String[] tokens = line.split(" ");
                    String[] classes = tokens[2].substring(1, tokens[2].length()-1).split(",");
                    Collections.addAll(attrClassList, classes);
                    attrClass = new AttributeClass(attrClassList);
                }
            }
            for(AttributeReal attributeReal: attrReal){
                attributeReal.idf = Math.log((double)totalDocs/(1+attributeReal.docs));
            }
            br.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ArrfFile(attrReal, attrClass, attrClassList, docs, totalDocs, fileName, relation);
    }
    static void writeTFfile(ArrfFile arrfFile){
        String file = arrfFile.fileName;
        if(file.split("/").length > 0){
            String[] tokens = file.split("/");
            file = tokens[tokens.length-1];
        }else if(file.split("\\\\").length>0){
            String[] tokens = file.split("\\\\");
            file = tokens[tokens.length-1];
        }
        File f = new File("pjain11."+file.split("\\.")[0]+".tf1.arff");
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
                pw.println(document.toTFString());
            }
            pw.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ArrfFile arrfFile = ArrfFile.readFile(args[0]);
        ArrfFile.writeTFfile(arrfFile);
        System.out.println(arrfFile.docs.get(0).getTop10()+arrfFile.docs.get(0).docEntry+" "+arrfFile.docs.get(0).docClass);
    }
}
