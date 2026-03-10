package arsw.java.Live_Board.Controllers;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import arsw.java.Live_Board.Services.BoardService;
import arsw.java.Live_Board.factory.BoardCommunicationFactory;
import arsw.java.Live_Board.model.JoinRequest;
import arsw.java.Live_Board.model.JoinResponse;
import arsw.java.Live_Board.model.PointDto;
import arsw.java.Live_Board.model.StrokeDto;
import arsw.java.Live_Board.strategy.BoardCommunicationStrategy;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class BoardControllerTest {

  @Mock private BoardService boardService;

  @Mock private BoardCommunicationFactory communicationFactory;

  @Mock private BoardCommunicationStrategy strategy;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    BoardController controller = new BoardController(boardService, communicationFactory);
    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    objectMapper = new ObjectMapper();

    when(communicationFactory.getStrategy()).thenReturn(strategy);
  }

  @Test
  void join_ValidRequest_ShouldReturnJoinResponse() throws Exception {
    JoinRequest request = new JoinRequest();
    request.userId = "usuario1";

    JoinResponse expectedResponse = new JoinResponse();
    expectedResponse.userId = "usuario1";
    expectedResponse.color = "#FF0000";

    when(boardService.join("usuario1")).thenReturn(expectedResponse);

    mockMvc
        .perform(
            post("/api/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value("usuario1"))
        .andExpect(jsonPath("$.color").value("#FF0000"));

    verify(boardService, times(1)).join("usuario1");
  }

  @Test
  void join_NullColorResponse_ShouldReturnConflict() throws Exception {
    JoinRequest request = new JoinRequest();
    request.userId = "usuario1";

    JoinResponse response = new JoinResponse();
    response.userId = "usuario1";
    response.color = null;

    when(boardService.join("usuario1")).thenReturn(response);

    mockMvc
        .perform(
            post("/api/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict());
  }

  @Test
  void join_EmptyColorResponse_ShouldReturnConflict() throws Exception {
    JoinRequest request = new JoinRequest();
    request.userId = "usuario1";

    JoinResponse response = new JoinResponse();
    response.userId = "usuario1";
    response.color = "";

    when(boardService.join("usuario1")).thenReturn(response);

    mockMvc
        .perform(
            post("/api/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict());
  }

  @Test
  void draw_ValidStroke_ShouldReturnCreated() throws Exception {
    StrokeDto stroke = createValidStroke();

    doNothing().when(strategy).sendStroke(any(StrokeDto.class));

    mockMvc
        .perform(
            post("/api/draw")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(stroke)))
        .andExpect(status().isCreated());

    verify(strategy, times(1)).sendStroke(any(StrokeDto.class));
  }

  @Test
  void clear_ShouldReturnNoContent() throws Exception {
    doNothing().when(strategy).sendClear();

    mockMvc.perform(post("/api/clear")).andExpect(status().isNoContent());

    verify(strategy, times(1)).sendClear();
  }

  @Test
  void syncBoard_WithoutSince_ShouldReturnChanges() throws Exception {
    Map<String, Object> expectedChanges = Map.of("strokes", List.of(), "boardVersion", 1L);

    when(strategy.getChangesSince(isNull())).thenReturn(expectedChanges);

    mockMvc
        .perform(get("/api/board/sync"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.boardVersion").value(1));

    verify(strategy, times(1)).getChangesSince(isNull());
  }

  @Test
  void syncBoard_WithSince_ShouldReturnChanges() throws Exception {
    Long since = System.currentTimeMillis();
    Map<String, Object> expectedChanges =
        Map.of("strokes", List.of(createValidStroke()), "boardVersion", 2L);

    when(strategy.getChangesSince(eq(since))).thenReturn(expectedChanges);

    mockMvc
        .perform(get("/api/board/sync").param("since", since.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.boardVersion").value(2));

    verify(strategy, times(1)).getChangesSince(eq(since));
  }

  @Test
  void heartbeat_ExistingUser_ShouldReturnUserInfo() throws Exception {
    String userId = "usuario1";
    Map<String, Object> expectedResponse = Map.of("userId", userId, "color", "#FF0000");

    when(strategy.sendHeartbeat(userId)).thenReturn(expectedResponse);

    mockMvc
        .perform(post("/api/users/{userId}/heartbeat", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(userId));

    verify(strategy, times(1)).sendHeartbeat(userId);
  }

  @Test
  void heartbeat_NonExistentUser_ShouldReturnNotFound() throws Exception {
    String userId = "usuarioInexistente";

    when(strategy.sendHeartbeat(userId)).thenReturn(Map.of());

    mockMvc.perform(post("/api/users/{userId}/heartbeat", userId)).andExpect(status().isNotFound());

    verify(strategy, times(1)).sendHeartbeat(userId);
  }

  @Test
  void getBoardInfo_ShouldReturnInfo() throws Exception {
    Map<String, Object> expectedInfo =
        Map.of(
            "totalStrokes", 5,
            "activeUsers", 2,
            "boardVersion", 3L);

    when(strategy.getBoardInfo()).thenReturn(expectedInfo);

    mockMvc
        .perform(get("/api/board/info"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalStrokes").value(5))
        .andExpect(jsonPath("$.activeUsers").value(2))
        .andExpect(jsonPath("$.boardVersion").value(3));

    verify(strategy, times(1)).getBoardInfo();
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
