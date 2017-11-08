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
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;
// endregion

// region CLASSE ChatServer Thread

public class ChatServerThread implements Runnable
{
    //region Atributos
    private InputStream clientStream;
    private PrintStream clientPrintStream;
    private ChatServer serverD;
    private ChatClientData clientData;
    //endregion

    //region Construtores
    public ChatServerThread(InputStream clientStream, ChatServer serverD, ChatClientData clientData) throws IOException {
        this.clientStream = clientStream;
        this.clientPrintStream = new PrintStream(clientData.getClientSocket().getOutputStream());
        this.serverD = serverD;
        this.clientData = clientData;
    }
    // endregion

    //region METODOS

    //region METODO DefineUser
    // Metodo que tem por objectivo assim que cliente entrar no chat, perguntar qual o nome que pretende usar, caso nome ja esteja a ser usado volta
    // perguntar, ou seja nao premitimos que existam utilizadores com nome igual
    public void DefineUser(Scanner s)
    {
        // variaveis
        String username;
        boolean validate = false;
        int flag = 0;
        int numUser;

        do
        {
            // lista os utilizadores online
            numUser = serverD.ListOn(clientPrintStream);

            clientPrintStream.println("\nInsira o nome de utilizador a utilizar no chat: "); flag++;

            try {
                s = new Scanner(clientData.getClientSocket().getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            // utilizador
            username = s.nextLine().toString();
            System.out.println("> client" + clientData.getClientSocket().getRemoteSocketAddress().toString() + " escolheu como utilizador: " + username );

            ChatClientData newCCD;
            newCCD = new ChatClientData(username, clientData.getClientSocket());

            clientData.setUsername(username);

            // valida a inserção do utilizador
            validate = serverD.ValidateUser(username);

            // caso o username inserido já exista
            if (validate==false) clientPrintStream.println("\nO utilizador inserido já existe.");

            String h = serverD.AddUser(validate, newCCD);
            System.out.println(h);

        }while(validate==false);

        // imprime (no cliente) mensagem de boas vindas
        clientPrintStream.println("O utilizador inserido foi aceite. Bem-vindo: " + username + "\n_________________________________");


        // imprime (no cliente) os utilizadores online
        if (numUser > 0)
        {
            clientPrintStream.println("\nTotal de utilizadores online: " + numUser);

            // lista os utilizadores online (excepto o utilizador atual)
            numUser = serverD.ListOn(clientPrintStream, username);

            clientPrintStream.println("\nDigite « .UTILIZADOR » a trocar messagem: ");

            // envia para todos os utilizadores online que o utilizador iniciou sessão
            serverD.SendToUser(username, "iniciou sessão.");
        }
        else
        {
            clientPrintStream.println("\nVocê é o único utilizador no chat... aguarde novos utilizadores.\n");
        }
    }
    //endregion


    //region METODO SendOrder
    // Metodo para ser executado no final do DefineUser, este metodo intrepreta o que o cliente escreve e dependendo do comando executa diversas acções
    public void SendOrder(Scanner s)
    {
        // variaveis de validação
        boolean exit = false;
        int flag = 0;


        do {

            try {
                s = new Scanner(clientData.getClientSocket().getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            String clientMsg = s.nextLine().toString();

            // validações
            if (clientMsg.equals(".exit") == true)
            {
                clientPrintStream.println("Sessão Terminada. Até Já!");
                try {
                    clientStream.close();
                    clientStream.close();
                    clientData.getClientSocket().close();

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {

                    if (serverD.RemoveUser(clientData.getUsername()) == true) {
                        System.out.println("> utilizador " + clientData.getUsername().toString() + " foi removido da lista de clientes online.");
                    } else {
                        System.out.println("> utilizador " + clientData.getUsername().toString() + " NÃO foi removido da lista de clientes online.");
                    }

                    serverD.LogoutAlert(clientData.getUsername().toString());
                }
                exit = true;
            }
            else if (clientMsg.equals(".online") ==true)
            {
                int numUser = 0;

                clientPrintStream.println("Lista de Utilizadores Online:");

                // lista os utilizadores online (excepção do atual)
                numUser = serverD.ListOn(clientPrintStream,clientData.getUsername());


                // imprime (no cliente) os utilizadores online
                if (numUser > 0)
                {
                    clientPrintStream.println("\nTotal de Utilizadores On-Line: " + numUser + "\nDigite « .UTILIZADOR » a trocar messagem: ");
                }
                else
                {
                    clientPrintStream.println("Você é o único utilizador! Forever Alone :)\n");
                }
            }
            else if (clientMsg.equals(".help") ==true)
            {
                clientPrintStream.println("Ajuda/Opções\n\nA qualquer momento digite:\n« .online » para verificar quem está online.\n« .all    » para enviar a mensagem a todos os utilizadores online.\n« .exit   » para terminar a sua sessão no chat.\n");
            }
            else
            {
                // enviar mensagem
                SendMessage(clientData.getUsername().toString(), clientMsg);

                // imprimir/confirmar no servidor
                System.out.println("> client " + clientData.getClientSocket().getRemoteSocketAddress().toString() + " «" + clientData.getUsername() + "» || mensagem: " + clientMsg);
            }


        } while (exit == false);
    }
    //endregion

    //region METODO SendMessage
    // Metodo que recebe a mensagem e o utilizador destino e encaminha para ele
    public void SendMessage(String clientSending,String text)
    {
        // variaveis
        String clientToSend;
        String message;
        boolean validate=false;


        // se o texto contem "espaço" -> contem cliente e mensagem
        if (text.contains(" "))
        {
            // separar o texto que o cliente enviou
            String arr[] = text.split(" ", 2);

            // cliente a receber a mensagem
            clientToSend = arr[0];
            // mensagem a ser enviada
            message = arr[1];
        }
        else
        {
            // cliente definido com todo o texto
            clientToSend = text;
            // mensagem definida como vazia
            message = "";
        }

        // verifica se o cliente e mensagem inseridos são válidos
        // se cliente começa por "ponto" e se a mensagem não é vazia
        if(clientToSend.startsWith(".") == true && !message.isEmpty()) {

            // remover o "." do cliente
            clientToSend = clientToSend.substring(1);

            // se for para enviar a todos
            if (clientToSend.equals("all") == true) {

                // envio de mensagem
                validate = serverD.SendToUser(clientSending, message);

                // envia mensagem de quem envia a confirmar o envio ou não envio
                if (validate == true)
                {
                    clientPrintStream.println("« mensagem enviada a todos os utilizadores »");
                }
                else
                {
                    clientPrintStream.println("« mensagem não enviada... não existe mais nenhum utilizador »");
                }

            }
            else if (clientToSend.toLowerCase().equals(clientSending.toLowerCase()) == true)
            {
                clientPrintStream.println("« mensagem não enviada... está a tentar enviar para si própio »");
            }
            else
            {
                // envio de mensagem
                validate = serverD.SendToUser(clientSending, clientToSend, message);

                // envia mensagem de quem envia a confirmar o envio ou não envio
                if (validate == true)
                {
                    clientPrintStream.println("« mensagem enviada para "+ clientToSend +" »");
                }
                else
                {
                    clientPrintStream.println("« mensagem não enviada... o utilizador "+ clientToSend +" não existe ou não esta online»");
                }
            }
        }
        else
        {
            clientPrintStream.println("mensagem invalida!! por favor inserir nome de utilizador precedido de ponto, espaço e depois a mensagem (.jonh stuff).");
        }
    }
    //endregion


    //region METODO run
    public void run()
    {
        Scanner s = new Scanner(this.clientStream);

        // definir utilizador
        try
        {
            DefineUser(s);
        } catch (Exception e)
        {
            System.out.println(e.toString());
        }

        // enviar mensagem/ordem
        try
        {
            SendOrder(s);
        } catch (Exception e)
        {
            System.out.println(e.toString());
        }

        try {

            clientData.getClientSocket().close();

            if (serverD.RemoveUser(clientData.getUsername()) == true) {
                System.out.println("> utilizador " + clientData.getUsername().toString() + " foi removido da lista de clientes online.");
            }

            System.out.println("> utilizador " + clientData.getUsername().toString() + " terminou sessão.");

        } catch (IOException e) {
            e.printStackTrace();

            //////
            System.out.println("erro.");
        }
    }
    //endregion

    //endregion
}
//endregion