package application;
	

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JOptionPane;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.*;




public class Main extends Application {
	public static ExecutorService threadPool;
	public static Vector<Client> clients = new Vector<Client>();
	ServerSocket serverSocket;
	
	public void startServer(String IP, int port) {
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(IP,port));
		} catch(Exception e) {
			e.printStackTrace();
			if(!serverSocket.isClosed())
				stopServer();
			return;
		}
		
		Runnable thread = new Runnable() {

			@Override
			public void run() {
				while(true) {
					try {
						Socket socket = serverSocket.accept();
						clients.add(new Client(socket));
						System.out.println("[클라이언트 접속] "
								+ socket.getRemoteSocketAddress()
								+": " + Thread.currentThread().getName());
					}catch (Exception e) {
						if(!serverSocket.isClosed())
							stopServer();
						break;
					}
				}
				
			}
			
		};
		threadPool = Executors.newCachedThreadPool();
		threadPool.submit(thread);
	}
	
	public void stopServer() {
		try {
			Iterator<Client> iterator = clients.iterator();
			while(iterator.hasNext()) {
				Client client = iterator.next();
				client.socket.close();
				iterator.remove();
			}
			if(serverSocket != null && !serverSocket.isClosed())
				serverSocket.close();
			if(threadPool != null && !threadPool.isShutdown())
				threadPool.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));
		
		TextArea textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setFont(new Font("Arial",15));
		root.setCenter(textArea);
		
		
		Button toggleButton = new Button("시작하기");
		toggleButton.setMaxWidth(Double.MAX_VALUE);
		BorderPane.setMargin(toggleButton, new Insets(1,0,0,0));
		root.setBottom(toggleButton);
		
		String IP = "192.168.0.77";
		int port = 22222;
		
		toggleButton.setOnAction(event -> {
			if(toggleButton.getText().equals("시작하기")) {
				startServer(IP, port);
				Platform.runLater(() -> {
					String message = String.format("[서버 시작]\n", IP,port);
					JOptionPane.showMessageDialog(null, "서버 시작");
					textArea.appendText("작동중......");
					toggleButton.setText("종료하기");
				});
			}else {
				stopServer();
				Platform.runLater(() ->{
					String message = String.format("[서버 종료]\n", IP,port);
					JOptionPane.showMessageDialog(null, "서버 종료");
					textArea.setText(null);
					toggleButton.setText("시작하기");
				});
			}
		});
		Scene scene = new Scene(root,400,400);
		primaryStage.setTitle("[까똑 서버]");
		primaryStage.getIcons().add(new Image("file:resources/images/emoji.png"));
		primaryStage.setOnCloseRequest(event ->stopServer());
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
