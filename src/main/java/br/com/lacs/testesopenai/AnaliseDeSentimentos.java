package br.com.lacs.testesopenai;

import com.theokanning.openai.OpenAiHttpException;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AnaliseDeSentimentos {

    public static void main(String[] args) {
        try {

            var diretorioAvaliacoes = Path.of("src/main/resources/avaliacoes");

            var arquivosDeAvaliacoes = carregarArquivosDeAvaliacoes(diretorioAvaliacoes);

            for (Path arquivo: arquivosDeAvaliacoes ) {

                System.out.println("Iniciando a análise do produto: " + arquivo.getFileName());

                String resposta = enviarRequisicao(arquivo);

                salvarAnalise(arquivo.getFileName().toString().replace(".txt", ""), resposta);
            }
            System.out.println("Anáise Finalizada!");

        } catch (IOException | InterruptedException e) {
            System.out.println("Ocorreu um erro ao analisar as Análises de Sentimentos");
            throw new RuntimeException(e);
        }
    }

    private static String enviarRequisicao(Path arquivo) throws InterruptedException {

        var chave = System.getenv("OPENAI_API_KEY");
        var service = new OpenAiService(chave, Duration.ofSeconds(60));

        var promptUsuario = lerConteudoDoArquivo(arquivo);

        var promptSistema = """
                    Você é um analisador de sentimentos de avaliações de produtos.
                    Escreva um parágrafo com até 50 palavras resumindo as avaliações e depois atribua qual o sentimento geral para o produto.
                    Identifique também 3 pontos fortes e 3 pontos fracos identificados a partir das avaliações.
                                    
                    #### Formato de saída
                    Nome do produto:
                    Resumo das avaliações: [resuma em até 50 palavras]
                    Sentimento geral: [deve ser: POSITIVO, NEUTRO ou NEGATIVO]
                    Pontos fortes: [3 bullets points]
                    Pontos fracos: [3 bullets points]
                    """;

        var request = ChatCompletionRequest
                .builder()
                .model("gpt-4-1106-preview")
                .messages(Arrays.asList(
                        new ChatMessage(
                                ChatMessageRole.SYSTEM.value(),
                                promptSistema),
                        new ChatMessage(
                                ChatMessageRole.USER.value(),
                                promptUsuario)))
                .build();

        var segundoParaProximaTentiva = 5;
        var tentativas = 0;
        while (tentativas++ != 5) {
            try {
                return service
                        .createChatCompletion(request)
                        .getChoices().get(0).getMessage().getContent();
            } catch (OpenAiHttpException ex) {
                var errorCode = ex.statusCode;
                switch (errorCode) {
                    case 401 -> throw new RuntimeException("Erro com a chave da API!", ex);
                    case 429 -> {
                        System.out.println("Rate Limit atingido! Nova tentativa em instantes");
                        Thread.sleep(1000 * segundoParaProximaTentiva);
                        segundoParaProximaTentiva *= 2;
                    }
                    case 500, 503 -> {
                        System.out.println("API fora do ar! Nova tentativa em instantes");
                        Thread.sleep(1000 * segundoParaProximaTentiva);
                        segundoParaProximaTentiva *= 2;
                    }
                }
            }
        }
        throw new RuntimeException("API Fora do ar! Tentativas finalizadas sem sucesso!");
    }

    private static List<Path> carregarArquivosDeAvaliacoes(Path diretorioAvaliacoes) throws IOException {

        var arqAvaliacoes = Files
                .walk(diretorioAvaliacoes, 1)
                .filter(path -> path.toString().endsWith(".txt"))
                .collect(Collectors.toList());
        return arqAvaliacoes;
    }

    private static String lerConteudoDoArquivo(Path arquivo) {
        try {
            //var path = Path.of("src/main/resources/avaliacoes/avaliacoes-" +arquivo +".txt");
            return Files.readAllLines(arquivo).toString();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar o arquivo!", e);
        }
    }

    private static void salvarAnalise(String arquivo, String analise) {
        try {
            var path = Path.of("src/main/resources/analises/analise-sentimentos-" +arquivo +".txt");
            Files.writeString(path, analise, StandardOpenOption.CREATE_NEW);

            System.out.println("Arquivo: analise-sentimentos-" + arquivo + ".txt" +
                            " de análise gerado com sucesso! \n");

        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar o arquivo!", e);
        }
    }

}
