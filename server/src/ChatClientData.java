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
import java.net.Socket;
// endregion


//region Class ChatClientData

public class ChatClientData {

    // region Atributos
    private String username;
    private Socket clientSocket;
    // endregion

    // region Construtores
    public ChatClientData(String username, Socket clientSocket) {
        this.username = username;
        this.clientSocket = clientSocket;
    }
    // endregion

    // region Propriedades
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public void setClientSream(Socket clientSream) {
        this.clientSocket = clientSocket;
    }
    // endregion

}
// endregion Class
