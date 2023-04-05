import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JOptionPane;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MulticastChat {

    private static final String MULTICAST_ADDRESS = "239.0.0.1";
    private static final int PORT1 = 1234;
    private static final int PORT2 = 1235;
    private static int PORT = 1;

    private static String getCurrentTime() {
      return new SimpleDateFormat("dd/MM/yyyy - HH:mm").format(new Date());
    }
    
    public static void main(String[] args) throws IOException {

        try (// Lê o nome e topico do usuário
        Scanner scanner = new Scanner(System.in)) {
         String  name = JOptionPane.showInputDialog("Digite seu nome:");
          String topic = JOptionPane.showInputDialog("Digite o topico:");

          if(topic.equals("Esportes") ){
            PORT=PORT1;
          }else{
            PORT=PORT2;
          }

          // Cria um socket multicast
          InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
          MulticastSocket socket = new MulticastSocket(PORT);
   

          try {
              // Junta-se ao grupo multicast
              socket.joinGroup(new InetSocketAddress(group, PORT), NetworkInterface.getByInetAddress(InetAddress.getLocalHost()));

            
              // Cria uma thread para receber as mensagens do grupo multicast
              Thread receiverThread = new Thread(() -> {
                  try {
                      // Define o tamanho máximo do pacote a ser recebido
                      byte[] buffer = new byte[1024];
                      DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                      while (true) {
                          // Recebe um pacote do grupo multicast
                          socket.receive(packet);

                          // Exibe a mensagem recebida
                          String message = new String(packet.getData(), 0, packet.getLength());
                          String[] parts = message.split(":", 2);
                          String text = parts[1];
                          System.out.println(text);
                      }
                  } catch (IOException e) {
                      e.printStackTrace();
                  }
              });
              receiverThread.start();

              

              // Lê as mensagens a serem enviadas pelo usuário
              System.out.println("Digite uma mensagem a ser enviada ou SAIR");
              while (true) {
                  String text = JOptionPane.showInputDialog("Digite a mensagem a ser enviada ou SAIR:");

                  if (text.equalsIgnoreCase("SAIR")) {
                      // Cria um pacote com a mensagem de saída do usuário
                      SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
                      String message = "[" + formatter.format(new Date()) + "] " + name + " saiu do grupo.";
                      byte[] buffer = message.getBytes();
                      DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);

                      // Envia o pacote para o grupo multicast
                      socket.send(packet);

                      break; // Encerra o loop e encerra o programa
                  } else {
                      // Cria um pacote com a mensagem a ser enviada
                      String formattedMessage = String.format("[%s] %s : %s", getCurrentTime(), name, text);
                      byte[] data = formattedMessage.getBytes();
                      DatagramPacket packet = new DatagramPacket(data, data.length, group, PORT);
                      socket.send(packet);
                      // socket.send(packet);
                      
                  }
              }
              
          } catch (SocketException e) {
              e.printStackTrace();
          } catch (IOException e) {
              e.printStackTrace();
          } finally {
              // Sai do grupo multicast
              try {
                  NetworkInterface netIf = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
                  socket.leaveGroup(new InetSocketAddress(group, PORT), netIf);
              } catch (IOException e) {
                  e.printStackTrace();
              }

              // Fecha o socket
              socket.close();
          }
        }
    }
}
