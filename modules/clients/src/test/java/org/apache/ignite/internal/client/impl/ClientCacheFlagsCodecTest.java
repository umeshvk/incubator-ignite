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

package org.apache.ignite.internal.client.impl;

import junit.framework.*;
import org.apache.ignite.internal.client.*;
import org.apache.ignite.internal.client.impl.connection.*;
import org.apache.ignite.internal.processors.rest.handlers.cache.*;
import org.apache.ignite.internal.util.typedef.*;

import java.util.*;

import static org.apache.ignite.internal.client.GridClientCacheFlag.*;

/**
 * Tests conversions between GridClientCacheFlag.
 */
public class ClientCacheFlagsCodecTest extends TestCase {
    /**
     * Tests that each client flag will be correctly converted to server flag.
     */
    public void testEncodingDecodingFullness() {
        for (GridClientCacheFlag f : GridClientCacheFlag.values()) {
            if (f == KEEP_PORTABLES)
                continue;

            int bits = GridClientConnection.encodeCacheFlags(Collections.singleton(f));

            assertTrue(bits != 0);

            boolean out = GridCacheCommandHandler.parseCacheFlags(bits);

            assertEquals(out, true);
        }
    }

    /**
     * Tests that groups of client flags can be correctly converted to corresponding server flag groups.
     */
    public void testGroupEncodingDecoding() {
        // all
        doTestGroup(GridClientCacheFlag.values());
        // none
        doTestGroup();
    }

    /**
     * @param flags Client flags to be encoded, decoded and checked.
     */
    private void doTestGroup(GridClientCacheFlag... flags) {
        EnumSet<GridClientCacheFlag> flagSet = F.isEmpty(flags) ? EnumSet.noneOf(GridClientCacheFlag.class) :
            EnumSet.copyOf(Arrays.asList(flags));

        int bits = GridClientConnection.encodeCacheFlags(flagSet);

        boolean out = GridCacheCommandHandler.parseCacheFlags(bits);

        int length = flagSet.contains(KEEP_PORTABLES) ? flagSet.size() - 1 : flagSet.size();

        assertEquals(length > 0, out);
    }
}
