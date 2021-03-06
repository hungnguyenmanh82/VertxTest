package hung.com.app6_exception;

import java.util.Set;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * vd: Khởi tạo 1 vertical  < xem khái niệm vertical>
 *
 */
public class App2_exception_handler extends AbstractVerticle {

	public static void main(String[] args) throws InterruptedException{
		Vertx vertx = Vertx.vertx();

		Verticle verticle = new App2_exception_handler();
		
		vertx.deployVerticle(verticle); //asynchronous call MyVerticle1.start() in worker thread

		
		vertx.exceptionHandler(new Handler<Throwable>() {
			
			@Override
			public void handle(Throwable event) {
				// Vertx 4.0 Milestone5 ko hiển thị Vertx-Log => lỗi
				// chỉ có cách dùng Try/catch bắt exception ở đoạn code nghi vấn
				System.out.println("=====" + event.getCause());
				
			}
		});
		
	
		
	}
	
	@Override
	public void start() throws Exception {
		super.start();
		
		// Vertx 4.0 Milestone5 ko hiển thị Vertx-Log => lỗi
		// chỉ có cách dùng Try/catch bắt exception ở đoạn code nghi vấn
		throw new Exception("test Exception Handler");

	}

}
