import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static String outFileSuffix=".txt";
    private static String output="output";
    private static String dir="C:\\Users\\yan\\Desktop\\PDFParser\\example\\";
    private static String inFile=dir+"200\\10.1007_s002030050495.pdf";
    private static String outFileAll;
    private static String outFileHighlighted;
    private static String outFileWithoutHighLighted;
    private static FileWriter writerAll;
    private static FileWriter writerHighlighted;
    private static FileWriter writerWithoutHighLighted;

    public static void main(String[] args) {
        if(!entry(args)) return;
        try {
            initWriter();
            System.out.println("Start process pdf.");
            List<File> fileList=getFileList(inFile);
            for (File fin:fileList) {
                System.out.println("processing "+fin.getAbsolutePath()+"...");
                Extractor extractor = new Extractor(fin);
                List<String> highlightedList=extractor.getHighLightedText();
                processHighlightedList(highlightedList);
            }
            closeWriter();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Usage: java -jar PDFHighLightExtractor.jar -i inFile | Directory [-o output.txt]");
        }
    }

    private static void processHighlightedList(List<String> highlightedList){
        // 希腊字母首尾
        char xilastart='α',xilaend='ω';
        Queue<String> queue = new LinkedList<String>();
        queue.add("");
        List<String> newHighlightedList=new ArrayList<String>();
        for (String s: highlightedList) {
            if(queue.peek().equals(""))
            if(s.length()==1&&s.charAt(0)>=xilastart&&s.charAt(0)<=xilaend){
                queue.add(s);
            }
            System.out.print(s);
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

    public static String processFullText(String s) {
        // 将-两个空格变成空，将两个空格变成一个空格
        s=replaceSpecialStr(s,"\r\n"," ");
        s=replaceSpecialStr(s,"-  ","");
        s=replaceSpecialStr(s,"  "," ");
        s=replaceSpecialStr(s,"- ","");
        // 处理开头的段落符
        s=s.substring(s.indexOf("\t")+1);
        // 处理作者与摘要之间的换行
        s=s.replace(" \tAbstract","\r\nAbstract");
        // 处理摘要
        int index_of_abstract=s.indexOf("Abstract");
        int index_of_end=s.indexOf(". \t",index_of_abstract);
        if(index_of_abstract>=0&&index_of_end>=0){
            String abstracts=s.substring(index_of_abstract,index_of_end);
            String new_abstracts=replaceSpecialStr(abstracts,"\t","");
            s= s.replace(abstracts,new_abstracts);
        }
        // 处理换行
        s=s.replace(". \t",".\r\n");
        // 处理tab
        s=s.replace(" \t"," ");
        s=s.replace("\t","");
        return s;
    }

    public static String processHighLightedText(String s) {
        // 将-两个空格变成空，将两个空格变成一个空格
        s=replaceSpecialStr(s,"\r\n"," ");
        s=replaceSpecialStr(s,"-  ","");
        s=replaceSpecialStr(s,"  "," ");
        s=replaceSpecialStr(s,"- ","");
        if(s.charAt(s.length()-1)=='.'||s.charAt(s.length()-2)=='.')
            s=s+"\r\n";// 最后一个是句号，换行
        s=s.replace(". \r\n",".\r\n");
        return s;
    }

    private static boolean entry(String[] args){
        for(int i=0;i<args.length;i++){
            if("-i".equals(args[i])){
                if(++i>=args.length){
                    System.out.println("inFile is necessary.");
                    System.out.println("Usage: java -jar PDFHighLightExtractor.jar -i inFile | Directory [-o output]");
                    return false;
                }
                inFile=args[i];
            }else if("-o".equals(args[i])){
                if(++i>=args.length){
                    System.out.println("you add \"-o\", but do not type in outFile.");
                    System.out.println("Usage: java -jar PDFHighLightExtractor.jar -i inFile | Directory [-o output]");
                    return false;
                }
                output=args[i];
            }
        }
        if(inFile==null){
            System.out.println("inFile is necessary.");
            System.out.println("Usage: java -jar PDFHighLightExtractor.jar -i inFile | Directory [-o output.txt]");
            return false;
        }
        assembleOutFileString(output);
        return true;
    }

    private static void assembleOutFileString(String output){
        outFileAll=dir+output+"_all"+outFileSuffix;
        outFileHighlighted=dir+output+"_highlighted"+outFileSuffix;
        outFileWithoutHighLighted=dir+output+"_without_highlighted"+outFileSuffix;
    }

    private static void initWriter() throws IOException {
        writerAll=new FileWriter(outFileAll);
        writerHighlighted=new FileWriter(outFileHighlighted);
        writerWithoutHighLighted=new FileWriter(outFileWithoutHighLighted);
    }
    private static void closeWriter() throws IOException {
        writerAll.close();
        writerHighlighted.close();
        writerWithoutHighLighted.close();
    }
    private static List<File> getFileList(String inFile){
        List<File> fileList=new ArrayList<File>();
        File in =new File(inFile);
        if(in.isDirectory()){
            fileList= Arrays.asList(in.listFiles());
        }else{
            fileList.add(in);
        }
        return fileList;
    }
}
