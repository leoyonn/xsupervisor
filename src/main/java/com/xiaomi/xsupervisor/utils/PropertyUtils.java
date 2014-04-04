/**
 * PropertyUtils.java
 * [CopyRight]
 * @author leo [liuy@xiaomi.com]
 * @date Aug 15, 2013 11:59:33 AM
 */
package com.xiaomi.xsupervisor.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Properties;

import com.xiaomi.miliao.utils.IOUtil;

/**
 * 
 * @author leo
 */
public class PropertyUtils {
    public static final com.xiaomi.miliao.utils.PropertyUtil MILIAO_PROP_UTIL = new com.xiaomi.miliao.utils.PropertyUtil();

    /**
     * Load properties from stream, and return the {@link Properties} instance. <br>
     * The input stream will be closed by this method.
     * 
     * @param stream
     * @param putToSystemProperties
     * @return the Properties instance, or null if failed.
     */
    public static Properties loadFromStream(InputStream stream, boolean putToSystemProperties) {
        Properties properties = new Properties();
        try {
            properties.load(stream);
            if (putToSystemProperties) {
                System.getProperties().putAll(properties);
            }
            return properties;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } finally {
            new IOUtil().closeIgnoreException(stream);
        }
    }

    public static Properties loadFromString(String string, boolean putToSystemProperties) {
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(string));
            return properties;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static Properties loadFromString(String string) {
        return loadFromString(string, false);
    }
}
