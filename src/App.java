import java.io.IOException;

public class App {
    
    public static void main(String[] args) {
        Simulador2WayLFU simulador = new Simulador2WayLFU();

        simulador.inicializarCache();
        try {
            simulador.lerMemoriaPrincipal();
            simulador.lerEnderecos();
        } catch (IOException e) {
            e.printStackTrace();
        }

        simulador.imprimirResultados();
    }

}
