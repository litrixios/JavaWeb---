package com.bjfu.cms.common.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.File;
import java.io.IOException;

public class PdfUtils {

    /**
     * 提取PDF文本内容
     */
    public static String extractText(String pdfFilePath) throws IOException {
        try (PDDocument document = PDDocument.load(new File(pdfFilePath))) {
            // 检查文档是否加密
            if (document.isEncrypted()) {
                throw new RuntimeException("PDF文件已加密，无法提取文本");
            }
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document); // 提取全文文本
        }
    }

    /**
     * 统计PDF字数（中文按字符，英文按单词，过滤空白字符）
     */
    public static int countWordsInPdf(String pdfFilePath) throws IOException {
        String text = extractText(pdfFilePath);
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        // 去除所有空白字符（空格、换行、制表符等）
        String cleanedText = text.replaceAll("\\s+", "");
        // 中文按字符数统计，英文按单词数统计（这里简化处理为总字符数）
        // 若需精确区分中英文，可使用正则匹配（如中文：[\u4e00-\u9fa5]，英文单词：[a-zA-Z]+）
        return cleanedText.length();
    }
}