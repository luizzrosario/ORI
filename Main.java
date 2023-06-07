import java.util.*;
//import java.io.*;
//import java.text.Normalizer;
public class Main{
  public static void main(String args[]){
    
    if (args.length != 3) {
      System.out.println("Quantidade de parametros correta nao foi passada");
      System.out.println("Digite: java Main <conjunto.txt> <desconsideradas.txt> <consulta.txt>");
      System.exit(0);//finaliza o programa
    }
    /*
     pegar o que foi passado por parâmetro ao programa(nome dos arquivos)
     e usar esses caminhos para a execução do programa
    */
    String basesConjunto = args[0];//arquivo que contem os nomes(caminhos) de todos os arquivos da base
    String desconsideradas = args[1];//arquivo usado para desconsiderar palavras ao criar índice
    String consultaEmAnalise = args[2];//arquivo em consulta para gerar resposta
    
    List<String>caminhosArquivosBase = Manipulacao.lerArq(basesConjunto);//como a quantidade é arbitrária insere na lista todos os caminhos com os arquivos pra busca 
    Set<String>palavrasDesconsideradas = Manipulacao.lerDesconsideradas(desconsideradas);//seta as palavras desconsideras em um conjunto
    String consulta = Manipulacao.lerConsulta(consultaEmAnalise);

    System.out.println(palavrasDesconsideradas.toString());
    System.out.println(caminhosArquivosBase.toString());
    System.out.println(consulta.toString());

    Set<String>palavrasValidas=Manipulacao.palavrasValidas(caminhosArquivosBase, 1, palavrasDesconsideradas);
    System.out.println(palavrasValidas.toString());

    Manipulacao.constroiIndice(caminhosArquivosBase,palavrasValidas,palavrasDesconsideradas,"indice.txt");

  }
}