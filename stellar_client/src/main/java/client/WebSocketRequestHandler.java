package client;

import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.RestTemplateXhrTransport;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class WebSocketRequestHandler {
    private static String WEBSOCKET_URI = "ws://localhost:8080/ws-connect";
    WebSocketStompClient stompClient;

    public WebSocketRequestHandler() {
        WebSocketClient client = new StandardWebSocketClient();
        stompClient = new WebSocketStompClient(client);

        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        stompClient.connect(WEBSOCKET_URI, new StompSessionHandler() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                System.out.println("Connected");
                session.subscribe("/topic/gameState", new StompSessionHandler() {
                    @Override
                    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                        System.out.println("Connected to topic");
                    }

                    @Override
                    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {

                    }

                    @Override
                    public void handleTransportError(StompSession session, Throwable exception) {

                    }

                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return String.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        System.out.println("Received message");
                    }
                });
                session.send("/app/chat", "hello");
            }

            @Override
            public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {

            }

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {

            }

            @Override
            public Type getPayloadType(StompHeaders headers) {
                return null;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {

            }
        });
    }

    public void close() {
        stompClient.stop();
    }
}
