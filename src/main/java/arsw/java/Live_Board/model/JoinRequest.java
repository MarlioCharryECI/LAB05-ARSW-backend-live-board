package arsw.java.Live_Board.model;

import jakarta.validation.constraints.NotBlank;

public class JoinRequest {
  @NotBlank(message = "userId es requerido")
  public String userId;
}
