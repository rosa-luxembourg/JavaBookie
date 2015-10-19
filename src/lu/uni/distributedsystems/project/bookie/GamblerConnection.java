package lu.uni.distributedsystems.project.bookie;

import com.google.code.gsonrmi.Parameter;
import com.google.code.gsonrmi.RpcResponse;

import lu.uni.distributedsystems.project.common.JsonRpcConnection;

public class GamblerConnection extends JsonRpcConnection {
	
	private String gamblerID; // gambler-ID of the gambler on the other side of this connection
	private Bookie bookie;   // the enclosing bookie
	
	// A GamblerConnection is used to communicate with a connected Gambler.
	public GamblerConnection(Bookie bookie, String gamblerID, String gamblerIP, int gamblerPort) {
		// initialize JsonRpcConnection base class
		super(gamblerIP, gamblerPort);
		this.bookie = bookie;
		this.gamblerID = gamblerID;
	}

	// This sample method is just there for illustration purposes
	public void sayHello() {
		Parameter[] params = new Parameter[] { new Parameter(bookie.getBookieID()) };
		RpcResponse response = handleJsonRpcRequest("sayHelloToGambler", params);
		
		// show hello message returned by bookie
		String sayHelloResponse = response.result.getValue(String.class, getGson());
		System.out.println("Gambler " + gamblerID + " sent response: " + sayHelloResponse);
	}
	
	
	// TODO insert the needed methods to communicate with a Gambler

}
