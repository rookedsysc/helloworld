package org.rookedsysc.openapirequest.xml;

import lombok.RequiredArgsConstructor;
import org.rookedsysc.openapirequest.ApiKey;
import org.rookedsysc.openapirequest.xml.dto.DiseaseResponse;
import org.rookedsysc.openapirequest.xml.dto.Item;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * https://www.data.go.kr/data/15119055/openapi.do#/API%20%EB%AA%A9%EB%A1%9D/getDissNameCodeList1
 * 건강보험심사평가원_질병정보서비스
 */
@Service
@RequiredArgsConstructor
public class XmlRestClient {

    private final ApiKey apiKey;

    public List<Item> getDiseaseInfoList() {
        RestClient restClient = RestClient.builder()
            .requestFactory(new HttpComponentsClientHttpRequestFactory())
            .baseUrl("https://apis.data.go.kr")
            .defaultHeader("Content-Type", MediaType.APPLICATION_XML_VALUE)
            .defaultHeader("Accept", MediaType.APPLICATION_XML_VALUE)
            .defaultHeader("Accept-Charset", StandardCharsets.UTF_8.name())
            .messageConverters(List.of(new MappingJackson2XmlHttpMessageConverter()))
            .build();

        DiseaseResponse diseaseResponse = restClient.get()
            .uri("/B551182/diseaseInfoService1/getDissNameCodeList1?serviceKey=" + apiKey.disease()
                + "&numOfRows=10&pageNo=1&sickType=1&medTp=1&diseaseType=SICK_CD")
            .retrieve()
            .body(DiseaseResponse.class);

        return diseaseResponse.getBody().getItems().getItemList();
    }
}
