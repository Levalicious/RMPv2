package org.levk.rmp2.frames;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.levk.rmp2.schemas.Frames;

public class Stream {
    private long messageLength;
    private int chunkIndex;
    private int chunkLength;
    private byte[] data;
    private byte[] additionalData = null;

    public Stream(long messageLength, int chunkIndex, int chunkLength, byte[] data) {
        this.messageLength = messageLength;
        this.chunkIndex = chunkIndex;
        this.chunkLength = chunkLength;
        this.data = data;
    }

    public Stream(long messageLength, int chunkIndex, int chunkLength, byte[] data, byte[] additionalData) {
        this.messageLength = messageLength;
        this.chunkIndex = chunkIndex;
        this.chunkLength = chunkLength;
        this.data = data;
        this.additionalData = additionalData;
    }

    public Stream(byte[] serialized) throws InvalidProtocolBufferException {
        Frames.Stream frame = Frames.Stream.parseFrom(serialized);

        this.messageLength = frame.getMessageLength();
        this.chunkIndex = frame.getChunkIndex();
        this.chunkLength = frame.getChunkLength();
        this.data = frame.getData().toByteArray();

        if (frame.hasAdditionalData()) {
            this.additionalData = frame.getAdditionalData().toByteArray();
        } else {
            this.additionalData = null;
        }
    }

    public long getMessageLength() {
        return messageLength;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public int getChunkLength() {
        return chunkLength;
    }

    public byte[] getData() {
        return data;
    }

    public byte[] getAdditionalData() {
        return additionalData;
    }

    public byte[] getSer() {
        if (additionalData == null) {
            return Frames.Stream.newBuilder().setMessageLength(messageLength).setChunkIndex(chunkIndex).setChunkLength(chunkLength).setData(ByteString.copyFrom(data)).build().toByteArray();
        } else {
            return Frames.Stream.newBuilder().setMessageLength(messageLength).setChunkIndex(chunkIndex).setChunkLength(chunkLength).setData(ByteString.copyFrom(data)).setAdditionalData(ByteString.copyFrom(additionalData)).build().toByteArray();
        }
    }
}
