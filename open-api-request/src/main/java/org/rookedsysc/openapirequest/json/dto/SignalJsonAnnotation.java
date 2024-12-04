package org.rookedsysc.openapirequest.json.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.sql.Timestamp;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class SignalJsonAnnotation {
    private String dataId;
    private String trsmUtcTime;
    private Long ntPdsgRmdrCs;
    private Long etPdsgRmdrCs;
    private Long stPdsgRmdrCs;
    private Long wtPdsgRmdrCs;
    private Long nePdsgRmdrCs;
    private Long sePdsgRmdrCs;
    private Long swPdsgRmdrCs;
    private Long nwPdsgRmdrCs;
}
