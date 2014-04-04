/**
 * Executor.java
 * [CopyRight]
 * @author leo [liuy@xiaomi.com]
 * @date Aug 6, 2013 1:44:23 PM
 */
package com.xiaomi.xsupervisor;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TException;

import com.xiaomi.miliao.thrift.ClientFactory;
import com.xiaomi.miliao.thrift.PerfCounterVal;
import com.xiaomi.xsupervisor.thrift.Cmd;
import com.xiaomi.xsupervisor.thrift.OperationResult;
import com.xiaomi.xsupervisor.thrift.OperationStatus;
import com.xiaomi.xsupervisor.thrift.XsupervisorServer;
import com.xiaomi.xsupervisor.utils.Shell;
import com.xiaomi.xsupervisor.utils.ShellResult;

/**
 * tool executor
 * 
 * @author leo
 */
public class XsupervisorCtl implements XsupervisorServer.Iface {

    /**
     * call cmd
     * 
     * @author leo
     */
    private abstract class CmdCaller {
        public CmdCaller(Cmd cmd) {
        }

        public boolean call(String[] args) {
            try {
                return doCall(args);
            } catch (Exception ex) {
                out.print(ex.getMessage());
                return false;
            }
        }

        protected abstract boolean doCall(String[] args) throws Exception;
    }

    private final Map<Cmd, CmdCaller> callers = new HashMap<Cmd, CmdCaller>() {
        private static final long serialVersionUID = 1L;
        {
            put(Cmd.add, new CmdCaller(Cmd.add) {
                @Override
                protected boolean doCall(String[] args) throws Exception {
                    if (args.length < 1) {
                        help();
                        return false;
                    }
                    return OperationStatus.Ok == add(args[0]).status;
                }
            });
            put(Cmd.drop, new CmdCaller(Cmd.drop) {
                @Override
                protected boolean doCall(String[] args) throws Exception {
                    if (args.length < 1) {
                        help();
                        return false;
                    }
                    return OperationStatus.Ok == drop(args[0]).status;
                }
            });
            put(Cmd.restart, new CmdCaller(Cmd.restart) {
                @Override
                public boolean doCall(String[] args) throws Exception {
                    if (args.length < 1) {
                        help();
                        return false;
                    }
                    return OperationStatus.Ok == restart(args[0]).status;
                }
            });
            put(Cmd.start, new CmdCaller(Cmd.start) {
                @Override
                public boolean doCall(String[] args) throws Exception {
                    if (args.length < 1) {
                        help();
                        return false;
                    }
                    return OperationStatus.Ok == start(args[0]).status;
                }
            });
            put(Cmd.stop, new CmdCaller(Cmd.stop) {
                @Override
                public boolean doCall(String[] args) throws Exception {
                    if (args.length < 1) {
                        help();
                        return false;
                    }
                    return OperationStatus.Ok == stop(args[0]).status;
                }
            });
            put(Cmd.status, new CmdCaller(Cmd.status) {
                @Override
                public boolean doCall(String[] args) throws Exception {
                    return OperationStatus.Ok == status().status;
                }
            });
            put(Cmd.boot, new CmdCaller(Cmd.boot) {
                @Override
                public boolean doCall(String[] args) throws Exception {
                    startServer();
                    return OperationStatus.Ok == reload().status;
                }
            });
            put(Cmd.reboot, new CmdCaller(Cmd.reboot) {
                @Override
                public boolean doCall(String[] args) throws Exception {
                    xreboot();
                    killServer();
                    startServer();
                    return OperationStatus.Ok == reload().status;
                }
            });
            put(Cmd.shutdown, new CmdCaller(Cmd.shutdown) {
                @Override
                public boolean doCall(String[] args) throws Exception {
                    boolean ok = OperationStatus.Ok == xshutdown().status;
                    killServer();
                    return ok;
                }
            });
            put(Cmd.reload, new CmdCaller(Cmd.reload) {
                @Override
                public boolean doCall(String[] args) throws Exception {
                    return OperationStatus.Ok == reload().status;
                }
            });
            put(Cmd.help, new CmdCaller(Cmd.help) {
                @Override
                public boolean doCall(String[] args) throws Exception {
                    return OperationStatus.Ok == help().status;
                }
            });
        }
    };

    /**
     * Executes the job.
     * 
     * @param args the arguments from the console
     * @param out the output PrintWriter
     * @return the return value for the application
     */
    public boolean exec(String[] args, PrintWriter out) {
        this.out = out;
        // get tool's cmd
        Cmd cmd = null;
        try {
            if (args == null || args.length == 0 || args[0].equals("help")) {
                help();
                return false;
            }
            try {
                cmd = Cmd.valueOf(args[0]);
            } catch (Exception ex) {} // fall through
            if (cmd == null) {
                out.println(args[0] + " is not a legal command.");
                help();
                return false;
            }
        } catch (TException ex) {
            out.print(ex.getMessage());
            return false;
        }
        args = Arrays.copyOfRange(args, 1, args.length);
        return callers.get(cmd).call(args);
    }

    public static void main(String[] args) {
        XsupervisorCtl exec = new XsupervisorCtl();
        PrintWriter out = new PrintWriter(System.out, true);
        exec.exec(args, out);
        System.exit(0);
    }

    /**********************************************************************************************/
    private PrintWriter out;
    private XsupervisorServer.Iface client = ClientFactory.getClient(XsupervisorServer.Iface.class);

    @Override
    public OperationResult add(String confFile) throws TException {
        out.println("adding service from conf: " + confFile + " to service list...");
        OperationResult r = client.add(confFile);
        out.println(r.result);
        return r;
    }

    @Override
    public OperationResult drop(String serviceId) throws TException {
        out.println("dropping " + serviceId + " from service list...");
        OperationResult r =  client.drop(serviceId);
        out.println(r.result);
        return r;
    }

    @Override
    public OperationResult status() throws TException {
        out.println("getting status of all services...");
        OperationResult r = client.status();
        out.println(r.result);
        return r;
    }

    @Override
    public OperationResult stop(String serviceId) throws TException {
        out.println("stopping " + serviceId + "...");
        OperationResult r =  client.stop(serviceId);
        out.println(r.result);
        return r;
    }

    @Override
    public OperationResult start(String serviceId) throws TException {
        out.println("starting " + serviceId + "...");
        OperationResult r =  client.start(serviceId);
        out.println(r.result);
        return r;
    }

    @Override
    public OperationResult restart(String serviceId) throws TException {
        out.println("restarting " + serviceId + "...");
        OperationResult r =  client.restart(serviceId);
        out.println(r.result);
        return r;
    }

    @Override
    public OperationResult help() throws TException {
        OperationResult r =  client.help();
        out.println(r.result);
        return r;
    }

    @Override
    public OperationResult xreboot() throws TException {
        out.println("rebooting xsupervisor-server...");
        OperationResult r =  client.xreboot();
        out.println(r.result);
        return r;
    }

    @Override
    public OperationResult xshutdown() throws TException {
        out.println("rebooting xsupervisor-server...");
        OperationResult r =  client.xshutdown();
        out.println(r.result);
        return r;
    }

    @Override
    public OperationResult reload() throws TException {
        out.println("reloading xsupervisor services...");
        OperationResult r =  client.reload();
        out.println(r.result);
        return r;
    }

    protected void killServer() {
        out.println("killing xsupervisor-sever...");
        ShellResult r = Shell.kill("com.xiaomi.xsupervisor.XsupervisorMain");
        out.println(r.success() ? "success" : "failed");
    }
    
    protected void startServer() {
        out.println("starting xsupervisor-sever...");
        ShellResult r = Shell.exec("./xsupervisor-server");
        out.println(r.success() ? "success" : "failed");
    }

    /**********************************************************************************************/
    @Deprecated
    @Override
    public String getName() throws TException {
        throw new RuntimeException("should not call this.");
    }

    @Deprecated
    @Override
    public Map<String, Long> getCounters() throws TException {
        throw new RuntimeException("should not call this.");
    }

    @Deprecated
    @Override
    public Map<String, Long> getCountersByCategory(String prefix) throws TException {
        throw new RuntimeException("should not call this.");
    }

    @Deprecated
    @Override
    public List<String> getCounterNames() throws TException {
        throw new RuntimeException("should not call this.");
    }

    @Deprecated
    @Override
    public long getCounter(String key) throws TException {
        throw new RuntimeException("should not call this.");
    }

    @Deprecated
    @Override
    public long aliveSince() throws TException {
        throw new RuntimeException("should not call this.");
    }

    @Deprecated
    @Override
    public void shutdown() throws TException {
        throw new RuntimeException("should not call this.");
    }

    @Deprecated
    @Override
    public void setLogLevel(String name, String level) throws TException {
        throw new RuntimeException("should not call this.");
    }

    @Deprecated
    @Override
    public Map<String, PerfCounterVal> getPerfCounters() throws TException {
        throw new RuntimeException("should not call this.");
    }
}