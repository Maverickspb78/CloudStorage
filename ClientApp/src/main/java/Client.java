import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Client extends JFrame{

	public static void main(String[] args) throws IOException {
		AuthorizationGUI authorizationGUI = new AuthorizationGUI();
		authorizationGUI.setSize(300,150);
		authorizationGUI.setLocationRelativeTo(null);
		authorizationGUI.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
}
