package left.connection;


import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.json.JSONObject;

import left.book.Books;
import left.Model.Models;
import left.trades.MarketTrades;
import left.websocket.WebSocketServerService;
import left.websocket.WebsocketApi;

	public class SocketMain {

	private String ACCESS_KEY="ACCESS_KEY_HERE";
	private String SECRET_KEY="SECRET_KEY_HERE";
	private static String HMAC_SHA1_ALGORITHM = "HmacSHA1";
	private String postdata="";
	private String tonce = ""+(System.currentTimeMillis() * 1000);


		public static void run() throws URISyntaxException{
			 IO.Options opt = new IO.Options();
			 opt.reconnection = true;


			 Books.getInstance().init();
			 MarketTrades.getInstance().init();
			 Models.getInstance().init();
			 try {
					Thread th = new Thread(
							WebSocketServerService.getInstance() );
					th.start();

			} catch (Exception e1) {
				// TODO Auto-generated catch block
				System.out.println("Cannot start websocket server");
				e1.printStackTrace();
			}
			 final Socket socket = IO.socket("https://websocket.btcchina.com", opt);

			 socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
			 SocketMain sm= new SocketMain();
			 @Override
			 public void call(Object... args) {
			 System.out.println("-- Connected --");
			 socket.emit("subscribe", "marketdata_cnybtc"); // subscribe at cny btc

			 socket.emit("subscribe", "grouporder_cnybtc"); // subscribe grouporder 
			 //Use 'private' method to subscribe the order and account_info feed
			   try {
			    List arg = new ArrayList();
			    arg.add(sm.get_payload());
			    arg.add(sm.get_sign());
			    socket.emit("private",arg);
			   } catch (Exception e) {
			     // TODO Auto-generated catch block
			     e.printStackTrace();
			   }
			  }
			 }).on("trade", new Emitter.Listener() {
			  @Override
			  public void call(Object... args) {
			   JSONObject json = (JSONObject) args[0]; //receive the trade message
			   MarketTrades.getInstance().updateMarketTrade(json); //update market trades
			   System.out.println("trade: " + json);
			  }
			 }).on("ticker", new Emitter.Listener() {
			  @Override
			  public void call(Object... args) {
			   JSONObject json = (JSONObject) args[0];//receive the ticker message
			   //WebsocketApi.getInstance().sendMessage(json.toString());
			   System.out.println("ticker: " + json);
			  }
			 }).on("grouporder", new Emitter.Listener() {
			  @Override
			  public void call(Object... args) {
			   JSONObject json = (JSONObject) args[0];//receive the grouporder message
			   //WebsocketApi.getInstance().sendMessage(json.toString());
			   Books.getInstance().updateBook(json);
			   System.out.println(json);
			  }
			 }).on("order", new Emitter.Listener() {
			  @Override
			  public void call(Object... args) {
			   JSONObject json = (JSONObject) args[0];//receive your order feed
			   System.out.println("order: " + json);
			  }
			 }).on("account_info", new Emitter.Listener() {
			  @Override
			  public void call(Object... args) {
			   JSONObject json = (JSONObject) args[0];//receive your account_info feed
			   System.out.println(json);
			  }
			 }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
			  @Override
			  public void call(Object... args) {
			   System.out.println("-- Disconnected --");

			  }
			 });
			 socket.connect();
		}

		public static void main(String[] args) throws Exception {
			try {
				run();
			} catch (URISyntaxException ex) {
				System.out.println("Error in main:");
				Logger.getLogger(SocketMain.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		public String get_payload() throws Exception{
			postdata = "{\"tonce\":\""+tonce.toString()+"\",\"accesskey\":\""+ACCESS_KEY+"\",\"requestmethod\": \"post\",\"id\":\""+tonce.toString()+"\",\"method\": \"subscribe\", \"params\": [\"order_cnyltc\",\"account_info\"]}";//subscribe order feed for cnyltc market and balance feed
			System.out.println("postdata is: " + postdata);
			return postdata;
		}

		public String get_sign() throws Exception{
			String params = "tonce="+tonce.toString()+"&accesskey="+ACCESS_KEY+"&requestmethod=post&id="+tonce.toString()+"&method=subscribe&params=order_cnyltc,account_info"; //subscribe the order of cnyltc market and the account_info
			String hash = getSignature(params, SECRET_KEY);
			String userpass = ACCESS_KEY + ":" + hash;
			String basicAuth = DatatypeConverter.printBase64Binary(userpass.getBytes());
			return basicAuth;
		}

		public String getSignature(String data,String key) throws Exception {
			// get an hmac_sha1 key from the raw key bytes
			SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
			// get an hmac_sha1 Mac instance and initialize with the signing key
			Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac.init(signingKey);
			// compute the hmac on input data bytes
			byte[] rawHmac = mac.doFinal(data.getBytes());
			return bytArrayToHex(rawHmac);
		}

		private String bytArrayToHex(byte[] a) {
			StringBuilder sb = new StringBuilder();
			for(byte b: a)
				sb.append(String.format("%02x", b&0xff));
		 return sb.toString();
		}
		}
