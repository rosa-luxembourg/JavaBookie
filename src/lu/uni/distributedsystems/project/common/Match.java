package lu.uni.distributedsystems.project.common;

public class Match {
	
	private static int count = 0;
	private int id = 0;
	private String teamA;
	private float oddsA;
	private String teamB; 
	private float oddsB; 
	private int limit;
	
	public Match(String teamA, float oddsA, String teamB, float oddsB, int limit){
		id = ++count;
		this.teamA = teamA;
		this.oddsA = oddsA;
		this.teamB = teamB;
		this.oddsB = oddsB;
		this.limit = limit;
	}

	public int getId() {
		return id;
	}

	public String getTeamA() {
		return teamA;
	}

	public float getOddsA() {
		return oddsA;
	}

	public void setOddsA(float oddsA) {
		this.oddsA = oddsA;
	}

	public String getTeamB() {
		return teamB;
	}

	public float getOddsB() {
		return oddsB;
	}

	public void setOddsB(float oddsB) {
		this.oddsB = oddsB;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}
	
	

}
