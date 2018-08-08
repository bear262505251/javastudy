package com.xiongxiangming.nio;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * 一个简单的nio服务端程序
 * @author Sammy Xiong
 */
public class NioServer {

    private Selector selector;

    private void init(int port) throws Exception{
        //通过静态方法开启一个selector
        selector = Selector.open();
        //创建一个ServerSocketChannel
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking( false );
        ServerSocket ss = ssc.socket();
        InetSocketAddress address = new InetSocketAddress(port);
        ss.bind( address );
        //将ServerSocketChannel注册到selector上，并关注OP_ACCEPT事件
        ssc.register( selector, SelectionKey.OP_ACCEPT );
        System.out.println( "Going to listen on " + port);
    }

    private void listen() throws Exception {
        while(true){
            //开始监听事件，该方法会阻塞，只要有一个channel有事件，则立马返回
            selector.select();
            Set selectedKeys = selector.selectedKeys();
            Iterator it = selectedKeys.iterator();
            //遍历selectedKeys
            while (it.hasNext()){
                SelectionKey key = (SelectionKey)it.next();
                it.remove();
                if ((key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {
                    ServerSocketChannel ssc = (ServerSocketChannel)key.channel();
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking( false );
                    //将socketchannel注册到selector，监听数据
                    sc.register( selector, SelectionKey.OP_READ );
                }else if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ){
                    SocketChannel sc = (SocketChannel)key.channel();
                    ByteBuffer byteBuffer = ByteBuffer.allocate( 512 );
                    while(true){
                        int number = sc.read(byteBuffer);
                        //连接断开
                        if (number < 0){
                            key.cancel();
                            break;
                            //暂时没有数据
                        }else if (number == 0){
                            break;
                            //收到数据
                        }else {
                            System.out.println("收到" + number + "字节的数据");
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws Exception{
        NioServer nioServer = new NioServer();
        nioServer.init(8081);
        nioServer.listen();
    }
}
