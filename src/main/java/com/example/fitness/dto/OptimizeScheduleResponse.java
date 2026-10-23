package com.example.fitness.dto;

import java.util.List;

public class OptimizeScheduleResponse {

    private List<SessionRequestDto> acceptedSessions;
    private List<SessionRequestDto> rejectedSessions;
    private Integer requestsConsidered;
    private Long nodesExplored;

    public List<SessionRequestDto> getAcceptedSessions() { return acceptedSessions; }
    public void setAcceptedSessions(List<SessionRequestDto> acceptedSessions) { this.acceptedSessions = acceptedSessions; }
    public List<SessionRequestDto> getRejectedSessions() { return rejectedSessions; }
    public void setRejectedSessions(List<SessionRequestDto> rejectedSessions) { this.rejectedSessions = rejectedSessions; }
    public Integer getRequestsConsidered() { return requestsConsidered; }
    public void setRequestsConsidered(Integer requestsConsidered) { this.requestsConsidered = requestsConsidered; }
    public Long getNodesExplored() { return nodesExplored; }
    public void setNodesExplored(Long nodesExplored) { this.nodesExplored = nodesExplored; }
}