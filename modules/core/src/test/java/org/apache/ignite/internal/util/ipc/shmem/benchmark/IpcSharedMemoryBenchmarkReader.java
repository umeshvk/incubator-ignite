/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.util.ipc.shmem.benchmark;

import org.apache.ignite.*;
import org.apache.ignite.internal.util.ipc.*;
import org.apache.ignite.internal.util.ipc.shmem.*;
import org.apache.ignite.internal.util.typedef.*;
import org.apache.ignite.internal.util.typedef.internal.*;
import org.apache.ignite.testframework.junits.*;

import javax.swing.*;
import java.io.*;
import java.util.concurrent.atomic.*;

/**
 *
 */
public class IpcSharedMemoryBenchmarkReader implements IpcSharedMemoryBenchmarkParty {
    /** Destination buffer size. */
    public static final int DST_BUFFER_SIZE = 512 * 1024 * 1024;

    /** */
    private static volatile boolean done;

    /**
     * @param args Args.
     * @throws IgniteCheckedException If failed.
     */
    public static void main(String[] args) throws IgniteCheckedException {
        IpcSharedMemoryNativeLoader.load();

        int nThreads = (args.length > 0 ? Integer.parseInt(args[0]) : 1);

        final AtomicLong transferCntr = new AtomicLong();

        Thread collector = new Thread(new Runnable() {
            @SuppressWarnings("BusyWait")
            @Override public void run() {
                try {
                    while (!done) {
                        Thread.sleep(5000);

                        X.println("Transfer rate: " + transferCntr.getAndSet(0) / (1024 * 1024 * 5) + " MB/sec");
                    }
                }
                catch (InterruptedException ignored) {
                    // No-op.
                }

            }
        });

        collector.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override public void run() {
                System.out.println("Shutting down...");

                done = true;
            }
        });

        try (IpcSharedMemoryServerEndpoint srv = new IpcSharedMemoryServerEndpoint()) {
            new IgniteTestResources().inject(srv);

            srv.start();

            for (int i = 0; i < nThreads; i++) {
                final IpcEndpoint endPnt = srv.accept();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        InputStream space = null;

                        try {
                            space = endPnt.inputStream();

                            byte[] buf = new byte[DST_BUFFER_SIZE];

                            int pos = 0;

                            while (!done) {
                                int maxRead = Math.min(buf.length - pos, DFLT_BUF_SIZE);

                                int read = space.read(buf, pos, maxRead);

                                if (read == -1) {
                                    X.println("Space has been closed");

                                    return;
                                }

                                transferCntr.addAndGet(read);

                                pos += read;

                                if (pos >= buf.length)
                                    pos = 0;
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        finally {
                            U.closeQuiet(space);
                        }
                    }
                }).start();
            }
        }

        JOptionPane.showMessageDialog(null, "Press OK to stop READER.");

        done = true;
    }
}
