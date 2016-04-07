package es.bsc.autonomicbenchmarks.benchmarks.scriptgenerators;

public class NasMPIScriptGenerator {


    private static final String END_OF_LINE = System.getProperty("line.separator");

    public String generateScript() {
        return "#cloud-config" + END_OF_LINE
                + "password: bsc" + END_OF_LINE
                + "chpasswd: { expire: False }" + END_OF_LINE
                + "ssh_pwauth: True" + END_OF_LINE
                + "runcmd:" + END_OF_LINE
                + " - sleep 5" + END_OF_LINE
                + END_OF_LINE;
    }

}
