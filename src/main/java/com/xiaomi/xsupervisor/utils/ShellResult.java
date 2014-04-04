/**
 * ShellResult.java
 * [CopyRight]
 * @author leo [liuy@xiaomi.com]
 * @date Aug 15, 2013 3:49:38 PM
 */
package com.xiaomi.xsupervisor.utils;

/**
 * @author leo
 */
public class ShellResult {
    public int code = -1;
    public int pid = -1;
    public String cmd = "";
    public String result = "";
    public String error = "";

    public boolean success() {
        return code == 0;
    }

    public int getCode() {
        return code;
    }

    public ShellResult setCode(int code) {
        this.code = code;
        return this;
    }

    public int getPid() {
        return pid;
    }

    public ShellResult setPid(int pid) {
        this.pid = pid;
        return this;
    }

    public String getCmd() {
        return cmd;
    }

    public ShellResult setCmd(String cmd) {
        this.cmd = cmd;
        return this;
    }

    public String getResult() {
        return result;
    }

    public ShellResult setResult(String result) {
        this.result = result;
        return this;
    }

    public String getError() {
        return error;
    }

    public ShellResult setError(String error) {
        this.error = error;
        return this;
    }

    public String toString() {
        return "cmd:\t" + cmd + "\ncode:\t" + code + "\npid:\t" + pid + "\nresult:\t" + result + "\nerror:\t" + error;
    }
}
