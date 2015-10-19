package lu.uni.distributedsystems.project.bookie;

import java.io.IOException;

import com.google.code.gsonrmi.Parameter;
import com.google.code.gsonrmi.RpcRequest;
import com.google.code.gsonrmi.RpcResponse;
import com.google.code.gsonrmi.annotations.RMI;
import com.google.code.gsonrmi.serializer.ExceptionSerializer;
import com.google.code.gsonrmi.serializer.ParameterSerializer;
import com.google.code.gsonrmi.server.RpcTarget;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lu.uni.distributedsystems.gsonrmi.server.BaseServer;
import lu.uni.distributedsystems.gsonrmi.server.Interceptor;
import lu.uni.distributedsystems.gsonrmi.server.RpcSocketListener;

// Your server implementation should extend the class BaseServer;
// this way, the setModeOfHost remote method to control how requests
// will be processed is added automatically
public class BookieServer extends BaseServer {

	// the enclosing bookie
	private Bookie bookie;
	// port on which the bookie's JSON-RPC server listens
	private int bookiePort; 
	
	public BookieServer(Bookie bookie, int bookiePort) {
		this.bookie = bookie;
		this.bookiePort = bookiePort;
	}
	
	// the @RMI annotation exposes some method for remote invocation
	@RMI
	public String sayHelloToBookie(String gamblerID) {
		// implementation of the sayHelloToBookie method, which
		// can be invoked remotely with one argument of
		// type String, returning another String
		System.out.println("sayHelloToBookie(" + gamblerID + ")");
		
		bookie.getGamblerConnection(gamblerID).sayHello();
		
		return "Bookie says: Hello, gambler " + gamblerID;
	}

	// boilerplate code for a gambler who wants to connect to a bookie,
	// specifying at which IP and port to reach the gambler.
	@RMI
	public String connect(String gamblerID, String gamblerIP, int gamblerPort) {
		bookie.createNewGamblerConnection(gamblerID, gamblerIP, gamblerPort);
		return bookie.getBookieID();
	}

	
	// TODO insert the methods that a Gambler can execute via RPC on this Bookie
	
	
	public void start() {
		// start a JSON-RPC server; all methods tagged with the @RMI annotation will
		// be invokable remotely; all requests as well as all responses will be
		// passed on to the interceptor; please note that this class extends BaseServer
		// and thus will expose an additional method setModeOfHost to control how
		// requests will be processed

		// create an instance of Gson, the primary class for using the Gson libraries
		Gson gson = new GsonBuilder()
				.registerTypeAdapter(Exception.class, new ExceptionSerializer())
				.registerTypeAdapter(Parameter.class, new ParameterSerializer())
				.create();
		
		
		// This creates an Interceptor that has the two functions interceptRequest and interceptResponse
		//Follwoing the code for an Interceptor which simply prints all intercepted requests
		// and responses, just for illustration purposes
		
		Interceptor interceptor = new Interceptor() {
			
			// TODO replace the simple interceptor with a more meaningful implementation
			
			@Override
			public RpcResponse interceptRequest(RpcRequest request) {
				System.out.println("intercepted request: " + gson.toJson(request));
				return null;
			}
			
			@Override
			public void interceptResponse(RpcRequest request, RpcResponse response) {
				
				// HINT 1: 	this message returns an RPCResponse object, in case that it shall do nothing with it, simply return null.
				// HINT 2: 	If you want to have more details on the interceptors you can have a look at  
				//			the code in the project GSONRMI, in the package lu.uni.distributedsystems.gsonrmi.server, in the class: RpcConnectionHandler.java
				
				
				System.out.println("intercepted response: " + gson.toJson(response) + " for request: " + gson.toJson(request));
			}

		};

		
		// launch a socket listener accepting and handling JSON-RPC requests
		try {
			new RpcSocketListener(bookiePort, new RpcTarget(this, gson), gson, interceptor).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
