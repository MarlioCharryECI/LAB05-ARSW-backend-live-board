package arsw.java.Live_Board.Services;

import arsw.java.Live_Board.model.JoinResponse;
import arsw.java.Live_Board.model.PointDto;
import arsw.java.Live_Board.model.StrokeDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BoardServiceTest {

    private BoardService boardService;

    @BeforeEach
    void setUp() {
        boardService = new BoardService();
    }

    @Test
    void join_NewUser_ShouldAssignColorAndCreateResponse() {
        String userId = "usuario1";
        
        JoinResponse response = boardService.join(userId);
        
        assertNotNull(response);
        assertEquals(userId, response.userId);
        assertNotNull(response.color);
        assertTrue(response.color.startsWith("#"));
        assertEquals(7, response.color.length());
    }

    @Test
    void join_ExistingUser_ShouldReturnSameColor() {
        String userId = "usuario1";
        
        JoinResponse firstResponse = boardService.join(userId);
        JoinResponse secondResponse = boardService.join(userId);
        
        assertEquals(firstResponse.color, secondResponse.color);
        assertEquals(firstResponse.userId, secondResponse.userId);
    }

    @Test
    void join_NullUserId_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> boardService.join(null));
    }

    @Test
    void join_EmptyUserId_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> boardService.join(""));
    }

    @Test
    void getBoard_EmptyBoard_ShouldReturnEmptyList() {
        List<StrokeDto> board = boardService.getBoard();
        
        assertNotNull(board);
        assertTrue(board.isEmpty());
    }

    @Test
    void addStroke_ValidStroke_ShouldAddToBoard() {
        StrokeDto stroke = createValidStroke("usuario1");
        
        boardService.addStroke(stroke);
        List<StrokeDto> board = boardService.getBoard();
        
        assertEquals(1, board.size());
        assertEquals(stroke.userId, board.get(0).userId);
        assertEquals(stroke.points.size(), board.get(0).points.size());
    }

    @Test
    void addStroke_NullStroke_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> boardService.addStroke(null));
    }

    @Test
    void addStroke_InvalidStroke_ShouldThrowException() {
        StrokeDto invalidStroke = new StrokeDto();
        invalidStroke.userId = "";
        invalidStroke.points = List.of(createPoint(10, 20));
        
        assertThrows(IllegalArgumentException.class, () -> boardService.addStroke(invalidStroke));
    }

    @Test
    void clear_BoardWithStrokes_ShouldRemoveAllStrokes() {
        StrokeDto stroke1 = createValidStroke("usuario1");
        StrokeDto stroke2 = createValidStroke("usuario2");
        
        boardService.addStroke(stroke1);
        boardService.addStroke(stroke2);
        assertEquals(2, boardService.getBoard().size());
        
        boardService.clear();
        assertTrue(boardService.getBoard().isEmpty());
    }

    @Test
    void updateUserActivity_ExistingUser_ShouldReturnUserInfo() {
        String userId = "usuario1";
        boardService.join(userId);
        
        Map<String, Object> userInfo = boardService.updateUserActivity(userId);
        
        assertFalse(userInfo.isEmpty());
        assertEquals(userId, userInfo.get("userId"));
        assertNotNull(userInfo.get("color"));
        assertNotNull(userInfo.get("lastActivity"));
        assertNotNull(userInfo.get("boardVersion"));
    }

    @Test
    void updateUserActivity_NonExistentUser_ShouldReturnEmptyMap() {
        String userId = "usuarioInexistente";
        
        Map<String, Object> userInfo = boardService.updateUserActivity(userId);
        
        assertTrue(userInfo.isEmpty());
    }

    @Test
    void updateUserActivity_NullUserId_ShouldReturnEmptyMap() {
        Map<String, Object> userInfo = boardService.updateUserActivity(null);
        
        assertTrue(userInfo.isEmpty());
    }

    @Test
    void updateUserActivity_EmptyUserId_ShouldReturnEmptyMap() {
        Map<String, Object> userInfo = boardService.updateUserActivity("");
        
        assertTrue(userInfo.isEmpty());
    }

    @Test
    void getBoardInfo_ShouldReturnCompleteInfo() {
        String userId = "usuario1";
        boardService.join(userId);
        StrokeDto stroke = createValidStroke(userId);
        boardService.addStroke(stroke);
        
        Map<String, Object> info = boardService.getBoardInfo();
        
        assertNotNull(info);
        assertEquals(1, info.get("totalStrokes"));
        assertEquals(1, info.get("activeUsers"));
        assertTrue(info.containsKey("availableColors"));
        assertTrue(info.containsKey("boardVersion"));
        assertTrue(info.containsKey("lastModified"));
        assertTrue(info.containsKey("timestamp"));
    }

    @Test
    void getChangesSince_NullSince_ShouldReturnAllStrokes() {
        StrokeDto stroke = createValidStroke("usuario1");
        boardService.addStroke(stroke);
        
        Map<String, Object> changes = boardService.getChangesSince(null);
        
        assertNotNull(changes);
        assertTrue(changes.containsKey("strokes"));
        assertTrue(changes.containsKey("boardVersion"));
        assertTrue(changes.containsKey("lastModified"));
        assertTrue(changes.containsKey("timestamp"));
        
        @SuppressWarnings("unchecked")
        List<StrokeDto> strokes = (List<StrokeDto>) changes.get("strokes");
        assertEquals(1, strokes.size());
    }

    @Test
    void getChangesSince_WithSince_ShouldReturnChanges() {
        long beforeTime = System.currentTimeMillis();
        StrokeDto stroke = createValidStroke("usuario1");
        boardService.addStroke(stroke);
        
        Map<String, Object> changes = boardService.getChangesSince(beforeTime);
        
        @SuppressWarnings("unchecked")
        List<StrokeDto> strokes = (List<StrokeDto>) changes.get("strokes");
        assertEquals(1, strokes.size());
    }

    @Test
    void getStats_ShouldReturnStatistics() {
        String userId1 = "usuario1";
        String userId2 = "usuario2";
        boardService.join(userId1);
        boardService.join(userId2);
        boardService.addStroke(createValidStroke(userId1));
        
        Map<String, Object> stats = boardService.getStats();
        
        assertEquals(1, stats.get("totalStrokes"));
        assertEquals(2, stats.get("activeUsers"));
        assertTrue(stats.containsKey("availableColors"));
        assertTrue(stats.containsKey("boardVersion"));
        assertTrue(stats.containsKey("lastModified"));
    }

    private StrokeDto createValidStroke(String userId) {
        StrokeDto stroke = new StrokeDto();
        stroke.userId = userId;
        stroke.points = List.of(createPoint(10, 20), createPoint(30, 40), createPoint(50, 60));
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
