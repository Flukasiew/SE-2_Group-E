package com.pw.common.dto;

import com.pw.common.model.Action;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class PlayerConnectMessageDTO {
    private Action action;
    private Integer portNumber;
    private String playerGuid;
}
