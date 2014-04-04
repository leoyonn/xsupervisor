/**
 * ServiceConf.java
 * [CopyRight]
 * @author leo [liuy@xiaomi.com]
 * @date Aug 15, 2013 10:09:44 AM
 */
package com.xiaomi.xsupervisor;

import java.util.Properties;

import com.xiaomi.xsupervisor.utils.PropertyUtils;

/**
 * @author leo
 */
public class ServiceConf {
    protected String id;
    protected String path;
    protected String host;
    protected String port;
    protected String restart;
    protected String start;
    protected String stop;
    protected String ping;
    protected String zkPath;
    protected Properties zkData;

    public String getId() {
        return id;
    }

    public ServiceConf setId(String id) {
        this.id = id;
        return this;
    }

    public String getPath() {
        return path;
    }

    public ServiceConf setPath(String path) {
        this.path = path;
        return this;
    }

    public String getHost() {
        return host;
    }

    public ServiceConf setHost(String host) {
        this.host = host;
        return this;
    }

    public String getPort() {
        return port;
    }

    public ServiceConf setPort(String port) {
        this.port = port;
        return this;
    }

    public String getRestart() {
        return restart;
    }

    public ServiceConf setRestart(String restart) {
        this.restart = restart;
        return this;
    }

    public String getStart() {
        return start;
    }

    public ServiceConf setStart(String start) {
        this.start = start;
        return this;
    }

    public String getStop() {
        return stop;
    }

    public ServiceConf setStop(String stop) {
        this.stop = stop;
        return this;
    }

    public String getPing() {
        return ping;
    }

    public ServiceConf setPing(String ping) {
        this.ping = ping;
        return this;
    }

    public String getZkPath() {
        return zkPath;
    }

    public ServiceConf setZkPath(String zkPath) {
        this.zkPath = zkPath;
        return this;
    }

    public Properties getZkData() {
        return zkData;
    }

    public ServiceConf setZkData(Properties zkData) {
        this.zkData = zkData;
        return this;
    }

    /**
     * @param prop
     * @return
     */
    public static ServiceConf load(Properties prop) {
        return new ServiceConf()
                .setId(prop.getProperty("id", "invalid"))
                .setPath(prop.getProperty("path", ""))
                .setHost(prop.getProperty("host", "localhost"))
                .setPort(prop.getProperty("port", "0"))
                .setRestart(prop.getProperty("restart", ""))
                .setStart(prop.getProperty("start", ""))
                .setStop(prop.getProperty("stop", ""))
                .setPing(prop.getProperty("ping", ""))
                .setZkPath(prop.getProperty("zkPath", ""))
                .setZkData(PropertyUtils.loadFromString(prop.getProperty("zkData", "")));
    }

    @Override
    public String toString() {
        return "{id:" + id + ",path:" + path + ",host:" + host + ",port:" + port + ",restart:" + restart + ",start:"
                + start + ",stop:" + stop + ",ping:" + ping + ",zkPath:" + zkPath + ",zkData:" + zkData + "}";
    }
}
