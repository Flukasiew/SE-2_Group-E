package com.pw.common.dto;

        import com.pw.common.model.Action;
        import com.pw.common.model.GameSetupStatus;
        import lombok.AllArgsConstructor;
        import lombok.Data;
        import lombok.EqualsAndHashCode;
        import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class GameSetupStatusDTO {
    private Action action;
    private GameSetupStatus status;
}
