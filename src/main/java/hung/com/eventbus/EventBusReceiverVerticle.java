package hung.com.eventbus;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;

/**
 * 
 * Vertx đóng vai trò là Broker
 * Verticle đóng vai trò consumer or provider để gửi nhận Message queue qua Broker
 */

public class EventBusReceiverVerticle extends AbstractVerticle {
	private String name;	

	public EventBusReceiverVerticle(String name) {
		super();
		this.name = name;
	}

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		super.start(startFuture);
		System.out.println("=> EventBusReceiverVerticle.start():"+ "name="+ name +
				", thread="+Thread.currentThread().getId());

		//vertx member of AbstractVerticle  => where this Verticle was deployed
		//consumer=(register to receive event): this Verticle context đăng ký nhận Event từ Vertx
		// event đc xử lý trên threadpool của Verticle context này
		// address: của bên nhận và gửi phải giống nhau để giao tiếp với nhau
		// Message<String> = address + header + body
		// body kiểu String
		EventBus eb = vertx.eventBus();
		String address = "anAddress";
		MessageConsumer<String> consumer = eb.consumer(address);  //register Address với EventBus to reciever message
		
		// register nhận Message có address tại Eventbus
		consumer.handler(new Handler<Message<String>>() { //có thể thay String bằng kiểu khác: object, int,float...
			@Override
			public void handle(Message<String> message) {
				// chạy trên Thread của Verticle
				System.out.println("***Handle(): EventBusReceiverVerticle:Consumer():"+
						"name="+ name +
						", thread="+Thread.currentThread().getId());
				System.out.println("receive Message: name="+ name +  
						", address="+ message.address()+ 
						", body=" +message.body());  //body kiểu <string>
				
				// message tổ chức giống như Http protocol vậy
				// bên publish có thể gửi headers, bên nhận có thê nhận header
				//message.headers();
				
			}

		} );
		
		//register to receive Message ok
		consumer.completionHandler(res -> {
			  // chạy trên Thread của Verticle
			  if (res.succeeded()) {
			    System.out.println("The handler registration has reached all nodes");
			  } else {
			    System.out.println("Registration failed!");
			  }
			});

		// để unregister Message đã đăng ký với lệnh consumer()
		// phải gọi hàm này trên cùng Thread => vì ThreadId đc qui đổi ra Context
		/*consumer.unregister(res -> {
			if (res.succeeded()) {
				System.out.println("The handler un-registration has reached all nodes");
			} else {
				System.out.println("Un-registration failed!");
			}
		});*/

	}
}
