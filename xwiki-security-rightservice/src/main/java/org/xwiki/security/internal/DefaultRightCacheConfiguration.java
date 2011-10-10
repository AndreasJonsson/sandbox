/*
 * Copyright 2010 Andreas Jonsson
 * 
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */
package org.xwiki.security.internal;

import org.xwiki.cache.Cache;
import org.xwiki.cache.CacheFactory;
import org.xwiki.cache.CacheException;
import org.xwiki.cache.config.CacheConfiguration;
import org.xwiki.cache.eviction.EntryEvictionConfiguration;
import org.xwiki.cache.eviction.LRUEvictionConfiguration;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.configuration.ConfigurationSource;
import static org.xwiki.component.descriptor.ComponentInstantiationStrategy.PER_LOOKUP;

import org.xwiki.security.RightCacheConfiguration;
import org.xwiki.security.RightCacheEntry;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;

/**
 * Default configuration for right cache.
 * @version $Id: DefaultRightCacheConfiguration.java 30733 2010-08-24 22:22:15Z sdumitriu $
 */
@Component
@InstantiationStrategy(PER_LOOKUP)
public class DefaultRightCacheConfiguration implements RightCacheConfiguration
{
    /** Logger object. */
    @Inject private Logger logger;

    /** Prefix for the configuration property keys. */
    private static final String RIGHTCACHE_PREFIX = "security.rightcache.";

    /** Obtain configuration from the xwiki.properties file. */
    @Inject
    @Named("xwikiproperties")
    private ConfigurationSource configuration;

    /** Cache factory. */
    @Inject
    @Named("jbosscache")
    private CacheFactory cacheFactory;

    /**
     * @param name Name of the property.
     * @param defaultValue A default value to use if none could be
     * found in the configuration.
     * @return a configured property, or the given default value. 
     */
    private String getRightCacheProperty(String name, String defaultValue)
    {
        return configuration.getProperty(RIGHTCACHE_PREFIX + name, defaultValue);
    }

    @Override
    public Cache<RightCacheEntry> getCache()
    {
        Cache<RightCacheEntry> cache;
        CacheConfiguration cacheConfig = new CacheConfiguration();
        cacheConfig.setConfigurationId("xwiki.security.rightcache");
        LRUEvictionConfiguration lru = new LRUEvictionConfiguration();
        Integer defaultCapacity = 500;
        String capacityString = getRightCacheProperty("capacity", defaultCapacity.toString());
        int capacity;
        try {
            capacity = Integer.parseInt(capacityString);
        } catch (NumberFormatException e) {
            capacity = defaultCapacity;
        }
        lru.setMaxEntries(capacity);
        cacheConfig.put(EntryEvictionConfiguration.CONFIGURATIONID, lru);
        try {
            cache = cacheFactory.newCache(cacheConfig);
        } catch (CacheException e) {
            logger.error("Failed to create rights cache.");
            throw new RuntimeException(e);
        }
        logger.info("Created a cache of type "
                    + cache.getClass().getName()
                    + " with a capacity of "
                    + capacity
                    + " entries.");

        return cache;
    }
}