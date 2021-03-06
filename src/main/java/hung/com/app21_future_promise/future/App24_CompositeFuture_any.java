package hung.com.app21_future_promise.future;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;

/**
 * http://vertx.io/docs/vertx-core/java/#_concurrent_composition
 * 
Future<Type>:  extends Handler<type> và AsyncResult<type> => là kết hợp 2 class này để tạo funtion point (làm call back function khi có event).
 Khi 2 hàm future.complete(result) or future.fail(result) đc gọi thì lập tức nó sẽ gọi hàm callback của nó là Handler.handle(AsyncResult<result>). 
Future là function point => thread nào gọi nó thì nó chạy trên thread đó (đã test).

 */
public class App24_CompositeFuture_any {

	public static void main(String[] args) throws InterruptedException{
		System.out.println("main(): thread=" + Thread.currentThread().getId());
		Vertx vertx = Vertx.vertx();
		
		//===================================== http server =======================================
		HttpServer httpServer = vertx.createHttpServer();  //http server đc tạo trên Vertx context (ko có Verticle)

		httpServer.requestHandler(new Handler<HttpServerRequest>() {
			
			@Override
			public void handle(HttpServerRequest request) {
				//dùng Browser để test:  http://localhost:8080/abc
				System.out.println("uri = "+ request.uri()); //
				System.out.println("httpserver requestHandler: thread=" + Thread.currentThread().getId());
			}
		});
		// future để quản lý event => thông báo khi http Server listen thành công trên port (port ko bị chiếm dụng bởi app khac) 
		// future đc tạo gắn với context nào sẽ tạo event để gửi về context đó
		// future extends Handler<AsynResultc<Object>> vì thế dùng future như Handler đc
		// Handler của future khi đc handle() sẽ gửi Event tới Context gắn với nó cung AsyncResult
		// Handler bản chất là runable thôi
		Future<HttpServer> httpServerFuture = Future.future();
		int httpPort = 8080;
		httpServer.listen(httpPort,httpServerFuture); //= httpServerFuture.completer() = Handler
		
		
		//===================================== TCP server ================================================
		NetServer netServer = vertx.createNetServer(); //tcp server đc tạo trên Vertx Context => threadpool của Vertx context
		netServer.connectHandler(new Handler<NetSocket>() {
			
			@Override
			public void handle(NetSocket netSocket) {
				//http base trên tcp => //dùng Browser để test:  http://localhost:8081/abc
				System.out.println("TCP server connectHandler: thread=" + Thread.currentThread().getId());			
			}
		});
		
		// future để quản lý event => thông báo TCP server listen thành công trên port
		Future<NetServer> netServerFuture = Future.future();
		int tcpPort = 8081;
		netServer.listen(tcpPort,netServerFuture); //= netServerFuture.completer()

		//==================================== future 3 ===========================================
		Future<String> futureTest = Future.future(); //fut1: gắn với context của Vertx

		vertx.deployVerticle(new FutureInplementAtVerticle(futureTest));
		
		//=====================================  wait all Futures ==================================
		//chờ cho 2 Server đc khởi tạo thành công (listening) or fail
		//CompositeFuture: nếu 1 trong 2 fail thì tất cả fail
		// đăng ký nhận future ở context hiện tại
		//Trường hợp đặc biệt là 1 Future event, vd dưới là 2 event (có thể có N event)
		CompositeFuture.any(httpServerFuture, futureTest).setHandler(new Handler<AsyncResult<CompositeFuture>>() {
			
			@Override
			public void handle(AsyncResult<CompositeFuture> event) {
				if (event.succeeded()) {		    
					// At least one is succeeded
					  System.out.println("At least one is succeeded");  
				  } else {
					// All failed
					  System.out.println("All failed"); 
				  }
				
			}
		});

		// app ko stop với Main() stop vì có 1 worker thread quản lý Vertx có loop bắt Event
		//vertx.close();
	}

}
