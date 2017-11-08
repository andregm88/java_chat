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
import java.io.InputStream;
import java.util.Scanner;
// endregion


// region Class ChatClientThread
public class ChatClientThread implements Runnable {

    // region Atributos
    private InputStream serverStream;
    // endregion

    // region Construtores
    public ChatClientThread(InputStream serverStream) {
        this.serverStream = serverStream;
    }
    // endregion

    //region Metodo run
    public void run() {
        Scanner text = new Scanner(this.serverStream);
        while(text.hasNextLine())
        {
            System.out.println(text.nextLine());
        }
    }
    //endregion

}
//endregion
