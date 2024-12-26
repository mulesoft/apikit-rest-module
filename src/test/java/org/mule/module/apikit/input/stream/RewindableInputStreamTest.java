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
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        try {
            while ((nRead = rewindableInputStream.read()) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            byte[] byteArray = buffer.toByteArray();
            assertFalse(new String(byteArray, UTF_8).isEmpty());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}