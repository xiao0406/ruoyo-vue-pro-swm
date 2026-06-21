package cn.iocoder.yudao.module.iot.tcp.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 智能帧解码器
 * 支持多种消息格式：
 * 1. 以换行符结尾的消息（标准格式）
 * 2. 没有换行符的消息（在连接空闲或达到最大长度时处理）
 * 3. 以特定分隔符结尾的消息（如#）
 */
public class SmartFrameDecoder extends ByteToMessageDecoder {

    private static final Logger logger = LoggerFactory.getLogger(SmartFrameDecoder.class);

    private final int maxFrameLength;
    private static final byte LINE_FEED = '\n';
    private static final byte CARRIAGE_RETURN = '\r';
    private static final byte HASH = '#';

    // 超时时间（毫秒），如果在这个时间内没有收到完整消息，就处理缓冲区中的数据
    private static final long FRAME_TIMEOUT_MS = 1000;
    private long lastDataTime = 0;

    public SmartFrameDecoder(int maxFrameLength) {
        this.maxFrameLength = maxFrameLength;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 更新最后接收数据的时间
        lastDataTime = System.currentTimeMillis();

        // 检查是否超过最大帧长度
        if (in.readableBytes() > maxFrameLength) {
            logger.warn("消息长度超过最大限制 {} bytes，丢弃数据", maxFrameLength);
            in.clear();
            return;
        }

        // 方案1：查找换行符分隔的消息
        int lineEndIndex = findLineEnd(in);
        if (lineEndIndex >= 0) {
            // 找到换行符，提取消息
            ByteBuf frame = in.readRetainedSlice(lineEndIndex);
            skipDelimiters(in); // 跳过分隔符
            out.add(frame);
            logger.info("解码换行符分隔的消息，长度: {}", frame.readableBytes());
            return;
        }

        // 方案2：查找#分隔的消息
        int hashIndex = findByte(in, HASH);
        if (hashIndex >= 0) {
            // 找到#分隔符，提取消息（包含#）
            ByteBuf frame = in.readRetainedSlice(hashIndex + 1);
            out.add(frame);
            logger.info("解码#分隔的消息，长度: {}", frame.readableBytes());
            return;
        }

        // 方案3：检查是否应该处理缓冲区中的所有数据
        // 条件：数据停止流入超过指定时间，或者缓冲区接近满
        long currentTime = System.currentTimeMillis();
        boolean shouldFlush = false;

        // 如果有数据且满足以下条件之一，就处理缓冲区中的数据：
        if (in.readableBytes() > 0) {
            // 1. 数据停止流入超过1秒
            if (currentTime - lastDataTime > FRAME_TIMEOUT_MS) {
                shouldFlush = true;
                logger.debug("数据流入超时，处理缓冲区数据");
            }
            // 2. 缓冲区使用率超过80%
            else if (in.readableBytes() > maxFrameLength * 0.8) {
                shouldFlush = true;
                logger.debug("缓冲区接近满，处理缓冲区数据");
            }
        }

        if (shouldFlush) {
            // 处理缓冲区中的所有数据
            ByteBuf frame = in.readRetainedSlice(in.readableBytes());
            out.add(frame);
            logger.debug("解码无分隔符消息，长度: {}", frame.readableBytes());
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 处理空闲事件，如果有缓冲数据就处理
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleEvent = (IdleStateEvent) evt;
            if (idleEvent.state() == IdleState.READER_IDLE) {
                // 读空闲时，如果缓冲区有数据，就处理它
                ByteBuf buffer = internalBuffer();
                if (buffer != null && buffer.readableBytes() > 0) {
                    logger.debug("读空闲触发，处理缓冲区剩余数据，长度: {}", buffer.readableBytes());
                    ByteBuf frame = buffer.readRetainedSlice(buffer.readableBytes());
                    ctx.fireChannelRead(frame);
                }
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    /**
     * 查找行结束符的位置
     */
    private int findLineEnd(ByteBuf buffer) {
        for (int i = buffer.readerIndex(); i < buffer.writerIndex(); i++) {
            byte b = buffer.getByte(i);
            if (b == LINE_FEED) {
                return i - buffer.readerIndex();
            } else if (b == CARRIAGE_RETURN && i + 1 < buffer.writerIndex() &&
                    buffer.getByte(i + 1) == LINE_FEED) {
                return i - buffer.readerIndex();
            }
        }
        return -1;
    }

    /**
     * 查找指定字节的位置
     */
    private int findByte(ByteBuf buffer, byte target) {
        for (int i = buffer.readerIndex(); i < buffer.writerIndex(); i++) {
            if (buffer.getByte(i) == target) {
                return i - buffer.readerIndex();
            }
        }
        return -1;
    }

    /**
     * 跳过分隔符
     */
    private void skipDelimiters(ByteBuf buffer) {
        while (buffer.isReadable()) {
            byte b = buffer.getByte(buffer.readerIndex());
            if (b == LINE_FEED || b == CARRIAGE_RETURN) {
                buffer.skipBytes(1);
            } else {
                break;
            }
        }
    }

    @Override
    protected void decodeLast(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 连接关闭时，处理缓冲区中剩余的数据
        if (in.readableBytes() > 0) {
            logger.debug("连接关闭，处理剩余数据，长度: {}", in.readableBytes());
            ByteBuf frame = in.readRetainedSlice(in.readableBytes());
            out.add(frame);
        }
    }
}
