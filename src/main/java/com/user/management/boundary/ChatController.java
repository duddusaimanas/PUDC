package com.user.management.boundary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.user.management.config.CrawlerService;
import com.user.management.config.WebPageLoader;
import com.user.management.entity.LocalDocument;
import com.user.management.entity.LocalSearchRequest;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
@Slf4j
public class ChatController {

        private final ChatClient chatClient;
        private final VectorStore vectorStore;
        private final CrawlerService crawlerService;
        private final WebPageLoader webPageLoader;

        @Value("classpath:templates/DefaultSystemTemplate.ST")
        private Resource defaultSystemTemplate;

        @Value("classpath:templates/WithContextSystemTemplate.ST")
        private Resource withContextSystemTemplate;

        @Value("classpath:templates/WithContextUserTemplate.ST")
        private Resource withContextUserTemplate;

        @Value("classpath:templates/PromptVariationsUserTemplate.ST")
        private Resource promptVariationsUserTemplate;

        private static final String MEMORY_CONVERSATION_ID = "chat_memory_conversation_id";

        private static final String MEMORY_RESPONSE_SIZE = "chat_memory_response_size";

        public ChatController(ChatClient.Builder chatClientBuilder, VectorStore vectorStore,
                        CrawlerService crawlerService, WebPageLoader webPageLoader,
                        @Value("classpath:templates/PromptVariationsSystemTemplate.ST") Resource promptVariationsSystemTemplate) {
                this.chatClient = chatClientBuilder
                                .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()),
                                                new SimpleLoggerAdvisor())
                                .build();
                this.vectorStore = vectorStore;
                this.crawlerService = crawlerService;
                this.webPageLoader = webPageLoader;
        }

        @GetMapping("/")
        public Flux<String> chat(@RequestParam final String prompt, @RequestParam final String conversationId)
                        throws IOException {
                List<String> urls = extractUrls(prompt);
                if (!urls.isEmpty()) {
                        log.info("Found URLs in the prompt..");

                        List<String> context = urls.stream()
                                        .map(url -> webPageLoader.visit(url))
                                        .map(LocalDocument::toString)
                                        .filter(StringUtils::hasText).toList();

                        if (CollectionUtils.isEmpty(context)) {
                                return defaultClientCall(prompt, conversationId);
                        }
                        return clientCallWithContext(prompt, conversationId, context);
                }

                LocalSearchRequest search = promptVariations(prompt);
                log.info("search: {}", search);
                if (search.isRequiresWebSearch()) {
                        log.info("Needs real time data..");

                        List<String> context = search.getPromptVariations().stream()
                                        .map(promptVariation -> webPageLoader.webSearch(promptVariation))
                                        .map(LocalDocument::toString)
                                        .filter(StringUtils::hasText).toList();

                        if (CollectionUtils.isEmpty(context)) {
                                return defaultClientCall(prompt, conversationId);
                        }
                        return clientCallWithContext(prompt, conversationId, context);
                }

                return defaultClientCall(prompt, conversationId);
        }

        private List<String> extractUrls(String text) throws IOException {
                Pattern pattern = Pattern.compile(
                                "\\b((?:https?|ftp|file):\\/\\/[a-zA-Z0-9.-]+(?:\\:[a-zA-Z0-9.&%$-]+)?(?:\\/[a-zA-Z0-9+&@#/%?=~_|!:,.;-]*)?)",
                                Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(text);

                List<String> urlList = new ArrayList<>();
                while (matcher.find()) {
                        urlList.add(matcher.group());
                }
                return urlList;
        }

        @GetMapping("/web")
        public Flux<String> webSearch(@RequestParam final String prompt, @RequestParam final String conversationId) {
                List<String> context = null;
                try {
                        context = crawlerService.crawl(promptVariations(prompt)).stream().toList();
                } catch (Exception e) {
                        log.warn("Crawler service failed. {}", e.getMessage(), e);
                }
                if (context == null) {
                        return Flux.just("Something wen't wrong. Try again.");
                }
                return clientCallWithContext(prompt, conversationId, context);
        }

        private LocalSearchRequest promptVariations(String prompt) {
                return chatClient.prompt()
                                .user(new PromptTemplate(promptVariationsUserTemplate).render(Map.of("prompt", prompt)))
                                .call().entity(LocalSearchRequest.class);
        }

        @GetMapping("/pdf")
        public Flux<String> pdfSearch(@RequestParam final String prompt,
                        @RequestParam final String conversationId) {
                return chatClient.prompt()
                                .advisors(advisor -> advisor.param(MEMORY_CONVERSATION_ID, conversationId)
                                                .param(MEMORY_RESPONSE_SIZE, 100))
                                .advisors(new QuestionAnswerAdvisor(vectorStore, SearchRequest.defaults()))
                                .user(prompt)
                                .stream().content();
        }

        private Flux<String> defaultClientCall(final String prompt, final String conversationId) {
                return chatClient.prompt()
                                .advisors((advisor -> advisor.param(MEMORY_CONVERSATION_ID, conversationId)
                                                .param(MEMORY_RESPONSE_SIZE, 100)))
                                .system(defaultSystemTemplate)
                                .user(prompt)
                                .stream().content();
        }

        private Flux<String> clientCallWithContext(final String prompt, final String conversationId,
                        List<String> context) {
                return chatClient.prompt()
                                .advisors(advisor -> advisor.param(MEMORY_CONVERSATION_ID, conversationId)
                                                .param(MEMORY_RESPONSE_SIZE, 100))
                                .system(withContextSystemTemplate)
                                .user(new PromptTemplate(withContextUserTemplate).render(
                                                Map.of("context", context.stream()
                                                                .collect(Collectors.joining(
                                                                                System.lineSeparator())),
                                                                "prompt", prompt)))
                                .stream().content();
        }
}