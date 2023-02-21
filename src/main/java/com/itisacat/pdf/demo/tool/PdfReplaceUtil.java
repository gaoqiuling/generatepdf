package com.itisacat.pdf.demo.tool;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.kernel.pdf.canvas.parser.listener.IPdfTextLocation;
import com.itextpdf.kernel.pdf.canvas.parser.listener.RegexBasedLocationExtractionStrategy;
import com.itisacat.pdf.demo.LocationComparator;
import com.itisacat.pdf.demo.model.OverAreaDTO;

import java.io.IOException;
import java.util.*;

public class PdfReplaceUtil {

    public static List<OverAreaDTO> overText(PdfDocument pdfDocument, String regex, Map<String, String> replaceMap, PdfFont font) throws IOException {
        List<OverAreaDTO> list = new LinkedList<>();
        for (int i = 1; i <= pdfDocument.getNumberOfPages(); i++) {
            PdfPage page = pdfDocument.getPage(i);
            //按正则表达式匹配
            RegexBasedLocationExtractionStrategy strategy = new RegexBasedLocationExtractionStrategy(regex);
            PdfCanvasProcessor canvasProcessor = new PdfCanvasProcessor(strategy);
            canvasProcessor.processPageContent(page);
            //在这里返回的结果是排序
            Collection<IPdfTextLocation> resultantLocations = strategy.getResultantLocations();
            List<IPdfTextLocation> locationList = new ArrayList<>();
            for (IPdfTextLocation location : resultantLocations) {
                locationList.add(location);
            }
            //Y轴降序,X轴升序
            Collections.sort(locationList, new LocationComparator());
            //获取pdf画布
            PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamAfter(), pdfDocument.getPage(i).getResources(), pdfDocument);
            //遍历匹配到的每个位置
            for (IPdfTextLocation location : locationList) {
                String key = location.getText();
                if (!replaceMap.containsKey(key)) {
                    throw new RuntimeException(key + ":没有提供填充内容");
                }
                //原有的区域用白色背景填充
                Rectangle rectangle = location.getRectangle();
                pdfCanvas.saveState();
                pdfCanvas.setFillColor(ColorConstants.WHITE);
                pdfCanvas.rectangle(rectangle);
                pdfCanvas.fill();
                pdfCanvas.restoreState();
                //填充新内容
                pdfCanvas.beginText();
                pdfCanvas.setFontAndSize(font, rectangle.getHeight());
                pdfCanvas.setTextMatrix(rectangle.getX(), rectangle.getY() + 1.5f);
                pdfCanvas.newlineShowText(replaceMap.get(key));
                pdfCanvas.endText();
                OverAreaDTO overAreaDTO = new OverAreaDTO();
                overAreaDTO.setPageNum(i);
                overAreaDTO.setX(rectangle.getX());
                overAreaDTO.setY(rectangle.getY());
                overAreaDTO.setWidth(rectangle.getWidth());
                overAreaDTO.setHeight(rectangle.getHeight());
                overAreaDTO.setKey(key);
                overAreaDTO.setValue(replaceMap.get(key));
                list.add(overAreaDTO);
            }
            pdfCanvas.release();
        }
        pdfDocument.close();
        return list;
    }
}
