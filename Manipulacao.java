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

  // ler o arquivo desconsideradas.txt
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

  // funcao usada pra remover possibilidade de acentos presentes nas palavras
  private static String removeAcentos(String text) {
    String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
    return normalized.replaceAll("\\p{M}", "");
  }

  /*
   * funcao usada para construir o arquivo índice.txt.A função percorre
   * a base de dados(conjuntos.txt), todas as palavras validas,todas as
   * palavras que devem ser desconsideradas e o nome do arquivo indice.txt.
   */
  public static void constroiIndice(List<String> caminhosBase, Set<String> palavrasValidas,
      Set<String> desconsideradas, String caminhoIndice) {
    int[][] indices = new int[palavrasValidas.size()][];// ligar cada palavra valida uma matriz(numero do arquivo+qtde
                                                        // ocorrencia)na linha e coluna
    List<String> palavras = new ArrayList<>(palavrasValidas);
    /*
     * lista de palavras com as palavras validas pra ser possível usar o get(TreeSet
     * usada foi
     * para garantir que a inserçao já acontece na ordem alfabética solicitada na
     * formatação do
     * projeto
     */

    // percorrer com dois for para o preenchimento da matriz contendo numArq + qtde
    // ocorrencia
    for (int i = 0; i < palavrasValidas.size(); i++) {
      String palavra = palavras.get(i);// pega cada uma das palavras validas
      int numOcorrencias = 0;// contadora para verificar ocorrencia de cada posicao de palavrasValidas
      List<Integer> paresArquivoQuantidade = new ArrayList<>();// lista pra guardar numero do arquivo+qtde ocorrencia

      for (int j = 0; j < caminhosBase.size(); j++) {
        List<String> linhasArq = lerArq(caminhosBase.get(j));// ler cada linha de conjunto, cada linha contem cada um
                                                             // dos arquivos da base de dados, logo percorre cada um dos
                                                             // arquivos
        int quantidade = contarOcorrencias(palavra, linhasArq);// conta a quantidade de vezes que ocorre cada palavra

        // se houve a palavra no arquivo->quantidade>0
        if (quantidade > 0) {
          paresArquivoQuantidade.add(j + 1);// j guarda o numero do arquivo
          paresArquivoQuantidade.add(quantidade);// insere quantidade ocorrencias
          numOcorrencias += quantidade;// adiciona a quantidade de uma palavra
        }
      }

      // a cada ocorrencia da palavra entra no if
      if (numOcorrencias > 0) {
        // i itera sobre a quantidade de palavras, para cada palavra leio em todos os
        // arquivos
        indices[i] = new int[paresArquivoQuantidade.size()];// pega o i atual pra indicar que a linha i vai guardar
                                                            // todas as ocorrencias da posicao i em palavras validas
        for (int j = 0; j < paresArquivoQuantidade.size(); j++) {// itera sobre o tamanho da lista que guarda cada par
          indices[i][j] = paresArquivoQuantidade.get(j);// guarda na matriz de indices o numArq +qtde ocorrencia palavra
        }
      }
    }
    escreveArq(palavras, indices, caminhoIndice);// escreve o arquivo indice.txt como solicitado
  }

  // conta ocorrencia da palavra nas linhas do arquivo, ou seja, em um arquivo
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

  /*
   * Para escrever no arquivo foi considerado a lista de palavras
   * geradas pela funcao constroIndice
   */
  private static void escreveArq(List<String> palavras, int[][] indices, String caminhoArquivo) {
    try (PrintWriter writer = new PrintWriter(caminhoArquivo)) {
      for (int i = 0; i < palavras.size(); i++) {// itera sobre a quantidade de palavras validas
        StringBuilder linha = new StringBuilder();// stringBuilder para concatenar
        linha.append(palavras.get(i)).append(": ");// pega a palavra e concatena com ": "

        for (int j = 0; j < indices[i].length; j += 2) {// a cada duas colunas tenho numArq+qtde de ocorrencia da linha
                                                        // i, sendo que a linha representa cada palavra
          int numeroArquivo = indices[i][j];// pego o numArq
          int quantidade = indices[i][j + 1];// ando na mesma linha e pego a quantidade de ocrrencia
          linha.append(numeroArquivo).append(",").append(quantidade).append(" ");// concatena
        }
        writer.println(linha.toString().trim());// escreve a linha
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  // passa a lista lida em conjunto.txt e separa todas as palavras encontradas
  private static Set<String> separaPalavras(List<String> caminhosBase) {
    List<String> linhasArq = new ArrayList<String>();// guardar linhas de cada arquivo
    Set<String> palavrasSeparadas = new TreeSet<String>();// gurdar as palavras separadas
    for (int i = 0; i < caminhosBase.size(); i++) {
      linhasArq = lerArq(caminhosBase.get(i));// cada linha do arquivo é uma posição que será quebrada em palavras
      for (int j = 0; j < linhasArq.size(); j++) {
        String[] palavras = linhasArq.get(j).split("[\\s,!.?]+");// vetor com as palavras separadas
        for (int k = 0; k < palavras.length; k++) {
          palavrasSeparadas.add(palavras[k]);// adiciona todas as palavras em uma TreeSet
        }
      }
    }
    return palavrasSeparadas;// TreeSet com as palavras separadas
  }

  public static List<String> processarConsulta(String consulta, Set<String> palavrasValidas,
      List<String> caminhosBase) {
    List<String> termosConsulta = Arrays.asList(consulta.split(";"));
    List<String> arquivosSatisfatorios = new ArrayList<>();

    for (String caminho : caminhosBase) {
      List<String> linhasArq = lerArq(caminho);
      boolean satisfatorio = false;

      if (consulta.contains(",")) {
        // Operador AND
        satisfatorio = verificarPresencaAnd(termosConsulta, linhasArq);
      } else {
        // Operador OR
        satisfatorio = verificarPresencaOr(termosConsulta, linhasArq);
      }

      if (satisfatorio) {
        arquivosSatisfatorios.add(caminho);
      }
    }

    return arquivosSatisfatorios;
  }

  public static boolean verificarPresencaAnd(List<String> termosConsulta, List<String> linhasArq) {
    // Verificar se todos os termos da consulta estão presentes nas linhas do
    // arquivo
    for (String termo : termosConsulta) {
      boolean termoEncontrado = verificarPresenca(termo, linhasArq);
      if (!termoEncontrado) {
        return false;
      }
    }
    return true;
  }

  public static boolean verificarPresencaOr(List<String> termosConsulta, List<String> linhasArq) {
    // Verificar se pelo menos um dos termos da consulta está presente nas linhas do
    // arquivo
    for (String termo : termosConsulta) {
      boolean termoEncontrado = verificarPresenca(termo, linhasArq);
      if (termoEncontrado) {
        return true;
      }
    }
    return false;
  }

  public static boolean verificarPresenca(String termo, List<String> linhasArq) {
    // Verificar se o termo está presente em alguma linha do arquivo
    for (String linha : linhasArq) {
      if (linha.contains(termo)) {
        return true;
      }
    }
    return false;
  }

  public static void escreveResposta(List<String> arquivosSatisfatorios, String caminhoResposta) {
    // Escrever o número de documentos satisfatórios e seus respectivos nomes no
    // arquivo resposta
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(caminhoResposta))) {
      writer.write(Integer.toString(arquivosSatisfatorios.size()));
      writer.newLine();
      for (String arquivo : arquivosSatisfatorios) {
        writer.write(arquivo);
        writer.newLine();
      }
    } catch (IOException e) {
      System.out.println("Ocorreu um erro ao escrever o arquivo de resposta.");
      e.printStackTrace();
    }
  }
}