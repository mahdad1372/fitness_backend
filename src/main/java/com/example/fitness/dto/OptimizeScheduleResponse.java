package com.example.fitness.dto;

import java.util.List;

public class OptimizeScheduleResponse {

    private List<SessionRequestDto> acceptedSessions;
    private List<SessionRequestDto> rejectedSessions;
    private Double totalRevenue;
    private Integer requestsConsidered;
    // How many Branch & Bound search nodes it took to find the exact optimum -
    // exposed so the UI can show it as evidence this is genuinely searching a
    // decision tree, not just running a fixed-time DP like the other planners.
    private Long nodesExplored;

    public List<SessionRequestDto> getAcceptedSessions() { return acceptedSessions; }
    public void setAcceptedSessions(List<SessionRequestDto> acceptedSessions) { this.acceptedSessions = acceptedSessions; }

    public List<SessionRequestDto> getRejectedSessions() { return rejectedSessions; }
    public void setRejectedSessions(List<SessionRequestDto> rejectedSessions) { this.rejectedSessions = rejectedSessions; }

    public Double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(Double totalRevenue) { this.totalRevenue = totalRevenue; }

    public Integer getRequestsConsidered() { return requestsConsidered; }
    public void setRequestsConsidered(Integer requestsConsidered) { this.requestsConsidered = requestsConsidered; }

    public Long getNodesExplored() { return nodesExplored; }
    public void setNodesExplored(Long nodesExplored) { this.nodesExplored = nodesExplored; }
}