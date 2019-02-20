package org.levk.rmp2.frames;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.levk.rmp2.schemas.Frames;

public class Payload {
    private byte[] data;
    private byte[] FEC;

    public Payload(byte[] data, byte[] FEC) {
        this.data = data;
        this.FEC = FEC;
    }

    public Payload(byte[] serialized) throws InvalidProtocolBufferException {
        Frames.Payload frame = Frames.Payload.parseFrom(serialized);

        this.data = frame.getData().toByteArray();
        this.FEC = frame.getFEC().toByteArray();
    }

    public byte[] getData() {
        return data;
    }

    public byte[] getFEC() {
        return FEC;
    }

    public byte[] getSer() {
        return Frames.Payload.newBuilder().setData(ByteString.copyFrom(data)).setFEC(ByteString.copyFrom(FEC)).build().toByteArray();
    }
}
