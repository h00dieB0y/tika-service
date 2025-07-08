package engineer.mkitsoukou.tika.application.auth.model;

import java.util.Set;

public record AuthSubject(String userId, Set<String> roles) {}
