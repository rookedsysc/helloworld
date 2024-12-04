package org.rookedsysc.openapirequest.json.dto;

import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.Serializable;

@Data
public class SignalJsonSerializable implements Serializable {
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
