/**
 * Autogenerated by Thrift
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 */
package com.xiaomi.xsupervisor.thrift;


import java.util.Map;
import java.util.HashMap;
import org.apache.thrift.TEnum;

public enum Cmd implements TEnum {
  add(0),
  drop(1),
  status(2),
  stop(3),
  start(4),
  restart(5),
  help(6),
  boot(7),
  reboot(8),
  shutdown(9),
  reload(10);

  private final int value;

  private Cmd(int value) {
    this.value = value;
  }

  /**
   * Get the integer value of this enum value, as defined in the Thrift IDL.
   */
  public int getValue() {
    return value;
  }

  /**
   * Find a the enum type by its integer value, as defined in the Thrift IDL.
   * @return null if the value is not found.
   */
  public static Cmd findByValue(int value) { 
    switch (value) {
      case 0:
        return add;
      case 1:
        return drop;
      case 2:
        return status;
      case 3:
        return stop;
      case 4:
        return start;
      case 5:
        return restart;
      case 6:
        return help;
      case 7:
        return boot;
      case 8:
        return reboot;
      case 9:
        return shutdown;
      case 10:
        return reload;
      default:
        return null;
    }
  }
}
