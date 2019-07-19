package application;
	


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;




import javax.swing.JOptionPane;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;


public class Main extends Application {
	@FXML private ImageView imgView;
	Socket socket;
	TextArea textArea;
	File file;
	Scene scene = null;
	public void startClient(String IP, int port) {
		Thread thread = new Thread() {
			public void run() {
				try {
					socket = new Socket(IP,port);
					receive();
				}catch (Exception e) {
					if(socket == null) {
						JOptionPane.showMessageDialog(null, "서버가 닫혀있음");
					}
					if(!socket.isClosed()) {
						stopClient();
						System.out.println("[서버 접속 실패]");
						Platform.exit();
					}
				}
			}
		};
		thread.start();
	}
	
	public void stopClient() {
		try {
			if(socket != null && !socket.isClosed()) {
				socket.close();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void receive() {
		while(true) {
			try {
				InputStream in = socket.getInputStream();
				
				byte[] buffer = new byte[5000];
				int length = in.read(buffer);
				if(length == 5000) {
					System.out.println("하이하이");
					Long num = System.currentTimeMillis();
					File f = new File("C:\\Users\\user\\Desktop\\"+num+".jpg");
					BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f),5000);
					while(true) {
						bos.write(buffer, 0, length);
						bos.flush();
						if(length < 5000) break;
						length = in.read(buffer);
					}
					textArea.appendText("이미지 파일"+f+"가 바탕화면에 다운로드 되었습니다.\n");
					length = -1;
				}
				if(length != -1) {
				String message = new String(buffer, 0, length,"UTF-8");
				Platform.runLater(() ->{
					textArea.appendText(message);
					});
				}
			} catch(Exception e) {
				stopClient();
				break;
			}
		}
	}
	public void send(String message) {
		Thread thread = new Thread() {
			public void run() {
				try {
					OutputStream out =  socket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer);
					out.flush();
				}catch(Exception e) {
					stopClient();
				}
			}
		};
		thread.start();
	}
	public void uploadImage(String file) {
		Thread thread = new Thread() {
			public void run() {
				try {
					File f = new File(file);
					
					OutputStream out = socket.getOutputStream();
					byte[] buffer = new byte[5000];
					BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f), 5000);
					int len;
					while((len = bis.read(buffer)) != -1) {
						out.write(buffer, 0 , len);
						out.flush();
					}
					textArea.appendText("사진 "+f+"를 보냈습니다.");
					textArea.appendText("\r\n");
					bis.close();
	
				}catch(Exception e) {
					e.printStackTrace();
				}
				
			}
		};
		thread.start();
	}
	/*새로운 창 띄워서 이미지 띄우기
	 * public void downloadIamge(String path) {
		while(true) {
			try {
				InputStream in = socket.getInputStream();
				byte[] buffer = new byte[1024];
				int length = in.read(buffer);
				if(length == -1) throw new IOException();
				Image image = new Image(buffer);
				Platform.runLater(() ->{
					StackPane pane = new StackPane();
					Scene scene2 = new Scene(pane,500,400);
					Stage stage = new Stage();
					ImageView iv = new ImageView();
					iv.setImage(image);
					pane.getChildren().add(iv);
					stage.setScene(scene2);
					stage.show();
					stage.setTitle(path);
				});
			}catch(Exception e) {
				stopClient();
				break;
			}
		}
	}*/
	
	
	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));
		HBox hbox = new HBox();
		hbox.setSpacing(5);
		TextField userName = new TextField();
		userName.setPrefWidth(150);
		userName.setPromptText("닉네임을 입력하세요.");
		HBox.setHgrow(userName, Priority.ALWAYS);
		
		TextField IPText = new TextField("192.168.0.77");
		TextField portText = new TextField("22222");
		IPText.setEditable(false);
		IPText.setPrefWidth(100);
		
		
		
		textArea = new TextArea();
		textArea.setEditable(false);
		root.setCenter(textArea);
		
		TextField input = new TextField();
		input.setPrefWidth(Double.MAX_VALUE);
		input.setDisable(true);
		
		input.setOnAction(event -> {
			send(userName.getText()+ ": "+input.getText()+"\n");
			input.setText("");
			input.requestFocus();
		});
		
		Button search = new Button("사진");
		search.setDisable(true);
		search.setOnAction(event ->{
			FileChooser fc = new FileChooser();
			fc.setTitle("사진 선택");
			fc.setInitialDirectory(new File("C:/"));
			ExtensionFilter imgType = new ExtensionFilter("image file", "*.jpg","*.gif","*.png");
			fc.getExtensionFilters().add(imgType);
			File selectFile = fc.showOpenDialog(null);
			String path = selectFile.getAbsolutePath();
			
			
			uploadImage(path);
			input.requestFocus();
		});
		
		Button sendButton = new Button("보내기");
		sendButton.setDisable(true);
		
		sendButton.setOnAction(event -> {
			send(userName.getText() + ": "+input.getText()+"\n");
			input.setText("");
			input.requestFocus();
		});
		
		
		
		Button connectionButton = new Button("접속하기");
		connectionButton.setOnAction(event ->{
			if(connectionButton.getText().equals("접속하기")) {
				int port = 22222;
				try {
					port = Integer.parseInt(portText.getText());
				}catch(Exception e) {
					e.printStackTrace();
				}
				if(userName.getText().equals("")) {
					JOptionPane.showMessageDialog(null, "닉네임을 입력해주세요!");
					return;
				}
				startClient(IPText.getText(), port);
				Platform.runLater(() -> {
					JOptionPane.showMessageDialog(null, "채팅방 접속 성공!");
				});
				userName.setEditable(false);
				connectionButton.setText("종료하기");
				input.setDisable(false);
				sendButton.setDisable(false);
				search.setDisable(false);
				input.requestFocus();
			} else {
				stopClient();
				Platform.runLater(() ->{
					JOptionPane.showMessageDialog(null, "접속 종료!");
					textArea.setText(null);
				});
				userName.setEditable(true);
				connectionButton.setText("접속하기");
				input.setDisable(true);
				sendButton.setDisable(true);
			}
		});
		hbox.getChildren().addAll(userName, IPText, connectionButton);
		root.setTop(hbox);
		BorderPane pane = new BorderPane();
		
		pane.setLeft(search);
		pane.setCenter(input);
		pane.setRight(sendButton);
		
		root.setBottom(pane);
		scene = new Scene(root,400,400);
		primaryStage.setTitle("까똑");
		
		primaryStage.setScene(scene);
		primaryStage.getIcons().add(new Image("file:resources/images/emoji.png"));
		primaryStage.setOnCloseRequest(event -> stopClient());
		primaryStage.show();
		connectionButton.requestFocus();
		
	}
	 

	public static void main(String[] args) {
		launch(args);
	}
}
