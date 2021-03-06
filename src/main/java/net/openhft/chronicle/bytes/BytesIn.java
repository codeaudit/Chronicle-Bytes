/*
 * Copyright 2016 higherfrequencytrading.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package net.openhft.chronicle.bytes;

import org.jetbrains.annotations.NotNull;

/*
 * Created by Peter Lawrey on 20/04/2016.
 */
public interface BytesIn<Underlying> extends
        StreamingDataInput<Bytes<Underlying>>,
        ByteStringParser<Bytes<Underlying>> {
    /**
     * Reads messages from this tails as methods.  It returns a BooleanSupplier which returns
     *
     * @param objects which implement the methods serialized to the file.
     * @return a reader which will read one Excerpt at a time
     */
    @NotNull
    default MethodReader bytesMethodReader(Object... objects) {
        return new BytesMethodReaderBuilder(this).build(objects);
    }

    @NotNull
    default BytesMethodReaderBuilder bytesMethodReaderBuilder() {
        return new BytesMethodReaderBuilder(this);
    }

    <T extends ReadBytesMarshallable> T readMarshallableLength16(Class<T> tClass, T object);
}
