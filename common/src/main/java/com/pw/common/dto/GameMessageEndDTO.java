package com.pw.common.dto;

import com.pw.common.model.Action;
import com.pw.common.model.GameEndResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class GameMessageEndDTO {
    private Action action;
    private GameEndResult result;
}
