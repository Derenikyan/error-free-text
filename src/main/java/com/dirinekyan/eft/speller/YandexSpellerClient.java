package com.dirinekyan.eft.speller;

import com.dirinekyan.eft.domain.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

/**
 * Клиент метода checkTexts Яндекс.Спеллера:
 * https://yandex.ru/dev/speller/doc/ru/reference/checkTexts
 */
@Component
public class YandexSpellerClient implements SpellerClient {

    private static final Logger log = LoggerFactory.getLogger(YandexSpellerClient.class);

    private final RestClient restClient;

    public YandexSpellerClient(SpellerProperties properties, RestClient.Builder builder) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeout());
        requestFactory.setReadTimeout(properties.readTimeout());
        this.restClient = builder
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public List<SpellError> check(String text, Language language, int options) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("text", text);
        form.add("lang", language.code());
        form.add("options", String.valueOf(options));
        form.add("format", "plain");
        try {
            List<List<SpellError>> response = restClient.post()
                    .uri("/checkTexts")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<List<SpellError>>>() {
                    });
            if (response == null || response.isEmpty()) {
                throw new SpellerException("Пустой ответ от Яндекс.Спеллера");
            }
            return response.get(0);
        } catch (RestClientException e) {
            log.warn("Speller request failed: {}", e.getMessage());
            throw new SpellerException("Ошибка обращения к Яндекс.Спеллеру: " + e.getMessage(), e);
        }
    }
}
