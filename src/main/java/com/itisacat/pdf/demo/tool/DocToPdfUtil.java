package com.itisacat.pdf.demo.tool;

import com.aspose.words.Document;
import com.aspose.words.SaveFormat;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Slf4j
public class DocToPdfUtil {
    public static ByteArrayOutputStream docToPdf(InputStream inputStream) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024 * 1024);
            Document doc = new Document(inputStream);
            doc.save(outputStream, SaveFormat.PDF);
            return outputStream;
        } catch (Exception e) {
            log.error("合同文件转换为pdf异常", e);
            throw new RuntimeException("合同文件转换为pdf异常");
        }
    }
}
