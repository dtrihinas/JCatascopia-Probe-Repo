import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.celarcloud.jcatascopia.probepack.Probe;
import eu.celarcloud.jcatascopia.probepack.ProbeMetric;
import eu.celarcloud.jcatascopia.probepack.ProbePropertyType;


public class CouchbaseProbe extends Probe {
	private static int DEFAULT_SAMPLING_PERIOD = 10;
	private static String DEFAULT_PROBE_NAME = "CouchbaseProbe";
	
	private static final String CONFIG_PATH = "couchbase.properties";
	private Properties config;
	
	private static String auth_header;
	private static String couchbase_stats_url;
	private String[] buckets; 

	
	public CouchbaseProbe(String name, int freq) {
		super(name, freq);

		this.addProbeProperty(0,"curr_connections",ProbePropertyType.INTEGER,"conns","Number of active connections");
		this.addProbeProperty(1,"curr_items",ProbePropertyType.INTEGER,"items","Number of items stored on node");
		this.addProbeProperty(2,"ops",ProbePropertyType.INTEGER,"ops","Operations per second");
		this.addProbeProperty(3,"couch_views_ops",ProbePropertyType.INTEGER,"ops","Operations per second on bucket views");
		this.addProbeProperty(4,"cache_miss_rate",ProbePropertyType.INTEGER,"%","percentage of reads to bucket from disk opposed to RAM");
		
		parseConfig();	   

		String user = config.getProperty("couchbase.username", "user");
	    String pass = config.getProperty("couchbase.password", "password");
	    
	    auth_header = "Basic " + new sun.misc.BASE64Encoder().encode((user+':'+pass).getBytes());
	    
	    String host = config.getProperty("couchbase.host", "localhost");
	    String port = config.getProperty("couchbase.port", "8091");
	    String bucket = config.getProperty("couchbase.buckets","default").split(",")[0]; //TODO add support for multiple buckets
	    
	    couchbase_stats_url = "http://"+host+":"+port+"/pools/default";
	    String hostname = this.findNodeHostname(couchbase_stats_url);
	    System.out.println("hostname in use by couchbase for this node is: " + hostname);
	    couchbase_stats_url = couchbase_stats_url+"/buckets/"+bucket+"/nodes/"+hostname+"/stats";
	}
	
	public CouchbaseProbe(){
		this(DEFAULT_PROBE_NAME, DEFAULT_SAMPLING_PERIOD);
	}
	
	@Override
	public String getDescription() {
		return "A JCatascopia Probe collecting usage stats from Couchbase document NoSQL store";
	}

	@Override
	public ProbeMetric collect() {
		HashMap<Integer,Object> values = new HashMap<Integer,Object>();
		int curr_connections = 0, ops = 0, curr_items = 0, couch_views_ops = 0, cache_miss_rate = 0;

		try{
			String resp = this.getURLResponse(couchbase_stats_url);
			resp = this.couchHack(resp); //json produced by couchbase is not valid since "ep_num_value_ejects" is a duplicate key -> produced workaround
						
			JSONObject json = new JSONObject(resp.toString());

//			System.out.println(json.toString(2));
			
			JSONObject samples = json.getJSONObject("op").getJSONObject("samples");
			curr_connections = samples.getJSONArray("curr_connections").getInt(0);
			curr_items = samples.getJSONArray("curr_items").getInt(0);
			ops = samples.getJSONArray("ops").getInt(0);
			couch_views_ops = samples.getJSONArray("couch_views_ops").getInt(0);
			cache_miss_rate = samples.getJSONArray("ep_cache_miss_rate").getInt(0);		
		}
		catch(Exception e){
			this.writeToProbeLog(Level.WARNING, e);
			e.printStackTrace();
			return null;
		}
		
		values.put(0, curr_connections);
		values.put(1, curr_items);
		values.put(2, ops);
		values.put(3, couch_views_ops);
		values.put(4, cache_miss_rate);	
		
//		System.out.println("curr_connections= "+curr_connections+", ops = "+ops+", curr_items = "+curr_items+", couch_view_ops= "+couch_views_ops+", cache_miss_rate= "+cache_miss_rate);
	
		return new ProbeMetric(values);
	}
	
	private String couchHack(String r){
		int hack = 0; 
		String[] temp = r.split("ep_num_value_ejects");
		String resp = "";
		for(String s : temp){
			s += (++hack);
			resp += s;
		}
		return resp;
	}
	
	private String getURLResponse(String url) { 
		String resp = null;
		try{	
			URL obj = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
			conn.setRequestMethod("GET");		
			conn.setRequestProperty("Authorization", auth_header);
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			StringBuilder sb = new StringBuilder();
			if(conn.getResponseCode() == 200){
				while ((line = in.readLine()) != null)
					sb.append(line);
				resp = sb.toString();
			}
					
			return resp;
		}
		catch(Exception e){
			this.writeToProbeLog(Level.WARNING, e);
			e.printStackTrace();
			return null;
		}		
	}
	
	private String findNodeHostname(String url){
		String hostname = null;
	
		String resp = this.getURLResponse(url);
		if (resp == null) 
			return hostname; //null
		
		try{
			JSONObject json = new JSONObject(resp.toString());
	//		System.out.println(json.toString(2));
			JSONArray nodes = json.getJSONArray("nodes");
			for(int i=0; i< nodes.length(); i++){
				JSONObject node = nodes.getJSONObject(i);
				if (!node.isNull("thisNode"))
					//found the node time to get hostname
					hostname = node.getString("hostname").replace(":", "%3A");
			}
		}
		catch(Exception e){
			this.writeToProbeLog(Level.WARNING, e);
			e.printStackTrace();
			return null;
		}		
		
		return hostname;
	}
	
	//parse the configuration file
	private void parseConfig(){
		this.config = new Properties();
		//load config properties file
		try {				
			InputStream fis = getClass().getResourceAsStream(CONFIG_PATH);
			config.load(fis);
			if (fis != null)
	    		fis.close();
		} 
		catch (FileNotFoundException e){
			this.writeToProbeLog(Level.SEVERE,"config file not found");
			e.printStackTrace();
		} 
		catch (IOException e){
			this.writeToProbeLog(Level.SEVERE,"config file parsing error");
			e.printStackTrace();
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CouchbaseProbe p = new CouchbaseProbe();
		p.activate();
	}

}
