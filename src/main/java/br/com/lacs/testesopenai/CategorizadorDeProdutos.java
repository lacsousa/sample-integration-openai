package br.com.lacs.testesopenai;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

import java.time.Duration;
import java.util.Arrays;
import java.util.Scanner;

public class CategorizadorDeProdutos {

    public static void main(String[] args) {

        var leitor = new Scanner(System.in);

        // Higiene pessoal, Eletrônicos, Esportes, Alimentação, Outros
        System.out.println("Digite as categorias válidas");
        var categorias = leitor.nextLine();

        while(true) {

            System.out.println("\n Digite o nome do Produto");
            var user = leitor.nextLine();

            var system = """
                        Você é um categorizador de produtos e deve responder apenas o nome da categoria do produto informado
                        
                        Escolha uma categoria dentro a lista abaixo:
                        
                        %s
                        
                        ######Exemplo de uso:
                        Pergunta: Bola de futebol
                        Resposta: Esportes
                        
                        ###### regras a serem seguidas:
                        Caso o usuário pergunte algo que não seja de categorização de produtos, voce deve responder que não pode ajudá-lo, pois o seu papel é apenas responder a categoria dos produtos
                                  
                    """.formatted(categorias);

            dispararRequisicao(user, system);

            System.out.println("Digite <Ctrl + D> para finalizar");
        }
    }

    private static void dispararRequisicao(String user, String system) {
        var chaveToken = System.getenv("OPENAI_API_KEY");
        var service = new OpenAiService(chaveToken, Duration.ofSeconds(30));

        var completionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-4")
                .messages(Arrays.asList(
                        new ChatMessage(ChatMessageRole.USER.value(), user),
                        new ChatMessage(ChatMessageRole.SYSTEM.value(), system)
                ))
                .build();

        service
                .createChatCompletion(completionRequest)
                .getChoices()
                .forEach(c -> {
                    System.out.println("---------------");
                    System.out.println("Categoria: "+ c.getMessage().getContent());
                    System.out.println("---------------");
                });
    }
}
