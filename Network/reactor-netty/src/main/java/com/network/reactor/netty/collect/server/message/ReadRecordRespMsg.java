package com.network.reactor.netty.collect.server.message;


import com.network.reactor.netty.collect.domain.PaveRecord;
import io.netty.buffer.ByteBuf;


import java.util.ArrayList;
import java.util.List;

public class ReadRecordRespMsg extends BasePaveMsg implements PaveMsgInput {

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
        for (Integer w = 0; w < this.readCount; w++) {
            PaveRecord r = parseData(buf);

            records.add(r);
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
