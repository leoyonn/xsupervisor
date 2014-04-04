xsupervisor
===========

主要功能：
1. 管理那些无法自己注册/解除注册到zookeeper上的服务，如redis等；
2. 根据配置定时ping所管理的服务；
3. 如果服务ping不通，先将其从zookeeper上摘除，再尝试将其重启，重启成功后再添加到zookeeper上；
4. 提供如supervisor一样的丰富shell，例如：
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

