package cn.iocoder.yudao.module.swm.service;
public interface AiService {
    void processSwmDify(String difyId);

    /**
     * 将 Markdown 文本转换为 PDF 字节数组
     * @param markdown Markdown 文本
     * @return PDF 字节数组
     */
    byte[] convertMdToPdfBytes(String markdown);

    /**
     * 生成报告的第四、五部分 Markdown 内容
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param reportType 报告类型（daily/weekly/monthly）
     * @return Markdown 文本
     */
    String generatePart45Markdown(String startDate, String endDate, String reportType);
}
