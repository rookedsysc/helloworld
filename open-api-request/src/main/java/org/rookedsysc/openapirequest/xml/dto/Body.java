package org.rookedsysc.openapirequest.xml.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Body {
    @JacksonXmlProperty(localName = "items")
    private Items items;
    @JacksonXmlProperty(localName = "numOfRows")
    private int numOfRows;
    @JacksonXmlProperty(localName = "pageNo")
    private int pageNo;
    @JacksonXmlProperty(localName = "totalCount")
    private int totalCount;
}
