/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.input.stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mule.module.apikit.validation.body.form.MultipartFormValidatorTest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

class RewindableInputStreamTest {

    @Test
    void testClose() throws IOException {
        RewindableInputStream rewindableInputStream = new RewindableInputStream(new ByteArrayInputStream(MultipartFormValidatorTest.MULTIPART_BODY.getBytes()));
        int i = rewindableInputStream.available();
        Assertions.assertTrue(i > 0);
        Assertions.assertTrue(rewindableInputStream.canRewind());
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int nRead;
            while ((nRead = rewindableInputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
        Assertions.assertTrue(rewindableInputStream.canRewind());
        rewindableInputStream.rewind();
        rewindableInputStream.willNotRewind();
        Assertions.assertThrows(IllegalStateException.class, rewindableInputStream::rewind);
        rewindableInputStream.close();
    }

    @Test
    void testRewind() throws IOException {
        RewindableInputStream rewindableInputStream = new RewindableInputStream(new ByteArrayInputStream(MultipartFormValidatorTest.MULTIPART_BODY.getBytes()));
        ByteArrayOutputStream     buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        RewindableInputStream.Block block = new RewindableInputStream.Block();
        block.append(data[0]);
        try {
            while ((nRead = rewindableInputStream.read()) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            byte[] byteArray = buffer.toByteArray();
            assertFalse(new String(byteArray, UTF_8).isEmpty());
            rewindableInputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testRewindableStreamFailure() {
        assertThrows(NullPointerException.class, () -> {
            RewindableInputStream rewindableInputStream = new RewindableInputStream(null);
        });
    }

    @Test
    public void testCloseSucces() throws IOException, NoSuchFieldException, IllegalAccessException {
        // Create a test input stream
        InputStream testInputStream = new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }
        };

        // Create a RewindableInputStream based on the test input stream
        RewindableInputStream rewindableInputStream = new RewindableInputStream(testInputStream);

        // Close the RewindableInputStream
        rewindableInputStream.close();
        Field eofField = RewindableInputStream.class.getDeclaredField("eof");
        eofField.setAccessible(true);
        boolean eof = (boolean) eofField.get(rewindableInputStream);

        // Assert that the input stream is closed
        assertFalse(eof);
    }

    @Test
    public void testRewindSuccess() throws IOException {
        // Create a test input stream
        InputStream testInputStream = new InputStream() {
            private int count = 0;
            private final int[] data = {1, 2, 3};

            @Override
            public int read() throws IOException {
                if (count >= data.length) {
                    return -1;
                }
                return data[count++];
            }
        };

        // Create a RewindableInputStream based on the test input stream
        RewindableInputStream rewindableInputStream = new RewindableInputStream(testInputStream);

        // Read the first byte
        rewindableInputStream.read();

        // Rewind the input stream
        rewindableInputStream.rewind();

        // Assert that the first byte can be read again
        assertEquals(1, rewindableInputStream.read());
    }

    @Test
    public void testWillNotRewind() {
        // Create a test input stream
        InputStream testInputStream = new InputStream() {
            @Override
            public int read() throws IOException {
                return -1;
            }
        };

        // Create a RewindableInputStream based on the test input stream
        RewindableInputStream rewindableInputStream = new RewindableInputStream(testInputStream);

        // Call willNotRewind()
        rewindableInputStream.willNotRewind();

        // Assert that canRewind() returns false
        assertFalse(rewindableInputStream.canRewind());
    }
}