package lu.uni.distributedsystems.project.common;

public class Match {
	
	private static int count = 0;
	private int id = 0;
	private String bookieID;
	private String teamA;
	private float oddsA;
	private String teamB; 
	private float oddsB;
	private float oddsDraw;
	private int limit;
	private boolean opened;
	
	public Match(String bookieID, String teamA, float oddsA, String teamB, float oddsB, float oddsDraw, int limit){
		id = ++count;
		this.bookieID = bookieID;
		this.teamA = teamA;
		this.oddsA = oddsA;
		this.teamB = teamB;
		this.oddsB = oddsB;
		this.oddsDraw = oddsDraw;
		this.limit = limit;
		opened = true;
	}

	public int getId() {
		return id;
	}

	public String getBookieID() {
		return bookieID;
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
	
	public float getOddsDraw() {
		return oddsDraw;
	}

	public void setOddsDraw(float oddsDraw) {
		this.oddsDraw = oddsDraw;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}
	
	public boolean isOpened() {
		return opened;
	}

	public void closeGame() {
		opened = false;
	}
	
	@Override
	public String toString(){
		return "Bookie " + bookieID + ": Match " + id + " between team " + teamA + " (odds: " + oddsA + ") and team " + teamB + 
				" (odds: " + oddsB + "), odds for a draw: " + oddsDraw + " limit: " + limit;
	}
	

}
