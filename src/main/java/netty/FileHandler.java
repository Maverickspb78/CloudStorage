package netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class FileHandler extends SimpleChannelInboundHandler<String> {
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("Client connected: " + ctx.channel().remoteAddress());
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {

		String filename = "";

		System.out.println("Message: " + msg);
		System.out.println(msg.startsWith("list-files"));
		String command = msg;
		System.out.println("command replace: " + command);
		if (command.equals("list-files")) {
			File file = new File("server");
			File[] files = file.listFiles();
			StringBuffer sb = new StringBuffer();
			for (File f : files) {
				sb.append(f.getName() + "\n");
			}
			sb.append("end");
			ctx.writeAndFlush(sb.toString());
		} else if (command.startsWith("remove")) {

			filename = command.split(" ")[1];

			remove(ctx, filename);

		} else if (command.startsWith("download")) {
			System.out.println("download command: " + command);
			filename = command.split(" ")[1];
			System.out.println("download command: " + filename);
			ctx.write(("download" + "end").getBytes());
			download(ctx, filename);
		} else {
			System.out.println("Chanel closed");
			ctx.channel().closeFuture();
			ctx.channel().close();
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("Client disconnected: " + ctx.channel().remoteAddress());
	}

	public void remove(ChannelHandlerContext ctx, String filename) throws IOException {
		try {
			File file = new File("server" + File.separator + filename);
			if (file.exists()) {
				if (file.delete()) {
					ctx.writeAndFlush("File " + filename +" deleted from server\nend");
				} else {
					ctx.writeAndFlush("The file" + filename +" is not deleted from the server\nend");
				}
			}

		} catch (Exception e) {
			ctx.writeAndFlush("ERROR\nend".getBytes(StandardCharsets.UTF_8));
		}
	}

	public void download(ChannelHandlerContext ctx, String filename) throws IOException{
		try {
			File file = new File("client" + File.separator + ctx.read());
			if (!file.exists()) {
				file.createNewFile();
			}
			ChannelHandlerContext channelHandlerContext = ctx.read();
			System.out.println(channelHandlerContext);
			ctx.write(channelHandlerContext);
			System.out.println(channelHandlerContext);
//			long size = in.readLong();
//			FileOutputStream fos = new FileOutputStream(file);
//			byte[] buffer = new byte[256];
//			for (int i = 0; i < (size + 255) / 256; i++) { // FIXME
//				int read = in.read(buffer);
//				fos.write(buffer, 0, read);
//			}
//			fos.close();
//			out.writeUTF("DONE");
		} catch (Exception e) {
//			out.writeUTF("ERROR");
		}
	}
}
