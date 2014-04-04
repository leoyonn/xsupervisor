/**
 * XsupervisorServerImpl.java
 * [CopyRight]
 * @author leo [liuy@xiaomi.com]
 * @date Aug 13, 2013 8:12:29 PM
 */

package com.xiaomi.xsupervisor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaomi.miliao.thrift.MiliaoSharedServiceBase;
import com.xiaomi.miliao.utils.PropertyUtil;
import com.xiaomi.xsupervisor.thrift.Cmd;
import com.xiaomi.xsupervisor.thrift.OperationResult;
import com.xiaomi.xsupervisor.thrift.OperationStatus;
import com.xiaomi.xsupervisor.thrift.XsupervisorServer.Iface;

/**
 * @author leo
 */
public class XsupervisorServerImpl extends MiliaoSharedServiceBase implements Iface {
    private static final String SERVICES_DB = "/opt/soft/xsupervisor-services/services.conf";
    private static final Logger LOGGER = LoggerFactory.getLogger(XsupervisorServerImpl.class);
    private static final PropertyUtil propUtil = new PropertyUtil();
    private static final long PING_INTERVAL = 5 * 1000L;
    private final Map<String, Service> services = new ConcurrentHashMap<String, Service>();
    private ScheduledExecutorService sheduler = Executors.newScheduledThreadPool(1); 

    public XsupervisorServerImpl() {
        LOGGER.info("starting {}...");
        sheduler.scheduleWithFixedDelay(pinger, PING_INTERVAL, PING_INTERVAL, TimeUnit.MILLISECONDS);
    }

    /**
     * ping all services
     */
    private Runnable pinger = new Runnable() {
        @Override
        public void run() {
            try {
                LOGGER.info("pinging all {} services.", services.size());
                for (Map.Entry<String, Service> e : services.entrySet()) {
                    Service service = e.getValue();
                    if (service.status == ServiceStatus.Stopped) {
                        LOGGER.debug("skip stopped service: {}.", service);
                        continue;
                    }
                    OperationResult res = service.ping();
                    if (res.status != OperationStatus.Ok) {
                        LOGGER.error("Service {} not ok, restarting!", service);
                        try {
                            res = restart(e.getKey());
                            LOGGER.info("Service {} restart result:{}.", service, res);
                        } catch (TException ex1) {
                            LOGGER.error("Service " + service + " restart got exception!", ex1);
                        }
                    } else {
                        LOGGER.info("Service {} check ok.", service);
                    }
                }
            } catch (Exception ex) {
                LOGGER.error("Ping services got exception!", ex);
            }
        }
    };

    @Override
    public OperationResult add(String confFile) throws TException {
        LOGGER.info("add {}...", confFile);
        // get configure.
        Properties prop = null;
        try {
            prop = propUtil.getPropertiesFromFile(confFile);
        } catch (Exception ex) {
            LOGGER.error("read conf failed.", ex);
            return new OperationResult(OperationStatus.Fail, "conf file invalid:" + confFile + ", ex:" + ex.getMessage());
        }
        if (prop == null) {
            return new OperationResult(OperationStatus.Fail, "conf file invalid:" + confFile);
        }
        prop.put("path", confFile);
        ServiceConf conf = ServiceConf.load(prop);

        // start service.
        Service service = new Service(conf);
        OperationResult r = service.start();
        if (r.getStatus() == OperationStatus.Ok) {
            addService(service);
        }
        return r;
    }

    @Override
    public OperationResult drop(String serviceId) throws TException {
        LOGGER.info("drop {}...", serviceId);
        OperationResult r = stop(serviceId);
        if (r.status == OperationStatus.Ok) {
            dropService(serviceId);
        }
        return r;
    }

    @Override
    public OperationResult status() throws TException {
        LOGGER.info("status...");
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Service> e : services.entrySet()) {
            sb.append(e.getValue().status().result).append("\n");
        }
        if (sb.length() == 0) {
            sb.append("...\nseems no services at all...\n");
        }
        LOGGER.info("status:\n{}", sb);
        return new OperationResult(OperationStatus.Ok, sb.toString());
    }

    @Override
    public OperationResult stop(String serviceId) throws TException {
        LOGGER.info("stop {}...", serviceId);
        Service service = services.get(serviceId);
        if (service == null) {
            return new OperationResult(OperationStatus.Fail, "no suche service: " + serviceId + " -- all:\n" + services);
        }
        return service.stop();
    }

    @Override
    public OperationResult start(String serviceId) throws TException {
        LOGGER.info("start {}...", serviceId);
        Service service = services.get(serviceId);
        if (service == null) {
            return new OperationResult(OperationStatus.Fail, "no suche service: " + serviceId + " -- all:\n" + services);
        }
        return service.start();
    }

    @Override
    public OperationResult restart(String serviceId) throws TException {
        LOGGER.info("restart {}...", serviceId);
        Service service = services.get(serviceId);
        if (service == null) {
            return new OperationResult(OperationStatus.Fail, "no suche service: " + serviceId + " -- all:\n" + services);
        }
        return service.restart();
    }

    @Override
    public OperationResult help() throws TException {
        LOGGER.info("help...");
        StringBuilder sb = new StringBuilder();
        sb.append("Usage:\n")
            .append("  add conf-file\n")
            .append("  drop service\n")
            .append("  stop service\n")
            .append("  start service\n")
            .append("  restart service\n")
            .append("  status\n")
            .append("  boot\n")
            .append("  reboot\n")
            .append("  shutdown\n")
            .append("  reload\n")
            .append("  help\n");


        for (Cmd cmd : Cmd.values()) {
            sb.append("  ").append(cmd).append("\n");
        }
        return new OperationResult(OperationStatus.Ok, sb.toString());
    }

    @Override
    public OperationResult reload() throws TException {
        services.clear();
        boolean ok = loadServicesFromDisk();
        OperationResult r = status();
        return r.setStatus(ok ? OperationStatus.Ok: OperationStatus.Fail);
    }

    @Override
    public OperationResult xreboot() throws TException {
        LOGGER.info("reboot...");
        return xshutdown();
    }

    @Override
    public OperationResult xshutdown() throws TException {
        LOGGER.info("xshutdown...");
        StringBuilder sb = new StringBuilder();
        sb.append("shutdowning...\n");
        OperationStatus status = OperationStatus.Ok;
        for (Map.Entry<String, Service> e : services.entrySet()) {
            Service service = e.getValue();
            OperationResult r = service.stop();
            sb.append(service.getId()).append(":  \t")
                    .append(service.status).append("\t")
                    .append(r.status).append("|")
                    .append(r.result).append("\n");
            if (r.status != OperationStatus.Ok) {
                status = OperationStatus.Fail;
            }
        }
        sb.append("done shutdown...\n");
        services.clear();
        return new OperationResult(status, sb.toString());
    }

    @Deprecated
    @Override
    public void stop() {
        LOGGER.info("miliao.iface's stop not implemeted, use stop(xxx) or xreboot/xshutdown.");
    }

    synchronized private void addService(Service service) {
        services.put(service.getId(), service);
        dumpServicesToDisk();
    }

    synchronized private void dropService(String serviceId) {
        services.remove(serviceId);
        dumpServicesToDisk();
    }

    synchronized private void dumpServicesToDisk() {
        LOGGER.info("dump to disk of {}...", services);
        FileWriter fw = null;
        try {
            fw = new FileWriter(new File(SERVICES_DB));
            for (Map.Entry<String, Service> e : services.entrySet()) {
                fw.write(e.getKey() + "=" + e.getValue().conf.path + "\n");
            }
        } catch (IOException ex) {
            LOGGER.error("dump to disk " + SERVICES_DB + " of " + services + " got exception.", ex);
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException ex1) {
                    LOGGER.error("close file writer " + SERVICES_DB + " got exception.", ex1);
                }
                fw = null;
            }
        }
        LOGGER.info("dump to disk to {} done!", SERVICES_DB);
    }


    synchronized private boolean loadServicesFromDisk() {
        LOGGER.info("load services from disk {}...", SERVICES_DB);
        Properties prop = null;
        try {
            prop = propUtil.getPropertiesFromFile(SERVICES_DB);
        } catch (Exception ex) {
            LOGGER.error("load service properties from disk " + SERVICES_DB + "got exception.", ex);
        }
        if (prop == null) {
            LOGGER.error("load service properties from disk {} got nothing!", SERVICES_DB);
            return false;
        }

        boolean ok = true;
        for (Entry<Object, Object> e : prop.entrySet()) {
            try {
                OperationResult r = add((String) e.getValue());
                if (r.status != OperationStatus.Ok) {
                    ok = false;
                }
            } catch (TException ex) {
                ok = false;
            }
        }
        LOGGER.info("loaded {} from disk done!", services);
        return ok;
    }

}
