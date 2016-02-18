package es.bsc.demiurge.cloudsuiteperformancedriver.cloud_suite_cloud.boot_scripts;

import es.bsc.demiurge.cloudsuiteperformancedriver.models.VmSize;

import java.util.HashMap;
import java.util.Map;

public class WebServingScriptGenerator {

    public static Map<String, String> generateScripts(VmSize vmSize) {
        Map<String, String> result = new HashMap<>();
        result.put("client", generateClientVmScript());
        result.put("frontend", generateFrontendVmScript());
        result.put("backend", generateBackendVmScript());
        return result;
    }

    private static String generateBackendVmScript() {
        // TODO
        return null;
    }

    private static String generateFrontendVmScript() {
        // TODO
        return null;
    }

    private static String generateClientVmScript() {
        // TODO
        return null;
    }

}
