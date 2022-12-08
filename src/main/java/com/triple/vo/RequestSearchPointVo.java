package com.triple.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class RequestSearchPointVo {
    @NotNull(message = "UserId field is required")
    private UUID userId;
}
