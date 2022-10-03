package com.uniware.integrations.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class GraphQLRequest{
    String query;
    Map<String, Object> variables;
}
