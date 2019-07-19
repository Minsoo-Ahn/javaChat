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

import javafx.stage.FileChooser;




public class Client {
	Socket socket;
	
	public Client(Socket socket) {
		this.socket = socket;
		receive();
	}
	
	public void receive() {
		Runnable thread = new Runnable() {

			@Override
			public void run() {
				try {
					while(true) {
						InputStream in = socket.getInputStream();
						byte[] buffer = new byte[5000];
						int length = in.read(buffer);
						long cnt = System.currentTimeMillis();
						File f;
						BufferedOutputStream bos;
						//바이트 5000이상일때 파일로 취급해서 아래 위치에 이미지 파일로 저장
						if (length ==5000) {
							f = new File("C:\\Users\\user\\Downloads\\server\\"+cnt+".jpg");
							bos = new BufferedOutputStream(new FileOutputStream(f), 5000);
							
							while(true) {
								bos.write(buffer, 0, length);
								bos.flush();
								if(length < 5000) break;
								length = in.read(buffer);
							}
							for(Client client : Main.clients) {
								client.send(f);
							}
							System.out.println("[이미지 수신 성공]"+
									socket.getRemoteSocketAddress()
									+": " + Thread.currentThread().getName());
							length = -1;
							bos.close();
							
						}
						//그게 아닐경우 일반 메시지 받기
						if(length != -1){
						String message = new String(buffer, 0, length, "UTF-8");
						for(Client client : Main.clients) {
							client.send(message);
						}
						System.out.println("[메시지 수신 성공]"+
						socket.getRemoteSocketAddress()
						+": " + Thread.currentThread().getName());
						
						} 
					}
				
				}catch(Exception e) {
					try {
						e.printStackTrace();
						System.out.println("[메시지 수신 오류] "
								+socket.getRemoteSocketAddress()
								+": "+Thread.currentThread().getName() );
					} catch(Exception e2) {
						e2.printStackTrace();
					}
				}
				
			}
			
		};
		Main.threadPool.submit(thread);
	}
	
	public void send(String message) {
		Runnable thread = new Runnable() {

			@Override
			public void run() {
				try {
					OutputStream out = socket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer);
					out.flush();
				} catch(Exception e) {
					try {
						System.out.println("[메시지 송신 오류]"
								+socket.getRemoteSocketAddress()
								+": " +Thread.currentThread().getName());
						Main.clients.remove(Client.this);
						socket.close();
					} catch(Exception e2) {
						e2.printStackTrace();
					}
				}
				
			}
			
		};
		Main.threadPool.submit(thread);
	}
	public void send(File f) {
		Runnable thread = new Runnable() {

			@Override
			public void run() {
				try {
					OutputStream out = socket.getOutputStream();
					byte[] buffer = new byte[5000];
					BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f), 5000);
					int len;
					while((len = bis.read(buffer)) != -1) {
						out.write(buffer, 0 , len);
						out.flush();
					}
					bis.close();
				} catch(Exception e) {
					try {
						System.out.println("[이미지 송신 오류]"
								+socket.getRemoteSocketAddress()
								+": " +Thread.currentThread().getName());
					} catch(Exception e2) {
						e2.printStackTrace();
					}
				}
				
			}
			
		};
		Main.threadPool.submit(thread);
	}
}
