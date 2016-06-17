package es.bsc.demiurge.renewit.ganglia;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.log4j.LogManager;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GangliaAdapter {

    private static org.apache.log4j.Logger log = LogManager.getLogger(GangliaAdapter.class);
    private String monitoringPath;

    public GangliaAdapter(String monitoringPath) {
        this.monitoringPath = monitoringPath;
    }


    public static Double getHostGangliaMetrics(String server, long timestampStart, long timestampEnd){

        String cluster = "testing-cloud";
        String url = "https://bscgrid28.bsc.es/ganglia2/api/query_json.php";

        String json;
        List<GangliaSchema> schema;
        List<GangliaSchema> allMetricsSchema = new ArrayList<GangliaSchema>();

        System.setProperty("jsse.enableSNIExtension", "false");


        // Get server type (amd, intel, etc..)
        String metric = "powerWatts";
            try {
                String surl =  url +"?&cluster=" + cluster + "&host=" + server + "&metric="+ metric +"&start="+ timestampStart +"&end="+ timestampEnd;
                //System.out.println(surl);
                json = readGangliaUrl(surl);
                Gson gson = new Gson();
                Type gangliaSchemaList = new TypeToken<Collection<GangliaSchema>>() {}.getType();
                schema = gson.fromJson(json, gangliaSchemaList);

                if (schema.get(0).getDatapoints().get(1).getValue().equals("NaN")){
                    //System.out.println(server + ": NaN");
                    return Double.parseDouble(schema.get(0).getDatapoints().get(1).getValue());
                }else {
                    return Double.parseDouble(schema.get(0).getDatapoints().get(0).getValue());
                }
                //allMetricsSchema.add(schema.get(0));
            } catch (Exception e) {
                log.error("Metric " + metric + " not found: " + e.getMessage());
            }
        return 0d;
    }

    private boolean saveToCsv(String s, String filename, boolean append){
        try{

            FileWriter fw = new FileWriter(filename, append);
            PrintWriter out = new PrintWriter(new BufferedWriter(fw));
            out.println(s);
            out.close();
            return true;

        }catch (FileNotFoundException fe){
            log.error("Permission denied for: " + filename);
            return false;
        } catch (IOException e) {
            log.error(e.toString());
            return false;
        }

    }


    private static String readGangliaUrl(String urlString) throws Exception {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read);

            return buffer.toString();
        } finally {
            if (reader != null)
                reader.close();
        }
    }



}
