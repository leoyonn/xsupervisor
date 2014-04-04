/**
 * IService.java
 * [CopyRight]
 * @author leo [liuy@xiaomi.com]
 * @date Aug 15, 2013 10:33:03 AM
 */
package com.xiaomi.xsupervisor;

import com.xiaomi.xsupervisor.thrift.OperationResult;

/**
 * @author leo
 */
public interface IService {
    OperationResult ping();

    OperationResult restart();

    OperationResult start();

    OperationResult stop();
    
    OperationResult zkReg();

    OperationResult zkUnreg();
    
    OperationResult status();
}
