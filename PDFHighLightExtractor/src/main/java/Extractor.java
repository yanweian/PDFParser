import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.PDFTextStripperByArea;


public class Extractor{

    private String paragraphEnd="$$";
    private File in;

    public Extractor(File in) {
        this.in = in;
    }

    // 获取所有文本
    public List<String> getFullText(){
        PDDocument pddDocument = null;
        List<String> stringList=new ArrayList<String>();
        try {
            pddDocument = PDDocument.load(in);
            PDFTextStripper pdfStripper = new PDFTextStripper();
            //设置段落
            pdfStripper.setParagraphEnd(paragraphEnd);
            String text = pdfStripper.getText(pddDocument);
            String strings[]=text.split("\r\n");
            stringList=new ArrayList<String>(text.length());
            Collections.addAll(stringList,strings);
        }catch (Exception e){
            System.out.println("run error!!!");
            System.out.println(in.getAbsolutePath());
        }
        return stringList;
    }

    public List<String> getHighLightedText() {
        PDDocument pddDocument = null;
        // 文本内容
        List<String> strings=new ArrayList<String>();
        try {
            pddDocument = PDDocument.load(in);
            List<PDPage> allPages = new ArrayList<PDPage>();
            pddDocument.getDocumentCatalog().getPages().getAllKids(allPages);

            for (PDPage page : allPages) {
                List<PDAnnotation> la = page.getAnnotations();
                for (PDAnnotation anot : la) {
                    if (anot instanceof PDAnnotationTextMarkup && anot.getSubtype().equals(PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT))
                        strings.addAll(processHighlight((PDAnnotationTextMarkup) anot, page));
                }
            }
            pddDocument.close();
        } catch (Exception ex) {
            System.out.println("run error!!!");
            System.out.println(in.getAbsolutePath());
        }
        return strings;
    }

    private List<String> processHighlight(PDAnnotationTextMarkup highlight, PDPage page) throws IOException {

        if ((highlight.getContents() != null && !highlight.getContents().isEmpty())
                && ((highlight.getSubtype().equals(PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT)))){
            System.out.println("error.");
            return null;
        }
        float[] quads = highlight.getQuadPoints();
        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        PDRectangle pagesize = page.findMediaBox();
        stripper.setSortByPosition(true);
        for (int i = 0; i < quads.length; i += 8) {
            Rectangle2D.Float rect = new Rectangle2D.Float(quads[i] - 1, pagesize.getHeight() - quads[i + 1] - 1,
                    quads[i + 6] - quads[i] + 1, quads[i + 1] - quads[i + 7] + 1);
            stripper.addRegion("" + i, rect);
        }
        stripper.extractRegions(page);
        List<String> lines = new ArrayList<String>();
        for (String region : stripper.getRegions()){
            String s=stripper.getTextForRegion(region);
            lines.add(s);
        }
        return lines;
    }

}

