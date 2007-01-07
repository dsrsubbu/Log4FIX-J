/*
 * The Log4FIX Software License
 * Copyright (c) 2006 - 2007 opentradingsolutions.org  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Neither the name of the product (Log4FIX), nor opentradingsolutions.org,
 *    nor the names of its contributors may be used to endorse or promote
 *    products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL OPENTRADINGSOLUTIONS.ORG OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

package org.opentradingsolutions.log4fix.importer;

import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Brian M. Coyner
 */
public class Importer {

    private Thread producer;
    private ImporterCallback callback;
    private BlockingQueue<String> queue;

    public Importer() {
        this (new LinkedBlockingQueue<String>());
    }
    
    protected Importer(BlockingQueue<String> queue) {
        this.queue = queue;
    }

    public void start(ImporterModel model, InputStream is, ImporterCallback callback) {
        this.callback = callback;


        producer = new Thread(new LogMessageParser(is, queue));

        Thread consumer = new Thread(new LogMessageBuilder(model, queue));

        callback.starting();
        producer.start();
        consumer.start();

        awaitTermination(consumer, callback);
    }

    public void stop() {
        callback.canceling();
        producer.interrupt();
    }

    private void awaitTermination(Thread consumer, ImporterCallback callback) {
        try {
            consumer.join();
        } catch (InterruptedException e) {
            stop();
        } finally {
            callback.done();
        }
    }
}