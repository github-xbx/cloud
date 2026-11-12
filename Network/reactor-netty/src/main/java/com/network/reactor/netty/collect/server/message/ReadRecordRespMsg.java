package com.network.reactor.netty.collect.server.message;


import com.network.reactor.netty.collect.domain.PaveRecord;
import io.netty.buffer.ByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.util.ArrayList;
import java.util.List;

public class ReadRecordRespMsg extends BasePaveMsg implements PaveMsgInput {

    private static final Logger logger = LogManager.getLogger(ReadRecordRespMsg.class);

    /**
     * 单条记录占用的字节数：rtc(4) + 毫秒(2) + 3 个状态(3) + 经纬度(8+8) + 6 个 float(24)
     */
    private static final int RECORD_BYTES = 49;

    /**
     * 本次发送记录数
     */
    private Integer readCount;

    /**
     * 未确认记录数
     */
    private Long noConfirm;

    /**
     * 记录
     */
    private List<PaveRecord> records = new ArrayList<>();

    @Override
    public void decode(ByteBuf buf) {
        this.readCount = buf.readUnsignedShort();
        this.noConfirm = buf.readUnsignedInt();

        // 防御：报文声明的条数可能和实际字节数对不上（对端 readCount 与列表不一致时就会这样），
        // 按剩余字节能容纳的条数解析，避免 IndexOutOfBoundsException 把整帧数据丢掉
        int available = buf.readableBytes() / RECORD_BYTES;
        int actual = Math.min(this.readCount == null ? 0 : this.readCount, available);
        if (actual < (this.readCount == null ? 0 : this.readCount)) {
            logger.warn("[Server] 报文声明 {} 条记录，但剩余 {} 字节只够 {} 条，按 {} 条解析",
                    this.readCount, buf.readableBytes(), available, actual);
        }

        for (int i = 0; i < actual; i++) {
            records.add(parseData(buf));
        }
    }


    public Integer getReadCount() {
        return readCount;
    }

    public void setReadCount(Integer readCount) {
        this.readCount = readCount;
    }

    public Long getNoConfirm() {
        return noConfirm;
    }

    public void setNoConfirm(Long noConfirm) {
        this.noConfirm = noConfirm;
    }

    public List<PaveRecord> getRecords() {
        return records;
    }

    public void setRecords(List<PaveRecord> records) {
        this.records = records;
    }

    @Override
    public String toString() {
        return "ReadRecordRespMsg{" +
                "readCount=" + readCount +
                ", noConfirm=" + noConfirm +
                ", records=" + records +
                ", frame=" + frame +
                ", sequence=" + sequence +
                ", crcValid=" + crcValid +
                '}';
    }
}
