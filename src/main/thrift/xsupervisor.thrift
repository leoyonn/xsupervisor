/**
 * Thrift interface for Xsupervisor
 *
 * [CopyRight]
 * @author leo [liuy@xiaomi.com]
 * @date Aug 13, 2013 8:12:29 PM
 */
namespace java com.xiaomi.xsupervisor.thrift

include "miliao_shared.thrift"

/**
 * status of operation.
 */
enum OperationStatus {
    Ok,
    Fail, 
}

enum Cmd {
    add, drop, status, stop, start, restart, help, boot, reboot, shutdown, reload,
}

struct OperationResult {
    1: OperationStatus status,
    2: string result
}

/**
 * thrift service of xsupervisor, such as add, status, restart,...
 */
service XsupervisorServer extends miliao_shared.MiliaoSharedService {

    /**
     * add a service described using confFile
     * @param confFile
     */
    OperationResult add(1:string confFile);
    
    /**
     * drop a service, which means no longer supervisor it. 
     * @param serviceId
     */
    OperationResult drop(1:string serviceId);

    /**
     * display all statuses of services. 
     */
    OperationResult status();

    /**
     * stop a service. 
     * @param serviceId
     */
    OperationResult stop(1:string serviceId);

    /**
     * start a serviceId
     */
    OperationResult start(1:string serviceId);

    /**
     * restart a serviceId
     */
    OperationResult restart(1:string serviceId);

    /**
     * display help information
     */
    OperationResult help();

    /**
     * boot monitor
     */
    OperationResult reload();

    /**
     * reboot monitor
     */
    OperationResult xreboot();

    /**
     * shutdown monitor
     */
    OperationResult xshutdown();
}
