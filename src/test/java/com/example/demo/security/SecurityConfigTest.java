package com.example.demo.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        properties = {
                "JWT_SECRET=ThisIsAValidTestSecretKeyForJwt123456789",
                "knowledge-base.initializer.enabled=false"
        }
)
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldAllowExternalTestEndpointWithoutAuthentication()
            throws Exception {

        mockMvc.perform(
                get("/api/test/external")
        ).andExpect(status().isOk());
    }

    @Test
    void shouldRejectProtectedEndpointWithoutAuthentication()
            throws Exception {

        mockMvc.perform(
                get("/api/chat/orders/1001")
        ).andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectChatEndpointWithoutAuthentication()
            throws Exception {

        mockMvc.perform(
                post("/api/chat")
                        .contentType("application/json")
                        .content("""
                                {
                                    "conversationId": "test",
                                    "message": "Hello"
                                }
                                """)
        ).andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectKnowledgeEndpointWithoutAuthentication()
            throws Exception {

        mockMvc.perform(
                get("/api/knowledge/search")
                        .param("query", "return policy")
        ).andExpect(status().isForbidden());
    }
}