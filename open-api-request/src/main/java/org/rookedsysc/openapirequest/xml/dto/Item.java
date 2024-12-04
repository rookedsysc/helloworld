package org.rookedsysc.openapirequest.xml.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Item {
    @JacksonXmlProperty(localName = "sickCd")
    private String sickCd;
    @JacksonXmlProperty(localName = "sickEngNm")
    private String sickEngNm;
    @JacksonXmlProperty(localName = "sickNm")
    private String sickNm;
}
