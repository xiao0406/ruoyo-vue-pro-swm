package cn.iocoder.yudao.module.iot.tcp.controller;

import cn.iocoder.yudao.module.iot.tcp.model.TcpMessageData;
import cn.iocoder.yudao.module.iot.tcp.parser.TcpMessageParser;
import cn.iocoder.yudao.module.iot.tcp.processor.AlarmZeroTcpProcessor;
import cn.iocoder.yudao.module.iot.util.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * TCP消息处理测试控制器
 * 用于测试TCP消息解析和处理功能
 *
 * @author Shawn
 * @date 2025-01-22
 */
@RestController
@RequestMapping("/tcp/test")
public class TcpMessageTestController {

    private static final Logger logger = LoggerFactory.getLogger(TcpMessageTestController.class);

    @Resource
    private TcpMessageParser messageParser;

    @Resource
    private AlarmZeroTcpProcessor alarmZeroProcessor;

    /**
     * 测试TCP消息解析
     *
     * @param message TCP消息内容
     * @return 解析结果
     */
    @PostMapping("/parse")
    public R<TcpMessageData> testParseMessage(@RequestBody String message) {
        try {
            logger.info("测试TCP消息解析: {}", message);

            TcpMessageData result = messageParser.parseMessage(message);
            if (result != null) {
                return R.ok(result);
            } else {
                return R.fail("消息解析失败");
            }

        } catch (Exception e) {
            logger.error("测试TCP消息解析时发生异常", e);
            return R.fail("解析异常: " + e.getMessage());
        }
    }

    /**
     * 测试TCP消息处理
     *
     * @param message TCP消息内容
     * @return 处理结果
     */
    @PostMapping("/process")
    public R<String> testProcessMessage(@RequestBody String message) {
        try {
            logger.info("测试TCP消息处理: {}", message);

            // 解析消息
            TcpMessageData tcpData = messageParser.parseMessage(message);
            if (tcpData == null) {
                return R.fail("消息解析失败");
            }

            // 检查是否可以处理
            if (!alarmZeroProcessor.canProcess(tcpData)) {
                return R.fail("不支持的消息类型，报警值: " + tcpData.getAlarmValue());
            }

            // 处理消息
            alarmZeroProcessor.process(tcpData);

            return R.ok("消息处理成功");

        } catch (Exception e) {
            logger.error("测试TCP消息处理时发生异常", e);
            return R.fail("处理异常: " + e.getMessage());
        }
    }

    /**
     * 测试用的示例TCP消息
     *
     * @return 示例消息
     */
    @GetMapping("/sample-message")
    public R<String> getSampleMessage() {
        String sampleMessage = "$13B,S,4015830001,N,0000.0000,E,00000.0000,00000,00000,VER2,00,99.0,060611-000000,19,0,42,023,001,090,"
                +
                "80ECCCD0A8E4,-71,011,80ECCCD0A8EB,-64,011,80ECCCD241A2,-66,011,80ECCCD2419B,-65,011,80ECCCD2419C,-68,011,"
                +
                "80ECCCD09D72,-72,011,80ECCCD0A004,-61,011,80ECCCD241E2,-72,011,80ECCCD241C6,-61,011,80ECCCD241A3,-69,011,"
                +
                "413,92,#";

        return R.ok(sampleMessage);
    }
}
