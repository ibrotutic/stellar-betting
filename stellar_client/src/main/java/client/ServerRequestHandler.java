package client;

import okhttp3.*;

import java.util.HashMap;
import java.util.Map;

public class ServerRequestHandler {
    private static String REGISTER_URL = "http://localhost:8080/game/user/register";
    private static String GAME_STATE_URL = "http://localhost:8080/game/gameState";
    private static String BETANDHASH_URL = "http://localhost:8080/game/user/bet";
    private static String SEND_NUMBER_URL = "http://localhost:8080/game/user/sendNumber";
    private static String GET_WINNER_URL = "http://localhost:8080/game/winner";

    OkHttpClient client;

    public ServerRequestHandler() {
        client = new OkHttpClient();
    }

    private void get(String url, Map<String,String> params, Callback responseCallback, boolean post) {
        HttpUrl.Builder httpBuilder = HttpUrl.parse(url).newBuilder();
        if (params != null) {
            for(Map.Entry<String, String> param : params.entrySet()) {
                httpBuilder.addQueryParameter(param.getKey(),param.getValue());
            }
        }

        Request request;
        if (post) {
            request = new Request.Builder().url(httpBuilder.build()).post(RequestBody.create(MediaType.parse("application/json"), "")).build();
        } else {
            request = new Request.Builder().url(httpBuilder.build()).get().build();
        }

        this.client.newCall(request).enqueue(responseCallback);
    }

    public void registerUser(String userId, Callback responseCallback) {
        HashMap<String, String> map = new HashMap<>();
        map.put("userId", userId);
        get(REGISTER_URL, map, responseCallback, true);
    }

    public void sendHashAndBets(String hash, String bet, String userId, Callback responseCallback) {
        HashMap<String, String> map = new HashMap<>();
        map.put("bet", bet);
        map.put("hash", hash);
        map.put("userId", userId);
        get(BETANDHASH_URL, map, responseCallback, true);
    }

    public void sendRealValue(String userId, String value, Callback responseCallback) {
        HashMap<String, String> map = new HashMap<>();
        map.put("number", value);
        map.put("userId", userId);
        get(SEND_NUMBER_URL, map, responseCallback, true);
    }

    public void getWinner(Callback responseCallback) {
        get(GET_WINNER_URL, null, responseCallback, false);
    }

    public void getGameState(Callback callback) {
        get(GAME_STATE_URL, null, callback, false);
    }
}
