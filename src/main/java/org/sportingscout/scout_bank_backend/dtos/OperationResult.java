package org.sportingscout.scout_bank_backend.dtos;

import java.util.List;

public record OperationResult<T>(
    T data,
    List<String> warnings,
    boolean hasWarnings) {
  public static <T> OperationResult<T> withWarnings(T data, List<String> warnings) {
    return new OperationResult<>(data, warnings, !warnings.isEmpty());
  }
}
