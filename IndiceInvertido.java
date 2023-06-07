import java.io.*;
import java.util.*;

public class IndiceInvertido {
    
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Número incorreto de argumentos.");
            System.out.println("Uso: java IndiceInvertido <conjuntoArquivos> <desconsideradas> <consulta>");
            return;
        }
        
        String conjuntoArquivosPath = args[0];
        String desconsideradasPath = args[1];
        String consultaPath = args[2];
        
        List<String> caminhosBase = lerCaminhosArquivos(conjuntoArquivosPath);
        Set<String> palavrasDesconsideradas = lerPalavrasDesconsideradas(desconsideradasPath);
        String consulta = lerConsulta(consultaPath);
        
        Map<String, List<ParOcorrencia>> indice = construirIndice(caminhosBase, palavrasDesconsideradas);
        escreverIndice(indice);
        
        Set<String> resultados = realizarConsulta(indice, consulta);
        escreverResposta(resultados);
    }
    
    public static List<String> lerCaminhosArquivos(String caminhoArquivo) {
        List<String> caminhos = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(caminhoArquivo))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                caminhos.add(linha.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return caminhos;
    }
    
    public static Set<String> lerPalavrasDesconsideradas(String caminhoArquivo) {
        Set<String> palavras = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(caminhoArquivo))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                palavras.add(linha.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return palavras;
    }
    
    public static String lerConsulta(String caminhoArquivo) {
        StringBuilder consulta = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(caminhoArquivo))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                consulta.append(linha.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return consulta.toString();
    }
    
    public static Map<String, List<ParOcorrencia>> construirIndice(List<String> caminhosBase, Set<String> palavrasDesconsideradas) {
        Map<String, List<ParOcorrencia>> indice = new HashMap<>();
        int numArquivo = 1;
    
        for (String caminho : caminhosBase) {
            List<String> palavrasArquivo = obterPalavrasArquivo(caminho);
            Map<String, Integer> ocorrencias = new HashMap<>();
    
            for (String palavra : palavrasArquivo) {
                if (!palavrasDesconsideradas.contains(palavra)) {
                    ocorrencias.put(palavra, ocorrencias.getOrDefault(palavra, 0) + 1);
                }
            }
    
            for (String palavra : ocorrencias.keySet()) {
                int qtdOcorrencias = ocorrencias.get(palavra);
    
                if (!indice.containsKey(palavra)) {
                    indice.put(palavra, new ArrayList<>());
                }
    
                List<ParOcorrencia> listaOcorrencias = indice.get(palavra);
                listaOcorrencias.add(new ParOcorrencia(numArquivo, qtdOcorrencias));
            }
    
            numArquivo++;
        }
    
        return indice;
    }
    
    
    public static List<String> obterPalavrasArquivo(String caminhoArquivo) {
        List<String> palavras = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(caminhoArquivo))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] tokens = linha.toLowerCase().split("[ .,!?\n]+");
                palavras.addAll(Arrays.asList(tokens));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return palavras;
    }
    
    public static Set<String> realizarConsulta(Map<String, List<ParOcorrencia>> indice, String consulta) {
        Set<String> resultados = new HashSet<>();
        if (consulta.contains(",")) {
            String[] palavrasConsulta = consulta.split(",");
            resultados = realizaConsultaAnd(indice, palavrasConsulta);
        } else if (consulta.contains(";")) {
            String[] palavrasConsulta = consulta.split(";");
            resultados = realizaConsultaOr(indice, palavrasConsulta);
        }
        return resultados;
    }
    
    public static Set<String> realizaConsultaAnd(Map<String, List<ParOcorrencia>> indice, String[] palavrasConsulta) {
        Set<String> resultados = new HashSet<>();
        
        if (palavrasConsulta.length == 0) {
            return resultados;
        }
        
        String palavra = palavrasConsulta[0].trim();
        
        if (!indice.containsKey(palavra)) {
            return resultados;
        }
        
        resultados.addAll(obterCaminhosArquivos(indice.get(palavra)));
        
        for (int i = 1; i < palavrasConsulta.length; i++) {
            palavra = palavrasConsulta[i].trim();
            
            if (!indice.containsKey(palavra)) {
                resultados.clear();
                return resultados;
            }
            
            resultados.retainAll(obterCaminhosArquivos(indice.get(palavra)));
        }
        
        return resultados;
    }
    
    public static Set<String> realizaConsultaOr(Map<String, List<ParOcorrencia>> indice, String[] palavrasConsulta) {
        Set<String> resultados = new HashSet<>();
        
        for (String palavra : palavrasConsulta) {
            palavra = palavra.trim();
            
            if (indice.containsKey(palavra)) {
                resultados.addAll(obterCaminhosArquivos(indice.get(palavra)));
            }
        }
        
        return resultados;
    }
    
    public static Set<String> obterCaminhosArquivos(List<ParOcorrencia> ocorrencias) {
        Set<String> caminhos = new TreeSet<>();
        for (ParOcorrencia par : ocorrencias) {
            caminhos.add((char) ('a' + par.getNumeroArquivo() - 1) + ".txt");
        }
        return caminhos;
    }
    
    
    
    public static void escreverIndice(Map<String, List<ParOcorrencia>> indice) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("indice.txt"))) {
            TreeMap<String, List<ParOcorrencia>> indiceOrdenado = new TreeMap<>(indice);
            
            for (String palavra : indiceOrdenado.keySet()) {
                List<ParOcorrencia> ocorrencias = indiceOrdenado.get(palavra);
                StringBuilder linha = new StringBuilder(palavra + ":");
                
                for (ParOcorrencia par : ocorrencias) {
                    linha.append(" ").append(par.getNumeroArquivo()).append(",").append(par.getQuantidade());
                }
                
                writer.write(linha.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void escreverResposta(Set<String> resultados) {
        List<String> resultadosOrdenados = new ArrayList<>(resultados);
        Collections.sort(resultadosOrdenados);
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("resposta.txt"))) {
            writer.write(Integer.toString(resultadosOrdenados.size()));
            writer.newLine();
            for (String resultado : resultadosOrdenados) {
                writer.write(resultado);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ParOcorrencia {
    private int numeroArquivo;
    private int quantidade;
    
    public ParOcorrencia(int numeroArquivo, int quantidade) {
        this.numeroArquivo = numeroArquivo;
        this.quantidade = quantidade;
    }
    
    public int getNumeroArquivo() {
        return numeroArquivo;
    }
    
    public int getQuantidade() {
        return quantidade;
    }
}