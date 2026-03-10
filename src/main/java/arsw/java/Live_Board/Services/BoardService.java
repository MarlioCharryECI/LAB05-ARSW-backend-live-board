package arsw.java.Live_Board.Services;
import arsw.java.Live_Board.model.JoinResponse;
import arsw.java.Live_Board.model.StrokeDto;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class BoardService {

    private final List<StrokeDto> strokes = new CopyOnWriteArrayList<>();
    private final Map<String, String> userColors = new HashMap<>();
    private final Deque<String> palette = new ArrayDeque<>(List.of(
            "#e6194b","#3cb44b","#ffe119","#4363d8","#f58231","#911eb4","#46f0f0",
            "#f032e6","#bcf60c","#fabebe","#008080","#e6beff","#9a6324","#fffac8",
            "#800000","#aaffc3","#808000","#ffd8b1","#000075","#808080"
    ));
    private final Random rnd = new Random();

    public synchronized JoinResponse join(String userId) {
        String color = userColors.get(userId);
        if (color == null) {
            color = palette.isEmpty() ? String.format("#%06x", rnd.nextInt(0xFFFFFF)) : palette.pop();
            userColors.put(userId, color);
        }
        JoinResponse resp = new JoinResponse();
        resp.userId = userId;
        resp.color = color;
        return resp;
    }

    public List<StrokeDto> getBoard() {
        return strokes;
    }

    public void addStroke(StrokeDto s) {
        if (s != null && s.points != null && s.points.size() >= 1) {
            strokes.add(s);
        }
    }

    public void clear() {
        strokes.clear();
    }
}
