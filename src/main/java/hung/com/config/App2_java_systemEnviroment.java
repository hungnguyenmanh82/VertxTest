package hung.com.config;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
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
 * system properties: là các properties của hệ điều hành
 * system properties có thể add thêm properties vào từ commandline hoặc từ source code java System.setProperty() :
 *   > java -D<propertyName>=<value> -jar <app_params>
 * 
 * add vào ở commandline thì chỉ có phạm vi trong Process đó thôi (là local system properties, ko phải global system properties)
 * Environment Variable add vào từ OS thì các app viết bằng ngôn ngữ khác đều đọc đc.   
 */
public class App2_java_systemEnviroment {
	public static void main(String[] args) throws InterruptedException{
		/**
		 * đây là thư viện của Java
		 * Liệt kê hết các system properties của hệ thông
		 */
		System.getenv().forEach((Object key, Object value)->{
			System.out.printf("{%s:%s}\n",key,value);
		});
		
		//
		System.out.println("========================");
		System.out.println("JAVA_HOME = " + System.getenv().get("JAVA_HOME"));
	}
}
