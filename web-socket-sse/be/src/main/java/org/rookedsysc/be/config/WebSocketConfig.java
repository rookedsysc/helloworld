package org.rookedsysc.be.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트가 메시지를 구독할 endpoint의 prefix 설정
        registry.enableSimpleBroker("/topic");
        
        // 클라이언트가 메시지를 발행할 수 있는 endpoint의 prefix 설정
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // "/ws-game-events" endpoint를 등록하고, SockJS 폴백 옵션 활성화
        registry.addEndpoint("/ws-game-events")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
} 