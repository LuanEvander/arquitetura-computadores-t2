import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Simulador2WayLFU {
    private static final int TAMANHO_CACHE = 8;
    private static final int TAMANHO_MEMORIA_PRINCIPAL = 32;
    private static final int TAMANHO_BLOCO = 2;
    private static final int CONJUNTOS = TAMANHO_CACHE / 2; // Cache associativa 2-way

    private static final String ARQUIVO_MEMORIA_PRINCIPAL = "src/MemoriaPrincipal.txt";
    private static final String ARQUIVO_ENDERECOS = "src/Leituras.txt";

    private static int[] memoriaPrincipal = new int[TAMANHO_MEMORIA_PRINCIPAL];
    private static List<Integer>[] cache = new List[CONJUNTOS];
    private static Map<Integer, Integer>[] frequencia = new Map[CONJUNTOS];
    private static double contadorLeituras = 0;
    private static double contadorMiss = 0;
    private static double contadorHit = 0;

    public void imprimirResultados() {
        double taxaMiss = contadorMiss / contadorLeituras;
        double taxaHit = contadorHit / contadorLeituras;

        System.out.println("Número de leituras: " + (int) contadorLeituras);
        System.out.println("Número de misses: " + (int) contadorMiss);
        System.out.println("Número de hits: " + (int) contadorHit);
        System.out.println("Taxa de misses: " + taxaMiss);
        System.out.println("Taxa de hits: " + taxaHit);
    }

    public void inicializarCache() {
        for (int i = 0; i < CONJUNTOS; i++) {
            cache[i] = new ArrayList<>();
            frequencia[i] = new HashMap<>();
        }
    }

    public void lerMemoriaPrincipal() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(ARQUIVO_MEMORIA_PRINCIPAL));
        String linha;
        int indice = 0;

        // Carrega a memória principal com os valores do arquivo
        while ((linha = reader.readLine()) != null) {
            memoriaPrincipal[indice] = Integer.parseInt(linha, 2);
            indice++;
        }

        reader.close();
    }

    public void lerEnderecos() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(ARQUIVO_ENDERECOS));
        String linha;

        // Lê cada endereço do arquivo e processa
        while ((linha = reader.readLine()) != null) {
            int endereco = Integer.parseInt(linha, 2);
            processarEndereco(endereco);
            contadorLeituras++;
        }

        reader.close();
    }

    private void processarEndereco(int endereco) {
        int indiceConjunto1 = (endereco / TAMANHO_BLOCO) % CONJUNTOS;
        int indiceConjunto2 = (indiceConjunto1 + 1) % CONJUNTOS;
        int tag = endereco / TAMANHO_BLOCO;

        if (cache[indiceConjunto1].contains(tag)) {
            System.out.println("Cache hit - Endereço: " + endereco + " Conjunto: " + indiceConjunto1 + " Tag: " + tag);
            contadorHit++;
            atualizarFrequencia(indiceConjunto1, tag);
            exibirEstadoCache();
        } else if (cache[indiceConjunto2].contains(tag)) {
            System.out.println("Cache hit - Endereço: " + endereco + " Conjunto: " + indiceConjunto2 + " Tag: " + tag);
            contadorHit++;
            atualizarFrequencia(indiceConjunto2, tag);
            exibirEstadoCache();
        } else {
            System.out.println("Cache miss - Endereço: " + endereco + " Tag: " + tag);
            contadorMiss++;
            
            // Verifica qual conjunto mais adequado para uso
            int melhorConjunto = indiceConjunto2;
            for (int i = 0; i < CONJUNTOS; i++) {
                if (cache[i].size() < cache[melhorConjunto].size()) {
                    melhorConjunto = i;
                }
            }

            // Substitui o bloco mais antigo do melhor conjunto
            substituirBloco(melhorConjunto, tag);
        }
    }

    private void substituirBloco(int indiceConjunto, int tag) {
        if (cache[indiceConjunto].size() < 2) { // Verificar se há espaço no conjunto
            cache[indiceConjunto].add(tag);
            frequencia[indiceConjunto].put(tag, 1);
        } else {
            int tagMenosFrequentementeUtilizada = encontrarMenosFrequentementeUtilizada(indiceConjunto);
            cache[indiceConjunto].remove(Integer.valueOf(tagMenosFrequentementeUtilizada));
            cache[indiceConjunto].add(tag);
            frequencia[indiceConjunto].put(tag, 1);
        }

        exibirEstadoCache();
    }

    private int encontrarMenosFrequentementeUtilizada(int indiceConjunto) {
        int tagMenosFrequentementeUtilizada = 0;
        int menorFrequencia = Integer.MAX_VALUE;

        for (Map.Entry<Integer, Integer> entry : frequencia[indiceConjunto].entrySet()) {
            int tag = entry.getKey();
            int freq = entry.getValue();

            if (freq < menorFrequencia) {
                tagMenosFrequentementeUtilizada = tag;
                menorFrequencia = freq;
            }
        }

        frequencia[indiceConjunto].remove(tagMenosFrequentementeUtilizada);
        return tagMenosFrequentementeUtilizada;
    }

    private void atualizarFrequencia(int indiceConjunto, int tag) {
        int freq = frequencia[indiceConjunto].getOrDefault(tag, 0);
        frequencia[indiceConjunto].put(tag, freq + 1);
    }

    private void exibirEstadoCache() {
        System.out.println("Estado da cache:");

        for (int i = 0; i < CONJUNTOS; i++) {
            System.out.print("Conjunto " + i + ": ");
            for (int tag : cache[i]) {
                System.out.print("[" + tag + "] (Freq: " + frequencia[i].getOrDefault(tag, 0) + ") ");
            }
            System.out.println();
        }

        System.out.println();
    }
}
