package arsw.java.Live_Board.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class StrokeDto {
  @NotBlank(message = "id es requerido")
  public String id;

  @NotBlank(message = "userId es requerido")
  public String userId;

  @NotBlank(message = "color es requerido")
  public String color;

  public double width;

  @NotNull(message = "points es requerido")
  public List<PointDto> points;
}
