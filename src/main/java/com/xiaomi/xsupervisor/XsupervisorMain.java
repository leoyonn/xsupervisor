/**
 * XmPushAckMain.java
 * [CopyRight]
 * @author leo [liuy@xiaomi.com]
 * @author chenqiliang
 * @date Jun 3, 2013 5:26:22 PM
 */
package com.xiaomi.xsupervisor;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaomi.miliao.thrift.ThriftServiceRunner;
import com.xiaomi.miliao.thrift.ThriftServiceStartupConfig;
import com.xiaomi.miliao.utils.PropertyUtil;
import com.xiaomi.miliao.zookeeper.ZKClient;
import com.xiaomi.miliao.zookeeper.ZKFacade;
import com.xiaomi.xsupervisor.thrift.XsupervisorServer;

/**
 * @author leo
 */
public class XsupervisorMain {
    private static final Logger LOGGER = LoggerFactory.getLogger(XsupervisorMain.class);
    private static final String PORT = "com.xiaomi.xsupervisor.thrift.port";
    private static final String DEFAULT_PORT = "33233";
    private static final String POOL = "com.xiaomi.xsupervisor.thrift.pool";
    private static final String SERVICE = XsupervisorServer.class.getName();
    private static final String ZK_RES = "/" + SERVICE + ".properties";
    private static final String ZK_PATH = "/services/" + SERVICE + "/Pool/";
    private static final PropertyUtil propertyUtil = new PropertyUtil();
    
    public static void main(String[] args) throws Throwable {
        LOGGER.info("XsupervisorMain start with args:\n{}", args);
        
        // delete old zk if exists
        ThriftServiceStartupConfig tconf = ThriftServiceStartupConfig.parseArgument(args);
        Properties prop = propertyUtil.getPropertiesFromResource(ThriftServiceRunner.class, ZK_RES);
        String ip = tconf.getLocalIpAddr();
        String port = prop.getProperty(PORT, DEFAULT_PORT);

        final ZKClient zkClient = ZKFacade.getAbsolutePathClient();
        final String zkPath = ZK_PATH + ip + ":" + port;
        zkClient.delete(zkPath);

        // run thrift
        ThriftServiceRunner.startThriftServer(tconf, XsupervisorServerImpl.class, PORT, POOL, (XsupervisorServerImpl) null);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    Thread.currentThread().setName("xsupervisor");
                    LOGGER.info("xsupervisor exited gracefully!");
                    zkClient.delete(zkPath);
                } catch (Throwable ex) {
                    LOGGER.error("xsupervisor exception during exit", ex);
                }
            }
        });
    }
}
