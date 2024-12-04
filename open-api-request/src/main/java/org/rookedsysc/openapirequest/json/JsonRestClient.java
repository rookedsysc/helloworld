package org.rookedsysc.openapirequest.json;

import lombok.RequiredArgsConstructor;
import org.rookedsysc.openapirequest.ApiKey;
import org.rookedsysc.openapirequest.json.dto.SignalJsonAnnotation;
import org.rookedsysc.openapirequest.json.dto.SignalJsonSerializable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 신호제어기 : https://t-data.seoul.go.kr/dataprovide/trafficdataviewopenapi.do?data_id=10120
 */
@Service
@RequiredArgsConstructor
public class JsonRestClient {

    private final ApiKey apiKey;

    public List<SignalJsonAnnotation> getSignalJsonAnnotation() {
        RestClient restClient = RestClient.builder()
            .baseUrl("https://t-data.seoul.go.kr")
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader("Accept-Charset", StandardCharsets.UTF_8.name())
            .build();

        String encodedApiKey = URLEncoder.encode(apiKey.signal(), StandardCharsets.UTF_8);

        List<SignalJsonAnnotation> signalJsonAnnotation = restClient.get()
            .uri("/apig/apiman-gateway/tapi/v2xSignalPhaseTimingInformation/1.0?apikey=" + apiKey.signal() + "&pageNo=1&numOfRows=10"
            )
            .retrieve()
            .body(new ParameterizedTypeReference<List<SignalJsonAnnotation>>() {
            });

        return signalJsonAnnotation;
    }

    public List<SignalJsonSerializable> getSignalJsonSerializable() {
        RestClient restClient = RestClient.builder()
            .baseUrl("https://t-data.seoul.go.kr")
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader("Accept-Charset", StandardCharsets.UTF_8.name())
            .build();

        String encodedApiKey = URLEncoder.encode(apiKey.signal(), StandardCharsets.UTF_8);

        List<SignalJsonSerializable> signalJsonAnnotation = restClient.get()
            .uri("/apig/apiman-gateway/tapi/v2xSignalPhaseTimingInformation/1.0?apikey=" + apiKey.signal() + "&pageNo=1&numOfRows=10"
            )
            .retrieve()
            .body(new ParameterizedTypeReference<List<SignalJsonSerializable>>() {
            });

        return signalJsonAnnotation;
    }

}
