import java.util.*;
import java.io.*;
import java.text.*;

public class Manipulacao {
  /*
   * usado para ler as linhas dos arquivos base
   * ou seja, pegar o caminho de cada arquivo
   * em conjunto.txt para a posterior leitura
   * de cada arquivo presente no conjunto.Além
   * disso é utilizado pra gerar um arraylist
   * contendo as desconsideradas também
   */
  public static List<String> lerArq(String caminhoArq) {
    List<String> linhasArq = new ArrayList<>();// lista para armazenar cada linha do caminho
    try (BufferedReader reader = new BufferedReader(new FileReader(caminhoArq))) {
      String linha;
      while ((linha = reader.readLine()) != null) {
        linhasArq.add(removeAcentos(linha.toLowerCase()));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return linhasArq;// retorna um arraylist, cada indice do arraylist contem uma linha do arquivo
  }

  public static Set<String> lerDesconsideradas(String caminhoArq) {
    Set<String> desconsideradas = new HashSet<>();// garante a não inserção de palavras repetidas, caso tenha
    try (BufferedReader reader = new BufferedReader(new FileReader(caminhoArq))) {
      String linha;
      while ((linha = reader.readLine()) != null) {
        desconsideradas.add(removeAcentos(linha.toLowerCase()));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return desconsideradas;// retorna um conjunto com as palavras desconsideradas
  }

  // função para pegar a consulta
  public static String lerConsulta(String caminhoArq) {
    String consulta = "";// string vazia pra preencher e retornar qual consulta vai ser realizada
    try (BufferedReader reader = new BufferedReader(new FileReader(caminhoArq))) {
      consulta = removeAcentos(reader.readLine().toLowerCase());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return consulta;// retorna a consulta que deve ser feita;
  }

  /*
   * Método pra percorrer a lista de arquivos gerados pelo conjunto.txt
   * ->cada linha do conjunto representa um arquivo.
   * Para cada linha(caminho) dessa lista(contendo todos os arquivos base)
   * lê-se a linha, pega o caminho e separa todas as palavras presente
   * em cada linha jogando em um vetor e adiciona ordenado na treeset.
   * Em seguida verifica-se utilizando padrão pra evitar concurrentmodifi
   * cationException o iterator pra analisar se alguma das palavras inseridas
   * anteriormente em palavrasValidas é realmente válida.
   */
  public static Set<String> palavrasValidas(List<String> caminhosBase, int numArq, Set<String> desconsideradas) {
    Set<String> palavrasValidas = new TreeSet<String>();
    palavrasValidas = separaPalavras(caminhosBase);// separa as palavras entre todos os arquivos
    // verifica se cada palavra deve desconsiderada ou não
    Iterator<String> iterator = palavrasValidas.iterator();
    while (iterator.hasNext()) {// enquanto percorro palavra valida
      String palavra = iterator.next();// equivalente a pegar a palavra de cada posicao
      if (desconsideradas.contains(palavra)) {// se palavra ta presente no conjunto desconsideradas
        iterator.remove();// usa iterator pra evitar excecao de concurrent modification
      }
    }
    return palavrasValidas;// retorna todas as palavras validas entre todos os arquivos
  }

  private static String removeAcentos(String text) {
    String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
    return normalized.replaceAll("\\p{M}", "");
  }

  /*
   * funcao usada para escrever as linhas de um arquivo
   * recebe a lista que deve ser escrita e escreve no caminho passado
   */

  public static void constroiIndice(List<String> caminhosBase, Set<String> palavrasValidas,
      Set<String> desconsideradas, String caminhoIndice) {
    int[][] indices = new int[palavrasValidas.size()][];
    List<String> palavras = new ArrayList<>(palavrasValidas);

    for (int i = 0; i < palavrasValidas.size(); i++) {
      String palavra = palavras.get(i);
      int numOcorrencias = 0;
      List<Integer> paresArquivoQuantidade = new ArrayList<>();

      for (int j = 0; j < caminhosBase.size(); j++) {
        List<String> linhasArq = lerArq(caminhosBase.get(j));
        int quantidade = contarOcorrencias(palavra, linhasArq);

        if (quantidade > 0) {
          paresArquivoQuantidade.add(j + 1);
          paresArquivoQuantidade.add(quantidade);
          numOcorrencias += quantidade;
        }
      }

      if (numOcorrencias > 0) {
        indices[i] = new int[paresArquivoQuantidade.size()];
        for (int j = 0; j < paresArquivoQuantidade.size(); j++) {
          indices[i][j] = paresArquivoQuantidade.get(j);
        }
      }
    }
    escreveArq(palavras, indices, caminhoIndice);
  }

  //conta ocorrencia da palavra nas linhas do arquivo, ou seja, em um arquivo
  private static int contarOcorrencias(String palavra, List<String> linhasArq) {
    int ocorrencias = 0;
    for (String linha : linhasArq) {
      String[] palavras = linha.split("[\\s,!.?]+");
      for (String p : palavras) {
        if (p.equals(palavra)) {
          ocorrencias++;
        }
      }
    }
    return ocorrencias;
  }

  private static void escreveArq(List<String> palavras, int[][] indices, String caminhoArquivo) {
    try (PrintWriter writer = new PrintWriter(caminhoArquivo)) {
        for (int i = 0; i < palavras.size(); i++) {
            StringBuilder linha = new StringBuilder();
            linha.append(palavras.get(i)).append(": ");

            for (int j = 0; j < indices[i].length; j += 2) {
                int numeroArquivo = indices[i][j];
                int quantidade = indices[i][j + 1];
                linha.append(numeroArquivo).append(",").append(quantidade).append(" ");
            }
            writer.println(linha.toString().trim());
        }
    } catch (FileNotFoundException e) {
        e.printStackTrace();
    }
}

//passa a lista lida em conjunto.txt e separa todas as palavras
private static Set<String> separaPalavras(List<String> caminhosBase) {
  List<String> linhasArq = new ArrayList<String>();//guardar linhas de cada arquivo
  Set<String> palavrasSeparadas = new TreeSet<String>();//gurdar as palavras separadas
  for (int i = 0; i < caminhosBase.size(); i++) {
    linhasArq = lerArq(caminhosBase.get(i));// cada linha do arquivo é uma posição que será quebrada em palavras
    for (int j = 0; j < linhasArq.size(); j++) {
      String[] palavras = linhasArq.get(j).split("[\\s,!.?]+");// vetor com as palavras separadas
      for (int k = 0; k < palavras.length; k++) {
        palavrasSeparadas.add(palavras[k]);// adiciona todas as palavras em uma TreeSet
      }
    }
  }
  return palavrasSeparadas;
}

}