package com.itisacat.pdf.demo.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AddContractTableBo {
    //行数
    private int rowNumber;
    //列数
    private int columnNumber;
    //表中值
    private List<List<String>> data;
}
