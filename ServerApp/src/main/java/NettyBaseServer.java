import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolver;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class NettyBaseServer {
	public NettyBaseServer() {
		EventLoopGroup auth = new NioEventLoopGroup(1);
		EventLoopGroup workers = new NioEventLoopGroup();
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(auth, workers)
					.channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							ch.pipeline().addLast("Logger", new LoggingHandler(LogLevel.INFO));
							ch.pipeline().addLast("StringDecoder", new StringDecoder()); // in-1
							ch.pipeline().addLast("StringEncoder", new StringEncoder()); // out-1
							ch.pipeline().addLast("FileHandler", new FileHandler()); // in-2
						}
					});
			ChannelFuture future = bootstrap.bind(1234).sync();
			System.out.println("Server started");
			future.channel().closeFuture().sync();
			System.out.println("Server finished");

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			auth.shutdownGracefully();
			workers.shutdownGracefully();
		}
	}

	public static void main(String[] args) {
		new NettyBaseServer();
	}
}
