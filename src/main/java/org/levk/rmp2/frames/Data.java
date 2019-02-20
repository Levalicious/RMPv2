package org.levk.rmp2.frames;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.levk.rmp2.schemas.Frames;

public class Data {
    private long messageId;
    private byte[] sender;
    private byte[] data;

    public Data(long messageId, byte[] sender, byte[] data) {
        this.messageId = messageId;
        this.sender = sender;
        this.data = data;
    }

    public Data(byte[] serialized) throws InvalidProtocolBufferException {
        Frames.Data frame = Frames.Data.parseFrom(serialized);

        this.messageId = frame.getMessageId();
        this.sender = frame.getSender().toByteArray();
        this.data = frame.getData().toByteArray();
    }


    public long getMessageId() {
        return messageId;
    }

    public byte[] getSender() {
        return sender;
    }

    public byte[] getData() {
        return data;
    }

    public byte[] getSer() {
        return Frames.Data.newBuilder().setMessageId(messageId).setSender(ByteString.copyFrom(sender)).setData(ByteString.copyFrom(data)).build().toByteArray();
    }
}
