package org.levk.rmp2.frames;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.levk.rmp2.schemas.Frames;

public class Handshake {
    private int stage;
    private byte[] pubkey;
    private byte[] signature;
    private byte[] data;

    public Handshake(int stage, byte[] pubkey, byte[] signature, byte[] data) {
        this.stage = stage;
        this.pubkey = pubkey;
        this.signature = signature;
        this.data = data;
    }

    public Handshake(byte[] serialized) throws InvalidProtocolBufferException {
        Frames.Handshake frame = Frames.Handshake.parseFrom(serialized);

        this.stage = frame.getStage();
        this.pubkey = frame.getPubkey().toByteArray();
        this.signature = frame.getSignature().toByteArray();
        this.data = frame.getData().toByteArray();
    }

    public int getStage() {
        return stage;
    }

    public byte[] getPubkey() {
        return pubkey;
    }

    public byte[] getSignature() {
        return signature;
    }

    public byte[] getData() {
        return data;
    }

    public byte[] getSer() {
        return Frames.Handshake.newBuilder().setStage(stage).setPubkey(ByteString.copyFrom(pubkey)).setSignature(ByteString.copyFrom(signature)).setData(ByteString.copyFrom(data)).build().toByteArray();
    }
}
