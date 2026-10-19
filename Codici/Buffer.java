public class Buffer {
    // Contenuto del buffer: null = vuoto
    private String buffer;

    public Buffer() {
        buffer = null;
    }

    // true se il buffer è vuoto
    public boolean vuoto() {
        return buffer == null;
    }

    // Scrive una frase nel buffer.
    // Se il buffer è pieno, attende che un consumatore lo svuoti.
    public synchronized void scrivi(String frase) {
        while (!vuoto()) {
            try {
                wait();   // rilascia il lock e attende di essere svegliato
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();   // ripristina il flag di interruzione
                System.out.println(e);
            }
        }
        buffer = frase;
        notifyAll();      // sveglia i consumatori in attesa
    }

    // Legge e svuota il buffer.
    // Se il buffer è vuoto, attende che un produttore ci scriva qualcosa.
    public synchronized String leggi() {
        while (vuoto()) {
            try {
                wait();   // rilascia il lock e attende di essere svegliato
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println(e);
            }
        }
        String frase = buffer;
        buffer = null;
        notifyAll();      // sveglia i produttori in attesa
        return frase;
    }
}