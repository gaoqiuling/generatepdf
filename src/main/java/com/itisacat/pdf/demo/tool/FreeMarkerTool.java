package com.itisacat.pdf.demo.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.StringWriter;
import java.util.Map;

import freemarker.template.Configuration;

@Component
public class FreeMarkerTool {
    private Configuration configuration;

    @PostConstruct
    private void init() {
        configuration = new Configuration(Configuration.VERSION_2_3_31);
        configuration.setClassLoaderForTemplateLoading(FreeMarkerTool.class.getClassLoader(), "ftl/");
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);
        configuration.setNumberFormat("####0");
    }

    public String process(String template, Map<String, Object> model) {
        try {
            Template t = configuration.getTemplate(template + ".ftl");
            StringWriter sw = new StringWriter();
            t.process(model, sw);
            return sw.toString();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public String process(String template, Object object) {
        Map<String, Object> model = JSON.parseObject(JSON.toJSONString(object), new TypeReference<Map<String, Object>>() {
        });
        return process(template, model);
    }
}
