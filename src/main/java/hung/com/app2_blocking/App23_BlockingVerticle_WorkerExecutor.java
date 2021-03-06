package hung.com.app2_blocking;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.WorkerExecutor;

/**
 * 
   //=============
   blocking execute code: Là 1 đoạn code đc Verticle trigger để chạy Asynchronous với Verticle (chạy song song).
    Sau khi chạy xong đoạn code này nó sinh event gửi tới Verticle qua Vertx.
    
   có thể tạo threadpool riêng để thực hiện Blocking code. Thay vì dùng chung threadpool với Vertx   

 */
public class App23_BlockingVerticle_WorkerExecutor extends AbstractVerticle {

	public static void main(String[] args) throws InterruptedException{
		System.out.println("start main(): thread="+Thread.currentThread().getId());
		//create a new instance Vertx => a worker thread
		VertxOptions vertxOptions = new VertxOptions().setWorkerPoolSize(4)  // threadPool của blocking code 
														.setEventLoopPoolSize(4);  //threadpool của EventLoop cho standard Verticle
		Vertx vertx = Vertx.vertx(vertxOptions);

		//register Verticale with Vertex instance to capture event.
		vertx.deployVerticle(new App23_BlockingVerticle_WorkerExecutor()); //asynchronous call MyVerticle1.start() in worker thread

		// app ko stop với Main() stop vì có 1 worker thread quản lý Vertx có loop bắt Event
		//vertx.close();
	}
	
	@Override
	public void start() throws Exception {	
		super.start();
		System.out.println(this.getClass().getName()+ ".start(): thread="+Thread.currentThread().getId() + ", ThreadName="+Thread.currentThread().getName());
		
		
		//blockingHandler run trên WorkerExecutor => độc lập với verticle thread
		// nó trigger Verticle context qua Promise
		Handler<Promise<String>> blockingHandler = new Handler<Promise<String>>() {
			public String test = "abc";
			//Future này quản lý bởi Vertx, ko phải Verticle
			@Override
			public void handle(Promise<String> future) {
				System.out.println("******blockingHandler: thread="+Thread.currentThread().getId() + ", ThreadName="+Thread.currentThread().getName());
				
				String result = "blockingHandler: thread="+Thread.currentThread().getId();
				//future đc dùng cho asynchronous function ở trong hàm handle này.	
				future.complete(result);   //sẽ gửi event tới context của Verticle
				System.out.println(test);
			}
			
		};
		
		//run on thread of this Verticle
		Handler<AsyncResult<String>> returnHandler =  new Handler<AsyncResult<String>>() {
			public void handle(AsyncResult<String> event) {
				System.out.println("returnHandler: thread=" + Thread.currentThread().getId()+
							 ", result=" + event.result());
			};
		};
		
		
		//======================== tạo threadpool riêng để thực hiện Blocking code =================
		// ko dùng chung threadpool với Vertx nữa
		int poolSize = 2;
		long maxExecuteTime = 1000; //mini second
		String threadPoolName = "my-WorkerExecutor-pool"; //tên là id duy nhất. 2 tham số còn lại chỉ dùng lần đầu tạo threadpool
												  // có thể dùng tên lại ở 1 verticle khác vẫn ok.	
		
		// nếu đã tồn tại threadpoolName này rồi, thì nó lấy luôn threadpool đó (ko tạo mới)
		WorkerExecutor executor = vertx.createSharedWorkerExecutor(threadPoolName,poolSize, maxExecuteTime);
		
		//order = false => sẽ có ưu tiên cao hơn. chạy trên thread độc lập ko quan tâm order trong queue
		//by default (function 2 tham số): order = true => chạy theo order trong queue 
		executor.executeBlocking(blockingHandler, false, returnHandler);
		
		//giải phóng threadpool này
		//executor.close();

	}

	@Override
	public void stop() throws Exception {
		super.stop();
		System.out.println("MyVerticle.stop(): thread=" + Thread.currentThread().getId());
	}

}
