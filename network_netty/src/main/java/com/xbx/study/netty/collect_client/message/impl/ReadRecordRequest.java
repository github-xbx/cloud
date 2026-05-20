package com.xbx.study.netty.collect_client.message.impl;

import com.xbx.study.netty.collect_client.domain.PaveRecord;
import com.xbx.study.netty.collect_client.message.BasePaveMessage;
import com.xbx.study.netty.collect_client.message.PaveMessageOutput;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

public class ReadRecordRequest extends BasePaveMessage implements PaveMessageOutput {

    /**
     * 本次发送记录数
     */
    private Integer readCount = 1;

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
        this.records = list;
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
        buf.writeShort(this.readCount);
        buf.writeInt(this.noConfirm);
        for (PaveRecord record : records) {
            generateData(buf, record);
        }
    }
}
