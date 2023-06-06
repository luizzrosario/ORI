import java.io.*;
import java.util.*;
import java.text.Normalizer;

public class Main {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println(
                    "Uso: java IndexingProgram <caminho_conjunto> <caminho_desconsideradas> <caminho_consulta>");
            return;
        }

        String conjuntoArquivo = args[0];
        String desconsideradasArquivo = args[1];
        String consultaArquivo = args[2];

        List<String> conjunto = readLines(conjuntoArquivo);
        Set<String> desconsideradas = readWords(desconsideradasArquivo);
        List<String> consulta = readLines(consultaArquivo);

        List<String> index = buildIndex(conjunto, desconsideradas);
        writeIndex(index, "indice.txt");

        List<String> resposta = executeQuery(index, consulta);
        writeLines(resposta, "resposta.txt");

        System.out.println("indice e resposta gerados com sucesso!");
    }

    private static List<String> buildIndex(List<String> files, Set<String> stopwords) {
        List<String> index = new ArrayList<>();
        int fileIndex = 1;
        for (String filePath : files) {
            List<String> lines = readLines(filePath);
            int lineIndex = 1;
            for (String line : lines) {
                String[] tokens = line.toLowerCase().split("[ .,!?;]+");
                for (String token : tokens) {
                    String normalizedToken = removeAccents(token);
                    if (!stopwords.contains(normalizedToken)) {
                        String entry = findIndexEntry(index, normalizedToken);
                        if (entry == null) {
                            entry = normalizedToken + ": ";
                            index.add(entry);
                        }
                        entry += fileIndex + "," + lineIndex + " ";

                        int entryIndex = index.indexOf(entry);
                        if (entryIndex != -1) {
                            index.set(entryIndex, entry.trim());
                        } else {
                            index.add(entry.trim());
                        }
                    }
                }
                lineIndex++;
            }
            fileIndex++;
        }
        return index;
    }

    private static String findIndexEntry(List<String> index, String token) {
        for (String entry : index) {
            if (entry.startsWith(token + ": ")) {
                return entry;
            }
        }
        return null;
    }

    private static List<String> executeQuery(List<String> index, List<String> query) {
        List<String> resposta = new ArrayList<>();
        String operador = ",";
        if (query.get(0).contains(";")) {
            operador = ";";
        }
    
        List<String> termos = new ArrayList<>();
        for (String termo : query.get(0).split(operador)) {
            termos.add(termo.trim());
        }
    
        int qtdDocumentos = 0;
        for (String entry : index) {
            String[] partes = entry.split(":");
            String token = partes[0].trim();
            String ocorrencias = partes[1].trim();
    
            if (termos.contains(token)) {
                qtdDocumentos++;
                resposta.add(token);
                resposta.add(ocorrencias);
            }
        }
    
        resposta.add(0, String.valueOf(qtdDocumentos));
        return resposta;
    }
    

    private static List<String> readLines(String filePath) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }
    

    private static Set<String> readWords(String filePath) {
        Set<String> words = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String word;
            while ((word = reader.readLine()) != null) {
                words.add(removeAccents(word.trim().toLowerCase()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return words;
    }

    private static void writeIndex(List<String> index, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            for (String entry : index) {
                writer.write(entry.trim() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeLines(List<String> lines, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            for (String line : lines) {
                writer.write(line.trim() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String removeAccents(String text) {
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "");
    }
}