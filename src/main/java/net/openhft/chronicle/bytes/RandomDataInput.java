/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.openhft.chronicle.bytes;

import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.annotation.ForceInline;
import org.jetbrains.annotations.NotNull;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;

/**
 * This allows random access to the underling bytes.  This instance can be used across threads as it is stateless.
 * The thread safety of the underlying data depends on how the methods are used.
 */
public interface RandomDataInput extends RandomCommon {
    String[] charToString = createCharToString();

    @NotNull
    static String[] createCharToString() {
        String[] charToString = new String[256];
        charToString[0] = "\u0660";
        for (int i = 1; i < 21; i++)
            charToString[i] = Character.toString((char) (i + 0x2487));
        for (int i = ' '; i < 256; i++)
            charToString[i] = Character.toString((char) i);
        for (int i = 21; i < ' '; i++)
            charToString[i] = "\\u00" + Integer.toHexString(i).toUpperCase();
        for (int i = 0x80; i < 0xA0; i++)
            charToString[i] = "\\u00" + Integer.toHexString(i).toUpperCase();
        return charToString;
    }

    @ForceInline
    default boolean readBoolean(long offset)
            throws BufferUnderflowException, IORuntimeException {
        return readByte(offset) != 0;
    }

    byte readByte(long offset) throws BufferUnderflowException, IORuntimeException;

    @ForceInline
    default int readUnsignedByte(long offset)
            throws BufferUnderflowException, IORuntimeException {
        return readByte(offset) & 0xFF;
    }

    short readShort(long offset) throws BufferUnderflowException, IORuntimeException;

    @ForceInline
    default int readUnsignedShort(long offset)
            throws BufferUnderflowException, IORuntimeException {
        return readShort(offset) & 0xFFFF;
    }

    int readInt(long offset) throws BufferUnderflowException, IORuntimeException;

    @ForceInline
    default long readUnsignedInt(long offset)
            throws BufferUnderflowException, IORuntimeException {
        return readInt(offset) & 0xFFFFFFFFL;
    }

    long readLong(long offset) throws BufferUnderflowException, IORuntimeException;

    float readFloat(long offset) throws BufferUnderflowException, IORuntimeException;

    double readDouble(long offset) throws BufferUnderflowException, IORuntimeException;

    default String printable(long offset) throws BufferUnderflowException, IORuntimeException {
        return charToString[readUnsignedByte(offset)];
    }

    /**
     * Read a 32-bit int from memory with a load barrier.
     *
     * @param offset to read
     * @return the int value
     */
    default int readVolatileInt(long offset)
            throws BufferUnderflowException, IORuntimeException {
        OS.memory().loadFence();
        return readInt(offset);
    }

    /**
     * Read a float from memory with a load barrier.
     *
     * @param offset to read
     * @return the float value
     */
    default float readVolatileFloat(long offset)
            throws BufferUnderflowException, IORuntimeException {
        return Float.intBitsToFloat(readVolatileInt(offset));
    }

    /**
     * Read a 64-bit long from memory with a load barrier.
     * @param offset to read
     * @return the long value
     */
    default long readVolatileLong(long offset)
            throws BufferUnderflowException, IORuntimeException {
        OS.memory().loadFence();
        return readLong(offset);
    }

    /**
     * Read a 64-bit double from memory with a load barrier.
     *
     * @param offset to read
     * @return the double value
     */
    default double readVolatileDouble(long offset)
            throws BufferUnderflowException, IORuntimeException {
        return Double.longBitsToDouble(readVolatileLong(offset));
    }

    default long parseLong(long offset) throws BufferUnderflowException, IORuntimeException {
        return BytesInternal.parseLong(this, offset);
    }

    /**
     * expert level method for copying data to native memory.
     *
     * @param position within the ByteStore to copy.
     * @param address  in native memory
     * @param size     in bytes
     */
    void nativeRead(long position, long address, long size);

    default void copyTo(@NotNull byte[] bytes)
            throws BufferUnderflowException, IORuntimeException {
        for (int i = 0; i < bytes.length; i++)
            bytes[i] = readByte(start() + i);
    }

    default long readIncompleteLong(long offset) throws IORuntimeException {
        long left = readRemaining() - offset;
        long l = 0;
        try {
            if (left >= 8)
                return readLong(offset);
            if (left == 4)
                return readInt(offset);
            l = 0;
            for (int i = 0, remaining = (int) left; i < remaining; i++) {
                l |= (long) readUnsignedByte(offset + i) << (i * 8);
            }
        } catch (BufferUnderflowException e) {
            throw new AssertionError(e);
        }
        return l;
    }

    long realCapacity();

    /**
     * Perform an atomic add and get operation for a 32-bit int
     * @param offset to add and get
     * @param adding value to add, can be 1
     * @return the sum
     */
    default int addAndGetInt(long offset, int adding)
            throws IllegalArgumentException, IORuntimeException, BufferOverflowException, BufferUnderflowException {
        return BytesInternal.addAndGetInt(this, offset, adding);
    }

    /**
     * Perform an atomic add and get operation for a 64-bit long
     * @param offset to add and get
     * @param adding value to add, can be 1
     * @return the sum
     */
    default long addAndGetLong(long offset, long adding)
            throws IllegalArgumentException, IORuntimeException, BufferOverflowException, BufferUnderflowException {
        return BytesInternal.addAndGetLong(this, offset, adding);
    }

    /**
     * Perform an atomic add and get operation for a 32-bit float
     *
     * @param offset to add and get
     * @param adding value to add, can be 1
     * @return the sum
     */
    default float addAndGetFloat(long offset, float adding)
            throws IllegalArgumentException, IORuntimeException, BufferOverflowException, BufferUnderflowException {
        return BytesInternal.addAndGetFloat(this, offset, adding);
    }

    /**
     * Perform an atomic add and get operation for a 64-bit double
     *
     * @param offset to add and get
     * @param adding value to add, can be 1
     * @return the sum
     */
    default double addAndGetDouble(long offset, double adding)
            throws IllegalArgumentException, IORuntimeException, BufferOverflowException, BufferUnderflowException {
        return BytesInternal.addAndGetDouble(this, offset, adding);
    }
}
