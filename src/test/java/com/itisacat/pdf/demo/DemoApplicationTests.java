package com.itisacat.pdf.demo;

import com.alibaba.fastjson.JSONObject;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.IBlockElement;
import com.itextpdf.layout.font.FontProvider;
import com.google.common.collect.ImmutableMap;
import com.itisacat.pdf.demo.model.AddContractTableBo;
import com.itisacat.pdf.demo.model.OverAreaDTO;
import com.itisacat.pdf.demo.tool.DocReplaceUtil;
import com.itisacat.pdf.demo.tool.DocToPdfUtil;
import com.itisacat.pdf.demo.tool.FreeMarkerTool;
import com.itisacat.pdf.demo.tool.PdfReplaceUtil;
import org.apache.commons.compress.utils.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import com.aspose.words.License;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class DemoApplicationTests {
    @Autowired
    private FreeMarkerTool freeMarkerTool;

    @Test
    public void html2Pdf() throws IOException {
        //1-获取html
        String html = getHtml();
        //2-生成pdf
        ByteArrayOutputStream o = genPdf(html);
        //3-找到盖章的位置并替换
        Map<String, String> fillValues = ImmutableMap.of("${seal}", "", "${sealDate}", "");
        savePdf(o, fillValues);
    }

    private String getHtml() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("number", 1);
        jsonObject.put("name", "test1");
        jsonObject.put("contractNo", "HSF123");
        jsonObject.put("servicePrice", "100.00");
        jsonObject.put("financePrice", "50.00");
        jsonObject.put("payPrice", "2000.00");
        jsonObject.put("dueDate", "2023-02-21");
        Map<String, Object> map = new HashMap<>();
        map.put("contractNo", 111111);
        map.put("list", Arrays.asList(jsonObject));
        map.put("seal", "${seal}");
        map.put("sealDate", "${sealDate}");
        return freeMarkerTool.process("confirm", map);
    }

    private ByteArrayOutputStream genPdf(String html) throws IOException {
        ByteArrayOutputStream accessOut = new ByteArrayOutputStream();
        PdfWriter accessWriter = new PdfWriter(accessOut);
        PdfDocument accessPdf = new PdfDocument(accessWriter);
        //字体
        FontProvider fontProvider = new FontProvider();
        fontProvider.addFont(System.getProperty("user.dir") + "\\font\\simfang.ttf");
        ConverterProperties properties = new ConverterProperties();
        properties.setFontProvider(fontProvider);
        properties.setCharset("utf-8");

        //设置纸张大小,A4
        Document document = new Document(accessPdf, PageSize.A4.rotate());
        document.setMargins(20, 20, 20, 20);
        try {
            HtmlConverter.convertToElements(html, properties).stream().forEach(t -> {
                document.add((IBlockElement) t);
            });
        } finally {
            document.close();
        }
        return accessOut;
    }

    private void savePdf(ByteArrayOutputStream o, Map<String, String> fillValues) throws IOException {
        ByteArrayOutputStream outputStreamPdfNew = new ByteArrayOutputStream(1024 * 1024);
        PdfDocument pdfDoc = new PdfDocument(new PdfReader(new ByteArrayInputStream(o.toByteArray())), new PdfWriter(outputStreamPdfNew));
        List<OverAreaDTO> areaList = PdfReplaceUtil.overText(pdfDoc, "\\$\\{(.+?)\\}", fillValues, PdfFontFactory.createFont(StandardFonts.COURIER));
        System.out.println(areaList);
        outputStreamPdfNew.writeTo(new FileOutputStream(new File("demo.pdf")));
        outputStreamPdfNew.flush();
        outputStreamPdfNew.close();
    }

    @Test
    public void doc2Pdf() throws IOException {
        Map<String, String> signFillValues = ImmutableMap.of("${seal}", "", "${sealDate}", "");
        Map<String, String> fillValues = ImmutableMap.of("agreementNo", "11111");
        AddContractTableBo table = AddContractTableBo.builder().rowNumber(1).columnNumber(5).data(Lists.newArrayList()).build();
        table.getData().add(Arrays.asList("1", "test", "test test test", "test2", "13500010001"));
        ByteArrayOutputStream pdfOutput = new ByteArrayOutputStream(1024 * 1024);
        try {
            //2-doc转pdf
            XWPFDocument xwpfDocument = new XWPFDocument(new FileInputStream(new File("demo.docx")));
            DocReplaceUtil.replacePlaceholder(xwpfDocument, fillValues, signFillValues.keySet());
            DocReplaceUtil.fillTables(xwpfDocument, Arrays.asList(table));
            //2-转换为PDF
            xwpfDocument.write(pdfOutput);
            try (InputStream is = DocReplaceUtil.class.getClassLoader().getResourceAsStream("License.xml")) {
                License license = new License();
                license.setLicense(is);
            } catch (Exception e) {
                throw new RuntimeException("初始化aspose license异常");
            }
            ByteArrayOutputStream outPdf = DocToPdfUtil.docToPdf(new ByteArrayInputStream(pdfOutput.toByteArray()));
            //2-找到盖章的位置并替换
            savePdf(outPdf, signFillValues);
        } catch (Exception e) {
            throw new RuntimeException("生成合同文件异常");
        }

    }
}
