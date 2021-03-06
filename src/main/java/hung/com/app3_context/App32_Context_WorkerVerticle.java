package hung.com.app3_context;

import io.vertx.core.Context;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

/**
Context: để chỉ 1 đơn vị quản lý tài nguyên Memory và threadpool luôn. 
Vertx context khác Verticle Context (đã test).

 Event, task thuộc context nào thì sẽ đc xử lý trên thread (or threadpool) của context đó.
blocking-code đc gọi ở Verticle nhưng thuộc Vertx Context nên run ở context của Vertx => dùng threadpool của Vertx.

Trong khi Event, task sinh ra ở Blocking-code lại thuộc context của Verticle tạo ra “blocking-code” => 
event hay task này sẽ chạy trên thread (or threadpool) của Verticle (ko chạy trên vertx context).

 */
public class App32_Context_WorkerVerticle {

	public static void main(String[] args) throws InterruptedException{
		System.out.println("main(): thread="+Thread.currentThread().getId());
		//create a new instance Vertx => a worker thread
		Vertx vertx = Vertx.vertx();
		
		//vertx.getOrCreateContext() sẽ trả về context gắn với Thread hiện tại:
		// convert Current Thread => Context và trả về
		// Verticle.start() luôn chạy trên thread của Verticle context hiện tại nên sẽ trả về Verticle context	
		Context context = vertx.getOrCreateContext();
		if (context.isEventLoopContext()) {
			System.out.println("main: Context attached to Event Loop: "+ context.deploymentID());
		} else if (context.isWorkerContext()) {
			System.out.println("main: Context attached to Worker Thread: "+ context.deploymentID());
		} else if (context.isMultiThreadedWorkerContext()) {
			System.out.println("main: Context attached to Worker Thread - multi threaded worker: "+ context.deploymentID());
		} else if (! Context.isOnVertxThread()) {
			System.out.println("main: Context not attached to a thread managed by vert.x: "+ context.deploymentID());
		}

		//register Verticale with Vertex instance to capture event.
		DeploymentOptions deploymentOptions = new DeploymentOptions()
//				.setInstances(6)          //create 6 instances of Verticles
				.setWorkerPoolName("WorkerPoolName1")   //ten là duy nhat, dùng chung, dùng lại đc
				.setWorkerPoolSize(3)
				.setWorker(true);   //true: worker-vertical dùng WorkerPoolName1  (các event vẫn tuần tự, nhưng trên thread khác nhau)
									//false: Standard-verticle dùng vert.x-eventloop-thread (fix thread to verticle)
									//blockingCode luôn dùng WorkerPoolName1
		
		vertx.deployVerticle(new ContextVerticle1(),deploymentOptions); //asynchronous call MyVerticle1.start() in worker thread
		
		
		// app ko stop với Main() stop vì có 1 worker thread quản lý Vertx có loop bắt Event
	    //vertx.close();
	}

}
