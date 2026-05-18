package rahafalamri.github.com.bookshare.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import rahafalamri.github.com.bookshare.Api.ApiException;
import rahafalamri.github.com.bookshare.Model.Book;
import rahafalamri.github.com.bookshare.Model.User;
import rahafalamri.github.com.bookshare.Repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AIService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private final BookRepository bookRepository;
    private final UserService userService;

    public Map<String, Object> recommendBooks(Integer userId) {

        User user = userService.getUserById(userId);

        if (user.getInterests() == null || user.getInterests().isEmpty()) {
            throw new ApiException("User interests are required for recommendations");
        }

        List<Book> books = bookRepository.findApprovedBooks();

        if (books.isEmpty()) {
            throw new ApiException("No approved books available for recommendation");
        }

        String availableBooks = "";

        for (Book book : books) {
            availableBooks +=
                    "Book ID: " + book.getBookId()
                            + ", Title: " + book.getTitle()
                            + ", Category: " + book.getCategory()
                            + ", Description: " + book.getDescription()
                            + "\n";
        }

        String prompt = """
                ROLE:
                You are an AI book recommendation assistant for a book borrowing platform.

                TASK:
                Recommend books to the user based on their interests.

                IMPORTANT RULES:
                Recommend ONLY books from the available books list.
                Do not invent book titles.
                Return maximum 3 books.
                The response must be valid JSON only.
                Do not add markdown.
                Do not add explanation outside JSON.

                USER INTERESTS:
                "%s"

                AVAILABLE BOOKS:
                %s

                OUTPUT JSON FORMAT:
                {
                  "message": "Recommended books based on your interests.",
                  "recommendations": [
                    {
                      "bookId": 1,
                      "title": "Book title",
                      "reason": "Short reason why this book matches the user's interests."
                    }
                  ]
                }
                """.formatted(user.getInterests(), availableBooks);

        return askChat(prompt);
    }

    private Map<String, Object> askChat(String prompt) {

        String url = "https://api.openai.com/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = new HashMap<>();

        body.put("model", "gpt-4o-mini");

        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", prompt);

        messages.add(userMsg);

        body.put("messages", messages);

        HttpEntity<Map<String, Object>> requestEntity =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> responseEntity =
                restTemplate.postForEntity(url, requestEntity, Map.class);

        Map responseBody = responseEntity.getBody();

        if (responseBody == null) {
            throw new ApiException("AI did not return a response");
        }

        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");

        if (choices == null || choices.isEmpty()) {
            throw new ApiException("AI returned no choices");
        }

        Map<String, Object> firstChoice = choices.get(0);

        Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");

        if (message == null) {
            throw new ApiException("AI returned an empty message");
        }

        Object content = message.get("content");

        if (content == null) {
            throw new ApiException("AI returned no content");
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();

            return objectMapper.readValue(
                    content.toString(),
                    Map.class
            );

        } catch (Exception e) {
            throw new ApiException("Failed to parse AI response");
        }
    }
}