package com.itisacat.pdf.demo.tool;


import com.itisacat.pdf.demo.model.AddContractTableBo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class DocReplaceUtil {

    /**
     * @param xwpfDocument word文档
     * @param params       需要替换内容 站位符-新的文本
     * @param skipKey      跳过的key
     */
    public static void replacePlaceholder(XWPFDocument xwpfDocument, Map<String, ?> params, Set<String> skipKey) {
        if (params == null || params.size() == 0) {
            return;
        }
        replaceInPara(xwpfDocument, params, skipKey);
        replaceInHeader(xwpfDocument, params, skipKey);
    }

    private static void replaceInHeader(XWPFDocument xwpfDocument, Map<String, ?> params, Set<String> skipKey) {
        List<XWPFHeader> headerList = xwpfDocument.getHeaderList();
        for (XWPFHeader xwpfHeader : headerList) {
            List<XWPFParagraph> paragraphs = xwpfHeader.getParagraphs();
            for (XWPFParagraph para : paragraphs) {
                replaceInPara(para, params, skipKey);
            }
        }
    }

    /**
     * 替换段落里面的变量
     *
     * @param doc    要替换的文档
     * @param params 参数
     */
    private static void replaceInPara(XWPFDocument doc, Map<String, ?> params, Set<String> skipKey) {
        for (XWPFParagraph para : doc.getParagraphs()) {
            replaceInPara(para, params, skipKey);
        }
    }


    /**
     * 替换段落里面的变量
     *
     * @param para   要替换的段落
     * @param params 参数
     */
    private static void replaceInPara(XWPFParagraph para, Map<String, ?> params, Set<String> skipKey) {
        List<XWPFRun> runs = null;
        Matcher matcher = null;
        if (matcher(para.getParagraphText()).find()) {
            // 修正Run分割不准确的问题,确保${站位符}在一个Run里面
            runs = replaceText(para);
            for (int i = 0; i < runs.size(); i++) {
                XWPFRun run = runs.get(i);
                String runText = run.toString();
                System.out.println(runText);
                //跳过不需要匹配的key
                if (CollectionUtils.isNotEmpty(skipKey) && skipKey.contains(runText.trim())) {
                    continue;
                }
                matcher = matcher(runText);
                if (matcher.find()) {
                    while ((matcher = matcher(runText)).find()) {
                        String key = matcher.group(1);
                        Object obj = params.get(key);
                      /*  if(obj==null){
                            throw new ServiceException("没有获取到"+key+"对应的值");
                        }*/
                        String replaceStr = "/";
                        if (obj == null || StringUtils.isBlank(String.valueOf(obj))) {
                            replaceStr = "/";
                        } else {
                            replaceStr = String.valueOf(obj);
                        }
                        runText = matcher.replaceFirst(replaceStr);
                    }
                    // 直接调用XWPFRun的setText()方法设置文本时，在底层会重新创建一个XWPFRun，把文本附加在当前文本后面，
                    // 所以我们不能直接设值，需要先删除当前run,然后再自己手动插入一个新的run。
                    //记录每个run的字体,字体大小,是否加粗,是否有下划线
                    run.setText(runText, 0);
                }
            }
        }
    }


    /**
     * 修正Run分割不准确的问题,确保${站位符}在一个Run里面
     *
     * @param para 要替换的段落
     * @return
     */
    private static List<XWPFRun> replaceText(XWPFParagraph para) {
        List<XWPFRun> runs = para.getRuns();
        String str = "";
        boolean flag = false;
        for (int i = 0; i < runs.size(); i++) {
            XWPFRun run = runs.get(i);
            String runText = run.toString();
            if (flag ||  (StringUtils.isNotBlank(runText) && runText.startsWith("${") && (!runText.contains("}")) )) {
                str = str + runText;
                flag = true;
                if (StringUtils.isNotBlank(runText) && runText.endsWith("}")) {
                    flag = false;
                    run.setText(str, 0);
                    str = "";
                } else {
                    para.removeRun(i);
                }
                i--;
            }
        }
        return runs;
    }


    /**
     * 正则匹配字符串
     *
     * @param str
     * @return
     */
    private static Matcher matcher(String str) {
        Pattern pattern = Pattern.compile("\\$\\{(.+?)\\}", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        return matcher;
    }

    /**
     * des:表末尾添加行(表，要复制样式的行，添加行数，插入的行下标索引)
     *
     * @param table
     * @param source
     * @param rows
     */
    public static void addRows(XWPFTable table, int source, int rows, int insertRowIndex) {

        //获取表格的总行数
        int index = table.getNumberOfRows();
        //循环添加行和和单元格
        for (int i = 1; i <= rows; i++) {
            //获取要复制样式的行
            XWPFTableRow sourceRow = table.getRow(source);
            //添加新行
            XWPFTableRow targetRow = table.insertNewTableRow(insertRowIndex++);
            //复制行的样式给新行
            targetRow.getCtRow().setTrPr(sourceRow.getCtRow().getTrPr());
            //获取要复制样式的行的单元格
            List<XWPFTableCell> sourceCells = sourceRow.getTableCells();
            //循环复制单元格
            for (XWPFTableCell sourceCell : sourceCells) {
                //添加新列
                XWPFTableCell newCell = targetRow.addNewTableCell();
                //复制单元格的样式给新单元格
                newCell.getCTTc().setTcPr(sourceCell.getCTTc().getTcPr());
                //设置垂直居中
                newCell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);//垂直居中
                //复制单元格的居中方式给新单元格
                CTPPr pPr = sourceCell.getCTTc().getPList().get(0).getPPr();
                if (pPr != null && pPr.getJc() != null && pPr.getJc().getVal() != null) {
                    CTTc cttc = newCell.getCTTc();
                    CTP ctp = cttc.getPList().get(0);
                    CTPPr ctppr = ctp.getPPr();
                    if (ctppr == null) {
                        ctppr = ctp.addNewPPr();
                    }
                    CTJc ctjc = ctppr.getJc();
                    if (ctjc == null) {
                        ctjc = ctppr.addNewJc();
                    }
                    ctjc.setVal(pPr.getJc().getVal()); //水平居中
                }

                //得到复制单元格的段落
                List<XWPFParagraph> sourceParagraphs = sourceCell.getParagraphs();
                if (StringUtils.isEmpty(sourceCell.getText())) {
                    continue;
                }
                //拿到第一段
                XWPFParagraph sourceParagraph = sourceParagraphs.get(0);
                //得到新单元格的段落
                List<XWPFParagraph> targetParagraphs = newCell.getParagraphs();
                //判断新单元格是否为空
                if (StringUtils.isEmpty(newCell.getText())) {
                    //添加新的段落
                    XWPFParagraph ph = newCell.addParagraph();
                    //复制段落样式给新段落
                    ph.getCTP().setPPr(sourceParagraph.getCTP().getPPr());
                    //得到文本对象
                    XWPFRun run = ph.getRuns().isEmpty() ? ph.createRun() : ph.getRuns().get(0);
                    //复制文本样式
                    run.setFontFamily(sourceParagraph.getRuns().get(0).getFontFamily());
                } else {
                    XWPFParagraph ph = targetParagraphs.get(0);
                    ph.getCTP().setPPr(sourceParagraph.getCTP().getPPr());
                    XWPFRun run = ph.getRuns().isEmpty() ? ph.createRun() : ph.getRuns().get(0);
                    run.setFontFamily(sourceParagraph.getRuns().get(0).getFontFamily());
                }
            }
        }

    }

    public static void fillTables(XWPFDocument xwpfDocument, List<AddContractTableBo> tableBoList) {
        if (CollectionUtils.isEmpty(tableBoList)) {
            return;
        }
        List<XWPFTable> tables = xwpfDocument.getTables();
        if (CollectionUtils.isEmpty(tables)) {
            return;
        }
        int count = Math.min(tableBoList.size(), tables.size());
        for (int i = 0; i < count; i++) {
            if (tableBoList.get(i).getRowNumber() == 0) {
                continue;
            }
            XWPFTable xwpfTable = tables.get(i);
            int oldSize = xwpfTable.getNumberOfRows();
            // 补足行数
            AddContractTableBo tableBo = tableBoList.get(i);
            if (tableBo.getRowNumber() > oldSize - 1) {
                addRows(xwpfTable, oldSize - 1, tableBo.getRowNumber() - oldSize + 1, oldSize - 1);
            }
            for (int rowIndex = 0; rowIndex < tableBo.getRowNumber(); rowIndex++) {
                XWPFTableRow row = xwpfTable.getRow(rowIndex + 1);
                List<String> columnValues = tableBo.getData().get(rowIndex);
                for (int columnIndex = 0; columnIndex < tableBo.getColumnNumber(); columnIndex++) {
                    row.getCell(columnIndex).setText(columnValues.get(columnIndex));
                }
            }
        }
    }
}
