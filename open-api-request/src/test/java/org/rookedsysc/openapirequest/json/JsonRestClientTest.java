package org.rookedsysc.openapirequest.json;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rookedsysc.openapirequest.json.dto.SignalJsonAnnotation;
import org.rookedsysc.openapirequest.json.dto.SignalJsonSerializable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class JsonRestClientTest {

    @Autowired
    private JsonRestClient jsonRestClient;

    @Test
    @DisplayName("Annotaiton API 호출 정상적으로 잘 되는지 테스트")
    void getSignalJsonAnnotationTest() {
        // given // when
        List<SignalJsonAnnotation> responses = jsonRestClient.getSignalJsonAnnotation();

        // then
        assertThat(responses.size()).isEqualTo(10);
    }

    @Test
    @DisplayName("API 호출 정상적으로 잘 되는지 테스트")
    void getSignalJsonSerializableTest() {
        // given // when
        List<SignalJsonSerializable> responses = jsonRestClient.getSignalJsonSerializable();

        // then
        assertThat(responses.size()).isEqualTo(10);
    }

}
