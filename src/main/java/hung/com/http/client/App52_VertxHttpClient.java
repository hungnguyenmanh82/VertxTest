package hung.com.http.client;

import hung.com.http.server.HttpServerVerticle;
import io.vertx.core.Vertx;

public class App52_VertxHttpClient {
	public static void main(String[] args) {
		System.out.println("start main(): thread="+Thread.currentThread().getId());
		Vertx vertx = Vertx.vertx();
		
		//tạo 5 connect tới server
		vertx.deployVerticle(new HttpClientVerticle()); 
/*		vertx.deployVerticle(new HttpClientVerticle());
		vertx.deployVerticle(new HttpClientVerticle()); 
		vertx.deployVerticle(new HttpClientVerticle()); 
		vertx.deployVerticle(new HttpClientVerticle()); */
	}
}
