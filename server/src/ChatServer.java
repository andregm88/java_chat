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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
// endregion

//region CLASSE ChatServer

public class ChatServer {

    //region Atributos
    private int serverPort;
    private List<ChatClientData> chatClients;
    private List<ChatClientData> chatClientsTemp;
    //endregion

    //region Construtores
    public ChatServer(int serverPort)
    {
        this.serverPort = serverPort;
        this.chatClients = new ArrayList<ChatClientData>();
        this.chatClientsTemp = new ArrayList<ChatClientData>();
    }
    // endregion

    //region METODOS

    // region METODO AddUser
    // Metodo que recebe uma variavel do tipo boolean que vem do metodo ValidaUser e recebe um novo cliente com o tipo de dados "ChatClientData"
    public String AddUser(boolean value, ChatClientData newChatClient)
    {
        // variavel
        String result;

        if(value == true)
        {
            chatClients.add(newChatClient);
            result = "> utilizador " + newChatClient.getUsername().toString()+ " autenticado no Servidor";
        }
        else
        {
            result = "> nome de utilizador " + newChatClient.getUsername().toString()+ " não aceite... já existe no servidor, efectue nova tentativa:";
        }
        return result;
    }
    // endregion



    // region METODO ValidateUser
    // Metodo que recebe uma string e valida se ja existe um cliente com nome igual na lista de clientes ligados ao servidor
    // valida/verifica se o utilizador existe
    public boolean ValidateUser(String user) {

        if (user.equals("exit") || user.equals("all") || user.equals("online") || user.equals("help")) return false;

        for (ChatClientData c : chatClients) {

            // verifica se existe usuário igual
            // compara os dois a "lowercase"
            if (c.getUsername().toLowerCase().equals(user.toLowerCase()))
            {
                return false;
            }
        }
        return true;
    }
    //endregion

    // region METODO SendToUser
    // Metodo que ira detectar para que cliente se dirige a mensagem e envia para ele
    // enviar mensagem para o utilizador pedido
    public boolean SendToUser(String clientSending, String user, String message) {

        // verifica por cada "cliente"
        for (ChatClientData c : chatClients) {

            // se o nome do "cliente" for igual ao nome pedido
            if (c.getUsername().toLowerCase().equals(user.toLowerCase())) {

                // declarar socket e printstream
                Socket clienteS = c.getClientSocket();
                PrintStream msg=null;

                try {
                    msg = new PrintStream(clienteS.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // imprimir a mensagem no cliente
                msg.println(clientSending + ": " + message);

                return true;
            }
        }
        return false;
    }
    // endregion

    // region METODO RemoveUser
    // Metodo que recebe um username atraves de string, verifica se existe na lista de clientes e remove.
    public boolean RemoveUser(String username)
    {
        for(ChatClientData c: chatClients)
        {
            if(c.getUsername().equals(username))
            {
                chatClients.remove(c);
                return true;
            }
        }
        return false;
    }
    // endregion

    // region METODO SendToUser
    // Metodo que envia mensagens para todos os clientes online, excepto para o utilizar que envia essa mensagem
    // enviar mensagem para todos utilizadores (menos o que envia)
    public boolean SendToUser(String clientSending, String message) {

        // variavel validação
        int i=0;

        // verifica por cada "cliente" e envia
        for (ChatClientData c : chatClients) {

            // se o nome do "cliente" for diferente ao nome de quem envia
            if (!c.getUsername().toLowerCase().equals(clientSending.toLowerCase())) {

                // declarar socket e printstream
                Socket clienteS = c.getClientSocket();
                PrintStream msg=null;

                try {
                    msg = new PrintStream(clienteS.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // imprimir a mensagem no cliente
                msg.println(clientSending + ": " + message);

                i++;
            }
        }

        // validação
        if (i>0){
            return true;
        }else{
            return false;
        }
    }
    // endregion

    // region METODO LogoutAlert
    // Metodo que avisa todos os utilizadores presentes no chat que x utilizador terminou sessão.
    // envia mensagem a todos a dizer que utilizador terminou sessao
    public void LogoutAlert (String username)
    {

        for (ChatClientData c: chatClients) {

            Socket clienteS = c.getClientSocket();
            PrintStream msg=null;

            try {
                msg = new PrintStream(clienteS.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            // imprimir a mensagem no cliente
            msg.println(username + " terminou sessão!");

        }
    }
    //endregion

    //region METODO ListOn
    // Metodo que imprie todos os utilizadores online no momento
    // listar os utilizadores online
    public int ListOn(PrintStream client, String user)
    {
        // variavel validação
        int i=0;

        for (ChatClientData c : chatClients) {

            // se o nome do "cliente" for diferente do nome do "user"
            if (!c.getUsername().toLowerCase().equals(user.toLowerCase())) {

                client.println(" > " + c.getUsername().toString());
                i++;
            }
        }

        return i;
    }

    public int ListOn(PrintStream client)
    {
        // variavel validação
        int i=0;

        for (ChatClientData c : chatClients) {

            if (i==0) client.println("\nLista de utilizadores online: ");

            client.println(" > " + c.getUsername().toString());
            i++;
        }

        return i;
    }

    //endregion


    //region METODO StartServer
    //metodo que inicia um novo servidor de chat na porta excolhida
    public void StartServer() throws IOException
    {
        ServerSocket server = new ServerSocket(this.serverPort);
        System.out.println("\nServidor inicializado com sucesso na porta: " + serverPort);
        System.out.println("Aguarda ligação de clientes...");

        while (true) {
            Socket clientChat = server.accept();
            System.out.println("> novo cliente ligado: " + clientChat.getRemoteSocketAddress().toString());

            //cria novo cliente e adiciona cliente a lista temporaria
            PrintStream newClientStream = new PrintStream(clientChat.getOutputStream());
            ChatClientData newClient = new ChatClientData("", clientChat);

            this.chatClientsTemp.add(newClient);

            //cria thread para "lidar" com novo cliente
            ChatServerThread newClientThread = new ChatServerThread(clientChat.getInputStream(), this, newClient);
            new Thread(newClientThread).start();
        }
    }
    //endregion

    //endregion

    // region MAIN
    public static void main(String[] args) throws IOException {

        // porta para iniciar servidor
        int serverPort = 5000;

        // inicia o Server
        new ChatServer(serverPort).StartServer();
    }
    // endregion
}
//endregion
