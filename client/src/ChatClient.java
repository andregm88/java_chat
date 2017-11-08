// region HEADER
/**
 * Sistemas Operativos e Sistemas Distribuídos (2º ESIPL)
 * Trabalho Proposto: Chat - Desenvolvimento de um programa de conversação.
 *
 *  3893 - André Fernandes
 * 10847 - Sérgio Cruz
 * 11198 - André Martins
 */
// endregion

// region PACKAGE's - IMPORT's
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.StringJoiner;
// endregion


// region Class ChatClient
public class ChatClient {

    // region Atributos
    private String serverHost;
    private int serverPort;
    // endregion

    // region Construtores
    public ChatClient(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }
    // endregion


    // region Metodo Validação Host/Port
    public static boolean VerifyHost(String data)
    {
        data = data.replace(".", "");

        try
        {
            Integer.parseInt(data);
            return true;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
        catch (NullPointerException e)
        {
            return false;
        }
    }
    // endregion


    // region Metodo startClient
    public void startClient() throws IOException
    {
        try {
            Socket client = new Socket(this.serverHost, this.serverPort);

            System.out.println("Ligação ao servidor com sucesso!");


            // cria thread para receber as mensagens do servidor
            ChatClientThread newClientThread = new ChatClientThread(client.getInputStream());
            new Thread(newClientThread).start();

            // le mensagens do teclado e envia para o servidor
            Scanner readMsg = new Scanner(System.in);
            PrintStream exit = new PrintStream(client.getOutputStream());
            while (readMsg.hasNextLine()) {
                String clientMsg = readMsg.nextLine();

                if (clientMsg.equals(".exit")) {
                    exit.println(clientMsg);
                    break;
                } else {
                    exit.println(clientMsg);
                }

            }
            //fecha ligação
            exit.close();
            readMsg.close();
            client.close();
        }
        catch (Exception e)
        {
            System.out.println("\nERRO: Não foi possivel estabelecer ligação ao servidor " + serverHost + " na porta " + serverPort);
        }
    }
    // endregion



    // region Main
    public static void main(String[] args) throws UnknownHostException, IOException {

        // variaveis
        String serverHost;
        int serverPort;

        Scanner s=new Scanner(System.in);

        // inserir o IP
        System.out.println("Insira o IP do servidor: ");
        serverHost = s.nextLine();

        if (serverHost.equals("auto"))
        {
            // dados automaticamente
            serverHost="127.0.0.1";
            serverPort=5000;
        }
        else
        {
            // verifica se o valor inserido para o IP é válido
            while (!VerifyHost(serverHost))
            {
                // inserir novamento o IP
                System.out.println("O valor inserido é inválido... Insira o IP do servidor: ");
                serverHost = s.nextLine();
            }


            // inserir a Porta
            System.out.println("Insira o numero da porta do servidor: ");

            // aguarda até que seja inserido um valor numerico
            while(!s.hasNextInt())
            {
                s.next();
                System.out.println("O valor inserido é inválido... Insira o numero da porta do servidor: ");
            }
            serverPort = s.nextInt();
        }

        // inicia Client
        new ChatClient(serverHost, serverPort).startClient();
    }
    // endregion

}
// endregion Class