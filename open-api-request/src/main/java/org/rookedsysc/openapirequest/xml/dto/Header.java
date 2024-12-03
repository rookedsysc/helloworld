package org.rookedsysc.openapirequest.xml.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Header {
    @JacksonXmlProperty(localName = "resultCode")
    private String resultCode;
    @JacksonXmlProperty(localName = "resultMsg")
    private String resultMsg;
}
