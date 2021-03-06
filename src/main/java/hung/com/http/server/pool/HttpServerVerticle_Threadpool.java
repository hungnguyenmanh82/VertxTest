package hung.com.http.server.pool;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpConnection;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

/**
 * 
http://tutorials.jenkov.com/vert.x/http-server.html

Vertical đăng ký nhận HttpServer event với Vertx.
Vertx thay mặt Vertical làm mọi thứ, bắt event.
Khi có event, Vertx sẽ trả về và gọi Vertical xử lý
Tất nhiên, Vertx phải cấp phát thread cho Verticle 
 */
public class HttpServerVerticle_Threadpool extends AbstractVerticle{
	private HttpServer httpServer = null;

	private Buffer totalBuffer = Buffer.buffer(4000); //4k byte
	//run on a worker thread
	@Override
	public void start(Future<Void> startFuture) throws Exception {
		System.out.println(this.getClass().getName()+ ".start(): thread="+Thread.currentThread().getId() + ", ThreadName="+Thread.currentThread().getName());
		System.out.println("try browser with url: http://localhost:81/atm?id=1&command=ejm");
		System.out.println("try browser with url: http://localhost:81/");
		
		HttpServerOptions httpServerOptions = new HttpServerOptions()
				.setMaxHeaderSize(4000)
				.setReceiveBufferSize(8000)
				.setSendBufferSize(8000);

		/**
		 *  HttpServer luôn run trên 1 thread dù option Deploy thế nào đi nữa
		 *  ok => vì quá trình read socket buffer ko nên có nhiều thread (sẽ gây áp lực lên server)
		 *  Thread nên tập trung vào write socket buffer  (khi write free thì từ nhiên read-thread sẽ có CPU để thực hiện)
		 */
		httpServer = vertx.createHttpServer(httpServerOptions);


		/**
		 * url = http://localhost:81/atm?id=1&command=ejm
		 * uri = /atm?id=1&command=ejm
		 * path = /atm
		 * id = 1
		 * command = ejm
		 */

		/**
		 * Lưu ý http1.x thì 1 connect socket sẽ cho nhiều request
		 */
		httpServer.connectionHandler(new Handler<HttpConnection>() {		
			@Override
			public void handle(HttpConnection connect) {
				System.out.println("ConnectHandler:"+ "thread="+Thread.currentThread().getId() + ", ThreadName="+Thread.currentThread().getName());
				//reject connect neu Server overload or number of connect > Max connect

				// Deploy Verticle de xu ly http request/response tren Threadpool khac nhu the se toi uu hon
				// 1 http request/reponse context nen xu ly tren 1 thread (threadpool worker= false) de dam bao Order
				//truong hop context lay du lieu tu 2 service khac nhau thi phai dam bao tinh thu tu

				/**
				 * http 1.1 dùng keep alive connect, nên nhiều request chung 1 socket connect (đã test với Chrome và Wireshark)
				 */
			}
		});

		// quá trình parser thực hiện trên EventLoopPool của Vertx, chứ ko phải trên thread của context này.
		// thread của context này chỉ để trả về event thôi.
		httpServer.requestHandler(new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {
				System.out.println("http requestHandler: thread="+Thread.currentThread().getId()+ ", ThreadName="+Thread.currentThread().getName());
				//================================ Move http HandlerRequest to other Thread context =========================== 
				DeploymentOptions options = new DeploymentOptions()
						.setWorkerPoolName("ThreadPoolForRequestHandler")   //name là duy nhất để share giữa các Verticle
						.setMaxWorkerExecuteTime(2000)			//nếu chạy quá 2s sẽ stop đoạn code đó lại để thu hồi thread
						.setWorkerPoolSize(3)  //thread for server, not client
						.setHa(true)         //HA: high Availability
						.setWorker(true);    //true: worker-vertical dùng ThreadPoolForRequestHandler  (các event vẫn tuần tự, nhưng trên thread khác nhau)
											//false: Standard-verticle dùng vert.x-eventloop-thread (fix thread to verticle)
											//blockingCode luôn dùng ThreadPoolForRequestHandler

				vertx.deployVerticle(new HttpRequestHandlerVerticle(request),options);
			}
		});


		httpServer.listen(81, new Handler<AsyncResult<HttpServer> >() {			
			@Override
			public void handle(AsyncResult<HttpServer> result) {
				if (result.succeeded()) {
					// thông báo khởi tạo thành công, cho phia vertx.deployVerticle(verticle, handler<AsyncResult<void>>)
					// <xem Future concept>
					startFuture.complete();  
				} else {
					startFuture.fail(result.cause());
				}

			}
		});//port = 81
	}

	// run on a worker thread
	@Override
	public void stop(Future<Void> stopFuture) throws Exception {
		//function này cần đc gọi để xác nhận undeploy() thành công (sẽ xóa DeploymentId)
		// hoặc phải gọi hàm stopFuture.complete()
		super.stop(stopFuture);
//		stopFuture.complete();
		System.out.println("Verticle_startFuture.stop(): thread=" + Thread.currentThread().getId());
	}
}
