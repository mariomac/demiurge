#!/bin/sh
# Vmmanager Startup Script

# How to install this init script
# sudo -i
# chmod +x /home/ubuntu/vmmanager/vmmanager.sh
# ln -s /home/ubuntu/vmmanager/vmmanager.sh /etc/init.d/vmmanager
# update-rc.d vmmanager defaults

start() {
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=54321 -jar /home/ubuntu/vmmanager/uber-vmmanagercore-0.0.1-SNAPSHOT.jar 2>&1 > /var/log/vmmanager.log &
}
stop() {
kill `ps -ef | grep uber-vmmanagercore | grep -v grep | awk '{print $2}'`
}

# See how we were called.
case "$1" in
  start)
        start
        ;;
  stop)
        stop
        ;;
  restart)
        stop
        start
        ;;
  *)
        echo $"Usage: service vmmanager {start|stop|restart}"
        exit
esac