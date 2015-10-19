package lu.uni.distributedsystems.project.common;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import com.google.code.gsonrmi.Parameter;
import com.google.code.gsonrmi.RpcRequest;
import com.google.code.gsonrmi.RpcResponse;
import com.google.code.gsonrmi.serializer.ExceptionSerializer;
import com.google.code.gsonrmi.serializer.ParameterSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import lu.uni.distributedsystems.gsonrmi.server.ServiceMode;

public class JsonRpcConnection {

	private String serverIP;  // IP address of the JSON-RPC server
	private int serverPort;   // port number of the JSON-RPC server
	
	
	private Socket socket;         // socket connection with the JSON-RPC server
	private Gson gson;             // gson object to use e.g. for serialization/deserialization
	private Writer writer;         // writer to write to socket's stream
	private JsonReader jsonReader; // JSON reader to read from socket's stream

	private static Logger logger = Logger.getLogger(JsonRpcConnection.class.getName());
	
	public JsonRpcConnection(String serverIP, int serverPort) {
		// remember IP and port of the JSON-RPC server, also in case we need to re-connect
		this.serverIP = serverIP;
		this.serverPort = serverPort;
		

		// create an instance of Gson, the primary class for using the Gson libraries
		gson = new GsonBuilder()
				.registerTypeAdapter(Exception.class, new ExceptionSerializer())
				.registerTypeAdapter(Parameter.class, new ParameterSerializer())
				.create();
	}
	
	public Gson getGson() {
		return gson;
	}
	
	public void establishSocketConnection() {
		// setup a socket connection to the JSON-RPC server
		try {
			socket = new Socket(serverIP, serverPort);
			logger.info("Connection with JSON-RPC server opened at local endpoint " + socket.getLocalAddress().getHostAddress() + ":" + socket.getLocalPort());
			// create a writer to send JSON-RPC requests to the JSON-RPC server
			writer = new OutputStreamWriter(socket.getOutputStream(), "utf-8");
			// create a JsonReader object to receive JSON-RPC response
			jsonReader = new JsonReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
		} catch (UnknownHostException e) {
		
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	// This closes the Socket.
	public void closeConnection() {
		try {
			socket.close();
		} catch (IOException e) {
			logger.warning("problem closing socket connection with JSON-RPC server at " + serverIP + ":" + serverPort);
			e.printStackTrace();
		}
	}
	
	
	/*
	 * The setModeOfHost method tells the designated Gambler to handle any incoming RPCalls originating 
	 * from this Bookie with the behaviour specified by the ServiceMode
	 * 
	 * Refer to the Getting started document for more information.
	 */
	public void setModeOfHost(ServiceMode serviceMode) {
		Parameter[] params = new Parameter[] {
				new Parameter(socket.getLocalAddress().getHostAddress()),
				new Parameter(socket.getLocalPort()),
				new Parameter(serviceMode)
				};
		handleJsonRpcRequest("setModeOfHost", params);
	}

	
	// TODO adapt this class to the applications needs when sending messages.
	protected RpcResponse handleJsonRpcRequest(String method, Parameter[] params) {
	
		RpcRequest request = new RpcRequest();
		RpcResponse response = null;
	
		request.method = method;
		request.params = params;
		
		// TODO make sure to assign appropriate ID's
		request.id = new Parameter(42);
	
			try {
				logger.info("sending request: " + gson.toJson(request));
				// attempting to send the request via the writer to
				// the JSON-RPC server might throw an IOException
				writer.write(gson.toJson(request));
				writer.flush();

				// in case of a connection loss, the following call will throw an exception
				response = gson.fromJson(jsonReader, RpcResponse.class);

				logger.info("received response: " + gson.toJson(response));
				
			}
			catch (Exception ex) {
				// connection to JSON-RPC server is lost
				
				// TODO Handle the case of a connection loss
				
				logger.info("server connection dropped");
			}
		
		return response;
	}


}