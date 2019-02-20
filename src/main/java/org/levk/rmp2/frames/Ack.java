package org.levk.rmp2.frames;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;
import org.levk.rmp2.schemas.Frames;

import java.util.List;

public class Ack {
    private long messageId;
    private byte[] sender;
    private List<Long> ranges;

    private boolean isFinal = false;

    public Ack(long messageId, byte[] sender, List<Long> ranges) {
        this.messageId = messageId;
        this.sender = sender;
        this.ranges = ranges;
    }

    public Ack(byte[] serialized) throws InvalidProtocolBufferException {
        Frames.Ack frame = Frames.Ack.parseFrom(serialized);

        this.messageId = frame.getMessageId();
        this.sender = frame.getSender().toByteArray();
        this.ranges = frame.getStartIndicesList();
    }

    public long getMessageId() {
        return messageId;
    }

    public byte[] getSender() {
        return sender;
    }

    public List<Long> getRanges() {
        return ranges;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void addRange(long startIndex) {
        if (!isFinal) {
            this.ranges.add(startIndex);
        } else {
            throw new RuntimeException("Cannot edit Ack after finalized.");
        }
    }

    public void finalize() {
        this.isFinal = true;
    }

    public byte[] getSer() {
        return Frames.Ack.newBuilder().setMessageId(messageId).setSender(ByteString.copyFrom(sender)).addAllStartIndices(ranges).build().toByteArray();
    }
}
