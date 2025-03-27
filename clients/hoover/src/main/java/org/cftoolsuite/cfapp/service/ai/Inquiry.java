package org.cftoolsuite.cfapp.service.ai;

import java.util.List;

public record Inquiry(String question, List<String> tools) {}
