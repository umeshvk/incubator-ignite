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

package org.apache.ignite.internal.processors.cache;

import org.apache.ignite.*;
import org.apache.ignite.cache.*;
import org.apache.ignite.configuration.*;
import org.apache.ignite.spi.discovery.tcp.*;
import org.apache.ignite.spi.discovery.tcp.ipfinder.*;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.*;
import org.apache.ignite.transactions.*;

import static org.apache.ignite.cache.CacheAtomicityMode.*;
import static org.apache.ignite.cache.CacheMode.*;

/**
 * Test for read through store.
 */
public class CacheReadThroughRestartSelfTest extends GridCacheAbstractSelfTest {
    /** */
    private static final TcpDiscoveryIpFinder IP_FINDER = new TcpDiscoveryVmIpFinder(true);

    /** {@inheritDoc} */
    @Override protected int gridCount() {
        return 2;
    }

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String gridName) throws Exception {
        IgniteConfiguration cfg = super.getConfiguration(gridName);

        CacheConfiguration cc = cacheConfiguration(gridName);

        cc.setLoadPreviousValue(false);

        cfg.setCacheConfiguration(cc);

        TcpDiscoverySpi disco = new TcpDiscoverySpi();

        disco.setIpFinder(IP_FINDER);

        cfg.setDiscoverySpi(disco);

        return cfg;
    }

    /** {@inheritDoc} */
    @Override protected CacheAtomicityMode atomicityMode() {
        return TRANSACTIONAL;
    }

    /** {@inheritDoc} */
    @Override protected CacheMode cacheMode() {
        return PARTITIONED;
    }

    /**
     * @throws Exception If failed.
     */
    public void testReadThroughInTx() throws Exception {
        IgniteCache<String, Integer> cache = grid(1).cache(null);

        for (int k = 0; k < 1000; k++)
            cache.put("key" + k, k);

        stopAllGrids();

        startGrids(2);

        Ignite ignite = grid(1);

        cache = ignite.cache(null);

        try (Transaction tx = ignite.transactions().
            txStart(TransactionConcurrency.PESSIMISTIC, TransactionIsolation.REPEATABLE_READ, 100000, 1000)) {
            for (int k = 0; k < 1000; k++)
                assertNotNull("Null key. [key=key" + k + "].", cache.get("key" + k));

            tx.commit();
        }
    }

    /**
     * @throws Exception If failed.
     */
    public void testReadThrough() throws Exception {
        IgniteCache<String, Integer> cache = grid(1).cache(null);

        for (int k = 0; k < 1000; k++)
            cache.put("key" + k, k);

        stopAllGrids();

        startGrids(2);

        Ignite ignite = grid(1);

        cache = ignite.cache(null);

        for (int k = 0; k < 1000; k++)
            assertNotNull("Null key. [key=key" + k + "].", cache.get("key" + k));
    }
}
