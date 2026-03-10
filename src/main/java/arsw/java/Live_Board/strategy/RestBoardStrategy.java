package arsw.java.Live_Board.strategy;

import arsw.java.Live_Board.Services.BoardService;
import arsw.java.Live_Board.model.StrokeDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Estrategia de comunicación usando REST.
 * Simplemente delega las llamadas al BoardService existente.
 */
@Component("rest")
public class RestBoardStrategy implements BoardCommunicationStrategy {

    @Autowired
    private BoardService boardService;

    @Override
    public void sendStroke(StrokeDto stroke) {
        boardService.addStroke(stroke);
    }

    @Override
    public void sendClear() {
        boardService.clear();
    }

    @Override
    public Map<String, Object> sendHeartbeat(String userId) {
        return boardService.updateUserActivity(userId);
    }

    @Override
    public Object getBoard() {
        return boardService.getBoard();
    }

    @Override
    public Map<String, Object> getBoardInfo() {
        return boardService.getBoardInfo();
    }

    @Override
    public Map<String, Object> getChangesSince(Long since) {
        return boardService.getChangesSince(since);
    }
}
