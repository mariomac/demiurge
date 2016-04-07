package es.bsc.autonomicbenchmarks.benchmarks.scriptgenerators;

public class WebServingScriptGenerator {

    private static final String END_OF_LINE = System.getProperty("line.separator");
    private static final String RUN_CONFIG_FILE = "/home/ubuntu/faban/config/profiles/run.xml.OlioDriver";
    
    public String generateClientScript(int clients) {
        return "#cloud-config" + END_OF_LINE
                + "password: ubuntu" + END_OF_LINE
                + "chpasswd: { expire: False }" + END_OF_LINE
                + "ssh_pwauth: True" + END_OF_LINE
                + "runcmd:" + END_OF_LINE
                + getSshDirCommand() + END_OF_LINE
                + getSetNumberOfClientsCommand(clients) + END_OF_LINE
                + getRemoveKnownHostsCommand() + END_OF_LINE
                + " - sleep 120" + END_OF_LINE // it has to wait for the other VMs
     //           + getSshKeygenCommand() + END_OF_LINE
      //          + getSetFabanHomeCommand() + END_OF_LINE
                //+ getSetJavaHomeCommand() + END_OF_LINE
                //+ getStartFabanCommand() + END_OF_LINE
                + " - sleep 10" + END_OF_LINE // VERY IMPORTANT!!
                + generatePrintTimestampStartCommand() + END_OF_LINE
                //+ getRunExperimentCommand() + END_OF_LINE
                //+ " - sleep 900" + END_OF_LINE
                //+ getPrintResultsCommand() + END_OF_LINE;
                + getSudoNoPassword() + END_OF_LINE;
    }

    public String generateFrontendScript() {
        return "#cloud-config" + END_OF_LINE
                + "password: ubuntu" + END_OF_LINE
                + "chpasswd: { expire: False }" + END_OF_LINE
                + "ssh_pwauth: True" + END_OF_LINE
                + "runcmd:" + END_OF_LINE
                + getRemoveKnownHostsCommand() + END_OF_LINE
                + " - sleep 60" + END_OF_LINE // it has to wait for the other VMs
                + getSshDirCommand() + END_OF_LINE
        //        + getSshKeygenCommand() + END_OF_LINE
                + getStartNginxCommand() + END_OF_LINE
                + getStartPhpFpmCommand() + END_OF_LINE
                + getSudoNoPassword() + END_OF_LINE;
    }


    public String generateBackendScript() {
        return "#cloud-config" + END_OF_LINE
                + "password: ubuntu" + END_OF_LINE
                + "chpasswd: { expire: False }" + END_OF_LINE
                + "ssh_pwauth: True" + END_OF_LINE
                + "runcmd:" + END_OF_LINE
                + getRemoveKnownHostsCommand() + END_OF_LINE
                //+ " - sleep 120" + END_OF_LINE // does not have to wait, because it is the last to be deployed
       //         + getSshKeygenCommand() + END_OF_LINE
                + getSshDirCommand() + END_OF_LINE
                + getSetMysqlHomeCommand() + END_OF_LINE
                + getSetCatalinaHomeCommand() + END_OF_LINE
                + getStartMysqlCommand() + END_OF_LINE
                + getStartTomcatCommand() + END_OF_LINE 
                + generatePrintTimestampStartCommand() + END_OF_LINE
                + getSudoNoPassword() + END_OF_LINE;
    }

    private String getSetNumberOfClientsCommand(int clients) {
        return " - sed -i.bak -e '19d' " + RUN_CONFIG_FILE + " " + END_OF_LINE
                + " - sed -i.bak '18 a\\<fa:scale>" + clients + "</fa:scale>" + "' " + RUN_CONFIG_FILE + END_OF_LINE
                + " - sed -i.bak -e '91d' " + RUN_CONFIG_FILE + " " + END_OF_LINE
                + " - sed -i.bak '90 a\\<scale>" + clients + "</scale>" + "' " + RUN_CONFIG_FILE;
    }
    
    private String getPrintResultsCommand() {
        return " - cat /home/ubuntu/faban/output/OlioDriver.2B/summary.xml";
    }

    private String getStartTomcatCommand() {
        return " - $CATALINA_HOME/bin/startup.sh";
    }

    private String getStartMysqlCommand() {
        return " - cd /home/ubuntu/web-release/mysql-5.5.20-linux2.6-x86_64" + END_OF_LINE
                + " - ./bin/mysqld_safe --defaults-file=/etc/my.cnf --basedir=$MYSQL_HOME " + 
                "--user=mysql --datadir=$MYSQL_HOME/data &> a.txt" + END_OF_LINE
                + " - cd $HOME";
    }

    private String getSetCatalinaHomeCommand() {
        return " - export CATALINA_HOME=/home/ubuntu/web-release/apache-tomcat-6.0.35";
    }

    private String getSetMysqlHomeCommand() {
        return " - export MYSQL_HOME=/home/ubuntu/web-release/mysql-5.5.20-linux2.6-x86_64";
    }

    private String getRunExperimentCommand() {
        return " - /home/ubuntu/faban/bin/fabancli submit OlioDriver default " +
                "/home/ubuntu/faban/config/profiles/run.xml.OlioDriver";
    }

    private String getStartFabanCommand() {
        return " - $FABAN_HOME/master/bin/startup.sh";
    }

    private String getSetJavaHomeCommand() {
        return " - export JAVA_HOME=/usr/lib/jvm/java-6-openjdk-amd64";
    }

    private String getSetFabanHomeCommand() {
        //return " - export FABAN_HOME=/home/ubuntu/faban";
        // change ip addresses
        return " - sed -i 's/172.16.8.151/172.16.8.159/g' /home/ubuntu/faban/config/profiles/run.xml.OlioDriver" + END_OF_LINE
                + " - sed -i 's/172.16.8.152/172.16.8.163/g' /home/ubuntu/faban/config/profiles/run.xml.OlioDriver" + END_OF_LINE
                + " - sed -i 's/172.16.8.154/172.16.8.168/g' /home/ubuntu/faban/config/profiles/run.xml.OlioDriver" + END_OF_LINE
                +  " - sed -i 's/172.16.8.151/172.16.8.159/g' /etc/hosts" + END_OF_LINE
                + " - sed -i 's/172.16.8.152/172.16.8.163/g' /etc/hosts" + END_OF_LINE
                + " - sed -i 's/172.16.8.154/172.16.8.168/g' /etc/hosts";
    }

    private String getSshKeygenCommand() {
        return " - ssh-keyscan 172.16.8.159 >> /root/.ssh/known_hosts" + END_OF_LINE
                + " - ssh-keyscan 172.16.8.163 >> /root/.ssh/known_hosts" + END_OF_LINE
                + " - ssh-keyscan 172.16.8.168 >> /root/.ssh/known_hosts" + END_OF_LINE
                + " - ssh-keyscan cloud-suite-web-serving-client >> /root/.ssh/known_hosts" + END_OF_LINE
                + " - ssh-keyscan cloud-suite-web-serving-frontend >> /root/.ssh/known_hosts" + END_OF_LINE
                + " - ssh-keyscan cloud-suite-web-serving-backend >> /root/.ssh/known_hosts";
    }
    private String getSshDirCommand() {
        return " - mkdir -p /root/.ssh";
    }

    private String getRemoveKnownHostsCommand() {
        return " - rm /home/ubuntu/.ssh/known_hosts" + END_OF_LINE
                + " - rm /root/.ssh/known_hosts";
    }

    private String getStartPhpFpmCommand() {
        return " - /usr/local/sbin/php-fpm";
    }

    private String getStartNginxCommand() {
        return " - /usr/local/nginx/sbin/nginx";
    }

    private String generatePrintTimestampStartCommand() {
        return " - echo \"timestamp_start:$(date +%s)\"";
    }

    private String getSudoNoPassword() {
        return " - echo 'ubuntu ALL=(ALL) NOPASSWD:ALL' >> /etc/sudoers";
    }

}
