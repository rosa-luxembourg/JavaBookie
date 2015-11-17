package lu.uni.distributedsystems.project.common;


// These are some of the different states that a place bet can result in.
public enum PlaceBetResult {
	
	ACCEPTED,
	REJECTED_UNKNOWN_MATCH,
	REJECTED_CLOSED_MATCH,
	REJECTED_UNKNOWN_TEAM,
	REJECTED_ALREADY_PLACED_BET,
	REJECTED_LIMIT_EXCEEDED,
	REJECTED_ODDS_MISMATCH

}
