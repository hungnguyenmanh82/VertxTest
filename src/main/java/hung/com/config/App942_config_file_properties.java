package hung.com.config;


import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
/**
 * 
https://vertx.io/docs/vertx-config/java/

By default, the Config Retriever is configured with the following stores (in this order):
The Vert.x verticle config()
The system properties
The environment variables
A conf/config.json file. This path can be overridden using the vertx-config-path system property or VERTX_CONFIG_PATH environment variable.
 *
 */

/**
 * system properties có thể add vào ở commandline hoặc từ source code java System.setProperty() :
 *   > java -Dvertx.hazelcast.config=./src/config/cluster.xml -jar ./target/vertx-docker-config-launcher.jar -cluster -conf ./src/config/local.json
 * 
 * System properties chỉ JavaApp add nó vào mới đọc đc 
 * Environment Variable add vào từ OS thì các app (process)  khác đều đọc đc.   
 */
public class App942_config_file_properties {
	public static void main(String[] args) throws InterruptedException{

		/**
		 * config đc lưu trong Vertx context.
		 * Verticle khác nhau có context khác nhau => config khác nhau.
		 */
		Vertx vertx = Vertx.vertx();

		/**
		 * lúc compile sẽ gộp "main/resources/" và "main/java/" vào 1 folder chung
		 App81_https_Server.class.getResource("/") = root = main/resources/ = main/java/
		 App81_https_Server.class.getResource("/abc") = main/resource/abc  = main/java/abc  
		 //
		 App81_https_Server.class.getResource("..") = root/pakage_name       => package_name của class này
		 App81_https_Server.class.getResource(".") = root/pakage_name/ 
		 App81_https_Server.class.getResource("abc") = root/pakage_name/abc
		 */

		/**
		 * "./" = là folder chạy CMD gọi tới java app ( thường là folder chưa *.jar file)
		 * config file thường đặt ở ngoài *.jar file
		 */
		ConfigStoreOptions configStoreOptions = new ConfigStoreOptions()
									.setType("file")          // lay config tu file
									.setFormat("properties")  //format của file là propertice (ko phải Json)
									.setConfig(new JsonObject()
													.put("path", "./src/config/config.properties")   //Debug F11 chạy ở Project folder
													.put("hierarchical", false)    //true or false
												);

		ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(configStoreOptions);

		ConfigRetriever retriever = ConfigRetriever.create(vertx,options);
		
		retriever.getConfig()		// = Future<JsonObject>
		.onSuccess((JsonObject config)->{
			//===========================================================================
			// hau het cac lib của Vertx đêu hỗ trợ options là JsonObject
			// vd: vertx options, http server option, verticle deploy option, threadpool option, circuit Breaker options...
			
			System.out.println(config.toString()); //
			System.out.println("server.host:"+ config.getString("server.host"));
		})
		.onFailure(thr->{
			thr.printStackTrace();
		});

		//vertx.close();   //get config asynchronous => ko đc gọi hàm này
	}
}
