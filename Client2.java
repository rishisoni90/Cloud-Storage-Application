//This program implements the client program
package code;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileSystemView;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

//This is the main class for the client and all the operations are handled in this class
public class Client2 extends JFrame implements ActionListener, ListSelectionListener {
//	A few variables that being used through out the program this includes the variables for GUI as well
	public Socket s;
	public String filename;
	static JList jl;
	static JTextArea t, t1;
	static Hashtable<String, String> my_dict;
	static JButton b, b1, b2, b3, b4;
	static JFrame f;
	Scanner sc = new Scanner(System.in);
	static DataOutputStream dos;
	FileInputStream fis;
	String recv, send, clientID;
	static File file = new File("E:\\Client1");
	static String[] fl = file.list();
	static JToggleButton jtbButton;
	static DefaultListModel lm = new DefaultListModel();
	static Boolean toggleState;

//	This is the constructor for the class Client2 and has host and port number as the parameters
	public Client2(String host, int port) {
		try {
			toggleState = true;
//			We are tryign to get a new socket object for further communication with the client
			s = new Socket(host, port);
			clientsendrecv c1 = new clientsendrecv(s);
//			clientsendrecv as name suggest it contains methods to communicate with the server and we are using a thread to handle this
			Thread t1 = new Thread(c1);
			t1.start();
			dos = new DataOutputStream(s.getOutputStream());
		} catch (Exception e) {
			System.out.println("1");
			e.printStackTrace();
		}
	}

//	Main method to run the program
	public static void main(String[] args) {
		try {
			Client2 fc = new Client2("localhost", 1988);
//			These are some of the variables used for GUI
			f = new JFrame("File storage application");
			b = new JButton("Upload");
			t = new JTextArea(16, 10);
			t1 = new JTextArea(16, 10);
			b1 = new JButton("Sync");
			b2 = new JButton("Open");
			b4 = new JButton("Save");
			jl = new JList(lm);
//			This is for the toggle button for sync
			jtbButton = new JToggleButton("Sync On", true);

			ItemListener itemListener = new ItemListener() {

				public void itemStateChanged(ItemEvent itemEvent) {

					// event is generated in button
					int state = itemEvent.getStateChange();
					try {
						// if selected print selected in console
						if (state == ItemEvent.SELECTED) {
							dos.writeUTF("syncSelected");
							System.out.println("Selected");
							toggleState = true;

						} else {
//							dos.writeUTF("fileTransfer");
							// else print deselected in console
							dos.writeUTF("syncDeselected");
							System.out.println("syncDeselected");
							toggleState = false;
						}
					} catch (Exception e) {

					}
				}
			};

//			Some more code lines for the GUI of the application and adding things to the frame
			jtbButton.addItemListener(itemListener);
			t.setBounds(450, 10, 400, 100);
			t1.setBounds(10, 250, 400, 100);
			b.setBounds(10, 150, 95, 30);
			b1.setBounds(150, 150, 95, 30);
			jl.setBounds(10, 10, 400, 100);
			jtbButton.setBounds(10, 200, 100, 30);
			b2.setBounds(300, 150, 95, 30);
			b4.setBounds(450, 320, 95, 30);
			f.add(b);
			f.add(b1);
			f.add(b2);
			f.add(b4);
			f.add(t);
			f.add(t1);
			f.add(jl);
			f.add(jtbButton);
			f.setSize(880, 400);
			f.setLayout(null);
			f.setVisible(true);
			b.addActionListener(fc);
			b1.addActionListener(fc);
			b2.addActionListener(fc);
			b4.addActionListener(fc);
			t.setEditable(false);
			t1.setEditable(true);
			jl.addListSelectionListener(fc);

		} catch (Exception e) {
			System.out.println("2");
		}

	}

//	This is overriding of the method valueChanged so that on selection of the files various actions can be performed.
	@Override
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub
		t.setText(null);
		t.setText((String) jl.getSelectedValue() + "\n");
		File file3 = new File("E:\\Client" + clientID);
		String[] f3 = file3.list();
		Path p = Paths.get("E:\\Client" + clientID + "\\" + (String) jl.getSelectedValue());
		Path p1 = Paths.get("E:\\Server\\" + (String) jl.getSelectedValue());
		try {
			BasicFileAttributes attr = Files.readAttributes(p, BasicFileAttributes.class);
			BasicFileAttributes attr1 = Files.readAttributes(p1, BasicFileAttributes.class);
			if (attr.lastModifiedTime().equals(attr1.lastModifiedTime())) {
				t.append("Synchronized on " + attr.lastModifiedTime());
			} else {
				t.append("Not Synchronized");
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

//	This is actionPerformed on different buttons we use getSource to find out which button is pressed and access its functionalities
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		try {
			if (e.getSource() == b) {
//				JFileChooser j = new JFileChooser(FileSystemView.getFileSystemView().getDefaultDirectory());
//				This is a windows specific application to browse files 
				JFileChooser j = new JFileChooser();
				j.setCurrentDirectory(new java.io.File("E:\\"));
				int r = j.showOpenDialog(null);
				filename = j.getSelectedFile().getAbsolutePath();
				File file2 = new File("E:\\Client" + clientID);
				String[] st = file2.list();
				Boolean flag = false;
				for (int i = 0; i < st.length; i++) {
//					System.out.println(j.getSelectedFile().getName()+"---"+st[i]);
					if (j.getSelectedFile().getName().contains(st[i])) {
						flag = true;
						JOptionPane.showMessageDialog(f, "File name already exsits");
						break;
					}
				}
				if (flag == false) {
					dos.writeUTF("fileTransfer");
					this.sendFile();
				}
			}
			// on the click of sync button following code lines get executed
			if (e.getSource() == b1) {
//				dos.writeUTF("syncStart");
				FileWriter outFile4 = new FileWriter("E:\\Client" + clientID + "\\" + (String) jl.getSelectedValue(),
						false);
				outFile4.write(t1.getText());
				outFile4.close();
				Path sourceDirectory = Paths.get("E:\\Server\\" + (String) jl.getSelectedValue());
				Path targetDirectory = Paths.get("E:\\Client" + clientID + "\\" + (String) jl.getSelectedValue());
				// copy source to target using Files Class
				Files.copy(sourceDirectory, targetDirectory, StandardCopyOption.REPLACE_EXISTING);
			}
			// On the click of open button following set of code lines get executed
			if (e.getSource() == b2) {
				System.out.println("E:\\Client" + clientID + "\\" + (String) jl.getSelectedValue());
				FileReader reader = new FileReader("E:\\Client" + clientID + "\\" + (String) jl.getSelectedValue());
				t1.setText(null);
				t1.read(reader, "Open edit");
				reader.close();
			}
			// On the click of save button following set of code lines get executed
			if (e.getSource() == b4) {
				FileWriter outFile = new FileWriter("E:\\Client" + clientID + "\\" + (String) jl.getSelectedValue(),
						false);
				outFile.write(t1.getText());
				outFile.close();
				FileWriter outFile1 = new FileWriter("E:\\Server\\" + (String) jl.getSelectedValue(), false);
				outFile1.write(t1.getText());
				outFile1.close();
				if (toggleState) {
					if (clientID.contains("1")) {
						FileWriter outFile2 = new FileWriter("E:\\Client" + "2" + "\\" + (String) jl.getSelectedValue(),
								false);
						outFile2.write(t1.getText());
						outFile2.close();
					} else {
						FileWriter outFile2 = new FileWriter("E:\\Client" + "1" + "\\" + (String) jl.getSelectedValue(),
								false);
						outFile2.write(t1.getText());
						outFile2.close();
					}
				}
			}

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			System.out.println("3");
			e1.printStackTrace();
		}
	}

// Here we have used sync method to send file to the server using dataoutputstream object.
	public synchronized void sendFile() throws IOException {

		DataOutputStream dos2 = new DataOutputStream(s.getOutputStream());
		fis = new FileInputStream(filename);
		System.out.println(filename);
		filename = filename.replace("E:\\", "");
		System.out.println(filename);
		dos2.writeUTF(filename);
		byte[] buffer = new byte[1024];
		while (fis.read(buffer) > 0) {
			dos2.flush();
			dos2.write(buffer);
		}
		dos2.writeUTF("");
		dos2.flush();
		fis.close();
	}

//	This class is used for maintaining the communication between client and server
	class clientsendrecv extends Thread {
		public Socket s1;
		DataInputStream dis;
		DataOutputStream dos = null;

// Contructor of the class clientsendrecv
		public clientsendrecv(Socket s) {
			s1 = s;
			try {
				dis = new DataInputStream(s1.getInputStream());
				dos = new DataOutputStream(s1.getOutputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("4");
				e.printStackTrace();
			}
		}

// as this class extends Threads we are overriding run method 
		@Override
		public void run() {
			try {
				recv = dis.readUTF();
				clientID = recv.toString();
				System.out.println("received " + recv);
//				We start a new thread for file refresh and display in the application monitor
				fileRefresh fr = new fileRefresh();
				Thread t2 = new Thread(fr);
				t2.start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("5");
			}
		}
	}

// This is exclusively for file refresh in the client monitor
	class fileRefresh extends Thread {
		public fileRefresh() {

		}

// Similarly to the above we have overridden the run method this thread runs continuously until the client application is running
		@Override
		public void run() {
			File file1 = new File("E:\\Client" + clientID);
			System.out.println("E:\\Client" + clientID);
			String[] fl1 = file1.list();
			String[] fl2 = {};
			while (true) {
				try {
					fl1 = file1.list();
//					we have used sleep method so that the CPU time is saved 
					sleep(2000);
					if (fl1.length != fl2.length) {
						if (file1 != null) {
							fl2 = file1.list();
						}
//						lm.clear();
						if (lm.isEmpty()) {

						} else {
							lm.clear();
						}
						for (int i = 0; i < fl1.length; i++) {
							if (fl1[i] != null) {
								lm.addElement(fl1[i]);
							}
						}
					}
//				jl.updateUI();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("5");
					break;
				}
			}
		}
	}
}