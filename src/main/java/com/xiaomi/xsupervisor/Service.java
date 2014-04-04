/**
 * Service.java
 * [CopyRight]
 * @author leo [liuy@xiaomi.com]
 * @date Aug 15, 2013 10:32:50 AM
 */
package com.xiaomi.xsupervisor;

import java.sql.Timestamp;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaomi.miliao.zookeeper.ZKClient;
import com.xiaomi.miliao.zookeeper.ZKFacade;
import com.xiaomi.xsupervisor.thrift.OperationResult;
import com.xiaomi.xsupervisor.thrift.OperationStatus;
import com.xiaomi.xsupervisor.utils.Shell;
import com.xiaomi.xsupervisor.utils.ShellResult;

/**
 * @author leo
 */
public class Service implements IService {
    private static final Logger LOGGER = LoggerFactory.getLogger(Service.class);

    public ServiceConf conf;
    public ServiceStatus status = ServiceStatus.Failed;
    public long startTs = -1;
    public int pid = -1;

    private ZKClient zkClient;

    public Service(ServiceConf conf) {
        LOGGER.info("init service of {}.", conf);
        this.conf = conf;
        zkClient = ZKFacade.getAbsolutePathClient();
    }

    public String getId() {
        return conf.getId();
    }

    @Override
    public OperationResult ping() {
        LOGGER.info("ping by {}.", conf.ping);
        ShellResult res = exec(conf.ping);
        LOGGER.debug("ping result: \n{}", res);
        if (res.success()) {
            return new OperationResult(OperationStatus.Ok, "ping ok with: " + conf.ping);
        } else {
            return new OperationResult(OperationStatus.Fail, "ping failed with: " + conf.ping);
        }
    }

    @Override
    public OperationResult restart() {
        if (!StringUtils.isBlank(conf.restart)) {
            LOGGER.info("restart by {}.", conf.restart);
            zkUnreg();
            ShellResult res = exec(conf.restart);
            LOGGER.debug("restart result: \n{}", res);
            startTs = System.currentTimeMillis();
            if (!res.success()) {
                return new OperationResult(OperationStatus.Fail, "restart failed with: " + conf.restart);
            }
            pid = res.pid;
            status = ServiceStatus.Running;
            return zkReg();
        } else {
            LOGGER.info("restart by {} and then {}.", conf.start, conf.stop);
            stop();
            return start();
        }
    }

    @Override
    public OperationResult start() {
        LOGGER.info("start by {} and then zkReg().", conf.start);
        startTs = System.currentTimeMillis();
        ShellResult res = exec(conf.start);
        LOGGER.debug("start result: \n{}", res);
        if (!res.success()) {
            return new OperationResult(OperationStatus.Fail, "start failed with: " + conf.start);
        }
        pid = res.pid;
        status = ServiceStatus.Running;
        return zkReg();
    }

    @Override
    public OperationResult stop() {
        LOGGER.info("stop by {} and then zkUnreg().", conf.stop);
        zkUnreg();
        ShellResult res = exec(conf.stop);
        LOGGER.debug("stop result: \n{}", res);
        if (!res.success()) {
            return new OperationResult(OperationStatus.Fail, "stop failed with: " + conf.stop);
        }
        status = ServiceStatus.Stopped;
        return new OperationResult(OperationStatus.Ok, "stop ok with: " + conf.stop);
    }

    @Override
    public OperationResult zkReg() {
        LOGGER.info("zkReg of path {} with data:\n {}.", conf.zkPath, conf.zkData);
        String msg = " of path " + conf.zkPath + " with data:\n " + conf.zkData + ".";
        OperationResult r = null;
        try {
            zkClient.createPersistent(conf.zkPath, true, conf.zkData);
            r = new OperationResult(OperationStatus.Ok, "register to zk ok " + msg);
        } catch (Exception ex) {
            LOGGER.error("zkReg exception of path " + conf.zkPath + " with data:\n " + conf.zkData + ".", ex);
            r = new OperationResult(OperationStatus.Fail, "register to zk fail " + msg + ", ex: " + ex.getMessage());
        }
        LOGGER.debug("zkreg result: \n{}", r);
        return r;
    }

    @Override
    public OperationResult zkUnreg() {
        LOGGER.info("zkUnreg of path {} .", conf.zkPath);
        OperationResult r = null;
        try {
            boolean ok = zkClient.delete(conf.zkPath);
            if (ok) {
                r = new OperationResult(OperationStatus.Ok, "unregister zk path ok at: " + conf.zkPath);
            } else {
                r = new OperationResult(OperationStatus.Fail, "unregister zk path fail at: " + conf.zkPath);
            }
        } catch (Exception ex) {
            LOGGER.error("zkUnreg exception of path " + conf.zkPath + ".", ex);
            r = new OperationResult(OperationStatus.Fail, "unregister zk path fail at: " + conf.zkPath + ", ex: " + ex.getMessage());
        }
        LOGGER.debug("zkunreg result: \n{}", r);
        return r;
    }

    ShellResult exec(String cmd) {
        return Shell.exec(cmd);
    }

    @Override
    public String toString() {
        return status() + " | conf: " + conf.path;
    }

    @Override
    public OperationResult status() {
        // compute readable time interval.
        int y = 0, d = 0, h = 0, m = 0, s = 0;
        s = (int) ((System.currentTimeMillis() - startTs) / 1000);
        if (s > 60) {
            m = s / 60;
            s = s % 60;
            if (m > 60) {
                h = m / 60;
                m = m % 60;
                if (h > 24) {
                    d = h / 24;
                    h = h % 24;
                    if (d > 365) {
                        y = d / 365;
                        d = d % 365;
                    }
                }
            }
        }
        // format output.
        String res;
        if (y > 0) {
            String fmt = "%-16s %10s    pid %-6d, uptime %d years, %d days, %0$02dh:%0$02dm:%0$02ds";
            res = String.format(fmt, conf.id, status.toString(), pid, y, d, h, m, s);
        } else if (d > 0) {
            String fmt = "%-16s %10s    pid %-6d, uptime %d days, %0$02dh:%0$02dm:%0$02ds";
            res = String.format(fmt, conf.id, status.toString(), pid, d, h, m, s);
        } else {
            String fmt = "%-16s %10s    pid %-6d, uptime %0$02dh:%0$02dm:%0$02ds";
            res = String.format(fmt, conf.id, status.toString(), pid, h, m, s);
        }
        return new OperationResult(OperationStatus.Ok, res);
    }

    public static void main(String[] args) {
        System.out.println(new Timestamp(1376541990820l));
    }
}
