package com.sajad.aideveloperagent.service;
import com.sajad.aideveloperagent.model.Analysis;
import java.util.List;
public interface AiGateway {
    Analysis analyse(String service, List<String> evidence);
    String chat(String message);
}
