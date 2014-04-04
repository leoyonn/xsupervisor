#!/bin/bash
SUPERBAR="=======================================================================================================\n|| "
function say() {
	echo -e "$(basename $0) | ${SUPERBAR} $@"
}

function getLocalhost() {
	localhost=("localhost")
	localhost=("${localhost} "`hostname --fqdn`)
	localhost=("${localhost} `/sbin/ifconfig -a|grep inet | grep -v inet6|awk '{print $2}'|tr -d 'addr:'`")
}

if [ $# -lt 2 ]; then
	say "usage: ./release.sh env hosts\n|| "\
		"(such as: './release.sh 10.237.12.18,10.237.12.17'\n||  "\
		"- 'env' can be one of 'onebox/staging/sandbox/production'\n||  "\
		"- 'hosts' can be list of ip's, split by ',')\n||"
	exit
fi
env=$1
shift
realEnv=${env}
if [ ${env} == "sandbox" ]; then
    realEnv="onebox"
fi

hosts=$1
hosts=(${hosts//,/ })
say "got env: '${env}', hosts: '${hosts[@]}'"

D="/opt/soft/xsupervisor/"
src=`pwd`

say "compiling xsupervisor..."
mvn -P${realEnv} -D${realEnv}=true clean package;
cp scripts/* target/;
if [ ${env} == "sandbox" ]; then
    cp target/classes/xmpush.sandbox.zk.properties target/classes/zookeeper_servers.properties
fi
getLocalhost

for host in ${hosts[@]} ; do

	[[ "${localhost[@]}" =~ "${host}" || "${localhost[${#localhost[@]}-1]}" == "${host}" ]];
	isRemote=$?
    echo "ip='${host}'" > target/ip.conf
	say "dispatching to host: ${host}, isRemote: ${isRemote} (localhosts: ${localhost[@]})"
	if [ ${isRemote} -eq 1 ]; then
        ssh root@${host} "rm -rf ${D}*; mkdir -p $D; mkdir -p /data/soft/xsupervisor/logs";
        sleep 1
        scp -r ${src}/target/* root@${host}:${D};
        ssh root@${host} " cd $D; chmod +x xsupervisor-server xsupervisorctl; ./xsupervisor-server";
    else
        source /etc/profile; rm -rf ${D}*; mkdir -p $D; mkdir -p /data/soft/xsupervisor/logs;
        chmod +x xsupervisor-server xsupervisorctl;
        cp -r ${src}/target/* $D;
        cd $D;
        ./xsupervisor-server;
	fi
	say "complete releasing xsupervisor to ${host}!"
done

say "complete releasing xsupervisor to all hosts ${hosts}!"

