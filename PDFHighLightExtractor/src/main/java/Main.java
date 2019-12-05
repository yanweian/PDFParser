import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        String outFile="out.txt";
        String inFile=null;
        for(int i=0;i<args.length;i++){
            if("-i".equals(args[i])){
                if(++i>=args.length){
                    System.out.println("inFile is necessary.");
                    System.out.println("Usage: java -jar PDFHighLightExtractor.jar -i inFile | Directory [-o output.txt]");
                    return;
                }
                inFile=args[i];
            }else if("-o".equals(args[i])){
                if(++i>=args.length){
                    System.out.println("you add \"-o\", but not input outFile.");
                    System.out.println("Usage: java -jar PDFHighLightExtractor.jar -i inFile | Directory [-o output.txt]");

                    return;
                }
                outFile=args[i];
            }
        }
        if(inFile==null){
            System.out.println("inFile is necessary.");
            System.out.println("Usage: java -jar PDFHighLightExtractor.jar -i inFile | Directory [-o output.txt]");

            return;
        }
        FileWriter writer;
        try {
//            writer = new FileWriter("C:\\Users\\yan\\Desktop\\pdftool\\out100.txt");
            writer = new FileWriter(outFile);
            System.out.println("start process pdf.");
            List<File> files=new ArrayList<File>();
            File in =new File(inFile);
            if(in.isDirectory()){
                files= Arrays.asList(in.listFiles());
            }else{
                files.add(in);
            }
            for (File fin:files
                 ) {
                Extractor extractor = new Extractor(fin,true,false);
                List<String> strings=extractor.run();
                for (String s:strings
                ) {
                    //将-两个空格变成空，将两个空格变成一个空格
                    s=replaceSpecialStr(s,"\r\n"," ");
                    s=replaceSpecialStr(s,"-  ","");
                    s=replaceSpecialStr(s,"  "," ");
                    s=replaceSpecialStr(s,"- ","");
                    if(s.charAt(s.length()-2)=='.')
                        s=s+"\r\n";// 最后一个是句号，换行
                    System.out.println(s);
                    writer.write(s);
                    writer.flush();

                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Usage: java -jar PDFHighLightExtractor.jar -i inFile | Directory [-o output.txt]");

        }
    }
    public static String replaceSpecialStr(String str,String res,String replacement) {
        if(res==null) res="\r|\n";
        String repl = "";
        if (str!=null) {
            Pattern p = Pattern.compile(res);
            Matcher m = p.matcher(str);
            repl = m.replaceAll(replacement);
        }
        return repl;
    }
}
