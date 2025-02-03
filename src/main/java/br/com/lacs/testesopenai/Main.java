package br.com.lacs.testesopenai;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        var user = "Gere 5 produtos para um eCommerce";

        var system = "Você é um gerador de produtos fictícios para um ecommerce e deve gerar apenas o nome dos produtos";

        var chaveToken = System.getenv("OPENAI_API_KEY");
        var service = new OpenAiService(chaveToken);

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
                .forEach(System.out::println);
    }
}
