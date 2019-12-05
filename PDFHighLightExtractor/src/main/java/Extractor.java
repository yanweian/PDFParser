
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup;
import org.apache.pdfbox.util.PDFTextStripperByArea;


public class Extractor{


    private File in;
    private boolean stopped;
    private boolean includeHighlights;
    private boolean includeUnderlines;
    private int start;
    private int finish;

    public static final int FINISH = -1;

    public Extractor(File in, boolean includeHighlights, boolean includeUnderlines) {
        this.in = in;
        this.includeHighlights = includeHighlights;
        this.includeUnderlines = includeUnderlines;
    }

    public List<String> run() {
        PDDocument pddDocument = null;
        stopped = false;
        List<String> strings=new ArrayList<String>();
        try {
            pddDocument = PDDocument.load(in);
            List<PDPage> allPages = new ArrayList<PDPage>();
            pddDocument.getDocumentCatalog().getPages().getAllKids(allPages);

            finish = allPages.size();
            start=1;
            if (start <= 0 || allPages.size() < (finish - 1)) {
                System.out.println("page error.");
                return null;
            }
            List<PDPage> selectedPages = allPages.subList(start - 1, finish - 1);

            int nPages = selectedPages.size();
            int i = 1;
            for (PDPage page : selectedPages) {
                if (stopped)
                    break;
                List<PDAnnotation> la = page.getAnnotations();
                for (PDAnnotation anot : la) {
                    if (stopped)
                        break;
                    if (anot instanceof PDAnnotationTextMarkup && ((includeHighlights
                            && anot.getSubtype().equals(PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT))
                            || (includeUnderlines
                            && anot.getSubtype().equals(PDAnnotationTextMarkup.SUB_TYPE_UNDERLINE))))
                        strings.add(processHighlight((PDAnnotationTextMarkup) anot, page));
                }
            }
            pddDocument.close();
        } catch (Exception ex) {
            System.out.println("run error!!!");
            System.out.println(in.getAbsolutePath());
        }
        return strings;
    }

    private String processHighlight(PDAnnotationTextMarkup highlight, PDPage page) throws IOException {

        if ((highlight.getContents() != null && !highlight.getContents().isEmpty())
                && ((highlight.getSubtype().equals(PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT))
                || (highlight.getSubtype().equals(PDAnnotationTextMarkup.SUB_TYPE_UNDERLINE))))
            return null;
        float[] quads = highlight.getQuadPoints();
        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        PDRectangle pagesize = page.findMediaBox();
        for (int i = 0; i < quads.length; i += 8) {
            stripper.setSortByPosition(true);
            Rectangle2D.Float rect = new Rectangle2D.Float(quads[i] - 1, pagesize.getHeight() - quads[i + 1] - 1,
                    quads[i + 6] - quads[i] + 1, quads[i + 1] - quads[i + 7] + 1);
            stripper.addRegion("" + i, rect);

        }
        stripper.extractRegions(page);
        List<String> lines = new LinkedList<String>();
        for (String region : stripper.getRegions())
            lines.add(stripper.getTextForRegion(region));

        // Format text and set it as comment of the annotation
        String highlightText = "";
        for (String line : lines)
            highlightText = highlightText + line;
        highlight.setContents(highlightText);
        return highlightText;
    }

}

