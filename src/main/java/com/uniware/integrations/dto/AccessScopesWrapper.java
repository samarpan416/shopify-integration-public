package com.uniware.integrations.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AccessScopesWrapper {
    @JsonProperty("access_scopes")
    List<AccessScope> accessScopes;

    @Data
    public static class AccessScope {
        String handle;
    }
}
