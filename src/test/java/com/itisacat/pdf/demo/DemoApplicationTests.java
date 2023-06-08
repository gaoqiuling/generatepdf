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

    //生成pdf后替换盖章的位置
    @Test
    public void html2PdfWithSign() throws IOException {
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

    //生成pdf后替换盖章的位置，利用doc功能可以支持强制分页，可以支持段落必须在一页中
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


    //html转pdf,包含图片、checkbox,radio
    @Test
    public void testHtml2Pdf() {
        try {
            FileOutputStream accessOut = new FileOutputStream("output.pdf");
            PdfWriter accessWriter = new PdfWriter(accessOut);
            PdfDocument document = new PdfDocument(accessWriter);
            //设置纸张大小,A4
            FontProvider fontProvider = new FontProvider();
            fontProvider.addFont(System.getProperty("user.dir") + "\\font\\simfang.ttf");
            ConverterProperties properties = new ConverterProperties();
            properties.setFontProvider(fontProvider);
            properties.setCharset("utf-8");
            HtmlConverter.convertToPdf(getTestHtml(), document, properties);
            System.out.println("PDF generated successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getTestHtml() {
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
        return freeMarkerTool.process("test", map);
    }

//region 使用Jsoup解析html元素
//    @Test
//    public void JsoupTest() {
//        String html = "<html><body>"
//                + "<h1>Hello, World!</h1>"
//                + "<img src='https://cc.hjfile.cn/cc/img/20230519/2023051904120936475935.jpg' style='width:400px;height:400px;'>" +
//                "<input type=\"checkbox\" name=\"option\" value=\"1\" checked /> Option 1" +
//                "<input type=\"checkbox\" name=\"option\" value=\"2\" /> Option 2" +
//                "<input type=\"radio\" name=\"radio\" value=\"2\" checked/> first" +
//                "<input type=\"radio\" name=\"radio\" value=\"3\" /> second" +
//                "</body></html>";
//
//        try {
//            org.jsoup.nodes.Document document = Jsoup.parse(html);
//            com.itextpdf.text.Document pdfDocument = new com.itextpdf.text.Document();
//            PdfWriter writer = PdfWriter.getInstance(pdfDocument, new FileOutputStream("output.pdf"));
//            pdfDocument.open();
//            // Process each element in the HTML document
//            for (Element element : document.body().children()) {
//                processElement(pdfDocument, writer, element);
//            }
//            pdfDocument.close();
//            System.out.println("PDF generated successfully.");
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (DocumentException e) {
//            throw new RuntimeException(e);
//        }
//
//    }
//
//    private static void processElement(com.itextpdf.text.Document pdfDocument, PdfWriter writer, Element element) throws com.itextpdf.text.DocumentException, IOException {
//        switch (element.tagName()) {
//            case "h1":
//                pdfDocument.add(new com.itextpdf.text.Paragraph(element.text()));
//                break;
//            case "img":
//                String imageUrl = element.attr("src");
//                com.itextpdf.text.Image image = com.itextpdf.text.Image.getInstance(imageUrl);
//                pdfDocument.add(image);
//                break;
//            case "input":
//                if (element.attr("type").equals("checkbox")) {
//                    boolean checked = element.hasAttr("checked");
//                    String value = element.val();
//                    createCheckboxField(writer, value, checked);
//                }
//                break;
//            default:
//                // Handle other element types if needed
//                break;
//        }
//    }
    //endregion
}
