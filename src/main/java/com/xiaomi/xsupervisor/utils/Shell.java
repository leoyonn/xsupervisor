/**
 * Shell.java
 * [CopyRight]
 * @author leo [liuy@xiaomi.com]
 * @date Aug 6, 2013 10:48:44 AM
 */
package com.xiaomi.xsupervisor.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author leo
 */
public class Shell {
    private static final Logger LOGGER = LoggerFactory.getLogger(Shell.class);

    /**
     * run a system command
     * 
     * @param cmd
     * @return
     * @throws IOException
     */
    public static ShellResult exec(String cmd) {
        ShellResult res = new ShellResult().setCmd(cmd);
        String[] cmds = {
            "/bin/sh", "-c", cmd + "; echo $?"
        };
        innerExec(cmds, res);
        res.pid = pid(cmd);
        return res;
    }

    public static int pid(String cmd) {
        String pidCmd = "ps ux | grep \"" + cmd + "\" | grep -v grep | awk '{print $2}'";
        ShellResult pidRes = new ShellResult().setCmd(pidCmd);
        String[] cmds = {
            "/bin/bash", "-c", pidCmd + "; echo $?"
        };
        innerExec(cmds, pidRes);
        try {
            return Integer.valueOf(pidRes.getResult().trim());
        } catch (Exception ex) {
            LOGGER.error("pid of " + cmd + " got exception: " + ex);
            return -1;
        }
    }

    public static ShellResult kill(String cmd) {
        int pid = pid(cmd);
        LOGGER.info("killing {}, pid: {}.", cmd, pid);
        return exec("kill " + pid);
        
    }

    private static void innerExec(String[] cmds, ShellResult res) {
        BufferedReader inReader = null;
        BufferedReader errReader = null;
        try {
            Process process = null;
            process = Runtime.getRuntime().exec(cmds);
            if (process != null) {
                inReader = new BufferedReader(new InputStreamReader(process.getInputStream()), 1024);
                errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()), 1024);
                process.waitFor();
            }
            String line = null, last = "";
            StringBuilder sb = new StringBuilder();
            while (inReader != null && (line = inReader.readLine()) != null) {
                last = line;
                sb.append(line).append("\n");
            }
            if (sb.length() - last.length() - 2 >= 0) {
                sb.setLength(sb.length() - last.length() - 2);
            }
            res.setCode(Integer.valueOf(last));
            res.setResult(sb.toString());
            sb.setLength(0);
            while (errReader != null && (line = errReader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            res.setError(sb.toString());
            sb.setLength(0);
        } catch (Exception ex) {
            res.setError(ex.getMessage());
        } finally {
            if (inReader != null) {
                try {
                    inReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (errReader != null) {
                try {
                    errReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static ShellResult mv(String src, String des) {
        return exec("mv " + src + " " + des);
    }

    public static ShellResult cp(String src, String des) {
        return exec("cp " + src + " " + des);
    }

    public static ShellResult rm(String file) {
        return exec("rm " + file);
    }

    public static ShellResult sort(String srcfile, String params) {
        return exec("sort " + params + " " + srcfile);
    }

    public static ShellResult mkdir(String dir) {
        return exec("mkdir " + dir);
    }

    public static void main(String[] args) throws IOException {
        System.out.println(Shell.exec("/opt/soft/twemproxy/nutcracker -d -c /opt/soft/conf/twemproxy/cluster1.yml -o /data/soft/twemproxy/twemproxy-cluster1.log"));
    }
}