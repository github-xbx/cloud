package com.network.reactor.netty.collect.server.message;



import com.network.reactor.netty.collect.domain.PaveRecord;
import io.netty.buffer.ByteBuf;


public class RealTimeRespMsg extends BasePaveMsg implements PaveMsgInput {
    
    /**
     * 未确认记录数
     */
    private Long noConfirm;

    /**
     * 记录
     */
    private PaveRecord paveRecord;

    @Override
    public void decode(ByteBuf buf) {
        this.noConfirm = buf.readUnsignedInt();
        this.paveRecord = parseData(buf);
    }


    public Long getNoConfirm() {
        return noConfirm;
    }

    public void setNoConfirm(Long noConfirm) {
        this.noConfirm = noConfirm;
    }

    public PaveRecord getPaveRecord() {
        return paveRecord;
    }

    public void setPaveRecord(PaveRecord paveRecord) {
        this.paveRecord = paveRecord;
    }


}
