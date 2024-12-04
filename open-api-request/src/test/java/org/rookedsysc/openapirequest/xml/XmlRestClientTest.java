package org.rookedsysc.openapirequest.xml;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rookedsysc.openapirequest.xml.dto.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class XmlRestClientTest {

    @Autowired
    private XmlRestClient xmlRestClient;

    @Test
    @DisplayName("질병정보 리스트 조회 테스트")
    void getDiseaseInfoList() {
        // given
        // when
        List<Item> items = xmlRestClient.getDiseaseInfoList();
        // then
        assertThat(items.size()).isEqualTo(10);
    }

}
