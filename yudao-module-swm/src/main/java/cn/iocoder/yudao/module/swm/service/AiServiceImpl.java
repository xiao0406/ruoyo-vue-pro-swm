package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.module.swm.dal.dataobject.SwmDifyDO;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@Slf4j
public class AiServiceImpl implements AiService {

    private static final List<Extension> MARKDOWN_EXTENSIONS = List.of(TablesExtension.create());

    @Resource
    private SwmDifyService swmDifyService;

    @Override
    public void processSwmDify(String difyId) {
        SwmDifyDO dify = swmDifyService.getSwmDify(difyId);
        if (dify == null) {
            log.warn("SwmDify record not found, id={}", difyId);
            return;
        }
        if (StringUtils.isBlank(dify.getReportContent())) {
            dify.setReportContent(generatePart45Markdown(null, null, dify.getDifyType()));
            swmDifyService.save(dify);
        }
    }

    @Override
    public byte[] convertMdToPdfBytes(String markdown) {
        if (StringUtils.isBlank(markdown)) {
            return new byte[0];
        }
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            String html = toHtml(markdown);
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Markdown 转 PDF 失败", e);
        }
    }

    @Override
    public String generatePart45Markdown(String startDate, String endDate, String reportType) {
        String range = StringUtils.defaultIfBlank(startDate, "未指定开始日期")
                + " 至 " + StringUtils.defaultIfBlank(endDate, "未指定结束日期");
        String type = StringUtils.defaultIfBlank(reportType, "daily");
        return """
                ## 四、风险研判

                - 报告类型：%s
                - 统计周期：%s
                - 重点关注：人员考勤异常、设备离线、危险区域停留、告警处置闭环。

                ## 五、管控建议

                - 对未闭环告警建立责任人和完成时限。
                - 对频繁离线设备开展现场巡检和电量核查。
                - 对高风险区域加强班前教育、电子围栏和现场巡查。
                """.formatted(type, range);
    }

    private String toHtml(String markdown) {
        Parser parser = Parser.builder().extensions(MARKDOWN_EXTENSIONS).build();
        HtmlRenderer renderer = HtmlRenderer.builder().extensions(MARKDOWN_EXTENSIONS).build();
        Node document = parser.parse(markdown);
        return """
                <!doctype html>
                <html>
                <head>
                    <meta charset="UTF-8"/>
                    <style>
                        body { font-family: "Microsoft YaHei", "SimSun", sans-serif; line-height: 1.7; color: #222; }
                        table { border-collapse: collapse; width: 100%%; margin: 12px 0; }
                        th, td { border: 1px solid #ddd; padding: 6px 8px; }
                        th { background: #f5f7fa; }
                    </style>
                </head>
                <body>%s</body>
                </html>
                """.formatted(renderer.render(document));
    }

}
