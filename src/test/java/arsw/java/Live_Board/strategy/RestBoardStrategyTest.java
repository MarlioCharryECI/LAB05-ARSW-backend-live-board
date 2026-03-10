package arsw.java.Live_Board.strategy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import arsw.java.Live_Board.Services.BoardService;
import arsw.java.Live_Board.model.PointDto;
import arsw.java.Live_Board.model.StrokeDto;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class RestBoardStrategyTest {

  @Mock private BoardService boardService;

  private RestBoardStrategy restStrategy;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    restStrategy = new RestBoardStrategy();

    // Inyectar el mock usando reflection
    java.lang.reflect.Field field;
    try {
      field = RestBoardStrategy.class.getDeclaredField("boardService");
      field.setAccessible(true);
      field.set(restStrategy, boardService);
    } catch (Exception e) {
      fail("Failed to inject mock BoardService: " + e.getMessage());
    }
  }

  @Test
  void sendStroke_ShouldDelegateToBoardService() {
    StrokeDto stroke = createValidStroke();

    restStrategy.sendStroke(stroke);

    verify(boardService, times(1)).addStroke(stroke);
  }

  @Test
  void sendClear_ShouldDelegateToBoardService() {
    restStrategy.sendClear();

    verify(boardService, times(1)).clear();
  }

  @Test
  void sendHeartbeat_ShouldDelegateToBoardService() {
    String userId = "usuario1";
    Map<String, Object> expectedResponse = Map.of("userId", userId);

    when(boardService.updateUserActivity(userId)).thenReturn(expectedResponse);

    Map<String, Object> response = restStrategy.sendHeartbeat(userId);

    verify(boardService, times(1)).updateUserActivity(userId);
    assertEquals(expectedResponse, response);
  }

  @Test
  void getBoard_ShouldDelegateToBoardService() {
    List<StrokeDto> expectedBoard = List.of(createValidStroke());

    when(boardService.getBoard()).thenReturn(expectedBoard);

    Object board = restStrategy.getBoard();

    verify(boardService, times(1)).getBoard();
    assertEquals(expectedBoard, board);
  }

  @Test
  void getBoardInfo_ShouldDelegateToBoardService() {
    Map<String, Object> expectedInfo =
        Map.of(
            "totalStrokes", 5,
            "activeUsers", 2);

    when(boardService.getBoardInfo()).thenReturn(expectedInfo);

    Map<String, Object> info = restStrategy.getBoardInfo();

    verify(boardService, times(1)).getBoardInfo();
    assertEquals(expectedInfo, info);
  }

  @Test
  void getChangesSince_ShouldDelegateToBoardService() {
    Long since = System.currentTimeMillis();
    Map<String, Object> expectedChanges = Map.of("strokes", List.of(), "boardVersion", 1L);

    when(boardService.getChangesSince(since)).thenReturn(expectedChanges);

    Map<String, Object> changes = restStrategy.getChangesSince(since);

    verify(boardService, times(1)).getChangesSince(since);
    assertEquals(expectedChanges, changes);
  }

  @Test
  void getChangesSince_WithNullSince_ShouldDelegateToBoardService() {
    Map<String, Object> expectedChanges =
        Map.of("strokes", List.of(createValidStroke()), "boardVersion", 2L);

    when(boardService.getChangesSince(null)).thenReturn(expectedChanges);

    Map<String, Object> changes = restStrategy.getChangesSince(null);

    verify(boardService, times(1)).getChangesSince(null);
    assertEquals(expectedChanges, changes);
  }

  private StrokeDto createValidStroke() {
    StrokeDto stroke = new StrokeDto();
    stroke.userId = "testUser";
    stroke.points = List.of(createPoint(10, 20), createPoint(30, 40));
    stroke.color = "#FF0000";
    return stroke;
  }

  private PointDto createPoint(double x, double y) {
    PointDto point = new PointDto();
    point.x = x;
    point.y = y;
    return point;
  }
}
