package com.network.reactor.netty.collect.client.message.impl;


import com.network.reactor.netty.collect.domain.PaveRecord;
import com.network.reactor.netty.collect.client.message.BasePaveMessage;
import com.network.reactor.netty.collect.client.message.PaveMessageOutput;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

public class ReadRecordRequest extends BasePaveMessage implements PaveMessageOutput {

    /**
     * 本次发送记录数。默认 0，并且始终以 records.size() 为准，
     * 不要手动设成和列表不一致的值（否则对端会按声明的条数去解析，直接越界）
     */
    private Integer readCount = 0;

    /**
     * 未确认记录数
     */
    private Integer noConfirm = 0;

    /**
     * 记录
     */
    private List<PaveRecord> records = new ArrayList<>();


    public ReadRecordRequest() {
    }

    public ReadRecordRequest(List<PaveRecord> list, Integer frame, Integer sequence){
        this.records = list == null ? new ArrayList<>() : list;
        this.readCount = this.records.size();
        this.frame = frame;
        this.sequence = sequence;
    }


    public Integer getReadCount() {
        return readCount;
    }

    public void setReadCount(Integer readCount) {
        this.readCount = readCount;
    }

    public Integer getNoConfirm() {
        return noConfirm;
    }

    public void setNoConfirm(Integer noConfirm) {
        this.noConfirm = noConfirm;
    }

    public List<PaveRecord> getRecords() {
        return records;
    }

    public void setRecords(List<PaveRecord> records) {
        this.records = records;
    }

    @Override
    public void encode(ByteBuf buf) {
        // 以实际条数为准，彻底避免"声明 N 条却写 0 条"
        buf.writeShort(this.records.size());
        buf.writeInt(this.noConfirm);
        for (PaveRecord record : records) {
            generateData(buf, record);
        }
    }
}
