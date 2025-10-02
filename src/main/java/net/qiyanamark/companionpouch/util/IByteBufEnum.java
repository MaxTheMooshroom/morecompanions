package net.qiyanamark.companionpouch.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import net.minecraft.network.FriendlyByteBuf;

public interface IByteBufEnum<T extends Enum<T> & IByteBufEnum<T>> {
    default void writeByte(FriendlyByteBuf buf) {
        T self = cast(this);
        if (self.getClass().getEnumConstants().length > Byte.MAX_VALUE) {
            throw new UnsupportedOperationException("Cannot write to a byte an enum with more than " + Byte.MAX_VALUE + " variants");
        }

        buf.writeByte(self.ordinal());
        buf.writeShort(hashFnv1a(self.name()));
    }

    static <T extends Enum<T> & IByteBufEnum<T>> T readByte(Class<T> clazz, FriendlyByteBuf buf) {
        T[] constants = clazz.getEnumConstants();
        if (constants.length > Byte.MAX_VALUE) {
            throw new UnsupportedOperationException("Cannot read from a byte an enum with more than " + Byte.MAX_VALUE + " variants");
        }

        int ordinal = buf.readByte();
        if (ordinal >= constants.length) {
            throw new IllegalArgumentException("ordinal value " + ordinal + " provided for enum with only " + constants.length + " variants");
        }

        T result = constants[ordinal];

        long hashSent = buf.readShort();
        short hashCalculated = hashFnv1a(result.name());

        if (hashCalculated != hashSent) {
            throw new RuntimeException(
                    "Hash mismatch for " + clazz.getName() +
                    ": expected " + hashCalculated + " but got " + hashSent
            );
        }

        return result;
    }

    private static short hashFnv1a(String s) {
        int hash = 0x811c9dc5;
        for (byte b : s.getBytes(StandardCharsets.UTF_8)) {
            hash ^= b & 0xff;
            hash *= 0x01000193;
        }
        return (short) ((hash & 0xffff) ^ (hash >> 16));
    }

    @SuppressWarnings("unchecked")
    private static <T extends Enum<T> & IByteBufEnum<T>> T cast(IByteBufEnum<T> self) {
        return (T) self;
    }
}
