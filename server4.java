//This program implements the server program
package code;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.Semaphore;

// This is the main class that implements the server application
public class server4 extends Thread {
//	These are some of the variables used in the class
	private ServerSocket ss;
	static int i = 0;
//	Used for mainting the active list of clients in the program
	static List<Socket> clientList = new ArrayList<Socket>();
	DataInputStream dis, dis1;
	DataOutputStream dos, dos1;
	String fileName;
	Boolean sync = true;
//	we have used semaphores in the server for effective thread management
	Semaphore bs = new Semaphore(1);

// constructor for the class and has port number as the parameter
	public server4(int port) {
		try {
			ss = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

// as we extend THread we are overiding run method  
	@Override
	public void run() {
		while (true) {
			try {
//				We accept clients as and when they request
				Socket clientSock = ss.accept();
				clientList.add(clientSock);
				Socket clientSock1 = ss.accept();
				clientList.add(clientSock1);
//				Using various Data Input and Output streams for communication with the client
				dis = new DataInputStream(clientSock.getInputStream());
				dos = new DataOutputStream(clientSock.getOutputStream());
				dis1 = new DataInputStream(clientSock1.getInputStream());
				dos1 = new DataOutputStream(clientSock1.getOutputStream());
				ClientHandler mtch = new ClientHandler(clientSock, "1", dis, dos);
				ClientHandler mtch1 = new ClientHandler(clientSock1, "2", dis1, dos1);
//				We are starting one thread for one client each time a client connects
				Thread t1 = new Thread(mtch);
				Thread t2 = new Thread(mtch1);
				t1.start();
				t2.start();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

// This is a clienthandler class where we use this to communicate effectively with the client
	class ClientHandler implements Runnable {
		Scanner scn = new Scanner(System.in);
		private String name;
		Socket s1;
		boolean isloggedin;
		DataInputStream dis3, dis4;
		DataOutputStream dos3;
		String recv, send;

//		Constructor fo the class and uses socket object of the client, client name, Input/output stream variables.
		public ClientHandler(Socket s, String name, DataInputStream dist, DataOutputStream dost) {
			this.dis3 = dist;
			this.dos3 = dost;
			this.name = name;
			this.s1 = s;
			this.isloggedin = true;

		}

// Overiding the run method for server client communication
		@Override
		public synchronized void run() {
			try {
//				using dataoutputstream object we write thing to the client for it to take up it is synchronous communication
				dos3.writeUTF(this.name);
//				This method runs indefinitely until the client is open
				while (true) {
					String msg = dis3.readUTF();
//					System.out.println("msg" + msg);
					if (msg.contains("fileTransfer")) {
						System.out.println("Waiting to acquire permit by client " + name);
//						we need to acquire the lock before proceeding the critical section of saving the file in the server folder
						bs.acquireUninterruptibly();
						System.out.println("Permit acquired for client " + name);
//						below method saves the file in the server folder
						saveFile(dis3, name);
//						release the permit once file saved
						bs.release();
//						notify any waiting threads
						notifyAll();
						System.out.println("Released by " + name);
					}
					if (msg.contains("syncSelected")) {
						sync = true;

					}
					if (msg.contains("syncDeselected")) {
						sync = false;
					}
					if (msg.contains("syncStart")) {
//						File file1 = new File("E:\\Server");
//						String[] t = file1.list();
//						for (i=0;i<t.length;i++) {
//							Path sourceDirectory = Paths.get("E:/Server/"+t[0]);
//					        Path targetDirectory = Paths.get("E:/Client"+name+"/"+t[0]);
//					        //copy source to target using Files Class
//					        Files.copy(sourceDirectory, targetDirectory,StandardCopyOption.REPLACE_EXISTING);
//						}
					}
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

// its a sync method for saveing the file 
	private synchronized void saveFile(DataInputStream dis, String name) throws IOException {

		try {
//			Reads the file name of the file that being sent by the client
			fileName = dis.readUTF();
			FileWriter myWriter = new FileWriter("E:/Server/" + fileName);
			myWriter.close();
			FileWriter myWriter1 = new FileWriter("E:/Client" + name + "/" + fileName);
			myWriter1.close();
//			These are the file output streams that are taken so that the the files are sent to appropriate clients and server folders
			FileOutputStream fos = new FileOutputStream("E:/Server/" + fileName);
			FileOutputStream fos1 = new FileOutputStream("E:/Client" + name + "/" + fileName);
			FileOutputStream fos2 = null;
			if (sync) {
				if (name.contains("2")) {
//					This is used to create a file with the filename if it doesn't exits
					FileWriter myWriter2 = new FileWriter("E:/Client1/" + fileName);
					myWriter2.close();
					fos2 = new FileOutputStream("E:/Client1/" + fileName);
				} else {
					FileWriter myWriter2 = new FileWriter("E:/Client2/" + fileName);
					myWriter2.close();
					fos2 = new FileOutputStream("E:/Client2/" + fileName);
				}
			}

			byte[] buffer = new byte[1024];

			int filesize = 15123;
			int read = 0;
			int totalRead = 0;
			int remaining = filesize;
// read 1024 bytes of data at a time
			while ((read = dis.read(buffer)) > 0) {
				totalRead += read;
				remaining -= read;
//				System.out.println("read " + totalRead + " bytes." + buffer.toString());
				fos.write(buffer, 0, read);
				fos1.write(buffer, 0, read);
				if (sync) {
					fos2.write(buffer, 0, read);
				}
				if (read < 1024) {
					break;
				}
			}
//			Close the file once the write operation is complete
			fos.close();
			fos1.close();
			if (sync) {
				fos2.close();
			}

		} catch (Exception e) {

		}
	}
//	public class BinarySemaphore {
//
//	    private final Semaphore countingSemaphore;
//
//	    public BinarySemaphore(boolean available) {
//	        if (available) {
//	            countingSemaphore = new Semaphore(1, true);
//	        } else {
//	            countingSemaphore = new Semaphore(0, true);
//	        }
//	    }
//
//	    public void acquire() throws InterruptedException {
//	        countingSemaphore.acquire();
//	    }
//
//	    public synchronized void release() {
//	        if (countingSemaphore.availablePermits() != 1) {
//	            countingSemaphore.release();
//	        }
//	    }
//	}

//	Main method
	public static void main(String[] args) {
		server4 fs = new server4(1988);
		fs.start();
	}
}