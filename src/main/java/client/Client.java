package client;


import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Client {
	private final Socket socket;
	private final DataInputStream in;
	private final DataOutputStream out;

	private int height = 300;
	private int width = 400;
	private DefaultListModel<String> myModel = new DefaultListModel<>();


	public Client() throws IOException {
		socket = new Socket("localhost", 1234);
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
		runClient();
	}

	private void runClient() {
		JFrame frame = new JFrame("Cloud Storage");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(new Dimension(width,height));
		frame.setLocationRelativeTo(null);
		JPanel panel = new JPanel();
		JTextArea ta = new JTextArea();
		Icon iconRefresh = new ImageIcon("icon\\refresh2.png");


		JList<String> list = new JList<>();
//		DefaultListModel<String> myModel = new DefaultListModel<>();
		list.setModel(myModel);

		JButton uploadButton = new JButton("Upload");
		uploadButton.setSize(20,40);

		JButton downloadButton = new JButton("download");
		downloadButton.setSize(20,40);

		JButton removeButton = new JButton("remove");
		removeButton.setSize(20,40);

		JButton refreshButton = new JButton(iconRefresh);
		refreshButton.setSize(20,20);


		frame.getContentPane().add(BorderLayout.NORTH, ta);
		frame.getContentPane().add(BorderLayout.CENTER, new JScrollPane(list));
		frame.getContentPane().add(BorderLayout.SOUTH, panel);
		panel.add(uploadButton);
		panel.add(downloadButton);
		panel.add(removeButton);
		panel.add(refreshButton);

		fillList(myModel);

		frame.setVisible(true);

		uploadButton.addActionListener(a -> {
			System.out.println(sendFile(ta.getText()));
		});
		downloadButton.addActionListener(a -> {
			System.out.println(downloadFile(ta.getText()));
		});
		removeButton.addActionListener(a -> {
			System.out.println(remove(ta.getText()));
			fillList(myModel);
		});
		refreshButton.addActionListener(a -> {
			fillList(myModel);
		});
		list.addListSelectionListener(a ->{
			ta.setText(list.getSelectedValue());
		});

	}

	private void fillList(DefaultListModel<String> myModel) {
		List<String> list =  downloadFileList();
		myModel.clear();
		for (String filename : list) {
			myModel.addElement(filename);
		}
	}

	private List<String> downloadFileList() {
		List<String> list = new ArrayList<String>();
		try {
			StringBuilder sb = new StringBuilder();
			out.write("list-files".getBytes(StandardCharsets.UTF_8));
			while (true) {
				byte[] buffer = new byte[512];
				int size = in.read(buffer);
				sb.append(new String(buffer, 0, size));
				if (sb.toString().endsWith("end")) {
					break;
				}
			}
			String fileString = sb.substring(0, sb.toString().length() - 4);
			list = Arrays.asList(fileString.split("\n"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return list;
	}

	private String sendFile(String filename) {
		try {
			File file = new File("client" + File.separator + filename);
			if (file.exists()) {
				out.writeUTF("upload");
				out.writeUTF(filename);
				long length = file.length();
				out.writeLong(length);
				FileInputStream fis = new FileInputStream(file);
				int read = 0;
				byte[] buffer = new byte[256];
				while ((read = fis.read(buffer)) != -1) {
					out.write(buffer, 0, read);
				}
				out.flush();
				out.writeUTF("list-files");
				out.flush();
				String status = in.readUTF();
				return status;
			} else {
				return "File is not exists";
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Something error";
	}

	private String downloadFile(String filename) {
		try {
			File file = new File("server" + File.separator + filename);
			if (file.exists()) {
				out.writeUTF("download " + filename);
//				out.writeUTF(filename);
				long length = file.length();
				out.writeLong(length);
				FileInputStream fis = new FileInputStream(file);
				int read = 0;
				byte[] buffer = new byte[256];
				while ((read = fis.read(buffer)) != -1) {
					out.write(buffer, 0, read);
				}
				out.flush();

				byte [] b = in.readAllBytes();
				String status = "";
				for(byte a : b)
					status+=a;
				System.out.println(status);
//				String status = in.readUTF();
				return status;
			} else {
				return "File is not exists";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Something error";
	}

	private String remove(String filename){
		try {

			File file = new File("server" + File.separator + filename);
			if (file.exists()) {
				String msg = "remove " + filename;
				out.write(msg.getBytes(StandardCharsets.UTF_8));
				out.flush();
//				String status = in.readUTF();
//				System.out.println(status);

				StringBuilder sbr = new StringBuilder();
				while (true) {
					byte[] buffer = new byte[512];
					int size = in.read(buffer);
					sbr.append(new String(buffer, 0, size));
					if (sbr.toString().endsWith("end")) {
						break;
					}
				}


				String status = sbr.substring(0, sbr.toString().length() - 4);
				out.write("list-files".getBytes(StandardCharsets.UTF_8));
				out.flush();



				return status;

			} else {
				return "File is not exists";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Something error";
	}

	public static void main(String[] args) throws IOException {
		new Client();
	}
}
