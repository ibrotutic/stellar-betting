package game.controller;

import game.model.CoinFlipGame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.xml.ws.http.HTTPException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CoinFlipController {
    CoinFlipGame currentGame = new CoinFlipGame();
    List<String> previousGames = new ArrayList<>();

    @MessageMapping("/chat")
    @SendTo("/topic/gameState")
    public String sendCurrentGameState(final String message) {
        return "hello";
    }

    @PostMapping("/game/user/register")
    @ResponseBody
    public void registerUser(@RequestParam() String userId) throws IOException {
        currentGame.addUser(userId);
        sendCurrentGameState("hello");
    }

    @PostMapping("/game/user/sendNumber")
    @ResponseBody
    public void recordUsersNumber(@RequestParam() String number, @RequestParam() String userId) throws IOException {
        currentGame.updateUserGuess(Integer.parseInt(number), userId);
    }

    @PostMapping("/game/user/bet")
    @ResponseBody
    public void registerBetAndHash(@RequestParam() String userId, @RequestParam() String bet, @RequestParam() String hash) throws IOException {
        currentGame.updateUserBetAndHash(userId, Double.parseDouble(bet), hash);
    }

    @GetMapping("/game/user/balance")
    @ResponseBody
    public double getUserBalance(@RequestParam() String userId) {
        try {
            return currentGame.getUserBalance(userId);
        } catch (IOException e) {
            throw new HTTPException(HttpStatus.BAD_REQUEST.value());
        }
    }

    @GetMapping("/game/winner")
    @ResponseBody
    public String getWinner() {
        try {
            String result = currentGame.getWinner();
            previousGames.add(currentGame.userHistory());
            currentGame = new CoinFlipGame();
            return result;
        } catch (IOException e) {
            throw new HTTPException(HttpStatus.BAD_REQUEST.value());
        }
    }

    @GetMapping("/game/history")
    @ResponseBody
    public List<String> getHistory() throws IOException{
        if (previousGames.size() > 0) {
            return previousGames;
        } else {
            throw new IOException("No games have been played");
        }
    }

    @GetMapping("/game/gameState")
    @ResponseBody
    public String getGameState() {
        return currentGame.getGameState().toString();
    }
}
