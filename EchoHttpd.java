/* Copyright(c) 2013 M Hata
 This software is released under the MIT License.
 http://opensource.org/licenses/mit-license.php */
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 */
public class EchoHttpd {
    static private final int DEFAULT_PROXY_PORT = 8080;
    static private final byte[] RESPONSE = (
        "HTTP/1.0 200 OK\r\n" +
        "Content-type: text/plain\r\n" +
        "Connection: close\r\n" +
        "\r\n").getBytes();

    public static void usage() {
        System.err.println("usage: java tabou.http.EchoHttp [-p port]");
        System.exit(-1);
    }
    public static void main(String[] args) throws Exception{
        int localPort = DEFAULT_PROXY_PORT;  /* ポート番号取得       */
        EchoHttpd echoHttpd = new EchoHttpd();
        int argi = 0;
        for (; argi < args.length; argi++) {
            char[] chars = args[argi].toCharArray();
            if (chars[0] != '-') {
                break;
            }
            if (chars.length != 2) {
                System.err.println("invalid option:" + args[argi]);
                usage();
            }
            char c = chars[1];
            switch (c) {
            case 'p':
                localPort = Integer.parseInt(args[++argi]);  /* ポート番号取得       */
                break;
            default:
                System.err.println("invalid option:" + c);
                usage();
            }
        }
        echoHttpd.accrpt(localPort);
    }
    public void accrpt(int localPort) throws IOException{
        try(ServerSocket serverSocket = new ServerSocket(localPort)){
            while (true) {
                System.out.println("wait:*."+ localPort  +" ...");
                try(Socket requestSocket = serverSocket.accept()){
                    System.out.println("accept:" + requestSocket.getInetAddress());
                    request(requestSocket);
                }catch(Exception e){
                    System.err.println(e.toString());
                }
                System.out.println("close");
            }
        }
    }
    public void request(Socket requestSocket) throws IOException{
        requestSocket.setSoTimeout(1000 * 100);
        try(BufferedInputStream requestIn =
            new BufferedInputStream(requestSocket.getInputStream())){
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(RESPONSE);
            String firstLine = readLine(requestIn,byteArrayOutputStream);
            System.out.println(firstLine);
            int contentLength = 0;
            while(true){
                String line = readLine(requestIn,byteArrayOutputStream);
                if(line == null){
                    break;
                }else if(line.equals("")){
                    break;
                }
                System.out.println(line);
                int index = line.indexOf(':');
                String tagName = line.substring(0,index).toUpperCase();
                String value   = line.substring(index +1).trim();
                if(tagName.equals("CONTENT-LENGTH")){
                    contentLength = Integer.parseInt(value);
                }
            }
            for(int i = 0;i < contentLength;i++){
                int c = requestIn.read();
                if(c < 0){
                    break;
                }
                byteArrayOutputStream.write(c);
            }
            try(OutputStream requestOut = requestSocket.getOutputStream()){
                requestOut.write(byteArrayOutputStream.toByteArray());
            }
        }
    }
    /**
     * 一行読み込み
     */
    public String readLine(InputStream in,ByteArrayOutputStream byteArrayOutputStream) throws IOException{
        ByteArrayOutputStream sb = new ByteArrayOutputStream();
        while(true){
            int c = in.read();
            if(c < 0){
                if(sb.size() == 0){
                    return null; //読み込み終了
                }
                break;
            }else{
                byteArrayOutputStream.write(c);
                if(c == '\n'){
                    break;
                }else if(c == '\r'){
                }else{
                    sb.write(c);
                }
            }
        }
        return sb.toString();
    }
}
