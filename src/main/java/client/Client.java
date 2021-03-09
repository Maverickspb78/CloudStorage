package client;


import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class Client {
	private final Socket socket;
	private final DataInputStream in;
	private final DataOutputStream out;

	private int height = 300;
	private int width = 400;

	private DefaultListModel<String> listModel;



	public Client() throws IOException {
		socket = new Socket("localhost", 1235);
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
		runClient();
	}

	private void runClient() {

		try {
			System.out.println(in.readUTF());
		} catch (IOException e) {
			e.printStackTrace();
		}

		listModel = new DefaultListModel<String>();

		JFrame frame = new JFrame("Cloud Storage");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(new Dimension(width,height));
		frame.setLocationRelativeTo(null);
		JPanel panel = new JPanel();
		JTextArea ta = new JTextArea();

		// TODO: 02.03.2021
		// list file - JList

		File rootFolder = new File("server");
		String[] filesTemp = rootFolder.list();
		for (int i=0; i < filesTemp.length; i++){
			listModel.addElement(filesTemp[i]);
		}
		JList<String> list = new JList<String>(listModel);

		JButton uploadButton = new JButton("Upload");
		uploadButton.setSize(20,40);

		JButton downloadButton = new JButton("download");
		downloadButton.setSize(20,40);

		JButton removeButton = new JButton("remove");
		removeButton.setSize(20,40);

		frame.getContentPane().add(BorderLayout.NORTH, ta);
		frame.getContentPane().add(BorderLayout.CENTER, list);
		frame.getContentPane().add(BorderLayout.SOUTH, panel);
		panel.add(uploadButton);
		panel.add(downloadButton);
		panel.add(removeButton);

		frame.setVisible(true);

		uploadButton.addActionListener(a -> {
			System.out.println(sendFile(ta.getText()));
			if (listModel.contains(ta.getText())){

			}else {
				listModel.addElement(ta.getText());
				System.out.println("Новый :" + listModel);
			}
		});

		downloadButton.addActionListener(a -> {
			System.out.println(downloadFile(ta.getText()));
		});
		removeButton.addActionListener(a -> {
			System.out.println(remove(ta.getText()));

			for (int i = 0; i < listModel.size(); i++) {
				System.out.println("ta :" + ta.getText());


				if (listModel.getElementAt(i).contains(ta.getText())){
					System.out.println("Сработало перед удалением: " + listModel.getElementAt(i));
					listModel.remove(i);
					System.out.println("Новый :" + listModel);
					break;


				}


			}




		});
		list.addListSelectionListener(a ->{
			ta.setText(list.getSelectedValue());
		});
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
				out.writeUTF("download");
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

	private String remove(String filename){
		try {
			File file = new File("server" + File.separator + filename);
			if (file.exists()) {
				out.writeUTF("remove");
				out.writeUTF(filename);
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

	public static void main(String[] args) throws IOException {
		new Client();
	}
}
