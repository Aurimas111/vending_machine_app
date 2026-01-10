package vendingmachine.security;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;

import java.util.ArrayList;

@Component
public class WebSocketAuthListener {

    private final JwtValidation jwtValidation;

    public WebSocketAuthListener(JwtValidation jwtValidation) {
        this.jwtValidation = jwtValidation;
    }

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        String authHeader = accessor.getFirstNativeHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing JWT in WebSocket CONNECT");
        }

        JwtValidationResult result =
                jwtValidation.validateToken(authHeader.substring(7));

        if (!result.isValid()) {
            throw new IllegalArgumentException("Invalid JWT in WebSocket CONNECT");
        }

        accessor.setUser(
                new UsernamePasswordAuthenticationToken(
                        result.getUserConfig().getOwnerWalletAddress(),
                        null,
                        new ArrayList<>()
                )
        );
    }
}
