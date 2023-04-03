import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class MulticastChat {

    private static final String MULTICAST_ADDRESS = "239.0.0.1";
    private static final int PORT = 1234;

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);

            System.out.print("Digite seu nome: ");
            String userName = scanner.nextLine();

            System.out.print("Digite o tópico de interesse (Entretenimento ou Esportes): ");
            String topic = scanner.nextLine().toLowerCase();

            MulticastSocket socket = new MulticastSocket(PORT);

            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            socket.joinGroup(group);

            Thread receiverThread = new Thread(new MessageReceiver(socket, userName, topic));
            receiverThread.start();

            while (true) {
                System.out.print("Digite uma mensagem ou SAIR para sair do grupo: ");
                String message = scanner.nextLine();

                if (message.equalsIgnoreCase("SAIR")) {
                    String leaveMessage = String.format("[%s] %s saiu do grupo.", getCurrentTime(), userName);
                    byte[] leaveData = leaveMessage.getBytes();
                    DatagramPacket leavePacket = new DatagramPacket(leaveData, leaveData.length, group, PORT);
                    socket.send(leavePacket);

                    socket.leaveGroup(group);
                    socket.close();

                    break;
                }

                String formattedMessage = String.format("[%s] %s : %s", getCurrentTime(), userName, message);
                byte[] data = formattedMessage.getBytes();
                DatagramPacket packet = new DatagramPacket(data, data.length, group, PORT);
                socket.send(packet);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getCurrentTime() {
        return new SimpleDateFormat("dd/MM/yyyy - HH:mm").format(new Date());
    }

    private static class MessageReceiver implements Runnable {

        private MulticastSocket socket;
        private String userName;
        private String topic;

        public MessageReceiver(MulticastSocket socket, String userName, String topic) {
            this.socket = socket;
            this.userName = userName;
            this.topic = topic;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    String message = new String(packet.getData(), 0, packet.getLength());

                    if (message.toLowerCase().contains(topic) || message.contains(userName)) {
                        System.out.println(message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
